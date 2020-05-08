 package com.twt.xtreme;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 public class AvailViewActivity extends Activity {
 
 	private static final String T = "AvailViewActivity";
 	private SharedPreferences pref;
 	private String view_url;
 	private WebView webview;
 	Location loc;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		
 		
 		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 		loc = ( locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null ?
 					locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) :
 						locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) );
 		if (loc != null) {
 			Log.d(T, loc.toString());
 			
 		} else {
 			Log.e(T, "Can't get any location");
 		}
 		
 		pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 		view_url = getText(R.string.avail_view_url).toString() + 
 					"?latitude="+loc.getLatitude()+
 					"&longitude="+loc.getLongitude()+
					"&accuracy="+loc.getAccuracy();
 		
 		webview = new WebView(this);
 		webview.setVerticalFadingEdgeEnabled(true);
 		webview.setVerticalScrollbarOverlay(true);
 		setContentView(webview);
 	}
 
 	@Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		webview.getSettings().setJavaScriptEnabled(true);
 		webview.setWebViewClient(new WebViewClient() {
 			public boolean shouldOverrideUrlLoading(WebView view, String url) {
 				webview.loadUrl(url);
 				return true;
 			}
 			
 		});
 		webview.loadUrl(view_url);
 	}
 
 
     @Override
 	public void onBackPressed() {
     	if (webview.getUrl() != null && !webview.getUrl().equals(view_url)) {
     		webview.loadUrl(view_url);
     	} else {
     		Log.d(T, "at the view url, closing activity.");
     		super.onBackPressed();
     	}
 	}
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.view_menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.menu_close:
         	this.finish();
         	return true;
         }
 
         return false;
     }
 }
