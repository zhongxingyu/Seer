 package com.off.on;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.off.on.communication.WebSpeaker;
 import com.off.on.sensors.LocationService;
 import com.off.on.sensors.OrientationService;
 import com.off.on.utils.Constants;
 import com.off.on.utils.Utils;
 
 public class IndexActivity extends Activity {
 
 	private Button ONorOFF;
 	private TextView mainText;
 	private MyReceiver onStateReceiver;
 	private boolean isOrientationReceived = false, isLocationReceived = false;
 
 	private double latitude, longitude, altitude, azimuth, pitch, roll;
 	private float accuracy;
 	private boolean networkProvider, gpsProvider;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_index);
 
 		ONorOFF = (Button) findViewById(R.id.buttonRefresh);
 		mainText = (TextView) findViewById(R.id.mainText);
 
 		ONorOFF.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
				/*Toast.makeText(getApplicationContext(), "Refreshed",
						Toast.LENGTH_SHORT).show();*/
 			}
 		});
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 	}
 
 	@Override
 	protected void onDestroy() {
 		unregisterReceiver(onStateReceiver);
 		super.onStop();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		getCurrentState(true, true);
		onStateReceiver = new MyReceiver();
		IntentFilter intentLocationFilter = new IntentFilter();
		intentLocationFilter.addAction(Constants.LocationActionTag);
		intentLocationFilter.addAction(Constants.OrientationActionTag);
		registerReceiver(onStateReceiver, intentLocationFilter);
 		super.onResume();
 	}
 
 	private class MyReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context arg0, Intent receiverIntent) {
 
 			if (receiverIntent.getAction().equals(Constants.LocationActionTag)) {
 				isLocationReceived = true;
 
 				latitude = receiverIntent.getDoubleExtra(
 						Constants.LocationFlagLatitude, 0);
 				longitude = receiverIntent.getDoubleExtra(
 						Constants.LocationFlagLongitude, 0);
 				altitude = receiverIntent.getDoubleExtra(
 						Constants.LocationFlagAltitude, 0);
 				accuracy = receiverIntent.getFloatExtra(
 						Constants.LocationFlagAccuracy, 0);
 				gpsProvider = receiverIntent.getBooleanExtra(
 						Constants.LocationGPSProvider, false);
 				networkProvider = receiverIntent.getBooleanExtra(
 						Constants.LocationNetworkProvider, false);
 			}
 
 			if (receiverIntent.getAction().equals(
 					Constants.OrientationActionTag)) {
 				isOrientationReceived = true;
 
 				azimuth = receiverIntent.getDoubleExtra(
 						Constants.OrientationFlagAzimuth, 0);
 				pitch = receiverIntent.getDoubleExtra(
 						Constants.OrientationFlagPitch, 0);
 				roll = receiverIntent.getDoubleExtra(
 						Constants.OrientationFlagRoll, 0);
 			}
 
 			if (isOrientationReceived && isLocationReceived) {
 				new WebSpeaker(getApplicationContext(),
 						Utils.nameValuePairsRequest(latitude, longitude,
 								altitude, accuracy, gpsProvider,
 								networkProvider, azimuth, pitch, roll))
 						.execute(Constants.WebServerURL);
				unregisterReceiver(onStateReceiver);
 			}
 
 			mainText.setText("Latitude: " + String.valueOf(latitude)
 					+ "; Longitude: " + String.valueOf(longitude)
 					+ "; Altitude: " + String.valueOf(altitude)
 					+ "; Accuracy: " + String.valueOf(accuracy) + "; GPS: "
 					+ gpsProvider + "; Network: " + networkProvider
 					+ "; Azimuth: " + String.valueOf(azimuth) + "; Roll "
 					+ String.valueOf(roll) + "; Pitch: " + pitch);
 		}
 	}
 
 	private void getCurrentState(boolean locationService,
 			boolean orientationService) {
 		if (locationService) {
 			startService(new Intent(getApplicationContext(),
 					LocationService.class));
 		}
 		if (orientationService) {
 			startService(new Intent(getApplicationContext(),
 					OrientationService.class));
 		}
 	}
 
 	public void testMap(View view) {
 		Intent intent = new Intent(this, TabActivity.class);
 		startActivity(intent);
 	}
 }
