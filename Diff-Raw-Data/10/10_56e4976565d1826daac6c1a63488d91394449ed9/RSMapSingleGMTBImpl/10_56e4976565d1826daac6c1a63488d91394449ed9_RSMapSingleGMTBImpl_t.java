 package com.riverspart.map.view;
 
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.riverspart.nurse.R;
 
 public abstract class RSMapSingleGMTBImpl extends RSMapGMTBImpl implements RSMapSingleGMTB {
 	
 	protected GoogleMap mMap;
 	
 	protected ArrayList<Marker> markers = new ArrayList<Marker>();
 	private ArrayList<String> markerIds = new ArrayList<String>();
 	
 //	private void checkSum() {
 //		//Gurantee right id for maker
 //	}
 	
 	protected Marker buildMarker(MarkerOptions markerOption) {
 		return mMap.addMarker(markerOption);
 	}

 	@Override
 	public synchronized void updateMarker(String id, MarkerOptions makerOption) {
 		// TODO Auto-generated method stub
 		if(markerIds.contains(id)) {
 			Marker marker = markers.get(markerIds.indexOf(id));
 			marker.remove();
 			marker = mMap.addMarker(makerOption);
			markers.set(markerIds.indexOf(id), marker);
 		} else {
 			markers.add(buildMarker(makerOption));
 			markerIds.add(id);
 		}
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
     protected void setUpMapIfNeeded() {
         // Do a null check to confirm that we have not already instantiated the map.
         if (mMap == null) {
             // Try to obtain the map from the SupportMapFragment.
             mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                     .getMap();
         }
     }
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.map_single_map_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch(item.getItemId()) {
         
         	case R.id.map_single_map_type_satelite:
                 mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                 return true;
             case R.id.map_single_map_type_normal:
             	mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                 return true;
             case R.id.map_single_map_type_hybrid:
             	mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                 return true;
             case R.id.map_single_map_type_terrain:
             	mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 }
