 /*
  * Copyright (C) 2013 CampusUB1 Development Team
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.dev.campus.map;
 
 import java.text.Normalizer;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.dev.campus.CampusUB1App;
 import com.dev.campus.R;
 import com.dev.campus.map.Position.PositionType;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.CheckBox;
 import android.widget.SearchView;
 import android.widget.Toast;
 
 /**
  * Class responsible for the maps activity UI and lifecycle,
  * handles map display and animation, map markers and map search queries,
  * as well as location services.
  * 
  * @author CampusUB1 Development Team
  *
  */
 public class MapActivity extends Activity implements LocationListener {
 
 	// Google Play Services URL
 	private final String PLAY_SERVICES_URL = "http://play.google.com/store/apps/details?id=com.google.android.gms";
 
 	// Utilities
 	private final LatLng MAP_CENTER = new LatLng(44.80736, -0.596572);
 	private SearchView mSearchView;
 	private Resources mResources;
 	
 	// Camera position attributes
 	private final int BEARING = 69;
 	private final int DEFAULT_ZOOM = 16;
 	private final int SEARCH_ZOOM = 18;
 	// Position update frequency (ms)
 	private final int UPDATE_FREQUENCY = 5000;
 	
 	// Map-related objects
 	private GoogleMap mMap;
 	private Marker mCurrentLocation;
 	private LocationManager mLocationManager;
 	private HashMap<PositionType, ArrayList<Marker>> mMarkers;
 	private CheckBox mServices, mRestauration, mBuildings;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		mResources = getResources();
 		
 		if (isGooglePlayServicesAvailable()) {
 			setContentView(R.layout.activity_map);
 			mServices = (CheckBox) findViewById(R.id.services_check);
 			mRestauration = (CheckBox) findViewById(R.id.restaurants_check);
 			mBuildings = (CheckBox) findViewById(R.id.buildings_check); 
 			mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 
 			setupMap();
 			setupMarkers();
 			setupLocationServices();
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		// Fresh registration of location listeners after resuming activity
 		if (mLocationManager != null)
 			getNewProviders();
 	}
 	
 	@Override     
 	protected void onPause() {  
 		super.onPause();
 		// Remove location listeners on pause for battery saving
 		if (mLocationManager != null)
 			mLocationManager.removeUpdates(this);		
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.map_menu, menu);
 		mSearchView = (SearchView) menu.findItem(R.id.map_search).getActionView();
 		mSearchView.setOnQueryTextListener(mQueryTextListener);		
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_campus_pos:
 			goToPosition(MAP_CENTER, DEFAULT_ZOOM);
 			return true;	
 		case R.id.menu_my_pos:
 			goToPosition(mCurrentLocation.getPosition(), DEFAULT_ZOOM);
 			return true;
 		case android.R.id.home:
 			finish();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	/**
 	 * Handles state changes of filter checkboxes
 	 * @param view reference to the clicked view
 	 */
 	public void onCheckboxClicked(View view) {
 		switch(view.getId()) {
 		case R.id.services_check:
 			populateMap(PositionType.SERVICE, mServices.isChecked());
 			break;
 		case R.id.restaurants_check:
 			populateMap(PositionType.RESTAURANT, mRestauration.isChecked());
 			break;
 		case R.id.buildings_check:
 			populateMap(PositionType.BUILDING, mBuildings.isChecked());
 			break;
 		}
 	}
 	
 	/**
 	 * Checks if Google Play Services is installed on the device.
 	 * If not, launches a dialog to prompt user to install it.
 	 * @return true if Google Play Services is already installed, else false
 	 */
 	public boolean isGooglePlayServicesAvailable() {
 		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
 		if (status != ConnectionResult.SUCCESS) {
 			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 			alertDialogBuilder
 			.setTitle(R.string.play_services)
 			.setMessage(R.string.need_play_services)
 			.setCancelable(false)
 			.setPositiveButton(R.string.install, new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int id) {
 					finish();
 					try {		
 						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_SERVICES_URL));
 						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
 						intent.setPackage("com.android.vending");
 						startActivity(intent);
 					} catch (ActivityNotFoundException e) {
 						CampusUB1App.getInstance().showToast(R.string.no_play_store);
 					}
 				}})
 			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog,int id) {
 					finish();
 				}})
 			.create()
 			.show();
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Sets up the main map components, and centers camera to default position
 	 */
 	public void setupMap() {
 		CameraPosition UB1Position = new CameraPosition.Builder()
 		.target(MAP_CENTER)
 		.zoom(DEFAULT_ZOOM)                   
 		.bearing(BEARING)               
 		.build();  
 
 		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_UB1)).getMap();
 		mMap.getUiSettings().setCompassEnabled(false);
 		mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
 		mMap.moveCamera(CameraUpdateFactory.newCameraPosition(UB1Position));
 	}
 
 	/**
 	 * Initializes location services and network providers,
 	 * and calculates last known position
 	 */
 	public void setupLocationServices() {	
		if(!isGpsEnabled())
 			Toast.makeText(this, R.string.no_gps, Toast.LENGTH_SHORT).show();
 
 		// Register listeners	
 		getNewProviders();
 
 		// Initially use network provider
 		Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		LatLng currentPosition = new LatLng(latitude, longitude);
 
 		mCurrentLocation = mMap.addMarker(new MarkerOptions()
 		.position(currentPosition)
 		.title(mResources.getString(R.string.my_position))
 		.icon(BitmapDescriptorFactory.fromResource(R.drawable.user_location_marker)));   
 
 		if (location != null)
 			onLocationChanged(location);
 	}
 
 	/**
 	 * Adds all the custom positions defined in Position.java as markers on the map
 	 */
 	public void setupMarkers() {
 		mMarkers = new HashMap<PositionType, ArrayList<Marker>>();
 		for (PositionType type : PositionType.values())
 			mMarkers.put(type, new ArrayList<Marker>());
 		
 		for (Position pos : Position.values()) {
 			MarkerOptions options = new MarkerOptions()
 			.position(new LatLng(pos.getLat(), pos.getLng()))
 			.icon(BitmapDescriptorFactory.fromResource(pos.getType().getDrawableId()))
 			.draggable(false)
 			.title(pos.getName());
 			Marker marker = mMap.addMarker(options);
 			mMarkers.get(pos.getType()).add(marker);
 		}
 	}
 
 	/**
 	 * Toggles visibility of a collection of markers
 	 * @param markers the collection of markers
 	 * @param isChecked new visibility state
 	 */
 	public void populateMap(PositionType type, boolean isChecked) {
 		for (Marker marker : mMarkers.get(type))
 			marker.setVisible(isChecked);			
 	}
 
 	/**
 	 * Animates camera movement to a specified position
 	 * @param pos destination
 	 * @param zoom zoom level
 	 */
 	public void goToPosition(LatLng pos, int zoom) {
 		CameraPosition position  = new CameraPosition.Builder()
 		.target(pos)
 		.zoom(zoom)                   
 		.bearing(BEARING)               
 		.build();                  
 		mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
 	}
 	
 	/**
 	 * Looks up for a position that matches one of the database suggestions
 	 * and centers the camera on it if found
 	 * The suggestions list can be found in the "positions.xml" file
 	 * located in the res/values folder of the project
 	 * @param input description of the marker to search
 	 * @return the marker's ID if found, else null
 	 */
 	@SuppressLint("DefaultLocale")
 	public String searchPosition(String input) {
 		for (Position pos : Position.values()) {
 			for (int i = 0; i < pos.getSuggestions().length; i++) {
 				if (reformatString(pos.getSuggestions()[i]).startsWith(reformatString(input))) {
 					ArrayList<Marker> markerType = mMarkers.get(pos.getType());
 					String markerId = null;
 					for (Marker marker : markerType) {
 						if (pos.getId().equals(marker.getId())) {
 							markerId = marker.getId();
 							marker.setVisible(true);
 							marker.showInfoWindow();
 							break;
 						}
 					}
 					goToPosition(new LatLng(pos.getLat(), pos.getLng()), SEARCH_ZOOM);
 					return markerId;
 				}
 			}
 		}
 		Toast.makeText(this, R.string.map_not_found, Toast.LENGTH_SHORT).show();
 		return null;
 	}
 	
 	/**
 	 * Strips diacritics from argument string and sets it to lower case
 	 * @param str the string to reformat
 	 * @return 
 	 */
 	public String reformatString(String str) {
 		str = Normalizer.normalize(str, Normalizer.Form.NFD);
 		str = str.replaceAll("[^\\p{ASCII}]", "");
 		return str.toLowerCase();
 	}
 	
 	/**
 	 * Search query listener for handling query submissions
 	 */
 	final SearchView.OnQueryTextListener mQueryTextListener = new SearchView.OnQueryTextListener() {
 		@Override
 		public boolean onQueryTextChange(String text) {
 			return true;
 		}
 
 		@Override
 		public boolean onQueryTextSubmit(String query) {
 			searchPosition(query);
 			//Hide soft keyboard
 			if (getCurrentFocus() != null) {
 				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 				imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
 				mSearchView.clearFocus();
 			}
 			return true;
 		}
 	};
 
 	/**
 	 * Checks if GPS is currently enabled on the device
 	 * @return
 	 */
 	public boolean isGpsEnabled() {
 		return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 	}
 
 	/**
 	 * Registers listeners for location changes on both network providers
 	 */
 	public void getNewProviders() {
 		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
 		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_FREQUENCY, 0, this);
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {	
 		LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
 		mCurrentLocation.remove();
 		mCurrentLocation = mMap.addMarker(new MarkerOptions()
 		.position(currentPosition)
 		.title(mResources.getString(R.string.my_position))
 		.icon(BitmapDescriptorFactory.fromResource(R.drawable.user_location_marker)));  
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
