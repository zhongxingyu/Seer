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
 
 public class SQLiteAdapter implements DBAdapter {
     
     private static final String TAG = "FortyWinks.SQLite";
     
     private static final String DATABASE_NAME = "fortywinks";
     private static final int DATABASE_VERSION = 1;
     
     /* Alarms table names */
     private static final String ALARMS_TABLE = "alarms";
     private static final String ALARMS_ID_COL = "id";
     private static final String ALARMS_HOUR_COL = "hour";
     private static final String ALARMS_MINUTE_COL = "minute";
     private static final String ALARMS_THRESHOLD_COL = "threshold";
     private static final String ALARMS_DAYS_COL = "days_of_week";
     private static final String ALARMS_FOLLOWUPS_COL = "followups";
     private static final String ALARMS_INTERVAL_START_COL = "interval_start";
     private static final String ALARMS_INTERVAL_END_COL = "interval_end";
     private static final String ALARMS_ENABLED_COL = "enabled";
     
     /* Followups table names */
     private static final String FOLLOWUPS_TABLE = "followups";
     private static final String FOLLOWUPS_ID_COL = "id";
     private static final String FOLLOWUPS_ALARM_COL = "alarm";
     private static final String FOLLOWUPS_TIME_COL = "time";
     
     /* Powernap table names */
     private static final String POWERNAP_TABLE = "powernap";
     private static final String POWERNAP_ID_COL = "id";
     
     /* Quik Alarms table names */
     private static final String QUIK_ALARMS_TABLE = "quikalarms";
     private static final String QUIK_ALARMS_ID_COL = "id";
     
     /* Lists of constants */
     private static final String[] ALL_TABLES = new String[] {ALARMS_TABLE, FOLLOWUPS_TABLE, POWERNAP_TABLE, QUIK_ALARMS_TABLE};
     private static final String[] ALARMS_COLS = new String[] {ALARMS_ID_COL, ALARMS_HOUR_COL, ALARMS_MINUTE_COL, ALARMS_THRESHOLD_COL, 
                                                                  ALARMS_DAYS_COL, ALARMS_FOLLOWUPS_COL, ALARMS_INTERVAL_START_COL, 
                                                                  ALARMS_INTERVAL_END_COL, ALARMS_ENABLED_COL};
     private static final String[] FOLLOWUPS_COLS = new String[] {FOLLOWUPS_ID_COL, FOLLOWUPS_ALARM_COL, FOLLOWUPS_TIME_COL};
     
     /* Queries */
     private static final String INSERT_ALARM_Q = "INSERT INTO ? (?, ?, ?, ?, ?, ?, ?, ?, ?) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
     private static final String INSERT_FOLLOWUP_Q = "INSERT INTO ? (?, ?, ?) VALUES (?, ?, ?)";
     private static final String INSERT_POWERNAP_Q = "INSERT INTO ? (?) VALUES (?)";
     private static final String INSERT_QUIK_ALARM_Q = "INSERT INTO ? (?) VALUES (?)";
     
     private SQLiteOpenHelper mOpenHelper;
     private SQLiteDatabase mDb;
     
     private SQLiteStatement mInsertAlarmQuery;
     private SQLiteStatement mInsertFollowupQuery;
     private SQLiteStatement mInsertPowerNapQuery;
     private SQLiteStatement mInsertQuikAlarmQuery;
     
     public SQLiteAdapter(Context context) {
         mOpenHelper = new OpenHelper(context);
         
         Log.d(TAG, "Adapter created. Expecting database version " + DATABASE_VERSION);
         
         /* We just use the open helper to get the database */
         Log.d(TAG, "Getting a database connection");
         mDb = mOpenHelper.getWritableDatabase();
         Log.d(TAG, "Got the database, clear to read/write");
         
         /* Compile our reusable statements once */
         mInsertAlarmQuery = mDb.compileStatement(INSERT_ALARM_Q);
         mInsertAlarmQuery.bindString(0, ALARMS_TABLE);
         for (int i = 1; i < ALARMS_COLS.length; mInsertAlarmQuery.bindString(i, ALARMS_COLS[i++]));
         
         mInsertFollowupQuery = mDb.compileStatement(INSERT_FOLLOWUP_Q);
         mInsertFollowupQuery.bindString(0, FOLLOWUPS_TABLE);
         for (int i = 1; i < FOLLOWUPS_COLS.length; mInsertFollowupQuery.bindString(i, FOLLOWUPS_COLS[i++]));
         
         mInsertPowerNapQuery = mDb.compileStatement(INSERT_POWERNAP_Q);
         mInsertPowerNapQuery.bindString(0, POWERNAP_TABLE);
         mInsertPowerNapQuery.bindString(1, POWERNAP_ID_COL);
         
         mInsertQuikAlarmQuery = mDb.compileStatement(INSERT_QUIK_ALARM_Q);
         mInsertQuikAlarmQuery.bindString(0, QUIK_ALARMS_TABLE);
         mInsertQuikAlarmQuery.bindString(1, QUIK_ALARMS_ID_COL);
     }
     
     private String boolToIntString(boolean b) { return b ? "1" : "0"; }
     
     private void nullSafeCloseCursor(Cursor c) {
         if (c != null && !c.isClosed()) {
             c.close();
         }  
     }
     
     private int getNextAlarmId() {
         Cursor c = mDb.query(ALARMS_TABLE, new String[] {ALARMS_ID_COL}, "", null, null, null, ALARMS_ID_COL + " DESC", "1");
         if (c.moveToFirst()) {
             int result = c.getInt(0);
             nullSafeCloseCursor(c);
             return result;
         }
         return 1;
     }
     
     private int getNextFollowupId() {
         Cursor c = mDb.query(FOLLOWUPS_TABLE, new String[] {FOLLOWUPS_ID_COL}, "", null, null, null, ALARMS_ID_COL + " DESC", "1");
         if (c.moveToFirst()) {
             int result = c.getInt(0);
             nullSafeCloseCursor(c);
             return result;
         }
         return 1;
     }
     
     private void writeAlarmData(Alarm a) {
         mInsertAlarmQuery.bindLong(10, a.getId());
         mInsertAlarmQuery.bindLong(11, a.getHour());
         mInsertAlarmQuery.bindLong(12, a.getMinute());
         mInsertAlarmQuery.bindLong(13, a.getThreshold());
         mInsertAlarmQuery.bindLong(14, a.getDaysOfWeek());
        mInsertAlarmQuery.bindLong(15, a.getNumFollowups());
        mInsertAlarmQuery.bindLong(16, a.getIntervalStart());
        mInsertAlarmQuery.bindLong(17, a.getIntervalEnd());
        mInsertAlarmQuery.bindString(18, boolToIntString(a.getEnabled()));
         mInsertAlarmQuery.executeInsert();
     }
     
     private Alarm populateAlarmFromCursor(Cursor c) {
         Alarm result = new Alarm(c.getInt(0));
         result.setHour(c.getInt(1));
         result.setMinute(c.getInt(2));
         result.setThreshold(c.getInt(3));
         result.setDaysOfWeek(c.getInt(4));
        result.setNumFollowups(c.getInt(5));
        result.setIntervalStart(c.getInt(6));
        result.setEnabled(c.getInt(7) == 1);
         
         result.setFollowups(getFollowupsForAlarm(result));
         
         return result;
     }
     
     private void populateAndInsertFollowupFromCursor(Cursor c, HashMap<Integer, Long> m) {
         m.put(c.getInt(0), c.getLong(1));
     }
     
     private HashMap<Integer, Long> getFollowupsForAlarm(Alarm a) {
         HashMap<Integer, Long> result = new HashMap<Integer, Long>();
 
         Cursor c = mDb.query(FOLLOWUPS_TABLE, new String[] {FOLLOWUPS_ID_COL, FOLLOWUPS_TIME_COL}, "? = ?", 
                              new String[] {FOLLOWUPS_ALARM_COL, Integer.toString(a.getId())} , null, null, null);
         
         if (c.moveToFirst()) {
             do {
                 populateAndInsertFollowupFromCursor(c, result);
             } while (c.moveToNext());
         }
         
         return result;
     }
     
     private void saveNewFollowups(Alarm a) {
         int[] ids = new int[a.getNumFollowups()];
         for (int i = 0; i < a.getNumFollowups(); i++) {
             ids[i] = getNextFollowupId();
         }
         
         a.populateFollowups(ids);
         
         HashMap<Integer, Long> followups = a.getFollowups();
         
         mInsertFollowupQuery.bindLong(5, a.getId());
         
         for (int id : followups.keySet()) {
             Log.d(TAG, "Saving followup: ID: " + id + " TIME: " + followups.get(id));
             mInsertFollowupQuery.bindLong(4, id);
             mInsertFollowupQuery.bindLong(6, followups.get(id));
             mInsertFollowupQuery.executeInsert();
         }
     }
     
     private void setPowerNap(Alarm a) {
         mDb.delete(POWERNAP_TABLE, null, null);
         mInsertPowerNapQuery.bindLong(2, a.getId());
         mInsertPowerNapQuery.executeInsert();
     }
     
     private void setQuikAlarm(Alarm a) {
         mInsertQuikAlarmQuery.bindLong(2, a.getId());
         mInsertQuikAlarmQuery.executeInsert();
     }
 
     @Override
     public boolean alarmExists(int id) {
         return getAlarm(id) == null;
     }
     
     @Override
     public void deleteAlarm(Alarm a) {
         deleteAlarm(a.getId());
     }
 
     @Override
     public void deleteAlarm(int id) {
         String simpleEq = "? = ?";
         String idStr = Integer.toString(id);
         mDb.delete(FOLLOWUPS_TABLE, simpleEq, new String[] {FOLLOWUPS_ALARM_COL, idStr});
         mDb.delete(POWERNAP_TABLE, simpleEq, new String[] {POWERNAP_ID_COL, idStr});
         mDb.delete(QUIK_ALARMS_TABLE, simpleEq, new String[] {QUIK_ALARMS_ID_COL, idStr});
         mDb.delete(ALARMS_TABLE, simpleEq, new String[] {ALARMS_ID_COL, idStr});
     }
 
     @Override
     public Alarm saveAlarm(Alarm a) {
         if (a.getId() == -1) {
             a.setId(getNextAlarmId());
             saveNewFollowups(a);
         } else {
             deleteAlarm(a.getId());
         }
         
         if (a.isPowerNap()) {
             setPowerNap(a);
         } else if (a.isQuikAlarm()) {
             setQuikAlarm(a);
         }
         
         writeAlarmData(a);
         
         return null;
     }
 
     @Override
     public Alarm getAlarm(int id) {
         Alarm result = null;
         Cursor c = mDb.query(FOLLOWUPS_TABLE, ALARMS_COLS, "", null, null, null, "", "1");
         if (c.moveToFirst()) {
             result = populateAlarmFromCursor(c);
             nullSafeCloseCursor(c);
         }
         return result;
     }
 
     @Override
     public Alarm getPowerNap() {
         Alarm result = null;
         Cursor c = mDb.rawQuery("SELECT * FROM ?, ?", new String[] {POWERNAP_TABLE, ALARMS_TABLE});
         if (c.moveToFirst()) {
             result = populateAlarmFromCursor(c);
             nullSafeCloseCursor(c);
         }
         return result;
     }
 
     @Override
     public List<Alarm> getQuikAlarmsAndAlarms() {
         List<Alarm> result = new ArrayList<Alarm>();
         Cursor c = mDb.rawQuery("SELECT * FROM ?, ? WHERE ? != (SELECT ? FROM ? LIMIT 1)", 
                                 new String[] {QUIK_ALARMS_TABLE, ALARMS_TABLE, ALARMS_ID_COL, POWERNAP_ID_COL, POWERNAP_TABLE});
         if (c.moveToFirst()) {
             do {
                 result.add(populateAlarmFromCursor(c));
             } while (c.moveToNext());
         }
         Collections.sort(result);
         return result;
     }
 
     @Override
     public List<Alarm> getFullAlarmList() {
         return getFullAlarmList(-1);
     }
 
     @Override
     public List<Alarm> getFullAlarmList(int numItems) {
         List<Alarm> result = new ArrayList<Alarm>();
         Alarm powerNap = getPowerNap();
         
         if (powerNap != null) {
             result.add(powerNap);
         }
         
         result.addAll(getQuikAlarmsAndAlarms());
         
         return numItems == -1 ? result : result.subList(0, numItems);
     }
 
     @Override
     public void resetDatabase() {
         Log.d(TAG, "Recieved order to clear database. Faking an upgrade.");
         mOpenHelper.onUpgrade(mDb, -1, DATABASE_VERSION);
     }
     
     private static class OpenHelper extends SQLiteOpenHelper {
         
         OpenHelper(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
             
             Log.d(TAG, "Creating tables");
             
             /* Create alarms table */
             db.execSQL("CREATE TABLE ? (? INTEGER PRIMARY KEY, ? INTEGER, ? INTEGER, ? INTEGER, ? INTEGER, ? INTEGER, ? INTEGER, ? INTEGER, ? INTEGER",
                        new String[] {ALARMS_TABLE, ALARMS_ID_COL, ALARMS_HOUR_COL, ALARMS_MINUTE_COL, ALARMS_THRESHOLD_COL, ALARMS_DAYS_COL, 
                                      ALARMS_FOLLOWUPS_COL, ALARMS_INTERVAL_START_COL, ALARMS_INTERVAL_END_COL, ALARMS_ENABLED_COL});
 
             /* Create followups table */
             db.execSQL("CREATE TABLE ? (? INTEGER PRIMARY KEY, ? INTEGER, ? REAL, FOREIGN KEY(?) REFERENCES ?(?))",
                        new String[] {FOLLOWUPS_TABLE, FOLLOWUPS_ID_COL, FOLLOWUPS_ALARM_COL, FOLLOWUPS_TIME_COL, 
                                      FOLLOWUPS_ALARM_COL, ALARMS_TABLE, ALARMS_ID_COL});
             
             /* Create powernap table */
             db.execSQL("CREATE TABLE ? (? INTEGER PRIMARY KEY, FOREIGN KEY(?) REFERENCES ?(?))",
                        new String[] {POWERNAP_TABLE, POWERNAP_ID_COL, POWERNAP_ID_COL, ALARMS_TABLE, ALARMS_ID_COL});
             
             /* Create quik alarms table */
             db.execSQL("CREATE TABLE ? (? INTEGER PRIMARY KEY, FOREIGN KEY(?) REFERENCES ?(?))",
                        new String[] {QUIK_ALARMS_TABLE, QUIK_ALARMS_ID_COL, QUIK_ALARMS_ID_COL, ALARMS_TABLE, ALARMS_ID_COL});
             
             Log.d(TAG, "Finished creating tables");
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
             
             Log.d(TAG, "Upgrading database from Version " + oldVersion + " to Version " + newVersion);
             
             Log.d(TAG, "Deleting all tables");
             
             /* Delete all tables */
             for (String tableName : ALL_TABLES) {
                 db.execSQL("DELETE TABLE IF EXISTS ?", new String[] {tableName});
             }
             
             Log.d(TAG, "Finished deleting tables");
             
             /* Recreate all tables */
             onCreate(db);
             
             Log.d(TAG, "Upgrade complete");
         }
     }
 }
