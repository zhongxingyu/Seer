 package org.teleportr;
 
 import java.util.ArrayList;
 
 import android.content.ContentProvider;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.net.Uri;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class RidesProvider extends ContentProvider {
 
     static final String TAG = "RideProvider";
     private DataBaseHelper db;
     private UriMatcher route;
     private Uri myrides;
     private Uri jobs;
 
     @Override
     public boolean onCreate() {
         db = new DataBaseHelper(getContext());
         try {
             setAuthority(getContext().getPackageName());
         } catch (UnsupportedOperationException e) {
         }
         return false;
     }
 
     public void setAuthority(String authority) {
         route = new UriMatcher(0);
         route.addURI(authority, "rides/#/rides", SUBRIDES);
         route.addURI(authority, "jobs/resolve", RESOLVE);
         route.addURI(authority, "jobs/publish", PUBLISH);
         route.addURI(authority, "jobs/search", SEARCH);
         route.addURI(authority, "history", HISTORY);
         route.addURI(authority, "myrides", MYRIDES);
         route.addURI(authority, "places/#", PLACE);
         route.addURI(authority, "places", PLACES);
         route.addURI(authority, "rides/#", RIDE);
         route.addURI(authority, "rides", RIDES);
         jobs = Uri.parse("content://" + authority + "/jobs/search");
         myrides = Uri.parse("content://" + authority + "/myrides");
     }
 
     @Override
     public String getType(Uri uri) {
         return null;
     }
 
     @Override
     public Uri insert(Uri uri, ContentValues values) {
         long id = 0;
         try {
             db.getWritableDatabase().beginTransaction();
             switch (route.match(uri)) {
             case PLACES:
                 id = db.insertPlace(values);
                 break;
             case RIDES:
                 id = db.insertRide(values.getAsInteger("parent_id"),
                         values.getAsInteger("from_id"),
                         values.getAsInteger("to_id"), values);
                 break;
             case SEARCH:
                 id = db.getWritableDatabase().replace("jobs", null, values);
                 break;
             }
             db.getWritableDatabase().setTransactionSuccessful();
             getContext().getContentResolver().notifyChange(myrides, null);
         } catch (Exception e) {
             Log.e(TAG, "error during insert: " + e);
             e.printStackTrace();
         } finally {
             db.getWritableDatabase().endTransaction();
         }
         return ContentUris.withAppendedId(uri, id);
     }
 
     @Override
     public int bulkInsert(Uri uri, ContentValues[] values) {
         try {
             db.getWritableDatabase().beginTransaction();
             switch (route.match(uri)) {
             case PLACES:
                 for (int i = 0; i < values.length; i++) {
                     db.insertPlace(values[i]);
                 }
                 break;
             case RIDES:
                 int from;
                 int to;
                 int s_from = Integer.parseInt(uri.getQueryParameter("from_id"));
                 int s_to = Integer.parseInt(uri.getQueryParameter("to_id"));
                 ArrayList<Integer> placeIdx = new ArrayList<Integer>();
                 long parent = 0;
                 for (int i = 0; i < values.length; i++) {
                     if (parent == 0 && !values[i].containsKey("ref")) {
                         placeIdx.add(db.insertPlace(values[i]));
                     } else {
                         from = placeIdx.get(values[i].getAsInteger("from_id"));
                         to = placeIdx.get(values[i].getAsInteger("to_id"));
                         if (values[i].containsKey("ref")) {
                             parent = db.insertRide(0, from, to, values[i]);
                             db.insertMatch(from, to, s_from, s_to);
                         } else {
                             db.insertRide(parent, from, to, values[i]);
                         }
                     }
                 }
             }
             db.getWritableDatabase().setTransactionSuccessful();
         } catch (Exception e) {
             Log.e(TAG, "error during bulk insert: " + e);
             e.printStackTrace();
         } finally {
             db.getWritableDatabase().endTransaction();
             getContext().getContentResolver().notifyChange(uri, null);
         }
         return 1;
     }
 
     @Override
     public Cursor query(Uri uri, String[] p, String s, String[] a, String o) {
         switch (route.match(uri)) {
         case PLACE:
             String key = uri.getQueryParameter("key");
             if (key != null) {
                 return db.getReadableDatabase().query("place_keys", null,
                         "place_id=" + uri.getLastPathSegment() +
                         " AND key='" + key + "'", null, null, null, null);
             } else {
                 return db.getReadableDatabase().query("places", null,
                         "_id=" + uri.getLastPathSegment(),
                         null, null, null, null);
             }
         case PLACES:
             String from_id = uri.getQueryParameter("from_id");
             String q = uri.getQueryParameter("q");
             if (q == null) q = "%";
             else q = q + "%";
             if (from_id == null) {
                 return db.autocompleteFrom(q);
             } else {
                 return db.autocompleteTo(from_id, q);
             }
         case RIDES:
             Cursor rslt = db.queryRides(
                     uri.getQueryParameter("from_id"),
                     uri.getQueryParameter("to_id"),
                     uri.getQueryParameter("dep"));
             rslt.setNotificationUri(getContext().getContentResolver(), uri);
             return rslt;
         case SUBRIDES:
             return db.querySubRides(uri.getPathSegments().get(1));
         case RIDE:
             return db.queryRide(uri.getLastPathSegment());
         case HISTORY:
             return db.getReadableDatabase().query("rides", null,
                     "type=" + Ride.SEARCH, null, null, null, "_id DESC");
         case MYRIDES:
             rslt = db.queryMyRides();
             rslt.setNotificationUri(getContext().getContentResolver(), myrides);
             return rslt;
         case SEARCH:
             long olderThan;
             try {
                olderThan = PreferenceManager.getDefaultSharedPreferences(
                        getContext()).getLong("refresh", 0);
             } catch (Exception e) {
                 olderThan = 10 * 60 * 1000; // 10min
             }
             return db.queryJobs(System.currentTimeMillis() - olderThan);
         case PUBLISH:
             return db.queryPublishJobs();
         case RESOLVE:
             return db.getReadableDatabase().query("places", null,
                     "geohash IS NULL", null, null, null, null);
         }
         return null;
     }
 
     @Override
     public int update(Uri uri, ContentValues values, String sel, String[] args) {
         getContext().getContentResolver().notifyChange(uri, null);
         int id = -1;
         switch (route.match(uri)) {
         case PLACE:
             id =  db.getWritableDatabase().update("places", values,
                     "_id=?", new String[] { uri.getLastPathSegment() });
             break;
         case RIDE:
             id =  db.getWritableDatabase().update("rides", values,
                     "_id=?", new String[] { uri.getLastPathSegment() });
             getContext().getContentResolver().notifyChange(jobs, null);
             getContext().getContentResolver().notifyChange(myrides, null);
         }
         return id;
     }
 
     @Override
     public int delete(Uri uri, String where, String[] args) {
         getContext().getContentResolver().notifyChange(uri, null);
         switch (route.match(uri)) {
         case RIDE:
             return db.getWritableDatabase().delete("rides",
                     "parent_id=?", new String[] { uri.getLastPathSegment() });
         case RIDES:
             db.getWritableDatabase().delete("jobs", null, null);
             String param = uri.getQueryParameter("older_than");
             Log.d(TAG, "clear ride cache older than " + param);
             if (param != null) {
                 return db.getWritableDatabase().delete("rides",
                         OFFERS + " AND dep<?", new String[] { param });
             } else {
                 db.getWritableDatabase().delete("route_matches", null, null);
                 return db.getWritableDatabase().delete("rides", OFFERS, null);
             }
         }
         return -1;
     }
     private static final String OFFERS = "type=" + Ride.OFFER;
 
     private static final int RIDE = 0;
     private static final int RIDES = 1;
     private static final int PLACE = 2;
     private static final int PLACES = 3;
     private static final int SEARCH = 4;
     private static final int RESOLVE = 5;
     private static final int PUBLISH = 6;
     private static final int MYRIDES = 7;
     private static final int HISTORY = 8;
     private static final int SUBRIDES = 9;
 }
