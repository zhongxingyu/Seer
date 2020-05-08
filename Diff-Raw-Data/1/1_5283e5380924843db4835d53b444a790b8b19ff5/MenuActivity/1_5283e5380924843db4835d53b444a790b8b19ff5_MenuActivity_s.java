 package com.example.myhealthapp;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 
 public class MenuActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_menu);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.menu, menu);
 		return true;
 	}
 	
 	public void goToLoginActivity(View view){
 		Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
 		MenuActivity.this.startActivity(intent);
 	}
 	
 	public void goToMeasurementsActivity(View view){
 		Intent intent = new Intent(MenuActivity.this, MeasurementsActivity.class);
 		MenuActivity.this.startActivity(intent);
 	}
 	
 	public void goToUrinetestActivity(View view){
 		Intent intent = new Intent(MenuActivity.this, UrinetestActivity.class);
 		MenuActivity.this.startActivity(intent);
 	}
 
 }
