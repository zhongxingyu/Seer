 package org.bxmy.shiftclock.db;
 
 import java.util.ArrayList;
 
 import org.bxmy.shiftclock.shiftduty.Watch;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 
 public class WatchTable extends DBHelper.ITableBase {
 
     private String mTableName = "watch";
 
     @Override
     public String getTableName() {
         return mTableName;
     }
 
     @Override
     public void onUpgrade(int oldVersion, int newVersion) {
         final int CURRENT_VERSION = 4;
         ArrayList<Watch> watches = null;
         try {
             if (oldVersion < CURRENT_VERSION)
                 watches = upgradeFrom(oldVersion);
             else
                 watches = upgradeFrom(CURRENT_VERSION);
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         mDb.recreateTable(this);
         if (watches != null)
             rebuildTable(watches);
     }
 
     @Override
     public String getCreateSQL() {
         return "create table " + getTableName()
                 + " (_id integer primary key autoincrement, "
                 + " dutyid integer not null, " + " day bigint not null, "
                 + " duration integer not null, " + " before integer not null, "
                 + " after integer not null, "
                 + " alarmStopped integer not null, "
                 + " alarmPaused bigint not null)";
     }
 
     @Override
     public String[] getAllFields() {
         return new String[] { "_id", "dutyid", "day", "duration", "before",
                 "after", "alarmStopped", "alarmPaused" };
     }
 
     public ArrayList<Watch> selectAll() {
         ArrayList<Watch> watches = new ArrayList<Watch>();
         if (mDb != null) {
             Cursor cursor = mDb.cursorListAll(this);
             cursor.moveToFirst();
 
             while (!cursor.isAfterLast()) {
                 int id = cursor.getInt(0);
                 int dutyId = cursor.getInt(1);
                 long day = cursor.getLong(2);
                 int duration = cursor.getInt(3);
                 int before = cursor.getInt(4);
                 int after = cursor.getInt(5);
                 int alarmStopped = cursor.getInt(6);
                 long alarmPaused = cursor.getLong(7);
 
                 try {
                     Watch watch = new Watch(id, dutyId, day, duration, before,
                             after, alarmStopped, alarmPaused);
                     watches.add(watch);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
 
                 cursor.moveToNext();
             }
         }
 
         return watches;
     }
 
     public Watch insert(int dutyId, long dayInSeconds, int durationSeconds,
             int beforeSeconds, int afterSeconds) {
         ContentValues values = new ContentValues();
         values.put("dutyid", dutyId);
         values.put("day", dayInSeconds);
         values.put("duration", durationSeconds);
         values.put("before", beforeSeconds);
         values.put("after", afterSeconds);
 
         int id = this.mDb.insert(this, values);
         return new Watch(id, dutyId, dayInSeconds, durationSeconds,
                 beforeSeconds, afterSeconds, 0, 0);
     }
 
     public void update(Watch watch) {
         ContentValues values = new ContentValues();
         values.put("dutyid", watch.getDutyId());
         values.put("day", watch.getDayInSeconds());
         values.put("duration", watch.getDutyDurationSeconds());
         values.put("before", watch.getBeforeSeconds());
         values.put("after", watch.getAfterSeconds());
         values.put("alarmStopped", watch.getAlarmStopped());
         values.put("alarmPaused", watch.getAlarmPausedInSeconds());
 
         String where = "_id=?";
         String[] whereArgs = new String[] { String.valueOf(watch.getId()) };
         this.mDb.update(this, values, where, whereArgs);
     }
 
     private void rebuildTable(ArrayList<Watch> watches) {
         ArrayList<ContentValues> watchValues = new ArrayList<ContentValues>();
         for (int i = 0; i < watches.size(); ++i) {
             Watch watch = watches.get(i);
 
             ContentValues values = new ContentValues();
             values.put("_id", watch.getId());
             values.put("dutyid", watch.getDutyId());
             values.put("day", watch.getDayInSeconds());
             values.put("duration", watch.getDutyDurationSeconds());
             values.put("before", watch.getBeforeSeconds());
             values.put("after", watch.getAfterSeconds());
             values.put("alarmStopped", watch.getAlarmStopped());
             values.put("alarmPaused", watch.getAlarmPausedInSeconds());
 
             watchValues.add(values);
         }
 
         mDb.rebuildTable(this, watchValues);
     }
 
     private ArrayList<Watch> upgradeFrom(int oldVersion) {
         // list from newly version to oldest version
         if (oldVersion >= 0x70000000) {
             return null;
         } else if (oldVersion >= 4) {
             String[] fields = new String[] { "_id", // integer primary key
                     "dutyid", // integer
                     "day", // bigint
                     "duration", // integer
                     "before", // integer
                     "after", // integer
                     "alarmStopped", // integer
                     "alarmPaused", // bigint
             };
 
             DBHelper.IDbObjectCreator<Watch> creator = new DBHelper.IDbObjectCreator<Watch>() {
 
                 @Override
                 public Watch create(Cursor cursor) {
                     int id = cursor.getInt(0);
                     int dutyId = cursor.getInt(1);
                     long day = cursor.getLong(2);
                     int duration = cursor.getInt(3);
                     int before = cursor.getInt(4);
                     int after = cursor.getInt(5);
                     int alarmStopped = cursor.getInt(6);
                     long alarmPaused = cursor.getLong(7);
 
                     return new Watch(id, dutyId, day, duration, before, after,
                             alarmStopped, alarmPaused);
                 }
             };
 
             return mDb.new UpgradeHelper<Watch>(getTableName(), fields, creator)
                     .selectAll();
         } else if (oldVersion >= 2) {
             String[] fields = new String[] { "_id", // integer primary key
                     "dutyid", // integer
                     "day", // bigint
                     "before", // integer
                     "after", // integer
             };
 
             DBHelper.IDbObjectCreator<Watch> creator = new DBHelper.IDbObjectCreator<Watch>() {
 
                 @Override
                 public Watch create(Cursor cursor) {
                     int id = cursor.getInt(0);
                     int dutyId = cursor.getInt(1);
                     long day = cursor.getLong(2);
                     int before = cursor.getInt(3);
                     int after = cursor.getInt(4);
 
                     return new Watch(id, dutyId, day, 0, before, after, 0, 0);
                 }
             };
 
             return mDb.new UpgradeHelper<Watch>(getTableName(), fields, creator)
                     .selectAll();
         } else {
             return null;
         }
     }
 }
