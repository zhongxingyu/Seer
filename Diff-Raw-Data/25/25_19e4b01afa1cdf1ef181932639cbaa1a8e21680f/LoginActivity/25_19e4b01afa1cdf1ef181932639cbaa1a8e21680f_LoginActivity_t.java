 package directi.androidteam.training.chatclient.Authentication;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import directi.androidteam.training.chatclient.R;
 
 public class LoginActivity extends Activity {
     public static final String USERNAME = "directi.androidteam.training.chatclient.Authentication.LoginActivity.USERNAME";
     public static String uname = "";
     public static String pwd = "";
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.login);
         ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress_bar);
         progressBar.setVisibility(View.GONE);
     //   this.registerReceiver(new NetworkConnectionChangeReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
     }
 
     /**
      * Called when the user clicks the Login button
      */
     public void loginUser(View view) {
         EditText username_edit_text = (EditText) findViewById(R.id.username);
         EditText password_edit_text = (EditText) findViewById(R.id.password);
         String username = username_edit_text.getText().toString();
         String password = password_edit_text.getText().toString();
         uname = username;
         pwd = password;
         ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress_bar);
         progressBar.setVisibility(View.VISIBLE);
         (new ConnectGTalk(this)).execute(username, password);
     }
 }
