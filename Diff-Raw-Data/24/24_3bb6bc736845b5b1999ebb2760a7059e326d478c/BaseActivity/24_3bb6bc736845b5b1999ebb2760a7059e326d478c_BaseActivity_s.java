 package com.lovamimi;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import com.facebook.SessionState;
 import com.mixpanel.android.mpmetrics.MixpanelAPI;
 
 public class BaseActivity extends Activity {
 
     private MixpanelAPI mixpanel;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mixpanel = MixpanelAPI.getInstance(this, "e516b8643dc2d4d9b1779d243b7db7e5");
     }
 
     protected void track(String eventName) {
         mixpanel.track("Android:" + eventName, null);
     }
 
     protected void error(String s) {
         Log.e(this.getClass().toString(), s);
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         mixpanel.flush();
     }
 
     public String getSessionId() {
         LovamimiApplication app = (LovamimiApplication) getApplication();
         return app.getSessionId();
     }
 
     public void setSessionId(String sessionId) {
         LovamimiApplication app = (LovamimiApplication) getApplication();
         app.setSessionId(sessionId);
     }
 
     private void lovamimiLogin(final Context context,
                                final Class nextActivityClass,
                                String fbSessionId) {
         new AsyncTask<String, Void, String>() {
             @Override
             protected void onPostExecute(String sessionId) {
                 super.onPostExecute(sessionId);
                 setSessionId(sessionId);
                 Intent intent = new Intent(context, nextActivityClass);
                 startActivity(intent);
             }
 
             @Override
             protected String doInBackground(String... strings) {
                 String fbSessionId = strings[0];
                 return com.lovamimi.Session.login(fbSessionId);
             }
         }.execute(fbSessionId);
     }
 
     private void tryLogin(final Context context, final Class nextActivityClass) {
         // Facebook login
         com.facebook.Session.openActiveSession(this, true, new com.facebook.Session.StatusCallback() {
             @Override
             public void call(com.facebook.Session session, SessionState state, Exception exception) {
                 lovamimiLogin(context, nextActivityClass, session.getAccessToken());
             }
         });
     }
 
     private void showLoginDialog(final Class nextActivityClass) {
         track("Showed Login Dialog");
         AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
         alertDialogBuilder.setTitle("ログイン");
         alertDialogBuilder.setMessage("匿名投稿といいね！をするには Facebook ログインが必要です");
         alertDialogBuilder.setPositiveButton("Facebook ログイン",
                 new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         track("Clicked button Login Dialog");
                         tryLogin(BaseActivity.this, nextActivityClass);
                     }
                 });
         alertDialogBuilder.setNegativeButton("キャンセル",
                 new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         track("Canceled Login Dialog");
                     }
                 });
         alertDialogBuilder.setCancelable(true);
         AlertDialog alertDialog = alertDialogBuilder.create();
         alertDialog.show();
     }
 
     protected void loginAndNextActivity(Class nextActivityClass) {
         if (getSessionId() == null) {
            showLoginDialog(PostCommentActivity.class);
         } else {
             Intent intent = new Intent(this, nextActivityClass);
             startActivity(intent);
            Secret secret = (Secret) getIntent().getExtras().get("secret");
            intent.putExtra("sid", secret.sid);
         }
     }
 }
