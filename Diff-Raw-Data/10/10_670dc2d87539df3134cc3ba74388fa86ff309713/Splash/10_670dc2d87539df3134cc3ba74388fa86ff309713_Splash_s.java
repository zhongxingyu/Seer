 package com.c1.charityworkout;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 
 public class Splash extends Activity {
 
 	@Override
	protected void onCreate(Bundle RandomVariableName) {
 		// TODO Auto-generated method stub
		super.onCreate(RandomVariableName);
 		setContentView(R.layout.splash);
 		Thread timer = new Thread() {
 			public void run() {
 				try {
 					sleep(5000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				} finally {
					Intent openStartingPoint = new Intent("com.example.thenewboston.MENU");
					startActivity(openStartingPoint);
 				}
 			}
 		};
 		timer.start();
 	}
 
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		finish();
 	}
 
 	
 }
