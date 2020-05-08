 package com.trydish.main;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
import android.view.Gravity;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class SignupHome extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_signup_home);
 		
 		ActionBar actionBar = getActionBar();
 		actionBar.hide();
 	}
 	
 	public void signupNext(View view) {
 		EditText userText = (EditText)(findViewById(R.id.signup_username));
 		EditText passText = (EditText)(findViewById(R.id.signup_password));
 		EditText confText = (EditText)(findViewById(R.id.signup_password_confirm));
 		
 		String user = userText.getText().toString();
 		String pass = passText.getText().toString();
 		String conf = confText.getText().toString();
 		
 		if (user.equals("")) {
			Toast toast = Toast.makeText(this, "Please enter a username.", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 400);
 			toast.show();
 			return;
 		} else if (pass.equals("")) {
			Toast toast = Toast.makeText(this, "Please enter a password.", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 400);
 			toast.show();
 			return;
 		} else if (conf.equals("")) {
			Toast toast = Toast.makeText(this, "Please confirm your password.", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 400);
 			toast.show();
 			return;
 		} else if (!(conf.equals(pass))) {
			Toast toast = Toast.makeText(this, "Mismatched passwords, try again.", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 400);
 			toast.show();
 			passText.setText("");
 			confText.setText("");
 			return;
 		}
 		
 		
 		Intent intent = new Intent(this, SignupAllergies.class);
 		startActivity(intent);
 	}
 
 //	@Override
 //	public boolean onCreateOptionsMenu(Menu menu) {
 //		// Inflate the menu; this adds items to the action bar if it is present.
 //		getMenuInflater().inflate(R.menu.activity_signup_home, menu);
 //		return true;
 //	}
 
 }
