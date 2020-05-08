 package it.unibz.ait.view;
 
 import it.unibz.ait.model.Place;
 import it.unibz.ait.model.PlaceData;
 import it.unibz.ait.model.PlaceList;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Typeface;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 
 import com.google.api.client.googleapis.GoogleHeaders;
 import com.google.api.client.http.GenericUrl;
 import com.google.api.client.http.HttpRequest;
 import com.google.api.client.http.HttpRequestFactory;
 import com.google.api.client.http.HttpRequestInitializer;
 import com.google.api.client.http.HttpResponseException;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.apache.ApacheHttpTransport;
 import com.google.api.client.http.json.JsonHttpParser;
 import com.google.api.client.json.jackson.JacksonFactory;
 
 public class PoiView extends View implements LocationListener,
 		SensorEventListener {
 
 	private LocationManager locationManager;
 	private Location currentLocation = null;
 	private SensorManager mSensorManager;
 	private float[] mMData;
 	private float[] mGData;
 	private float[] mRMatrix = new float[MATRIX_SIZE];
 	private float[] mIMatrix = new float[MATRIX_SIZE];
 	private float[] mOutRMatrix = new float[MATRIX_SIZE];
 	private float[] values = new float[3];
 	private static final long MIN_TIME = 0;
 	private static final float MIN_DISTANCE = 5;
 	private static final int TWO_MINUTES = 1000 * 60 * 2;
 	private static final int MATRIX_SIZE = 16;
 	private List<PlaceData> placesData;
 	private float cameraHorizontalAngle;
 	private float cameraVerticalAngle;
 	private int widthPixels;
 	private int heightPixels;
 	private int mCount;
 
 
 	public PoiView(Context context, float cameraHorizontalAngle, float cameraVerticalAngle, int widthPixels, int heightPixels) {
 		super(context);
 		this.cameraHorizontalAngle = cameraHorizontalAngle;
 		this.cameraVerticalAngle = cameraVerticalAngle;
 		this.widthPixels = widthPixels;
 		this.heightPixels = heightPixels;
 		placesData = new ArrayList<PlaceData>();
 		/* get sensor manager */
 		mSensorManager = (SensorManager) context
 				.getSystemService(Context.SENSOR_SERVICE);
 
 		locationManager = (LocationManager) context
 				.getSystemService(Context.LOCATION_SERVICE);
 
 		/* Find best known last location */
 		List<String> providers = locationManager.getAllProviders();
 		for (String provider : providers) {
 			Location location = locationManager.getLastKnownLocation(provider);
 			if (location != null && isBetterLocation(location, currentLocation)){
 				currentLocation = location;
 				Log.i("Provider:", provider + "enabled: " + locationManager.isProviderEnabled(provider) + " Current Location: " + currentLocation + " Location: " + location);
 			}
 		}
 		if (currentLocation != null)
 			new DownloadPlacesTask().execute(currentLocation);
 	}
 
 	protected boolean isBetterLocation(Location location,
 			Location currentBestLocation) {
 		if (currentBestLocation == null)
 			return true;
 
 		// Check whether the new location fix is newer or older
 		long timeDelta = location.getTime() - currentBestLocation.getTime();
 		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
 		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
 		boolean isNewer = timeDelta > 0;
 
 		// If it's been more than two minutes since the current location, use
 		// the new location
 		// because the user has likely moved
 		if (isSignificantlyNewer) {
 			return true;
 			// If the new location is more than two minutes older, it must be
 			// worse
 		} else if (isSignificantlyOlder) {
 			return false;
 		}
 
 		// Check whether the new location fix is more or less accurate
 		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
 				.getAccuracy());
 		boolean isLessAccurate = accuracyDelta > 0;
 		boolean isMoreAccurate = accuracyDelta < 0;
 		boolean isSignificantlyLessAccurate = accuracyDelta > 200;
 
 		// Check if the old and new location are from the same provider
 		boolean isFromSameProvider = isSameProvider(location.getProvider(),
 				currentBestLocation.getProvider());
 
 		// Determine location quality using a combination of timeliness and
 		// accuracy
 		if (isMoreAccurate) {
 			return true;
 		} else if (isNewer && !isLessAccurate) {
 			return true;
 		} else if (isNewer && !isSignificantlyLessAccurate
 				&& isFromSameProvider) {
 			return true;
 		}
 		return false;
 	}
 
 	private boolean isSameProvider(String provider1, String provider2) {
 		if (provider1 == null) {
 			return provider2 == null;
 		}
 		return provider1.equals(provider2);
 	}
 
 	public void startLocationUpdates() {
 		locationManager.requestLocationUpdates(
 				locationManager.getBestProvider(getCriteria(), true), MIN_TIME,
 				MIN_DISTANCE, this);
 		Log.i("start_location_updates", locationManager.getBestProvider(getCriteria(), true));
 	}
 
 	protected Criteria getCriteria() {
 		Criteria criteria = new Criteria();
 		criteria.setPowerRequirement(Criteria.POWER_LOW);
 		criteria.setAccuracy(Criteria.ACCURACY_FINE);
 		criteria.setAltitudeRequired(false);
 		criteria.setBearingRequired(false);
 		criteria.setCostAllowed(true);
 		criteria.setSpeedRequired(false);
 		return criteria;
 	}
 
 	public void stopLocationUpdates() {
 		locationManager.removeUpdates(this);
 		Log.i("stop_location_updates", "stopped");
 	}
 
 	public void onLocationChanged(Location location) {
 		currentLocation = location;
 		new DownloadPlacesTask().execute(currentLocation);
 		Log.i("location_changed", "changed");
 	}
 
 	public void onProviderDisabled(String provider) {
 		locationManager.removeUpdates(this);
 		locationManager.requestLocationUpdates(
 				locationManager.getBestProvider(getCriteria(), true), MIN_TIME,
 				MIN_DISTANCE, this);
 		Log.i("on_provider_disabled", locationManager.getBestProvider(getCriteria(), true));
 	}
 
 	public void onProviderEnabled(String provider) {
 		locationManager.removeUpdates(this);
 		locationManager.requestLocationUpdates(
 				locationManager.getBestProvider(getCriteria(), true), MIN_TIME,
 				MIN_DISTANCE, this);
 		Log.i("on_provider_enabled", locationManager.getBestProvider(getCriteria(), true));
 	}
 
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		switch(status){
 		case LocationProvider.AVAILABLE:
 			Log.i("on_status_changed", provider + " available");
 			break;
 		case LocationProvider.OUT_OF_SERVICE:
 			Log.i("on_status_changed", provider + " out of service");
 			break;
 		case LocationProvider.TEMPORARILY_UNAVAILABLE:
 			Log.i("on_status_changed", provider + " temporarily unavailable");
 			break;
 			
 		}
 	}
 
 	private class DownloadPlacesTask extends AsyncTask<Location, Void, Integer> {
 
 		private static final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";
 		private final HttpTransport transport = new ApacheHttpTransport();
 		private static final String API_KEY = "AIzaSyDsuAJz24_oGbgqUzALidG2jD_-Wu-831E";
 		private static final String TAG = "PlacesSearchService";
		private static final float RADIUS = 1000;
 
 		@Override
 		protected Integer doInBackground(Location... location) {
 			if (location.length == 0 || location[0] == null) return null;
 			try {
 				Log.i(TAG, "Perform Search ....");
 				HttpRequestFactory httpRequestFactory = transport
 						.createRequestFactory(new HttpRequestInitializer() {
 							public void initialize(HttpRequest request) {
 								GoogleHeaders headers = new GoogleHeaders();
 								headers.setApplicationName("Google-Places-DemoApp");
 								request.headers = headers;
 								JsonHttpParser parser = new JsonHttpParser();
 								parser.jsonFactory = new JacksonFactory();
 								request.addParser(parser);
 							}
 						});
 				HttpRequest request = httpRequestFactory
 						.buildGetRequest(new GenericUrl(PLACES_SEARCH_URL));
 				request.url.put("key", API_KEY);
 				request.url.put("location", location[0].getLatitude() + ","
 						+ location[0].getLongitude());
 				request.url.put("radius", RADIUS);
 				request.url.put("sensor", "false");
 
 				PlaceList places = request.execute().parseAs(PlaceList.class);
 				Log.i(TAG, "Places list: " + places.results.size());
 				placesData.clear();
 				for (Place place : places.results) {
 					Log.i(TAG, "Places : " + place.toString());
 					if (!place.isExcludedType()) {
 						PlaceData pData = new PlaceData(place.name);
 						pData.setLongitude(place.geometry.location.lng);
 						pData.setLatitude(place.geometry.location.lat);
 						placesData.add(pData);
 						//Log.i(TAG, "Places : " + pData.getProvider());
 					}
 				}
 				return placesData.size();
 			} catch (HttpResponseException e) {
 				try {
 					Log.e(TAG, e.getResponse().parseAsString(), e);
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return 0;
 		}
 		
 		protected void onPostExecute(Integer result) {
 	         Log.i("Downloaded: ", result + " places");
 	    }
 
 	}
 
 	public void startSensorUpdates() {
 		mSensorManager.registerListener(this,
 				mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
 				SensorManager.SENSOR_DELAY_NORMAL);
 		mSensorManager.registerListener(this,
 				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 				SensorManager.SENSOR_DELAY_NORMAL);
 	}
 
 	public void stopSensorUpdates() {
 		mSensorManager.unregisterListener(this);
 	}
 
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 	}
 
 	public void onSensorChanged(SensorEvent event) {
 		synchronized (this) {
 			if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER
 					&& event.sensor.getType() != Sensor.TYPE_MAGNETIC_FIELD)
 				return;
 			switch (event.sensor.getType()) {
 			case Sensor.TYPE_MAGNETIC_FIELD:
 				mMData = event.values.clone();
 				break;
 			case Sensor.TYPE_ACCELEROMETER:
 				mGData = event.values.clone();
 				break;
 			}
 			//if (mMData != null && mGData != null) {
 			if (mCount++ > 10) {
 				/*
 				 * Compute rotation and inclination matrix
 				 * based on gravity and magnetic values
 				 */
 				SensorManager.getRotationMatrix(mRMatrix, mIMatrix, mGData, mMData);
 				/*
 				 * transform rotation matrix to new matrix - mOutRMatrix
 				 */
 				SensorManager.remapCoordinateSystem(mRMatrix, SensorManager.AXIS_X,
 						SensorManager.AXIS_Z, mOutRMatrix);
 				/*
 				 * Calculate orientation from rotation matrix
 				 * values[0] = azimuth values[1] = pitch values[2] = roll 
 				 */
 				SensorManager.getOrientation(mOutRMatrix, values);
 				/*
 				 * transform azimuth to 0-360 degrees
 				 */
 				float azimuth = Math.round(Math.toDegrees(values[0]));
 				if (azimuth < 0)
 					azimuth = 360 - Math.abs(azimuth);
 /*				Log.d("GRAVITY_ACCEL", "azimuth: " + azimuth +
 	                    "  pitch: " + Math.round(Math.toDegrees(values[1])) +
 	                    "  roll: " + Math.round(Math.toDegrees(values[2])) 
 	                    );
 */
 				recalculateVisiblePlaces(azimuth, 
 						Math.round(Math.toDegrees(values[1])), 
 						Math.round(Math.toDegrees(values[2])));
 				mCount = 0;
 			}
 		}
 	}
 
 	private void recalculateVisiblePlaces(float azimuth, float pitch, float roll) {
 		Log.i("Phone info:", "azimuth: " + azimuth + " pitch: " + pitch + " roll: " + roll +
 				" hor_angle: " + cameraHorizontalAngle + " ver_angle: " + cameraVerticalAngle +
 				" longitude: " + currentLocation.getLongitude() + " latitude: " + currentLocation.getLatitude());
 		
 		double halfCameraHAngle = cameraHorizontalAngle * 0.5;
 		double phoneRightSide = azimuth + halfCameraHAngle;
 		if (phoneRightSide > 360)
 			phoneRightSide = phoneRightSide - 360;
 
 		double phoneLeftSide = azimuth - halfCameraHAngle;
 		if (phoneLeftSide < 0)
 			phoneLeftSide = phoneLeftSide + 360;
 		Log.i("Phone info 2:", "halfAngle " + halfCameraHAngle + " phone_right_side: " + phoneRightSide + " phone_left_side: " + phoneLeftSide);
 		
 		for (PlaceData place : placesData) {
			double locAzimuth = Math.abs(currentLocation.bearingTo(place));
 			if (phoneRightSide >= phoneLeftSide) {
 				if ((locAzimuth > phoneLeftSide)
 						&& (locAzimuth < phoneRightSide)) {
 					place.setVisible(true);
 
 				} else {
 					place.setVisible(false);
 				}
 
 			}
			if (phoneRightSide <= phoneLeftSide) {
 				if ((locAzimuth < phoneRightSide)
 						|| (locAzimuth > phoneLeftSide)) {
 					place.setVisible(true);
 				} else {
 					place.setVisible(false);
 				}
 			}
 			
 			/*
 			 * Transform angle to pixels
 			 * n - location azimuth in digrees
 			 * (rightside - leftside) - interval
 			 * (n-leftside)*1/interval
 			 */
 			if (phoneRightSide >= phoneLeftSide) {
 				double interval = phoneRightSide - phoneLeftSide;
 				double weight = (locAzimuth - phoneLeftSide)/interval;
 				place.setX((float) (weight*widthPixels));
 			}
 			if (phoneRightSide <= phoneLeftSide){
 				double mPhoneRightSide = phoneRightSide + 360;
 				double mLocAzimuth;
 				if (locAzimuth >= 0)
 					mLocAzimuth = locAzimuth + 360;
 				else
 					mLocAzimuth = locAzimuth;
 				double interval = mPhoneRightSide - phoneLeftSide;
 				double weight = (mLocAzimuth - phoneLeftSide)/interval;
 				place.setX((float) (weight*widthPixels));
 			}
 
 			Log.i("Place data:", "place name: " + place.getProvider() + " place azimuth: " + locAzimuth + " visibility: " + place.isVisible());
 		}
 
 	}
 
 	@Override
	protected void onDraw(Canvas canvas) {		
 		for (PlaceData place : placesData) {
 			if (place.isVisible()) {
 				Paint paint = new Paint();
 				paint.setTextAlign(Paint.Align.CENTER);
 				paint.setStyle(Paint.Style.FILL_AND_STROKE);
 				paint.setColor(Color.WHITE);
 				paint.setShadowLayer(3, 0, 0, Color.BLACK);
 				paint.setTypeface(Typeface.DEFAULT_BOLD);
 				paint.setTextSize(12);
 				canvas.drawText(place.getProvider(), place.getX(),
						30, paint);
 			}
 		}
 		super.onDraw(canvas);
 		invalidate();
 	}
 }
