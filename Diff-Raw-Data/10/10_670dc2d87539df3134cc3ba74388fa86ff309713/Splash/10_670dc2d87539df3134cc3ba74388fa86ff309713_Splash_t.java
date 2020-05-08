 package com.c1.charityworkout;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 
 public class Splash extends Activity {
 
 	@Override
	protected void onCreate(Bundle Splash) {
 		// TODO Auto-generated method stub
		super.onCreate(Splash);
 		setContentView(R.layout.splash);
 		Thread timer = new Thread() {
 			public void run() {
 				try {
 					sleep(5000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				} finally {
					Intent openMainActivity = new Intent("com.c1.charityworkout.mainActivity");
					startActivity(openMainActivity);
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
