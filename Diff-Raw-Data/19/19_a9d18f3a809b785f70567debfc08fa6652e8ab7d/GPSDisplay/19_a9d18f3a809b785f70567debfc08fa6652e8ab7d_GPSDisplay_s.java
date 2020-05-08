 package org.miggy.android.gpsant;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.TextView;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import java.util.Calendar;
 import java.util.Date;
 import java.text.DateFormat;
 
 import org.miggy.android.gpsant.GPSStatus;
 
 public class GPSDisplay extends Activity implements LocationListener {
 //////////////////////////////////////////////////////////////////////////////////////////
 // Class globals
 //////////////////////////////////////////////////////////////////////////////////////////
 	boolean GPSAllowed = false;
 	boolean GPSActive = false;
 	GPSStatus gpsStatus = new GPSStatus(this);
 	LocationManager locationManager = null;
 	long defaultMinTime = 1000L;
 	float defaultMinDistance = 1.0f;
 //////////////////////////////////////////////////////////////////////////////////////////
 	
 //////////////////////////////////////////////////////////////////////////////////////////
 // Access methods for other classes to read/set data
 //////////////////////////////////////////////////////////////////////////////////////////
 	public void setSatsSeen(String s) {
 		((TextView) findViewById(R.id.ValueSatsSeen)).setText(s);
 		updateLastInfo();
 	}
 	public void setSatsSeenDefault() {
 		((TextView) findViewById(R.id.ValueSatsSeen)).setText(getString(R.string.DefaultSatsSeen));
 		updateLastInfo();
 	}
 
 	public void setSatsLockedDefault() {
 		((TextView) findViewById(R.id.ValueSatsLocked)).setText(getString(R.string.DefaultSatsLocked));
 		updateLastInfo();
 	}
 
 	public void setSatsLocked(String s) {
 		((TextView) findViewById(R.id.ValueSatsLocked)).setText(s);
 		updateLastInfo();
 	}
 
 	public void setActive(String s) {
 		((TextView) findViewById(R.id.ValueActive)).setText(s);
 		updateLastInfo();
 	}
 //////////////////////////////////////////////////////////////////////////////////////////
 
 //////////////////////////////////////////////////////////////////////////////////////////
 // Startup code
 //////////////////////////////////////////////////////////////////////////////////////////
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
     }
 //////////////////////////////////////////////////////////////////////////////////////////
        
 //////////////////////////////////////////////////////////////////////////////////////////
 // Pause/Resume/Stop/Start code
 //////////////////////////////////////////////////////////////////////////////////////////
     @Override
     protected void onResume() {
      	super.onResume();
    
      	startGPS(defaultMinTime, defaultMinDistance);
     }
 
     @Override
 	protected void onStart() {
 		super.onStart();
 		
 		startGPS(defaultMinTime, defaultMinDistance);
 	}
     
     @Override
     protected void onPause() {
     	super.onPause();
     	
     	stopGPS();
     }
 
     @Override
     protected void onStop() {
     	super.onStop();
     	
     	stopGPS();
     }
     
     void startGPS(long minTime, float minDistance) {
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
 		GPSAllowed = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 		if (GPSAllowed) {
 			((TextView) findViewById(R.id.ValueAllowed)).setText(getString(R.string.GPSAllowedEnabled));
 		} else {
 			((TextView) findViewById(R.id.ValueAllowed)).setText(getString(R.string.GPSAllowedDisabled));
 		}
 		updateLastInfo();
 				
 		// Hook in our GpsStatus.Listener
 		locationManager.addGpsStatusListener(gpsStatus);
     }
     
     void stopGPS() {
     	locationManager.removeGpsStatusListener(gpsStatus);
     	locationManager.removeUpdates(this);
     	updateLastInfo();
     }
 //////////////////////////////////////////////////////////////////////////////////////////
     
 //////////////////////////////////////////////////////////////////////////////////////////
 // Status change/update code
 //////////////////////////////////////////////////////////////////////////////////////////
     public void onProviderEnabled(String provider) {
     	if (provider.equals(LocationManager.GPS_PROVIDER)) {
     		GPSAllowed = true;
     	}
     }
     
     public void onProviderDisabled(String provider) {
     	if (provider.equals(LocationManager.GPS_PROVIDER)) {
     		GPSAllowed = false;
     	}
     }
     
     public void onLocationChanged(Location location) {
        	((TextView) findViewById(R.id.ValueProvider)).setText(location.getProvider());
     	((TextView) findViewById(R.id.ValueLatitude)).setText(Location.convert(location.getLatitude(), Location.FORMAT_DEGREES));
     	((TextView) findViewById(R.id.ValueLongitude)).setText(Location.convert(location.getLongitude(), Location.FORMAT_DEGREES));
     	if (location.hasAltitude()) {
     		((TextView) findViewById(R.id.ValueAltitude)).setText(String.valueOf(location.getAltitude()));
     	} else {
     		((TextView) findViewById(R.id.ValueAltitude)).setText(getString(R.string.DefaultAltitude));
     	}
     	if (location.hasSpeed()) {
     		((TextView) findViewById(R.id.ValueSpeed)).setText(String.valueOf(location.getSpeed()));
     	} else {
     		((TextView) findViewById(R.id.ValueSpeed)).setText(getString(R.string.DefaultSpeed));
     	}
     	if (location.hasBearing()) {
     		((TextView) findViewById(R.id.ValueBearing)).setText(String.valueOf(location.getBearing()));
     	} else {
     		((TextView) findViewById(R.id.ValueBearing)).setText(getString(R.string.DefaultBearing));
     	}
     	if (location.hasAccuracy()) {
     		((TextView) findViewById(R.id.ValueAccuracy)).setText(String.valueOf(location.getAccuracy()));
     	} else {
     		((TextView) findViewById(R.id.ValueAccuracy)).setText(getString(R.string.DefaultAccuracy));
     	}
    	Date now = Calendar.getInstance().getTime();
     	DateFormat df = DateFormat.getTimeInstance();
     	((TextView) findViewById(R.id.ValueTimestamp)).setText(df.format(now));
    	setActive(getString(R.string.GotFix));
     }
    
     public void onStatusChanged(String provider, int status, Bundle extras) {
     	if (provider.equals(LocationManager.GPS_PROVIDER)) {
     		switch (status) {
     		case LocationProvider.OUT_OF_SERVICE:
     			GPSActive = false;
     			((TextView) findViewById(R.id.ValueActive)).setText(getString(R.string.OutOfService));
     			updateLastInfo();
     			break;
     		case LocationProvider.TEMPORARILY_UNAVAILABLE:
     			GPSActive = false;
     			((TextView) findViewById(R.id.ValueActive)).setText(getString(R.string.TempUnavail));
     			setSatsSeenDefault();
     			setSatsLockedDefault();
     			updateLastInfo();
     			break;
     		case LocationProvider.AVAILABLE:
     			GPSActive = true;
     			((TextView) findViewById(R.id.ValueActive)).setText(getString(R.string.Available));
     			updateLastInfo();
     			break;
     		}
     	}
     }
     
     private void updateLastInfo() {
     	Date now = Calendar.getInstance().getTime();
     	DateFormat df = DateFormat.getTimeInstance();
     	((TextView) findViewById(R.id.ValueLastInfo)).setText(df.format(now));
     }
 //////////////////////////////////////////////////////////////////////////////////////////
 
     
 //////////////////////////////////////////////////////////////////////////////////////////
 // Menu code
 //////////////////////////////////////////////////////////////////////////////////////////
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_menu, menu);
     	
     	return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
     	case R.id.About:
     		View aboutView = getLayoutInflater().inflate(R.layout.about, null, false);
     		
     		AlertDialog.Builder builder = new AlertDialog.Builder(this);
     		builder.setTitle(R.string.AboutTitle);
     		builder.setView(aboutView);
     		builder.create();
     		builder.show();
     		break;
     	case R.id.Exit:
     		finish();
     		return true;
     	}
     	
     	return super.onOptionsItemSelected(item);
     }
 //////////////////////////////////////////////////////////////////////////////////////////
 }
