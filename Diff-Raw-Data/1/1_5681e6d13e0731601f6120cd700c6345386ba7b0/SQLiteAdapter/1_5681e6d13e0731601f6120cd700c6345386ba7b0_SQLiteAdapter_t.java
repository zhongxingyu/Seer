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
         mInsertAlarmQuery.bindLong(15, a.getIntervalStart());
         mInsertAlarmQuery.bindLong(16, a.getIntervalEnd());
         mInsertAlarmQuery.bindString(17, boolToIntString(a.getEnabled()));
         mInsertAlarmQuery.executeInsert();
     }
     
     private Alarm populateAlarmFromCursor(Cursor c) {
         Alarm result = new Alarm(c.getInt(0));
         result.setHour(c.getInt(1));
         result.setMinute(c.getInt(2));
         result.setThreshold(c.getInt(3));
         result.setDaysOfWeek(c.getInt(4));
         result.setIntervalStart(c.getInt(5));
         result.setEnabled(c.getInt(6) == 1);
         
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
 
     @Override
     public boolean alarmExists(int id) {
         return getAlarm(id) == null;
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
         
         // TODO check for powernap, quikalarm
         
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
 
 //private static class OpenHelper extends SQLiteOpenHelper {
 //    58   
 //    59        OpenHelper(Context context) {
 //    60           super(context, DATABASE_NAME, null, DATABASE_VERSION);
 //    61        }
 //    62   
 //    63        @Override
 //    64        public void onCreate(SQLiteDatabase db) {
 //    65           db.execSQL("CREATE TABLE " + TABLE_NAME + "
 //    66            (id INTEGER PRIMARY KEY, name TEXT)");
 //    67        }
 //    68   
 //    69        @Override
 //    70        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 //    71           Log.w("Example", "Upgrading database, this will drop tables and recreate.");
 //    72           db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
 //    73           onCreate(db);
 //    74        }
 
 
 //public class SQLiteAdapter extends SQLiteOpenHelper {
 //
 //    private static final int DATABASE_VERSION = 4;
 //    private static final String DATABASE_NAME = "fortywinks";
 //
 //    private SQLiteDatabase mDb;
 //
 //    public SQLiteAdapter(Context context) {
 //        super(context, DATABASE_NAME, null, DATABASE_VERSION);
 //        mDb = getWritableDatabase();
 //    }
 //
 //    private int getNextAlarmId() {
 //        String query = "SELECT id FROM alarms ORDER BY id DESC";
 //        Cursor cursor = mDb.rawQuery(query, null);
 //        int result = -1;
 //
 //        if (cursor.moveToFirst()) {
 //            result = cursor.getInt(0);
 //        } else {
 //            result = 1;
 //        }
 //
 //        if (cursor != null && !cursor.isClosed()) {
 //            cursor.close();
 //        }
 //
 //        return result;
 //    }
 //
 //    private int[] getNextFollowupIds(int n) {
 //        int[] result = new int[n];
 //
 //        String query = "SELECT id FROM followups ORDER BY id DESC";
 //        Cursor cursor = mDb.rawQuery(query, null);
 //
 //        int seed = 1;
 //        if (cursor.moveToFirst()) {
 //            seed = cursor.getInt(0);
 //        }
 //
 //        for (int i = 0; i < n; i++) {
 //            result[i] = ++seed;
 //        }
 //
 //        if (cursor != null && !cursor.isClosed()) {
 //            cursor.close();
 //        }
 //
 //        return result;
 //    }
 //
 //    /**
 //     * Populate the alarm object if it needs it
 //     * 
 //     * @param alarm
 //     * @return whether or not we're going to be creating a new one
 //     */
 //    private boolean populateAlarm(Alarm alarm) {
 //        if (alarm.getId() == -1) {
 //            alarm.setId(getNextAlarmId());
 //            alarm.populateFollowups(getNextFollowupIds(alarm.getNumFollowups()));
 //            return true;
 //        }
 //        return false;
 //    }
 //
 //    /**
 //     * Save the given alarm to the Database. If it does not have an ID, it will
 //     * be given one, otherwise, its values will override the previous values,
 //     * effectively giving this query add/edit privilages.
 //     * 
 //     * @param alarm
 //     *            The alarm to add to the database.
 //     */
 //    public void saveAlarm(Alarm alarm) {
 //        if (populateAlarm(alarm)) {
 //            SQLiteStatement fs = mDb.compileStatement("INSERT INTO followups VALUES(?, ?, ?)");
 //            fs.bindString(2, alarm.getId() + ""); // our alarm object id will remain
 //                                           // constant
 //
 //            HashMap<Integer, Long> followups = alarm.getFollowups();
 //
 //            /* Insert all of our new followups */
 //            for (int id : followups.keySet()) {
 //                Log.i("40W", "Inserting Followup: ID: " + id + " ALARM: " + alarm.getId() + " TIME: " + followups.get(id));
 //                fs.bindString(1, id + "");
 //                fs.bindLong(3, followups.get(id));
 //                fs.executeInsert();
 //            }
 //
 //        } else {
 //            mDb.delete("alarms", "id = ?", new String[] { alarm.getId() + "" });
 //        }
 //
 //        if (alarm.isPowerNap()) {
 //            Log.w("40W", "40W Replacing Power Nap");
 //            /* Delete the current power nap */
 //            Alarm currentPowerNap = getPowerNap();
 //            if (currentPowerNap != null) {
 //                mDb.delete("alarms", "id = ?", new String[] { currentPowerNap.getId() + "" });
 //            }
 //            mDb.delete("powernap", "", null);
 //
 //            SQLiteStatement ps = mDb.compileStatement("INSERT INTO powernap VALUES(?)");
 //            ps.bindString(1, alarm.getId() + "");
 //            ps.executeInsert();
 //        } else if (alarm.isQuikAlarm()) {
 //            mDb.delete("quikalarms", "id = ?", new String[] { alarm.getId() + "" });
 //            SQLiteStatement qs = mDb.compileStatement("INSERT INTO quikalarms VALUES(?)");
 //            qs.bindString(1, alarm.getId() + "");
 //            qs.executeInsert();
 //        }
 //
 //        SQLiteStatement s = mDb.compileStatement("INSERT INTO alarms (id, hour, minute, threshold, days_of_week, followups, interval_start, interval_end, enabled) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");
 //        s.bindString(1, alarm.getId() + "");
 //        s.bindString(2, alarm.getHour() + "");
 //        s.bindString(3, alarm.getMinute() + "");
 //        s.bindString(4, alarm.getThreshold() + "");
 //        s.bindString(5, alarm.getDaysOfWeek() + "");
 //        s.bindString(6, alarm.getNumFollowups() + "");
 //        s.bindString(7, alarm.getIntervalStart() + "");
 //        s.bindString(8, alarm.getIntervalEnd() + "");
 //        s.bindString(9, (alarm.getEnabled() ? 1 : 0) + "");
 //        s.executeInsert();
 //        Log.w("40W", "40W: Alarm Saved - ID: " + alarm.getId() + ", Time:" + alarm);
 //    }
 //
 //    public void deleteAlarm(int id) {
 //        String[] args = new String[] {id + ""};
 //        mDb.delete("followups", "alarm = ?", args);
 //        mDb.delete("powernap", "id = ?", args);
 //        mDb.delete("quikalarms", "id = ?", args);
 //        mDb.delete("alarms", "id = ?", args);
 //
 //    }
 //
 //    public boolean alarmExists(int id) {
 //        String query = "SELECT id FROM alarms WHERE id = ?";
 //        Cursor cursor = mDb.rawQuery(query, new String[] { id + "" });
 //        return cursor.moveToFirst();
 //    }
 //
 //    /**
 //     * Get a List of all the Alarms in the database.
 //     * 
 //     * @return a List of all the Alarms in ArrayList form.
 //     */
 //    public List<Alarm> listAlarms() {
 //        List<Alarm> result = new ArrayList<Alarm>();
 //        String query = "SELECT * FROM alarms";
 //        Cursor cursor = mDb.rawQuery(query, null);
 //
 //        if (cursor.moveToFirst()) {
 //            do {
 //
 //                /* Create and populate each alarm */
 //                Alarm next = new Alarm(cursor.getInt(0));
 //                next.setHour(cursor.getInt(1));
 //                next.setMinute(cursor.getInt(2));
 //                next.setThreshold(cursor.getInt(3));
 //                next.setDaysOfWeek(cursor.getInt(4));
 //                next.setNumFollowups(cursor.getInt(5));
 //                next.setIntervalStart(cursor.getInt(6));
 //                next.setIntervalEnd(cursor.getInt(7));
 //                next.setEnabled(cursor.getInt(8) == 1);
 //                result.add(next);
 //
 //            } while (cursor.moveToNext());
 //        }
 //
 //        if (cursor != null && !cursor.isClosed()) {
 //            cursor.close();
 //        }
 //
 //        return result;
 //    }
 //
 //    /**
 //     * Returns the most recently set PowerNap.
 //     * 
 //     * @return the most recent PowerNap.
 //     */
 //    public Alarm getPowerNap() {
 //        Alarm result;
 //        String query = "SELECT * FROM alarms, powernap";
 //        Cursor cursor = mDb.rawQuery(query, null);
 //
 //        if (cursor.moveToFirst()) {
 //
 //            /* Populate alarm object */
 //            int newId = cursor.getInt(0);
 //            result = new Alarm(newId);
 //            result.setHour(cursor.getInt(1));
 //            result.setMinute(cursor.getInt(2));
 //            result.setThreshold(cursor.getInt(3));
 //            result.setDaysOfWeek(cursor.getInt(4));
 //            result.setNumFollowups(cursor.getInt(5));
 //            result.setIntervalStart(cursor.getInt(6));
 //            result.setIntervalEnd(cursor.getInt(7));
 //            result.setEnabled(cursor.getInt(8) == 1);
 //            
 //            /* Populate followups */
 //            
 //            String followupQuery = "SELECT id, time FROM followups WHERE alarm = ?";
 //            Cursor followupCursor = mDb.rawQuery(followupQuery, new String[] {cursor.getInt(0) + "" });
 //            
 //            HashMap<Integer, Long> followups = new HashMap<Integer, Long>();
 //            
 //            Log.i("40W", "Looking for followups in the database for alarm " + newId);
 //            if (followupCursor.moveToFirst()) {
 //                do {
 //                    Log.i("40W", "Found a followup in the database");
 //                    followups.put(followupCursor.getInt(0), followupCursor.getLong(1));
 //                } while (followupCursor.moveToNext());
 //            }
 //            
 //            result.setFollowups(followups);
 //            
 //        } else {
 //
 //            /* We didn't find that one */
 //            result = null;
 //        }
 //
 //        if (cursor != null && !cursor.isClosed()) {
 //            cursor.close();
 //        }
 //        
 //        return result;
 //    }
 //    
 //    public List<Alarm> getQuickAlarmsAndAlarms() {
 //        List<Alarm> result = new ArrayList<Alarm>();
 //        
 //        String query = "SELECT * FROM alarms, quikalarms";
 //        Cursor cursor = mDb.rawQuery(query, null);
 //
 //        if (cursor.moveToFirst()) {
 //            do {
 //
 //                /* Create and populate each alarm */
 //                Alarm next = new Alarm(cursor.getInt(0));
 //                next.setHour(cursor.getInt(1));
 //                next.setMinute(cursor.getInt(2));
 //                next.setThreshold(cursor.getInt(3));
 //                next.setDaysOfWeek(cursor.getInt(4));
 //                next.setNumFollowups(cursor.getInt(5));
 //                next.setIntervalStart(cursor.getInt(6));
 //                next.setIntervalEnd(cursor.getInt(7));
 //                next.setEnabled(cursor.getInt(8) == 1);
 //                result.add(next);
 //
 //            } while (cursor.moveToNext());
 //        }
 //
 //        if (cursor != null && !cursor.isClosed()) {
 //            cursor.close();
 //        }
 //        
 //        Collections.sort(result);
 //        return result;
 //    }
 //    
 //    public List<Alarm> getFullAlarmList(int numEntries) {
 //        List<Alarm> result = new ArrayList<Alarm>();
 //        Alarm powerNap = getPowerNap();
 //        if (powerNap != null) {
 //            result.add(powerNap);
 //        }
 //        result.addAll(getQuickAlarmsAndAlarms());
 //        return result.subList(0, numEntries);
 //    }
 //
 //    /**
 //     * Retrieves an alarm from the database with the given ID.
 //     * 
 //     * @param id
 //     *            The ID of the alarm to retrieve
 //     * @return An alarm with all quantities specified
 //     */
 //    public Alarm getAlarm(int id) {
 //        Alarm result = new Alarm(id);
 //        String query = "SELECT * FROM alarms WHERE id = ?";
 //        Cursor cursor = mDb.rawQuery(query, new String[] { id + "" });
 //
 //        if (cursor.moveToFirst()) {
 //
 //            /* Populate alarm object */
 //            result.setHour(cursor.getInt(1));
 //            result.setMinute(cursor.getInt(2));
 //            result.setThreshold(cursor.getInt(3));
 //            result.setDaysOfWeek(cursor.getInt(4));
 //            result.setNumFollowups(cursor.getInt(5));
 //            result.setIntervalStart(cursor.getInt(6));
 //            result.setIntervalEnd(cursor.getInt(7));
 //            result.setEnabled(cursor.getInt(8) == 1);
 //
 //        } else {
 //
 //            /* We didn't find that one */
 //            result = null;
 //        }
 //
 //        if (cursor != null && !cursor.isClosed()) {
 //            cursor.close();
 //        }
 //
 //        return result;
 //    }
 //
 //    public void smokeDatabase() {
 //        onUpgrade(mDb, 0, DATABASE_VERSION);
 //    }
 //
 //    @Override
 //    public void onCreate(SQLiteDatabase db) {
 //        db.execSQL("CREATE TABLE alarms (id INTEGER PRIMARY KEY AUTOINCREMENT, hour INTEGER, minute INTEGER, threshold INTEGER, days_of_week INTEGER, followups INTEGER, interval_start INTEGER, interval_end INTEGER, enabled INTEGER);");
 //        db.execSQL("CREATE TABLE followups (id INTEGER PRIMARY KEY, alarm INTEGER, time REAL, FOREIGN KEY(alarm) REFERENCES alarms(id))");
 //        db.execSQL("CREATE TABLE powernap (id INTEGER PRIMARY KEY, FOREIGN KEY(id) REFERENCES alarms(id))");
 //        db.execSQL("CREATE TABLE quikalarms (id INTEGER PRIMARY KEY, FOREIGN KEY(id) REFERENCES alarms(id))");
 //    }
 //
 //    @Override
 //    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 //        db.execSQL("DROP TABLE IF EXISTS alarms");
 //        db.execSQL("DROP TABLE IF EXISTS followups");
 //        db.execSQL("DROP TABLE IF EXISTS powernap");
 //        db.execSQL("DROP TABLE IF EXISTS quikalarms");
 //        onCreate(db);
 //    }
 //
 //}
