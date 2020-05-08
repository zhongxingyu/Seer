 package com.zhaoyunhe.pw;
 
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Menu;
 import android.view.WindowManager;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.backends.android.AndroidApplication;
 import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
 
 public class MainActivity extends AndroidApplication {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
		this.setLogLevel(LOG_DEBUG);//TODO
		
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 		ApplicationListener gameView = new PhysicalWorld();
 		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
 		config.useGL20 = false;
 		config.numSamples = 2;
		config.useAccelerometer=false;
		config.useCompass=false;
 		initialize(gameView, config);
 
 		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
 
 		Log.d("d", mDisplayMetrics.widthPixels + ":"+ mDisplayMetrics.heightPixels);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 }
