 package com.kii.cloud.board;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.kii.cloud.board.cache.TopicCache;
 import com.kii.cloud.board.sdk.KiiBoardClient;
 import com.kii.cloud.board.utils.ProgressingDialog;
 import com.kii.cloud.storage.KiiUser;
 import com.kii.cloud.storage.callback.KiiUserCallBack;
 import com.kii.cloud.storage.exception.CloudExecutionException;
 
 public class LoginActivity extends Activity {
 
     private TextView mUserNameView;
     private TextView mPwdView;
 
     ProgressingDialog progressing;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.login);
         KiiBoardClient.getInstance();
         mUserNameView = (TextView) findViewById(R.id.sync_login_username_edit);
         mPwdView = (TextView) findViewById(R.id.sync_login_password_edit);
         progressing = new ProgressingDialog(this);
     }
 
     @Override
     public void onResume() {
         super.onResume();
         String username = KiiBoardClient.getLoginUserName(this);
         mUserNameView.setText(username);
 
         String pwd = KiiBoardClient.getLoginUserPassword(this);
         mPwdView.setText(pwd);
     }
 
     public void handleSignUp(View v) {
         Intent intent = new Intent(this, SignupActivity.class);
         startActivity(intent);
         finish();
     }
 
     public void handleLogin(View v) {
         String username = mUserNameView.getText().toString();
         String pwd = mPwdView.getText().toString();
 
         int token = asyncUserLogin(username, pwd);
         progressing.showProcessing(token, "User logining!");
     }
 
     public void clearThreadCache() {
         getContentResolver().delete(TopicCache.CONTENT_URI, null, null);
     }
 
     private int asyncUserLogin(final String username, final String pwd) {
 
         int token = KiiUser.logIn(new KiiUserCallBack() {
             @Override
             public void onLoginCompleted(int token, boolean success,
                     KiiUser user, Exception exception) {
                 progressing.closeProgressDialog();
                 if (success) {
                     // check the old username, if username changed,
                     // clear the old cache!
                     String old_username = KiiBoardClient
                             .getLoginUserName(LoginActivity.this);
                     if (!old_username.equals(user.getUsername())) {
                         clearThreadCache();
                     }
 
                     KiiBoardClient.setLoginUserName(LoginActivity.this,
                             user.getUsername(), pwd);
                     Toast.makeText(LoginActivity.this, "Login successfully!",
                             Toast.LENGTH_SHORT).show();
 
                     Intent intent = new Intent(LoginActivity.this,
                             RemoteMessageListActivity.class);
                     startActivity(intent);
                     finish();
                 } else {
                     if (exception instanceof CloudExecutionException) {
                         CloudExecutionException cloudException = (CloudExecutionException) exception;
                         StringBuilder sb = new StringBuilder();
                         sb.append("Error:" + cloudException.getError());
                         sb.append("\n\n");
                         sb.append("Exception:" + cloudException.getException());
                         sb.append("\n\n");
                         sb.append("Error Details:"
                                 + cloudException.getErrorDetails());
                         String msg = sb.toString();
                         AlertDialog.Builder builder = new AlertDialog.Builder(
                                 LoginActivity.this);
                        builder.setTitle("Register Failed")
                                 .setMessage(msg)
                                 .setNegativeButton(
                                         getString(android.R.string.ok), null)
                                 .show();
                     }
                 }
 
             }
 
         }, username, pwd);
 
         return token;
 
     }
 }
