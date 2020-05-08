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
     
     public void playFull(View view) {
     	Globals.playAll = true;
     	
     	playAlpha(view);
     }
     
     public void playAlpha(View view) {
     	Intent intent = new Intent(this, AlphaTest.class);
     	startActivityForResult(intent, 0);
     }
     
     public void playBal(View view) {
     	Intent intent = new Intent(this, BalTest.class);
     	startActivityForResult(intent, 1);
     }
     
     public void playMem(View view) {
     	Intent intent = new Intent(this, MemTest.class);
     	startActivityForResult(intent, 2);
     }
     
     public void playLog(View view) {
     	Intent intent = new Intent(this, LogTest.class);
     	startActivityForResult(intent, 3);
     }
     
     public void showRes(View view) {
     	Globals.playAll = false;
     	Intent intent = new Intent(this, ResultScreen.class);
     	startActivityForResult(intent, 4);
     }
     
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	if (Globals.playAll) {
     		if (requestCode == 0) {
    			playAlpha(null);
    		} else if (requestCode == 1) {
     			playBal(null);
    		} else if (requestCode == 2) {
     			playMem(null);
    		} else if (requestCode == 3) {
     			playLog(null);
    		} else {
     			showRes(null);
     		}
    	} else {
     		showRes(null);
     	}
     }
 }
