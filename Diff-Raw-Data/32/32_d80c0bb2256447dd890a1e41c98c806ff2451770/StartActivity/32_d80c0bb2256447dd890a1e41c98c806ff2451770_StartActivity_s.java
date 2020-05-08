 package com.kii.demo.sync.ui;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.kii.cloud.sync.KiiSyncClient;
 import com.kii.demo.sync.R;
 import com.kii.demo.sync.tasks.ChangePwdTask;
 import com.kii.demo.sync.tasks.LoginTask;
 import com.kii.demo.sync.tasks.LogoutTask;
 import com.kii.demo.sync.tasks.RegisterTask;
 import com.kii.demo.sync.utils.UiUtils;
 import com.kii.demo.sync.utils.Utils;
 import com.kii.sync.KiiFile;
 import com.kii.sync.SyncMsg;
 import com.kii.sync.SyncPref;
 
 public class StartActivity extends Activity {
     protected static final String TAG = "StartActivity";
 
     public static final String ACTION_ENTER_PASSWORD = "com.kii.demo.sync.ENTER_PASSWORD";
     public static final String ACTION_LOGOUT = "com.kii.demo.sync.LOGOUT";
 
     EditText mUsr = null;
     EditText mPwd = null;
 
     ProgressBar mProgressStatus = null;
     TextView mProgressMsg = null;
     TextView mLastSyncTime = null;
     TextView mStorage = null;
     TextView mServerSite = null;
     TextView mUserInfo = null;
     Button mRegister = null;
     Button mViewer = null;
     Button mLogin = null;
     Button mLogout = null;
     Button mSync = null;
     Button mChangePwd = null;
     Button mSetting = null;
 
     private static Context mContext;
     private static StartActivity mActivity = null;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         mContext = this.getApplicationContext();
         mActivity = this;
 
         SyncPref.init(this);
 
         mStorage = (TextView) findViewById(R.id.storage);
         mUsr = (EditText) findViewById(R.id.username);
         mPwd = (EditText) findViewById(R.id.password);
 
         mProgressStatus = (ProgressBar) findViewById(R.id.progress);
         mProgressMsg = (TextView) findViewById(R.id.progress_text);
         mLastSyncTime = (TextView) findViewById(R.id.lastSyncTime);
         mServerSite = (TextView) findViewById(R.id.server);
         mUserInfo = (TextView) findViewById(R.id.kiiid);
 
         mRegister = (Button) findViewById(R.id.register);
         mRegister.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 registerDemo();
             }
         });
 
         mLogin = (Button) findViewById(R.id.login);
         mLogin.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 loginDemo();
             }
         });
 
         mLogout = (Button) findViewById(R.id.logout);
         mLogout.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 lougoutDemo();
             }
         });
 
         mSync = (Button) findViewById(R.id.sync);
         mSync.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 manualSync();
             }
         });
 
         mChangePwd = (Button) findViewById(R.id.changePwd);
         mChangePwd.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 changePwd();
             }
         });
 
         mViewer = (Button) findViewById(R.id.viewer);
         mViewer.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (KiiSyncClient.getInstance(mContext) != null) {
 
                     Intent intent = new Intent(mContext, FileTabActivity.class);
                     intent.putExtra(FileTabActivity.TAB_INDEX_EXTRA,
                             FileTabActivity.TAB_INDEX_CLOUD);
                     startActivity(intent);
                     finish();
                 } else {
                     Toast.makeText(mContext, "Kii Client is not ready",
                             Toast.LENGTH_SHORT).show();
                 }
             }
         });
 
         mSetting = (Button) findViewById(R.id.launch_settings);
         mSetting.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(mContext, SettingsActivity.class);
                 startActivity(intent);
             }
         });
 
         updateView();
 
         Intent i = getIntent();
         if (i != null && i.getAction() != null) {
             if (i.getAction().compareToIgnoreCase(ACTION_ENTER_PASSWORD) == 0) {
                 updatePwd();
             } else if (i.getAction().compareToIgnoreCase(ACTION_LOGOUT) == 0) {
                 lougoutDemo();
             }
         }
     }
 
     void updateView() {
         if (SyncPref.isLoggedIn()) {
             Intent i = getIntent();
             if (i == null
                     || i.getAction() == null
                     || (i.getAction().compareToIgnoreCase(
                             Intent.ACTION_CONFIGURATION_CHANGED) != 0 && i
                             .getAction().compareToIgnoreCase(
                                     ACTION_ENTER_PASSWORD) != 0)) {
                 Intent intent = new Intent(mContext, FileTabActivity.class);
                 intent.putExtra(FileTabActivity.TAB_INDEX_EXTRA,
                         FileTabActivity.TAB_INDEX_CLOUD);
                 startActivity(intent);
                 finish();
             } else {
                 mUsr.setText(SyncPref.getUsername());
                 mPwd.setText(SyncPref.getPassword());
                 mUsr.setEnabled(false);
 
                 mRegister.setEnabled(false);
                 mLogin.setEnabled(true);
                 mLogin.setText("ReLogIn");
                 mViewer.setEnabled(true);
                 mLogout.setEnabled(true);
                 mSync.setEnabled(true);
                 mChangePwd.setEnabled(true);
                 mSetting.setEnabled(true);
                 updateSyncStatus();
             }
         } else {
             Toast.makeText(mContext, "Register or Login", Toast.LENGTH_SHORT)
                     .show();
             mRegister.setEnabled(true);
             mLogin.setEnabled(true);
             mLogin.setText("Log In");
             mUsr.setEnabled(true);
             mViewer.setEnabled(false);
             mLogout.setEnabled(false);
             mSync.setEnabled(false);
             mChangePwd.setEnabled(false);
             mSetting.setEnabled(false);
         }
     }
 
     protected void updatePwd() {
         final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                 .setTitle("Verification has failed")
                 .setMessage("Enter New Password:")
                 .setView(input)
                 .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
                         String newPassword = input.getText().toString();
                         if (TextUtils.isEmpty(newPassword)) {
                             Toast.makeText(mContext,
                                     "Password can't be nothing.",
                                     Toast.LENGTH_SHORT).show();
                         } else {
                             updatePwd(newPassword);
                         }
                     }
                 })
                 .setNegativeButton("Cancel",
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog,
                                     int whichButton) {
                                 Toast.makeText(mContext,
                                         "Cancel Enter Password.",
                                         Toast.LENGTH_SHORT).show();
                             }
                        }).show();
     }
 
     private void updatePwd(String newPassword) {
         mPwd.setText(newPassword);
         LoginTask task = new LoginTask(this);
         task.execute(mUsr.getText().toString(), newPassword);
     }
 
     protected void changePwd() {
         final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                 .setTitle("Change Password")
                 .setMessage("Enter new password:")
                 .setView(input)
                 .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
                         String newPassword = input.getText().toString();
                         if (TextUtils.isEmpty(newPassword)) {
                             Toast.makeText(mContext,
                                     "Password can't be nothing.",
                                     Toast.LENGTH_SHORT).show();
                         } else {
                             changePwd(newPassword);
                         }
                     }
                 })
                 .setNegativeButton("Cancel",
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog,
                                     int whichButton) {
                                 Toast.makeText(mContext,
                                         "Cancel Change Password.",
                                         Toast.LENGTH_SHORT).show();
                             }
                        }).show();
     }
 
     protected void changePwd(String newPassword) {
         ChangePwdTask task = new ChangePwdTask(mActivity);
         task.execute(mPwd.getText().toString(), newPassword);
     }
 
     protected void manualSync() {
         SyncTask task = new SyncTask(this, mUsr.getText().toString(), mPwd
                 .getText().toString());
         task.execute();
     }
 
     protected void lougoutDemo() {
         LogoutTask task = new LogoutTask(this);
         task.execute();
     }
 
     private void registerDemo() {
         String country = "US"; // Country is a valid ISO 3166-1 alpha-2 code
         // (two-letter code).
         String nickName = "Tester";
         String mobile = ""; // "+86-139-1604-5378";
 
         RegisterTask task = new RegisterTask(this);
         task.execute(mUsr.getText().toString(), mPwd.getText().toString(),
                 country, nickName, mobile);
     }
 
     private void loginDemo() {
         LoginTask task = new LoginTask(this);
         task.execute(mUsr.getText().toString(), mPwd.getText().toString());
     }
 
     public static void showAlertDialog(Context context, String title,
             int errorCode) {
         showAlertDialog(context, title, getErrorMsg(errorCode, context));
     }
 
     static void showToast(Context context, String message) {
         Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
     }
 
     private static AlertDialog mDialog = null;
 
     public static void showAlertDialog(Context context, String title,
             CharSequence msg) {
 
         AlertDialog.Builder builder = new AlertDialog.Builder(context);
         builder.setMessage(msg).setTitle(title).setCancelable(true)
                 .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                     }
                 });
         mDialog = builder.create();
         mDialog.show();
 
         mActivity.updateView();
     }
 
     @Override
     public void onDestroy() {
         if (mDialog != null) {
             mDialog.dismiss();
         }
         super.onDestroy();
     }
 
     /**
      * Print the List of file and list of file that have been trashed
      * 
      * @param kiiClient
      * @return
      */
     static public StringBuilder showKiiFileList(KiiSyncClient kiiClient) {
         StringBuilder msg = new StringBuilder();
 
         msg.append("Normal:\n");
         KiiFile[] files = kiiClient.getBackupFiles();
         int ct = 1;
         if (files != null && files.length > 0) {
             for (KiiFile file : files) {
                 Log.e(StartActivity.TAG + " ALL", "" + file);
                 msg.append(ct + ":  " + Utils.getStatus(file, mContext) + ":"
                         + getKiiFilePath(file) + "\n");
                 ct++;
             }
         }
 
         msg.append("Trash:\n");
         KiiFile[] trashFiles = kiiClient.getTrashFiles();
         if (trashFiles != null && trashFiles.length > 0) {
             for (KiiFile file : trashFiles) {
                 Log.e(StartActivity.TAG, file.getAppData() + "; remotePath: "
                         + file.getRemotePath());
                 msg.append(ct + ":  " + Utils.getStatus(file, mContext) + ":"
                         + file.getAppData() + ":" + getKiiFilePath(file) + "\n");
                 ct++;
             }
         }
         return msg;
     }
 
     /**
      * Get the KiiFile path which can download the file
      * 
      * @param file
      * @return
      */
     static String getKiiFilePath(KiiFile file) {
         if (file.isFile()) {
             String path = file.getLocalPath();
             if (path == null)
                 return file.getAvailableURL().toString();
             else
                 return path;
         } else {
             return file.getBucketName();
         }
     }
 
     /**
      * Set the number of files that are backed up o the cloud
      * 
      * @param kiiClient
      */
     private void updateSyncStatus() {
 
         KiiSyncClient kClient = KiiSyncClient.getInstance(mContext);
 
         if (kClient == null)
             return;
 
         int completed = kClient.getSyncCount();
         int total = completed + kClient.getUnsyncCount();
 
         if (total == completed) {
             mProgressStatus.setProgress(mProgressStatus.getMax());
         } else {
             mProgressStatus.setMax(total);
             mProgressStatus.setProgress(completed);
         }
         double percentage;
         if (total == 0)
             percentage = 0;
         else
             percentage = ((double) (completed) / (double) total) * 100;
 
         String progressMsg = String.format(
                 "%3.2f%s  (%d out of %d items are sync)", percentage, "%",
                 completed, total);
         mProgressMsg.setText(progressMsg);
 
         mStorage.setText("Storage Uasge is "
                 + Long.toString(kClient.getStorageUsage()) + "bytes");
 
         mLastSyncTime.setText(UiUtils.getLastSyncTime(this));
 
         // print the server location and SDK version
         mServerSite.setText("Connect to Server:" + SyncPref.getServerSite()
                 + " SDK Ver:" + SyncPref.getSDKVersion());
 
         // print the User Id and App ID
         mUserInfo.setText("KiiID:" + SyncPref.getKiiId() + "\nAppID:"
                 + SyncPref.getAppName());
     }
 
     /**
      * Convert the error code to error message
      * 
      * @param code
      * @param context
      * @return
      */
     static public String getErrorMsg(int code, Context context) {
         switch (code) {
 
             case SyncMsg.OK:
                 return "Successful";
 
             case SyncMsg.PFS_SYNCRESULT_FORCE_STOP:
             case SyncMsg.PFS_SYNCRESULT_REQUEST_FORCE_STOP:
                 return context.getString(R.string.msg_ERROR_FORCE_STOP);
 
             case SyncMsg.PFS_SYNCRESULT_RUNNING:
             case SyncMsg.PFS_SYNCRESULT_BUSY:
                 return context.getString(R.string.msg_ERROR_BUSY);
 
             case SyncMsg.ERROR_ALREADY_KII_USER:
                 return context.getString(R.string.msg_ERROR_ALREADY_KII_USER);
 
             case SyncMsg.ERROR_GET_ACCOUNTS:
                 return context.getString(R.string.msg_ERROR_GET_ACCOUNTS);
 
             case SyncMsg.ERROR_AUTHENTICAION_ERROR:
                 return context
                         .getString(R.string.msg_ERROR_AUTHENTICAION_ERROR);
             case SyncMsg.ERROR_NO_ACCOUNT:
                 return context.getString(R.string.msg_ERROR_NO_ACCOUNT);
             case SyncMsg.ERROR_DIFFERENT_USERNAME:
                 return context.getString(R.string.msg_ERROR_DIFFERENT_USERNAME);
             case SyncMsg.ERROR_NON_VERIFIED_USERNAME:
                 return context
                         .getString(R.string.msg_ERROR_NON_VERIFIED_USERNAME);
             case SyncMsg.ERROR_INVALID_INPUT:
                 return context.getString(R.string.msg_ERROR_INVALID_INPUT);
             case SyncMsg.ERROR_NO_CONNECTION:
                 return context.getString(R.string.msg_ERROR_NO_CONNECTION);
             case SyncMsg.ERROR_NO_HOST:
                 return context.getString(R.string.msg_ERROR_NO_HOST);
             case SyncMsg.ERROR_TIMEOUT:
                 return context.getString(R.string.msg_ERROR_TIMEOUT);
             case SyncMsg.ERROR_IO:
                 return context.getString(R.string.msg_ERROR_IO);
             case SyncMsg.ERROR_PROTOCOL:
                 return context.getString(R.string.msg_ERROR_PROTOCOL);
             case SyncMsg.ERROR_SERVER_HARD_ERROR:
                 return context.getString(R.string.msg_ERROR_SERVER_HARD_ERROR);
             case SyncMsg.ERROR_SERVER_TEMP_ERROR:
                 return context.getString(R.string.msg_ERROR_SERVER_TEMP_ERROR);
             case SyncMsg.ERROR_GET_SITE_ERROR:
                 return context.getString(R.string.msg_ERROR_GET_SITE_ERROR);
             case SyncMsg.ERROR_JSON:
                 return context.getString(R.string.msg_ERROR_JSON);
             case SyncMsg.ERROR_FILE_NULL:
                 return context.getString(R.string.msg_ERROR_UPLOAD_FILES);
             default:
                 return String.format(
                         context.getString(R.string.msg_ERROR_OTHERS), code);
         }
     }
 
     public class SyncTask extends AsyncTask<String, Void, Integer> {
 
         static final String TAG = "SyncTask";
 
         ProgressDialog dialog = null;
 
         private Context mContext;
         private String mEmail;
         private String mPassword;
 
         StringBuilder msg = new StringBuilder();
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             dialog = new ProgressDialog(mContext);
             dialog.setTitle(TAG);
             dialog.setMessage("...");
             dialog.setCancelable(false);
             dialog.setButton("Cancel", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     KiiSyncClient kiiClient = KiiSyncClient
                             .getInstance(mContext);
                     if (kiiClient != null) {
                         kiiClient.suspend();
                     }
                 }
             });
             dialog.show();
 
             if (handler != null) {
                 handler.sendEmptyMessageDelayed(0, 100);
             }
         }
 
         private Handler handler = new Handler() {
             @Override
             public void handleMessage(Message msg) {
 
                 if (dialog != null && dialog.isShowing()) {
                     KiiSyncClient kiiClient = KiiSyncClient
                             .getInstance(mContext);
                     if (kiiClient != null) {
                         int progress = kiiClient.getOverallProgress();
                         String progressMsg = null;
                         if (progress > 0) {
                             progressMsg = "Sync in progress " + progress + "%";
                         } else if (progress == -1) {
                             progressMsg = "...";
                         } else {
                             progressMsg = StartActivity.getErrorMsg(progress,
                                     mContext);
                         }
                         dialog.setMessage(progressMsg);
                     }
                     handler.sendEmptyMessageDelayed(0, 1000);
                 }
             }
         };
 
         /**
      * 
      */
         public SyncTask(Context context, String email, String password) {
             mContext = context;
             mEmail = email;
             mPassword = password;
         }
 
         /**
          * {@inheritdoc}
          * 
          * @see android.os.AsyncTask#doInBackground(Params[])
          */
         @Override
         protected Integer doInBackground(String... params) {
             int result = SyncMsg.ERROR_SETUP;
 
             try {
                 KiiSyncClient syncClient = KiiSyncClient.getInstance(mContext);
                 result = syncClient.login(mEmail, mPassword);
                 if (result != SyncMsg.OK)
                     return result;
                 // execute the sync
                 result = syncClient.refresh();
                 if (result != SyncMsg.OK)
                     return result;
 
                 msg = StartActivity.showKiiFileList(syncClient);
 
             } catch (Exception e) {
                 Log.e(TAG, "", e);
             }
             return result;
         }
 
         /**
          * {@inheritdoc}
          * 
          * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
          */
         @Override
         protected void onPostExecute(Integer result) {
             if (dialog != null)
                 dialog.dismiss();
             if (result == SyncMsg.OK) {
                 StartActivity.showAlertDialog(mContext, TAG, msg);
             } else {
                 StartActivity.showAlertDialog(mContext, TAG, result);
             }
         }
 
     }
 }
