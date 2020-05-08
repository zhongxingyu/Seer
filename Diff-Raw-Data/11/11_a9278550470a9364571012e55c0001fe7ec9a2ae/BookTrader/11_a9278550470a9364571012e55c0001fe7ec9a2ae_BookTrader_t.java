 package org.abstractbinary;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.Toast;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class BookTrader extends Activity {
     /* Remote API */
     static final String LOGIN_URL = "http://146.169.25.146:6543/users/login";
 
     /* Debugging */
     static final String TAG = "BookTrader";
 
     /* Login-state stuff */
     static final int STATE_NOT_LOGGED_IN = 0;
     static final int STATE_LOGGING_IN = 1;
     static final int STATE_LOGGED_IN = 2;
     int state;
 
     /* Dialogs */
     static final int DIALOG_LOGIN = 0;
 
     /* Often used widgets */
     FrameLayout menuBar;
     Button loginButton;
 
     /* Internal gubbins */
     String username = "username";
     String password = "password";
 
 
     /* Application life-cycle */
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         menuBar = (FrameLayout)findViewById(R.id.menu_bar);
 
         loginButton = (Button)findViewById(R.id.login_button);
 
         state = STATE_NOT_LOGGED_IN;
 
         Log.v(TAG, "BookTrader running...");
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         final Dialog dialog;
         switch (id) {
         case DIALOG_LOGIN:
             dialog = new Dialog(this);
 
             dialog.setContentView(R.layout.login_dialog);
             dialog.setTitle("Login");
 
             final EditText usernameField =
                 (EditText)dialog.findViewById(R.id.username_field);
             usernameField.setText(username);
             final EditText passwordField =
                 (EditText)dialog.findViewById(R.id.password_field);
             passwordField.setText(password);
             final Button button =
                 (Button)dialog.findViewById(R.id.login_dialog_button);
             button.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         username = usernameField.getText().toString();
                         password = passwordField.getText().toString();
                         Log.v(TAG, "New login info: " + username);
                         dialog.dismiss();
                         doLogin();
                     }
             });
 
             break;
         default:
             throw new RuntimeException("Unknown dialog type: " + id);
         }
         return dialog;
     }
 
 
     /* Event handlers */
 
     /** Called when the login button is pressed. */
     public void logIn(View v) {
         nextState();
     }
 
 
     /* Helpers */
 
     /** Used to cycle from one login-state to the next. */
     void nextState() {
         switch (state) {
         case STATE_NOT_LOGGED_IN:
             state = STATE_LOGGING_IN;
             loginButton.setEnabled(false);
             showDialog(DIALOG_LOGIN);
             break;
         case STATE_LOGGING_IN:
             state = STATE_LOGGED_IN;
             break;
         case STATE_LOGGED_IN:
             state = STATE_NOT_LOGGED_IN;
             loginButton.setEnabled(true);
             break;
         default:
             throw new RuntimeException("unknown state: " + state);
         }
 
         Log.v(TAG, "Now in state " + state);
 
         View v = menuBar.getChildAt(menuBar.getChildCount() - 1);
         menuBar.removeView(v);
         menuBar.addView(v, 0);
     }
 
     /** Perform the remote login and switch to login state if successful.
      *  Cheers for:
      *  <a href="http://www.androidsnippets.com/executing-a-http-post-request-with-httpclient">Executing a HTTP POST Request with HttpClient</a> */
     void doLogin() {
         HttpClient httpClient = new DefaultHttpClient();
         HttpPost httpPost = new HttpPost(LOGIN_URL);
 
         try {
             List<NameValuePair> values = new ArrayList<NameValuePair>();
             values.add(new BasicNameValuePair("username", username));
             values.add(new BasicNameValuePair("password", password));
             HttpResponse response = httpClient.execute(httpPost);
             Log.v(TAG, "login request done with " + response.getStatusLine());

            Toast.makeText(this, "Logged in", Toast.LENGTH_SHORT).show();
         } catch (Exception e) {
             Log.v(TAG, "login failed with " + e);
            Toast.makeText(this, "Login failed :(", Toast.LENGTH_LONG).show();
            nextState();
            nextState();
         }
     }
 }
