 package com.rogoapp;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;
 
 public class LoginActivity extends Activity {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.login);
		
		public void onClick(View v) {
			// Switch to main activity
			Intent i = new Intent(getApplicationContext(),MainScreenActivity.class);
			startActivity(i);
		}
		
 	}
 	
}
