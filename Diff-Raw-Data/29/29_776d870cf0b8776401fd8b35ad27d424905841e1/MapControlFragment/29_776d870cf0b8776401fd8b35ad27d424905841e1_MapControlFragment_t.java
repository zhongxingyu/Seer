 package com.cardinal.cardinalhack;
 
 import android.util.Log;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
public class MapControlFragment extends MapFragment {
 	private static final String TAG = MapControlFragment.class.getSimpleName();
 
 	@Override
 	public void onResume() {
 		Log.d(TAG, "onResume()");
 		super.onResume();
 
		GoogleMap map = this.getMap();
 
		LatLng packardPlace = new LatLng(35.227267, -80.846474); // GPS cords of Packard Place
 		map.addMarker(new MarkerOptions().position(packardPlace).title(
 				"Packard Place"));
 		map.moveCamera(CameraUpdateFactory.newLatLngZoom(packardPlace, 10));
 		// Zoom in, animating the camera.
 		map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
 	}
 }
