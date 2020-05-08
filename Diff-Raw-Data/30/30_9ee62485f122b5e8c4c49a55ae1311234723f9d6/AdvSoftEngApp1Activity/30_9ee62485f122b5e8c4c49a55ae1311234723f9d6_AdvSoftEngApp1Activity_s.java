 package com.advsofteng.app1;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.*;
 import android.os.Bundle;
 import android.provider.Settings.Secure;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 /**
  * @author alandonohoe
  * 
  * This App displays the current time and the device's current GPS co-ordinates.
  * if GPS data is unavailable, it displays "GPS not available".
  * 
  */
 public class AdvSoftEngApp1Activity extends Activity {
 
 	/* Interval between deliveries of location data to the server */
 	private static final int POLL_INTERVAL = (10 * 1000);
 	
 	public static final String ENDPOINT = "http://nearme.tomhume.org:8080/NearMeServer";
 	public static final String TAG = "LocationPoster";	/* used for logging purposes */
 	public static final int MAGIC_NUMBER = 12345;		/* used as a reference to an Alarm */
 	
 
 	private LocationManager manager;
 	
 	private PendingIntent alarmIntent = null;	/* Handle to the repeatedly called Intent for triggering polls */
 	private TextView tvGPS = null;				/* TextView to show GPS location on-screen */
 	private SharedPreferences prefs = null;		/* used to share location & time between Activity and BroadcastReceiver */
	private Button button = null;				/* start/stop button */
 	private Button buttonGetPOI = null;			/* get POIs button*/
 	private Button buttonMap = null;         /* View Map POI Button*/
 
 	public static ArrayList<Poi>  poiArray = new ArrayList<Poi>(); 
 	public static AddressBook globalAddressBook = new AddressBook();
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.options, menu);
 	    return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 		    case R.id.add_poi:
 		        addPlace();
 		        return true;
 		    case R.id.upload_ab:
 		    	manageAddressBook();
 		    	return true;
 		    default:
 		        return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	private void addPlace() {
 		Intent intentAdd = new Intent(AdvSoftEngApp1Activity.this, addPlace.class);
 		startActivity(intentAdd);
 	}
 	
 	private void manageAddressBook() {
 		Intent intentAddressRip = new Intent(AdvSoftEngApp1Activity.this, AddressBookRipperActivity.class);
 		Log.i(TAG, "ManageAddressBook");
 		startActivity(intentAddressRip);
 	}
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.main);
 
 
         /* get handles to the TextView where GPS position is displayed */
         
         tvGPS = (TextView) findViewById(R.id.textViewGPS);
         
         /* We may know a location even before we start; if so, use it. */
 
         prefs = getSharedPreferences(TAG, Context.MODE_PRIVATE);
         saveDeviceId();
 
         manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
         Location loc = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
         setLocation(loc);
 
       
 		/* connect the listener object to receive GPS updates */
 
 		manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new AdvSoftEngLocationListener(this));
 		
 	
 		
 	   //*************************************
         //Deal with view map button
        
        buttonMap=  (Button) findViewById(R.id.mapButton);
        buttonMap.setOnClickListener(new View.OnClickListener() {
 		public void onClick(View v) {
 			Intent intentMAP = new Intent(AdvSoftEngApp1Activity.this, map.class);
 			startActivity(intentMAP);
 		}
        });
         
  
         //////////////////////////////////////////
         // deals with getPOI button.
         buttonGetPOI = (Button) findViewById(R.id.getPOIButton);
         buttonGetPOI.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent intentPOI = new Intent(AdvSoftEngApp1Activity.this, PreferencesActivity.class);
 				startActivity(intentPOI);
 				 buttonMap.setVisibility(0);
 			} // end of onClickView(View v)
 			}//  end of View.OnClickListener
         ); // end of setOnClickListener
     }
     
 	/**
 	 * updates TextView tv text with location's longitude and latitude data.
 	 * 
 	 * @param location
 	 * @param tv
 	 */
     
     public void setLocation(Location location)
     {
     	/* If we get a null location, clear out the preferences
     	 * and pop up the error message
     	 */
     	
     	if (null == location) {
     		Log.i(TAG, "null location, clearing out");
     		emptyPrefs();
     		tvGPS.setText(getString(R.string.no_gps_error));
     		return;
     	}
     	
 		String strGPS = "Longitude = " + location.getLongitude() + "\n";
 		strGPS += "Latitude = " + location.getLatitude();
 		tvGPS.setText(strGPS);
 		
 		// then slap it into those shared preferences so it gets sent up in a poll
 		
 		SharedPreferences.Editor edit = prefs.edit();
 		edit.putString(PreferencesActivity.KEY_TIME, new Date().toString());
 		edit.putString(PreferencesActivity.KEY_LAT, Double.toString(location.getLatitude()));
 		edit.putString(PreferencesActivity.KEY_LNG, Double.toString(location.getLongitude()));
 		edit.commit();
     }
     
     private void saveDeviceId() {
 		SharedPreferences.Editor edit = prefs.edit();
 		edit.putString(PreferencesActivity.KEY_ID, Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID));
 		edit.commit();
     }
     
     private void emptyPrefs() {
 		Log.i(TAG, "emptyPrefs");
 		SharedPreferences.Editor edit = prefs.edit();
 		edit.remove(PreferencesActivity.KEY_TIME);
 		edit.remove(PreferencesActivity.KEY_LAT);
 		edit.remove(PreferencesActivity.KEY_LNG);
 		edit.commit();
 		
     }
 
 	protected void onPause() {
 		super.onPause();
 
 		/* Leaving the app, even briefly? Cancel any subsequent polls and reset button to start */
 
 		if (alarmIntent!=null) {
 			Log.i(TAG,"starting polling");
 			Context ctx = getApplicationContext();
 			AlarmManager am = (AlarmManager) ctx.getSystemService(Activity.ALARM_SERVICE);
 			am.cancel(alarmIntent);
 			alarmIntent = null;
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
         /*
          * Start the regular polling for POIs
          */
 
 		if (alarmIntent==null) {
 			Log.i(TAG,"starting polling");
 			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
 	    	Calendar cal = Calendar.getInstance();
 			Intent intent = new Intent(getApplicationContext(), PoiPoller.class);
 			alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), MAGIC_NUMBER, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 			am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), POLL_INTERVAL , alarmIntent);
 		}
 	}
 	
 
 	
 }
 
 
