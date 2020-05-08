 /**
  * Fahrgemeinschaft / Ridesharing App
  * Copyright (c) 2013 by it's authors.
  * Some rights reserved. See LICENSE..
  *
  */
 
 package org.teleportr;
 
 import java.util.Map.Entry;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 import android.util.Log;
 
 class DataBaseHelper extends SQLiteOpenHelper {
 
     private static final int VERSION = 19;
     private static final String TAG = "DB";
     private static final String EMPTY = "";
     private static final String JOBS = "jobs";
     private static final String RIDES = "rides";
 //    private static final String ARROW = " -> ";
 //    private static final String STORED_KEY = "+ stored key ";
 //    private static final String STORED_RIDE = "+ stored ride ";
 //    private static final String STORED_PLACE = "+ stored place ";
 //    private static final String RESOLVE_PLACE_BY_NAME = "* resolve place by name ";
 //    private static final String RESOLVE_PLACE_BY_ADDRESS = "* resolve place by address ";
 //    private static final String RESOLVE_PLACE_BY_GEOHASH = "* resolve place by geohash ";
     private static final String INVALIDATE_CACHE = "invalidate cache";
     private static final String LAST_REFRESH = "last_refresh";
     private static final String DB_FILENAME = "teleportr.db";
     private SQLiteStatement insertPlace;
     private SQLiteStatement insertPlaceKey;
     private SQLiteStatement insertRide;
     private SQLiteStatement getIdByGeohash;
     private SQLiteStatement getIdByName;
     private SQLiteStatement insertMatch;
     private SQLiteStatement getIdByAddress;
     private SQLiteStatement getLatestRef;
     private SQLiteStatement isDeleted;
 
     @Override
     public void onCreate(SQLiteDatabase db) {
         db.execSQL("create table places ("
                 + "'_id' integer primary key autoincrement,"
                 + " 'geohash' text unique,"
                 + " 'name' text unique,"
                 + " 'address' text unique);");
         db.execSQL("create table place_keys ("
                 + "'_id' integer primary key autoincrement,"
                 + " 'place_id' integer, 'key' text, 'value' text);");
         db.execSQL("CREATE UNIQUE INDEX place_keys_idx"
                 + " ON place_keys ('place_id', 'key');");
         db.execSQL("create table rides ("
                 + "'_id' integer primary key autoincrement, 'type' integer,"
                 + " 'from_id' integer, 'to_id' integer,"
                 + " 'dep' integer, 'arr' integer,"
                 + " distance integer, price integer, seats integer,"
                 + " mode text, operator text, who text, details text,"
                 + " marked integer, dirty integer, active integer,"
                 + " parent_id integer, ref text, refresh integer);");
         db.execSQL("CREATE UNIQUE INDEX rides_idx ON rides"
                 + " ('type', 'ref', 'dep', from_id, to_id, seats, parent_id);");
         db.execSQL("create table jobs ("
                 + "'_id' integer primary key autoincrement,"
                 + " from_id integer, to_id integer,"
                 + " dep integer, arr integer, last_refresh integer);");
         db.execSQL("CREATE UNIQUE INDEX jobs_idx"
                 + " ON jobs ('from_id', 'to_id', dep);");
         db.execSQL("create table route_matches ("
                 + "'_id' integer primary key autoincrement,"
                 + " from_id integer, to_id integer,"
                 + " sub_from_id integer, sub_to_id integer"
                 + ");");
         db.execSQL("CREATE UNIQUE INDEX matches_idx ON route_matches"
                 + " ('from_id', 'to_id', 'sub_from_id', 'sub_to_id' );");
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersn, int newVersn) {
         Log.d(TAG, "upgrade db schema");
         db.execSQL("DROP table places;");
         db.execSQL("DROP table place_keys;");
         db.execSQL("DROP table rides;");
         db.execSQL("DROP table jobs;");
         db.execSQL("DROP table route_matches;");
         onCreate(db);
     }
 
     public DataBaseHelper(Context context) {
         super(context, DB_FILENAME, null, VERSION);
         insertPlace = getWritableDatabase().compileStatement(INSERT_PLACE);
         insertPlaceKey = getWritableDatabase().compileStatement(INSERT_KEY);
         insertRide = getWritableDatabase().compileStatement(INSERT_RIDE);
         insertMatch = getWritableDatabase().compileStatement(INSERT_MATCH);
         getIdByGeohash = getReadableDatabase().compileStatement(BY_GEOH);
         getIdByAddress = getReadableDatabase().compileStatement(BY_ADDR);
         getIdByName = getReadableDatabase().compileStatement(BY_NAME);
     }
 
     static final String INSERT_PLACE = "INSERT OR IGNORE INTO places"
             + " ('geohash', 'name', 'address')"
             + " VALUES (?, ?, ?);";
 
     static final String INSERT_KEY = "INSERT OR IGNORE INTO place_keys"
             + " ('place_id', 'key', 'value')" + " VALUES (?, ?, ?);";
     
     static final String BY_GEOH = "SELECT sum(_id) FROM places WHERE geohash=?";
     static final String BY_ADDR = "SELECT sum(_id) FROM places WHERE address=?";
     static final String BY_NAME = "SELECT sum(_id) FROM places WHERE name=?";
 
     public int insertPlace(ContentValues cv) {
         long place_id = 0;
         String key = cv.getAsString(Place.GEOHASH);
         if (key != null) {
             getIdByGeohash.bindString(1, key);
             place_id = getIdByGeohash.simpleQueryForLong();
             if (place_id == 0)
                 insertPlace.bindString(1, key);
             cv.remove(Place.GEOHASH);
 //            Log.d(TAG, RESOLVE_PLACE_BY_GEOHASH + key + ARROW + place_id);
         } else insertPlace.bindNull(1);
         key = cv.getAsString(Place.ADDRESS);
         if (key != null) {
             if (place_id == 0) {
                 getIdByAddress.bindString(1, key);
                 place_id = getIdByAddress.simpleQueryForLong();
                 if (place_id == 0)
                     insertPlace.bindString(3, key);
             }
             cv.remove(Place.ADDRESS);
 //            Log.d(TAG, RESOLVE_PLACE_BY_ADDRESS + key + ARROW + place_id);
         } else if (place_id == 0) insertPlace.bindNull(3);
         key = cv.getAsString(Place.NAME);
         if (key != null) {
             if (place_id == 0) {
                 getIdByName.bindString(1, key);
                 place_id = getIdByName.simpleQueryForLong();
                 if (place_id == 0)
                     insertPlace.bindString(2, key);
             }
             cv.remove(Place.NAME);
 //            Log.d(TAG, RESOLVE_PLACE_BY_NAME + key + ARROW + place_id);
         } else if (place_id == 0) insertPlace.bindNull(2);
         if (place_id == 0)
             place_id = (int) insertPlace.executeInsert();
 //            Log.d(TAG, STORED_PLACE + place_id);
         if (place_id == -1) { // insert has been ignored / already exists
             Log.d(TAG, "how could this possibly ever happen???");
         }
         if (cv.size() > 0) { // insert keys
             insertPlaceKey.bindLong(1, place_id);
             for (Entry<String, Object> entry : cv.valueSet()) {
                 insertPlaceKey.bindString(2, entry.getKey());
                 insertPlaceKey.bindString(3, (String) entry.getValue());
                 insertPlaceKey.executeInsert();
 //                Log.d(RidesProvider.TAG, STORED_KEY + entry);
             }
         }
         return (int) place_id;
     }
 
     static final String GET_REF = "SELECT ref from rides WHERE _id IS ?";
 
     public String getLatestRef(int id) {
         if (getLatestRef == null)
             getLatestRef = getReadableDatabase().compileStatement(GET_REF);
         getLatestRef.bindLong(1, id);
         return getLatestRef.simpleQueryForString();
     }
 
     static final String INSERT_RIDE = "INSERT OR REPLACE INTO rides "
             + "('type', from_id, to_id, dep, arr, mode, operator, who, details,"
             + " price, seats, marked, dirty, active, parent_id, ref, refresh)"
             + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
 
     public int insertRide(int parent, int from, int to, ContentValues cv) {
 
 //        if (from == to) {
 //            Log.d(RidesProvider.TAG, "- NOT store from=" + from + " to=" + to);
 //            return -1;
 //        }
         insertRide.bindLong(17, System.currentTimeMillis());
         insertRide.bindLong(15, parent);
         insertRide.bindLong(2, from);
         insertRide.bindLong(3, to);
         bind(cv, 12, Ride.MARKED, 0);
         bind(cv, 13, Ride.DIRTY, 0);
         bind(cv, 14, Ride.ACTIVE, 0);
         bind(cv, 16, Ride.REF, EMPTY);
 
         if (parent == 0) {
             bind(cv, 1, Ride.TYPE, 0);
             bind(cv, 4, Ride.DEP, 0);
             bind(cv, 5, Ride.ARR, 0);
             bind(cv, 6, Ride.MODE, EMPTY);
             bind(cv, 8, Ride.WHO, EMPTY);
             bind(cv, 7, Ride.OPERATOR, EMPTY);
             bind(cv, 9, Ride.DETAILS, EMPTY);
             bind(cv, 10, Ride.PRICE, 0);
             bind(cv, 11, Ride.SEATS, 0);
         }
         long id = insertRide.executeInsert();
 //        Log.d(TAG, STORED_RIDE + id);
         return (int) id;
     }
 
     private void bind(ContentValues cv, int index, String key, String defVal) {
         if (cv.containsKey(key))
             insertRide.bindString(index, cv.getAsString(key));
         else insertRide.bindString(index, defVal);
     }
 
     private void bind(ContentValues cv, int index, String key, long defVal) {
         if (cv.containsKey(key))
             insertRide.bindLong(index, cv.getAsLong(key));
         else insertRide.bindLong(index, defVal);
     }
 
     static final String INSERT_MATCH = "INSERT OR IGNORE INTO route_matches"
             + " ('from_id', 'to_id', sub_from_id, 'sub_to_id')"
             + " VALUES (?, ?, ?, ?);";
     
     public void insertMatch(int from, int to, int sub_from, int sub_to) {
         insertMatch.bindLong(1, from);
         insertMatch.bindLong(2, to);
         insertMatch.bindLong(3, sub_from);
         insertMatch.bindLong(4, sub_to);
         insertMatch.executeInsert();
     }
 
 
 
     static final String SELECT_FROM = "SELECT * FROM 'places'"
             + " LEFT JOIN ("
                 + "SELECT count(_id) AS count, from_id FROM 'rides'"
                 + " WHERE type=" + Ride.SEARCH
                 + " GROUP BY from_id"
                 + ") AS from_history ON _id=from_history.from_id"
             + " LEFT JOIN ("
                 + "SELECT count(_id) AS count, to_id FROM 'rides'"
                 + " WHERE type=" + Ride.SEARCH
                 + " GROUP BY to_id"
                 + ") AS to_history ON _id=to_history.to_id"
             + " WHERE (from_history.count > 0 OR to_history.count > 0)"
                 + " AND (name LIKE ? OR address LIKE ?)"
             + " ORDER BY from_history.count DESC, to_history.count DESC, name ASC;";
 
     public Cursor autocompleteFrom(String q) {
         return getReadableDatabase().rawQuery(SELECT_FROM, new String[] {q, q});
     }
 
     static final String SELECT_TO = "SELECT * FROM 'places'"
             + " LEFT JOIN ("
                 + "SELECT count(_id) AS count, from_id FROM 'rides'"
                 + " WHERE type=" + Ride.SEARCH
                 + " GROUP BY from_id"
                 + ") AS from_history ON _id=from_history.from_id"
             + " LEFT JOIN ("
                 + "SELECT count(_id) AS count, to_id FROM 'rides'"
                 + " WHERE type=" + Ride.SEARCH
                 + " GROUP BY to_id"
             + ") AS to_history ON _id=to_history.to_id"
             + " WHERE (from_history.count > 0 OR to_history.count > 0)"
                     + " AND (name LIKE ? OR address LIKE ?)"
             + " ORDER BY to_history.count DESC, from_history.count DESC, name ASC;";
 
     static final String SELECT_TO_FOR_FROM = "SELECT * FROM 'places'"
             + " LEFT JOIN ("
                 + "SELECT count(_id) AS cnt, to_id FROM 'rides'"
                 + " WHERE type=" + Ride.SEARCH + " AND from_id=?"
                 + " GROUP BY to_id"
                 + ") AS to_h_from ON _id=to_h_from.to_id"
             + " LEFT JOIN ("
                 + "SELECT count(_id) AS cnt, to_id FROM 'rides'"
                 + " WHERE type=" + Ride.SEARCH
                 + " GROUP BY to_id"
                 + ") AS to_h ON _id=to_h.to_id"
             + " LEFT JOIN ("
                 + "SELECT count(_id) AS cnt, from_id FROM 'rides'"
                 + " WHERE type=" + Ride.SEARCH
                 + " GROUP BY from_id"
                 + ") AS from_h ON _id=from_h.from_id"
             + " WHERE (to_h_from.cnt > 0 OR to_h.cnt > 0 OR from_h.cnt > 0)"
                     + " AND _id<>? AND (name LIKE ? OR address LIKE ?)"
             + " ORDER BY to_h_from.cnt DESC, to_h.cnt DESC, from_h.cnt DESC,"
                     + " name ASC;";
 
 
     static final String SELECT_TO_FOR_ = "SELECT * FROM 'places'"
             + " LEFT JOIN ("
             + "SELECT count(_id) AS count, to_id FROM 'rides'"
             + " WHERE from_id=?" + " GROUP BY to_id"
             + ") AS history ON _id=history.to_id"
             + " WHERE _id<>? AND (name LIKE ? OR address LIKE ?)"
             + " ORDER BY history.count DESC, name ASC;";
 
     public Cursor autocompleteTo(String from, String q) {
         if (from.equals(ZERO)) {
             return getReadableDatabase().rawQuery(SELECT_TO,
                     new String[] { q, q });
         } else {
             return getReadableDatabase().rawQuery(SELECT_TO_FOR_FROM,
                     new String[] { from, from, q, q });
         }
     }
     private static final String ZERO = "0";
 
     static final String SELECT_JOBS = "SELECT"
                 + " rides.from_id, rides.to_id,"
                 + " rides.dep, CASE"
                     + " WHEN jobs.dep IS NULL"
                         + " OR jobs.arr >= rides.arr"
                     + " THEN rides.dep"
                     + " ELSE jobs.arr"
                 + " END, jobs.last_refresh"
             + " FROM (SELECT * from rides"
                 + " WHERE rides.type=" + Ride.SEARCH
                 + " ORDER BY _id DESC LIMIT 1) AS rides"
             + " LEFT JOIN jobs ON"
                 + " rides.from_id IS jobs.from_id"
                 + " AND rides.to_id IS jobs.to_id"
                 + " AND rides.dep IS jobs.dep"
             + " WHERE jobs.dep IS NULL" // never searched before
             + " OR ((jobs.arr < rides.arr OR jobs.last_refresh < ?)"
                 /*+ " AND (SELECT arr FROM jobs AS overlap"
                     + " WHERE from_id IS rides.from_id"
                     + " AND to_id IS rides.to_id"
                     + " AND dep IS NOT rides.dep"
                     + " AND dep <= jobs.arr"
                     + " AND rides.arr < arr"
                 + ") IS NULL" */
             + ")";
 
     public Cursor queryJobs(long older_than_last_refresh) {
         return getReadableDatabase().rawQuery(SELECT_JOBS,
                 new String[] { String.valueOf(older_than_last_refresh) });
     }
 
     public Cursor queryPublishJobs() {
         return getReadableDatabase().rawQuery(
                 SELECT_RIDES + WHERE_DIRTY, null);
     }
     private static final String WHERE_DIRTY =
             " WHERE dirty > 0 AND parent_id = 0";
 
     static final String SELECT_RIDES_COLUMNS = "SELECT rides._id, rides.type,"
                 + " \"from\"._id, \"from\".name, \"from\".address,"
                 + " \"to\"._id, \"to\".name, \"to\".address,"
                 + " rides.dep, rides.arr,"
                 + " rides.mode, rides.operator, rides.who, rides.details,"
                 + " rides.distance, rides.price, rides.seats, rides.marked,"
                 + " rides.dirty, rides.active, rides.parent_id, rides.ref";
 
     static final String JOIN = " FROM 'rides'"
             + " JOIN 'places' AS \"from\" ON rides.from_id=\"from\"._id"
             + " JOIN 'places' AS \"to\" ON rides.to_id=\"to\"._id";
 
     static final String SELECT_RIDES = SELECT_RIDES_COLUMNS + JOIN;
 
     public Cursor queryRide(String id) {
         return getReadableDatabase().rawQuery(SELECT_RIDES
                 + WHERE_RIDES_ID_IS, new String[] { id });
     }
 
     private static final String WHERE_RIDES_ID_IS = " WHERE rides._id=?";
 
     static final String SELECT_RIDE_MATCHES =
                 SELECT_RIDES_COLUMNS + ", max(rides._id)" + JOIN
             + " LEFT JOIN 'route_matches' AS match ON "
                 + " rides.from_id=match.from_id AND rides.to_id=match.to_id"
             + " WHERE rides.parent_id=0 AND rides.type=" + Ride.OFFER
                 + " AND match.sub_from_id=? AND match.sub_to_id =?"
                 + " AND rides.dep > ? AND rides.who <> '' AND active = 1"
             + " GROUP BY rides.ref, rides.dep ORDER BY rides.dep, rides._id;";
 
     public Cursor queryRides(String from_id, String to_id, String dep) {
         return getReadableDatabase().rawQuery(SELECT_RIDE_MATCHES,
                 new String[] { from_id, to_id, (dep != null)? dep : MINUS_ONE});
     }
 
     static final String SELECT_SUB_RIDES = SELECT_RIDES
             + " WHERE parent_id=? ORDER BY rides.dep;";
 
     public Cursor querySubRides(String parent_id) {
         return getReadableDatabase().rawQuery(SELECT_SUB_RIDES,
                 new String[] { parent_id });
     }
 
     static final String SELECT_MYRIDES = SELECT_RIDES_COLUMNS
             + ", max(rides._id)" + JOIN
             + " WHERE marked=1 AND dirty > -2"
             + " GROUP BY rides.ref"
             + " ORDER BY dep DESC, rides._id DESC;";
 
     public Cursor queryMyRides() {
         return getReadableDatabase().rawQuery(SELECT_MYRIDES, null);
     }
 
     static final String WHERE_OUTDATED = " _id IN ( SELECT rides._id FROM rides"
             + " LEFT JOIN 'route_matches' AS match ON "
                 + " rides.from_id=match.from_id AND rides.to_id=match.to_id"
             + " WHERE rides.type=" + Ride.OFFER
                 + " AND match.sub_from_id = ? AND match.sub_to_id = ? "
                 + " AND dep >= ? AND dep <= ?"
                 + " AND rides.refresh < ?";
 
     public int deleteOutdated(
             String from, String to, String dep, String arr, String time) {
         return getWritableDatabase().delete(RIDES, WHERE_OUTDATED
                     + (from.equals(MINUS_ONE)? IS_MARKED : CLOSE),
                     new String[] { from, to, dep, arr, time });
     }
     private static final String MINUS_ONE = "-1";
     private static final String CLOSE = ");";
     private static final String IS_MARKED = " AND marked = 1" + CLOSE;
 
     public int invalidateCache() {
         Log.d(TAG, INVALIDATE_CACHE);
         ContentValues values = new ContentValues();
        values.putNull(LAST_REFRESH);
         return getWritableDatabase().update(JOBS, values, null, null);
     }
 
     static final String IS_DELETED = "SELECT dirty from rides WHERE _id IS ?";
 
     public boolean isDeleted(String id) {
         if (isDeleted == null)
             isDeleted = getReadableDatabase().compileStatement(IS_DELETED);
         isDeleted.bindLong(1, Long.parseLong(id));
         return isDeleted.simpleQueryForLong() == Ride.FLAG_DELETED;
     }
 }
