 package com.infectosaurus;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 
 public class Main extends Activity {
 	private static final String TAG = "MyActivity";
 	private static final int GAME_OUTCOME = 0;
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        Intent i = new Intent(this, GameBoard.class);
        startActivityForResult(i, GAME_OUTCOME);
     }
 }
