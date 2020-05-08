 package net.sourcewalker.garanbot.data;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class GaranbotDBHelper extends SQLiteOpenHelper {
 
     private final String TAG = "GaranbotDBHelper";
     private static final String DATABASE_NAME = "garanbot.sql";
     private static final int DATABASE_VERSION = 2;
 
     public GaranbotDBHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
         db.execSQL(GaranbotDBMetaData.SCHEMA);
 
         // create dummy entry
         db
                 .execSQL("INSERT INTO "
                         + GaranbotDBMetaData.TABLE_NAME
                         + " (name,manufacturer,itemtype,vendor,location,notes,haspicture,purchasedate,endofwarranty) "
                        + "VALUES ('dummyEntry','matchbox','toy','schinacher','fn','green car', 0,'2002-12-24T00:00:00+00:00','2004-12-24T00:00:00+00:00')");
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
         Log.w(TAG, "Upgrading database, which will destroy all old data.");
         db.execSQL("DROP TABLE IF EXISTS " + GaranbotDBMetaData.TABLE_NAME);
         onCreate(db);
     }
 
 }
