 package com.hrupin.flickrapp.ui.activities;
 
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.gmail.yuyang226.flickr.oauth.OAuth;
 import com.gmail.yuyang226.flickr.oauth.OAuthToken;
 import com.gmail.yuyang226.flickr.people.User;
 import com.hrupin.flickrapp.R;
 import com.hrupin.flickrapp.UserPreferences;
 import com.hrupin.flickrapp.development.Logger;
 import com.hrupin.flickrapp.task.GetOAuthTokenTask;
 import com.hrupin.flickrapp.task.OAuthTask;
 
 public class LoginActivity extends Activity implements OnClickListener {
 
     public static final String CALLBACK_SCHEME = "com-hrupin-flickrapp-oauth"; //$NON-NLS-1$
     private static final String TAG = LoginActivity.class.getSimpleName();
     private Button buttonSignIn;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_login);
 
         buttonSignIn = (Button) findViewById(R.id.buttonSignIn);
         buttonSignIn.setOnClickListener(this);
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         // this is very important, otherwise you would get a null Scheme in the
         // onResume later on.
         setIntent(intent);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         Intent intent = getIntent();
         String scheme = intent.getScheme();
         OAuth savedToken = UserPreferences.getOAuthToken();

        if (!isNeedLogin(savedToken)) {
            openApp();
         }
        
        if (CALLBACK_SCHEME.equals(scheme) && isNeedLogin(savedToken)) {
             Uri uri = intent.getData();
             String query = uri.getQuery();
             Logger.d(TAG, "Returned Query: {}", query);
             String[] data = query.split("&");
             if (data != null && data.length == 2) {
                 String oauthToken = data[0].substring(data[0].indexOf("=") + 1);
                 String oauthVerifier = data[1].substring(data[1].indexOf("=") + 1);
                 Logger.d(TAG, "OAuth Token: {}; OAuth Verifier: {}", oauthToken, oauthVerifier);
 
                 OAuth oauth = UserPreferences.getOAuthToken();
                 if (oauth != null && oauth.getToken() != null && oauth.getToken().getOauthTokenSecret() != null) {
                     GetOAuthTokenTask task = new GetOAuthTokenTask(this);
                     task.execute(oauthToken, oauth.getToken().getOauthTokenSecret(), oauthVerifier);
                 }
             }
        } 
    }

    private boolean isNeedLogin(OAuth savedToken) {
        return savedToken == null || savedToken.getUser() == null;
     }
 
     private void openApp() {
         Intent intent = new Intent(this, FlickrAppActivity.class);
         startActivity(intent);
         finish();
     }
 
     public void onOAuthDone(OAuth result) {
         if (result == null) {
             Toast.makeText(this, "Authorization failed", Toast.LENGTH_LONG).show();
         } else {
             User user = result.getUser();
             OAuthToken token = result.getToken();
             if (user == null || user.getId() == null || token == null || token.getOauthToken() == null || token.getOauthTokenSecret() == null) {
                 Toast.makeText(this, "Authorization failed", Toast.LENGTH_LONG).show();
                 return;
             }
             String message = String.format(Locale.US, "Authorization Succeed: user=%s, userId=%s, oauthToken=%s, tokenSecret=%s", //$NON-NLS-1$
                     user.getUsername(), user.getId(), token.getOauthToken(), token.getOauthTokenSecret());
            Logger.i(TAG, message);
            Toast.makeText(this, "Authorization Succeed", Toast.LENGTH_LONG).show();
             UserPreferences.saveOAuthToken(user.getUsername(), user.getId(), token.getOauthToken(), token.getOauthTokenSecret());
             openApp();
         }
     }
 
     @Override
     public void onClick(View v) {
         if (v.getId() == buttonSignIn.getId()) {
             OAuthTask task = new OAuthTask(this);
             task.execute();
         }
     }
 }
