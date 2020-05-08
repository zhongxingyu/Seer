 package com.infectosaurus;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
import android.util.Log;
 
 public class Main extends Activity {
 	private static final String TAG = "MyActivity";
	
 	private static final int GAME_OUTCOME = 0;
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        Log.d(TAG, "imhere");
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setClassName(this, GameBoard.class.getName());
//        startActivityForResult(i, GAME_OUTCOME);
        startActivity(i);
     }
 }
