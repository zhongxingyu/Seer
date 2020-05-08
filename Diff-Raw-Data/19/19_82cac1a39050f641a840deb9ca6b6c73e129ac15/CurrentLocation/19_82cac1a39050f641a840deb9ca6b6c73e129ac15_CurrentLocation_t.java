 package geoarmy.android;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import geoarmy.android.locationList;
 import geoarmy.android.location;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 public class CurrentLocation extends MapActivity {
 	MapController mapController;
 	MapView mapView;
 	StaticItemizedOverlay treasureOverlay;
 	DynamicItemizedOverlay hunterOverlay;
 	Context mContext;
 	TextView txt_lat, txt_lng, txt_geocount, txt_gps;
 	int latitude = 51618120, longitude = -1279020;
 	String currentStatus;
 	UserPreferences userPrefs;
 	
 	/** Menu references **/
 	private static final int ACCOUNT_ID = R.id.account;
 	private static final int RADAR_ID   = R.id.compass;
 	private static final int REFRESH_ID = R.id.refresh;
 	private static final int CENTER_ID  = R.id.center;
 	private static final int LIST_ID    = R.id.geocachelist;
 	private static final int SHOW_ID    = R.id.geocacheshow;
     private ProgressDialog m_ProgressDialog = null; 
     private static final String TAG     = "NetworkUtilities";
     
     public static locationList currentLocationList;
     final Context context = CurrentLocation.this;
 	final Handler mHandler = new Handler();
 	
 	/** Called when the activity is first created. */
     public void onCreate(Bundle bundle) {
         super.onCreate(bundle);
        
         setContentView(R.layout.main); // bind the layout to the activity
         enableGPS();
         
         /** Set up GPS pinger **/
         setGPSPing();
         
         // lat-lng text
         txt_lng = (TextView) findViewById(R.id.location_text_lng);
         txt_lat = (TextView) findViewById(R.id.location_text_lat); 
         // geocache counter
         txt_geocount = (TextView) findViewById(R.id.status_text_00);
         // Satellite text
         txt_gps = (TextView) findViewById(R.id.status_text_01);
         
         // create a map view
         mapView = (MapView) findViewById(R.id.mapview);
         mapView.setBuiltInZoomControls(true);
         
         mapController = mapView.getController();
         mapController.setZoom(17);
         mapView.setSatellite(false);
         mapView.invalidate();
       
         String username = UserPreferences.getUsername(context);
         String password = UserPreferences.getPassword(context);
         
         NetworkTools.authenticate(username, password, mHandler, context, false);
         if (UserPreferences.getLoggedIn(context)) {
         	Toast.makeText(context, "Successfully logged into your account", Toast.LENGTH_LONG).show();
         	getGeocaches();
        	drawGeocaches(currentLocationList);
         } else {
         	Toast.makeText(context, "Failed to log into your account", Toast.LENGTH_LONG).show();
         }
     }
     
     public void enableGPS() {
         /** Check if GPS is enabled, if not prompt the user to enable it **/
         if (!NetworkTools.gpsEnabled(context)) {
         	MessageTools.createGpsDisabledAlert(context);
         }
     }
     
     public void setGPSPing() {
     	LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
     	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, new GeoUpdateHandler());
     }
     
     public static locationList getCurrentLocationList() {
     	return currentLocationList;
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.menu, menu);
     	return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle all of the possible menu actions.
     	switch (item.getItemId()) {
         case ACCOUNT_ID:
             editAccount();
             //finish();
             break;
         case RADAR_ID:
         	compassView();
         	break;
         case REFRESH_ID:
             getGeocaches();
             break;
         case CENTER_ID:
         	centerMap();
         	break;
         case LIST_ID:
         	listView();
         	break;
         case SHOW_ID:
         	showView();
         	break;
         }
         return super.onOptionsItemSelected(item);
     }
     
     private final void listView() {
     	Intent i = new Intent(this, geocacheListActivity.class);
     	startActivity(i);
     }
     
     private final void showView() {
     	Intent i = new Intent(this, geocacheShow.class);
     	startActivity(i);
     }
     
     private final void editAccount() {
     	Intent i = new Intent(this, account.class);
     	startActivity(i);
     }
     
     private final void compassView() {
     	Intent i = new Intent(this, compass.class);
     	startActivity(i);
     }
     
     private final void centerMap() {
     	GeoPoint point = new GeoPoint(latitude,longitude);
     	mapController.animateTo(point);
 	}
     
     public void onGeocachesResult(locationList mylocationList) {
     	currentLocationList = mylocationList;
     	drawGeocaches(currentLocationList);
     	m_ProgressDialog.dismiss();
     }
     
     public void onNetworkError(String error) {
    	if (!(m_ProgressDialog == null) && m_ProgressDialog.isShowing()) {
    		m_ProgressDialog.dismiss();
    	}
    	MessageTools.alert("Sorry a network error has occured", context);
     }
     
     private final void getGeocaches() {
     	if (!UserPreferences.getLoggedIn(context)) {
     		Toast.makeText(context, "You are not logged in", Toast.LENGTH_LONG).show();
     		return;
     	}
     	if (NetworkTools.gpsEnabled(context)) {
 	       m_ProgressDialog = ProgressDialog.show(CurrentLocation.this,    
 	                "Please wait...", "Retrieving geocache data ...", true);
 	        final Handler mHandler = new Handler();
 	    	double parsedLat = latitude;
 	    	parsedLat = parsedLat / 1000000;
 	    	double parsedLon = longitude;
 	    	parsedLon = parsedLon / 1000000;
 	    	//get geocache points	
 	    	NetworkTools.getLocations(parsedLat, parsedLon, mHandler, context);
     	} else {
 	    	enableGPS();
     	}
     }
     
     private final void drawGeocaches(locationList mylocationList) {
    	 if (mylocationList == null) {
    		 return;
    	 }
         List<Overlay> mapOverlays = mapView.getOverlays();
         mapOverlays.clear();
         //Add geocache markers
         //
         Drawable treasurePin = this.getResources().getDrawable(R.drawable.ruby);
         GeoPoint treasurepoint;
         
     	treasureOverlay = new StaticItemizedOverlay(treasurePin, this); 
     	OverlayItem overlayTreasure;
 
     	// our location
         GeoPoint point = new GeoPoint(latitude, longitude);
     	
     	int len = mylocationList.length();
     	
    	if (len < 0) {
    		return;
    	}
     	ArrayList<location> localArray = mylocationList.getLocations();
     	location localLocation = new location();
     	
     	
     	int myLat = 0;
     	int myLon = 0;
     	for (int i = 0; i < len; i++) {
     		localLocation = localArray.get(i);
     		myLat = (int) (Double.parseDouble(localLocation.getLat())*1E6);
     		myLon = (int) (Double.parseDouble(localLocation.getLon())*1E6);
     		treasurepoint = new GeoPoint(myLat, myLon);
 
         	overlayTreasure = new OverlayItem(treasurepoint, localLocation.getName(), "Point: " + treasurepoint);
         	treasureOverlay.addOverlay(overlayTreasure);
         	mapOverlays.add(treasureOverlay); // add new marker
          }
     	
     	txt_geocount.setText("Geocache count: " + len);
     	drawMarker(point);
         mapController.setCenter(point);
     }
     
 	public void getGPS() {
         LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, new GeoUpdateHandler());
 	}
     
     @SuppressWarnings("unused")
 	private final void hideMenu() {
     	
     }
     
     @Override
     protected boolean isRouteDisplayed() {
     	return false;
     }
     
 	private void drawMarker(GeoPoint point) {		
 		List<Overlay> mapOverlays = mapView.getOverlays();
 		int hunterIdx = mapOverlays.indexOf(hunterOverlay);
 		if (hunterIdx >= 0 && !mapOverlays.isEmpty()) {
 			mapOverlays.remove(hunterIdx);
 		}
         Drawable userPin = context.getResources().getDrawable(R.drawable.center_marker_male);
         hunterOverlay = new DynamicItemizedOverlay(userPin, context);
         OverlayItem overlayitem = new OverlayItem(point, "Hello GeoArmy recruit", "Point: " + point);
         hunterOverlay.addOverlay(overlayitem);
         mapOverlays.add(hunterOverlay); // add new marker
         Log.d(TAG, "------>"+mapOverlays.toString());
 	}
     
     public class GeoUpdateHandler implements LocationListener {
        	
     	public void onLocationChanged(Location location) {
     		
     		if (location != null) {
     			latitude = (int) (location.getLatitude()*1E6);
     			longitude = (int) (location.getLongitude()*1E6);
     		 
 	    		GeoPoint point = new GeoPoint(latitude,longitude);
 	
 	    		String currentLat = "Lat: " + location.getLatitude();
 	    		String currentLng = "Lng: " + location.getLongitude();
 	    		txt_lat.setText(currentLat);
 	    		txt_lng.setText(currentLng);
 	    		
 	    		currentStatus = "GPS: Active";
 	    		txt_gps.setText(currentStatus);
 	
 	    		drawMarker(point);
 	    		//mapController.animateTo(point); always keep hunter in middle of map
     		}
        	}
   	  	
     	public void onProviderDisabled(String provider) {
     	}
     	
     	public void onProviderEnabled(String provider) {
     	}
     	
     	public void onStatusChanged(String provider, int status, Bundle extras) {
     	}
     }
 }
