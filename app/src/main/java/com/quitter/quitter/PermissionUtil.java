package com.quitter.quitter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.SettingService;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建者     lizhong
 * 创建时间   2018/4/10 17:41
 * 描述	      ${TODO}
 * <p>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   $权限申请工具类
 */

public class PermissionUtil {
    /**
     * 是否需要检查权限
     */
    public static boolean needCheckPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * 获取sd存储卡读写权限
     *
     * @return 是否已经获取权限，没有自动申请
     */
    public static boolean getExternalStoragePermissions(@NonNull Activity activity, int requestCode) {
        return requestPerssions(activity, requestCode, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    /**
     * 获取拍照权限
     *
     * @return 是否已经获取权限，没有自动申请
     */
    public static boolean getCameraPermissions(@NonNull Activity activity, int requestCode) {
        return requestPerssions(activity, requestCode, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    /**
     * 获取麦克风权限
     *
     * @return 是否已经获取权限，没有自动申请
     */
    public static boolean getAudioPermissions(@NonNull Activity activity, int requestCode) {
        return requestPerssions(activity, requestCode, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    /**
     * 获取定位权限
     *
     * @return 是否已经获取权限，没有自动申请
     */
    public static boolean getLocationPermissions(@NonNull Activity activity, int requestCode) {
        return requestPerssions(activity, requestCode, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    /**
     * 获取读取联系人权限
     *
     * @return 是否已经获取权限，没有自动申请
     */
    public static boolean getContactsPermissions(@NonNull Activity activity, int requestCode) {
        return requestPerssions(activity, requestCode, Manifest.permission.READ_CONTACTS);
    }

    /**
     * 获取发送短信权限
     *
     * @return 是否已经获取权限，没有自动申请
     */
    public static boolean getSendSMSPermissions(@NonNull Activity activity, int requestCode) {
        return requestPerssions(activity, requestCode, Manifest.permission.SEND_SMS);
    }

    /**
     * 获取拨打电话权限
     *
     * @return 是否已经获取权限，没有自动申请
     */
    public static boolean getCallPhonePermissions(@NonNull Activity activity, int requestCode) {
        return requestPerssions(activity, requestCode, Manifest.permission.CALL_PHONE);
    }


    public static List<String> getDeniedPermissions(@NonNull Activity activity, @NonNull String... permissions) {
        if (!needCheckPermission()) {
            return null;
        }
        List<String> deniedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission);
            }
        }
        if (!deniedPermissions.isEmpty()) {
            return deniedPermissions;
        }

        return null;
    }

    /**
     * 是否拥有权限
     */
    public static boolean hasPermissons(@NonNull Activity activity, @NonNull String... permissions) {
        if (!needCheckPermission()) {
            return true;
        }
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否拒绝了再次申请权限的请求（点击了不再询问）
     */
    public static boolean deniedRequestPermissonsAgain(@NonNull Activity activity, @NonNull String... permissions) {
        if (!needCheckPermission()) {
            return false;
        }
        List<String> deniedPermissions = getDeniedPermissions(activity, permissions);
        for (String permission : deniedPermissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_DENIED) {

                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    //当用户之前已经请求过该权限并且拒绝了授权这个方法返回true
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 打开app详细设置界面<br/>
     * <p>
     * 在 onActivityResult() 中没有必要对 resultCode 进行判断，因为用户只能通过返回键才能回到我们的 App 中，<br/>
     * 所以 resultCode 总是为 RESULT_CANCEL，所以不能根据返回码进行判断。<br/>
     * 在 onActivityResult() 中还需要对权限进行判断，因为用户有可能没有授权就返回了！<br/>
     */
    public static void startApplicationDetailsSettings(@NonNull Activity activity, int requestCode) {
        Toast.makeText(activity, "点击权限，请打开存储权限", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivityForResult(intent, requestCode);


    }

    /**
     * 申请权限<br/>
     * 使用onRequestPermissionsResult方法，实现回调结果或者自己普通处理
     *
     * @return 是否已经获取权限
     */
    public static boolean requestPerssions(Activity activity, int requestCode, String... permissions) {

        if (!needCheckPermission()) {
            return true;
        }

        if (!hasPermissons(activity, permissions)) {
            if (deniedRequestPermissonsAgain(activity, permissions)) {
                startApplicationDetailsSettings(activity, requestCode);
                //返回结果onActivityResult
            } else {
                List<String> deniedPermissions = getDeniedPermissions(activity, permissions);
                if (deniedPermissions != null) {
                    ActivityCompat.requestPermissions(activity, deniedPermissions.toArray(new String[deniedPermissions.size()]), requestCode);
                    //返回结果onRequestPermissionsResult
                }
            }
            return false;
        }
        return true;
    }

    /**
     * 申请权限返回方法
     */
    public static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                  @NonNull int[] grantResults, @NonNull OnRequestPermissionsResultCallbacks callBack) {
        // Make a collection of granted and denied permissions from the request.
        List<String> granted = new ArrayList<>();
        List<String> denied = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            String perm = permissions[i];
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                granted.add(perm);
            } else {
                denied.add(perm);
            }
        }

        if (null != callBack) {
            if (!granted.isEmpty()) {
                callBack.onPermissionsGranted(requestCode, granted, denied.isEmpty());
            }
            if (!denied.isEmpty()) {
                callBack.onPermissionsDenied(requestCode, denied, granted.isEmpty());
            }
        }


    }


    /**
     * 申请权限返回
     */
    //    public interface OnRequestPermissionsResultCallbacks extends ActivityCompat.OnRequestPermissionsResultCallback {
    public interface OnRequestPermissionsResultCallbacks {

        /**
         * @param isAllGranted 是否全部同意
         */
        void onPermissionsGranted(int requestCode, List<String> perms, boolean isAllGranted);

        /**
         * @param isAllDenied 是否全部拒绝
         */
        void onPermissionsDenied(int requestCode, List<String> perms, boolean isAllDenied);

    }

    /*请求相关权限*/
    public static void requestPermissions(String[] permissions, final Activity context, final String message, final int requestCode, final onGrantedPermissionsResultCallbacks callbacks) {
        AndPermission.with(context)
                .permission(permissions)
                .onGranted(new Action() {
                    @Override
                    public void onAction(List<String> permissions) {
                        if (null != callbacks) {
                            callbacks.onGranted();
                        }

                    }
                })
                .onDenied(new Action() {
                    @Override
                    public void onAction(@NonNull List<String> permissions) {
                        if (AndPermission.hasAlwaysDeniedPermission(context, permissions)) {
                            showSetting(permissions, requestCode, context);
                        } else {
                            if(!TextUtils.isEmpty(message)) {
//                                DialogFactory.showSingleBtnDialog(context, message, "知道了");
                            }
                        }
                    }
                }).start();
    }

    /*提示用户去设置中打开权限*/
    public static void showSetting(final List<String> permissions, final int requestCode, Activity mContext) {
        List<String> permissionNames = Permission.transformText(mContext, permissions);
        String message = mContext.getString(R.string.message_permission_always_failed, TextUtils.join("、", permissionNames));
        final SettingService settingService = AndPermission.permissionSetting(mContext);
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        Dialog dialog = alert.setTitle("权限提示")
                .setMessage(message)
                .setPositiveButton(
                        "设置",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which) {
                                dialog.dismiss();
                                settingService.execute(requestCode);
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        settingService.cancel();
                    }
                }).create();
        dialog.setCancelable(false);
        dialog.show();
    }


    public interface onGrantedPermissionsResultCallbacks {
        void onGranted();
    }


    /**
     * android 6.0 以上需要动态申请权限
     */
    public static void initPermission(Activity activity, String permissions[]) {

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(activity, perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限.
            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(activity, toApplyList.toArray(tmpList), 123);
        }

    }
}
