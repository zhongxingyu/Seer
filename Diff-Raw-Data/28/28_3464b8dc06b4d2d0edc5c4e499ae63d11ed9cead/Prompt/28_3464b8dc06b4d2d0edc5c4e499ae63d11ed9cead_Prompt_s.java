 package org.jbs.happysad;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Toast;
 
 /**
  * Creates Prompt Activity
  * @author HappySad
  */
 public class Prompt extends Activity implements OnClickListener{
 
 
 	private long myID;
 	public static final String USER_DATA = "userdata";
 	private UIDhelper UIDh;
 	/**
 	 * Initializes activity
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		//Finds the happy_button view
 		View happyButton = findViewById(R.id.happy_button);
 		happyButton.setOnClickListener(this);
 
 		//Finds the sad_button view
 		View sadButton = findViewById(R.id.sad_button);
 		sadButton.setOnClickListener(this);
 
 		//Finds the submit_button view
 		View submitButton = findViewById(R.id.more_to_map);
 		submitButton.setOnClickListener(this);
 
 		UIDh = new UIDhelper();
 		
 		myID = UIDh.getSetUID(getSharedPreferences(USER_DATA,-3), this); 
 		//TODO
 		//we need a checker to see if it returns user=-1
 		//if so, how are we going to deal with it?
 		//right now, it set userID to -1 FOREVER and not give you a chance to fix the mistake. 
 		
 		
 	}
 
 		/**
 		 * Invoked when a view is clicked
 		 */
 		public void onClick(View v) {
 			Intent i = new Intent(this, More.class);
 
 			switch(v.getId()) {		
 			case R.id.happy_button:	
 				i.putExtra("Clicked", "Happy");
 				i.putExtra("Emotion", 1);
 				startActivity(i);
 				break;
 			case R.id.sad_button:
 				i.putExtra("Clicked", "Sad");
 				i.putExtra("Emotion", 0);
 				startActivity(i);
 				break;
 			case R.id.more_to_map:
 				Intent j = new Intent(this, MyMap.class);
 				j.putExtra("Street", 1);
 				j.putExtra("GoToMyLocation", true);
 				j.putExtra("Happy", 1);
 				j.putExtra("Sad", 1);
 				startActivity(j);
 				break;
 			}
 		}
 
 		//Safes 
 		@Override
 		public void onDestroy(){
 			super.onDestroy();
 		}
 
 		// here is the has function. it inputs the username and turns it into an integer.
 		private Integer userNameHash(String username){
 			int id = 1;
 			/* 	for  ( int j = 0; j <= username.length() - 1; j++){
     		  id= id*31+username.charAt(j);
    		}
 			 */return id;
 		}
 	
 	}
