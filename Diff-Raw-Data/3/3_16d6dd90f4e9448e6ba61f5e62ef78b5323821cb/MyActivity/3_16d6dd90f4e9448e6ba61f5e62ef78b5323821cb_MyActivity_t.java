 package com.turbosocialpost;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.facebook.*;
 import com.facebook.internal.Utility;
 import com.facebook.model.GraphUser;
 import com.turbosocialpost.Maps.GoogleMapsActivity;
 import com.turbosocialpost.Twitter.PrepareRequestTokenActivity;
 import com.turbosocialpost.Twitter.TwitterUtils;
 import oauth.signpost.OAuth;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 public class MyActivity extends Activity {
 
     private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
 
     private Button postFacebookButton;
     private Button postTwitterButton;
     private Button mapsButton;
     private EditText postMessage;
     private TextView loginFacebookStatus;
     private TextView loginTwitterStatus;
     private String userNameFacebook;
     private String userNameTwitter;
     private SharedPreferences preferences;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         preferences = PreferenceManager.getDefaultSharedPreferences(this);
 
         postMessage = (EditText) findViewById(R.id.postField);
         loginFacebookStatus = (TextView) findViewById(R.id.login_status_facebook);
         loginTwitterStatus = (TextView) findViewById(R.id.login_status_twitter);
 
         postFacebookButton = (Button) findViewById(R.id.postFacebook);
         postFacebookButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 postToFacebook();
             }
         });
 
         postTwitterButton = (Button) findViewById(R.id.postTwitter);
         postTwitterButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 postToTwitter();
             }
         });
 
         mapsButton = (Button) findViewById(R.id.mapsButton);
         mapsButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), GoogleMapsActivity.class));
             }
         });
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         showUserLoginFacebook();
         showUserLoginTwitter();
     }
 
     private void postToFacebook() {
         Session session = Session.getActiveSession();
         if (session != null && session.isOpened()) {
             String message = getMessage();
             Bundle postParams = new Bundle();
             postParams.putString("message", message);
             Request request = new Request(session, "me/feed", postParams, HttpMethod.POST);
             RequestAsyncTask task = new RequestAsyncTask(request);
             task.execute();
             setEmptyText();
             postedToast();
         }
         else {
             notLoggedToast();
         }
     }
 
     public void postToTwitter() {
         if (TwitterUtils.isAuthenticated(preferences)) {
             try {
                 TwitterUtils.sendTweet(preferences, getMessage());
                 setEmptyText();
                 postedToast();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         } else {
             notLoggedToast();
         }
     }
 
     public void logInFacebook() {
         Session.openActiveSession(this, true, new Session.StatusCallback() {
             @Override
             public void call(Session session, SessionState state, Exception exception) {
                 if (session.isOpened()) {
                     Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
                         @Override
                         public void onCompleted(GraphUser user, Response response) {
                             if (user != null) {
                                 userNameFacebook = user.getName();
                                 loginFacebookStatus.setText(" " + userNameFacebook);
                             }
                         }
                     });
                 }
             }
         });
         checkPublishPermisison();
     }
 
     public void logInTwitter() {
         if (!TwitterUtils.isAuthenticated(preferences)) {
             Intent intent = new Intent(getApplicationContext(), PrepareRequestTokenActivity.class);
             startActivity(intent);
         } else {
             alreadyLoggedToast();
         }
     }
 
     private void checkPublishPermisison() {
         Session session = Session.getActiveSession();
         if (session != null && session.isOpened()) {
             List<String> permissions = session.getPermissions();
             if (!hasPublishPermissionFacebook(PERMISSIONS, permissions)) {
                 Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, PERMISSIONS);
                 session.requestNewPublishPermissions(newPermissionsRequest);
             }
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater menuInflater = getMenuInflater();
         menuInflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         switch (item.getItemId()) {
             case R.id.menu_login_facebook:
                 logInFacebook();
                 break;
 
             case R.id.menu_logout_facebook:
                 logOutFacebook();
                 break;
 
             case R.id.menu_login_twitter:
                 logInTwitter();
                 break;
 
             case R.id.menu_logout_twitter:
                 logOutTwitter();
                 break;
         }
         return true;
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
     }
 
     private boolean hasPublishPermissionFacebook(Collection<String> listStatus, Collection<String> listPermissions) {
         for (String permissionStatus : listStatus) {
             if (!listPermissions.contains(permissionStatus)) {
                 return false;
             }
         }
         return true;
     }
 
     public void logOutFacebook() {
         Session session = Session.getActiveSession();
         if (session != null && !session.isClosed()) {
             session.closeAndClearTokenInformation();
             Utility.clearFacebookCookies(this);
             loginFacebookStatus.setText("");
         }
     }
 
     public void logOutTwitter() {
         if (TwitterUtils.isAuthenticated(preferences)) {
             final Editor edit = preferences.edit();
             edit.remove(OAuth.OAUTH_TOKEN);
             edit.remove(OAuth.OAUTH_TOKEN_SECRET);
             edit.commit();
             showUserLoginTwitter();
         }
     }
 
     public String getMessage() {
         return postMessage.getText().toString();
     }
 
     public void setEmptyText() {
         postMessage.setText("");
     }
 
     public void notLoggedToast() {
         Toast.makeText(getApplicationContext(), "Please login", Toast.LENGTH_SHORT).show();
     }
 
     public void alreadyLoggedToast() {
         Toast.makeText(getApplicationContext(), "Already logged", Toast.LENGTH_SHORT).show();
     }
 
     public void postedToast() {
         Toast.makeText(getApplicationContext(), "Posted", Toast.LENGTH_SHORT).show();
     }
 
     public void showUserLoginFacebook() {
         Session session = Session.getActiveSession();
         if (session != null) {
             Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
                 @Override
                 public void onCompleted(GraphUser user, Response response) {
                     if (user != null) {
                         userNameFacebook = user.getName();
                         loginFacebookStatus.setText(" " + userNameFacebook);
                     }
                 }
             });
         }
     }
 
     public void showUserLoginTwitter() {
         userNameTwitter = TwitterUtils.getUserNameTwitter(preferences);
         if (userNameTwitter == null) {
             loginTwitterStatus.setText("");
         } else {
             loginTwitterStatus.setText(" " + userNameTwitter);
         }
     }
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
