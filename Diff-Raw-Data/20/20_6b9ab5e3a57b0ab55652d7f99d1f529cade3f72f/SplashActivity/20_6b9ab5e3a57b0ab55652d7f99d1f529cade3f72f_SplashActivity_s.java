 package com.jobhound.activities;
 
 import com.jobhound.R;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 
 
 public class SplashActivity extends Activity {
 	
 	// how long until we go to the next activity
 		protected int _splashTime = 5000;
 		private Thread splashTread;
 		
 		
 		
 		@Override
 		protected void onCreate(Bundle savedInstanceState) {
 			// TODO Auto-generated method stub
 			super.onCreate(savedInstanceState);
 
 			setContentView(R.layout.splash_layout);
 			
 			
 			
 			
 			// thread for displaying the SplashScreen
 			splashTread = new Thread() {
 				@Override
 				public void run() {
 					try {
 						synchronized (this) {
 
 							// wait 5 sec
 							wait(_splashTime);
 						}
 
 					} catch (InterruptedException e) {
 					} finally {
 						finish();
 						// Check if a profile exists on the DB
							Intent openMainMenu = new Intent("android.intent.action.MAIN_MENU_LAYOUT");
 							startActivity(openMainMenu);
 							finish();
 
 					}
 				}
 			};
 
 			splashTread.start();
 			
 		}
 
 }
