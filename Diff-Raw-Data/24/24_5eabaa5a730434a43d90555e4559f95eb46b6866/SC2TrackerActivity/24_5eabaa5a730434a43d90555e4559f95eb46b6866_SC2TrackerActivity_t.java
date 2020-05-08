 package illinois.sweng.sctracker;
 
 
 
 import org.json.JSONArray;
 
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 /**
  * Main activity for the application; first activity launched. 
  */
 public class SC2TrackerActivity extends Activity implements DelegateActivity {
     /** Called when the activity is first created. */
 	
 	static String TAG = "sc2trackerMainActivity";
 	private Button mRegisterButton, mLoginButton, mUnregisterButton;
 	private EditText mEmail, mPassword;
 	private ServerCommunicator mServerCommunicator;
 	private String mUserpass;
 	private boolean mManualLogin = false;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         String prefsFile = getResources().getString(R.string.preferencesFilename);
         String key = getResources().getString(R.string.preferencesUserpass);
 		SharedPreferences sharedPreferences = getSharedPreferences(prefsFile, 0);
 		String userpass = sharedPreferences.getString(key, ":");
 		
 		Log.d("PREFS", userpass);
 		
 		mServerCommunicator = new ServerCommunicator(this, TAG);
 		mServerCommunicator.sendAuthenticationRequest(userpass);
 		mManualLogin = true;
 		
         setContentView(R.layout.main);
         
         mRegisterButton = (Button) findViewById(R.id.registerButton);
         mUnregisterButton = (Button) findViewById(R.id.unregisterButton);
         mLoginButton = (Button) findViewById(R.id.LoginButton);
         
         mRegisterButton.setOnClickListener(new RegisterButtonHandler());
         mUnregisterButton.setOnClickListener(new UnregisterButtonHandler());
         mLoginButton.setOnClickListener(new LoginButtonHandler());
         
         mEmail = (EditText) findViewById(R.id.mainEmailEditText);
         mPassword = (EditText) findViewById(R.id.mainPasswordTextEdit);
         
         mServerCommunicator = new ServerCommunicator(this, TAG);
         registerWithServer();
     }
    
     /**
      * Launches the registration activity.
      */
     private void launchRegister() {
     	Intent i = new Intent(this, RegisterActivity.class);
 		startActivity(i);
     }
     
     /**
      * Launches the unregister activity
      */
     private void launchUnregister() {
     	Intent i = new Intent(this, UnregisterActivity.class);
 		startActivity(i);
     }
     
     private void registerWithServer() {
     	Log.d("Registration Info", "Attempting to register c2dm");
     	Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
     	registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
     	registrationIntent.putExtra("sender", "star2tracker@gmail.com");
     	this.startService(registrationIntent);
     	Log.d("Registration Info", "Finished Sending registration intent");
     }
     
     /**
      * Attempts to login the user.
      */
     private void loginUser() {
     	
 		String username = mEmail.getText().toString();
 		String password = mPassword.getText().toString();
 		String userpass = username + ":" + password;
		mUserpass = userpass;
 		
 		mServerCommunicator.sendAuthenticationRequest(userpass);	
     }
     
     /* Button click handlers */
     private class RegisterButtonHandler implements View.OnClickListener {
 		public void onClick(View v) {
 			Log.d(TAG, "Reg Button clicked");
             launchRegister();
 		}
 	}
 	
 	private class UnregisterButtonHandler implements View.OnClickListener {
 		public void onClick(View v) {
 			Log.d(TAG, "Unreg Button clicked");
             launchUnregister();
 		}
 	};
 	
 	private class LoginButtonHandler implements View.OnClickListener {
 		public void onClick(View v) {
 			Log.d(TAG, "Login Button clicked");
 			loginUser();
 		}
 	}
 
 	public void handleServerError(String message) {
 		if(mManualLogin) {
 			Toast errorToast = Toast.makeText(this, message, Toast.LENGTH_LONG);
 			errorToast.show();
 		}
 	}
 
 
 	public void handleServerResponseData(JSONArray values) {
 	}
 	
 	public void handleServerResponseMessage(String message) {
 		mLoginButton.setText(R.string.registerNewAccountSuccess);	
 		
 		String prefsFile = getResources().getString(R.string.preferencesFilename);
 		String key = getResources().getString(R.string.preferencesUserpass);
 		
 		SharedPreferences sharedPreferences = getSharedPreferences(prefsFile, MODE_PRIVATE);
 		Editor editor = sharedPreferences.edit(); 
 		editor.putString(key, mUserpass);
 		editor.commit();
 		
 		Intent i = new Intent(this, HostTabsActivity.class);
 		startActivity(i);
 		Log.d("MAIN", message);
 	}
 }
