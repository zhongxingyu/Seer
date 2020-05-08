 package org.yaoha;
 
 
 import org.osmdroid.api.IGeoPoint;
 import org.osmdroid.util.GeoPoint;
 import org.osmdroid.views.MapController;
 import org.osmdroid.views.MapView;
 import org.osmdroid.views.overlay.MyLocationOverlay;
 
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 public class YaohaMapActivity extends Activity implements LocationListener {
 	MapView mapview;
 	MapController mapController;
 	LocationManager locationManager;
 	MyLocationOverlay mOverlay;
 	static final GeoPoint braunschweig = new GeoPoint(52265000, 10525000);
 	
 	String search_term = "";
 	
 	SharedPreferences mprefs = null;
 	SharedPreferences default_shared_prefs = null;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
         setContentView(R.layout.mapview);
         mapview = (MapView) findViewById(R.id.mapview);
         mapview.setBuiltInZoomControls(true);
         mapview.setMultiTouchControls(true);
         mapController = this.mapview.getController();
 
         mapview.setMapListener(new YaohaMapListener(this));
 
         mprefs = getPreferences(MODE_PRIVATE);
         default_shared_prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
         assert(mprefs != default_shared_prefs);
         int zoom = Integer.parseInt(default_shared_prefs.getString("zoomlevel", "-1"));
         zoom = mprefs.getInt("zoomlevel", zoom);
         //Toast.makeText(this, "Set zoom to " + zoom +"m!", Toast.LENGTH_LONG).show();
         //mapController.setZoom(prefs.getInt("zoomlevel", 15));
 
         // hardcoded default is braunschweig
         int longitude = mprefs.getInt("longitude", braunschweig.getLongitudeE6());
         int latitude = mprefs.getInt("latitude", braunschweig.getLatitudeE6());
         GeoPoint myPosition = new GeoPoint(latitude, longitude);
         
         // get saved values
 	    if (savedInstanceState != null) {
 	        savedInstanceState.getInt("zoomlevel", zoom);
             myPosition.setLatitudeE6(savedInstanceState.getInt("latitude", myPosition.getLatitudeE6()));
             myPosition.setLongitudeE6(savedInstanceState.getInt("longitude", myPosition.getLongitudeE6()));
 	    }
 	    
 	 // Acquire a reference to the system Location Manager
 	    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 	    Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 	    
 	    if (loc != null) {
             myPosition = new GeoPoint(loc);
             Log.i(YaohaMapActivity.class.getSimpleName(), "last known location is " + myPosition);
 	    }
 	    else {
 	        Log.i(YaohaMapActivity.class.getSimpleName(), "no last known location " + myPosition);
 	    }
 
         mapController.setZoom(zoom);
         mapController.setCenter(myPosition);
 
 	 // Register the listener with the Location Manager to receive location updates
 //        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 300, 200, this);
         locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 300000, 200, this);
         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300000, 200, this);
         
         mOverlay = new MyLocationOverlay(this, mapview);
         mapview.getOverlays().add(mOverlay);
         mapview.postInvalidate();
         
         // just save the search term
         // we may later add a method to actually look in maps after it
         Intent intent = getIntent();
         CharSequence text = intent.getCharSequenceExtra("org.yaoha.YaohaMapActivity.SearchText");
         search_term = text.toString();
         Log.i(YaohaMapActivity.class.getSimpleName(), "Search field input was: " + text);
 	}
 
     @Override
     public void onLocationChanged(Location location) {
         // TODO Auto-generated method stub
         GeoPoint myPosition = new GeoPoint(location);
         mapController.setCenter(myPosition);
         // Remove the listener you previously added
 //        locationManager.removeUpdates(this);
         Log.i(this.getClass().getSimpleName(), "got location update from " + location.getProvider());
         Log.i(this.getClass().getSimpleName(), "new location is " + location.getLatitude() + "," + location.getLongitude());
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
 
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu_map, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.track_me:
             Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
             if (loc == null) {
                 Toast.makeText(this, "No location known *crash*", Toast.LENGTH_LONG).show();
             } else {
                 GeoPoint myPosition = new GeoPoint(loc);
                 mapController.setCenter(myPosition);
                 Toast.makeText(this, "Tracking me!", Toast.LENGTH_LONG).show();
             }
             return true;
 
         default:
             return false;
         }
     }
     @Override
     protected void onResume() {
         super.onResume();
         mOverlay.enableMyLocation();
     }
     
     @Override
     protected void onPause() {
         super.onPause();
         mOverlay.disableMyLocation();
 
         IGeoPoint center = mapview.getMapCenter();
         Editor editor = mprefs.edit();
         editor.putInt("zoomlevel", mapview.getZoomLevel());
         editor.putInt("longitude", center.getLongitudeE6());
         editor.putInt("latitude", center.getLatitudeE6());
         editor.commit();
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         IGeoPoint center = mapview.getMapCenter();
         outState.putInt("zoomlevel", mapview.getZoomLevel());
         outState.putInt("longitude", center.getLongitudeE6());
         outState.putInt("latitude", center.getLatitudeE6());
     }
     
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
        int zoom_level = savedInstanceState.getInt("zoomleve");
         int longitude = savedInstanceState.getInt("longitude");
         int latitude = savedInstanceState.getInt("latitude");
         
         mapController.setZoom(zoom_level);
         mapController.setCenter(new GeoPoint(latitude, longitude));
     }
 }
