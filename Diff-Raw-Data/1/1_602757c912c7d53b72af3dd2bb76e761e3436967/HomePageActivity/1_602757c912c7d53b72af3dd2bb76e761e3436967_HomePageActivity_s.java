 package com.example.medicinereminder;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.ViewFlipper;
 
 public class HomePageActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_home_page);
 		
 		TextView fullNameText = (TextView)findViewById(R.id.fullNameText);
 
 		fullNameText.setText(MyGuy.getUser().fullName);
 
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_home_page, menu);
 		return true;
 	}
 	
 	public void toTakeMedicine(View v){
 		//TODO Make sure that going straight to take medicine is the same as activating alarm
 		Intent intent = new Intent(this,AlarmPage.class);
 		startActivity(intent);
 	}
 	
 	public void toEditSettings(View v){
 		Intent intent = new Intent(this, SettingsActivity.class);
 		startActivity(intent);
 	}	
 	
 	public void onResume(){
 		TextView fullNameText = (TextView)findViewById(R.id.fullNameText);
 
 		fullNameText.setText(MyGuy.getUser().fullName);
 		super.onResume();
 	}
 	
 	public void onClickPlay(View v){
 		Intent intent = new Intent(this, com.example.asteroids.Asteroids.class);
 		startActivity(intent);
		finish();
 	}
 
 }
