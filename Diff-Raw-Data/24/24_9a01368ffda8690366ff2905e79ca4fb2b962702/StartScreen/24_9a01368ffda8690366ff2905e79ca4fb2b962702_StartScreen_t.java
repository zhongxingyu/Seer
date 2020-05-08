 package com.inferno.boozegauge;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 
 public class StartScreen extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_start_screen);
         Globals.playAll = false;
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.start_screen, menu);
         return true;
     }
     
     /*
      * sets the global playAll to true in order to signal the
      * app to run all tests
      */
     public void playFull(View view) {
     	Globals.playAll = true;
     	
     	playAlpha(view);
     }
     
     //spins up the alphabet game
     public void playAlpha(View view) {
     	Intent intent = new Intent(this, AlphaTest.class);
     	startActivityForResult(intent, 0);
     }
     
     //spins up the balance test
     public void playBal(View view) {
     	Intent intent = new Intent(this, BalTest.class);
     	startActivityForResult(intent, 1);
     }
     
     //spins up the memory test
     public void playMem(View view) {
     	Intent intent = new Intent(this, MemTest.class);
     	startActivityForResult(intent, 2);
     }
     
     //spins up the logic test
     public void playLog(View view) {
     	Intent intent = new Intent(this, SpaceTest.class);
     	startActivityForResult(intent, 3);
     }
     
     //spins up the result screen
     public void showRes(View view) {
     	Intent intent = new Intent(this, ResultScreen.class);
     	startActivityForResult(intent, 4);
     }
     
     //controls app's play flow as determined by Globals.playAll
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	if (Globals.playAll) {
     		if (requestCode == 0) {
     			playBal(null);
     		} else if (requestCode == 1) {
     			playMem(null);
     		} else if (requestCode == 2) {
     			playLog(null);
     		} else if (requestCode == 3) {
     			showRes(null);
     		}
     	} else if (requestCode != 4) {
     		showRes(null);
     	}
     	
     	if (requestCode == 4) {
     		Globals.score = 0;
    		Globals.playAll = false;
     	}
     }
 }
