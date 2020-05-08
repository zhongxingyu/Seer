 package com.example.mobileticket;
 
 import com.parse.*;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 
 public class NewUserActivity extends Activity {
 
 	// Values for email and password at the time of the login attempt.
 	private String mEmail;
 	private String mPassword;
 	private String mUserName;
 
 	// UI references.
 	private EditText mEmailView;
 	private EditText mPasswordView;
 	private EditText mUserNameView;
 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_new_user);
 		
 		// on met les valeurs passes dans l'intent
 		mEmail = getIntent().getStringExtra("email");
 		mEmailView = (EditText) findViewById(R.id.userEmail);
 		mEmailView.setText(mEmail);
 		
 		mPassword = getIntent().getStringExtra("password");
 		mPasswordView = (EditText) findViewById(R.id.userPassword);
 		mPasswordView.setText(mPassword);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.new_user, menu);
 		return true;
 	}
 
 	public void createUser(View view)
 	{
 		mEmail = mEmailView.getText().toString();
 		mPassword = mPasswordView.getText().toString();
 		mUserName = mUserNameView.getText().toString();
 
 		// Crons le user
 		ParseUser user = new ParseUser();
 		user.setUsername(mUserName);
 		user.setPassword(mPassword);
 		user.setEmail(mEmail);
 		try {user.signUp();
 		}
 			catch (Exception e)
 			{ 
 			}
 		// and Go back to Home Screen
 		Intent intent = new Intent(this, HomeActivity.class);
 		startActivity(intent);
 	}
 	
 }
