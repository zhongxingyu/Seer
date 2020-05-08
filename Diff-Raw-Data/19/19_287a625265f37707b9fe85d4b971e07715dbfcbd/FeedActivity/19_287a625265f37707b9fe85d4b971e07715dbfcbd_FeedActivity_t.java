 package com.example.handsonandroid;
 
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
 
 
 public class FeedActivity extends Activity {
 
 	ArrayList<Events> events;
 	User user;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_feed);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	public void onStart(){
 		super.onStart();
		
		if(SaveSharedPreference.getData(this, SaveSharedPreference.USER_NAME_KEY).length() != 0){
			
		}
 	}
 	
 	@Override
 	public void onPause(){
 		super.onPause();
 	}
 	
 	@Override
 	public void onStop(){
 		super.onStop();
 	}
 
 }
