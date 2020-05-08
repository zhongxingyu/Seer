 package mobisocial.omnistanford.db;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 import android.net.Uri;
 
 public class LocationManager extends ManagerBase {
     public static final String TAG = "LocationManager";
     
     private static final int _id = 0;
     private static final int name = 1;
     private static final int principal = 2;
     private static final int accountType = 3;
     private static final int type = 4;
     private static final int minLat = 5;
     private static final int maxLat = 6;
     private static final int minLon = 7;
     private static final int maxLon = 8;
     private static final int feedUri = 9;
     private static final int imageUrl = 10;
     private static final int image = 11;
     
     private static final String[] STANDARD_FIELDS = new String[] {
         MLocation.COL_ID,
         MLocation.COL_NAME,
         MLocation.COL_PRINCIPAL,
         MLocation.COL_ACCOUNT_TYPE,
         MLocation.COL_TYPE,
         MLocation.COL_MIN_LAT,
         MLocation.COL_MAX_LAT,
         MLocation.COL_MIN_LON,
         MLocation.COL_MAX_LON,
         MLocation.COL_FEED_URI,
         MLocation.COL_IMAGE_URL,
         MLocation.COL_IMAGE
     };
 
     private SQLiteStatement mUpdateLocation;
     private SQLiteStatement mInsertLocation;
     
     public LocationManager(SQLiteOpenHelper databaseSource) {
         super(databaseSource);
     }
 
     public LocationManager(SQLiteDatabase db) {
         super(db);
     }
     
     public void insertLocation(MLocation location) {
         SQLiteDatabase db = initializeDatabase();
         if (mInsertLocation == null) {
             synchronized(this) {
                 StringBuilder sql = new StringBuilder(" INSERT INTO ")
                     .append(MLocation.TABLE).append("(")
                     .append(MLocation.COL_NAME).append(",")
                     .append(MLocation.COL_PRINCIPAL).append(",")
                     .append(MLocation.COL_ACCOUNT_TYPE).append(",")
                     .append(MLocation.COL_TYPE).append(",")
                     .append(MLocation.COL_MIN_LAT).append(",")
                     .append(MLocation.COL_MAX_LAT).append(",")
                     .append(MLocation.COL_MIN_LON).append(",")
                     .append(MLocation.COL_MAX_LON).append(",")
                     .append(MLocation.COL_FEED_URI).append(",")
                     .append(MLocation.COL_IMAGE_URL).append(",")
                     .append(MLocation.COL_IMAGE)
                    .append(") VALUES (?,?,?,?,?,?,?,?,?)");
                 mInsertLocation = db.compileStatement(sql.toString());
             }
         }
         
         synchronized(mInsertLocation) {
             bindField(mInsertLocation, name, location.name);
             bindField(mInsertLocation, principal, location.principal);
             bindField(mInsertLocation, accountType, location.accountType);
             bindField(mInsertLocation, type, location.type);
             bindField(mInsertLocation, minLat, location.minLatitude);
             bindField(mInsertLocation, maxLat, location.maxLatitude);
             bindField(mInsertLocation, minLon, location.minLongitude);
             bindField(mInsertLocation, maxLon, location.maxLongitude);
             if (location.feedUri == null) {
                 bindField(mInsertLocation, feedUri, null);
             } else {
                 bindField(mInsertLocation, feedUri, location.feedUri.toString());
             }
             bindField(mInsertLocation, imageUrl, location.imageUrl);
             bindField(mInsertLocation, image, location.image);
             location.id = mInsertLocation.executeInsert();
         }
     }
     
     public void updateLocation(MLocation location) {
         SQLiteDatabase db = initializeDatabase();
         if (mUpdateLocation == null) {
             synchronized(this) {
                 StringBuilder sql = new StringBuilder("UPDATE ")
                     .append(MLocation.TABLE)
                     .append(" SET ")
                     .append(MLocation.COL_NAME).append("=?,")
                     .append(MLocation.COL_PRINCIPAL).append("=?,")
                     .append(MLocation.COL_ACCOUNT_TYPE).append("=?,")
                     .append(MLocation.COL_TYPE).append("=?,")
                     .append(MLocation.COL_MIN_LAT).append("=?,")
                     .append(MLocation.COL_MAX_LAT).append("=?,")
                     .append(MLocation.COL_MIN_LON).append("=?,")
                     .append(MLocation.COL_MAX_LON).append("=?,")
                     .append(MLocation.COL_FEED_URI).append("=?,")
                     .append(MLocation.COL_IMAGE_URL).append("=?,")
                     .append(MLocation.COL_IMAGE).append("=?")
                     .append(" WHERE ").append(MLocation.COL_ID).append("=?");
                 mUpdateLocation = db.compileStatement(sql.toString());
             }
         }
             
         synchronized(mUpdateLocation) {
             bindField(mUpdateLocation, name, location.name);
             bindField(mUpdateLocation, principal, location.principal);
             bindField(mUpdateLocation, accountType, location.accountType);
             bindField(mUpdateLocation, type, location.type);
             bindField(mUpdateLocation, minLat, location.minLatitude);
             bindField(mUpdateLocation, maxLat, location.maxLatitude);
             bindField(mUpdateLocation, minLon, location.minLongitude);
             bindField(mUpdateLocation, maxLon, location.maxLongitude);
             if (location.feedUri == null) {
                 bindField(mUpdateLocation, feedUri, null);
             } else {
                 bindField(mUpdateLocation, feedUri, location.feedUri.toString());
             }
             bindField(mUpdateLocation, imageUrl, location.imageUrl);
             bindField(mUpdateLocation, image, location.image);
             bindField(mUpdateLocation, 12, location.id);
             mUpdateLocation.execute();
         }
     }
     
     public void ensureLocation(MLocation location) {
         MLocation existing = getLocation(location.name, location.type);
         if (existing != null) {
             location.id = existing.id;
             if (location.feedUri == null) {
                 location.feedUri = existing.feedUri;
             }
             if (location.imageUrl == null) {
                 location.imageUrl = existing.imageUrl;
             }
             if (location.image == null) {
                 location.image = existing.image;
             }
             updateLocation(location);
         } else {
             insertLocation(location);
         }
     }
     
     public List<MLocation> getLocations(String type) {
         SQLiteDatabase db = initializeDatabase();
         String table = MLocation.TABLE;
         String selection = MLocation.COL_TYPE + "=?";
         String[] selectionArgs = new String[] { type };
         Cursor c = db.query(table, STANDARD_FIELDS, selection, selectionArgs, null, null, null);
         try {
             List<MLocation> result = new ArrayList<MLocation>();
             while (c.moveToNext()) {
                 result.add(fillInStandardFields(c));
             }
             return result;
         } finally {
             c.close();
         }
     }
     
     public List<MLocation> getLocations() {
         SQLiteDatabase db = initializeDatabase();
         String table = MLocation.TABLE;
         Cursor c = db.query(table, STANDARD_FIELDS, null, null, null, null, null);
         try {
             List<MLocation> result = new ArrayList<MLocation>();
             while (c.moveToNext()) {
                 result.add(fillInStandardFields(c));
             }
             return result;
         } finally {
             c.close();
         }
     }
     
     public MLocation getLocation(Long id) {
         SQLiteDatabase db = initializeDatabase();
         String table = MLocation.TABLE;
         String selection = MLocation.COL_ID + "=?";
         String[] selectionArgs = new String[] { id.toString() };
         Cursor c = db.query(table, STANDARD_FIELDS, selection, selectionArgs, null, null, null);
         try {
             if (c.moveToFirst()) {
                 return fillInStandardFields(c);
             } else {
                 return null;
             }
         } finally {
             c.close();
         }
     }
     
     public MLocation getLocation(String principal) {
         SQLiteDatabase db = initializeDatabase();
         String table = MLocation.TABLE;
         String selection = MLocation.COL_PRINCIPAL + "=?";
         String[] selectionArgs = new String[] { principal };
         Cursor c = db.query(table, STANDARD_FIELDS, selection, selectionArgs, null, null, null);
         try {
             if (c.moveToFirst()) {
                 return fillInStandardFields(c);
             } else {
                 return null;
             }
         } finally {
             c.close();
         }
     }
     
     public MLocation getLocation(String name, String type) {
         SQLiteDatabase db = initializeDatabase();
         String table = MLocation.TABLE;
         String selection = MLocation.COL_NAME + "=? AND " +
                 MLocation.COL_TYPE + "=?";
         String[] selectionArgs = new String[] { name, type };
         Cursor c = db.query(table, STANDARD_FIELDS, selection, selectionArgs, null, null, null);
         try {
             if (c.moveToFirst()) {
                 return fillInStandardFields(c);
             } else {
                 return null;
             }
         } finally {
             c.close();
         }
     }
     
     public MLocation getLocation(double latitude, double longitude) {
         SQLiteDatabase db = initializeDatabase();
         String table = MLocation.TABLE;
         String selection = MLocation.COL_MIN_LAT + "<? AND " +
                 MLocation.COL_MAX_LAT + ">? AND " +
                 MLocation.COL_MIN_LON + "<? AND " +
                 MLocation.COL_MAX_LON + ">?";
         String lat = new Double(latitude).toString();
         String lon = new Double(longitude).toString();
         String[] selectionArgs = new String[] { lat, lat, lon, lon };
         Cursor c = db.query(table, STANDARD_FIELDS, selection, selectionArgs, null, null, null);
         try {
             if (c.moveToFirst()) {
                 return fillInStandardFields(c);
             } else {
                 return null;
             }
         } finally {
             c.close();
         }
     }
     
     private MLocation fillInStandardFields(Cursor c) {
         MLocation loc = new MLocation();
         loc.id = c.getLong(_id);
         loc.name = c.getString(name);
         loc.principal = c.getString(principal);
         loc.accountType = c.getString(accountType);
         loc.type = c.getString(type);
         try {
             loc.minLatitude = c.getDouble(minLat);
             loc.maxLatitude = c.getDouble(maxLat);
             loc.minLongitude = c.getDouble(minLon);
             loc.maxLongitude = c.getDouble(maxLon);
         } catch (Exception e) {
             loc.minLatitude = null;
             loc.maxLatitude = null;
             loc.minLongitude = null;
             loc.maxLongitude = null;
         }
         if (c.getString(feedUri) != null) {
             loc.feedUri = Uri.parse(c.getString(feedUri));
         } else {
             loc.feedUri = null;
         }
         loc.imageUrl = c.getString(imageUrl);
         loc.image = c.getBlob(image);
         return loc;
     }
 }
