 package edu.upenn.cis350;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 /**
  * Activity class to represent the one right price game. 
  * @author Ashley Baldwin
  * @version 1.0
  */
 
 public class OneRightPriceActivity extends Activity {
 	private String username;
 	private int score;
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         this.username = getIntent().getStringExtra(Constants.UNEXTRA); 
        setContentView(R.layout.onerightprice);

         //should never occur
         if (username == null) {
         	finish();
         }
     }
     
     /**
      * Get method for the username of the current user
      * @return the String of the username for the current user
      */
     public String getUsername() {
     	return this.username;
     }
    
     }
    
