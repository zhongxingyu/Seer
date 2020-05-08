 package shi.ning.locrem;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import shi.ning.locrem.ProximityManagerService.Stub;
 import shi.ning.locrem.ReminderEntry.Columns;
 import android.app.AlarmManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.database.Cursor;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.preference.PreferenceManager;
 import android.text.format.Time;
 import android.util.Log;
 
 public final class ProximityManager extends Service {
     static final String TAG = "ProximityManager";
 
     private static final int SERVICE_ALARM = 0;
 
     private static final int MIN_TIME = 300000; // 5 minutes
     private static final int MIN_DISTANCE = 200; // 200 meters
 
     private static final String PRIMARY_PROVIDER =
         LocationManager.NETWORK_PROVIDER;
     private static final String SECONDARY_PROVIDER =
         LocationManager.GPS_PROVIDER;
 
     private final Stub mBinder = new Stub() {
         @Override
         public void onEntryChanged(long id) throws RemoteException {
             if (Log.isLoggable(TAG, Log.VERBOSE))
                 Log.v(TAG, "remote access, entry " + id + " changed");
 
             ProximityManager.this.onEntryChanged(id);
         }
     };
 
     String mProvider;
     private Context mContext;
     private Geocoder mGeocoder;
     private LocationManager mManager;
     private ProximityListener mListener;
     private ProximityListener mSecondaryListener;
     int mRange;
 
     private final class ProximityListener implements LocationListener {
         @Override
         public void onStatusChanged(String provider, int status,
                                     Bundle extras) {
             if (Log.isLoggable(TAG, Log.DEBUG))
                 Log.w(TAG, "provider " + provider + " status changed to "
                       + status);
 
             if (status == LocationProvider.AVAILABLE)
                 onProviderEnabled(provider);
             else if (status == LocationProvider.OUT_OF_SERVICE)
                 onProviderDisabled(provider);
         }
 
         @Override
         public void onProviderEnabled(String provider) {
             if (Log.isLoggable(TAG, Log.DEBUG))
                 Log.d(TAG, "provider " + provider + " enabled");
             if (provider.equals(PRIMARY_PROVIDER)
                 && !mProvider.equals(provider)) {
                 unregister(mProvider);
                 mProvider = provider;
             }
         }
 
         @Override
         public void onProviderDisabled(String provider) {
             if (Log.isLoggable(TAG, Log.DEBUG))
                 Log.d(TAG, provider + " disabled");
             if (provider.equals(PRIMARY_PROVIDER)
                 && !mProvider.equals(SECONDARY_PROVIDER)) {
                 mProvider = SECONDARY_PROVIDER;
                 register(mProvider);
             }
         }
 
         @Override
         public void onLocationChanged(Location location) {
             final Address currentAddress = locationToAddress(location);
             final Time now = new Time();
             now.setToNow();
 
             checkAllEntry(now, currentAddress);
         }
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
         mContext = getApplicationContext();
         mManager =
             (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
         mGeocoder = new Geocoder(mContext);
         mListener = new ProximityListener();
         mSecondaryListener = new ProximityListener();
 
         final SharedPreferences settings =
             PreferenceManager.getDefaultSharedPreferences(mContext);
         settings.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
             @Override
             public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                                   String key) {
                 mRange = settings.getInt(key, Settings.DEFAULT_RANGE);
                 if (Log.isLoggable(TAG, Log.DEBUG))
                     Log.d(TAG, key + " value changed to " + mRange);
             }
         });
         mRange = settings.getInt(Settings.KEY_RANGE, Settings.DEFAULT_RANGE);
         if (Log.isLoggable(TAG, Log.VERBOSE))
             Log.v(TAG, "range settings is " + mRange);
 
         mProvider = PRIMARY_PROVIDER;
         register(mProvider);
         if (Log.isLoggable(TAG, Log.VERBOSE))
             Log.v(TAG, "created");
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return mBinder;
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         if (Log.isLoggable(TAG, Log.DEBUG))
             Log.d(TAG, "starting service with flags " + flags
                   + " and id " + startId);
 
         if (intent != null) {
             final long id = intent.getLongExtra(Columns._ID, -1);
             if (id >= 0) {
                 if (Log.isLoggable(TAG, Log.VERBOSE))
                     Log.v(TAG, "previously scheduled entry " + id
                           + " is woken");
 
                 onEntryChanged(id);
             }
         }
 
         return START_STICKY;
     }
 
     public Address locationToAddress(Location location) {
         try {
             if (location == null) {
                 if (Log.isLoggable(TAG, Log.VERBOSE))
                     Log.v(TAG, "cannot reverse geocode null location");
                 return null;
             }
             final List<Address> addresses =
                 mGeocoder.getFromLocation(location.getLatitude(),
                                           location.getLongitude(),
                                           1);
             if (addresses != null && addresses.size() == 1)
                 return addresses.get(0);
             if (Log.isLoggable(TAG, Log.DEBUG))
                 Log.d(TAG, "failed reverse geocoding " + location.toString());
             return null;
         } catch (IOException e) {
             if (Log.isLoggable(TAG, Log.DEBUG))
                 Log.d(TAG, "error reverse geocoding " + location.toString());
             return null;
         }
     }
 
     void unregister(final String provider) {
         if (provider.equals(PRIMARY_PROVIDER))
             mManager.removeUpdates(mListener);
         else
             mManager.removeUpdates(mSecondaryListener);
 
         if (Log.isLoggable(TAG, Log.VERBOSE))
             Log.v(TAG, provider + " unregistered");
     }
 
     void register(final String provider) {
         /*
          * TODO Should ask user if they want to enable location service
          * if no provider is available.
          */
         if (provider != null) {
             if (provider.equals(PRIMARY_PROVIDER))
                 mManager.requestLocationUpdates(provider,
                                                 MIN_TIME,
                                                 MIN_DISTANCE,
                                                 mListener);
             else
                 mManager.requestLocationUpdates(provider,
                                                 MIN_TIME,
                                                 MIN_DISTANCE,
                                                 mSecondaryListener);
             if (Log.isLoggable(TAG, Log.VERBOSE))
                 Log.v(TAG, "registered to location provider " + provider);
         } else {
             if (Log.isLoggable(TAG, Log.DEBUG))
                 Log.d(TAG, "no location provider available");
         }
     }
 
     void onEntryChanged(long id) {
         final Uri uri =
             ContentUris.withAppendedId(ReminderProvider.CONTENT_URI, id);
         final Cursor cursor =
             getContentResolver().query(uri, null, null, null, null);
 
         if (cursor.moveToFirst()) {
             final ReminderEntry entry = ReminderProvider.cursorToEntry(cursor);
             cursor.close();
             final Time now = new Time();
             now.setToNow();
 
             if (!entry.enabled)
                 return;
 
             if (mProvider == null) {
                 if (Log.isLoggable(TAG, Log.DEBUG))
                     Log.d(TAG, "no location provider is available");
                 return;
             }
 
             final Address current =
                 locationToAddress(mManager.getLastKnownLocation(mProvider));
             checkEntry(entry, now, current);
        } else {
            cursor.close();
         }
     }
 
     private void checkAllEntry(Time now, Address current) {
         final Cursor cursor =
             getContentResolver().query(ReminderProvider.ENABLED_URI,
                                        null, null, null, null);
         final LinkedList<ReminderEntry> entries =
             ReminderProvider.cursorToEntries(cursor);
         cursor.close();
 
         if (current == null || entries == null)
             return;
 
         final int length = entries.size();
         for (int i = 0; i < length; i++) {
             checkEntry(entries.get(i), now, current);
         }
     }
 
     private void checkEntry(ReminderEntry entry, Time now, Address current) {
         if (entry == null)
             return;
 
         if (entry.time.after(now)) {
             if (Log.isLoggable(TAG, Log.VERBOSE))
                 Log.v(TAG, "entry " + entry.id + " is scheduled to run after "
                       + entry.time.format("%F %T"));
 
             final Intent i = new Intent(this, ProximityManager.class);
             i.putExtra(Columns._ID, entry.id);
             final PendingIntent pi =
                 PendingIntent.getService(mContext, SERVICE_ALARM, i,
                                          PendingIntent.FLAG_ONE_SHOT);
             final AlarmManager alarmManager =
                 (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
             alarmManager.set(AlarmManager.RTC_WAKEUP,
                              entry.time.toMillis(false),
                              pi);
 
             return;
         }
 
         if (current == null)
             return;
 
         final List<Address> addresses = entry.addresses;
         final int length = addresses.size();
         for (int i = 0; i < length; i++) {
             final Address a = addresses.get(i);
             if (inRange(current, a)) {
                 // Alert the user
                 notifyUser(entry.note);
                 if (Log.isLoggable(TAG, Log.DEBUG))
                     Log.d(TAG, "close to " + a.toString());
 
                 // disable it
                 entry.enabled = false;
                 final Uri uri =
                     ContentUris.withAppendedId(ReminderProvider.CONTENT_URI,
                                                entry.id);
                 final ContentValues values =
                     ReminderProvider.packEntryToValues(entry);
                 getContentResolver().update(uri, values, null, null);
                 if (Log.isLoggable(TAG, Log.VERBOSE))
                     Log.v(TAG, "entry " + entry.id + " is disabled");
 
                 break;
             }
         }
     }
 
     private boolean inRange(Address current, Address test) {
         /*
          * Test order:
          *   admin area
          *   sub admin area
          *   locality
          *   longitude/latitude
          */
         final String curAdmin = current.getAdminArea();
         final String curSubAdmin = current.getSubAdminArea();
         final String curLocality = current.getLocality();
         final String curThoroughfare = current.getThoroughfare();
 
         final String admin = test.getAdminArea();
         final String subAdmin = test.getSubAdminArea();
         final String locality = test.getLocality();
         final String thoroughfare = test.getThoroughfare();
 
         if (test.hasLatitude() && test.hasLongitude()
             && current.hasLatitude() && current.hasLongitude()) {
                 float[] distance = new float[1];
                 Location.distanceBetween(current.getLatitude(),
                                          current.getLongitude(),
                                          test.getLatitude(),
                                          test.getLongitude(),
                                          distance);
 
                 if (distance[0] <= mRange) {
                     if (Log.isLoggable(TAG, Log.VERBOSE))
                         Log.v(TAG, "coordinates close to " + test.toString());
                     return true;
                 }
         }
         if (admin == null || curAdmin == null
             || !admin.equals(curAdmin)) {
             if (Log.isLoggable(TAG, Log.VERBOSE))
                 Log.v(TAG, "admin: " + admin + " != " + curAdmin);
             return false;
         }
         if (subAdmin != null && curSubAdmin != null
             && !subAdmin.equals(curSubAdmin)) {
             if (Log.isLoggable(TAG, Log.VERBOSE))
                 Log.v(TAG, "subAdmin: " + subAdmin + " != " + curSubAdmin);
             return false;
         }
         if (locality != null && curLocality != null
             && !locality.equals(curLocality)) {
             if (Log.isLoggable(TAG, Log.VERBOSE))
                 Log.v(TAG, "locality: " + locality + " != " + curLocality);
             return false;
         }
         if (thoroughfare != null && curThoroughfare != null
             && !thoroughfare.equals(curThoroughfare)) {
             if (Log.isLoggable(TAG, Log.VERBOSE))
                 Log.v(TAG, "thoroughfare: " + thoroughfare + " != "
                       + curThoroughfare);
             return false;
         }
 
         if (Log.isLoggable(TAG, Log.VERBOSE))
             Log.v(TAG, "address close to " + test.toString());
         return true;
     }
 
     private void notifyUser(String message) {
         final String ns = Context.NOTIFICATION_SERVICE;
         final NotificationManager mNotificationManager =
             (NotificationManager) getSystemService(ns);
 
         final Context context = getApplicationContext();
         final CharSequence contentTitle = "Location Alert";
         final Intent notificationIntent = new Intent(this, ReminderList.class);
         final PendingIntent contentIntent =
             PendingIntent.getActivity(this, 0, notificationIntent, 0);
 
         final int icon = android.R.drawable.alert_dark_frame;
         final long when = System.currentTimeMillis();
         final Notification notification = new Notification(icon, message, when);
         notification.defaults |= Notification.DEFAULT_ALL;
         notification.flags |= Notification.FLAG_AUTO_CANCEL;
         notification.setLatestEventInfo(context, contentTitle,
                                         message, contentIntent);
         mNotificationManager.notify(1, notification);
     }
 }
