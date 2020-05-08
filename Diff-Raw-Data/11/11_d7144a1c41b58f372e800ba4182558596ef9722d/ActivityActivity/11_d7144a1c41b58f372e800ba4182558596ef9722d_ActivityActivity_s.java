 package com.pwr.zpi;
 
 import java.util.LinkedList;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.location.LocationRequest;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.PolylineOptions;
 import com.pwr.zpi.Dialogs.ErrorDialogFragment;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.content.IntentSender;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ActivityActivity extends FragmentActivity implements
 		OnClickListener, GooglePlayServicesClient.ConnectionCallbacks,
 		GooglePlayServicesClient.OnConnectionFailedListener,
 		com.google.android.gms.location.LocationListener {
 
 	private GoogleMap mMap;
 	private Button stopButton;
 	private static final String TAG = "ActivityActivity";
 	private LocationClient mLocationClient;
 	private TextView DataTextView1;
 	private TextView DataTextView2;
 	private TextView DataTextView3;
 	private TextView DataTextView4;
 
 	private LinkedList<Location> trace;
 	private PolylineOptions traceOnMap;
 	private LocationRequest mLocationRequest;
 	private static final long LOCATION_UPDATE_FREQUENCY = 1000;
 
 	// measured values
 	double pace;
 	double avgPace;
 	double distance;
 	long time;
 	long startTime;
 
 	private int dataTextView1Content;
 	private int dataTextView2Content;
 	private int dataTextView3Content;
 	private int dataTextView4Content;
 
 	// measured values IDs
 	private static final int distanceID = 1;
 	private static final int paceID = 2;
 	private static final int avgPaceID = 3;
 	private static final int timeID = 4;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view);
 
 		stopButton = (Button) findViewById(R.id.stopButton);
 		mMap = ((SupportMapFragment) getSupportFragmentManager()
 				.findFragmentById(R.id.map)).getMap();
 		trace = new LinkedList<Location>();
 		stopButton.setOnClickListener(this);
 		traceOnMap = new PolylineOptions();
 
 		DataTextView1 = (TextView) findViewById(R.id.dataTextView1);
 		DataTextView2 = (TextView) findViewById(R.id.dataTextView2);
 		DataTextView3 = (TextView) findViewById(R.id.dataTextView3);
 		DataTextView4 = (TextView) findViewById(R.id.dataTextView4);
 
 		dataTextView1Content = distanceID;
 		dataTextView2Content = timeID;
 		dataTextView3Content = paceID;
 		dataTextView4Content = avgPaceID;
 
 		mLocationClient = new LocationClient(this, this, this);
 		mLocationRequest = LocationRequest.create();
 		mLocationRequest.setInterval(LOCATION_UPDATE_FREQUENCY);
 		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
 
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		// Connect the client.
 		mLocationClient.connect();
 	}
 
 	@Override
 	protected void onStop() {
 
 		// If the client is connected
 		if (mLocationClient.isConnected()) {
 			/*
 			 * Remove location updates for a listener. The current Activity is
 			 * the listener, so the argument is "this".
 			 */
 			mLocationClient.removeLocationUpdates(this);
 		}
 		/*
 		 * After disconnect() is called, the client is considered "dead".
 		 */
 		mLocationClient.disconnect();
 		super.onStop();
 	}
 
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 		overridePendingTransition(R.anim.in_up_anim, R.anim.out_up_anim);
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v == stopButton) {
 			// TODO finish and save activity
 			finish();
 			overridePendingTransition(R.anim.in_up_anim, R.anim.out_up_anim);
 		}
 
 	}
 
 	@Override
 	public void onConnectionFailed(ConnectionResult connectionResult) {
 		Log.i(TAG, "onConnectionFailed");
 		/*
 		 * Google Play services can resolve some errors it detects. If the error
 		 * has a resolution, try sending an Intent to start a Google Play
 		 * services activity that can resolve error.
 		 */
 		if (connectionResult.hasResolution()) {
 			try {
 				// Start an Activity that tries to resolve the error
 				connectionResult
 						.startResolutionForResult(
 								this,
 								MainScreenActivity.CONNECTION_FAILURE_RESOLUTION_REQUEST);
 				/*
 				 * Thrown if Google Play services canceled the original
 				 * PendingIntent
 				 */
 			} catch (IntentSender.SendIntentException e) {
 				// Log the error
 				e.printStackTrace();
 			}
 		} else {
 			/*
 			 * If no resolution is available, display a dialog to the user with
 			 * the error.
 			 */
 			// Get the error dialog from Google Play services
 			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
 					connectionResult.getErrorCode(), this,
 					MainScreenActivity.CONNECTION_FAILURE_RESOLUTION_REQUEST);
 
 			// If Google Play services can provide an error dialog
 			if (errorDialog != null) {
 				// Create a new DialogFragment for the error dialog
 				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
 				// Set the dialog in the DialogFragment
 				errorFragment.setDialog(errorDialog);
 				// Show the error dialog in the DialogFragment
 				errorFragment.show(getSupportFragmentManager(),
 						"Location Updates");
 			}
 		}
 
 	}
 
 	@Override
 	public void onConnected(Bundle bundle) {
 		Log.i(TAG, "onConnected");
 		mLocationClient.requestLocationUpdates(mLocationRequest, this);
 		startTime = System.currentTimeMillis();
 		
 		Location location = mLocationClient.getLastLocation();
 		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),13));
 	}
 
 	@Override
 	public void onDisconnected() {
 		Log.i(TAG, "onDisconnected");
 
 	}
 
 	private void updateData(TextView textBox, int meassuredValue) {
 		if (meassuredValue == distanceID) {
			textBox.setText(String.format("%.2f", distance));
 		} else if (meassuredValue == paceID) {
 			// convert pace to show second
 			double rest = pace - (int) pace;
 			rest = rest * 60;
 
 			String secondsZero = (rest < 10) ? "0" : "";
 
 			textBox.setText(String.format("%.0f:%s%.0f", avgPace, secondsZero,
 					rest));
 			
 			textBox.setText(String.format("%.0f:%.0f", pace, rest));
 		} else if (meassuredValue == avgPaceID) {
 			// convert pace to show second
 			double rest = avgPace - (int) avgPace;
 			rest = rest * 60;
 
 			String secondsZero = (rest < 10) ? "0" : "";
 
 			textBox.setText(String.format("%.0f:%s%.0f", avgPace, secondsZero,
 					rest));
 		}
 		// TODO a thread to update time every second
 		else if (meassuredValue == timeID) {
 			long hours = time / 3600000;
 			long minutes = (time / 60000) - hours * 60;
 			long seconds = (time / 1000) - hours * 3600 - minutes * 60;
 			String hourZero = (hours < 10) ? "0" : "";
 			String minutesZero = (minutes < 10) ? "0" : "";
 			String secondsZero = (seconds < 10) ? "0" : "";
 
 			textBox.setText(String.format("%s%d:%s%d:%s%d", hourZero, hours,
 					minutesZero, minutes, secondsZero, seconds));
 		}
 	}
 
 	private void countData(Location location, Location lastLocation) {
 
 		float speed = location.getSpeed();
 		Toast.makeText(this, speed + "", Toast.LENGTH_SHORT).show();
 
 		pace = (double) 1 / (speed * 60 / 1000);
		DataTextView3.setText(pace + " min/km");
 
 		distance += lastLocation.distanceTo(location);
		DataTextView1.setText(distance / 1000 + " km");
 
 		time = System.currentTimeMillis() - startTime;
 
		avgPace = ((double) time / 60000) / distance;
 
 		updateData(DataTextView1, dataTextView1Content);
 		updateData(DataTextView2, dataTextView2Content);
 		updateData(DataTextView3, dataTextView3Content);
 		updateData(DataTextView4, dataTextView4Content);
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 
 		//TODO upade ONLY when accuracy is good
 		
 		Location lastLocation;
 		LatLng latLng = new LatLng(location.getLatitude(),
 				location.getLongitude());
 		if (!trace.isEmpty()) {
 			lastLocation = trace.getLast();
 			mMap.addPolyline(new PolylineOptions().add(
 					new LatLng(lastLocation.getLatitude(), lastLocation
 							.getLongitude())).add(latLng));
 			countData(location, lastLocation);
 		}
 
 		trace.add(location);
 
 		traceOnMap.add(latLng);
 		
 		mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
 
 	}
 
 }
