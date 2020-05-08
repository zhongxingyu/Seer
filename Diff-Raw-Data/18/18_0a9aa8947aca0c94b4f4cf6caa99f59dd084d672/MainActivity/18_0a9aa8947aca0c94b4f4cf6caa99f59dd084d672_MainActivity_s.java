 package it.helpus.helpapp;
 
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.webkit.JsResult;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.ActionProvider;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 public class MainActivity extends SherlockActivity implements LocationListener {
 	private Location mostRecentLocation;
 	private WebView webView;
 	private static String BASE_URL = "file:///android_asset/maps.html";
	private static String REMOTE_URL = "http://10.1.90.56:3000";
 	private static String MARKERS_PATH = "/home/index.json";
 //	private static String URL = "file:///android_asset/maps.html";
 //	private static String URL = "http://10.1.90.76:3000/";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		if(!this._isNetworkAvailable()) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("No network connectivity.")
 			       .setCancelable(false)
 			       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			                MainActivity.this.finish();
 			           }
 			       }
 	        );
 			AlertDialog alert = builder.create();
 			alert.show();
 		}
 		else {
 			
 			setupWebView();
 			getLocation();
 		}
 	}
 
 	@Override
     public void onBackPressed() {
         // Pop the browser back stack or exit the activity
         if (webView.canGoBack()) {
         	webView.goBack();
         }
         else {
             super.onBackPressed();
         }
     }
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// If this callback does not handle the item click,
 		// onPerformDefaultAction
 		// of the ActionProvider is invoked. Hence, the provider encapsulates
 		// the
 		// complete functionality of the menu item.
 		Toast.makeText(this, "Handling in onOptionsItemSelected avoided",
 				Toast.LENGTH_SHORT).show();
 		return false;
 	}
 
 	private Boolean _isNetworkAvailable() {
 		ConnectivityManager conMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo i = conMgr.getActiveNetworkInfo();
 		if (i == null)
 			return false;
 		if (!i.isConnected())
 			return false;
 		if (!i.isAvailable())
 			return false;
 		return true;
 	}
 
 	/** Sets up the WebView object and loads the URL of the page **/
 	@SuppressLint("SetJavaScriptEnabled")
 	private void setupWebView() {
 		webView = (WebView) findViewById(R.id.webview);
 		webView.getSettings().setJavaScriptEnabled(true);
 		webView.getSettings().setAllowFileAccess(true);
 		webView.setWebViewClient(new WebViewClient());
 		webView.setWebChromeClient(new MyChromeWebViewClient());
 		webView.getSettings().setGeolocationEnabled(true);
 		webView.requestFocusFromTouch(); 
 		webView.loadUrl(BASE_URL);
 		/** Allows JavaScript calls to access application resources **/
 		webView.addJavascriptInterface(new JavaScriptInterface(), "android");
 	}
 
 	private void getLocation() {
 		// Acquire a reference to the system Location Manager
 		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 
 		Criteria criteria = new Criteria();
 		criteria.setAccuracy(Criteria.ACCURACY_FINE);
 		String provider = locationManager.getBestProvider(criteria, true);
 		// In order to make sure the device is getting the location, request
 		// updates.
 		locationManager.requestLocationUpdates(provider, 1, 0, this);
 		mostRecentLocation = locationManager.getLastKnownLocation(provider);
 		
 		if(mostRecentLocation == null) {
 			mostRecentLocation = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
 		}
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 	  if((mostRecentLocation.getLatitude() != location.getLatitude()) || (mostRecentLocation.getLongitude() != location.getLongitude())) {
 		  mostRecentLocation = location;
 		  webView.loadUrl("javascript:if(typeof fixLocation == 'function') {fixLocation();}");
 	  }
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * Sets up the interface for getting access to Latitude and Longitude data
 	 * from device
 	 **/
 	private class JavaScriptInterface {
 		@SuppressWarnings("unused")
 		public double getLatitude() {
 			return mostRecentLocation.getLatitude();
 		}
 
 		@SuppressWarnings("unused")
 		public double getLongitude() {
 			return mostRecentLocation.getLongitude();
 		}
 
 		@SuppressWarnings("unused")
 		public String getRemoteUrl() {
 			return REMOTE_URL;
 		}
 
 		@SuppressWarnings("unused")
 		public String getMarkersPath() {
 			return MARKERS_PATH;
 		}
 
 		@SuppressWarnings("unused")
 		public boolean openDetails(final String weburl) {
 			Log.d("HELPUS", weburl);
 			
 			// Launch Web Activity
 			Intent intent = null;
 			intent = new Intent(MainActivity.this, WebActivity.class);
 			intent.putExtra("weburl", weburl);
 			startActivity(intent);
 			
 			return false;
 		}
 	}
 
 	private class MyChromeWebViewClient extends WebChromeClient {
 
 	    @Override
 	    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
 	        Log.d("HELPUS", message);
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
 			builder.setMessage(message)
 			       .setCancelable(true)
 			       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			        	   dialog.cancel();
 			           }
 			       }
 	        );
 	        return true;
 	    }
 
 		@SuppressWarnings("unused")
 		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
 			Toast.makeText(MainActivity.this, "Oh no! " + description,
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 }
