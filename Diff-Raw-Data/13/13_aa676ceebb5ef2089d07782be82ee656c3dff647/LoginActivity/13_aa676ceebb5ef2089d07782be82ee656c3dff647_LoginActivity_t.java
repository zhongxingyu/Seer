 package com.example.payback;
 
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 /**
  * Activity which displays a login screen to the user, offering registration as
  * well.
  */
 public class LoginActivity extends TitleActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 		setTitle("PayBack");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		getMenuInflater().inflate(R.menu.login, menu);
 		return true;
 	}
 
 	public void Login(View view) throws InterruptedException {
 		//for log in, url stub is AccountLogin.php
 		Logger CONLOG = Logger.getLogger(LoginActivity.class .getName());
 		CONLOG.setLevel(Level.INFO);
 		CONLOG.info("Login page loaded");
 		final String EMAIL_PATTERN = 
 				"^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$";
 		final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
 		final Matcher matcher;
 
 		String email = ((EditText)findViewById(R.id.email)).getText().toString();
 		String password = ((EditText)findViewById(R.id.password)).getText().toString();
 		CONLOG.info("Login info: <"+email+", "+password+">");
 		matcher = pattern.matcher(email);
 
 		if(email.isEmpty()){
 			Toast.makeText(getApplicationContext(), "Email field is empty", Toast.LENGTH_SHORT).show();
 		}else if(password.isEmpty()){
 			Toast.makeText(getApplicationContext(), "Password field is empty", Toast.LENGTH_SHORT).show();
 		}else if(!matcher.matches()){
 			Toast.makeText(getApplicationContext(), "Email: \""+email+"\" is not a valid email address!", Toast.LENGTH_SHORT).show();
 		}else{
 
 			String status  ="fail";
 			AccessNet caller = new AccessNet();
 
 			String params = "email="+email+"&password="+password;
 			String urlstub = "AccountLogin.php";
 
 			CONLOG.info("Attempting to call server at: "+urlstub+", "+params);
 			status = caller.simpleServerCall(urlstub, params);
 
 			if(status.equalsIgnoreCase("success")){
 				CONLOG.info("Server call successful and logged in!");
 				Intent intent = new Intent(this, MainActivity.class);
 				Toast.makeText(getApplicationContext(),"Welcome", Toast.LENGTH_SHORT).show();
 				startActivity(intent);
				this.finish();
 			}else{
 				CONLOG.info("Server call successful but user failed login.");
 				Toast.makeText(getApplicationContext(),"Incorrect username or password", Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 
 	public void CreateAccount(View view) {
 		Intent intent = new Intent(getApplicationContext(), CreateAccountActivity.class);
         startActivity(intent);
         this.finish();
 	}
 	
 	public void bypass(View view) {
 		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
         startActivity(intent);
         this.finish();
 	}
 }
