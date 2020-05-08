 package com.example.tabs3;
 
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 
 import com.example.threetabs.R;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.UiSettings;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class Fragment2 extends SupportMapFragment implements LocationListener {
 
 	LocationManager locationManager;
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		init();
 		super.onActivityCreated(savedInstanceState);
 	}
 
 	@Override
 	public void onDestroy() {
 		locationManager.removeUpdates(this);
 		super.onDestroy();
 	}
 
 	private void init() {
 		UiSettings settings = getMap().getUiSettings();
 		settings.setAllGesturesEnabled(true);
 		settings.setMyLocationButtonEnabled(true);
 
 		/*
		 * Commented line to hide radius
		 * mode)
 		 */
 		// getMap().setMyLocationEnabled(true);
 
 		locationManager = (LocationManager) getActivity().getSystemService(
 				getActivity().LOCATION_SERVICE);
		Location location = null;
 		String provider = LocationManager.GPS_PROVIDER;
 		if (!locationManager.isProviderEnabled(provider)) {
 			Criteria criteria = new Criteria();
 			provider = locationManager.getBestProvider(criteria, true);
 		}
		location = locationManager.getLastKnownLocation(provider);
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		LatLng latLng = new LatLng(latitude, longitude);
		getMap().moveCamera(CameraUpdateFactory.newLatLng(latLng));
		getMap().moveCamera(CameraUpdateFactory.zoomTo(15));
 		if (location != null) {
 			onLocationChanged(location);
 		}
 		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 			locationManager.requestLocationUpdates(
 					LocationManager.GPS_PROVIDER, 5000, 0, this);
 		}
 		if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
 			locationManager.requestLocationUpdates(
 					LocationManager.NETWORK_PROVIDER, 10000, 0, this);
 		}
 
 	}
 
 	@Override
 	public void onLocationChanged(Location arg0) {
 		getMap().clear();
 		double latitude = arg0.getLatitude();
 		double longitude = arg0.getLongitude();
 		LatLng latLng = new LatLng(latitude, longitude);
 		getMap().moveCamera(CameraUpdateFactory.newLatLng(latLng));
 		getMap().addMarker(
 				new MarkerOptions().position(latLng).icon(
 						BitmapDescriptorFactory
 								.fromResource(R.drawable.ic_launcher)));
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 	}
 }
