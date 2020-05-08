 package com.ateam.tagger;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class Tagger extends Activity {
 	// Widgets
 	Location mLocation;
 	TextView latitudeLabel;
 	TextView longitudeLabel;
 	TextView useridLabel;
 	TextView barcodeLabel;
 	Button scanButton;
 
 	// Utilities
 	LocationManager locationManager;
 
 	// Constants
 	static final String TAG = "Tagger";
 	static final int BARCODE_ACTIVITY_RESULT = 0;
 	static final String AGENT_LOCATION_URL = "https://srtagger.appspot.com/agentlocation";
 	static final String PATIENT_LOCATION_URL = "https://srtagger.appspot.com/patientlocation";
 
 	// State variables
 	private Bundle mBundle;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		mBundle = (savedInstanceState != null) ? savedInstanceState : new Bundle();
 		mBundle.putAll(getIntent().getExtras());
 
 		setContentView(R.layout.tagger);
 		latitudeLabel = (TextView) findViewById(R.id.latitudeLabel);
 		longitudeLabel = (TextView) findViewById(R.id.longitudeLabel);
 		useridLabel = (TextView) findViewById(R.id.useridLabel);
 		useridLabel.setText(String.format("userid=%s\nauth token=%s", mBundle.getString("userid"),
 				mBundle.getString("auth-token")));
 		barcodeLabel = (TextView) findViewById(R.id.barcodeLabel);
 		scanButton = (Button) findViewById(R.id.scanButton);
 		scanButton.setOnClickListener(scanButtonOnClickListener);
 
 		// A LocationListener is an observer class that gets information from
 		// the Android location framework.
 		// The LocationManager is a facade for the location framework
 		// http://developer.android.com/guide/topics/location/obtaining-user-location.html
 		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 		LocationListener locationListener = new LocationListener() {
 			public void onLocationChanged(Location location) {
 				Log.i(TAG, "Recieved a LocationChanged signal");
 				mLocation = location;
 				latitudeLabel.setText(String.format("Latitude: %g", mLocation.getLatitude()));
 				longitudeLabel.setText(String.format("Longitude: %g", mLocation.getLongitude()));
 				sendAgentLocation();
 			}
 
 			// TODO: Update location precision information
 			public void onStatusChanged(String provider, int status, Bundle extras) {
 				Log.i(TAG, String.format("Recieved a StatusChanged signal: Provider %s, status %d", provider, status));
 			}
 
 			// TODO: Update status icon?
 			public void onProviderEnabled(String provider) {
 				Log.i(TAG, String.format("Recieved a ProviderEnabled signal: Provider %s", provider));
 			}
 
 			// TODO: Update status icon and possibly warn user if GPS is not
 			// working
 			public void onProviderDisabled(String provider) {
 				Log.i(TAG, String.format("Recieved a ProviderDisabled signal: Provider %s", provider));
 			}
 		};
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
 	}
 
 	protected void sendAgentLocation() {
 		Log.i(TAG, "Sending location");
 		Bundle message = new Bundle();
 		Bundle postData = new Bundle();
 
 		postData.putString("agentid", mBundle.getString("userid"));
 		postData.putString("deviceid", Utilities.getDeviceId(this));
 		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ", Locale.getDefault());
 		postData.putString("time", format.format(new Date()));
 		postData.putString("longitude", String.format("%f", mLocation.getLongitude()));
 		postData.putString("latitude", String.format("%f", mLocation.getLatitude()));
 
 		message.putString("url", AGENT_LOCATION_URL);
 		message.putBundle("postdata", postData);
 		
 		Intent sendMessageIntent = new Intent(this, CommunicationService.class);
 		sendMessageIntent.putExtra("message", message);
 		startService(sendMessageIntent);
 
 		// new SendHttpPostTask().execute(message);
 	}
 
 	protected void sendPatientLocation(Location pLocation, String qrcode) {
 		Log.i(TAG, "Sending patient location");
 		Bundle message = new Bundle();
 		Bundle postData = new Bundle();
 
 		postData.putString("agentid", mBundle.getString("userid"));
 		postData.putString("deviceid", Utilities.getDeviceId(this));
 		postData.putString("qrcode", qrcode);
 		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ", Locale.getDefault());
 		postData.putString("time", format.format(new Date()));
 		postData.putString("longitude", String.format("%f", pLocation.getLongitude()));
 		postData.putString("latitude", String.format("%f", pLocation.getLatitude()));
 
 		message.putString("url", PATIENT_LOCATION_URL);
 		message.putBundle("postdata", postData);
 		
 		Intent sendMessageIntent = new Intent(this, CommunicationService.class);
 		sendMessageIntent.putExtra("message", message);
 		startService(sendMessageIntent);
 
 		// new SendHttpPostTask().execute(message);
 	}
 
 	// See http://code.google.com/p/zxing/wiki/ScanningViaIntent
 	private OnClickListener scanButtonOnClickListener = new OnClickListener() {
 		public void onClick(View v) {
			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			intent.setPackage("com.google.zxing.client.android");
			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			startActivityForResult(intent, BARCODE_ACTIVITY_RESULT);
 		}
 	};
 
 	// See http://code.google.com/p/zxing/wiki/ScanningViaIntent
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		if (requestCode == BARCODE_ACTIVITY_RESULT) {
 			if (resultCode == RESULT_OK) {
 				String contents = intent.getStringExtra("SCAN_RESULT");
 				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
 
 				// Handle successful scan
 				barcodeLabel.setText(String.format("Format: %s, Contents: %s", format, contents));
 				sendPatientLocation(mLocation, contents);
 			} else if (resultCode == RESULT_CANCELED) {
 				Log.i(TAG, "Failed to acquire barcode");
 				barcodeLabel.setText("Failed to acquire barcode");
 			}
 		}
 	}
 }
