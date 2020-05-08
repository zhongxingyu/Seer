 package com.derpicons.gshelf;
 
 import android.app.Activity;
import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
import android.widget.Toast;
 
 public class Login extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 
 		Button login = (Button) findViewById(R.id.login);
 		TextView registerScreen = (TextView) findViewById(R.id.register);
 		TextView passwordScreen = (TextView) findViewById(R.id.forgotPassword);
 
 		// Attempts to authenticate the user.
 		login.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				final EditText username = (EditText) findViewById(R.id.usernameField);
 				final EditText password = (EditText) findViewById(R.id.passwordField);
 				final CheckBox Remember = (CheckBox) findViewById(R.id.rememberMe);
 				final TextView usernameText = (TextView) findViewById(R.id.username);
 				final TextView passwordText = (TextView) findViewById(R.id.password);
 				
 				// Check that all fields are filled.
 				/*
 				User CurUser = LoginUtilities.Authenticator(username.getText()
 						.toString(), password.getText().toString());
 				if (CurUser != null) {
 					Intent i = new Intent(getApplicationContext(), MainMenu.class);
 					startActivity(i);
 				} else {
 					//invisible field
 				}
 				*/
 				Intent i = new Intent(getApplicationContext(), MainMenu.class);
 				startActivity(i);
 			}
 		});
 
 		// Sends the user to the register screen.
 		registerScreen.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				
 				//Intent i = new Intent(getApplicationContext(),
 				//		RegisterActivity.class);
 				//startActivity(i);
 			}
 		});
 
 		// Sends the user to the password screen.
 		passwordScreen.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				
 				//Intent i = new Intent(getApplicationContext(),
 				//		UserQuestionActivity.class);
 				//startActivity(i);
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_login, menu);
 		return true;
 	}
 }
