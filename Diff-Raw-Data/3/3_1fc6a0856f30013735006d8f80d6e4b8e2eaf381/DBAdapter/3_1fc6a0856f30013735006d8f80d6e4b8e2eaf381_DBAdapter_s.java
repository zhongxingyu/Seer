 package com.fernferret.android.fortywinks;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 
 public class DBAdapter extends SQLiteOpenHelper {
 
     private static final int DATABASE_VERSION = 1;
     private static final String DATABASE_NAME = "fortywinks";
     
     private SQLiteDatabase mDb;
 
     public DBAdapter(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
         mDb = getWritableDatabase();
     }
     
     public void saveAlarm(Alarm alarm) {
         mDb.delete("alarms", "id = ?", new String[] {alarm.getId() + ""});
        SQLiteStatement s = mDb.compileStatement("INSERT INTO alarms VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");
        s.bindString(1, alarm.getId() + "");
         s.bindString(2, alarm.getHour() + "");
         s.bindString(3, alarm.getMinute() + "");
         s.bindString(4, alarm.getThreshold() + "");
         s.bindString(5, alarm.getDaysOfWeek() + "");
         s.bindString(6, alarm.getFollowups() + "");
         s.bindString(7, alarm.getIntervalStart() + "");
         s.bindString(8, alarm.getIntervalEnd() + "");
         s.bindString(9, (alarm.getEnabled() ? 1 : 0) + "");
         s.executeInsert();
     }
     
     public List<Alarm> listAlarms() {
         List<Alarm> result = new ArrayList<Alarm>();
         String query = "SELECT * FROM alarms";
         Cursor cursor = mDb.rawQuery(query, null);
         
         if (cursor.moveToFirst()) {
             do {
                 
                 /* Create and populate each alarm */
                 Alarm next = new Alarm(cursor.getInt(0));
                 next.setHour(cursor.getInt(1));
                 next.setMinute(cursor.getInt(2));
                 next.setThreshold(cursor.getInt(3));
                 next.setDaysOfWeek(cursor.getInt(4));
                 next.setFollowups(cursor.getInt(5));
                 next.setIntervalStart(cursor.getInt(6));
                 next.setIntervalEnd(cursor.getInt(7));
                 next.setEnabled(cursor.getInt(8) == 1);
                 result.add(next);
                 
             } while (cursor.moveToNext());
         }
         
         if (cursor != null && !cursor.isClosed()) {
             cursor.close();
         }
         
         return result;
     }
     
     public Alarm getAlarm(int id) {
         Alarm result = new Alarm(id);
         String query = "SELECT * FROM alarms WHERE id = ?";
         Cursor cursor = mDb.rawQuery(query, new String[] { id + "" });
         
         if (cursor.moveToFirst()) {
             
             /* Populate alarm object */
             result.setHour(cursor.getInt(1));
             result.setMinute(cursor.getInt(2));
             result.setThreshold(cursor.getInt(3));
             result.setDaysOfWeek(cursor.getInt(4));
             result.setFollowups(cursor.getInt(5));
             result.setIntervalStart(cursor.getInt(6));
             result.setIntervalEnd(cursor.getInt(7));
             result.setEnabled(cursor.getInt(8) == 1);
             
         } else {
             
             /* We didn't find that one */
             result = null;
         }
         
         if (cursor != null && !cursor.isClosed()) {
             cursor.close();
         }
         
         return result;
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
         db.execSQL("CREATE TABLE alarms (id INTEGER PRIMARY KEY, hour INTEGER, minute INTEGER, threshold INTEGER, days_of_week INTEGER, followups INTEGER, interval_start INTEGER, interval_end INTEGER, enabled INTEGER);");
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         db.execSQL("DROP TABLE IF EXISTS alarms");
         onCreate(db);
     }
 
 }
