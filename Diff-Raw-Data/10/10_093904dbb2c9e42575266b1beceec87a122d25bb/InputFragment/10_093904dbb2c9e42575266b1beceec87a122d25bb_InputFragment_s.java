 /*
  * 
  */
 package cz.android.monet.restfulclient.fragments;
 
 import java.util.List;
 
 import cz.android.monet.restfulclient.HistoryListActivity;
 import cz.android.monet.restfulclient.R;
 import cz.android.monet.restfulclient.SendUserIdAsyncTask;
 import cz.android.monet.restfulclient.interfaces.OnServerResultReturned;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.database.Cursor;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.provider.Settings;
 import android.provider.UserDictionary;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class InputFragment.
  */
 public class InputFragment extends Fragment {
 
 	/** The Constant TAG. */
 	private static final String TAG = "InputFragment";
 
 	/** The Constant PREF_HOST. */
 	private static final String PREF_HOST = "hostIP";
 
 	/** The Constant PREF_SEND_VALUE. */
 	private static final String PREF_SEND_VALUE = "lastBarCode";
 
 	/** The Constant PREF_PORT. */
 	private static final String PREF_PORT = "hostPort";
 
 	/** The host. */
 	private EditText host;
 
 	/** The send data. */
 	private EditText sendData;
 	// private ListView hostList;
 	/** The m result callback. */
 	OnServerResultReturned mResultCallback;
 
 	// The resul code
 	/** The Constant RESULT_OK. */
 	static final int RESULT_OK = -1;
 
 	// The request code
 	/** The Constant PICK_CONTACT_REQUEST. */
 	static final int PICK_CONTACT_REQUEST = 1;
 
 	/** The Constant HISTORY_REQUEST. */
 	static final int HISTORY_REQUEST = 2;
 	static final int UPDATE_LATLNG = 3;
 	static final int ENABLE_LOCATION = 4;
 
 	private static final int TWO_MINUTES = 1000 * 60 * 2;
 
 	private LocationManager locationManager;
 
 	private LocationListener listener;
 
 	private Handler mHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case UPDATE_LATLNG:
 				Log.v(TAG, "Location: " + msg.obj.toString());
 				break;
 			case ENABLE_LOCATION:
 				Intent settingsIntent = new Intent(
 						Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 				startActivity(settingsIntent);
 				break;
 			}
 		}
 	};
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
 	 */
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	/**
 	 * Save shared preferences.
 	 */
 	private void saveSharedPreferences() {
 		SharedPreferences settings = PreferenceManager
 				.getDefaultSharedPreferences(getActivity()
 						.getApplicationContext());
 		SharedPreferences.Editor editor = settings.edit();
 
 		editor.putString(PREF_HOST, host.getText().toString());
 		editor.putString(PREF_SEND_VALUE, sendData.getText().toString());
 		editor.putInt(PREF_PORT, 2323);
 
 		editor.commit();
 		editor.apply();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
 	 */
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		host = ((EditText) getView().findViewById(R.id.editHostAddress));
 		sendData = (EditText) getView().findViewById(R.id.editSendData);
 
 		// hostList.setAdapter(new HistoryHostsProvider()
 		// .getUserDictionaryWords(getActivity().getApplicationContext()));
 
 		// Get the intent that started this activity
 		Intent intent = getActivity().getIntent();
 		Uri data = intent.getData();
 
 		if (data != null && intent.getType().equals("text/plain")) {
 			host.setText(data.getHost().toString());
 		} else {
 			// Restore preferences
 			host.setText(PreferenceManager.getDefaultSharedPreferences(
 					getActivity().getApplicationContext()).getString(PREF_HOST,
 					"193.33.22.109"));
 		}
 
 		sendData.setText(PreferenceManager.getDefaultSharedPreferences(
 				getActivity().getApplicationContext()).getString(
 				PREF_SEND_VALUE, "123456789"));
 
 		Button butSend = (Button) getView().findViewById(R.id.btnSend);
 		butSend.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 
 				String word = host.getText().toString().trim();
 				saveSharedPreferences();
 
 				UserDictionary.Words.addWord(getView().getContext(), word, 1,
 						UserDictionary.Words.LOCALE_TYPE_ALL);
 
 				SharedPreferences settings = PreferenceManager
 						.getDefaultSharedPreferences(getActivity()
 								.getApplicationContext());
 				new SendUserIdAsyncTask().execute(word,
 						settings.getInt(PREF_PORT, 2323), sendData.getText()
 								.toString().trim(), mResultCallback);
 			}
 
 		});
 
 		Button butTest = (Button) getView().findViewById(R.id.btnReadBarCode);
 		butTest.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri
 						.parse("content://contacts"));
 				pickContactIntent.setType(Phone.CONTENT_TYPE); // Show user only
 																// contacts w/
 																// phone numbers
 
 				PackageManager packageManager = getActivity()
 						.getApplicationContext().getPackageManager();
 				List<ResolveInfo> activities = packageManager
 						.queryIntentActivities(pickContactIntent, 0);
 				boolean isIntentSafe = activities.size() > 0;
 
 				if (isIntentSafe) {
 					startActivityForResult(pickContactIntent,
 							PICK_CONTACT_REQUEST);
 				} else {
 					Log.e(TAG, "Activity pickContactIntent isn't safe.");
 				}
 			}
 		});
 
 		Button btnHistory = (Button) getView().findViewById(R.id.btnHistory);
 		btnHistory.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				Intent history = new Intent();
 				history.setComponent(new ComponentName(
 						"cz.android.monet.restfulclient",
 						"cz.android.monet.restfulclient.HistoryListActivity"));
 				startActivityForResult(history, HISTORY_REQUEST);
 
 			}
 		});
 
 		locationManager = (LocationManager) getView().getContext()
 				.getSystemService(Context.LOCATION_SERVICE);
 
 		final boolean gpsEnabled = locationManager
 				.isProviderEnabled(LocationManager.GPS_PROVIDER);
 
 		if (!gpsEnabled) {
 			// Build an alert dialog here that requests that the user enable
 			// the location services, then when the user clicks the "OK" button,
 			// call enableLocationSettings()
 			
 		    Message.obtain(mHandler, ENABLE_LOCATION, true).sendToTarget();
 		}
 
 		LocationProvider provider = locationManager
 				.getProvider(LocationManager.GPS_PROVIDER);
 		Log.v(TAG, "Provider accuracy: " + provider.getAccuracy());
 
 		listener = new LocationListener() {
 			public void onLocationChanged(Location location) {
 				// A new location update is received. Do something useful with
 				// it. In this case,
 				// we're sending the update to a handler which then updates the
 				// UI with the new
 				// location.
 				
 				
 				Message.obtain(mHandler, UPDATE_LATLNG,
 						location.getLatitude() + ", " + location.getLongitude())
 						.sendToTarget();
 			}
 
 			public void onStatusChanged(String provider, int status,
 					Bundle extras) {
 				// TODO Auto-generated metMessage.obtain(mHandler,
				// UPDATE_LATLNG,
 				// location.getLatitude() + ", " + location.getLongitude())
 				// .sendToTarget();hod stub
 
 			}
 
 			public void onProviderEnabled(String provider) {
 				// TODO Auto-generated method stub
 
 			}
 
 			public void onProviderDisabled(String provider) {
 				// TODO Auto-generated method stub
 
 			}
 		};
 
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
 				10000, // 10-second interval.
 				10, // 10 meters.
 				listener);
 
 	}
 
 	/**
 	 * Determines whether one Location reading is better than the current
 	 * Location fix
 	 * 
 	 * @param location
 	 *            The new Location that you want to evaluate
 	 * @param currentBestLocation
 	 *            The current Location fix, to which you want to compare the new
 	 *            one
 	 */
 	protected boolean isBetterLocation(Location location,
 			Location currentBestLocation) {
 		if (currentBestLocation == null) {
 			// A new location is always better than no location
 			return true;
 		}
 
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
 
 	/** Checks whether two providers are the same */
 	private boolean isSameProvider(String provider1, String provider2) {
 		if (provider1 == null) {
 			return provider2 == null;
 		}
 		return provider1.equals(provider2);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.support.v4.app.Fragment#onDestroyView()
 	 */
 	@Override
 	public void onDestroyView() {
 		super.onDestroyView();
 
 		locationManager.removeUpdates(listener);
 
 		saveSharedPreferences();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
 	 * android.view.ViewGroup, android.os.Bundle)
 	 */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		super.onCreateView(inflater, container, savedInstanceState);
 		// Inflate the layout for this fragment
 		return inflater.inflate(R.layout.input, container, false);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
 	 */
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 
 		// This makes sure that the container activity has implemented
 		// the callback interface. If not, it throws an exception
 		try {
 			mResultCallback = (OnServerResultReturned) activity;
 		} catch (ClassCastException e) {
 			throw new ClassCastException(activity.toString()
 					+ " must implement OnServerResultReturned");
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.support.v4.app.Fragment#onActivityResult(int, int,
 	 * android.content.Intent)
 	 */
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		// Check which request we're responding to
 		switch (requestCode) {
 		case PICK_CONTACT_REQUEST:
 			// Make sure the request was successful
 			if (resultCode == RESULT_OK) {
 				// Get the URI that points to the selected contact
 				Uri contactUri = data.getData();
 				// We only need the NUMBER column, because there will be only
 				// one row in the result
 				String[] projection = { Phone.NUMBER };
 
 				// Perform the query on the contact to get the NUMBER column
 				// We don't need a selection or sort order (there's only one
 				// result for the given URI)
 				// CAUTION: The query() method should be called from a separate
 				// thread to avoid blocking
 				// your app's UI thread. (For simplicity of the sample, this
 				// code doesn't do that.)
 				// Consider using CursorLoader to perform the query.
 				Cursor cursor = getActivity().getContentResolver().query(
 						contactUri, projection, null, null, null);
 				cursor.moveToFirst();
 
 				// Retrieve the phone number from the NUMBER column
 				int column = cursor.getColumnIndex(Phone.NUMBER);
 				String number = cursor.getString(column);
 
 				// Do something with the phone number...
 				mResultCallback.onResultReturned(number);
 				sendData.setText(number);
 
 			}
 			break;
 		case HISTORY_REQUEST:
 			if (resultCode == RESULT_OK) {
 				host.setText(data.getCharSequenceExtra("NEW_HOST"));
 			}
 
 			break;
 		}
 	}
 }
