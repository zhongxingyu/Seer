 package org.kvj.sstack;
 
 import org.apache.cordova.DroidGap;
 
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.webkit.WebView;
 
 public class StickStackActivity extends DroidGap {
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		WebView.enablePlatformNotifications();
 		// Log.i(TAG, "onCreate: " + savedInstanceState);
 		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.setIntegerProperty("backgroundColor", Color.WHITE);
 		super.setBooleanProperty("keepRunning", false);
 		// setStringProperty("loadingDialog", "Stick Stack,Loading...");
 		super.onCreate(savedInstanceState);
 		super.init();
 		appView.setVerticalScrollBarEnabled(true);
 		appView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
 		appView.setScrollbarFadingEnabled(true);
 		appView.getSettings().setUserAgentString(
 				appView.getSettings().getUserAgentString() + " Android "
 						+ android.os.Build.MODEL);
 		appView.clearCache(true);
 		super.loadUrl("file:///android_asset/client/sstack.html");
 	}
 
 	View getRoot() {
 		return this.appView;
 	}
 
 }
