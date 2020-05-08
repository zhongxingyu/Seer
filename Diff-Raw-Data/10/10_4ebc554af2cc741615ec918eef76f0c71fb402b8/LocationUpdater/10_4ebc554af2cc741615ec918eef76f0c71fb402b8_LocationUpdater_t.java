 package fr.utc.nf33.ins;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.location.GpsSatellite;
 import android.location.GpsStatus;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.support.v4.content.LocalBroadcastManager;
 
 import com.google.android.gms.maps.LocationSource;
 
 public class LocationUpdater extends Service {
 
   // Binder given to clients
   private final IBinder mBinder = new LocalBinder();
 
   /**
    * Class used for the client Binder.  Because we know this service always
    * runs in the same process as its clients, we don't need to deal with IPC.
    */
   public class LocalBinder extends Binder {
     LocationUpdater getService() {
       // Return this instance of LocationUpdater so clients can call public methods
       return LocationUpdater.this;
     }
   }
 
   @Override
   public IBinder onBind(Intent intent) {
 
     locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
     bestLocationProvider = new BestLocationProvider();
     gpsStatusListener = new GpsStatusListener();
     locationManager.addGpsStatusListener(gpsStatusListener);
     return mBinder;
   }
 
   @Override
   public boolean onUnbind(Intent intent) {
     locationManager.removeGpsStatusListener(gpsStatusListener);
     return super.onUnbind(intent);
   }
 
   private BestLocationProvider bestLocationProvider;
 
   private GpsStatusListener gpsStatusListener;
 
   private LocationManager locationManager;
 
   public static final int INDOOR = 0;
   public static final int OUTDOOR = 1;
 
   private enum BroadcastTypes {
     TRANSITION_INDOOR, TRANSITION_OUTDOOR, WRITE_SNR, NEW_POSITION
   }
 
   private void sendBroadcast(BroadcastTypes type) {
     Intent intent;
     switch(type){
       case TRANSITION_INDOOR:
         intent = new Intent("transition");
         intent.putExtra("situation", INDOOR);
         LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
         break;
       case TRANSITION_OUTDOOR:
         intent = new Intent("transition");
         intent.putExtra("situation", OUTDOOR);
         LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
         break;
       case WRITE_SNR:
         intent = new Intent("snr");
         intent.putExtra("snr", gpsStatusListener.averageSnr);
         LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
         break;
       case NEW_POSITION:
         intent = new Intent("new_position");
         intent.putExtra("latitude", bestLocationProvider.currentBestLocation.getLatitude());
         intent.putExtra("longitude", bestLocationProvider.currentBestLocation.getLongitude());
         LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
         break;
     }
   }
 
   private int currentSituation = OUTDOOR;
 
   public BestLocationProvider getBestLocationProvider() {
     return bestLocationProvider;
   }
 
   public GpsStatus.Listener getGpsStatusListener() {
     return gpsStatusListener;
   }
 
   private final class BestLocationProvider implements LocationSource, LocationListener {
     //
     private static final float GPS_MIN_DISTANCE = 10;
     //
     private static final short GPS_MIN_TIME = 3000;
     //
     private static final float NETWORK_MIN_DISTANCE = 0;
     //
     private static final short NETWORK_MIN_TIME = 30000;
     //
     private static final int TWO_MINUTES = 1000 * 60 * 2;
     //
     private Location currentBestLocation;
     //
     private OnLocationChangedListener listener;
 
     @Override
     public void activate(OnLocationChangedListener listener) {
       this.listener = listener;
 
       if (locationManager.getProvider(LocationManager.GPS_PROVIDER) != null)
         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_TIME,
           GPS_MIN_DISTANCE, this);
 
       if (locationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null)
         locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, NETWORK_MIN_TIME,
           NETWORK_MIN_DISTANCE, this);
     }
 
     @Override
     public void deactivate() {
       locationManager.removeUpdates(this);
     }
 
     /**
      * Determines whether one Location reading is better than the current Location fix.
      * 
      * @param location the new Location that you want to evaluate.
      * @return true when the new Location is better than the current one, false otherwise.
      */
     protected boolean isBetterLocation(Location location) {
       // A new location is always better than no location.
       if (currentBestLocation == null) return true;
 
       // Check whether the new location fix is newer or older.
       long timeDelta = location.getTime() - currentBestLocation.getTime();
       boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
       boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
       boolean isNewer = timeDelta > 0;
 
       // If it's been more than two minutes since the current location, use the new location
       // because the user has likely moved.
       if (isSignificantlyNewer)
         return true;
       // If the new location is more than two minutes older, it must be worse.
       else if (isSignificantlyOlder) return false;
 
       // Check whether the new location fix is more or less accurate.
       int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
       boolean isLessAccurate = accuracyDelta > 0;
       boolean isMoreAccurate = accuracyDelta < 0;
       boolean isSignificantlyLessAccurate = accuracyDelta > 200;
 
       // Check if the old and new location are from the same provider.
       boolean isFromSameProvider =
           isSameProvider(location.getProvider(), currentBestLocation.getProvider());
 
       // Determine location quality using a combination of timeliness and accuracy.
       if (isMoreAccurate)
         return true;
       else if (isNewer && !isLessAccurate)
         return true;
       else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
         return true;
       else
         return false;
     }
 
     // Checks whether two providers are the same.
     private boolean isSameProvider(String provider1, String provider2) {
       if (provider1 == null)
         return provider2 == null;
       else
         return provider1.equals(provider2);
     }
 
     @Override
     public void onLocationChanged(Location location) {
       if ((listener == null) || (!isBetterLocation(location))) return;
 
       currentBestLocation = location;
       listener.onLocationChanged(currentBestLocation);
       
       sendBroadcast(BroadcastTypes.NEW_POSITION);
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
 
   }
 
   private final class GpsStatusListener implements GpsStatus.Listener {
 
     private float averageSnr = 0;
 
     private final byte SATELLITES_COUNT = 3;
 
     private final byte SNR_THRESHOLD = 35;
    
    private boolean firstFix = false;
 
     @Override
     public void onGpsStatusChanged(int event) {
      
      if(event == GpsStatus.GPS_EVENT_FIRST_FIX) {
        firstFix = true;
      }
 
      if (event == GpsStatus.GPS_EVENT_STOPPED && firstFix) {
 
         float[] snrArr = new float[SATELLITES_COUNT];
 
         for (GpsSatellite sat : locationManager.getGpsStatus(null).getSatellites()) {
           int min = 0;
           for (int s = 0; s < SATELLITES_COUNT; ++s)
             if (snrArr[s] < snrArr[min]) min = s;
 
           float snr = sat.getSnr();
           if (snr > snrArr[min]) snrArr[min] = snr;
         }
 
         float newAvgSnr = 0;
         for (float snr : snrArr)
           newAvgSnr += snr;
         newAvgSnr /= SATELLITES_COUNT;
         if (newAvgSnr != 0) averageSnr = newAvgSnr;
 
         sendBroadcast(BroadcastTypes.WRITE_SNR);
 
         if(averageSnr < SNR_THRESHOLD && currentSituation != INDOOR) {
           currentSituation = INDOOR;
           sendBroadcast(BroadcastTypes.TRANSITION_INDOOR);
         }
         else if(averageSnr >= SNR_THRESHOLD && currentSituation != OUTDOOR){
           currentSituation = OUTDOOR;
           sendBroadcast(BroadcastTypes.TRANSITION_OUTDOOR);
         }
       }
     }
   }
 }
