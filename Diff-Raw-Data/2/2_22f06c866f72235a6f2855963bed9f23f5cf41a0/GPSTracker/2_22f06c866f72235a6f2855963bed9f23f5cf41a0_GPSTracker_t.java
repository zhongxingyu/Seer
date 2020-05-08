 package de.dhbw.tracking;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 
 import de.dhbw.contents.LiveTrackingFragment;
 import de.dhbw.database.Coordinates;
 import de.dhbw.database.DataBaseHandler;
 import de.dhbw.helpers.TrackService;
 import android.app.AlertDialog;
 import android.app.Service;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.StrictMode;
 import android.provider.Settings;
 import android.util.Log;
 import android.widget.Toast;
 
 public class GPSTracker extends Service implements LocationListener {
 	private final Context mContext;
 	
 	//LiveTrackingFragment um Listenwerte zu aktualisieren
 	private LiveTrackingFragment mLiveTrackingFragment;
 	private List<DistanceSegment> mSegmentList = new ArrayList<DistanceSegment>();
 	
 	// Flag fuer GPS Status
 	private boolean isGPSEnabled = false;
 
 	// Flag fuer Netzwerkstatus
 	private boolean isNetworkEnabled = false;
 
 	private boolean canGetLocation = false;
 	
 	//Standort
 	private Location location; 
 	
 	//Hoehengrad
 	private double latitude; 
 	
 	//Breitengrad
 	private double longitude; 
 	
 	
 	//Seehoehe
 	private double altitude; 
 	
 	//Zeitstempel für Trackingpunkte
 	private long timestamp;
 
 
 	// Die minimale Entfernung um Trackingdaten zu aktualisiern
 	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 Meter
 
 	//Die minimale vergangene Zeit um Trackingdaten zu aktualisieren
 	private static final long MIN_TIME_BW_UPDATES = 1000*25; //25 Sekunden
 
 
 	// Location Manager
 	protected LocationManager locationManager;
 	
 
 	// Barriere fuer Meilenstein-Kilometer-Berechnung
 	private int distanceBorder;	
 
 	private final static int DEFAULT_DISTANCE_BORDER = 1;
 
 	public GPSTracker(Context context) {
 		this.mContext = context;
 		this.distanceBorder = DEFAULT_DISTANCE_BORDER;
 		getLocation();
 	}
 	
 	public GPSTracker(Context context, LiveTrackingFragment mLiveTrackingFragment) {
 		this.mContext = context;
 		this.distanceBorder = DEFAULT_DISTANCE_BORDER;
 		setLiveTrackingFragment(mLiveTrackingFragment);
 		getLocation();
 	}
 	
 	
 	//aktuellen Standort ermitteln
 	public Location getLocation() {
         try {
             locationManager = (LocationManager) mContext
                     .getSystemService(LOCATION_SERVICE);
  
             // GPS Status abfragen
             isGPSEnabled = locationManager
                     .isProviderEnabled(LocationManager.GPS_PROVIDER);
  
             // Netzwerkstatus abfragen
             isNetworkEnabled = locationManager
                     .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
  
             if (!isGPSEnabled && !isNetworkEnabled) {
                 // kein Netzwerkprovider, kein GPS Empfang, Warnungsmeldung ausgeben
             	showSettingsAlert();
             } else {
                 this.canGetLocation = true;
                 // Standort bevorzugt ueber Netzwerk ermitteln
                 if (isNetworkEnabled) {
                     locationManager.requestLocationUpdates(
                             LocationManager.NETWORK_PROVIDER,
                             MIN_TIME_BW_UPDATES,
                             MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                     Log.d("Network", "Network");
                     if (locationManager != null) {
                         location = locationManager
                                 .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                     }
                 }
                 // Standortermittlung uber GPS statt Netzwerk
                 if (isGPSEnabled) {
                     if (location == null) {
                         locationManager.requestLocationUpdates(
                                 LocationManager.GPS_PROVIDER,
                                 MIN_TIME_BW_UPDATES,
                                 MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                         Log.d("GPS Enabled", "GPS Enabled");
                         if (locationManager != null) {
                             location = locationManager
                                     .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                         }
                     }
                 }
             }
  
         } catch (Exception e) {
             e.printStackTrace();
         }
  
         return location;
     }
 
 	@Override
 	public void onLocationChanged(Location location) {
 		if (location != null) {
 			latitude = location.getLatitude();
 			longitude = location.getLongitude();
 			altitude = getElevationFromGoogleMaps(longitude, latitude);
 			timestamp = location.getTime();
 			DataBaseHandler db = new DataBaseHandler(mContext);
 			
 			//neue Koordinaten speichern
 			db.addCoordinates(new Coordinates(longitude, latitude, altitude, timestamp));
 			
 			List<Coordinates> coordinatePairs = db.getAllCoordinatePairs();
 			
 			//TODO comment
 			double distance = TrackService.calcDistance(coordinatePairs);
 			if (distance >= distanceBorder)
 			{
 				int oldDuration = 0;
 				if (mLiveTrackingFragment.mSegmentList.size() >= 1)
					oldDuration = durationToSeconds(mLiveTrackingFragment.mSegmentList.get(mLiveTrackingFragment.mSegmentList.size()-1).getDuration());
 				int newDuration = durationToSeconds(TrackService.calcDuration(coordinatePairs));
 				String distanceString = String.valueOf(distance);
 				String duration = secondsToString(newDuration - oldDuration);
 				String speed = String.valueOf(TrackService.calcPace(coordinatePairs));
 				mLiveTrackingFragment.mSegmentList.add(new DistanceSegment(distanceString, duration, speed));
 				distanceBorder++;
 			}
 		}		
 		// Live Tracking Liste aktualisieren
 		mLiveTrackingFragment.setList();
 	}
 	
 	public String secondsToString(int seconds) {	
 		String duration = "";
 		DecimalFormat df = new DecimalFormat("00");
 		int realSeconds = seconds;
 		duration += String.valueOf(df.format((int)realSeconds/3600)) + ":";
 		realSeconds -= ((int)realSeconds/3600)*3600;
 		duration += String.valueOf(df.format((int)realSeconds/60)) + ":";
 		realSeconds -= ((int)realSeconds/60)*60;
 		duration += String.valueOf(df.format((int)realSeconds));
 		return duration;
 	}
 	
 	public int durationToSeconds(String duration) {		
 		int seconds = 0;
 		String[] durationArray = duration.split(":");
 		seconds += Integer.valueOf(durationArray[2]) + Integer.valueOf(durationArray[1])*60 + Integer.valueOf(durationArray[0])*3600;
 		return seconds;
 	}
  
     @Override
     public void onProviderDisabled(String provider) {
     }
  
     @Override
     public void onProviderEnabled(String provider) {
     }
  
     @Override
     public void onStatusChanged(String provider, int status, Bundle extras) {
     }
  
     @Override
     public IBinder onBind(Intent arg0) {
         return null;
     }
     
     /**
      * liefert Hoehengrad
      * */
     public double getLatitude(){
         if(location != null){
             latitude = location.getLatitude();
         }
          
         // return latitude
         return latitude;
     }
      
     /**
      * liefert Breitengrad
      * */
     public double getLongitude(){
         if(location != null){
             longitude = location.getLongitude();
         }
          
         // return longitude
         return longitude;
     }
     /**
      * Prueft besten Netzwerkprovider
      * @return boolean
      * */
     public boolean canGetLocation() {
         return this.canGetLocation;
     }
      
     /**
      * Warnung ueber Einstellungen
      * */
     public void showSettingsAlert(){
         AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
       
         // Warnungtitel
         alertDialog.setTitle("GPS Einstellungen");
   
         // Warnungsnachricht
         alertDialog.setMessage("GPS ist nicht aktiviert. Moechten Sie zu den Einstellungen wechseln?");
         
         // Wenn Einstellungen gedrückt
         alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog,int which) {
                 Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                 mContext.startActivity(intent);
             }
         });
   
         // Wenn Cancel gedrueckt
         alertDialog.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
             dialog.cancel();
             }
         });
   
         // Warnungstext einblenden
         alertDialog.show();
     }
     
     
     /**
      * Tracking anhalten
      * 
      * */
     public void stopUsingGPS(){
         if(locationManager != null){
             locationManager.removeUpdates(GPSTracker.this);
         }      
         distanceBorder = DEFAULT_DISTANCE_BORDER;
     }
     
     
     //Seehoehe ueber Googledienst ermitteln
     public static double getElevationFromGoogleMaps(double longitude, double latitude) {
     	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
     	StrictMode.setThreadPolicy(policy); 
         double result = Double.NaN;
         HttpClient httpClient = new DefaultHttpClient();
         HttpContext localContext = new BasicHttpContext();
         String url = "http://maps.googleapis.com/maps/api/elevation/"
                 + "xml?locations=" + String.valueOf(latitude)
                 + "," + String.valueOf(longitude)
                 + "&sensor=true";
         HttpGet httpGet = new HttpGet(url);
         try {
             HttpResponse response = httpClient.execute(httpGet, localContext);
             HttpEntity entity = response.getEntity();
             if (entity != null) {
                 InputStream instream = entity.getContent();
                 int r = -1;
                 StringBuffer respStr = new StringBuffer();
                 while ((r = instream.read()) != -1)
                     respStr.append((char) r);
                 String tagOpen = "<elevation>";
                 String tagClose = "</elevation>";
                 if (respStr.indexOf(tagOpen) != -1) {
                     int start = respStr.indexOf(tagOpen) + tagOpen.length();
                     int end = respStr.indexOf(tagClose);
                     String value = respStr.substring(start, end);
                     result = (double)(Double.parseDouble(value));
                 }
                 instream.close();
             }
         } catch (ClientProtocolException e) {} 
         catch (IOException e) {}
 
         return result;
     }
 
 	public LiveTrackingFragment getLiveTrackingFragment() {
 		return mLiveTrackingFragment;
 	}
 
 	public void setLiveTrackingFragment(LiveTrackingFragment mLiveTrackingFragment) {
 		this.mLiveTrackingFragment = mLiveTrackingFragment;
 	}
 
     
 }
