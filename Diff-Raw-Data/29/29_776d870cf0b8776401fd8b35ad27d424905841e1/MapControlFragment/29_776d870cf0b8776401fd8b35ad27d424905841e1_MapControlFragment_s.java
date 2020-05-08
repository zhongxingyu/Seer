 package com.cardinal.cardinalhack;
 
import android.app.Fragment;
import android.os.Bundle;
 import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
public class MapControlFragment extends Fragment {
 	private static final String TAG = MapControlFragment.class.getSimpleName();
	private MapFragment mMapFragment;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView()");
		return inflater.inflate(R.layout.map_fragment, container, false);

	}
 
 	@Override
 	public void onResume() {
 		Log.d(TAG, "onResume()");
 		super.onResume();
 
		mMapFragment = (MapFragment) getFragmentManager()
				.findFragmentById(R.id.map);
		GoogleMap map = mMapFragment.getMap();
 
		LatLng packardPlace = new LatLng(35.227267, -80.846474); // GPS cords of
																	// Packard
																	// Place
 		map.addMarker(new MarkerOptions().position(packardPlace).title(
 				"Packard Place"));
 		map.moveCamera(CameraUpdateFactory.newLatLngZoom(packardPlace, 10));
 		// Zoom in, animating the camera.
 		map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
 	}
 }
