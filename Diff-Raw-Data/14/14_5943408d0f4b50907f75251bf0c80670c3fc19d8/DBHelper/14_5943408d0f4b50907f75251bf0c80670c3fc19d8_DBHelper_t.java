 package com.example;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 
 public class DBHelper extends SQLiteOpenHelper {
 
     private static String DB_NAME = "energy_app";
     private static String TABLE_NAME = "energy_levels";
     private static String LEVEL = "level";
     private static String TIME = "log_time";
    private static String YEAR = "year";
    private static String MONTH = "month";
    private static String DAY = "day";
    private static String HOUR = "hour";
     private static int DB_VERSION = 1;
 
     public DBHelper(Context c) {
         super(c, DB_NAME, null, DB_VERSION);
     }
 
     @Override
     public void onCreate(SQLiteDatabase sqLiteDatabase) {
         String sql = String.format(
                 "CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s INTEGER)",
                 TABLE_NAME,
                 TIME,
                 LEVEL
         );
 
         sqLiteDatabase.execSQL(sql);
 
     }
 
     public void logLevel(int level) {
         ContentValues c = new ContentValues();
        c.put(TIME, System.currentTimeMillis()/1000);
         c.put(LEVEL, level);
 
         SQLiteDatabase db = getWritableDatabase();
         db.insert(TABLE_NAME, null, c);
         db.close();
 
     }
 
     public String getData() {
         SQLiteDatabase db = this.getReadableDatabase();
 
         Cursor c = db.query(
                 TABLE_NAME,
                 new String[]{TIME, LEVEL},
                 "",
                 new String[]{},
                 null,
                 null,
                 String.format("%s DESC", TIME)
         );
 
 
         String out = "";
         SimpleDateFormat time_formatter = new SimpleDateFormat("h:mm a", Locale.US);
         SimpleDateFormat year_formatter = new SimpleDateFormat("MM/dd", Locale.US);
 
         int prev_date = -1;
         while (c.moveToNext()) {
             Calendar cal = Calendar.getInstance();
             cal.setTimeInMillis(c.getLong(0));
 
             int cur_date = cal.get(Calendar.DATE);
             if (cur_date != prev_date) {
                 out+= String.format("%s\n", year_formatter.format(cal.getTime()));
                 prev_date = cur_date;
             }
             out += String.format("%s : %d\n", time_formatter.format(cal.getTime()), c.getInt(1));
         }
 
 
         db.close();
         return out;
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
     }
 }
