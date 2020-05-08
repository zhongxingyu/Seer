 package com.veenvliet.seeyousoon;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
 import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.location.LocationListener;
 import com.google.android.gms.location.LocationRequest;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Locale;
 
 public class MainActivity extends FragmentActivity {
 
 	private final int RQS_GooglePlayServices = 1;
 	private final static int RQS_PICK_CONTACT = 1;
 	private static LocationClient mLocationClient;
 	private static LocationRequest mLocationRequest;
 	private static Boolean mWarmUpGpsHardware = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_fullscreen);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		Log.i("MainActivity.onResume", "Resuming the GPS hardware...");
 
 		// check to see if Google Play Services are available on this device
 		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getApplicationContext());
 		if (ConnectionResult.SUCCESS == resultCode) {
 			mWarmUpGpsHardware = true;
 			this.InitiateLocationClient();
 			Log.i("MainActivity.onResume",
 					"Initiate get my current location request to warm up the GPS hardware...");
 		} else {
 			try {
 				Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
 						resultCode, this, RQS_GooglePlayServices);
 				if (dialog != null) {
 					dialog.show();
 				} else {
 					showOkDialogWithText(
 							this,
 							"Something went wrong. Please make sure that you have the Play Store installed and that you are connected to the internet. Contact developer with details if this persists.");
 				}
 			} catch (Exception ex) {
 				showOkDialogWithText(
 						this,
 						"Something went wrong. Please make sure that you have the Play Store installed and that you are connected to the internet. Contact developer with details if this persists.");
 			}
 		}
 
 		// add the phone number you are sending the message to (in the
 		// background)
 		TextView background = (TextView) findViewById(R.id.fullscreen_content);
 		String backgroundMessage = getText(R.string.backgroundtext)
 				+ System.getProperty("line.separator")
 				+ PreferenceWrapper
 						.Load("phoneNumberPreference",
 								getString(R.string.emptyPleaseProgramPhoneNumber),
 								this);
 		background.setText(backgroundMessage);
 	};
 
 	public static void showOkDialogWithText(Context context, String messageText) {
 		Builder builder = new AlertDialog.Builder(context);
 		builder.setMessage(messageText);
 		builder.setCancelable(true);
 		builder.setPositiveButton("OK", null);
 		AlertDialog dialog = builder.create();
 		dialog.show();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		// unregister so we don't get any more location updates
 		Log.i("MainActivity.onPause", "Shut down the GPS hardware...");
 		this.ShutdownLocationClient();
 	};
 
 	@Override
 	protected void onDestroy() {
 		// unregister so we don't get any more location updates
 		Log.i("MainActivity.onDestroy", "Shut down the GPS hardware...");
 		this.ShutdownLocationClient();
 		super.onDestroy();
 	};
 
 	// click handler for all buttons on this screen
 	public void onClick(View view) {
 		switch (view.getId()) {
 		// large button that fills the entire screen and is used to send SMS
 		case R.id.sendtext_button:
 
 			// get the sounds enabled/disabled setting
 			Boolean playSound = PreferenceWrapper.LoadBoolean(
 					"soundsPreference", true, getApplicationContext());
 			MediaPlayer sound1 = null;
 			// load sound into memory if the audio feature is enabled
 			if (playSound) {
 				sound1 = MediaPlayer.create(this, R.raw.electronicchime); // res/raw/electronicchime.wav
 			}
 			// find current location (new)
 			mWarmUpGpsHardware = false;
 			if (ConnectionResult.SUCCESS == GooglePlayServicesUtil
 					.isGooglePlayServicesAvailable(this)) {
 				this.InitiateLocationClient();
 				Toast.makeText(
 						this,
 						String.format(getString(R.string.findingYourLocationPleaseWait)),
 						Toast.LENGTH_LONG).show();
 				if (null != sound1)
 					sound1.start();
 			}
 			return;
 
 			// About - Legal Notices
 		case R.id.about_button:
 			String LicenseInfo = GooglePlayServicesUtil
 					.getOpenSourceSoftwareLicenseInfo(getApplicationContext());
 			AlertDialog.Builder LicenseDialog = new AlertDialog.Builder(
 					MainActivity.this);
 			LicenseDialog.setTitle(getString(R.string.legalNotices));
 			LicenseDialog.setMessage(LicenseInfo);
 			LicenseDialog.show();
 			return;
 
 			// Contacts - Select phone number from list of contacts
 		case R.id.contacts_button:
 			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
 			intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
 			startActivityForResult(intent, 1);
 			return;
 
 			// Settings page
 		case R.id.settings_button:
 			// open the settings intent page
 			Intent settingsIntent = new Intent(this, SettingsActivity.class);
 			startActivity(settingsIntent);
 			return;
 		}
 		return;
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (requestCode == RQS_PICK_CONTACT) {
 			if (resultCode == RESULT_OK) {
 				Uri contactData = data.getData();
 				@SuppressWarnings("deprecation")
 				Cursor cursor = managedQuery(contactData, null, null, null,
 						null);
 				cursor.moveToFirst();
 
 				String number = cursor
 						.getString(cursor
 								.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
 
 				// contactName.setText(name);
 				PreferenceWrapper.Save("phoneNumberPreference", number, this);
 				// contactEmail.setText(email);
 			}
 		}
 	}
 
 	private void InitiateLocationClient() {
 		if (mLocationClient == null) {
 			mLocationClient = new LocationClient(this, mConnectionCallbacks,
 					mConnectionFailedListener);
 		}
 		mLocationClient.connect();
 	}
 
 	private void ShutdownLocationClient() {
 		if (null != mLocationClient) {
 			if (mLocationClient.isConnected()) {
 				mLocationClient.removeLocationUpdates(mLocationListener);
 				mLocationClient.disconnect();
 			}
 		}
 	}
 
 	private ConnectionCallbacks mConnectionCallbacks = new ConnectionCallbacks() {
 
 		@Override
 		public void onDisconnected() {
 		}
 
 		@Override
 		public void onConnected(Bundle arg0) {
 			mLocationRequest = LocationRequest.create();
 			mLocationRequest
 					.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
 			mLocationRequest.setInterval(5000);
 			mLocationRequest.setFastestInterval(15000);
 			mLocationClient.requestLocationUpdates(mLocationRequest,
 					mLocationListener);
 		}
 	};
 
 	private LocationListener mLocationListener = new LocationListener() {
 		@Override
 		public void onLocationChanged(Location location) {
 
 			// log the location that we received
 			Log.i("MainActivity.onLocationChanged", String.format(
 					"http://maps.google.com/maps?q=%s,%s and accuracy = %s",
 					location.getLatitude(), location.getLongitude(),
 					location.getAccuracy()));
 
 			// this is a 'warmup the GPS hardware request' so get out and don't
 			// send SMS
 			if (mWarmUpGpsHardware) {
 				Log.i("MainActivity.onLocationChanged",
 						"GPS hardware is warmed up...  exiting method");
 				// disable GPS warmup flag
 				mWarmUpGpsHardware = false;
 				// unregister so we don't get any more location updates
 				if (null != mLocationClient) {
 					if (mLocationClient.isConnected()) {
 						mLocationClient
 								.removeLocationUpdates(mLocationListener);
 						mLocationClient.disconnect();
 					}
 				}
 				return;
 			}
 
 			// get the sounds enabled/disabled setting
 			Boolean playSound = PreferenceWrapper.LoadBoolean(
 					"soundsPreference", true, getApplicationContext());
 
 			// get the phone number to send the text message to
 			String phoneNumber = PreferenceWrapper.Load(
 					"phoneNumberPreference", "", getApplicationContext());
 
 			// get the predetermined text message
 			String message = PreferenceWrapper.Load("textMessagePreference",
 					getString(R.string.defaultTextMessageToSend),
 					getApplicationContext());
 
 			// send link to current location url on Google Maps
 			if (PreferenceWrapper.LoadBoolean("includeMyLocationGps", true,
 					getApplicationContext())) {
 				message += System.getProperty("line.separator");
 				message += getString(R.string.myCurrentLocationIs);
 				message += System.getProperty("line.separator");
 				message += String.format("http://maps.google.com/maps?q=%s,%s",
 						location.getLatitude(), location.getLongitude());
 			}
 
 			// try to find street address from Google API Geocoder
 			try {
 				if (PreferenceWrapper.LoadBoolean("includeMyLocationAddress",
 						true, getApplicationContext())) {
 					Geocoder geocoder = new Geocoder(getApplicationContext(),
 							Locale.getDefault());
 					List<Address> addresses = geocoder.getFromLocation(
 							location.getLatitude(), location.getLongitude(), 1);
 					if (addresses != null && addresses.size() > 0) {
 						Address address = addresses.get(0);
 						message += System.getProperty("line.separator");
 						message += address.getAddressLine(0) + ", "
 								+ address.getLocality();
 					}
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			// add accuracy
 			// message += System.getProperty("line.separator");
 			// message += location.getAccuracy();
 
 			// send message
 			Sms.Send(phoneNumber, message, playSound, getApplicationContext());
 
 			// unregister so we don't get any more location updates
 			if (null != mLocationClient) {
 				if (mLocationClient.isConnected()) {
 					mLocationClient.removeLocationUpdates(mLocationListener);
 					mLocationClient.disconnect();
 				}
 
 			}
 		}
 	};
 
 	private OnConnectionFailedListener mConnectionFailedListener = new OnConnectionFailedListener() {
 		@Override
 		public void onConnectionFailed(ConnectionResult arg0) {
 			Log.e("FullscreenActivity", "ConnectionFailed");
 		}
 	};
 
 }
