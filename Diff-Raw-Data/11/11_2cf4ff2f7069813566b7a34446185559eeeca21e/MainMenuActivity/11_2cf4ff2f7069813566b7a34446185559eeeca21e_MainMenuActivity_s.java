 package com.lostandfound;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 /**
  * The MainMenuActivity.
  */
 public class MainMenuActivity extends CustomActivity {
     /** constants needed*/
     public static final String TAG = "MainMenuActivity";
     
     /** views on the screen*/
     Button reportFound, searchLost;
     
     /**
      * Called when the activity is first created.
      *
      * @param savedInstanceState the saved instance state
      */
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.mainmenu);
         
         Log.d(TAG, "Creating main menu activity");
         
         //set up the button listeners
         reportFound = (Button) findViewById(R.id.FoundButton);
         searchLost = (Button) findViewById(R.id.LostButton);
         reportFound.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
             	//make the intent to call the new screen
                 Intent newScreen = new Intent(getBaseContext(), ReportFoundActivity.class);
                 
                 //start the new activity
                 MainMenuActivity.this.startActivity(newScreen);
             }
         });
         searchLost.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
             	//make the intent to call the new screen
                 Intent newScreen = new Intent(getBaseContext(), SearchLostActivity.class);
                 
                 //start the new activity
                 MainMenuActivity.this.startActivity(newScreen);
             }
         });
     }
 }
