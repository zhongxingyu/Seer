 package com.playground.farecalculator.activity;
 
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
import com.playground.farecalculator.R;
 import com.playground.farecalculator.fares.IFare;
 import com.playground.farecalculator.fares.MumbaiAutoRickshawFare;
 import com.playground.farecalculator.gui.CurrentLocationsOverlay;
 import com.playground.farecalculator.utils.MotionDetector;
 
 public class JourneyFareCalculatorActivity extends MapActivity implements OnClickListener
 {
 	private LocationManager locationManager;
 	private MapView mapView;
 	private TextView resultView;
 	private GeoPoint geoPoint;
 	private MapController mapController;
 	private String bestProvider;
 	private String pre = "";
 	private static final int INITIAL_LATITUDE = 19114445;
 	private static final int INITIAL_LONGITUDE = 72897860;
 	private static final int MAXIMUM_INTERVAL_BETWEEN_UPDATES = 1000 * 5;
 	private static final int MINIMUM_INTERVAL_BETWEEN_UPDATES = 50;
 	private Location currentLocation;
 	private double lastLatitude;
 	private double lastLongitude;
 	private CurrentLocationsOverlay currentLocationOverlay;
 	private double totalDistance;
 	private long startTime;
 	private int waitTime;
 	private Button mBtnSart;
 	private Button mBtnStop;
 	private Button mBtnReset;
 	private boolean isStarted;
 	private MotionDetector motionDetector;
 	private CustomLocationListener locationListener;
 	private IFare fareCalculator;
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		fareCalculator = new MumbaiAutoRickshawFare();
 		motionDetector = new MotionDetector(this);
 		
 		resultView = (TextView) findViewById(R.id.result);
 		mapView = (MapView) findViewById(R.id.mapview);
 		mapView.setBuiltInZoomControls(true);
 		mapView.setSatellite(false);
 		mapController = mapView.getController();
 
 		mapView.setBuiltInZoomControls(true);
 		mapView.displayZoomControls(true);
 
 		locationListener = new CustomLocationListener();
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		List<String> allProviders = locationManager.getAllProviders();
 		for (String provider : allProviders)
 		{
 			pre += provider + ", ";
 			// locationManager.requestLocationUpdates(provider, 0, 0,
 			// locationListener);
 		}
 		bestProvider = locationManager.getBestProvider(new Criteria(), false);
 		// locationManager.requestLocationUpdates(bestProvider, 0, 0,
 		// locationListener);
 		pre += "best-provider: " + bestProvider + "\n";
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
 		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
 
 		Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		if (lastKnownLocation != null)
 			geoPoint = new GeoPoint((int) (lastKnownLocation.getLatitude() * 1000000), (int) (lastKnownLocation.getLongitude() * 1000000));
 		else
 			geoPoint = new GeoPoint(INITIAL_LATITUDE, INITIAL_LONGITUDE);
 
 		mapController.setCenter(geoPoint);
 		mapController.setZoom(15);
 
 		setResult(geoPoint.toString()+", accel: " + motionDetector.getCurrentAccel());
 
 		List<Overlay> mapOverlays = mapView.getOverlays();
 		Drawable drawable = this.getResources().getDrawable(R.drawable.androidmarker);
 		currentLocationOverlay = new CurrentLocationsOverlay(drawable, this);
 
 		OverlayItem overlayitem = new OverlayItem(geoPoint, "Current Location", "Wassup?");
 		currentLocationOverlay.addOverlay(overlayitem);
 		mapOverlays.add(currentLocationOverlay);
 		
 		isStarted = false;
 		totalDistance = 0;
 		waitTime = 0;
 		
 		mBtnSart = (Button) findViewById(R.id.btnStart);
 		mBtnSart.setEnabled(true);
 		mBtnSart.setOnClickListener(this);
 		
 		mBtnStop = (Button) findViewById(R.id.btnStop);
 		mBtnStop.setEnabled(true);
 		mBtnStop.setOnClickListener(this);
 		
 		mBtnReset = (Button) findViewById(R.id.btnReset);
 		mBtnReset.setEnabled(true);
 		mBtnReset.setOnClickListener(this);
 	}
 
 	private void setResult(String text)
 	{
 		final String tmp = text;
 		runOnUiThread(new Runnable() {
 
 			@Override
 			public void run()
 			{
 				resultView.setText(pre + tmp);
 			}
 		});
 	}
 
 	@Override
 	protected boolean isRouteDisplayed()
 	{
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 	private void makeUseOfNewLocation(Location location)
 	{
 		if (location != null)
 		{
 			double lat = location.getLatitude();
 			double lng = location.getLongitude();
 
 			geoPoint = new GeoPoint((int) (lat * 1000000), (int) (lng * 1000000));
 
 			mapController.animateTo(geoPoint);
 			OverlayItem overlayitem = new OverlayItem(geoPoint, "Current Location", "Whola!");
 			currentLocationOverlay.addOverlay(overlayitem);
 			
 			if(currentLocation != null && isStarted)
 			{
 				float[] results = new float[1];
 				Location.distanceBetween(lastLatitude, lastLongitude, lat, lng, results);
 				//if(Math.abs(results[0]) <= 5) // no movement, add to wait time, accuracy of 5metres assumed
 				if(!motionDetector.isMoving() && Math.abs(results[0]) < 2)
 				{
 					waitTime += (location.getTime()-currentLocation.getTime());
 				}
 				else // has moved a little, add to distance
 				{
 					totalDistance += results[0];
 					lastLatitude = lat;
 					lastLongitude = lng;
 				}
 			}
 			else
 			{
 				startTime = location.getTime();
 				lastLatitude = lat;
 				lastLongitude = lng;
 			}
 			currentLocation = location;
 			setResult(geoPoint.toString() + ", wait-time:"+(waitTime/1000)+", distance:"+totalDistance+", fare: " + ((int)fareCalculator.getFare(totalDistance, waitTime)) +", accel: " + motionDetector.getCurrentAccel());
 		}
 		else
 		{
 			setResult("null location received");
 		}
 	}
 
 	private class CustomLocationListener implements LocationListener
 	{
 		@Override
 		public void onLocationChanged(Location location)
 		{
 			if(isBetterLocation(location, currentLocation))
 				makeUseOfNewLocation(location);
 		}
 
 		@Override
 		public void onProviderDisabled(String provider)
 		{
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void onProviderEnabled(String provider)
 		{
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras)
 		{
 			// TODO Auto-generated method stub
 
 		}
 
 		/** Determines whether one Location reading is better than the current Location fix
 		  * @param location  The new Location that you want to evaluate
 		  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
 		  */
 		protected boolean isBetterLocation(Location location, Location currentBestLocation)
 		{
 			if (currentBestLocation == null)
 			{
 				// A new location is always better than no location
 				return true;
 			}
 
 			// Check whether the new location fix is newer or older
 			long timeDelta = location.getTime() - currentBestLocation.getTime();
 			boolean isSignificantlyNewer = timeDelta > MAXIMUM_INTERVAL_BETWEEN_UPDATES;
 			boolean isSignificantlyOlder = timeDelta < -MAXIMUM_INTERVAL_BETWEEN_UPDATES;
 			boolean isNewer = timeDelta > 0;
 
 			// If it's been more than two minutes since the current location,
 			// use the new location
 			// because the user has likely moved
 			if (isSignificantlyNewer)
 			{
 				return true;
 				// If the new location is more than two minutes older, it must
 				// be worse
 			}
 			else if (isSignificantlyOlder)
 			{
 				return false;
 			}
 			
 			// significantly fast update, do not need to process, unlikely that user would have moved a lot.
 			if(timeDelta < MINIMUM_INTERVAL_BETWEEN_UPDATES)
 				return false;
 
 			// Check whether the new location fix is more or less accurate
 			int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
 			boolean isLessAccurate = accuracyDelta > 0;
 			boolean isMoreAccurate = accuracyDelta < 0;
 			boolean isSignificantlyLessAccurate = accuracyDelta > 200;
 
 			// Check if the old and new location are from the same provider
 			boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());
 
 			// Determine location quality using a combination of timeliness and
 			// accuracy
 			if (isMoreAccurate)
 			{
 				return true;
 			}
 			else if (isNewer && !isLessAccurate)
 			{
 				return true;
 			}
 			else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
 			{
 				return true;
 			}
 			return false;
 		}
 
 		/** Checks whether two providers are the same */
 		private boolean isSameProvider(String provider1, String provider2)
 		{
 			if (provider1 == null)
 			{
 				return provider2 == null;
 			}
 			return provider1.equals(provider2);
 		}
 	}
 
 	public void reset()
 	{
 		this.waitTime = 0;
 		this.totalDistance = 0;
 	}
 	
 	@Override
 	public void onClick(View v)
 	{
 		switch (v.getId()) {
 		case R.id.btnStart:
 			reset();
 			isStarted = true;
 			setResult(RESULT_OK);
 			break;
 		case R.id.btnStop:
 			isStarted = false;
 			setResult(RESULT_OK);
 			break;
 		case R.id.btnReset:
 			reset();
 			setResult(RESULT_OK);
 			break;
 		default:
 			break;
 		}			
 	}
 	
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 	}
 	
 	@Override 
 	public void onStop()
 	{
 		super.onStop();
 		
 	}
 	
 	@Override
 	public void onDestroy()
 	{
 		locationManager.removeUpdates(locationListener);
 		motionDetector.onDestroy();
 	}
 }
