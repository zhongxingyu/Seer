 package com.parse.starter;
 
 import com.parse.LogInCallback;
 import com.parse.Parse;
 import com.parse.ParseUser;
 import com.parse.PushService;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.parse.ParseException;
 
 public class LoginActivity extends Activity {
 	private static final String TAG = "Login Activity";
 
 	private static final String YOUR_APPLICATION_ID = "vQyhiWo3htopZhxEX2t7pspvbB2vDRSSuPPAASuX";
 	private static final String YOUR_CLIENT_KEY = "JCtAgree2otOnZI1inaziB4tM0RrlNJoZMe5lDJ5";
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Parse.initialize(this, YOUR_APPLICATION_ID, YOUR_CLIENT_KEY);
 		setTitle(R.string.app_name);
 
 		setContentView(R.layout.activity_login);
		Log.d("loginact" , "Inside login activity");
 		TextView loginButton = (TextView) findViewById(R.id.btnLogin);
 		loginButton.setOnClickListener(loginClickListener);// Listening to LOGIN button click
 		
 		TextView registerScreen = (TextView) findViewById(R.id.link_to_register);
 		registerScreen.setOnClickListener(registerClickListener);// Listening to register new account link
 	}
 	
 	private OnClickListener loginClickListener = new OnClickListener() {
 		public void onClick(View v) {
			Log.d("login clicked" , "Login button clicked!");
 			EditText editTextUserName = (EditText) findViewById(R.id.login_username);
 			String username = editTextUserName.getText().toString();
 
			EditText editTextPassword = (EditText) findViewById(R.id.login_username);
 			String password = editTextPassword.getText().toString();
 
 			ParseUser.logInInBackground(username, password, new LogInCallback() {
 				public void done(ParseUser user, ParseException e) {
 					if (user != null) {
						Log.d("info" , "User succesfully logged in!");
 						// Hooray! The user is logged in.
 						Intent homeIntent = new Intent(getApplicationContext(), PatientHomeActivity.class);
 						startActivity(homeIntent);
 
 					} else {
 						// Login failed. Look at the ParseException to see what happened.
 						Toast.makeText(getApplicationContext(), "Login Failed. Incorrect Username/Password.", Toast.LENGTH_SHORT).show();
 						Log.d(TAG , e.getMessage());
 					}
 				}
 			});
 		}
 	};
 	
 	private OnClickListener registerClickListener = new OnClickListener() {
 		public void onClick(View v) {
 			// Switching to Register screen
 			Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
 			startActivity(i);	
 		}
 	};
 }
