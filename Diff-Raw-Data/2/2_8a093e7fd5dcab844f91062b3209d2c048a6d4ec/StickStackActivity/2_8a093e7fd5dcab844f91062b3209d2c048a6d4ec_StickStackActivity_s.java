 package org.kvj.sstack;
 
 import android.content.pm.ActivityInfo;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.webkit.WebView;
 
 import com.phonegap.DroidGap;
 
 public class StickStackActivity extends DroidGap {
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		WebView.enablePlatformNotifications();
 		// Log.i(TAG, "onCreate: " + savedInstanceState);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		super.setIntegerProperty("backgroundColor", Color.BLACK);
 		super.setBooleanProperty("keepRunning", true);
		setStringProperty("loadingDialog", "Stick Stack,Loading...");
 		super.onCreate(savedInstanceState);
 		super.init();
 		appView.setVerticalScrollBarEnabled(true);
 		appView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
 		appView.setScrollbarFadingEnabled(true);
 		super.loadUrl("file:///android_asset/client/sstack.html");
 	}
 
 }
