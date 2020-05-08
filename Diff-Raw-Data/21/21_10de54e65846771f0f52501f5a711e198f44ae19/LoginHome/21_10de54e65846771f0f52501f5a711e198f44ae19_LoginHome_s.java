 package com.trydish.main;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 
 public class LoginHome extends Activity {
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 				
 		setContentView(R.layout.activity_login_home);
 		
 		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 	}
 	
 	public void loginCheck(View view) {
     	Intent intent = new Intent(this, PostLoginHome.class);
     	startActivity(intent);
 	}
 	
 	public void signupButton(View view) {
 		Intent intent = new Intent(this, SignupHome.class);
 		startActivity(intent);
 	}
 
 }
