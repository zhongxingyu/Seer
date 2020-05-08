 package com.welch.bitebyte;
 
 /**
  * Program: Browser.java
  * @author: Paul Welch
  * Purpose: This activity uses a webView and JavaScript Interface
  * to search for restaurants in an area within a 500m radius of GPS coordinates.
  * The GPS coordinates are passed to the Google API via a Java/JavaScript Interface.
  */
 
 import android.app.Activity;
 import android.content.Context;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 /** WebView Activity to display Places/Map */
 public class Browser extends Activity implements LocationListener {
 	private WebView webView; 					// Create WebView object
 	private Location mostRecentLocation;		// Create Location object
 	private LocationManager locationManager;	// Create LocationManager object
 	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.browser);
       getLocation();	// Get the current geocoordinates
       openBrowser();	// Open a browser with the Google MapView  
    } // end Main Browser.java Activity method
    
    /** Called when Activity Resumes **/
    @Override
    protected void onResume() {
 	   super.onResume();
 	   getLocation();
    } // end onResume method
    
    /** Called when Activity is Paused **/
    @Override
    protected void onPause() {
 	   super.onPause();
 	   locationManager.removeUpdates(this);
    } // end onPause method
    
    /** Create webView and JavaScript Interface **/
    private void openBrowser(){
	  // Create webview and assign to web_view item
  	  webView = (WebView) findViewById(R.id.web_view);
  	  // JavaScript is off by default, enabling JavaScript
  	  webView.getSettings().setJavaScriptEnabled(true);
  	  // Create and set WebViewClient subclass for rendering
  	  webView.setWebViewClient(new WebViewClient());		
  	  // Add Java to JavaScript Interface and call it 'android' for access.
  	  webView.addJavascriptInterface(new JavaScriptInterface(), "android");
  	  // Wait for the page to load then send the location information
 	    webView.setWebViewClient(new WebViewClient()); 
  	  // Load bite.html page with webView
  	  webView.loadUrl(getURL());							
    } // end Browser method
       
    // Method to set/return URL for map page source
    private String getURL() {
       final String url = "http://my.fit.edu/~pwelch2010/bite.html";
 	  return url;
 	} // end getURL method
 
    /** Retrieve current coordinates from GPS
     *  To save battery on the device the location update time has been increased. **/
    private void getLocation() {
 	    // Android GPS Manager
 	     locationManager =
 	      (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 	    // Application criteria for selection location provider
 	    Criteria criteria = new Criteria();	
 	    // Set the accuracy requirement
 	    criteria.setAccuracy(Criteria.ACCURACY_FINE);
 	    // Get location data from best source
 	    String provider = locationManager.getBestProvider(criteria,true);
 	    // Get updates of current location
 	    locationManager.requestLocationUpdates(provider, 15000, 1, this);
 	    // Pass location data mostRecentLocation for JavaScript Interface
 	    mostRecentLocation = locationManager.getLastKnownLocation(provider);
    } // end getLocation method
    
    /** JavaScriptInterface variables **/
    private class JavaScriptInterface {
 	   // return latitude to the JavaScript requesting it
 	   public double getLatitude(){
 		   //double test = 29.62170;
 	     //return test;
 	     return mostRecentLocation.getLatitude();	
 	   }
 	   // return longitude to the JavaScript requesting it
 	   public double getLongitude(){
 		 //  double test1 = -82.370915;
 	     //return test1;
 	    return mostRecentLocation.getLongitude();	
 	   }
 	   // return radius for Google Places API to the JavaScript requesting it
 	   public int getRadius(){
 		   int radius = 5600;
 		   return radius;		
 	   }
 	   // returns Google Places API type to the JavaScript requesting it
 	   public String getType(){
 		   String type = "restaurant";
 		   return type;		
 	   }
 	 } // end JavaScriptInterface class
    
    /** Required methods when using LocationListener 
     *  Referenced from Android API				**/ 
    @Override
    public void onProviderDisabled(String provider) {
    }
    @Override
    public void onProviderEnabled(String provider) {
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    @Override
    public void onLocationChanged(Location location) {
 	   mostRecentLocation = location;
    }
    /** End required methods when using LocationListener **/   
 } // End Browser.class Activity
