 package com.example.lufteapp;
 
 import java.util.ArrayList;
 
import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
import android.util.Log;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class ViewMap extends FragmentActivity {
     /**
      * Note that this may be null if the Google Play services APK is not available.
      * 
      * @author http://stackoverflow.com/questions/15098243/android-app-keeps-crashing-when-using-googlemap-v2
      * 
      */
     private GoogleMap mMap;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_view_map);
         
         setUpMapIfNeeded();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         setUpMapIfNeeded();
     }
 
     /**
      * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
      * installed) and the map has not already been instantiated.. This will ensure that we only ever
      * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
      * If it isn't installed {@link SupportMapFragment} (and
      * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
      * install/update the Google Play services APK on their device.
     * <p>
      * A user can return to this FragmentActivity after following the prompt and correctly
      * installing/updating/enabling the Google Play services. Since the FragmentActivity may not have been
      * completely destroyed during this process (it is likely that it would only be stopped or
      * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
      * {@link #onResume()} to guarantee that it will be called.
      */
     private void setUpMapIfNeeded() {
         // Do a null check to confirm that we have not already instantiated the map.
         if (mMap == null) {
         	
         	// Try to obtain the map from the SupportMapFragment.
             mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                     .getMap();
            
     		Bundle extras = getIntent().getExtras();
     		
     		if(extras != null){
     			
     			if(extras.get("POSITION") != null){
     			
     				String[] coords = (String[]) extras.get("POSITION");
     				LatLng position = getLatLng(coords[0], coords[1]);
     			
     				setUpMarker(position);
     				
     				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
     				
     			} else if(extras.get("POSITIONS") != null) {
    				
     				ArrayList<String[]> positions = (ArrayList<String[]>) extras.get("POSITIONS");
     				
     				LatLng position = null;
     				
     				
     				for(int i = 0; i < positions.size(); i++) {
     					String[] coords = positions.get(i);
     					position = getLatLng(coords[0], coords[1]);
     					setUpMarker(position);
     				}
     				
     				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
     			}
     			
     		} else {
     			// If there is no extras via intent -> activity started from main menu
     			// Check if we were successful in obtaining the map. And sets map to our location
                 if (mMap != null) {
                     setUpMap();
                 }
     		} 
         }
     }
 
     /**
      * Finds the users posistion on the map
      */
     private void setUpMap() {
     	mMap.setMyLocationEnabled(true);
     }
     
     private void setUpMarker(LatLng coords) {
    	
     	Marker marker = mMap.addMarker(new MarkerOptions().position(coords).title(getString(R.string.map_text)));
     }
     
     private LatLng getLatLng(String lat, String lng){
    	
     	LatLng result;
     	
 		Double mapLat = Double.parseDouble((String)lat); 
 		Double mapLng = Double.parseDouble((String)lng);
 		
 		result = new LatLng(mapLat, mapLng);
 		
     	return result;
     }
 }
