 package com.example.taximap.map;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.example.taximap.Constants;
 import com.example.taximap.Login;
 import com.example.taximap.R;
 import com.example.taximap.db.QueryDatabaseCustomerLoc;
 import com.example.taximap.db.QueryDatabaseDriverLoc;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.LocationSource;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.UiSettings;
 import com.google.android.gms.maps.model.BitmapDescriptor;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.FragmentActivity;
 import android.text.format.Time;
 import android.util.Log;
 import android.util.SparseIntArray;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.*;
 
 public class MapViewActivity extends FragmentActivity implements
 		OnClickListener, LocationListener, LocationSource {
 	private static GoogleMap gmap;
 	public static int markerType = Constants.DRIVER;
 	public static List<Driver> driverLst;
 	public static List<Customer> customerLst;
 	private static Driver currentDriver;
 	private static Customer currentCustomer;
 	private static LatLngBounds.Builder boundsBuilder;
 	private static LatLngBounds currentBounds = null;
 	private static Handler loadMarkerHandler;
 	private static Runnable loadMarkerRunnable;
 	public static String uID = "10";
 	public static String uName = "TaxiMap User";
 	public static LatLng myLastLatLng = new LatLng(39.983434, -83.003082);
 	public static String myLastAddress = "Mahoning CT, Columbus OH 43210";
 	private static boolean hailStatus = false;
 	private static LocationManager locationManager;
 	private static final String TAG = "-------------";
 	private static Activity context;
 	private static LocationListener locationListener;
 	private static Map<String, String> companies;
 	private static Map<String, Integer> ratings;
 	private static Map<String, Integer> distance;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		context = this;
 		locationListener = (LocationListener) this;
 		// Create user filters
 		createFilters();
 		setContentView(R.layout.content_map_layout);
 		gmap = ((SupportMapFragment) getSupportFragmentManager()
 				.findFragmentById(R.id.map)).getMap();
 		setupMapView();
 		enableLocationUpdate();
 		loadMarkerHandler = new Handler();
 		loadMarkerRunnable = new Runnable() {
 			public void run() {
 				callDB();
 				long delayTime = 60000;
 				loadMarkerHandler.postDelayed(this, delayTime);
 			}
 		};
 		// callDB();
 		new Thread(loadMarkerRunnable).run();
 		((Button) findViewById(R.id.hailTaxi)).setOnClickListener(this);
 	}
 
 	private void enableLocationUpdate() {
 		gmap.setLocationSource(this);
 		gmap.setMyLocationEnabled(true);
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		if (locationManager != null) {
 			boolean gpsIsEnabled = locationManager
 					.isProviderEnabled(LocationManager.GPS_PROVIDER);
 			boolean networkIsEnabled = locationManager
 					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 			// update real time location every 5s
 			long timeInterval = 60000;
 			float distDifference = 30;
 			if (gpsIsEnabled) {
 				// public void requestLocationUpdates (String provider, long
 				// minTime, float minDistance, LocationListener listener)
 				// min time interval 5s, min difference meters 10m.
 				locationManager.requestLocationUpdates(
 						LocationManager.GPS_PROVIDER, timeInterval,
 						distDifference, locationListener);
 			} else {
 				Toast.makeText(this,
 						"GPS is disabled will try Network location",
 						Toast.LENGTH_LONG).show();
 			}
 			if (networkIsEnabled) {
 				locationManager.requestLocationUpdates(
 						LocationManager.NETWORK_PROVIDER, timeInterval,
 						distDifference, locationListener);
 			} else { // Show an error dialog that GPS is disabled...
 				Toast.makeText(
 						this,
 						"Both GPS and Network are disabled. Please turn on GPS.",
 						Toast.LENGTH_LONG).show();
 				Intent gpsOptionsIntent = new Intent(
 						android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 				startActivity(gpsOptionsIntent);
 			}
 		} else { // Show some generic error dialog because something must have
 					// gone wrong with
 			Toast.makeText(this, "Location manager error", Toast.LENGTH_SHORT)
 					.show();
 		}
 	}
 
 	// As Vince said, this func is not needed.
 	/*
 	 * private void disableLocationUpdate(){ gmap.setMyLocationEnabled(false);
 	 * locationManager.removeUpdates(this); }
 	 */
 
 	private void setupMapView() {
 		UiSettings settings = gmap.getUiSettings();
 		settings.setAllGesturesEnabled(true);
 		settings.setCompassEnabled(true);
 		settings.setZoomControlsEnabled(false);
 		gmap.animateCamera(CameraUpdateFactory
 				.newCameraPosition(new CameraPosition(new LatLng(39.983434,
 						-83.003082), 8f, 0, 0)));
 		gmap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
 		gmap.setTrafficEnabled(true);
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		// Create current user marker if not exists.
 		// move current user marker if exists.
 		double latitude = location.getLatitude();
 		double longitude = location.getLongitude();
 		myLastLatLng = new LatLng(latitude, longitude);
 		// get physical address from that lat lon location
 		GeolocationHelper.getAddressFromLocation(location, this,
 				new GeocoderHandler());
 		// add a marker to show current user location.
 		if (markerType == Constants.DRIVER) {
 			if (currentDriver == null) {
 				BitmapDescriptor icon = BitmapDescriptorFactory
 						.fromResource(R.drawable.customerdefault);
 				MarkerOptions markerOptions = new MarkerOptions()
 						.position(myLastLatLng).title(uName).icon(icon);
 				currentCustomer = new Customer(myLastLatLng, uName);
 				currentCustomer.markerOptions = markerOptions;
 				currentCustomer.marker = gmap.addMarker(markerOptions);
 				gmap.animateCamera(CameraUpdateFactory
 						.newCameraPosition(new CameraPosition(myLastLatLng,
 								10f, 0, 0)));
 				if (currentBounds == null) {
 					boundsBuilder = new LatLngBounds.Builder();
 				}
 				boundsBuilder.include(myLastLatLng);
 				currentBounds = boundsBuilder.build();
 				// gmap.moveCamera(CameraUpdateFactory.newLatLngBounds(currentBounds,
 				// 50)); //50px
 			} else {
 				currentCustomer.marker.setPosition(myLastLatLng);
 			}
 		}
 	}
 
 	private static class GeocoderHandler extends Handler {
 		@Override
 		public void handleMessage(Message message) {
 			String address;
 			switch (message.what) {
 			case 1:
 				Bundle bundle = message.getData();
 				address = bundle.getString("address");
 				myLastAddress = address;
 				showUserMarker();
 				ProfileViewActivity.updateLocation(myLastAddress, myLastLatLng);
 				break;
 			}
 		}
 	}
 
 	private static void showUserMarker() {
 		if (markerType == Constants.DRIVER) {
 			currentCustomer.marker.setSnippet(myLastAddress);
 			new Thread() {
 				@Override
 				public void run() {
 					while (MapViewActivity.currentCustomer.marker == null) {
 						// loop and wait for currentCustomer constructor to
 						// complete
 						Log.i(TAG, "currentCustomer.marker==null");
 					}
 					context.runOnUiThread(new Runnable() {
 						public void run() {
 							MapViewActivity.currentCustomer.marker
 									.showInfoWindow();
 							try {
 								Thread.sleep(2000);
 							} catch (InterruptedException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 							MapViewActivity.currentCustomer.marker
 									.hideInfoWindow();
 						}
 					});
 				}
 			}.start();
 		}
 	}
 
 	public static void callDB() {
 		if (markerType == Constants.DRIVER) {
 			Log.d("----", uID);
 			(new QueryDatabaseDriverLoc()).execute(uID,
 					Double.toString(myLastLatLng.latitude),
 					Double.toString(myLastLatLng.longitude)); // pass in uid.
 																// modify
 		} else {
 			Log.d("----", uID);
 			(new QueryDatabaseCustomerLoc()).execute(uID);
 		}
 	}
 
 	// load markers to the map every 5 seconds based on filter and
 	// classification setting.
 	// first query db based on myLastLatLng
 	// render markers on the map using classification and filter settings.
 	public static void loadMarkers() {
 		
 		try {
 			// clear all markers except for the current user
 			if (markerType == Constants.DRIVER) {
 				gmap.clear();/*
 							 * if (driverLst != null) { for(Driver d:driverLst){
 							 * if(d.marker!=null){ d.marker.remove(); }else{
 							 * break; } } }
 							 */
 				if (currentCustomer != null) {
 					gmap.addMarker(currentCustomer.markerOptions);
 					showUserMarker();
 				}
 				for (Driver driver : driverLst) {
 					driver.isActive = true;
 				}
 				if (FilterActivity.filters != null) { // filter is previously
 														// set
 					for (int key : FilterActivity.filters.get(Constants.DRIVER)
 							.keySet()) {
 						String value = FilterActivity.filters.get(
								Constants.DRIVER).get(key);
 						if (!value.equals("Any")) { // is not "Any"
 							if (key == Constants.COMPANY) {
 								for (Driver driver : driverLst) {
 									if (!driver.company.equals(companies
 											.get(value))) {
 										driver.isActive = false;
 									}else{
 										driver.isActive = true;
 									}
 								}
 								FilterActivity.classificationCode[0] = '1';		// should classify by company by default
 							}
 							if (key == Constants.RATING) {
 								for (Driver driver : driverLst) {
 									if (driver.rating < ratings.get(value)) {
 /*										Log.e(TAG, String.format("%s<%s",
 												driver.rating,
 												ratings.get(value)));*/
 										driver.isActive = false;
 									}else{
 										driver.isActive = true;
 									}
 								}
 								FilterActivity.classificationCode[1] = '1';		// should classify by rating by default
 							}
 							if (key == Constants.DISTANCE) {
 								for (Driver driver : driverLst) {
 									if (driver.distance > distance.get(value)) {
 										driver.isActive = false;
 									}
 									else{
 										driver.isActive = true;
 									}
 								}
 							}
 						}
 					}
 				}
 				// map out active markers and classify by assigning different
 				// icons
 				boundsBuilder = new LatLngBounds.Builder();
 				if (myLastLatLng != null) {
 					boundsBuilder.include(myLastLatLng);
 				}
 				for (Driver driver : driverLst) {
 					if (driver.isActive) {
 						BitmapDescriptor icon = findIcon(driver);
 						MarkerOptions markerOptions = new MarkerOptions()
 								.position(driver.latlng).title(driver.title())
 								.snippet(driver.snippet()).icon(icon);
 						driver.markerOptions = markerOptions;
 						driver.marker = gmap.addMarker(markerOptions);
 						boundsBuilder.include(driver.latlng);
 					}
 				}
 				currentBounds = boundsBuilder.build();
 
 				gmap.moveCamera(CameraUpdateFactory.newLatLngBounds(
 						currentBounds, 50)); // padding 50
 
 				// showMessages(this, "Drivers Updated");
 				/* updateListView(); */
 				ListViewActivity.createList();
 			} else if (markerType == Constants.CUSTOMER) {
 			}
 		} catch (java.lang.IllegalStateException e) {
 			// Do not load markers if map size is 0
 			// Markers will be loaded next time location is updated
 		}
 
 	}
 
 	private static BitmapDescriptor findIcon(Driver driver) {
 		BitmapDescriptor icon = null;
 		// company, rating
 		// classificationScheme={"00","10","01","11"};
 		// 10 classify by company
 		String s = new String(FilterActivity.classificationCode);
 		if (s.equals("10")) {
 			Map<String, Integer> resource = new HashMap<String, Integer>();
 			resource.put("Blue Cab", R.drawable.taxibluedefault);
 			resource.put("Yellow Cab", R.drawable.taxiyellowdefault);
 			resource.put("Green Cab", R.drawable.taxigreendefault);
 			try {
 				icon = BitmapDescriptorFactory.fromResource(resource
 						.get(driver.company));
 			} catch (Exception e) {
 				icon = BitmapDescriptorFactory
 						.fromResource(R.drawable.taxidefault);
 			}
 
 		} else if (s.equals("01")) {
 			SparseIntArray resource = new SparseIntArray();
 			resource.put(5, R.drawable.taxi5);
 			resource.put(4, R.drawable.taxi4);
 			resource.put(3, R.drawable.taxi3);
 			resource.put(2, R.drawable.taxi2);
 			resource.put(1, R.drawable.taxi1);
 			try {
 				icon = BitmapDescriptorFactory.fromResource(resource
 						.get(driver.rating));
 			} catch (Exception e) {
 				icon = BitmapDescriptorFactory
 						.fromResource(R.drawable.taxidefault);
 			}
 		} else if (s.equals("11")) {
 			Map<String, Integer> resource = new HashMap<String, Integer>();
 			resource.put("Blue Cab5", R.drawable.taxiblue5);
 			resource.put("Blue Cab4", R.drawable.taxiblue4);
 			resource.put("Blue Cab3", R.drawable.taxiblue3);
 			resource.put("Blue Cab2", R.drawable.taxiblue2);
 			resource.put("Blue Cab1", R.drawable.taxiblue1);
 			resource.put("Yellow Cab5", R.drawable.taxiyellow5);
 			resource.put("Yellow Cab4", R.drawable.taxiyellow4);
 			resource.put("Yellow Cab3", R.drawable.taxiyellow3);
 			resource.put("Yellow Cab2", R.drawable.taxiyellow2);
 			resource.put("Yellow Cab1", R.drawable.taxiyellow1);
 			resource.put("Green Cab5", R.drawable.taxigreen5);
 			resource.put("Green Cab4", R.drawable.taxigreen4);
 			resource.put("Green Cab3", R.drawable.taxigreen3);
 			resource.put("Green Cab2", R.drawable.taxigreen2);
 			resource.put("Green Cab1", R.drawable.taxigreen1);
 			try {
 				icon = BitmapDescriptorFactory.fromResource(resource
 						.get(driver.company + Integer.toString(driver.rating)));
 			} catch (Exception e) {
 				icon = BitmapDescriptorFactory
 						.fromResource(R.drawable.taxidefault);
 			}
 		} else {
 			// default driver icon
 			icon = BitmapDescriptorFactory.fromResource(R.drawable.taxidefault);
 		}
 		return icon;
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 		Toast.makeText(this, "provider disabled", Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 		Toast.makeText(this, "provider enabled", Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 		Toast.makeText(this, "Location Updated!", Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	public void activate(OnLocationChangedListener arg0) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void deactivate() {
 		// TODO Auto-generated method stub
 	}
 
 	public void onCameraChange(CameraPosition arg0) {
 		// Move camera.
 		gmap.moveCamera(CameraUpdateFactory.newLatLngBounds(
 				boundsBuilder.build(), 10));
 		// Remove listener to prevent position reset on camera move.
 		gmap.setOnCameraChangeListener(null);
 	}
 
 	@Override
 	public void onClick(View v) {
 		int yellow = R.color.Yellow;
 		int lightYellow = R.color.LightYellow;
 		switch (v.getId()) {
 		case R.id.hailTaxi:
 			if (hailStatus == true) {
 				v.setBackgroundColor(getResources().getColor(lightYellow));
 				ProfileViewActivity.cancelHail();
 				Toast.makeText(this, "Pick-up request Cancelled.",
 						Toast.LENGTH_SHORT).show();
 				hailStatus = false;
 			} else {
 				v.setBackgroundColor(getResources().getColor(yellow));
 				Time today = new Time(Time.getCurrentTimezone());
 				today.setToNow();
 				String waitTime = getAverageArrivalTime();
 				ProfileViewActivity.updateHail(today.format("%k:%M:%S"),
 						waitTime);
 				Toast.makeText(
 						this,
 						"Pick-up request sent to all drivers in the view. Arrive in "
 								+ " " + waitTime + "  minutes",
 						Toast.LENGTH_SHORT).show();
 				hailStatus = true;
 			}
 		}
 	}
 
 	private static String getAverageArrivalTime() {
 		double sum = 0;
 		int count = 0;
 		for (Driver d : driverLst) {
 			if (d.isActive) { // 30 miles/hour speed
 				sum += d.distance;
 				count++;
 			}
 		}
 		return Integer.toString((int) (sum / count / 30 * 60));
 	}
 
 	// Creates user filters
 	private static void createFilters() {
 		// set up filter mapping
 		companies = new HashMap<String, String>();
 		companies.put("Blue Cab", "Blue Cab");
 		companies.put("Yellow Cab", "Yellow Cab");
 		companies.put("Green Cab", "Green Cab");
 		ratings = new HashMap<String, Integer>();
 		ratings.put("5 Stars", 5);
 		ratings.put("4 Stars and Above", 4);
 		ratings.put("3 Stars and Above", 3);
 		ratings.put("2 Stars and Above", 2);
 		ratings.put("1 Star and Above", 1);
 		distance = new HashMap<String, Integer>();
 		distance.put("Within 30 mins", 15); // 15 miles, 30 miles/hour speed
 		distance.put("Within 20 mins", 10);
 		distance.put("Within 10 mins", 5);
 	}
 
 	private boolean doubleBackToExitPressedOnce = false;
 
 	@Override
 	protected void onResume() { // called when logged in with authentication.
 		super.onResume();
 		enableLocationUpdate();
 		// .... other stuff in my onResume ....
 		this.doubleBackToExitPressedOnce = false;
 		Log.e(TAG, "Map view onResume()");
 	}
 
 	@Override
 	public void onBackPressed() { // this handler helps to reset the variable
 									// after 2 second.
 		if (doubleBackToExitPressedOnce) {
 			// super.onBackPressed();
 			finish();
 			Login.exitStatus = true;
 			return;
 		}
 		// super.onBackPressed();
 		this.doubleBackToExitPressedOnce = true;
 		Toast.makeText(this, "Please click BACK again to exit",
 				Toast.LENGTH_SHORT).show();
 		new Handler().postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				doubleBackToExitPressedOnce = false;
 			}
 		}, 2000);
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 		Log.e(TAG, "Map view onDestroy()");
 
 	}
 
 	public void onPause() {
 		super.onPause();
 		Log.e(TAG, "Map view onPause()");
 	}
 
 	// this is the callback called for pressing both double clicking back
 	// or single clicking home button.
 	public void onStop() {
 		super.onStop();
 		// Log.e(TAG, "Map view onStop()");
 		diableLocationUpdate();
 	}
 
 	public void onStart() {
 		super.onStart();
 		Log.e(TAG, "Map View onStart()");
 	}
 
 	public void onRestart() {		
 		super.onRestart();
 		if(driverLst!=null){
 			loadMarkers();
 		}
 		Log.e(TAG, "Map View onRestart()");
 	}
 
 	public static void diableLocationUpdate() {
 		locationManager.removeUpdates(locationListener); // remove location
 															// updates after app
 															// exits
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		FragmentTabsActivity.currentTabIndex = 0;
 	}
 }
