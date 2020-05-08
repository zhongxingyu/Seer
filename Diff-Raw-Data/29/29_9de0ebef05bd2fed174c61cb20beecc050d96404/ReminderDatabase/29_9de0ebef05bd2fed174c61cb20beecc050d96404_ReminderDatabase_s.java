 package com.ksoft.android.dao;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 import com.ksoft.android.model.Location;
 import com.ksoft.android.model.Reminder;
 
 import java.sql.SQLException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Catalin Samarghitan
  * Date: 4/20/13
  * Time: 7:02 PM
  */
 public class ReminderDatabase{
 
     private DatabaseHelper dbHelper;
 
     private SQLiteDatabase database;
 
     /**
      *
      * @param context
      */
     public ReminderDatabase(Context context){
         debug("Building new Reminder Database");
 
         dbHelper = new DatabaseHelper(context);
         database = dbHelper.getWritableDatabase();
     }
 
 
     public long createReminder(Reminder reminder) {
         debug("Creating reminder : " + reminder);
         ContentValues values = new ContentValues();
 //        values.put(Reminder._ID, reminder.getId());
         values.put(Reminder.REMINDER_TEXT, reminder.getText());
         values.put(Reminder.REMINDER_TIME, reminder.getReminderTime().getTime());
         values.put(Reminder.TYPE, reminder.getType().ordinal());
 
         if (reminder.getLocation() != null) {
             debug("We have a location that needs to be linked with something from the DB or a new one created! Location = " + reminder.getLocation());
             Long locationId;
             // we have two options here, the location already exists
             Location location = getLocation(reminder.getLocation().getName());
             if (location != null) {
                 debug("The location already exists, returning the ID!");
                 locationId = location.getId();
             } else {// this is a new location
                 // save it
                 debug("Saving a new Location : " + reminder.getLocation());
                 locationId = createLocation(reminder.getLocation());
             }
 
             values.put(Reminder.LOCATION_ID, locationId);
         }
 
         return database.insert(Reminder.REMINDER_TABLE_NAME, null, values);
     }
 
     private Location getLocation(String name) {
         debug("Trying to get a Location from the DB for the name : " + name);
         Location location = null;
         String[] cols = new String[] {Location._ID, Location.COLUMN_NAME, Location.COLUMN_WIFI_NAME};
 
         Cursor mCursor = database.query(Location.LOCATION_TABLE_NAME, cols, Location.COLUMN_NAME + " = ? ", new String[]{name}, null, null, null );
         if (mCursor != null) {
             location = new Location();
             location.setId(mCursor.getLong(1));
             location.setName(mCursor.getString(2));
             location.setWifiName(mCursor.getString(3));
 
             debug("Location found : " + location);
         }
 
         return location;
     }
 
     public long createLocation(Location location) {
         debug("Creating a new Location : " + location);
         ContentValues values = new ContentValues();
         values.put(Location.COLUMN_NAME, location.getName());
         values.put(Location.COLUMN_WIFI_NAME, location.getWifiName());
 
         return database.insert(Location.LOCATION_TABLE_NAME, null, values);
     }
 
     public Cursor selectReminders() {
        String[] cols = new String[] {Reminder._ID, Reminder.REMINDER_TEXT, Reminder.REMINDER_TIME, Reminder.TYPE, Reminder.LOCATION_ID};
         Cursor mCursor = database.query(Reminder.REMINDER_TABLE_NAME,cols,null
                 , null, null, null, null, null);
         if (mCursor != null) {
             mCursor.moveToFirst();
         }
         return mCursor; // iterate to get each value.
     }
 
     public void close() {
         if (dbHelper != null) {
             dbHelper.close();
         }
     }
 
     private void debug(String msg) {
         Log.d(this.getClass().getName(), msg);
     }
 }
