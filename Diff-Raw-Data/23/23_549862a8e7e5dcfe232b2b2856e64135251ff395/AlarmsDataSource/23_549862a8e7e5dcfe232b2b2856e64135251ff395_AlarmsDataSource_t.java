 package me.ingeniando.conditionalalarm;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.util.Log;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 
 public class AlarmsDataSource {
 
   // Database fields
   private SQLiteDatabase database;
   private MySQLiteHelper dbHelper;
   private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
       MySQLiteHelper.COLUMN_ALARM_TEXT, MySQLiteHelper.COLUMN_HOUR,
       MySQLiteHelper.COLUMN_MINUTES, MySQLiteHelper.COLUMN_AM,
       MySQLiteHelper.COLUMN_ACTIVE };
 
   public AlarmsDataSource(Context context) {
     dbHelper = new MySQLiteHelper(context);
   }
 
   public void open() throws SQLException {
     database = dbHelper.getWritableDatabase();
   }
 
   public void close() {
     dbHelper.close();
   }
 
   public Alarm createAlarm(String alarm_text, int hour, int minutes, int AM, int active) {
     ContentValues values = new ContentValues();
     values.put(MySQLiteHelper.COLUMN_ALARM_TEXT, alarm_text);
     values.put(MySQLiteHelper.COLUMN_HOUR, hour);
     values.put(MySQLiteHelper.COLUMN_MINUTES, minutes);
     values.put(MySQLiteHelper.COLUMN_AM, AM);
     values.put(MySQLiteHelper.COLUMN_ACTIVE, active);
     long insertId = database.insert(MySQLiteHelper.TABLE_ALARMS, null,
         values);
     Cursor cursor = database.query(MySQLiteHelper.TABLE_ALARMS,
         allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
         null, null, null);
     cursor.moveToFirst();
     Alarm newAlarm = cursorToAlarm(cursor);
     cursor.close();
     return newAlarm;
   }
 
   public void deleteAlarm(Alarm alarm) {
     long id = alarm.getId();
     System.out.println("Alarm deleted with id: " + id);
     database.delete(MySQLiteHelper.TABLE_ALARMS, MySQLiteHelper.COLUMN_ID
         + " = " + id, null);
   }
 
   public ArrayList<Alarm> getAllAlarms() {
     ArrayList<Alarm> alarms = new ArrayList<Alarm>();
 
     Cursor cursor = database.query(MySQLiteHelper.TABLE_ALARMS,
         allColumns, null, null, null, null, null);
 
     cursor.moveToFirst();
     while (!cursor.isAfterLast()) {
       Alarm alarm = cursorToAlarm(cursor);
       alarms.add(alarm);
       cursor.moveToNext();
     }
     // Make sure to close the cursor
     cursor.close();
     return alarms;
   }
   
   public Alarm getAlarmById(String alarmId){
 	  //String ID = String.valueOf(alarmId);
 	  Log.w("getAlarmById: ", alarmId);
 	  Cursor cursor = database.rawQuery("select _id, alarm_text, hour, minutes, AM, active from alarms where _id = ?", new String[] {alarmId});
 	  cursor.moveToFirst();
 	  Alarm alarm = cursorToAlarm(cursor);
 	  Log.w("CUSTOM", alarm.toString());
 	  return alarm;
   }
   
  public Alarm getLastAlarmById(){
	  Cursor cursor = database.rawQuery("select _id, alarm_text, hour, minutes, AM, active from alarms where _id = (select max(_id) from alarms)", null);
	  cursor.moveToFirst();
	  Alarm alarm = cursorToAlarm(cursor);
	  return alarm;
  }
  
   private Alarm cursorToAlarm(Cursor cursor) {
     Alarm alarm = new Alarm();
     alarm.setId(cursor.getLong(0));
     alarm.setAlarm_text(cursor.getString(1));
     alarm.setHour(cursor.getInt(2));
     alarm.setMinutes(cursor.getInt(3));
     alarm.setAM(cursor.getInt(4));
     alarm.setActive(cursor.getInt(5));
     return alarm;
   }
 } 
