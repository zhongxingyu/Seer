 package edu.ua.moundville;
 
 import java.util.List;
 
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 public abstract class PlaceMap extends MapActivity {
 
	protected final String URL = "http://betatesting.as.ua.edu/mapexperience/images";
 	protected final static String TAG = "PlaceMap";
     protected final MapActivity mapActivity = this;
     protected final static GeoPoint MOUNDVILLE_LOCATION_CENTER = new GeoPoint(33005263, -87631438);
     protected final static GeoPoint THE_BLUFF = new GeoPoint(3322029,-87527894);
     protected final static GeoPoint HOUSER_HALL = new GeoPoint(33214743,-87544427);
     protected final static GeoPoint MOUNDVILLE_MOUND_A = new GeoPoint(33006034,-87631124);
     protected final static GeoPoint MOUNDVILLE_MUSEUM = new GeoPoint(33006176,-87634921);
     
 	protected MapView mapView;
 	protected MapController mapController;
 	
     protected MyLocationOverlay locationOverlay;
     protected List<Overlay> mapOverlays;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
 	    setContentView(R.layout.place_map);
         
         /*	Capture the LinearLayout and MapView through their layout resources. */
         mapView = (MapView) findViewById(R.id.mapview);
         /* 	Then get the ZoomControls from the MapView */
         mapView.setBuiltInZoomControls(true);
         mapView.setSatellite(false);
         mapView.setSaveEnabled(true);
         mapView.setWillNotCacheDrawing(false);
         
         /* get the controller to set custom pan and zoom */
         mapController = mapView.getController();
         //mapController.animateTo(MOUNDVILLE_LOCATION_CENTER);
         mapController.animateTo(MOUNDVILLE_LOCATION_CENTER);
         mapController.setZoom(17);
         
         final ToggleButton toggleLocation = (ToggleButton)this.findViewById(R.id.toggle_location);
         final ToggleButton toggleSatellite = (ToggleButton)this.findViewById(R.id.toggle_satellite);
         
         /* adding overlays */
         mapOverlays = mapView.getOverlays();
         locationOverlay = new MyLocationOverlay(this, mapView); 
         mapOverlays.add(locationOverlay);
         
         toggleLocation.setOnClickListener(new OnClickListener() {
         	public void onClick(View v) {
         		if (toggleLocation.isChecked()) {
         			locationOverlay.enableMyLocation();
         			Toast.makeText(v.getContext(), "Finding your location...", Toast.LENGTH_SHORT).show();
         			locationOverlay.runOnFirstFix(new Runnable(){
         				public void run() {
         					Runnable action = null;
         					GeoPoint userPoint = locationOverlay.getMyLocation();
 
         					if (userPoint == null) {
         						action = new Runnable() {
         							public void run() {
         								Toast.makeText(findViewById(R.id.mapview).getContext(), "Your location could not be determined.", Toast.LENGTH_LONG).show();
         								toggleLocation.performClick();
         							}
         						};
 
         					} else if (isLocationInRange(userPoint, MOUNDVILLE_LOCATION_CENTER, 500)) {
         						mapController.setZoom(17);
         					} else {
         						action = new Runnable() {
         							public void run() {
         								Toast.makeText(findViewById(R.id.mapview).getContext(), "You're location cannot be displayed.\n   You are too far from Moundville.", Toast.LENGTH_LONG).show();
         								toggleLocation.performClick();
         							}
         						};
         					}
         					if (action != null) runOnUiThread(action);
         				}
         			});
         		} else {
         			locationOverlay.disableMyLocation();
         		}
         	}
         });
         
         toggleSatellite.setChecked(false);
         toggleSatellite.setOnClickListener(new OnClickListener() {
         	public void onClick(View v) {
         		if (toggleSatellite.isChecked()) {
         			mapView.setSatellite(true);
         		} else {
         			mapView.setSatellite(false);
         		}
         	} });
     }
     
     abstract protected void populateMap();
     
     protected void redrawMap() {
     	mapView.invalidate();
     }
     
     protected static boolean isLocationInRange(GeoPoint point1, GeoPoint point2, float distance) {
     	Location location1 = new Location("MyLocationOverlay");
     	location1.setLatitude(point1.getLatitudeE6() / 1E6);
     	location1.setLongitude(point1.getLongitudeE6() / 1E6);
     	
     	Location location2 = new Location("MyLocationOverlay");
     	location2.setLatitude(point2.getLatitudeE6() / 1E6);
     	location2.setLongitude(point2.getLongitudeE6() / 1E6);
     	
     	float meterDistance = location1.distanceTo(location2);
     	
     	return meterDistance <= distance ? true : false;
     }
     
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.default_menu, menu);
 	    return true;
 	}    
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	        case R.id.home:
 	            // app icon in action bar clicked; go home
 	            Intent intent = new Intent(this, MoundvilleActivity.class);
 	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 	            startActivity(intent);
 	            return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}    
     
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 	
 	protected void onPause() {
 		super.onPause();
 	}
 }
