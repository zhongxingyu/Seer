 package edu.gatech.oad.rocket.findmythings;
 
 import android.location.Address;
 import android.location.Geocoder;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 import edu.gatech.oad.rocket.findmythings.util.ErrorDialog;

 import java.io.IOException;
 import java.util.List;

/**
 * CS 2340 - FindMyStuff Android App
  * Activity that deals with the maps feature for location of items
  * 
  * @author TeamRocket
  * */
 public class MapsActivity extends FragmentActivity {
 
 	public static final String LOCATION_EXTRA = "location";
 	
 	/**
 	 * Used to convert address into longitude and latitude
 	 */
 	private Geocoder findLoc;
 	
 	/**
 	 * Reference to the map
 	 */
 	private GoogleMap map;
 	
 	/**
 	 * Stores a list of possible addresses
 	 */
 	private List<Address> locations;
 
 	private String location;
 
 	private void setupMapIfNeeded() {
 
 		// Do a null check to confirm that we have not already instantiated the map.
 		if (map == null) {
 			// Try to obtain the map from the SupportMapFragment.
 			map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
 					.getMap();
 			// Check if we were successful in obtaining the map.
 			if (map != null) {
 				setupMap();
 			}
 		}
 	}
 
 	private void setupMap() {
 		if (location == null) return;
 
		//Geocoder object to convert a text address into an Address object
		findLoc = new Geocoder(this);

		LatLng currlocation = null;

 		//Used to add a marker to the map
 		MarkerOptions options = new MarkerOptions();
 
 		try { //Converts the location string into a list of possible long/lag addresses
 			for (int i = 0; i < 10 && (locations == null || locations.size() == 0); i++) {
 				locations = findLoc.getFromLocationName(location, 1);
 			}
 
 			if (locations.size() >= 1) {
 				//Object to store lat/long
 				currlocation = new LatLng(locations.get(0).getLatitude(),locations.get(0).getLongitude());
 			} else {
 				new ErrorDialog(R.string.maps_location_find_err).getDialog(this).show();
 			}
 		} catch (IOException e) {
 			new ErrorDialog(R.string.maps_location_inet_err).getDialog(this).show();
 		}
 
 		if (currlocation != null) {
 			//Sets the markers position
 			options.position(currlocation);
 			//Tells the camera where to go
 			CameraUpdate updatePosition = CameraUpdateFactory.newLatLng(currlocation);
 			//Adds the marker
 			map.addMarker(options);
 			//Moves the camera to the CameraUpdate location
 			map.moveCamera(updatePosition);
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		setupMapIfNeeded();
 	}
 
 	/**
 	 * create the window with correct layout
 	 * 
 	 * @param savedInstanceState
 	 */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_maps);
 
 		location = getIntent().getStringExtra(LOCATION_EXTRA);
 		setTitle(location);
 	}
 	
 	/**
 	 * Called to pop the map window from the navigation stack
 	 */
 	@Override 
     public void finish() {
         super.finish();
         overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
     }
        
 }
