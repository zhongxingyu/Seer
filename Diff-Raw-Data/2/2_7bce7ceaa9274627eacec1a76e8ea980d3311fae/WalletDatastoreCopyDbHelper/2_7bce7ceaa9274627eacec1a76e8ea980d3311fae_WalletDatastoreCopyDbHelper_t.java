 package com.rubixconsulting.walletcracker;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteCursor;
 import android.database.sqlite.SQLiteCursorDriver;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteQuery;
 
 public class WalletDatastoreCopyDbHelper extends SQLiteOpenHelper {
   public static final String TABLE_METADATA = "metadata";
  private static final int DATABASE_VERSION = 9999;
 
   public WalletDatastoreCopyDbHelper(Context context) {
     super(context, context.getString(R.string.db_name), null, DATABASE_VERSION);
   }
 
   @Override public void onCreate(SQLiteDatabase db) {
     // the database should never have to be created, it is copied from Google Wallet
   }
 
   @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
     // the database should never have to be upgraded, it is copied from Google Wallet
   }
 
   public static class MetadataCursor extends SQLiteCursor {
     private static final String QUERY = "SELECT id, proto FROM " + TABLE_METADATA;
 
     public MetadataCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
       super(db, driver, editTable, query);
     }
 
     private static class Factory implements SQLiteDatabase.CursorFactory {
       @Override public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
         return new MetadataCursor(db, driver, editTable, query);
       }
     }
 
     public long getColId() {
       return getLong(getColumnIndexOrThrow("id"));
     }
 
     public byte[] getColProto() {
       return getBlob(getColumnIndexOrThrow("proto"));
     }
   }
 
   public byte[] getDeviceInfo() {
     MetadataCursor c = null;
     try {
       final String sql = MetadataCursor.QUERY + " WHERE id = 'deviceInfo'";
       SQLiteDatabase d = getReadableDatabase();
       c = (MetadataCursor) d.rawQueryWithFactory(new MetadataCursor.Factory(), sql, null, null);
       c.moveToFirst();
       return c.getColProto();
     } finally {
       if (c != null) {
         c.close();
       }
     }
   }
 }
