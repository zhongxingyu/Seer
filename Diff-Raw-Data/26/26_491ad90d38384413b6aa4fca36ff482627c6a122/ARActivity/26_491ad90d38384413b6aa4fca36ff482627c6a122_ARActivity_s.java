 package com.appmunki.miragemobile.ar;
 
 import android.app.Activity;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.RelativeLayout;
 
 public class ARActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setFullscreen();
 		disableScreenTurnOff();
 		setOrientation();
 		setupLayout();
 		loadMatcher();
 	}
 
 	private void loadMatcher() {
		Matcher.fetch();
 
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				Matcher.fetch();
 			}
 		}).start();
 	}
 
 	/**
 	 * Avoid that the screen get's turned off by the system.
 	 */
 	public void disableScreenTurnOff() {
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
 				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 	}
 
 	/**
 	 * Maximize the application.
 	 */
 	public void setFullscreen() {
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 	}
 
 	/**
 	 * Set's the orientation to landscape, as this is needed by AndAR.
 	 */
 	public void setOrientation() {
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 	}
 
 	private void setupLayout() {
 		// Add a Parent
 		RelativeLayout main = new RelativeLayout(this);
 
 		// Defining the RelativeLayout layout parameters.
 		// In this case I want to fill its parent
 		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.MATCH_PARENT,
 				RelativeLayout.LayoutParams.MATCH_PARENT);
 
 		// Add preview
 		Preview preview = new Preview(this);
 
 		main.addView(preview);
 
 		// Add relative layout as main
 		setContentView(main, rlp);
 	}
 
 }
