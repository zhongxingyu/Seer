 package org.teleportr;
 
 import java.util.Date;
 
 import android.app.Service;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.Toast;
 
 public class ConnectorService extends Service
         implements OnSharedPreferenceChangeListener {
 
     protected static final String TAG = "ConnectorService";
     public static final String RESOLVE = "geocode";
     public static final String SEARCH = "search";
     private Connector fahrgemeinschaft;
     private Connector gplaces;
     private Handler worker;
     private boolean verbose;
     private Uri resolve_jobs_uri;
     private Uri search_jobs_uri;
     private Handler main;
 
     @Override
     public void onCreate() {
         HandlerThread thread = new HandlerThread("worker");
         thread.start();
         worker = new Handler(thread.getLooper());
         try {
             fahrgemeinschaft = (Connector) Class.forName(
                     "de.fahrgemeinschaft.FahrgemeinschaftConnector")
                     .newInstance();
             fahrgemeinschaft.setContext(this);
             gplaces = (Connector) Class.forName(
                     "de.fahrgemeinschaft.GPlaces")
                     .newInstance();
             gplaces.setContext(this);
         } catch (Exception e) {
             e.printStackTrace();
         }
         String uri = "content://" + ConnectorService.this.getPackageName();
         search_jobs_uri = Uri.parse(uri + "/jobs/search");
         resolve_jobs_uri = Uri.parse(uri + "/jobs/resolve");
         SharedPreferences prefs = PreferenceManager
                 .getDefaultSharedPreferences(this);
         verbose = prefs.getBoolean("verbose", false);
         prefs.registerOnSharedPreferenceChangeListener(this);
         main = new Handler();
         super.onCreate();
         long older_than = System.currentTimeMillis() -
                 prefs.getLong("cleanup_interval", 21 * 24 * 3600000); // 3 weeks
         if (prefs.getLong("last_cleanup", 0) < older_than) {
             getContentResolver().delete(Uri.parse(
                     "content://de.fahrgemeinschaft/rides?older_than="
                             + older_than), null, null);
             prefs.edit().putLong("last_cleanup", System.currentTimeMillis());
         }
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         String action = intent.getAction();
         if (action == null)
             return START_STICKY;
         if (action.equals(RESOLVE)) {
             worker.postAtFrontOfQueue(resolve);
         } else if (action.equals(SEARCH)) {
             retry_attempt = 0;
             worker.postAtFrontOfQueue(search);
         }
         return START_REDELIVER_INTENT;
     }
 
     @Override
     public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
         if (key.equals("username") || key.equals("password")) {
             worker.postAtFrontOfQueue(auth);
         } else if (key.equals("verbose")) {
             verbose = prefs.getBoolean("verbose", false);
         }
     }
 
     Runnable auth = new Runnable() {
 
         @Override
         public void run() {
             log("authenticating");
             try {
                 log(fahrgemeinschaft.getAuth());
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     };
 
     Runnable resolve = new Runnable() {
         
         @Override
         public void run() {
             
             Cursor c = getContentResolver()
                     .query(resolve_jobs_uri, null, null, null, null);
             if (c.getCount() != 0) {
                 c.moveToFirst();
                 Place p = new Place((int) c.getLong(0), ConnectorService.this);
                 log("resolving " + p.getName());
                 try {
                     gplaces.resolvePlace(p, ConnectorService.this);
                     log("resolved " + p.getName() + ": " + p.getLat());
                 } catch (Exception e) {
                     log("resolve error: " + e);
                 }
                 c.close();
             } else {
                 log("No places to resolve");
             }
         }
     };
 
     int retry_attempt = 0;
 
     Runnable search = new Runnable() {
 
         Place from;
         Place to;
         Date dep;
 
         @Override
         public void run() {
 
             Cursor jobs = getContentResolver()
                     .query(search_jobs_uri, null, null, null, null);
             if (jobs.getCount() != 0) {
                 log(jobs.getCount() + " jobs to do:");
                 jobs.moveToFirst();
                 from = new Place(jobs.getInt(0), ConnectorService.this);
                 to = new Place(jobs.getInt(1), ConnectorService.this);
                 long latest_dep = jobs.getLong(4);
                 if (latest_dep == 0 // first search - no latest_dep yet
                         || latest_dep >= jobs.getLong(3)) // or refresh
                     dep = new Date(jobs.getLong(2)); // then take search dep
                 else
                     dep = new Date(jobs.getLong(4)); // continue from latest_dep
                 jobs.close();
                 Ride query = new Ride().dep(dep).from(from).to(to);
                 onSearch(query);
                 try {
                     latest_dep = fahrgemeinschaft.search(from, to, dep, null);
                     onSuccess(query, fahrgemeinschaft.getNumberOfRidesFound());
                     fahrgemeinschaft.flush(from.id, to.id, latest_dep);
                     retry_attempt = 0;
                     worker.post(search);
                 } catch (Exception e) {
                     log("search error: " + e);
                     if (retry_attempt < 3) {
                         worker.postDelayed(resolve, retry_attempt * 2000);
                         worker.postDelayed(search, retry_attempt * 2000);
                         retry_attempt++;
                     } else {
                         log("giving up");
                         onFail(query, e.getMessage());
                     }
                 }
             } else {
                 log("no more to search.");
             }
         }
     };
 
     private void log(String msg) {
         Log.d(TAG, msg);
         if (verbose)
             Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
     }
 
     protected void onSearch(final Ride query) {
         main.post(new Runnable() {
             
             @Override
             public void run() {
                 if (gui != null) {
                     gui.onBackgroundSearch(query);
                } else {
                    Log.d(TAG, "Try again");
                    main.post(this);
                 }
             }
         });
     }
 
     protected void onSuccess(final Ride query, final int numberOfRidesFound) {
         main.post(new Runnable() {
             
             @Override
             public void run() {
                 if (gui != null) {
                     gui.onBackgroundSuccess(query, numberOfRidesFound);
                 }
             }
         });
     }
 
     protected void onFail(final Ride query, final String reason) {
         main.post(new Runnable() {
             
             @Override
             public void run() {
                 if (gui != null) {
                     gui.onBackgroundFail(query, reason);
                 }
             }
         });
     }
 
 
     public void register(BackgroundListener activity) {
         gui = activity;
     }
 
     private BackgroundListener gui;
 
     public interface BackgroundListener {
         public void onBackgroundSearch(Ride query);
         public void onBackgroundSuccess(Ride query, int numberOfRidesFound);
         public void onBackgroundFail(Ride query, String reason);
     }
 
 
 
     @Override
     public IBinder onBind(Intent intent) {
         return new Bind();
     }
     public class Bind extends Binder {
         public ConnectorService getService() {
             return ConnectorService.this;
         }
     }
 
     @Override
     public void unbindService(ServiceConnection conn) {
         super.unbindService(conn);
         gui = null;
     }
 }
