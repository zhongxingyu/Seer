 package edu.vanderbilt.vuphone.android.dining;
 
import edu.vanderbilt.vuphone.android.storage.Restaurant;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 
 public class SplashScreen extends Activity {
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	private final int SPLASH_DISPLAY_LENGTH = 1000;
 
 	// ===========================================================
 	// "Constructors"
 	// ===========================================================
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		setContentView(R.layout.splash_screen);
		
 
 		/*
 		 * New Handler to start the Menu-Activity and close this Splash-Screen
 		 * after some seconds.
 		 */
 		new Handler().postDelayed(new Runnable() {
 			@Override
 			public void run() {
				/* Create an Intent that will start the Main-Activity. */
 				Intent mainIntent = new Intent(SplashScreen.this, Main.class);
 				SplashScreen.this.startActivity(mainIntent);
 				SplashScreen.this.finish();
 			}
 		}, SPLASH_DISPLAY_LENGTH);
 	}
 }
