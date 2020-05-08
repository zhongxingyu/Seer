 package de.team06.psychoapp;
 
 /**
  * Created by Malte on 28.06.13.
  */
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class MySQLiteHelper extends SQLiteOpenHelper {
 
     public static final String TABLE_SOCIAL_INTERACTIONS = "interactions";
     /* All columns */
     public static final String[] TABLE_SOCIAL_INTERACTIONS_ALL = { "_id", "code", "alarmtime", "responsetime", "skip", "contacts", "hours", "minutes" };
     public static final String COLUMN_ID = "_id";
 
     private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 12;
 
     // Database creation sql statement
     private static final String DATABASE_CREATE = "CREATE TABLE "
             + TABLE_SOCIAL_INTERACTIONS + "(" + COLUMN_ID
             + " integer primary key, "
            // + " INTEGER auto_increment primary key NOT NULL, "
             + "code varchar(5) not null,"
             + "alarmtime integer,"
            + "responsetime integer default 0,"
             + "skip int NOT NULL DEFAULT 1,"
            + "contacts integer,"
            + "hours integer,"
            + "minutes integer);";
 
     public MySQLiteHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
     @Override
     public void onCreate(SQLiteDatabase database) {
         database.execSQL(DATABASE_CREATE);
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         Log.w(MySQLiteHelper.class.getName(),
                 "Upgrading database from version " + oldVersion + " to "
                         + newVersion + ", which will destroy all old data");
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_SOCIAL_INTERACTIONS);
         onCreate(db);
     }
 
 }
