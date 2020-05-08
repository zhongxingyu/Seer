 package cstdr.ningningcat.util;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.text.TextUtils;
 import android.webkit.JsResult;
 import android.widget.EditText;
 import android.widget.RelativeLayout.LayoutParams;
 import cstdr.ningningcat.FavoriteActivity.DialogItemClickListener;
 import cstdr.ningningcat.FavoriteActivity.DialogRenameListener;
 import cstdr.ningningcat.MainActivity;
 import cstdr.ningningcat.R;
 import cstdr.ningningcat.constants.Constants;
 
 /**
  * 弹窗工具类
  * @author cstdingran@gmail.com
  */
 public class DialogUtil {
 
     private static Dialog jsAlertDialog; // 页面JS警告框
 
     private static Dialog jsConfirmDialog; // 页面JS确认框
 
     private static Dialog alertDialog; // 警告框
 
     private static Dialog noConnectDialog; // 无网状态框
 
     private static Dialog favoriteDialog; // 长按收藏夹Item弹出框
 
     private static Dialog renameDialog; // 显示重命名收藏页面的弹出框
 
     /** 手机设置页面ACTION **/
     private static final String ACTION_SETTINGS="android.settings.SETTINGS";
 
     /**
      * 显示JS警告框
      * @param context
      * @param message
      * @param result
      */
     public static void showJsAlertDialog(Context context, String message, final JsResult result) {
         if(jsAlertDialog != null && jsAlertDialog.isShowing()) {
             return;
         }
         jsAlertDialog=
             new AlertDialog.Builder(context).setTitle(R.string.title_alert).setMessage(message)
                 .setPositiveButton(android.R.string.ok, new OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         result.confirm();
                     }
                 }).create();
         jsAlertDialog.show();
 
     }
 
     /**
      * 显示JS确认框
      * @param context
      * @param message
      * @param result
      */
     public static void showJsConfirmDialog(Context context, String message, final JsResult result) {
         if(jsConfirmDialog != null && jsConfirmDialog.isShowing()) {
             return;
         }
         jsConfirmDialog=
             new AlertDialog.Builder(context).setTitle(R.string.title_confirm).setMessage(message)
                 .setPositiveButton(android.R.string.ok, new OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         result.confirm();
                     }
                 }).setNegativeButton(android.R.string.cancel, new OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         result.cancel();
                     }
                 }).create();
         jsConfirmDialog.show();
 
     }
 
     /**
      * 显示普通提示窗 TODO
      * @param context
      * @param message
      */
     public static void showAlertDialog(Context context, String message) {
         if(alertDialog != null && alertDialog.isShowing()) {
             return;
         }
         alertDialog=
             new AlertDialog.Builder(context).setTitle(R.string.title_alert).setMessage(message)
                 .setPositiveButton(android.R.string.ok, new OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                     }
                 }).create();
         alertDialog.show();
     }
 
     /**
      * 显示无网状态框
      * @param context
      */
     public static void showNoConnectDialog(final Context context) {
         if(noConnectDialog != null && noConnectDialog.isShowing()) {
             return;
         }
         noConnectDialog=
             new AlertDialog.Builder(context).setTitle(R.string.title_no_connect).setMessage(R.string.msg_no_connect)
                 .setPositiveButton(R.string.btn_cancel, new OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
 
                     }
                 }).setNegativeButton(R.string.btn_ok, new OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         MainActivity.getInstance().setNetworkMode(true); // 避免了设置完返回界面时二次提示
                         Intent intent=new Intent(ACTION_SETTINGS);
                         context.startActivity(intent);
                     }
                 }).create();
         noConnectDialog.show();
     }
 
     /**
      * 长按收藏夹Item弹出框
      * @param context
      * @param title
      * @param position
      * @param listener
      */
     public static void showFavoriteDialog(Context context, String title, final int position, final DialogItemClickListener listener) {
         if(favoriteDialog != null && favoriteDialog.isShowing()) {
             return;
         }
         favoriteDialog=
             new AlertDialog.Builder(context).setTitle(title)
                 .setItems(new String[]{"添加快捷方式到桌面", "设为首页", "重命名", "分享", "删除"}, new OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         listener.onClick(position, which);
                     }
                 }).create();
         favoriteDialog.show();
     }
 
     /**
      * 显示重命名收藏页面的弹出框
      * @param context
      * @param title
      * @param url
      * @param listener
      */
     public static void showRenameDialog(Context context, String title, final String url, final DialogRenameListener listener) {
         if(renameDialog != null && renameDialog.isShowing()) {
             return;
         }
         final EditText editText=new EditText(context);
         LayoutParams params=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
         editText.setLayoutParams(params);
         editText.setText(title);
        editText.setMaxHeight(1);
         renameDialog=
             new AlertDialog.Builder(context).setTitle(title).setView(editText)
                 .setPositiveButton(R.string.btn_cancel, new OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                     }
                 }).setNegativeButton(R.string.btn_ok, new OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         String titleStr=editText.getText().toString();
                         if(TextUtils.isEmpty(titleStr)) {
                             titleStr=Constants.TITLE_NULL;
                         } else if(titleStr.length() > 20) { // 数据库中定义title长度为20
                             titleStr=titleStr.substring(0, 20);
                         }
                         listener.onClick(titleStr, url);
                     }
                 }).create();
         renameDialog.show();
     }
 }
