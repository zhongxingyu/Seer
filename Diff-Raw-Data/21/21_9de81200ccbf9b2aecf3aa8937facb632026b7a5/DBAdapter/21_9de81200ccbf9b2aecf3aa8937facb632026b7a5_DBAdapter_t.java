 package net.analogyc.wordiary.database;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.preference.PreferenceManager;
 
 import net.analogyc.wordiary.models.DateFormats;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 
 /**
  * Aggregator for all the database queries
  */
 public class DBAdapter {
 
     private DataBaseHelper mDbHelper;
     private SQLiteDatabase mDatabase;
     private SharedPreferences mPreferences;
 
     /**
      * You must call open() on this object to use other methods
      */
     public DBAdapter(Context context) {
         mDbHelper = new DataBaseHelper(context);
         mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
     }
 
     /**
      * Returns an open, writable database, or creates a new instance
      */
     private SQLiteDatabase getConnection() {
         if (mDatabase == null) {
             mDatabase = mDbHelper.getWritableDatabase();
         }
 
         return mDatabase;
     }
 
     /**
      * Close databaseHelper, any class that use DBAdapter must call this method when it don't use it anymore
      */
     public void close() {
         if (mDatabase != null) {
             mDatabase.close();
             mDatabase = null;
         }
     }
 
 
     /**** ENTRIES OPERATIONS ****/
 
     /**
      * Get the entry with the given id
      *
      * @param id entry's id
      * @return a Cursor that contains the selected entry
      */
     public Cursor getEntryById(int id) {
         String query = "SELECT * FROM " + Entry.TABLE_NAME + " WHERE " + Entry._ID + " = " + id + " LIMIT 1";
         return getConnection().rawQuery(query, null);
     }
 
     /**
      * Get the  entries associated with the given day id
      *
      * @param id entry's id
      * @return a Cursor that contains the entries ordered by date
      */
     public Cursor getEntriesByDay(int id) {
         String query = "SELECT * FROM " + Entry.TABLE_NAME +
                 " WHERE " + Entry.COLUMN_NAME_DAY_ID + " = " + id +
                 " ORDER BY " + Entry._ID + " DESC";
         return getConnection().rawQuery(query, null);
     }
 
     /**
      * Add a new entry
      *
      * @param text the message of the entry
      * @param mood the correspondent mood
      */
     public void addEntry(String text, int mood) {
         //create the current timestamp
         Date now = new Date(System.currentTimeMillis());
         SimpleDateFormat sdf = new SimpleDateFormat(DateFormats.DATABASE, Locale.getDefault());
 
         //if there's a the correspond day for this entry, we'll create a new day
         String query = "SELECT * " +
                 "FROM " + Day.TABLE_NAME +
                 " WHERE " + Day.COLUMN_NAME_CREATED + " LIKE '" + sdf.format(now).substring(0, 8) + "%'" +
                 " LIMIT 1";
         Cursor c = getConnection().rawQuery(query, null);
         int photo;
         if (c.moveToFirst()) {
             photo = c.getInt(0);
         } else {
             addPhoto("");
             query = "SELECT * " +
                     "FROM " + Day.TABLE_NAME +
                     " WHERE " + Day.COLUMN_NAME_CREATED + " LIKE '" + sdf.format(now).substring(0, 8) + "%'" +
                     " LIMIT 1";
 
             c = getConnection().rawQuery(query, null);
             c.moveToFirst();
             photo = c.getInt(0);
 
         }
         c.close();
 
         //insert the entry
         query = "INSERT INTO " + Entry.TABLE_NAME + " ( " +
                 Entry.COLUMN_NAME_MESSAGE + " , " +
                 Entry.COLUMN_NAME_MOOD + " , " +
                 Entry.COLUMN_NAME_DAY_ID + " , " +
                 Entry.COLUMN_NAME_CREATED +
                 ") VALUES ( ?,?,?,? )";
         getConnection().execSQL(query, new Object[]{text, mood, photo, sdf.format(now)});
     }
 
     /**
      * Delete an entry
      *
      * @param id the message id
      */
     public void deleteEntryById(int id) {
         //get the day id of this entry
         Cursor entry = getEntryById(id);
         entry.moveToFirst();
         int day_id = entry.getInt(1);
         entry.close();
 
         //if this entry is the only one associated with the day and it has no photo, we'll delete this day
         Cursor day = getDayById(day_id);
         Cursor day_entries = getEntriesByDay(day_id);
         day.moveToFirst();
         String filename = day.getString(1);
         if (filename.equals("") && day_entries.getCount() <= 1) {
             deleteDay(day_id, false);
         }
         day.close();
         day_entries.close();
 
         //delete entry
         String query = "DELETE FROM " + Entry.TABLE_NAME + " WHERE " + Entry._ID + " = " + id;
         getConnection().execSQL(query);
     }
 
     /**
      * Modify the mood of the selected entry
      *
      * @param entryId entry id
      * @param moodId  filename of the mood
      */
     public void updateMood(int entryId, String moodId) {
         String query = "UPDATE " + Entry.TABLE_NAME +
                 " SET " + Entry.COLUMN_NAME_MOOD + " =  ?" +
                 " WHERE " + Entry._ID + " = ?";
         getConnection().execSQL(query, new Object[]{moodId, entryId});
     }
 
 
     /**
      * Modify the message of the selected entry
      *
      * @param entryId entry id
      * @param message The message to insert
      */
     public void updateMessage(int entryId, String message) {
         String query = "UPDATE " + Entry.TABLE_NAME +
                 " SET " + Entry.COLUMN_NAME_MESSAGE + " =  ?" +
                 " WHERE " + Entry._ID + " = ?";
         getConnection().execSQL(query, new Object[]{message, entryId});
     }
 
     /**
      * Verify if the selected entry can be modified
      *
      * @param entryId entry id
      * @return boolean true if is editable, false otherwise
      */
     public boolean isEditableEntry(int entryId) {
         int grace_period = Integer.parseInt(mPreferences.getString("grace_period", "1"));
         //create the current timestamp
         Date now = new Date(System.currentTimeMillis());
         SimpleDateFormat sdf = new SimpleDateFormat(DateFormats.DATABASE, Locale.getDefault());
 
         String query = "SELECT * FROM " + Entry.TABLE_NAME + " WHERE " + Entry._ID + " = " + entryId + " LIMIT 1";
         Cursor c = getConnection().rawQuery(query, null);
         c.moveToFirst();
         Date created;
         try {
             created = sdf.parse(c.getString(4));
         } catch (ParseException e) {
             e.printStackTrace();
             return false;
         }
         c.close();
 
         long now_mil = now.getTime();
         long created_mil = created.getTime();
 
         long diff = now_mil - created_mil;
         return diff < grace_period * 60 * 60 * 1000;
     }
 
     /**
      * Gets the next or the previous entry id
      *
      * @param currentEntry The id of the current entry opened
      * @param backwards    If it should look for the previous image
      * @return The cursor containing the single row or zero rows, with as only column the ID
      */
     public Cursor getNextEntry(int currentEntry, boolean backwards) {
         String query = "SELECT " + Entry._ID + " FROM " + Entry.TABLE_NAME + " " +
                 "WHERE " + Entry._ID + " " + (backwards ? "<" : ">") + currentEntry + " " +
                 "ORDER BY " + Day._ID + " " + (backwards ? "DESC" : "ASC") + " " +
                 "LIMIT 1";
         Cursor result = getConnection().rawQuery(query, null);
         if (result.getCount() <= 0) {
             result.close();
             return getEntryById(currentEntry);
         } else {
             return result;
         }
     }
 
     /**
      * Determine if entry has a next ( or the previous) entry
      *
      * @param currentEntry The id of the current entry opened
      * @param backwards    If it should look for the previous image
      * @return true is it has a next or previous, false otherwise
      */
     public boolean hasNextEntry(int currentEntry, boolean backwards) {
         String query = "SELECT " + Entry._ID + " FROM " + Entry.TABLE_NAME + " " +
                 "WHERE " + Entry._ID + " " + (backwards ? "<" : ">") + currentEntry + " " +
                 "ORDER BY " + Day._ID + " " + (backwards ? "DESC" : "ASC") + " " +
                 "LIMIT 1";
         Cursor result = getConnection().rawQuery(query, null);
         boolean hasNext = result.getCount() > 0;
         result.close();
         return hasNext;
     }
 
 
     /**** DAYS OPERATIONS ****/
 
     /**
      * Delete a day, this method could maintain the consistency of the data stored (in this case so a day
      * can be deleted only if it has no entry)
      *
      * @param id          the day id
      * @param consistency true if method has to make sure about data consistency
      * @return 0 if the selected day is correctly deleted from db, the number of relative entries
      *         otherwise (in this case db isn't modified)
      */
     public int deleteDay(int id, boolean consistency) {
         Cursor c = getEntriesByDay(id);
         int count = c.getCount();
         if (count <= 0 || !consistency) {
             //delete the entry
             String query = "DELETE FROM " + Day.TABLE_NAME + " WHERE " + Day._ID + " = " + id;
             getConnection().execSQL(query);
         }
         c.close();
         return count;
     }
 
     /**
      * Get the day associated with the given entry
      *
      * @param id entry's id
      * @return a Cursor that contains the selected day
      */
     public Cursor getDayByEntry(int id) {
         Cursor c = this.getEntryById(id);
         c.moveToFirst();
         int day = c.getInt(1);
         c.close();
         return getDayById(day);
     }
 
     /**
      * Delete the selected photo
      *
      * @param id the day id
      */
     public void deletePhoto(int id) {
         //try to delete the day
         int entries = this.deleteDay(id, true);
         //if deleteDay coldn't delete the day we have to clear the field 'filename'
         if (entries > 0) {
             //delete the filename
             String query = "UPDATE " + Day.TABLE_NAME + " " +
                     "SET " + Day.COLUMN_NAME_FILENAME + " = ''" +
                     "WHERE " + Day._ID + " = " + id;
             getConnection().execSQL(query);
         }
     }
 
     /**
      * Add a photo to the current day
      *
      * @param filename the path of the photo
      */
     public void addPhoto(String filename) {
         //create the current timestamp
         Date now = new Date(System.currentTimeMillis());
         SimpleDateFormat sdf = new SimpleDateFormat(DateFormats.DATABASE, Locale.getDefault());
         String date = sdf.format(now);
 
         //verify if there is a row for this day
         String query = "SELECT * " +
                 " FROM " + Day.TABLE_NAME +
                 " WHERE " + Day.COLUMN_NAME_CREATED + " LIKE '" + date.substring(0, 8) + "%'" +
                 " LIMIT 1";
 
         Cursor c = getConnection().rawQuery(query, null);
 
         if (c.getCount() > 0) {
             //modify the filename
             c.moveToFirst();
             query = "UPDATE " + Day.TABLE_NAME + " " +
                     "SET " + Day.COLUMN_NAME_FILENAME + " = ?" +
                     "WHERE " + Day._ID + " = ?";
             getConnection().execSQL(query, new Object[]{filename, c.getInt(0)});
         } else {
             //insert the entry
             query = "INSERT INTO " + Day.TABLE_NAME + " ( " +
                     Day.COLUMN_NAME_FILENAME + " , " +
                     Day.COLUMN_NAME_CREATED +
                     ") VALUES (?, ?)";
             getConnection().execSQL(query, new Object[]{filename, date});
         }
         c.close();
     }
 
     /**
      * Get the photo of the given day
      *
      * @param day Day in format yyyyMMdd
      * @return The database row, one or none
      */
     public Cursor getPhotoByDay(String day) {
         String query = "SELECT * " +
                 "FROM " + Day.TABLE_NAME + " " +
                 "WHERE " + Day.COLUMN_NAME_CREATED + " LIKE '" + day + "%' " +
                 "LIMIT 1";
 
         return getConnection().rawQuery(query, null);
     }
 
     /**
      * Get all the days ordered by date (DESC)
      *
      * @return Cursor containing the days
      */
     public Cursor getAllDays() {
         String query = "SELECT * FROM " + Day.TABLE_NAME + " ORDER BY " + Day._ID + " DESC";
         return getConnection().rawQuery(query, null);
     }
 
     /**
      * Get all the days ordered by date (DESC)
      *
      * @return Cursor containing the days
      */
     public Cursor getAllPhotos() {
         String query = "SELECT * FROM " + Day.TABLE_NAME +
                 " WHERE " + Day.COLUMN_NAME_FILENAME + "<> ''" +
                 " ORDER BY " + Day._ID + " DESC";
         return getConnection().rawQuery(query, null);
     }
 
     /**
      * Get the selected entry
      *
      * @param id entry's id
      * @return a Cursor that contains the selected entry, or null
      */
     public Cursor getDayById(int id) {
         String query = "SELECT * FROM " + Day.TABLE_NAME + " WHERE " + Day._ID + " = " + id + " LIMIT 1";
         return getConnection().rawQuery(query, null);
     }
 
     /**
      * Verify if the selected entry can be modified
      *
      * @param dayId day id
      * @return boolean true if it is editable, false otherwise
      */
     public boolean isEditableDay(int dayId) {
         //create the current timestamp
         Date now = new Date(System.currentTimeMillis());
         SimpleDateFormat sdf = new SimpleDateFormat(DateFormats.DATABASE, Locale.getDefault());
 
         String query = "SELECT * FROM " + Day.TABLE_NAME + " WHERE " + Day._ID + " = " + dayId + " LIMIT 1";
         Cursor c = getConnection().rawQuery(query, null);
         c.moveToFirst();
         String created_date = c.getString(2).substring(0, 8);
         c.close();
         String current_date = sdf.format(now).substring(0, 8);
         return created_date.equals(current_date);
     }
 
     /**
      * Gets the next or the previous image
      *
      * @param currentDay The id of the current day opened
      * @param backwards  If it should look for the previous image
      * @return The cursor containing the single row or zero rows, with as only column the ID
      */
     public Cursor getNextDay(int currentDay, boolean backwards) {
         String query = "SELECT " + Day._ID + " FROM " + Day.TABLE_NAME + " " +
                 "WHERE " + Day._ID + " " + (backwards ? "<" : ">") + " ? " +
                 "AND " + Day.COLUMN_NAME_FILENAME + " <> ? " +
                 "ORDER BY " + Day._ID + " " + (backwards ? "DESC" : "ASC") + " " +
                 "LIMIT 1";
         return getConnection().rawQuery(query, new String[]{Integer.toString(currentDay), ""});
     }
 }
