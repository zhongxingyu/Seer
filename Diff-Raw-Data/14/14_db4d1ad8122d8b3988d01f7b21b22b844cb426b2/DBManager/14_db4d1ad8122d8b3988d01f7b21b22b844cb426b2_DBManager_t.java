 package edu.upenn.cis573.database;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import edu.upenn.cis573.StudySpace;
 import edu.upenn.cis573.datastructure.Room;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 public class DBManager {
 
     private static DBHelper helper;
     private static SQLiteDatabase db;
 
     private static int MAX_NOTE_SIZE = 5; //max number of characters
     private static int MAX_ENTRY_NUM = 3; //max number of history entries
 
     public static void initDB(Context context) {
         helper = new DBHelper(context);         
         db = helper.getWritableDatabase(); 
 
     }
     
 
     /**
      * Adds new StudySpace entry to the database.
      * @param history New StudySpace entry to be added.
      */
     public static int add(StudySpace history) {
 
         ContentValues values = new ContentValues();
         values.put(DBHelper.COLUMN_NAME_STARTMIN,          history.getStartMin());
         values.put(DBHelper.COLUMN_NAME_ENDMIN,          history.getEndMin());
         values.put(DBHelper.COLUMN_NAME_STARTHOUR,          history.getStartHour());
         values.put(DBHelper.COLUMN_NAME_ENDHOUR,          history.getEndHour());
         values.put(DBHelper.COLUMN_NAME_STARTDATE, history.getStartDate());
         values.put(DBHelper.COLUMN_NAME_ENDDATE, history.getEndDate());
         values.put(DBHelper.COLUMN_NAME_MONTH,          history.getMonth());
         values.put(DBHelper.COLUMN_NAME_YEAR,          history.getYear());
         values.put(DBHelper.COLUMN_NAME_GROUPSIZE,          history.getGroupSize());
 
         values.put(DBHelper.COLUMN_NAME_BUILDINGNAME,  history.getBuildingName());
         values.put(DBHelper.COLUMN_NAME_SPACENAME,     history.getSpaceName());
         values.put(DBHelper.COLUMN_NAME_LATITUDE,      history.getSpaceLatitude());
         values.put(DBHelper.COLUMN_NAME_LONGITUDE,     history.getSpaceLongitude());
         values.put(DBHelper.COLUMN_NAME_ROOMNUM,       history.getNumberOfRooms());
         values.put(DBHelper.COLUMN_NAME_MAXOCCUPANCY,  history.getMaximumOccupancy());
         values.put(DBHelper.COLUMN_NAME_HASWHITEBOARD, history.hasWhiteboard());
         values.put(DBHelper.COLUMN_NAME_PRIVACY,       history.getPrivacy());
         values.put(DBHelper.COLUMN_NAME_HASCOMPUTER,   history.hasComputer());
         values.put(DBHelper.COLUMN_NAME_RESERVETYPE,   history.getReserveType());
         values.put(DBHelper.COLUMN_NAME_HASBIGSCREEN,  history.has_big_screen());
         values.put(DBHelper.COLUMN_NAME_COMMENTS,      history.getComments());
         values.put(DBHelper.COLUMN_NAME_ROOMNAME,      history.getRooms()[0].getRoomName());
         values.put(DBHelper.COLUMN_NAME_NOTE,      "");
 
         // Insert the new row, returning the primary key value of the new row
         if(size() >= MAX_ENTRY_NUM) {
             Log.i("DATABASE", "Database is full."); 
             return -1;
         }else {
             db.insert(DBHelper.TABLE_NAME, null, values);
             Log.i("Database", "Entry written to database: building name is:" + 
                     history.getBuildingName() + "space name is: " + history.getSpaceName());
             return 1;
         }
     }  
 
 
 
     public static int updateDb(String noteText){  
 
         Cursor c = queryTheCursor(true);
         c.moveToFirst();
 
 
         ContentValues values = new ContentValues();
 
         values.put(DBHelper.COLUMN_NAME_STARTMIN,          c.getLong(c.getColumnIndex(DBHelper.COLUMN_NAME_STARTMIN)));
         values.put(DBHelper.COLUMN_NAME_ENDMIN,          c.getLong(c.getColumnIndex(DBHelper.COLUMN_NAME_ENDMIN)));
         values.put(DBHelper.COLUMN_NAME_STARTHOUR,          c.getLong(c.getColumnIndex(DBHelper.COLUMN_NAME_STARTHOUR)));
         values.put(DBHelper.COLUMN_NAME_ENDHOUR,          c.getLong(c.getColumnIndex(DBHelper.COLUMN_NAME_ENDHOUR)));
         values.put(DBHelper.COLUMN_NAME_STARTDATE,          c.getLong(c.getColumnIndex(DBHelper.COLUMN_NAME_STARTDATE)));
         values.put(DBHelper.COLUMN_NAME_ENDDATE,          c.getLong(c.getColumnIndex(DBHelper.COLUMN_NAME_ENDDATE)));
         values.put(DBHelper.COLUMN_NAME_MONTH,          c.getLong(c.getColumnIndex(DBHelper.COLUMN_NAME_MONTH)));
         values.put(DBHelper.COLUMN_NAME_YEAR,          c.getLong(c.getColumnIndex(DBHelper.COLUMN_NAME_YEAR)));
         values.put(DBHelper.COLUMN_NAME_GROUPSIZE,          c.getLong(c.getColumnIndex(DBHelper.COLUMN_NAME_GROUPSIZE)));
 
         values.put(DBHelper.COLUMN_NAME_BUILDINGNAME,  c.getString(c.getColumnIndex(DBHelper.COLUMN_NAME_BUILDINGNAME)));
         Log.i("building name is:",  c.getString(c.getColumnIndex(DBHelper.COLUMN_NAME_BUILDINGNAME)));
         values.put(DBHelper.COLUMN_NAME_SPACENAME,     c.getString(c.getColumnIndex(DBHelper.COLUMN_NAME_SPACENAME)));
         values.put(DBHelper.COLUMN_NAME_LATITUDE,      c.getDouble(c.getColumnIndex(DBHelper.COLUMN_NAME_LATITUDE)));
         values.put(DBHelper.COLUMN_NAME_LONGITUDE,     c.getDouble(c.getColumnIndex(DBHelper.COLUMN_NAME_LONGITUDE)));
         values.put(DBHelper.COLUMN_NAME_ROOMNUM,       c.getInt(c.getColumnIndex(DBHelper.COLUMN_NAME_ROOMNUM)));
         values.put(DBHelper.COLUMN_NAME_MAXOCCUPANCY,  c.getInt(c.getColumnIndex(DBHelper.COLUMN_NAME_MAXOCCUPANCY)));
         values.put(DBHelper.COLUMN_NAME_HASWHITEBOARD, parseBoolean(c.getInt(c.getColumnIndex(DBHelper.COLUMN_NAME_HASWHITEBOARD))));
         values.put(DBHelper.COLUMN_NAME_PRIVACY,       c.getString(c.getColumnIndex(DBHelper.COLUMN_NAME_PRIVACY)));
         values.put(DBHelper.COLUMN_NAME_HASCOMPUTER,   parseBoolean(c.getInt(c.getColumnIndex(DBHelper.COLUMN_NAME_HASCOMPUTER))));
         values.put(DBHelper.COLUMN_NAME_RESERVETYPE,   c.getString(c.getColumnIndex(DBHelper.COLUMN_NAME_RESERVETYPE)));
         values.put(DBHelper.COLUMN_NAME_HASBIGSCREEN,  parseBoolean(c.getInt(c.getColumnIndex(DBHelper.COLUMN_NAME_HASBIGSCREEN))));
         values.put(DBHelper.COLUMN_NAME_COMMENTS,      c.getString(c.getColumnIndex(DBHelper.COLUMN_NAME_COMMENTS)));
         values.put(DBHelper.COLUMN_NAME_ROOMNAME,      new Room[] {new Room(c.getString(c.getColumnIndex(DBHelper.COLUMN_NAME_ROOMNAME)))}[0].getRoomName());
         values.put(DBHelper.COLUMN_NAME_NOTE,     noteText);
 
         int rowsAffected = 0;
         String[] args = {String.valueOf(c.getLong(c.getColumnIndex(DBHelper.COLUMN_NAME_ID)))}; 
         if(spaceUsed() + noteText.length() > MAX_NOTE_SIZE) {
             Log.i("DATABASE", "Database is full."); 
             return -1;
         }else {
             rowsAffected = db.update(DBHelper.TABLE_NAME, values, "_id=?",args);    
         }
         return rowsAffected;
     }  
     
     public static int updateDbWithSpecficEntry(StudySpace studySpace, String noteText){  
 
         ContentValues values = new ContentValues();
 
         values.put(DBHelper.COLUMN_NAME_STARTMIN,          studySpace.getStartMin());
         values.put(DBHelper.COLUMN_NAME_ENDMIN,          studySpace.getEndMin());
         values.put(DBHelper.COLUMN_NAME_STARTHOUR,          studySpace.getStartHour());
         values.put(DBHelper.COLUMN_NAME_ENDHOUR,          studySpace.getEndHour());
         values.put(DBHelper.COLUMN_NAME_STARTDATE,          studySpace.getStartDate());
         values.put(DBHelper.COLUMN_NAME_ENDDATE,          studySpace.getEndDate());
         values.put(DBHelper.COLUMN_NAME_MONTH,          studySpace.getMonth());
         values.put(DBHelper.COLUMN_NAME_YEAR,          studySpace.getYear());
         values.put(DBHelper.COLUMN_NAME_GROUPSIZE,          studySpace.getGroupSize());
         values.put(DBHelper.COLUMN_NAME_BUILDINGNAME,  studySpace.getBuildingName());
         values.put(DBHelper.COLUMN_NAME_SPACENAME,     studySpace.getSpaceName());
         values.put(DBHelper.COLUMN_NAME_LATITUDE,      studySpace.getSpaceLatitude());
         values.put(DBHelper.COLUMN_NAME_LONGITUDE,     studySpace.getSpaceLongitude());
         values.put(DBHelper.COLUMN_NAME_ROOMNUM,       studySpace.getNumberOfRooms());
         values.put(DBHelper.COLUMN_NAME_MAXOCCUPANCY,  studySpace.getMaximumOccupancy());
         values.put(DBHelper.COLUMN_NAME_HASWHITEBOARD, studySpace.hasWhiteboard());
         values.put(DBHelper.COLUMN_NAME_PRIVACY,       studySpace.getPrivacy());
         values.put(DBHelper.COLUMN_NAME_HASCOMPUTER,   studySpace.hasComputer());
         values.put(DBHelper.COLUMN_NAME_RESERVETYPE,   studySpace.getReserveType());
         values.put(DBHelper.COLUMN_NAME_HASBIGSCREEN,  studySpace.has_big_screen());
         values.put(DBHelper.COLUMN_NAME_COMMENTS,      studySpace.getComments());
         values.put(DBHelper.COLUMN_NAME_ROOMNAME,      studySpace.getRooms()[0].getRoomName());
         values.put(DBHelper.COLUMN_NAME_NOTE,     noteText);
 
         int rowsAffected = 0;
         String[] args = {studySpace.getBuildingName(), studySpace.getSpaceName(), String.valueOf(studySpace.getStartDate())}; 
        String oldNote = query(studySpace);
        if(spaceUsed() - oldNote.length() + noteText.length() > MAX_NOTE_SIZE) {
             Log.i("DATABASE", "Database is full."); 
             return -1;
         }else {
             rowsAffected = db.update(DBHelper.TABLE_NAME, values, "buildingName=? and spaceName = ? and start_date = ?", args);    
         }
         return rowsAffected;
    } 
 
 
     /** 
      * Queries all histories, return an ArrayList of StudySpace objects.
      * @return An ArrayList of StudySpace objects. 
      */  
     public static ArrayList<StudySpace> query() {  
         ArrayList<StudySpace> histories = new ArrayList<StudySpace>();  
         Cursor c = queryTheCursor(true);
         c.moveToFirst();
 
         while (!c.isAfterLast()) {  
             StudySpace history = new StudySpace(); 
             
             history.setStartMin(c.getInt(c.getColumnIndex(DBHelper.COLUMN_NAME_STARTMIN)));
             history.setEndMin(c.getInt(c.getColumnIndex(DBHelper.COLUMN_NAME_ENDMIN)));
             history.setStartHour(c.getInt(c.getColumnIndex(DBHelper.COLUMN_NAME_STARTHOUR)));
             history.setEndHour(c.getInt(c.getColumnIndex(DBHelper.COLUMN_NAME_ENDHOUR)));
             history.setStartDate(c.getInt(c.getColumnIndex(DBHelper.COLUMN_NAME_STARTDATE)));
             history.setEndDate(c.getInt(c.getColumnIndex(DBHelper.COLUMN_NAME_ENDDATE)));
             history.setMonth(c.getInt(c.getColumnIndex(DBHelper.COLUMN_NAME_MONTH)));
             history.setYear(c.getInt(c.getColumnIndex(DBHelper.COLUMN_NAME_YEAR)));
             history.setGroupSize(c.getInt(c.getColumnIndex(DBHelper.COLUMN_NAME_GROUPSIZE)));
             
             history.setBuildingName(c.getString(c.getColumnIndex(DBHelper.COLUMN_NAME_BUILDINGNAME)));
             history.setSpaceName(c.getString(c.getColumnIndex(DBHelper.COLUMN_NAME_SPACENAME)));
             history.setLatitude(c.getDouble(c.getColumnIndex(DBHelper.COLUMN_NAME_LATITUDE)));
             history.setLongitude(c.getDouble(c.getColumnIndex(DBHelper.COLUMN_NAME_LONGITUDE)));
             history.setNumber_of_rooms(c.getInt(c.getColumnIndex(DBHelper.COLUMN_NAME_ROOMNUM)));
             history.setMax_occupancy(c.getInt(c.getColumnIndex(DBHelper.COLUMN_NAME_MAXOCCUPANCY)));
             history.setHas_whiteboard(parseBoolean(c.getInt(c.getColumnIndex(DBHelper.COLUMN_NAME_HASWHITEBOARD))));
             history.setPrivacy(c.getString(c.getColumnIndex(DBHelper.COLUMN_NAME_PRIVACY)));
             history.setHas_computer(parseBoolean(c.getInt(c.getColumnIndex(DBHelper.COLUMN_NAME_HASCOMPUTER))));
             history.setReserve_type(c.getString(c.getColumnIndex(DBHelper.COLUMN_NAME_RESERVETYPE)));
             history.setHas_big_screen(parseBoolean(c.getInt(c.getColumnIndex(DBHelper.COLUMN_NAME_HASBIGSCREEN))));
             history.setComments(c.getString(c.getColumnIndex(DBHelper.COLUMN_NAME_COMMENTS)));
             history.setRooms(new Room[] {new Room(c.getString(c.getColumnIndex(DBHelper.COLUMN_NAME_ROOMNAME)))});
             history.setNote(c.getString(c.getColumnIndex(DBHelper.COLUMN_NAME_NOTE)));
             histories.add(history);
             c.moveToNext();
         }
         c.close();
         Log.i("Database", "Entries read from database (size: " + histories.size() + ").");
         return histories;  
     }  
 
 
     /** 
      * Queries a given history, return note associated with this history.
      * @return a String containing note text. 
      */  
     public static String query(StudySpace history) {  
         ArrayList<StudySpace> histories = query(); 
         Iterator<StudySpace> it = histories.iterator();
         String noteText;
         while (it.hasNext()) {
             StudySpace current = it.next();
             if(current.getStartDate() == history.getStartDate()
                     && current.getSpaceName().equals(history.getSpaceName())
                     && current.getBuildingName().equals(history.getBuildingName())) {
                 noteText = current.getNote();
                 Log.i("Database", "Note read from database (length: " + noteText.length() + ").");
                 return noteText;
             }
         }
         return null;
     }  
 
     /** 
      * Queries all histories, return a cursor.
      * @param reversed Set true if desired return list is
      * in a descending order; false otherwise.
      * @return A Cursor.
      */  
     public static Cursor queryTheCursor(boolean reversed) {  
         return db.rawQuery(
                 "SELECT * FROM " + DBHelper.TABLE_NAME +
                 " ORDER BY " + DBHelper.COLUMN_NAME_ID + 
                 (reversed ? " DESC" : " ASC"), null);  
     }
 
     /**
      * Clears the database
      */
     public static void clearDB() {
         db.execSQL("DELETE FROM " + DBHelper.TABLE_NAME);
     }
     
     /**
      * The total length (in char) of all notes.
      * @return The total length (in char) of all notes.
      */
     public static double spaceUsed() {
         Cursor c = db.rawQuery("SELECT sum(length(" + DBHelper.COLUMN_NAME_NOTE + ")) FROM " + DBHelper.TABLE_NAME + ";", null);
         c.moveToFirst();
         int noteSize = c.getInt(0);
         c.close();
         return noteSize;
     }
 
     /**
      * Gets the total number of  entries in the database.
      * @return The total number of entries in the database.
      */
     public static int size() {
         Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_NAME, null);
         c.moveToFirst();
         int cnt= c.getInt(0);
         c.close();
         return cnt;
     }
 
     /**
      * Checks whether the database is empty.
      * @return True if the database is empty;
      * false otherwise.
      */
     public static boolean isEmpty() {
         return size() == 0;
     }
 
     /** 
      * Close the database. 
      */  
     public static void closeDB() {  
         db.close();  
     }
 
     /**
      * Converts integers to booleans.
      */
     private static boolean parseBoolean(int b) {
         return b == 1;
     }
     
 
 }
