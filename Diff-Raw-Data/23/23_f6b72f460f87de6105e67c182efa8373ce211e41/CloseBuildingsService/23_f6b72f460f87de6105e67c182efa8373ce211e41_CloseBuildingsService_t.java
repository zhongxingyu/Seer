 /**
  * 
  */
 package fr.utc.nf33.ins.location;
 
 import java.util.List;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.support.v4.content.LocalBroadcastManager;
 
 import com.google.android.gms.maps.LocationSource;
 
 import fr.utc.nf33.ins.db.InsDbHelper;
 
 
 /**
  * 
  * @author
  * 
  */
 public final class CloseBuildingsService extends Service {
   //
   public final class BestLocationProvider implements LocationSource, LocationListener {
     //
     private final class BestLocationTask extends AsyncTask<Void, Void, List<Building>> {
       //
       private final class LocationPlaceholder {
         //
         private final float accuracy;
         //
         private final double latitude;
         //
         private final double longitude;
         //
         private final String provider;
         //
         private final long time;
 
         //
         private LocationPlaceholder(Location location) {
           accuracy = location.getAccuracy();
           latitude = location.getLatitude();
           longitude = location.getLongitude();
           provider = new String(location.getProvider());
           time = location.getTime();
         }
       }
 
       //
       private static final int TWO_MINUTES = 1000 * 60 * 2;
 
       //
       private final float mAverageSnr;
       //
       private LocationPlaceholder mBestLocation;
       //
       private final LocationPlaceholder mLocation;
       //
       private final Location mPassthrough;
 
       //
       private BestLocationTask(Location location, Location bestLocation, float averageSnr) {
         mPassthrough = location;
         mLocation = new LocationPlaceholder(location);
         if (bestLocation != null) mBestLocation = new LocationPlaceholder(bestLocation);
         mAverageSnr = averageSnr;
       }
 
       @Override
       protected final List<Building> doInBackground(Void... params) {
         if (!isBetterLocation(mLocation, mBestLocation)) return null;
 
         mBestLocation = mLocation;
         publishProgress();
 
         if (mAverageSnr >= LocationHelper.SNR_THRESHOLD) {
           InsDbHelper dbHelper = new InsDbHelper(CloseBuildingsService.this);
 
           Cursor cursor = dbHelper.getEntryPoints(mBestLocation.latitude, mBestLocation.longitude);
           if (isCancelled()) {
             cursor.close();
             dbHelper.close();
             return null;
           }
           List<Building> closeBuildings =
               LocationHelper.getCloseBuildings(mBestLocation.latitude, mBestLocation.longitude,
                   cursor);
           cursor.close();
           dbHelper.close();
 
           return closeBuildings;
         } else {
           return null;
         }
       }
 
       /**
        * Determines whether one Location reading is better than the current Location fix
        * 
        * @param location The new Location that you want to evaluate
        * @param currentBestLocation The current Location fix, to which you want to compare the new
        *        one
        */
       private final boolean isBetterLocation(LocationPlaceholder location,
           LocationPlaceholder currentBestLocation) {
         if (currentBestLocation == null) {
           // A new location is always better than no location
           return true;
         }
 
         // Check whether the new location fix is newer or older
         long timeDelta = location.time - currentBestLocation.time;
         boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
         boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
         boolean isNewer = timeDelta > 0;
 
         // If it's been more than two minutes since the current location, use the new location
         // because the user has likely moved
         if (isSignificantlyNewer) {
           return true;
           // If the new location is more than two minutes older, it must be worse
         } else if (isSignificantlyOlder) {
           return false;
         }
 
         // Check whether the new location fix is more or less accurate
         int accuracyDelta = (int) (location.accuracy - currentBestLocation.accuracy);
         boolean isLessAccurate = accuracyDelta > 0;
         boolean isMoreAccurate = accuracyDelta < 0;
         boolean isSignificantlyLessAccurate = accuracyDelta > 200;
 
         // Check if the old and new location are from the same provider
         boolean isFromSameProvider =
             isSameProvider(location.provider, currentBestLocation.provider);
 
         // Determine location quality using a combination of timeliness and accuracy
         if (isMoreAccurate) {
           return true;
         } else if (isNewer && !isLessAccurate) {
           return true;
         } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
           return true;
         }
         return false;
       }
 
       // Checks whether two providers are the same.
       private final boolean isSameProvider(String provider1, String provider2) {
         if (provider1 == null)
           return provider2 == null;
         else
           return provider1.equals(provider2);
       }
 
       @Override
       protected final void onCancelled(List<Building> closeBuildings) {
         mBestLocationTask = null;
       }
 
       @Override
       protected final void onPostExecute(List<Building> closeBuildings) {
         if (closeBuildings != null) BestLocationProvider.this.mCloseBuildings = closeBuildings;
 
         LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(CloseBuildingsService.this);
         lbm.sendBroadcast(LocationIntent.NewCloseBuildings.newIntent());
 
         mBestLocationTask = null;
       }
 
       @Override
       protected final void onProgressUpdate(Void... voiz) {
         BestLocationProvider.this.mBestLocation = mPassthrough;
         if (mListener != null) mListener.onLocationChanged(mPassthrough);
         LocalBroadcastManager.getInstance(CloseBuildingsService.this).sendBroadcast(
             LocationIntent.NewLocation.newIntent(mPassthrough.getLatitude(),
                 mPassthrough.getLongitude()));
       }
     }
 
     //
    private static final float GPS_MIN_DISTANCE = 0F;
     //
     private static final short GPS_MIN_TIME = 3000;
     //
     private static final float NETWORK_MIN_DISTANCE = 0F;
     //
     private static final short NETWORK_MIN_TIME = 30000;
 
     //
     private float mAverageSnr;
     //
     private Location mBestLocation;
     //
     private AsyncTask<Void, Void, List<Building>> mBestLocationTask;
     //
     private List<Building> mCloseBuildings;
     //
     private OnLocationChangedListener mListener;
 
     @Override
     public final void activate(OnLocationChangedListener listener) {
       mListener = listener;
     }
 
     @Override
     public final void deactivate() {
       mListener = null;
     }
 
     @Override
     public final void onLocationChanged(Location location) {
       if (mBestLocationTask != null) return;
       mBestLocationTask = new BestLocationTask(location, mBestLocation, mAverageSnr);
       mBestLocationTask.execute();
     }
 
     @Override
     public final void onProviderDisabled(String provider) {
 
     }
 
     @Override
     public final void onProviderEnabled(String provider) {
 
     }
 
     @Override
     public final void onStatusChanged(String provider, int status, Bundle extras) {
 
     }
 
     //
     private final void removeLocationUpdates() {
       LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
       lm.removeUpdates(mBestLocationProvider);
       if (mBestLocationTask != null) mBestLocationTask.cancel(true);
     }
 
     //
     private final void requestLocationUpdates() {
       LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
       if (lm.getProvider(LocationManager.GPS_PROVIDER) != null)
         lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_TIME, GPS_MIN_DISTANCE,
             this);
       if (lm.getProvider(LocationManager.NETWORK_PROVIDER) != null)
         lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, NETWORK_MIN_TIME,
             NETWORK_MIN_DISTANCE, this);
     }
   }
 
   /**
    * Class used for the client Binder. Because we know this service always runs in the same process
    * as its clients, we don't need to deal with IPC.
    */
   public final class LocalBinder extends Binder {
     public CloseBuildingsService getService() {
       // Return this instance of LocationService so that clients can call public methods.
       return CloseBuildingsService.this;
     }
   }
 
   //
   private BestLocationProvider mBestLocationProvider;
   //
   private BroadcastReceiver mNewSnrReceiver;
 
   /**
    * 
    * @return
    */
   public final BestLocationProvider getBestLocationProvider() {
     return mBestLocationProvider;
   }
 
   /**
    * 
    * @return
    */
   public final List<Building> getCloseBuildings() {
     return mBestLocationProvider.mCloseBuildings;
   }
 
   @Override
   public final IBinder onBind(Intent intent) {
     onRebind(intent);
 
     return new LocalBinder();
   }
 
   @Override
   public final void onCreate() {
     mBestLocationProvider = new BestLocationProvider();
   }
 
   @Override
   public final void onDestroy() {
     mBestLocationProvider = null;
   }
 
   @Override
   public final void onRebind(Intent intent) {
     // Register receivers.
     LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
 
     mNewSnrReceiver = new BroadcastReceiver() {
       @Override
       public final void onReceive(Context context, Intent intent) {
         mBestLocationProvider.mAverageSnr =
             intent.getFloatExtra(LocationIntent.NewSnr.EXTRA_SNR, 0);
       }
     };
     lbm.registerReceiver(mNewSnrReceiver, LocationIntent.NewSnr.newIntentFilter());
 
     //
     mBestLocationProvider.requestLocationUpdates();
   }
 
   @Override
   public final boolean onUnbind(Intent intent) {
     //
     mBestLocationProvider.removeLocationUpdates();
 
     // Unregister receivers.
     LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
     lbm.unregisterReceiver(mNewSnrReceiver);
     mNewSnrReceiver = null;
 
     return true;
   }
 }
