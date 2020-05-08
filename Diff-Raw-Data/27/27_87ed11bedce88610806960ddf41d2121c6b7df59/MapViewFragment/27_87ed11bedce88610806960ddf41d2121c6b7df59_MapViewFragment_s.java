 package com.bixito;
 
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.bixito.station.BikeStation;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.CancelableCallback;
 import com.google.android.gms.maps.LocationSource;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.UiSettings;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class MapViewFragment extends SupportMapFragment implements LocationListener, LocationSource {
 	ArrayList<BikeStation> stationList = null;
 	ArrayList<MarkerOptions> markerList = null;
 	static final Float initialZoomLevel = 10F;
 	static final double initialLongitude = -79.40;
 	static final double initialLatitude = 43.65;
 	static LatLng initialCameraLocation = new LatLng(initialLatitude, initialLongitude);
 	static final int initialAnimateTime = 1;
 	
 	static final Long updateFreq = 1000L; //Every 1000 ms
 	static final Float updateDist = 10F; //10 Meters
 	static final Float defaultZoomLevel = 17F; //Ranges from 2 (Furthest) - 21 (Closest)
 	private boolean mapIsLoaded = false;
 
 	GoogleMap map;
 	
 	private OnLocationChangedListener locationListener;
 	private LocationManager locationManager;
 	
 	@Override
 	public View onCreateView(LayoutInflater arg0, ViewGroup arg1, Bundle savedInstanceState) {
 	    View view = super.onCreateView(arg0, arg1, savedInstanceState);
 	    
 	    if(savedInstanceState == null){
 		    setRetainInstance(true);
 		    if(getArguments() != null){
 		    	stationList = getArguments().getParcelableArrayList("stationList");
 		    	Log.d("DEBUG", "Mapview got: " + stationList.size() + " stations.");
 		    	init();
 		    }
 		    else
 		    	Log.d("DEBUG", "MapView has a null station list.");
 	    }
 	    
 	    return view;
 	}
 	
 	public void init(){
 		if(googleMapsIsInstalled()){
     		updateUserLocation();
     		initMap();
     	}
     	else{
     		//Tell user to install google maps
     		installGoogleMaps();
     	}
 	}
 	
 	@Override
 	public void onPause(){
 		//Pause the location updates
 		if(locationManager != null){
 			locationManager.removeUpdates(this);
 		}
 		super.onPause();
 	}
 	
 	@Override
 	public void onResume(){
 		super.onResume();
 		if(locationManager != null){
 			map.setMyLocationEnabled(true);
 		}
 	}
 	
 	private void initMap(){
 		UiSettings mapsSettings = getMap().getUiSettings();
 		
 		placeMarkers();
 		map = getMap();
 		if(!mapIsLoaded){
 			//Map is loading for the first time, so move the camera to Toronto until we get the GPS location	
 			map.animateCamera(CameraUpdateFactory.newLatLngZoom(initialCameraLocation, initialZoomLevel), initialAnimateTime, new CancelableCallback() {
 		        @Override
 		        public void onFinish() {}
 		        @Override
 		        public void onCancel() {}
 		    });
 			
 
 		}
 		map.setLocationSource(this);
 		map.setMyLocationEnabled(true);
 		mapIsLoaded = true;
 		//map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
 		
 		
 	}
 	
 	private void updateUserLocation(){
 		locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
 		
 		if(locationManager != null){
 			boolean gpsIsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 			boolean networkLocationIsEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 			
 			if(gpsIsEnabled){
 				//Use gps to locate the user
 				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateFreq, updateDist, this);
 			}
 			else if(networkLocationIsEnabled){
 				//Locate user based on their network (less accurate)
 				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, updateFreq, updateDist, this);
 			}
 			else{
 				//TODO Show dialog telling user to enable location services
 				Log.d("DEBUG", "Location services not enabled.");
 			}
 		}
 		else{
 			//TODO Show dialog if something goes terribly wrong here with Location Manager
 			Log.d("DEBUG", "Something went wrong with location manager.");
 		}
 	}
 	
 	private void installGoogleMaps(){
 		Builder b = new AlertDialog.Builder(getActivity());
 		b.setMessage(getString(R.string.google_maps_missing));
 		b.setPositiveButton(getString(R.string.google_maps_install_button), installGoogleMapsListener());
 		AlertDialog dialog = b.create();
 		dialog.show();
 	}
 	
 	private void placeMarkers(){
 		Log.d("DEBUG", "Station list size: " + stationList.size());
 		
 		markerList = new ArrayList<MarkerOptions>();
 		MarkerOptions currentMarker;
 		BikeStation currentStation = null;
 		for(int i = 0; i < stationList.size(); i++){
 			currentMarker = new MarkerOptions();
 			currentStation = stationList.get(i);
 			
 			currentMarker.position(new LatLng(currentStation.getLatitude(), currentStation.getLongitude()));
 			currentMarker.title(currentStation.getStationName());
 			currentMarker.snippet("Bikes: " + currentStation.getNbBikes() + " Empty: " + currentStation.getNbEmptyDocks());
 			markerList.add(currentMarker);
 			getMap().addMarker(currentMarker);
 		}
 		
 	}
 	
 	public void updateMarkers(){
 		getMap().clear();
 		placeMarkers();
 	}
 	public void updateStationList(ArrayList<BikeStation> stationList){
 		Log.d("DEBUG", "Update map called.");
 		this.stationList = stationList;
 		if(!mapIsLoaded)
 			init();
 		else
 			updateMarkers();
 		Log.d("DEBUG", "Got: " + stationList.size() + " stations in the map.");
 	}
 	
 	public boolean googleMapsIsInstalled(){
 		try{
 			ApplicationInfo info = this.getActivity().getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0);
 			//If no exception is returned, then Google Maps is installed
 			return true;
 		}
 		catch(PackageManager.NameNotFoundException e){
 			//Google Maps is not installed
 			return false;
 		}
 	}
 	
 	public OnClickListener installGoogleMapsListener(){
 		return new OnClickListener(){
 
 			@Override
 			public void onClick(DialogInterface dialog, int arg1) {
 				
 				//Launch the market to install google maps
 				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.maps"));
 				startActivity(intent);
 				
 				getActivity().finish();
 			}
 			
 		};
 	}
 
 	@Override
 	public void activate(OnLocationChangedListener locationChangedListener) {
 		locationListener = locationChangedListener;
 	}
 
 	@Override
 	public void deactivate() {
 		locationListener = null;
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		if(locationListener != null){
 			locationListener.onLocationChanged(location);
 			//Center camera on user
 			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), defaultZoomLevel));
 		}
 	}
 
 	//These methods aren't used or required?
 	@Override
 	public void onProviderDisabled(String arg0) {
 		// TODO Auto-generated method stub
 		Log.d("DEBUG", "Maps onProviderDisabled method called.");
 	}
 
 	@Override
 	public void onProviderEnabled(String arg0) {
 		// TODO Auto-generated method stub
 		Log.d("DEBUG", "Maps onProviderEnabled method called.");
 	}
 
 	@Override
 	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 		// TODO Auto-generated method stub
 		Log.d("DEBUG", "Maps onStatusChanged method called.");
 		
 	}
 }
