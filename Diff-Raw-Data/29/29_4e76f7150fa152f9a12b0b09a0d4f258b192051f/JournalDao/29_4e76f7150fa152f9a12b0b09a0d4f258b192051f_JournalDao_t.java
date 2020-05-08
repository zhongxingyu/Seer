 package com.feedme.dao;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import com.feedme.model.Baby;
 import com.feedme.model.Journal;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * This DAO class handles all CRUD operations on the Entries table in the SQLLite database.
  */
 public class JournalDao {
 
     // Contacts table name
     private static final String TABLE_DATA = "entries";
 
     // Contacts Table Columns names
     private static final String KEY_ID = "id";
     private static final String KEY_DATE = "date";
     private static final String KEY_START_TIME = "start_time";
     private static final String KEY_END_TIME = "end_time";
     private static final String KEY_SIDE = "side";
     private static final String KEY_OUNCES = "ounces";
     private static final String KEY_CHILD_ID = "child_id";
 
     private SQLiteDatabase database;
     private JournalDatabaseHandler databaseHandler;
 
     public JournalDao(Context context) {
         databaseHandler = new JournalDatabaseHandler(context);
     }
 
     public void open() throws SQLException {
         database = databaseHandler.getWritableDatabase();
     }
 
     public void close() {
         databaseHandler.close();
     }
 
     /**
      * Adds a new Entry to the Database.
      *
      * @param entry - Journal POJO
      */
     public void addEntry(Journal entry) {
         
         open();
 
         ContentValues values = new ContentValues();
         values.put(KEY_DATE, entry.getDate()); // Date
         values.put(KEY_START_TIME, entry.getStartTime()); // Time
         values.put(KEY_END_TIME, entry.getEndTime()); // Time
         values.put(KEY_SIDE, entry.getSide()); // Side
         values.put(KEY_OUNCES, entry.getOunces()); // Ounces
         values.put(KEY_CHILD_ID, entry.getChild()); // Child ID
 
         // Inserting Row
         database.insert(TABLE_DATA, null, values);
         
         close();
     }
 
     /**
      * Getting single entry from the database by its ID.
      *
      * @param id - The ID of the Entry in the database.
      *
      * @return - Journal POJO representation of a specific Entry in the database.
      */
     public Journal getEntry(int id) {
 
         open();
         
         Cursor cursor = database.query(TABLE_DATA, new String[]{KEY_ID,
                 KEY_DATE, KEY_START_TIME, KEY_END_TIME, KEY_SIDE, KEY_OUNCES, KEY_CHILD_ID}, KEY_ID + "=?",
                 new String[]{String.valueOf(id)}, null, null, null, null);
         if (cursor != null)
             cursor.moveToFirst();
 
         Journal entry =  cursorToJournal(cursor);
         
         close();
 
         // return baby
         return entry;
     }
 
 
     /**
      * Getting single entry from the database by its date.
      *
      * @param date - The date of the entry in the database.
      *
      * @return - Journal POJO representation of a specific entry in the database.
      */
     public Journal getEntryByDate(String date) {
 
         open();
 
         Cursor cursor = database.query(TABLE_DATA, new String[] { KEY_ID,
                 KEY_DATE, KEY_START_TIME, KEY_END_TIME, KEY_SIDE, KEY_OUNCES, KEY_CHILD_ID }, KEY_DATE + "=?",
                 new String[] { String.valueOf(date) }, null, null, null, null);
         if (cursor != null)
             cursor.moveToFirst();
 
         Journal entry =  cursorToJournal(cursor);
 
         close();
 
         return entry;
     }
 
     /**
      * Get All Entries from the Database
      *
      * @return - Collection of all Entries located in the database.
      */
     public List<Journal> getAllEntries() {
         
         open();
         
         List<Journal> entryList = new ArrayList<Journal>();
 
         // Select All Query
         String selectQuery = "SELECT  * FROM " + TABLE_DATA;
         Cursor cursor = database.rawQuery(selectQuery, null);
 
         // looping through all rows and adding to list
         if (cursor.moveToFirst()) {
             do {
                 entryList.add(cursorToJournal(cursor));
             } while (cursor.moveToNext());
         }
         
         close();
 
         return entryList;
     }
 
     /**
     * Returns a Journal[] of all entries.
      *
      * @return
      */
     public Journal[] getAllEntriesAsArray(){

         List<Journal> entries = getAllEntries();

        Journal[] entryArray = new Journal[entries.size()];
        for(int i=0 ; i<entries.size() ; i++){
            Journal entry = entries.get(i);
            entryArray[i] = entry;
        }

        return entryArray;
     }
 
     /**
      * Returns the last X feedings (where X=limit) based on the passed in childId in the form of a List<Journal>.
      *
      * @param childId
      * @param limit
      * @return
      */
     public List<Journal> getLastFeedingsByChild(int childId, int limit){
 
         open();
 
         List<Journal> entryList = new ArrayList<Journal>();
 
         String query = "SELECT * FROM " + TABLE_DATA + " WHERE " + KEY_CHILD_ID + "=" + childId + " ORDER BY " + KEY_ID + " DESC LIMIT " + limit;
         Cursor cursor = database.rawQuery(query, null);
 
         if(cursor.moveToFirst()) {
             do{
                 entryList.add(cursorToJournal(cursor));
             } while (cursor.moveToNext());
         }
 
         close();
 
         return entryList;
     }
 
     /**
      * Returns the last X feedings (where X=limit) based on the passed in childId in the form of a Journal[].
      *
      * @param childId
      * @param limit
      * @return
      */
     public Journal[] getLastFeedingsByChildAsArray(int childId, int limit){
        
        List<Journal> entries = getLastFeedingsByChild(childId, limit);
 
        Journal[] entryArray = new Journal[entries.size()];
        for(int i=0 ; i<entries.size() ; i++){
            Journal entry = entries.get(i);
            entryArray[i] = entry;
        }
 
        return entryArray;
     }
 
     /**
      * Updates an existing Entry in the database.
      *
      * @return
      */
     public int updateEntry(Journal entry) {
 
         open();
         
         ContentValues values = new ContentValues();
         values.put(KEY_DATE, entry.getDate());
         values.put(KEY_START_TIME, entry.getStartTime());
         values.put(KEY_END_TIME, entry.getEndTime());
         values.put(KEY_SIDE, entry.getSide());
         values.put(KEY_OUNCES, entry.getOunces());
         values.put(KEY_CHILD_ID, entry.getChild());
 
         // updating row
         int result = database.update(TABLE_DATA, values, KEY_ID + " = ?", new String[] { String.valueOf(entry.getID()) });
         
         close();
         
         return result;
     }
 
     /**
      * Deletes an entry from the Database
      */
     public void deleteEntry(Journal entry) {
         open();
         database.delete(TABLE_DATA, KEY_ID + " = ?", new String[]{String.valueOf(entry.getID())});
         close();
     }
 
     /**
      * Returns the total number of entries that exist in the Database.
      *
      * @return - Total number of entries in the entries table.
      */
     public int getEntriesCount() {
         open();
         String countQuery = "SELECT  * FROM " + TABLE_DATA;
         Cursor cursor = database.rawQuery(countQuery, null);
         cursor.close();
 
         // return count
         int count = cursor.getCount();
         
         close();
         
         return count;
     }
 
     /**
      * Converts a Cursor from the Entries Table to a Journal Object.
      *
      * @param cursor - Cursor object that contains the Entry data from the Database
      *
      * @return - Journal
      */
     private Journal cursorToJournal(Cursor cursor) {
         return new Journal(Integer.parseInt(cursor.getString(0)),
                 cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), Integer.parseInt(cursor.getString(6)));
     }
 }
