 package com.fernferret.android.fortywinks;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 import android.util.Log;
 
 /**
  * Database adapter that allows easy communication with the SQLite DB with
  * handling of injection attacks.
  * 
  * @author Jimmy Theis
  * 
  */
 public class DBAdapter extends SQLiteOpenHelper {
 
     private static final int DATABASE_VERSION = 4;
     private static final String DATABASE_NAME = "fortywinks";
 
     private SQLiteDatabase mDb;
 
     public DBAdapter(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
         mDb = getWritableDatabase();
     }
 
     private int getNextAlarmId() {
         String query = "SELECT id FROM alarms ORDER BY id DESC";
         Cursor cursor = mDb.rawQuery(query, null);
         int result = -1;
 
         if (cursor.moveToFirst()) {
             result = cursor.getInt(0);
         } else {
             result = 1;
         }
 
         if (cursor != null && !cursor.isClosed()) {
             cursor.close();
         }
 
         return result;
     }
 
     private int[] getNextFollowupIds(int n) {
         int[] result = new int[n];
 
         String query = "SELECT id FROM followups ORDER BY id DESC";
         Cursor cursor = mDb.rawQuery(query, null);
 
         int seed = 1;
         if (cursor.moveToFirst()) {
             seed = cursor.getInt(0);
         }
 
         for (int i = 0; i < n; i++) {
             result[i] = ++seed;
         }
 
         if (cursor != null && !cursor.isClosed()) {
             cursor.close();
         }
 
         return result;
     }
 
     /**
      * Populate the alarm object if it needs it
      * 
      * @param alarm
      * @return whether or not we're going to be creating a new one
      */
     private boolean populateAlarm(Alarm alarm) {
         if (alarm.getId() == -1) {
             alarm.setId(getNextAlarmId());
             alarm.populateFollowups(getNextFollowupIds(alarm.getNumFollowups()));
             return true;
         }
         return false;
     }
 
     /**
      * Save the given alarm to the Database. If it does not have an ID, it will
      * be given one, otherwise, its values will override the previous values,
      * effectively giving this query add/edit privilages.
      * 
      * @param alarm
      *            The alarm to add to the database.
      */
     public void saveAlarm(Alarm alarm) {
         if (populateAlarm(alarm)) {
             SQLiteStatement fs = mDb.compileStatement("INSERT INTO followups VALUES(?, ?, ?)");
             fs.bindString(2, alarm.getId() + ""); // our alarm object id will remain
                                            // constant
 
             HashMap<Integer, Long> followups = alarm.getFollowups();
 
             /* Insert all of our new followups */
             for (int id : followups.keySet()) {
                Log.d("40W", "Inserting Followup: ID: " + id + " ALARM: " + alarm.getId() + " TIME: " + followups.get(id));
                 fs.bindString(1, id + "");
                 fs.bindLong(3, followups.get(id));
                 fs.executeInsert();
             }
 
         } else {
             mDb.delete("alarms", "id = ?", new String[] { alarm.getId() + "" });
         }
 
         if (alarm.isPowerNap()) {
         	Log.w("40W", "40W Replacing Power Nap");
             /* Delete the current power nap */
             Alarm currentPowerNap = getPowerNap();
             if (currentPowerNap != null) {
                 mDb.delete("alarms", "id = ?", new String[] { currentPowerNap.getId() + "" });
             }
             mDb.delete("powernap", "", null);
 
             SQLiteStatement ps = mDb.compileStatement("INSERT INTO powernap VALUES(?)");
             ps.bindString(1, alarm.getId() + "");
             ps.executeInsert();
         } else if (alarm.isQuikAlarm()) {
             mDb.delete("quikalarms", "id = ?", new String[] { alarm.getId() + "" });
             SQLiteStatement qs = mDb.compileStatement("INSERT INTO quikalarms VALUES(?)");
             qs.bindString(1, alarm.getId() + "");
             qs.executeInsert();
         }
 
         SQLiteStatement s = mDb.compileStatement("INSERT INTO alarms (hour, minute, threshold, days_of_week, followups, interval_start, interval_end, enabled) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
         s.bindString(1, alarm.getHour() + "");
         s.bindString(2, alarm.getMinute() + "");
         s.bindString(3, alarm.getThreshold() + "");
         s.bindString(4, alarm.getDaysOfWeek() + "");
         s.bindString(5, alarm.getNumFollowups() + "");
         s.bindString(6, alarm.getIntervalStart() + "");
         s.bindString(7, alarm.getIntervalEnd() + "");
         s.bindString(8, (alarm.getEnabled() ? 1 : 0) + "");
         s.executeInsert();
         Log.w("40W", "40W: Alarm Saved - ID: " + alarm.getId() + ", Time:" + alarm);
     }
 
     public void deleteAlarm(int id) {
         String[] args = new String[] {id + ""};
         mDb.delete("followups", "alarm = ?", args);
         mDb.delete("powernap", "id = ?", args);
         mDb.delete("quikalarms", "id = ?", args);
         mDb.delete("alarms", "id = ?", args);
 
     }
 
     public boolean alarmExists(int id) {
         String query = "SELECT id FROM alarms WHERE id = ?";
         Cursor cursor = mDb.rawQuery(query, new String[] { id + "" });
         return cursor.moveToFirst();
     }
 
     /**
      * Get a List of all the Alarms in the database.
      * 
      * @return a List of all the Alarms in ArrayList form.
      */
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
                 next.setNumFollowups(cursor.getInt(5));
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
 
     /**
      * Returns the most recently set PowerNap.
      * 
      * @return the most recent PowerNap.
      */
     public Alarm getPowerNap() {
         Alarm result;
         String query = "SELECT * FROM alarms, powernap";
         Cursor cursor = mDb.rawQuery(query, null);
 
         if (cursor.moveToFirst()) {
 
             /* Populate alarm object */
             result = new Alarm(cursor.getInt(0));
             result.setHour(cursor.getInt(1));
             result.setMinute(cursor.getInt(2));
             result.setThreshold(cursor.getInt(3));
             result.setDaysOfWeek(cursor.getInt(4));
             result.setNumFollowups(cursor.getInt(5));
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
     
     @SuppressWarnings("unchecked")
     public List<Alarm> getQuickAlarmsAndAlarms() {
         List<Alarm> result = new ArrayList<Alarm>();
         
         String query = "SELECT * FROM alarms, quikalarms";
         Cursor cursor = mDb.rawQuery(query, null);
 
         if (cursor.moveToFirst()) {
             do {
 
                 /* Create and populate each alarm */
                 Alarm next = new Alarm(cursor.getInt(0));
                 next.setHour(cursor.getInt(1));
                 next.setMinute(cursor.getInt(2));
                 next.setThreshold(cursor.getInt(3));
                 next.setDaysOfWeek(cursor.getInt(4));
                 next.setNumFollowups(cursor.getInt(5));
                 next.setIntervalStart(cursor.getInt(6));
                 next.setIntervalEnd(cursor.getInt(7));
                 next.setEnabled(cursor.getInt(8) == 1);
                 result.add(next);
 
             } while (cursor.moveToNext());
         }
 
         if (cursor != null && !cursor.isClosed()) {
             cursor.close();
         }
         
         Collections.sort(result);
         return result;
     }
     
     public List<Alarm> getFullAlarmList(int numEntries) {
         List<Alarm> result = new ArrayList<Alarm>();
         Alarm powerNap = getPowerNap();
         if (powerNap != null) {
             result.add(powerNap);
         }
         result.addAll(getQuickAlarmsAndAlarms());
         return result.subList(0, numEntries);
     }
 
     /**
      * Retrieves an alarm from the database with the given ID.
      * 
      * @param id
      *            The ID of the alarm to retrieve
      * @return An alarm with all quantities specified
      */
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
             result.setNumFollowups(cursor.getInt(5));
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
 
     public void smokeDatabase() {
         onUpgrade(mDb, 0, DATABASE_VERSION);
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
         db.execSQL("CREATE TABLE alarms (id INTEGER PRIMARY KEY AUTOINCREMENT, hour INTEGER, minute INTEGER, threshold INTEGER, days_of_week INTEGER, followups INTEGER, interval_start INTEGER, interval_end INTEGER, enabled INTEGER);");
         db.execSQL("CREATE TABLE followups (id INTEGER PRIMARY KEY, alarm INTEGER, time REAL, FOREIGN KEY(alarm) REFERENCES alarms(id))");
         db.execSQL("CREATE TABLE powernap (id INTEGER PRIMARY KEY, FOREIGN KEY(id) REFERENCES alarms(id))");
         db.execSQL("CREATE TABLE quikalarms (id INTEGER PRIMARY KEY, FOREIGN KEY(id) REFERENCES alarms(id))");
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         db.execSQL("DROP TABLE IF EXISTS alarms");
         db.execSQL("DROP TABLE IF EXISTS followups");
         db.execSQL("DROP TABLE IF EXISTS powernap");
         db.execSQL("DROP TABLE IF EXISTS quikalarms");
         onCreate(db);
     }
 
 }
