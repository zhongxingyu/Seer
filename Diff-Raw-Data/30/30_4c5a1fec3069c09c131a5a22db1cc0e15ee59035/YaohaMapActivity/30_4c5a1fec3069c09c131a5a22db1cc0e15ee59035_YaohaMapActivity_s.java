 package org.yaoha;
 
import java.util.logging.Logger;
 
 import org.osmdroid.util.GeoPoint;
 import org.osmdroid.views.MapController;
 import org.osmdroid.views.MapView;
 import org.osmdroid.views.overlay.MyLocationOverlay;
 
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
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
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.mapview);
 	    mapview = (MapView) findViewById(R.id.mapview);
 	    mapview.setBuiltInZoomControls(true);
 	    mapview.setMultiTouchControls(true);
 	    
 	    mapController = this.mapview.getController();
 	    SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 	    int zoom = Integer.parseInt(prefs.getString("zoomlevel", "-1"));
 	    //Toast.makeText(this, "Set zoom to " + zoom +"m!", Toast.LENGTH_LONG).show();
         //mapController.setZoom(prefs.getInt("zoomlevel", 15));
 	    mapController.setZoom(zoom);
 	    
	    
 	 // Acquire a reference to the system Location Manager
	    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 	    Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 	    // hardcoded default is braunschweig
	    GeoPoint myPosition = new GeoPoint(52265000, 10525000);
 	    if (loc != null) {
             myPosition = new GeoPoint(loc);
             Log.i(YaohaMapActivity.class.getSimpleName(), "last known location is " + myPosition);
 	    }
 	    else {
 	        Log.i(YaohaMapActivity.class.getSimpleName(), "no last known location " + myPosition);
 	    }
         mapController.setCenter(myPosition);
 	 // Register the listener with the Location Manager to receive location updates
 //        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 300, 200, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 300, 200, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300, 200, this);
         
         mOverlay = new MyLocationOverlay(this, mapview);
         mapview.getOverlays().add(mOverlay);
         mapview.postInvalidate();

 	}
 
     @Override
     public void onLocationChanged(Location location) {
         // TODO Auto-generated method stub
         GeoPoint myPosition = new GeoPoint(location);
         mapController.setCenter(myPosition);
     // Remove the listener you previously added
        locationManager.removeUpdates(this);
        Log.i(YaohaMapActivity.class.getSimpleName(), "got position update by " + location.getProvider());
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
     }
 }
