 package fr.utc.nf33.ins;
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.location.GpsSatellite;
 import android.location.GpsStatus;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.Menu;
 import android.widget.TextView;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMapOptions;
 import com.google.android.gms.maps.LocationSource;
 import com.google.android.gms.maps.SupportMapFragment;
 
 import fr.utc.nf33.ins.db.InsContract;
 import fr.utc.nf33.ins.db.InsDbHelper;
 
 /**
  * 
  * @author
  * 
  */
 public final class MainActivity extends FragmentActivity
     implements
       GpsDialogFragment.GpsDialogListener {
   //
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
 
       // TODO
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
 
   //
   private final class GpsStatusListener implements GpsStatus.Listener {
     //
     private float averageSnr = 0;
     //
     private final byte SATELLITES_COUNT = 3;
     //
     private final byte SNR_THRESHOLD = 35;
     //
     private DialogFragment transitionDialogFragment;
 
     private void dismissTransitionDialogFragment() {
       if (transitionDialogFragment != null) transitionDialogFragment.dismiss();
     }
 
     @Override
     public void onGpsStatusChanged(int event) {
       if (event == GpsStatus.GPS_EVENT_STOPPED) {
         // TODO
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
 
         ((TextView) MainActivity.this.findViewById(R.id.bottom)).setText("SNR (3 premiers): "
             + Float.toString(averageSnr));
         if (averageSnr < SNR_THRESHOLD)
           showTransitionDialogFragment();
         else
           dismissTransitionDialogFragment();
       }
     }
 
     private void showTransitionDialogFragment() {
       if (transitionDialogFragment == null)
         transitionDialogFragment = new TransitionDialogFragment();
       transitionDialogFragment.show(getSupportFragmentManager(), "TransitionDialogFragment");
     }
   }
 
   //
   private static final GoogleMapOptions GOOGLE_MAP_OPTIONS = new GoogleMapOptions();
   static {
     GOOGLE_MAP_OPTIONS.compassEnabled(false);
     GOOGLE_MAP_OPTIONS.mapType(GoogleMap.MAP_TYPE_NORMAL);
     GOOGLE_MAP_OPTIONS.rotateGesturesEnabled(true);
     GOOGLE_MAP_OPTIONS.tiltGesturesEnabled(true);
     GOOGLE_MAP_OPTIONS.zoomControlsEnabled(false);
     GOOGLE_MAP_OPTIONS.zoomGesturesEnabled(true);
   }
 
   //
   private BestLocationProvider bestLocationProvider;
   //
   private GpsStatus.Listener gpsStatusListener;
   //
   private LocationManager locationManager;
   //
   private SupportMapFragment mapFragment;
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.activity_main);
 
     // Create a Google Map Fragment with desired options.
     mapFragment = SupportMapFragment.newInstance(GOOGLE_MAP_OPTIONS);
     FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
     fragmentTransaction.add(R.id.map_fragment_container, mapFragment);
     fragmentTransaction.commit();
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     // Inflate the menu; this adds items to the action bar if it is present.
     getMenuInflater().inflate(R.menu.main, menu);
     return true;
   }
 
   @Override
   public void onGpsDialogCancel(DialogFragment dialog) {
     finish();
   }
 
   @Override
   public void onGpsDialogPositiveClick(DialogFragment dialog) {
     Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
     startActivity(settingsIntent);
   }
 
   @Override
   protected void onStart() {
     super.onStart();
 
     // Get the Location Manager.
     locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
     // Check whether the GPS provider is enabled.
     if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
       DialogFragment dialog = new GpsDialogFragment();
       dialog.show(getSupportFragmentManager(), "GpsDialogFragment");
     }
 
     // Setup the map.
     GoogleMap map = mapFragment.getMap();
     map.setMyLocationEnabled(true);
     map.setLocationSource(bestLocationProvider = new BestLocationProvider());
 
     // Add the GPS status listener.
     locationManager.addGpsStatusListener(gpsStatusListener = new GpsStatusListener());
 
     // TODO sample code to be removed
     SQLiteDatabase db = new InsDbHelper(this).getReadableDatabase();
     Cursor c =
         db.rawQuery(
             "SELECT * FROM Building b INNER JOIN EntryPoint ep ON b.idBuilding = ep.Building_idBuilding",
             null);
    while (c.moveToNext()) {
       double latitude =
           c.getDouble(c.getColumnIndexOrThrow(InsContract.EntryPoint.COLUMN_NAME_LATITUDE));
       double longitude =
           c.getDouble(c.getColumnIndexOrThrow(InsContract.EntryPoint.COLUMN_NAME_LONGITUDE));
       StringBuilder sb = new StringBuilder();
       sb.append("Entry Point at (").append(latitude).append(", ").append(longitude).append(")");
       Log.d("MainActivity", sb.toString());
     }
     // TODO sample code to be removed
   }
 
   @Override
   protected void onStop() {
     super.onStop();
 
     // Remove the GPS status listener.
     locationManager.removeGpsStatusListener(gpsStatusListener);
   }
 }
