 package com.hkw.assassins;
 
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.regex.Pattern;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.ndeftools.Message;
 import org.ndeftools.Record;
 import org.ndeftools.wellknown.TextRecord;
 
 import com.google.android.gcm.GCMRegistrar;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 import com.hkw.assassins.asyctasks.GetUserFromServer;
 import com.hkw.assassins.asyctasks.KillUserOnServer;
 import com.hkw.assassins.asyctasks.RegisterUserOnServer;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.ActionBar;
 import android.app.DialogFragment;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.graphics.drawable.Drawable;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.nfc.NdefMessage;
 import android.nfc.NdefRecord;
 import android.nfc.NfcAdapter;
 import android.nfc.NfcAdapter.CreateNdefMessageCallback;
 import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
 import android.nfc.NfcEvent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Parcelable;
 import android.os.Vibrator;
 import android.util.Log;
 import android.util.Patterns;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 public class MainActivity extends MapActivity implements
 		CreateNdefMessageCallback, OnNdefPushCompleteCallback, LocationListener {
 
 	private MapView mapView;
 	private LocationManager locationManager;
 	private GeoPoint userLocation;
 	private String provider;
 
 	private String TAG = "MainActivity";
 	private static final int MESSAGE_SENT = 1;
 	NfcAdapter nfcAdapter;
 	PendingIntent nfcPendingIntent;
 	boolean nfcEnabled = false;
 
 	SharedPreferences settings;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// Set up the action bar.
 		final ActionBar actionBar = getActionBar();
 		actionBar.setDisplayShowTitleEnabled(true);
 
 		settings = getSharedPreferences("assassins_preferences", 0);
 		initMap();
 
 		// register user on our server
 		Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
 		Account[] accounts = AccountManager.get(this).getAccounts();
 		for (Account account : accounts) {
 			if (emailPattern.matcher(account.name).matches()) {
 				String possibleEmail = account.name;
 			}
 		}
 
 		// register for gcm
 		GCMRegistrar.checkDevice(this);
 		GCMRegistrar.checkManifest(this);
 		final String regId = GCMRegistrar.getRegistrationId(this);
 		if (regId.equals("")) {
 			Log.v(TAG, "Registering");
 			GCMRegistrar.register(this, "138142482844");
 			settings.edit().putString("user_gcmid", regId).commit();
 			// send registration id to server
 		} else {
 			Log.v(TAG, "Already registered: " + regId);
 			settings.edit().putString("user_gcmid", regId).commit();
 		}
 
 		// NfcManager manager = (NfcManager)
 		// getSystemService(Context.NFC_SERVICE);
 
 		// NFC
 		// Check for available NFC Adapter
 		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
 
 		if (nfcAdapter != null && nfcAdapter.isEnabled()) {
 			nfcEnabled = true;
 			nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(
 					this, this.getClass())
 					.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
 
 			// Register Android Beam callback
 			nfcAdapter.setNdefPushMessageCallback(this, this);
 			// Register callback to listen for message-sent success
 			nfcAdapter.setOnNdefPushCompleteCallback(this, this);
 
 		}
 
 		String name = settings.getString("user_name", "");
 		if (name.equals("")) {
 
 			EditNameDialog editNameFragment = new EditNameDialog(settings);
 			editNameFragment.show(getFragmentManager(), "");
 
 		}
 
 	}
 
 	/**
 	 * Initialise the map and adds the zoomcontrols to the LinearLayout.
 	 */
 	private void initMap() {
 		mapView = (MapView) findViewById(R.id.mapView);
 
 		mapView.setBuiltInZoomControls(true);
 		// View zoomView = mapView.getZoomControls();
 		// mapView.displayZoomControls(true);
 
 		// Get the location manager
 	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 	    // Define the criteria how to select the location provider -> use
 	    // default
 	    Criteria criteria = new Criteria();
 	    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,this,null);
 	    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 	    
 	    // Initialize the location fields
 	    if (location != null) {
 	      onLocationChanged(location);
 	    } else {
 	      userLocation = new GeoPoint(33776902,-84396530);
 	    }
 		
 		List<Overlay> mapOverlays = mapView.getOverlays();
 		//mapOverlays.clear();
 	    Drawable drawable = this.getResources().getDrawable(R.drawable.targetmarker);
 	    MapItemizedOverlay itemizedOverlay = new MapItemizedOverlay(drawable, this);
 	    
 	    GeoPoint point = userLocation;
 	    OverlayItem overlayItem = new OverlayItem(point, "Hola, Mundo!", "I'm in Mexico City!");
 
 		// Initialize the location fields
 		// if (location != null) {
 		// onLocationChanged(location);
 		// } else {
 		// userLocation = new GeoPoint(33776902, -84396530);
 		// }
 	}
 
 	private void updateTargetLocation() {
 
 	}
 
 	private void updateUserLocation() {
 		// Geo
 		// settings.edit().putString("user_latitude", userLatitude).commit();
 		// settings.edit().putString("user_longitude", userLongitude).commit();
 	}
 
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		int lat = (int) (location.getLatitude() * 1e6);
 		int lng = (int) (location.getLongitude() * 1e6);
 		userLocation = new GeoPoint(lat, lng);
 		
 		List<Overlay> mapOverlays = mapView.getOverlays();
 		mapOverlays.clear();
 	    Drawable drawable = this.getResources().getDrawable(R.drawable.targetmarker);
 	    MapItemizedOverlay itemizedOverlay = new MapItemizedOverlay(drawable, this);
 	    
 	    GeoPoint point = userLocation;
 	    OverlayItem overlayItem = new OverlayItem(point, "Hola, Mundo!", "I'm in Mexico City!");
 
 	    itemizedOverlay.addOverlay(overlayItem);
 	    mapOverlays.add(itemizedOverlay);
 	    
 	    MapController mc = mapView.getController();
 	    mc.animateTo(point);
 	    mc.setZoom(19);
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		Toast.makeText(this, "Enabled new provider " + provider,
 				Toast.LENGTH_SHORT).show();
 
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		Toast.makeText(this, "Disabled provider " + provider,
 				Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.menu_playersinfo:
 			DialogFragment playerslistFragment = new PlayersListDialog();
 			playerslistFragment.show(getFragmentManager(), "missiles");
 			return true;
 		case R.id.menu_targetinfo:
 			DialogFragment targetinfoFragment = new TargetInfoDialog(settings);
 			targetinfoFragment.show(getFragmentManager(), "missiles");
 			return true;
 		case R.id.menu_settings:
 			return true;
 		case R.id.menu_clearname:
 			settings.edit().putString("user_name", "").commit();
 			return true;
 		case R.id.menu_share:
 			Intent sharingIntent = new Intent(
 					android.content.Intent.ACTION_SEND);
 			sharingIntent.setType("text/plain");
 			String shareBody = "Here is the share content body";
 			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
 					"Subject Here");
 			sharingIntent
 					.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
 			startActivity(Intent.createChooser(sharingIntent, "Share via"));
 			return true;
 		case R.id.menu_startgame:
 			DialogFragment startgameFragment = new StartGameDialog(settings);
 			startgameFragment.show(getFragmentManager(), "");
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	/* NFC */
 
 	public void enableForegroundMode() {
 		Log.d(TAG, "enableForegroundMode");
 		if (nfcEnabled) {
 			IntentFilter tagDetected = new IntentFilter(
 					NfcAdapter.ACTION_TAG_DISCOVERED); // filter for all
 			IntentFilter[] writeTagFilters = new IntentFilter[] { tagDetected };
 			nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent,
 					writeTagFilters, null);
 		}
 	}
 
 	public void disableForegroundMode() {
 		Log.d(TAG, "disableForegroundMode");
 		if (nfcEnabled) {
 
 			nfcAdapter.disableForegroundDispatch(this);
 		}
 	}
 
 	@Override
 	public void onNewIntent(Intent intent) {
 		Log.d(TAG, "onNewIntent");
 
 		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
 			// TextView textView = (TextView) findViewById(R.id.title);
 
 			// task 2
 			// textView.setText("Hello NFC!");
 
 			Toast.makeText(getApplicationContext(), "Message RECIEVED!",
 					Toast.LENGTH_LONG).show();
 
 			Parcelable[] messages = intent
 					.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
 			if (messages != null) {
 
 				Log.d(TAG, "Found " + messages.length + " NDEF messages"); // is
 																			// almost
 																			// always
 																			// just
 																			// one
 
 				vibrate(); // signal found messages :-)
 
 				// parse to records
 				for (int i = 0; i < messages.length; i++) {
 					try {
 						List<Record> records = new Message(
 								(NdefMessage) messages[i]);
 
 						Log.d(TAG, "Found " + records.size()
 								+ " records in message " + i);
 
 						for (int k = 0; k < records.size(); k++) {
 							Log.d(TAG,
 									" Record #"
 											+ k
 											+ " is of class "
 											+ records.get(k).getClass()
 													.getSimpleName()
 											+ ". payload: "
 											+ ((TextRecord) records.get(k))
 													.getText());
 							killUser(((TextRecord) records.get(k)).getText());
 						}
 					} catch (Exception e) {
 						Log.e(TAG, "Problem parsing message", e);
 					}
 
 				}
 			}
 		} else {
 			// ignore
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		Log.d(TAG, "onResume");
 
 		super.onResume();
 
 		locationManager.requestLocationUpdates(provider, 400, 1, this);
 
 		enableForegroundMode();
 
 		registerUser();
 
 	}
 
 	public void killUser(String targetSecretCode) {
 		Log.d(TAG, "Killing the user!");
 		String mySecretCode = settings.getString("user_secretcode", "");
 		if (!mySecretCode.equals("")) {
 			KillUserOnServer kuos = new KillUserOnServer(settings, "default");
 			kuos.execute(mySecretCode, targetSecretCode);
 		}
 	}
 
 	public void registerUser() {
		Log.d(TAG, "Checking to see if the user is registered");
 		String name = settings.getString("user_name", "");
 		if (!name.equals("")) {
 			GetUserFromServer gufs = new GetUserFromServer(settings, "default");
 			try {
 				String JSONString = gufs.execute(name).get();
 				Log.d(TAG, "JSONString: " + JSONString);
 				if (JSONString.equals("null")) {
 					Log.d(TAG, "Register the user!!");
 
 					RegisterUserOnServer ruos = new RegisterUserOnServer(
 							settings, "default");
 
 					ruos.execute(name, settings.getString("user_gcmid", ""), "");
 				} else {
 					Log.d(TAG, "User  found!!");
 					JSONObject jsonObject = new JSONObject(JSONString);
 					settings.edit()
 							.putString("user_secretcode",
 									jsonObject.getString("SecretCode"))
 							.commit();
 
 					// TODO: stuff for the user
 				}
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ExecutionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	@Override
 	protected void onPause() {
 		Log.d(TAG, "onPause");
 
 		super.onPause();
 
 		locationManager.removeUpdates(this);
 
 		disableForegroundMode();
 	}
 
 	@Override
 	public NdefMessage createNdefMessage(NfcEvent event) {
 		Log.d(TAG, "createNdefMessage");
 
 		// create record to be pushed
 		TextRecord record = new TextRecord(settings.getString("user_name", ""));
 		NdefMessage nmsg = new NdefMessage(
 				new NdefRecord[] { record.getNdefRecord() });
 		// encode one or more record to NdefMessage
 		return nmsg;
 	}
 
 	public void onNdefPushComplete(NfcEvent arg0) {
 		Log.d(TAG, "onNdefPushComplete");
 
 		// A handler is needed to send messages to the activity when this
 		// callback occurs, because it happens from a binder thread
 		mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
 	}
 
 	/** This handler receives a message from onNdefPushComplete */
 	private final Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(android.os.Message msg) {
 			switch (msg.what) {
 			case MESSAGE_SENT:
 				Toast.makeText(getApplicationContext(), "Message beamed!",
 						Toast.LENGTH_LONG).show();
 				break;
 			}
 		}
 	};
 
 	private void vibrate() {
 		Log.d(TAG, "vibrate");
 
 		Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		vibe.vibrate(500);
 	}
 
 }
