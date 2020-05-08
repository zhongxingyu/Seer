 package com.geertvanderploeg.kiekeboek.syncadapter.intranet;
 
 import java.util.Date;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 /**
  * Self contained (apart from sqllite database) store for getting sync status (currently last sync time only)
  */
 public class IntranetSyncStatus {
 
 
   public static Date getLastSyncDate(Context c) {
     final SQLiteDatabase rodb = getRODB(c);
     final Cursor cursor = rodb.query("syncstatus", new String[]{"lastSyncTime"}, null, null, null, null, null);
     final Date date;
     if (cursor.moveToFirst()) {
      date = new Date(cursor.getInt(0) * 1000);
     } else {
       cursor.close();
       throw new IllegalStateException("Expected one row with lastSyncTime but found none.");
     }
     cursor.close();
     rodb.close();
     return date;
   }
 
   public static void saveLastSyncDate(Context c, Date date) {
     final SQLiteDatabase db = getDBHelper(c).getWritableDatabase();
     ContentValues cv = new ContentValues(1);
    cv.put("lastSyncTime", date.getTime()/1000);
     db.update("syncstatus", cv, null, null);
     db.close();
   }
 
   private static SQLiteDatabase getRODB(Context c) {
     return getDBHelper(c).getReadableDatabase();
   }
 
   private static IntranetSyncStatusOpenHelper getDBHelper(Context c) {
     return new IntranetSyncStatusOpenHelper(c);
   }
 
   public static class IntranetSyncStatusOpenHelper extends SQLiteOpenHelper {
 
 
    private static final int DATABASE_VERSION = 5;
     private static final String CREATE_TABLE = "CREATE TABLE syncstatus (lastSyncTime INTEGER);";
     private static final String DATABASE_NAME = "intranetSyncStatus";
     private static final String DROP_TABLE = "DROP TABLE IF EXISTS syncstatus";
 
     IntranetSyncStatusOpenHelper(Context context) {
       super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
       db.execSQL(CREATE_TABLE);
       ContentValues cv = new ContentValues(1);
      cv.put("lastSyncTime", new Date(0).getTime());
       db.insert("syncstatus", null, cv);
     }
 
     @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
       db.execSQL(DROP_TABLE);
       onCreate(db);
     }
   }
 }
