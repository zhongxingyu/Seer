 package com.finalhack.totalelevation;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 
 public class ElevationActivity extends Activity {
 
 
 	private static final int GPS_UPDATE_FREQUENCY = 3000;
 	//UI views
 	private Graph graph;
 	private TextView numSatellites;
 	private TextView signalStrength;
 	private TextView fixQuality;
 	private TextView prnList;
 
 	//Some non-ui properties
 	private LocationManager locationManager;
 	private LocationListener locationListener;
 	private NmeaListener nmeaListener;
 	private double startElevation = Double.MIN_VALUE;	
 	private String locale = "en_US";
 
 	//All our constants
 	public static final String TAG_ELEVATION = "";
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.elevation);
     }
     
     //If we're stopping, kill GPS updates
     @Override
     public void onPause()
     {
     	super.onPause();
     	//If we were listening for locations, stop
     	if (locationListener != null) locationManager.removeUpdates(locationListener);
     	if (nmeaListener != null) locationManager.removeNmeaListener(nmeaListener);
     }
     
     //Start GPS updates
     @Override
     public void onResume()
     {
     	super.onResume();
     	//Save the locale for string formatting
     	locale = Locale.getDefault().toString();
     	
     	if (!startGps())
     	{
     		TextView elevation = (TextView)findViewById(R.id.text_elevation);
     		elevation.setText(getString(R.string.gps_disabled));
     	}
     }
     
     public void help(View view)
     {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(getString(R.string.help));
 		builder.setPositiveButton(getString(R.string.ok), null);
 		builder.create().show();
     }
     
     //Much of the app work happens here
     private boolean startGps()
     {
     	if (BuildConfig.DEBUG) Log.d(TAG_ELEVATION, Locale.getDefault().toString());
     	
     	//Hook up UI elements
     	graph = (Graph)findViewById(R.id.graph);
     	signalStrength = (TextView)findViewById(R.id.gps_signal_strength);
     	numSatellites = (TextView)findViewById(R.id.gps_number_of_satellites);
     	fixQuality = (TextView)findViewById(R.id.gps_fix_quality);
     	prnList = (TextView)findViewById(R.id.prn_list);
     	
     	//Startup location services
     	locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
     	
     	if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return false;
     	
     	locationListener = new LocationListener();
     	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_FREQUENCY, 0, locationListener);
     	
     	//Gather statistics using NMEA
     	ViewHolder viewHolder = new ViewHolder(this, numSatellites, signalStrength, fixQuality, prnList);
     	nmeaListener = new com.finalhack.totalelevation.NmeaListener(viewHolder);
     	locationManager.addNmeaListener(nmeaListener);
     	
     	return true;
     }
     
 
     
     // Use this to get the elevation
     private class LocationListener implements android.location.LocationListener
     {
     	private List<Double> lastElevations = new ArrayList<Double>();
 		private static final int MOVING_AVERAGE = 5;
     	
 		@Override
 		public void onLocationChanged(Location location)
 		{
 			//Hook up informational views
 			if (BuildConfig.DEBUG) Log.d(TAG_ELEVATION,"location:" + location);
 			TextView elevation = (TextView)findViewById(R.id.text_elevation);
 			TextView lat = (TextView)findViewById(R.id.text_lat);
 			TextView lon = (TextView)findViewById(R.id.text_lon);
 			TextView change = (TextView)findViewById(R.id.text_change);
 			TextView start = (TextView)findViewById(R.id.text_start);
 			
 			//Update all the text with location data
 			elevation.setTextSize(80);
 			elevation.setText(Util.localizeString(location.getAltitude(), locale));
 			lat.setText(getString(R.string.lat)+location.getLatitude());
 			lon.setText(getString(R.string.lon)+location.getLongitude());
 			
			startElevation = location.getAltitude();
 			start.setText(getString(R.string.elevation_start) + Util.localizeString(startElevation, locale));
 			
 			lastElevations.add(location.getAltitude());
 			
 			// Keep a moving average to smooth errors by removing the first element if we have more than we need
 			//  for a moving average
 			if (lastElevations.size() > MOVING_AVERAGE)	lastElevations.remove(0);
 			
 			// Don't update if we don't have at least two fixes to average
 			if (lastElevations.size() < 2) return;
 			
 			int movingAverageElevation = 0;
 			for (double previousElevation : lastElevations) movingAverageElevation += previousElevation;
 			
 			movingAverageElevation /= lastElevations.size();
 			
 			double totalChange = movingAverageElevation - startElevation;
 			change.setText(getString(R.string.elevation_change)+Util.localizeString(totalChange, locale));
 			
 			//Add the elevation to the graph
 			graph.updateElevation(Util.localizeInt(movingAverageElevation, locale));
 		}
 		
 		@Override public void onProviderDisabled(String provider){}
 		@Override public void onProviderEnabled(String provider){}
 		@Override public void onStatusChanged(String provider, int status, Bundle extras) {}
     }
 }
