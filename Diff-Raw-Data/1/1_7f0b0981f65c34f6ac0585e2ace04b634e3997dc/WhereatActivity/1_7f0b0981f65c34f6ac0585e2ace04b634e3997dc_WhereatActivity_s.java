 package com.whereat.app;
 
 import android.app.Activity;
 import android.location.Location;
 import android.os.Bundle;
 import android.util.Log;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 
 import com.whereat.app.LocationHelper.LocationUpdateListener;
 import com.whereat.jsinterface.LocalDBInterface;
 
 public class WhereatActivity extends Activity implements LocationUpdateListener {
 
 	WebView mWebView; //This is all we need for this activity
 	LocationHelper locationHelper;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main); //We wont need any more views because our entire navigation is in html and js.
 
 		// Set up location updates
 		//locationHelper = new LocationHelper(this);
 		//locationHelper.registerForUpdates(this);
 		
 		//Setup WebView
 		mWebView = (WebView) findViewById(R.id.webview);
 		mWebView.getSettings().setJavaScriptEnabled(true);
 		mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
 		
 		//Add any JS interfaces that we need like the DB
 		mWebView.addJavascriptInterface(new LocalDBInterface(this), "WhereatDB");
 		
 		//Load the URL from the assets folder.
 		mWebView.loadUrl("file:///android_asset/view.html");
 		
 		//Set up Error Logging.
 		mWebView.setWebChromeClient(new WebChromeClient(){
 			public void onConsoleMessage(String message, int lineNumber, String sourceID){
 				Log.d("Whereat",message + " -- LINE: " + lineNumber + " : " + sourceID);
 			}
 		});
 	}
 	
 	@Override
 	public void onBackPressed(){
 		this.finish(); //Stop it from doing location pooling.
 	}
 
 	public void onLocationUpdate(Location location) {
 		Log.d("Whereat", "lat " + location.getLatitude() + " lng " + location.getLongitude()); 
 	}
 
 }
