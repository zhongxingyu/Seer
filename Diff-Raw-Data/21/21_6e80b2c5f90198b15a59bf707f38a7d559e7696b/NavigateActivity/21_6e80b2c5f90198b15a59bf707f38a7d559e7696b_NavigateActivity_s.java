 package com.example.climbxpert;
 
 import com.example.climbxpert.POI.POI;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
 import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.location.LocationListener;
 import com.google.android.gms.location.LocationRequest;
 import com.google.android.gms.maps.model.LatLng;
 
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.location.Location;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class NavigateActivity extends Activity 
 				implements
 				SensorEventListener, // listen to sensor events
 				ConnectionCallbacks, //allow connection to location service
 				OnConnectionFailedListener, //notify when connection to location service failed
 				LocationListener //listen to location changes
 {
 
 	private SensorManager sensMngr;
 	private Sensor magno;
 	private TextView xV;
 	private TextView yV;
 	private TextView zV;
 	private TextView tV;
 	private POI currentPOI;
 	
 	// Client for connecting to location service
 	private LocationClient locClient;
 	
 	// Options for location requests
 	private static final LocationRequest REQUEST = LocationRequest.create()
 	            .setInterval(1000)         // 5 seconds
 	            .setFastestInterval(16)    // 16ms = 60fps
 	            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); 
 	 			//TODO consider lowering the accuracy - this may affect performance
 	
 	// The last received location from the location service
 	private Location lastKnownLocation;
 	
 	private LatLng targetLocation;
 	
 	
 	// The minimum distance in meters to enable camera view button
 	private final double MINIMUM_DISTANCE_TO_TARGET = 5.0;
 	
 		
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_navigate);
 		
 		xV = (TextView)findViewById(R.id.xValue);
 		yV = (TextView)findViewById(R.id.yValue);
 		zV = (TextView)findViewById(R.id.zValue);
 		tV = (TextView)findViewById(R.id.targetOffset);
 		
 		if(!ClimbXpertData.debug) {
 			xV.setVisibility(View.GONE);
 			yV.setVisibility(View.GONE);
 			zV.setVisibility(View.GONE);
 			tV.setVisibility(View.GONE);
 		}
 		sensMngr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 		magno = sensMngr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
 		
 		//TODO maybe -1 is not invalid id
 //		defaultTargetLocation = new LatLng(31.762641,35.201756);
 		int recievedPID = this.getIntent().getIntExtra("pid", -1);
 		currentPOI = ClimbXpertData.getPOI(recievedPID);
 		if (recievedPID < 0 || currentPOI == null) {
 			LoggerTools.LogToast(this, "Could not recieve POI from calling activity.");
 		}
 		
 		targetLocation = currentPOI.standLocation;
 	}
 
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 		sensMngr.registerListener(this, magno, SensorManager.SENSOR_DELAY_NORMAL);
 		setupLocationClient();
 		locClient.connect();
 	}
 	
 	@Override
 	protected void onPause(){
 		super.onPause();
 		sensMngr.unregisterListener(this);
 		if (null != locClient)
 		{
 			locClient.disconnect();
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.navigate, menu);
 		return true;
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// Do nothing
 	}
 
 	/**
 	 * Updates the direction of the compass' arrow in relation to given magnetic
 	 * data and last known location. 
 	 */
 	@Override
 	public void onSensorChanged(SensorEvent se) {
 
 		ImageView img = (ImageView)findViewById(R.id.arrowImage);
 		
 		
 		float compassOffset = 0;
 		float locationOffset =  getAngleToLocation();
 		
 		xV.setText("X:" + se.values[0]);
 		yV.setText("Y:" + se.values[1]);
 		tV.setText("target:" + locationOffset);
 		
 		float x = se.values[0];
 		float y = se.values[1];
 		
 		
 		double Dangle = MathOrientation.normalizeAngle((float)MathOrientation.getAngle(x,y)-90);
 		
 		
 		float rotaionangle = compassOffset - locationOffset - (float)Dangle;
 		zV.setText("rotaion Angle:" + rotaionangle);
 		
 		
 		img.setRotation(rotaionangle);
 		
 	}
 
 	
 	/***
 	 * initialize the locClient object if not already initialized
 	 */
 	public void setupLocationClient()
 	{
 		if (null == locClient)
 		{
 			locClient = new LocationClient(getApplicationContext(), this, this);
 		}
 	}
 	
 	
 	/**
 	 * Updates the last known location and update distance calculations.
 	 * Triggers Camera View button if close enough.
 	 */
 	@Override
 	public void onLocationChanged(Location location) {
 		lastKnownLocation = location;
 		
 		double distance = getDistanceToTarget();
 		
 		// show the user the remaining distance
		TextView tDistance = (TextView)findViewById(R.id.targetDistance);
 		tDistance.setText("Distance:" + distance);
 		
 		//TODO consider if Geo information is relevant for users
		TextView tGeo = (TextView) findViewById(R.id.geoData);
 		tGeo.setText("Geo:" + location.getLatitude() + ", " + location.getLongitude());
 		
 		if (distance < MINIMUM_DISTANCE_TO_TARGET)
 		{
 			enableCameraViewButton();
 		}
 		else
 		{
 			disableCamraViewButton();
 		}
 		
 	}
 
 
 	@Override
 	public void onConnectionFailed(ConnectionResult result) {
 		LoggerTools.LogToast(this, "Could not get current location information. Check GPS availability");
 	}
 
 	@Override
 	public void onConnected(Bundle connectionHint) {
 		locClient.requestLocationUpdates(REQUEST, this);		
 	}
 
 	@Override
 	public void onDisconnected() {
 		// Do nothing		
 	}
 	
 	
 	/**
 	 * Calculates the angle from the last known location to the target location 
 	 * @return The angle degrees, normalized.
 	 */
 	private float getAngleToLocation()
 	{
 		
 		if (null == lastKnownLocation)
 		{
 			return 0; // no current location yet
 			
 		}
 		
 		// latitude is y axis, longitude is x axis
 		return (float) MathOrientation.normalizeAngle((float)
 				MathOrientation.getAngle(targetLocation.longitude - lastKnownLocation.getLongitude(),
 										targetLocation.latitude - lastKnownLocation.getLatitude())-90);
 
 	}
 	
 	
 	/**
 	 * Calculates the distance from the last known location to the target location.
 	 * This is done with approximation with the assumption that the distance between 
 	 * the two locations is negligible comparing to earth radius.  
 	 * @return The distance in meters
 	 */
 	private double getDistanceToTarget()
 	{
 		if (null == lastKnownLocation)
 		{
 			return -1;
 		}
 		
 		return MathOrientation.calculateLocationsDistance(
 					new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), 
 					targetLocation);
 		
 	}
 	
 
 	/**
 	 *  Hide the Camera View button.
 	 */
 	private void disableCamraViewButton() {
 		Button cvb = (Button) findViewById(R.id.CameraViewButton);
 		
 		cvb.setVisibility(View.INVISIBLE);
 	}
 	
 	/**
 	 * Display the Camera View button and make it clickable
 	 */
 	private void enableCameraViewButton() {
 		Button cvb = (Button) findViewById(R.id.CameraViewButton);
 		
 		cvb.setVisibility(View.VISIBLE);
 	}
 	
 	
 	public void openCameraView(View view)
     {
     	Intent intent = new Intent(this, CameraViewActivity.class);
     	intent.putExtra("pid",currentPOI.pid);
     	startActivity(intent);
     }
 }
