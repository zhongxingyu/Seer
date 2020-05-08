 package com.ateam.tagger;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.telephony.TelephonyManager;
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
 	
 	// State variables
 	private String authToken;
 	private String userid;
 	private String deviceid;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         authToken= getIntent().getExtras().getString("AUTH_TOKEN");
         userid = getIntent().getExtras().getString("USERID");
         // This can sometimes throw a NullPointerException. Probably has something to do with 
         // the emulator not having any telephony services.
         try {
         	TelephonyManager phone = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
         	deviceid = phone.getDeviceId();
         } catch (NullPointerException e) {
         	deviceid = "FAKE_DEVICE_ID";
         }
         
         setContentView(R.layout.tagger);
         latitudeLabel = (TextView)findViewById(R.id.latitudeLabel);
         longitudeLabel = (TextView)findViewById(R.id.longitudeLabel);
         useridLabel = (TextView)findViewById(R.id.useridLabel);
         useridLabel.setText(String.format("userid=%s\nauth token=%s", userid, authToken));
         barcodeLabel = (TextView)findViewById(R.id.barcodeLabel);
         scanButton = (Button)findViewById(R.id.scanButton);
         scanButton.setOnClickListener(scanButtonOnClickListener);
         
         // A LocationListener is an observer class that gets information from the Android location framework.
         // The LocationManager is a facade for the location framework
         // http://developer.android.com/guide/topics/location/obtaining-user-location.html
         locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
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
         	
         	// TODO: Update status icon and possibly warn user if GPS is not working
         	public void onProviderDisabled(String provider) {
         		Log.i(TAG, String.format("Recieved a ProviderDisabled signal: Provider %s", provider));
         	}
         };
         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
     }
     
 	// Send location to central server if more than MAX_LOCATION_AGE seconds have passed
 	// since the last update. This should probably be done asynchronously (e.g. in a thread)
 	protected void sendAgentLocation() {
 		Log.i(TAG, "Sending location");
 		if (Server.sendAgentLocation(mLocation, userid, deviceid)) {
 			// Success
 			Log.i(TAG, "Location successfully sent to server");
 		} else {
 			// Failure ... 
 			// TODO notify user if this happens more than once or twice in x seconds
 			Log.e(TAG, "Unable to send location");
 		}
 	}
 	
 	protected void sendPatientLocation(Location pLocation, String qrcode) {
 		Log.i(TAG, "Sending patient location");
 		if (Server.sendPatientLoaction(pLocation, qrcode, userid, deviceid)) {
 			Log.i(TAG, String.format("QRCode %s successfully sent to server", qrcode));
 		} else {
 			Log.e(TAG, String.format("Failed to send QRCode %s to server", qrcode));
 		}
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
				barcodeLabel.setText("Failed to  barcode");
 			}
 		}
 	}
 
 }
