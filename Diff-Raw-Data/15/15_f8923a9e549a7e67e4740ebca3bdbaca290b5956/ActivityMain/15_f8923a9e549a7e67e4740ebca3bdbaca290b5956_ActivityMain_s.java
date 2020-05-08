 package put.medicallocator.ui;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import put.medicallocator.R;
 import put.medicallocator.io.Facility;
 import put.medicallocator.io.IFacilityProvider;
 import put.medicallocator.io.IFacilityProvider.AsyncQueryListener;
 import put.medicallocator.io.IFacilityProviderManager;
 import put.medicallocator.ui.overlay.BasicItemizedOverlay;
 import put.medicallocator.ui.overlay.FacilityOverlayItem;
 import put.medicallocator.utils.GeoUtils;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 
 public class ActivityMain extends MapActivity implements AsyncQueryListener {
 
 	// TODO: Add the options to turn off/on location tracking.
 	
 	private static final String TAG = ActivityMain.class.getName(); 
 	
 	/** Defines the start GeoPoint. Yeah, let's all do the Poznan! ;) */
 	private static final GeoPoint START_GEOPOINT = GeoUtils.convertToGeoPoint(52.408396, 16.92838);
 	/** Defines the start zoom level. */
 	private static final int START_ZOOM_LEVEL = 14;
 	
 	private int currentDistanceInKilometers = ActivityFilter.DEFAULT_DISTANCE_IN_KILOMETERS;
 	
     private MapView mapView;
     private MyLocationOverlay locationOverlay;
     private LocationListener myLocationListener;
     private GeoPoint lastVisibleGeoPoint;
     private int noChangeCounter = 0;
    private String[] filters = null;
     
     private Handler handler;
     private State state;
     
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
 		/* Initialize a handler here to ensure that it is attached to the UI thread */
 		handler = new Handler();
 
 		/* Do we have previous instance data? */
 		state = (State) getLastNonConfigurationInstance();
 		final boolean hasPreviosState = state == null ? false : true;
 		
 		if (!hasPreviosState) {
 			// This initializes also the default values. 
 			state = new State();
 		}
 		
         /* Initialize the IFacilityProvider */
 		if (!IFacilityProviderManager.isInitialized()) {
 			Toast.makeText(this, 
 				getResources().getString(R.string.activitymain_initializing_provider), 
 				Toast.LENGTH_SHORT).show();
 			IFacilityProviderManager.getInstance(this);
 		}
         
         /* Set the basic attributes of the MapView */
 		mapView = (MapView) findViewById(R.id.map_view);
 		locationOverlay = new MyLocationOverlay(this, mapView);
 		mapView.setBuiltInZoomControls(true);
 		myLocationListener = new MyLocationListener();
 
 		final MapController mapController = mapView.getController();
 		mapController.setCenter(state.currentPoint);
 		mapController.setZoom(state.zoomLevel);
     }
 
     @Override
     protected void onResume() {
     	super.onResume();
 		/* Register for the Location updates */
         final LocationManager locationManager = 
         	(LocationManager) getSystemService(Context.LOCATION_SERVICE);    
         
         locationManager.requestLocationUpdates(
             LocationManager.GPS_PROVIDER, 
             0, 
             0, 
             myLocationListener);   
 
         locationManager.requestLocationUpdates(
             LocationManager.NETWORK_PROVIDER, 
             0, 
             0, 
             myLocationListener);
 
         /* Enable the MyLocationOverlay as well */
         locationOverlay.enableMyLocation();
         locationOverlay.runOnFirstFix(onFirstFixRunnable);
 		mapView.getOverlays().add(locationOverlay);
 		
         /* Post query job */
 		noChangeCounter = 0;
 		handler.post(doQueryIfRequired);
 	}
     
     @Override
     protected void onPause() {
     	super.onPause();
     	/* Unregister for the Location updates */
     	final LocationManager locationManager = 
         	(LocationManager) getSystemService(Context.LOCATION_SERVICE);    
     	locationManager.removeUpdates(myLocationListener);
     	
         /* Disable the MyLocationOverlay as well */
     	locationOverlay.disableMyLocation();
     	
     	/* Disable the queries */
     	handler.removeCallbacks(doQueryIfRequired);
     }
     
     @Override
     public Object onRetainNonConfigurationInstance() {
     	handler.removeCallbacks(doQueryIfRequired);
     	state.currentPoint = mapView.getMapCenter();
     	state.zoomLevel = mapView.getZoomLevel();
     	return state;
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	final MenuInflater menuInflater = getMenuInflater();
     	menuInflater.inflate(R.menu.activity_main_menu, menu);
     	return true;
     }
     
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
     	final MenuItem trackingItem = menu.findItem(R.id.tracking_menuitem);
     	if (state.isTrackingEnabled) {
     		trackingItem.setTitle(getResources().getString(R.string.activitymain_disabletracking));
     	} else {
     		trackingItem.setTitle(getResources().getString(R.string.activitymain_enabletracking));    		
     	}
     	return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
     		case R.id.filter_menuitem:
     			openFilterOptions();
     			return true;
     		case R.id.tracking_menuitem:
     			state.isTrackingEnabled = !state.isTrackingEnabled;
     			return true;
     		case R.id.search_menuitem:
     			onSearchRequested();
     			return true;
     		case R.id.about_menuitem:
     			showAbout();
     			return true;
     		default:
     			return super.onOptionsItemSelected(item);
     	}
     }
     
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 			case ActivityFilter.FILTER_REQUEST_CODE:
 				if (resultCode == Activity.RESULT_OK) {
 					setFilters(data);
 				}
 			default:
 				break;
 		}
 	}
 	
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	public void onQueryComplete(int token, Cursor cursor,
 			Map<String, Integer> columnMapping) {
 		Log.e(TAG, "Query finished. Returned rows: " + cursor.getCount());
 		if (!cursor.moveToFirst()) {
 			cursor.close();
 			runOnUiThread(new Runnable() {
 				public void run() {
 					final List<Overlay> overlays = mapView.getOverlays();
 					overlays.clear();
 					overlays.add(locationOverlay);
 					Log.d(TAG, "Posting invalidate on MapView");
 					mapView.invalidate();
 				}
 			});
 			return;
 		}
 		
 		final Drawable marker = getResources().getDrawable(R.drawable.marker);
 		final BasicItemizedOverlay itemizedOverlay = new BasicItemizedOverlay(this, marker);
 
 		synchronized (ActivityMain.class) {
 			Log.d(TAG, "Starting going through the results - creating the overlays");
 			final double start = System.currentTimeMillis();
 			do {
 				final String name = cursor.getString(columnMapping.get(Facility.Columns.NAME));
 				final String address = cursor.getString(columnMapping.get(Facility.Columns.ADDRESS));
 				final String phone = cursor.getString(columnMapping.get(Facility.Columns.PHONE));
 				final String email  = cursor.getString(columnMapping.get(Facility.Columns.EMAIL));
 				final double latitude = cursor.getDouble(columnMapping.get(Facility.Columns.LATITUDE));
 				final double longitude = cursor.getDouble(columnMapping.get(Facility.Columns.LONGITUDE));
 				final GeoPoint geoPoint = GeoUtils.convertToGeoPoint(latitude, longitude);
 				
 				final Facility facility = new Facility();
 				facility.setName(name);
 				facility.setAddress(address);
 				facility.setPhone(phone);
 				facility.setEmail(email);
 				facility.setLocation(geoPoint);
 				final FacilityOverlayItem overlayItem = new FacilityOverlayItem(facility, geoPoint);
 				itemizedOverlay.addOverlay(overlayItem);
 			} while (cursor.moveToNext());
 			Log.d(TAG, "Overlays created. It took: " + (System.currentTimeMillis() - start) + " ms");
 			
 			// TODO: Find a less hacky way to avoid performance problem.
 			itemizedOverlay.populateNow();
 			
 			runOnUiThread(new Runnable() {
 				public void run() {
 					final List<Overlay> overlays = mapView.getOverlays();
 					overlays.clear();
 					overlays.add(itemizedOverlay);
 					overlays.add(locationOverlay);
 					Log.d(TAG, "Posting invalidate on MapView");
 					mapView.invalidate();
 				}
 			});
 		}
 		
 		cursor.close();
 	}
 
 	private Runnable doQueryIfRequired = new Runnable() {
 		public void run() {
 			/* Save the exact coordinates of the top-left visible vertex of MapView */
 			final GeoPoint currentVisibleGeoPoint = mapView.getMapCenter();
 			final int currentLatitude = currentVisibleGeoPoint.getLatitudeE6();
 			final int currentLongitude = currentVisibleGeoPoint.getLongitudeE6();
 
 			if (lastVisibleGeoPoint == null) {
 				noChangeCounter = 0;
 				lastVisibleGeoPoint = currentVisibleGeoPoint;
 			} else {
 				final int lastLatitude = lastVisibleGeoPoint.getLatitudeE6();
 				final int lastLongitude = lastVisibleGeoPoint.getLongitudeE6();
 				
 				if (lastLatitude == currentLatitude && lastLongitude == currentLongitude) {
 					noChangeCounter++;
 				} else {
 					noChangeCounter = 0;
 					lastVisibleGeoPoint = currentVisibleGeoPoint;
 				}
 			}
 			
 			if (noChangeCounter == 2) {
 				IFacilityProvider provider = IFacilityProviderManager.getInstance(ActivityMain.this);
 				
 				// TODO: Make the distance dependent on the current zooming.
 				final GeoPoint[] boundingGeoPoints = 
 					GeoUtils.boundingCoordinates(
 							currentDistanceInKilometers, currentVisibleGeoPoint);
 				final GeoPoint minGeoPoint = boundingGeoPoints[0];
 				final GeoPoint maxGeoPoint = boundingGeoPoints[1];
 				
 				final double minLatitude = GeoUtils.convertToDegrees(minGeoPoint)[0];
 				final double minLongitude = GeoUtils.convertToDegrees(minGeoPoint)[1];
 				final double maxLatitude = GeoUtils.convertToDegrees(maxGeoPoint)[0];
 				final double maxLongitude = GeoUtils.convertToDegrees(maxGeoPoint)[1];
 				
 				final Location lowerLeftLocation = new Location("");
 				final Location upperRightLocation = new Location("");
 				lowerLeftLocation.setLatitude(minLatitude);
 				lowerLeftLocation.setLongitude(minLongitude);
 				upperRightLocation.setLatitude(maxLatitude);
 				upperRightLocation.setLongitude(maxLongitude);
 				
 				try {
 					provider.getFacilitiesWithinArea(
 							ActivityMain.this, 
							lowerLeftLocation, upperRightLocation, filters);
 				} catch (Exception e) {
 					// It shouldn't happen - even if the query just won't be executed.
 				}
 			}
 			
 			/* Post this job once again after 1000ms */
 			handler.postDelayed(doQueryIfRequired, 1000);
 		}
 	};
 
 	private void setFilters(Intent intent) {
 		currentDistanceInKilometers = intent.getIntExtra(
 				ActivityFilter.RESULT_DISTANCE, 
 				ActivityFilter.DEFAULT_DISTANCE_IN_KILOMETERS);
		filters = intent.getStringArrayExtra(ActivityFilter.RESULT_FILTER_ARRAY);;
 		
 		Log.d(TAG, "Received filters: [ " + currentDistanceInKilometers + " km], " 
				+ Arrays.toString(filters));
 	}
 
 	private void openFilterOptions() {
 		final Intent intent = new Intent(this, ActivityFilter.class);
		intent.putExtra(ActivityFilter.INPUT_FILTER_ARRAY, filters);
 		intent.putExtra(ActivityFilter.INPUT_DISTANCE, currentDistanceInKilometers);
 		startActivityForResult(intent, ActivityFilter.FILTER_REQUEST_CODE);
 	}
 
 	private void showAbout() {
 		LayoutInflater inflater = getLayoutInflater(); 
 		View layout = inflater.inflate(R.layout.dialog_about, null);
 
 		try {
 			String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
 			String version = String.format(getString(R.string.app_version), versionName);
 			final TextView versionTextView = (TextView) layout.findViewById(R.id.dialogabout_appversion); 
 			versionTextView.setText(version);
 		} catch (NameNotFoundException e) {
 			// No version found, not critical..
 		}
 		
 		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setView(layout);
 		final AlertDialog dialog = builder.create();
 		dialog.setTitle(getString(R.string.app_name));
 		dialog.show();
 	}
 	
 	private class State {
 		public GeoPoint currentPoint;
 		public int zoomLevel;
 		public boolean isTrackingEnabled = false;
 		public boolean isGPSEnabled = false;
 		
 		private State () {
 			currentPoint = START_GEOPOINT;
 			zoomLevel = START_ZOOM_LEVEL;
 		}
 	}
 	
 	private class MyLocationListener implements LocationListener {
 
 		public void onLocationChanged(Location location) {
 			Log.d(TAG, "Received the Location update from " + location.getProvider() + ": " + 
 					location.getLatitude() + "; " + location.getLongitude());
 			if (mapView != null && state.isTrackingEnabled) {
 				final String provider = location.getProvider();
 				
 				if (LocationManager.NETWORK_PROVIDER.equals(provider)) {
 					// Use the NETWORK_PROVIDER only if GPS is not enabled.
 					if (state.isGPSEnabled) return;
 				} else if (LocationManager.GPS_PROVIDER.equals(provider)) {
 					state.isGPSEnabled = true;
 				}
 				
 				state.currentPoint = GeoUtils.convertToGeoPoint(
 						location.getLatitude(), location.getLongitude());
 
 				runOnUiThread(new Runnable() {
 					public void run() {
 						final MapController mapController = mapView.getController();
 						mapController.animateTo(state.currentPoint);
 					}
 				});
 			}
 		}
 
 		public void onProviderDisabled(String provider) {
 			if (LocationManager.GPS_PROVIDER.equals(provider)) 
 				state.isGPSEnabled = false;
 		}
 
 		public void onProviderEnabled(String provider) {
 			if (LocationManager.GPS_PROVIDER.equals(provider)) 
 				state.isGPSEnabled = true;
 		}
 
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			// No need to implement this here.
 		}
 	}
 	
 	private Runnable onFirstFixRunnable = new Runnable() {
 		
 		public void run() {
 			runOnUiThread(new Runnable() {
 				
 				public void run() {
 					final MapController mapController = mapView.getController();
 					mapController.animateTo(locationOverlay.getMyLocation());
 				}
 			});
 		}
 	};
 }
