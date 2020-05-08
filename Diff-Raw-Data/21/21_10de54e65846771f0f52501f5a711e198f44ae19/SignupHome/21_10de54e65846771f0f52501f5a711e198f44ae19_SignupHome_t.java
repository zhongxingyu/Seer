 package com.trydish.main;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 
 public class SignupHome extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_signup_home);
 		
 		ActionBar actionBar = getActionBar();
		actionBar.hide();
 	}
 	
 	public void signupNext(View view) {
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
