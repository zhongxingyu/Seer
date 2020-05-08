 package org.kvj.sstack;
 
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.os.Bundle;
import android.util.Log;
 import android.webkit.WebView;
 
 import com.phonegap.DroidGap;
 
 public class StickStackActivity extends DroidGap {
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate: " + savedInstanceState);
 		setRequestedOrientation(getResources().getConfiguration().orientation);
 		super.setIntegerProperty("backgroundColor", Color.BLACK);
 		super.setBooleanProperty("keepRunning", true);
 		super.onCreate(savedInstanceState);
 		super.init();
 		appView.setVerticalScrollBarEnabled(true);
 		appView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
 		appView.setScrollbarFadingEnabled(true);
		super.loadUrl("file:///android_asset/client/msstack.html");
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
		Log.i(TAG, "onSave");
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
		Log.i(TAG, "Config changed");
 	}
 
 	@Override
 	protected void onPause() {
		Log.i(TAG, "onPause");
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
		Log.i(TAG, "onResume");
 		super.onResume();
 	}
 
 	@Override
 	protected void onRestart() {
		Log.i(TAG, "onRestart");
 		super.onRestart();
 	}
 
 	@Override
 	public void onDestroy() {
		Log.i(TAG, "onDestroy");
 		super.onDestroy();
 	}
 
 	@Override
 	public void onBackPressed() {
		Log.i(TAG, "onBack");
 		super.onBackPressed();
 	}
 }
