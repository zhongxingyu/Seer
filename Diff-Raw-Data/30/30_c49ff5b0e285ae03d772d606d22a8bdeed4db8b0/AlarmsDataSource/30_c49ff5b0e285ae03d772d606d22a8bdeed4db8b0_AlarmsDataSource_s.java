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
       MySQLiteHelper.COLUMN_ALARM_NAME };
 
   public AlarmsDataSource(Context context) {
     dbHelper = new MySQLiteHelper(context);
   }
 
   public void open() throws SQLException {
     database = dbHelper.getWritableDatabase();
   }
 
   public void close() {
     dbHelper.close();
   }
 
   public Alarm createAlarm(String alarm_name) {
     ContentValues values = new ContentValues();
     values.put(MySQLiteHelper.COLUMN_ALARM_NAME, alarm_name);
     long insertId = database.insert(MySQLiteHelper.TABLE_ALARMS, null,
         values);
     Cursor cursor = database.query(MySQLiteHelper.TABLE_ALARMS,
         allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
         null, null, null);
     cursor.moveToFirst();
     Alarm newComment = cursorToAlarm(cursor);
     cursor.close();
     return newComment;
   }
 
   public void deleteAlarm(Alarm alarm) {
     long id = alarm.getId();
     System.out.println("Comment deleted with id: " + id);
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
	  Log.w("CUSTOM", alarmId);
	  Cursor cursor = database.rawQuery("select alarm_name from alarms where id = ?", new String[] {alarmId});
 	  Alarm alarm = cursorToAlarm(cursor);
 	  Log.w("CUSTOM", alarm.toString());
 	  return alarm;
   }
   
 
   private Alarm cursorToAlarm(Cursor cursor) {
     Alarm alarm = new Alarm();
     alarm.setId(cursor.getLong(0));
     alarm.setAlarmName(cursor.getString(1));
     return alarm;
   }
 } 
