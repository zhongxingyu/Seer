 package com.github.groupENIGMA.journalEgocentrique.model;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Environment;
 import com.github.groupENIGMA.journalEgocentrique.AppConstants;
 
 /**
  * This class implements the Application Database.
  * It creates or updates the database, if already existing
  * it also implements methods to manage the stored data for each Day
  * 
  * @version 0.1 
  * @author groupENIGMA
  */
 public class DB implements DBInterface {
 
     // Database Constants
     private static final String DB_NAME = "JournalEgocentrique.db"; 
     private static final int DB_VERSION = 1; 
 
     // DAY table
     public static final String Day_TABLE = "day";
     public static final String DAY_ID = "_id";
     public static final String DAY_DATE = "date";
     public static final String DAY_PHOTO = "photo";
 
     // ENTRY table
     public static final String Entry_TABLE = "entry";
     public static final String ENTRY_ID = "_id";
     public static final String ENTRY_TIME = "time";
     public static final String ENTRY_NOTE = "note";
     public static final String ENTRY_MOOD_ID = "mood_id";
     public static final String ENTRY_DAY_ID = "day_id";
 
     // MOOD table
     public static final String Mood_TABLE = "mood";
     public static final String MOOD_ID = "_id";
     public static final String MOOD_NAME = "name";
 
     // The Moods available in the first version of the database
     public static final String[][] MOODS_DB_VERSION_1 =  {
             {"0", "Happy"},
             {"1", "Sad"},
             {"2", "Angry"},
             {"3", "Bored"},
             {"4", "Depressed"},
             {"5", "Apathetic"},
             {"6", "Psycho"}
     };
 
     // Format used by the dates and times saved in the database
     public static final String DB_DATE_FORMAT = "yyyy-MM-dd";
     public static final String DB_TIME_FORMAT = "HH:mm:ss.SSS";
 
     // A reference to the database used by the application
     private SQLiteDatabase db;
     // the Activity or Application that is creating an object from this class.
     private Context context;
     private openHelper helper;
     // Used to convert Calendar to String and String to Calendar
     private SimpleDateFormat date_format;
     private SimpleDateFormat time_format;
 
     /**
      * Creates a DB object
      *
      * @param context The application context
      */
     public DB(Context context) {
         this.context = context;
         this.helper = new openHelper(context);  // Creates or open the db
         this.date_format = new SimpleDateFormat(DB_DATE_FORMAT);
         this.time_format = new SimpleDateFormat(DB_TIME_FORMAT);
     }
 
     /**
      * {@inheritDoc}
      */
     public void open() throws SQLException {
         db = helper.getWritableDatabase();
     }
 
     /**
      * {@inheritDoc}
      */
     public void close() {
         db.close();
     }
 
     @Override
     public boolean isOpen() {
         return db.isOpen();
     }
 
     /** Reads the rows available in the given Cursor to create and return
      * an Day object with all its related Entry
      * <p>
      * This works correctly only if the rows available in the cursor have the
      * following columns in this exact order:
      *      DAY_ID, DAY_DATE, DAY_PHOTO, ENTRY_ID, ENTRY_TIME, ENTRY_NOTE,
      *      ENTRY_MOOD_ID
      * (it's basically the Day table joined with the Entry one)
      * 
      * @param cur The Cursor containing the rows with the Day data
      * @throws DatabaseError when an error occurs parsing a date
      * @return a Day or, if the cur is "empty", null
      */
     private Day parseDayWithEntryFromCursor(Cursor cur) {
         // Check if the Cursor has at least one row
         if (cur.getCount() == 0) {
             return null;
         }
         else {
             // Move to the first row
             cur.moveToFirst();
             // Get the Day id
             long day_id = cur.getLong(0);
             // Get the Day date
             Calendar day_date = Calendar.getInstance();
             try {
                 day_date.setTime(date_format.parse(cur.getString(1)));
             } catch (ParseException e) {
                 throw new DatabaseError();
             }
             // Get the Day Photo (if exists)
             Photo day_photo;
             if (cur.isNull(2)) {
                 day_photo = null;
             }
             else {
                 day_photo= new Photo(cur.getString(2));
             }
             // Get all the Entry (if any)
             ArrayList<Entry> entry_list = new ArrayList<Entry>();
             // One or more Entry are available only if ENTRY_ID!=NULL
             if (!cur.isNull(3)) {
                 do {
                     // Parse the time from the database string
                     Calendar entry_time = Calendar.getInstance();
                     try {
                         entry_time.setTime(time_format.parse(cur.getString(4)));
                     } catch (ParseException e) {
                         throw new DatabaseError();
                     }
                     // Set the year, month and day using the date of Day
                     entry_time.set(Calendar.YEAR, day_date.get(Calendar.YEAR));
                     entry_time.set(Calendar.MONTH, day_date.get(Calendar.MONTH));
                     entry_time.set(Calendar.DATE, day_date.get(Calendar.DATE));
                     // Get the Entry Mood (if any)
                     Mood entry_mood;
                     if (cur.isNull(6)) {
                         entry_mood = null;
                     }
                     else {
                         entry_mood = new Mood(cur.getLong(6));
                     }
                     // Create the Entry and add it to the list
                     Entry entry = new Entry(
                             cur.getLong(3),     // ENTRY_ID
                             entry_time,         // ENTRY_TIME + DAY_DATE
                             cur.getString(5),   // ENTRY_NOTE
                             entry_mood          // Mood
                             );
                     entry_list.add(entry);
                 }
                 while (cur.moveToNext());
             }
             // Create and return the Day
             return new Day(day_id, day_date, day_photo, entry_list);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Day getDay() {
         Calendar today = Calendar.getInstance();
         return this.getDay(today);
     }
 
     /** 
      * {@inheritDoc}
      */
     public Day getDay(Calendar date) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
         // Convert the Calendar object to a String in the same format
         // used in the database
         String date_string = date_format.format(date.getTime());
         // Query the database
         Cursor cur = db.rawQuery(
                 "SELECT " + Day_TABLE   + "." + DAY_ID          + ", " +
                             Day_TABLE   + "." + DAY_DATE        + ", " +
                             Day_TABLE   + "." + DAY_PHOTO       + ", " +
                             Entry_TABLE + "." + ENTRY_ID        + ", " +
                             Entry_TABLE + "." + ENTRY_TIME      + ", " +
                             Entry_TABLE + "." + ENTRY_NOTE      + ", " +
                             Entry_TABLE + "." + ENTRY_MOOD_ID   +
                 " FROM " + Day_TABLE + " LEFT OUTER JOIN " + Entry_TABLE +
                         " ON " + Day_TABLE  + "." + DAY_ID + " = " +
                                  Entry_TABLE + "." + ENTRY_DAY_ID +
                 " WHERE " + Day_TABLE + "." + DAY_DATE + "=?" +
                 " ORDER BY " + Entry_TABLE + "." + ENTRY_TIME + " DESC ",
                 new String[] {date_string}
         );
 
         Day day = parseDayWithEntryFromCursor(cur);
         cur.close();
         return day;
     }
 
     /**
      * {@inheritDoc}
      */
     public Day getDay(long id) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
         // Query the database
         Cursor cur = db.rawQuery(
                 "SELECT " + Day_TABLE   + "." + DAY_ID          + ", " +
                             Day_TABLE   + "." + DAY_DATE        + ", " +
                             Day_TABLE   + "." + DAY_PHOTO       + ", " +
                             Entry_TABLE + "." + ENTRY_ID        + ", " +
                             Entry_TABLE + "." + ENTRY_TIME      + ", " +
                             Entry_TABLE + "." + ENTRY_NOTE      + ", " +
                             Entry_TABLE + "." + ENTRY_MOOD_ID   +
                 " FROM " + Day_TABLE + " LEFT OUTER JOIN " + Entry_TABLE +
                         " ON " + Day_TABLE  + "." + DAY_ID + " = " +
                                  Entry_TABLE + "." + ENTRY_DAY_ID +
                 " WHERE " + Day_TABLE + "." + DAY_ID + "=?"+
                 " ORDER BY " + Entry_TABLE + "." + ENTRY_TIME + " DESC ",
                 new String[] {Long.toString(id)}
         );
 
         Day day = parseDayWithEntryFromCursor(cur);
         cur.close();
         return day;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Day createDay() {
         Calendar today = Calendar.getInstance();
         return createDay(today);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Day createDay(Calendar date) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         // Throw InvalidOperationException if a Day with the given date already
         // exists
         if (existsDay(date)) {
             throw new InvalidOperationException();
         }
         // Insert the new Day
         else {
             ContentValues cv = new ContentValues();
             cv.put(DAY_DATE, date_format.format(date.getTime()));
             long newDayId = db.insert(Day_TABLE, null, cv);
             return getDay(newDayId);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void deleteDay(Day day){
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
         // Deletes the given day
         db.delete(Day_TABLE, DAY_ID + "=?",
                 new String [] {String.valueOf(day.getId())}
         );
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean existsDay() {
         Calendar today = Calendar.getInstance();
         return existsDay(today);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean existsDay(Calendar date) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         // Query the database
         Cursor cur = db.rawQuery(
                 "SELECT * " +
                 " FROM " + Day_TABLE +
                 " WHERE " + Day_TABLE + "." + DAY_DATE + "=?",
                 new String[] {date_format.format(date.getTime())}
         );
 
         // Check the number of returned rows
         boolean exists;
         if (cur.getCount() == 0) {
             exists = false;
         }
         else {
             exists = true;
         }
         // Close cursor and exit
         cur.close();
         return exists;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Calendar> getDatesList() {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         // Select all the dates stored in the database (they are UNIQUE)
         Cursor cur = db.rawQuery(
                 "SELECT " + Day_TABLE + "." + DAY_DATE +
                 " FROM " + Day_TABLE +
                 " ORDER BY " + Day_TABLE + "." + DAY_DATE + " DESC",
                 null
         );
 
         // Create the list to return
         List<Calendar> datesList = new ArrayList<Calendar>();
         // Create a calendar instance
         Calendar date;
         // Processes the query result with the cursor
         if (cur.moveToFirst()) {
             do {
                 try {
                     // Fill the list with the dates
                     date = Calendar.getInstance();
                     date.setTime(date_format.parse(cur.getString(0)));
                     datesList.add(date);
                 } catch (ParseException e) {
                     throw new DatabaseError();
                 }
             }
             while (cur.moveToNext());
         }
         cur.close();
         return datesList;
     }
 
     /**
      * {@inheritDoc}
      */
     public Entry insertEntry(Day day, String note, Mood mood) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         // Check if it's possible to add a Entry to the given Day
         if (day.canBeUpdated()) {
             // ENTRY_TIME = now (creation time)
             Calendar now = Calendar.getInstance();
             // Insert the Entry in the database
             ContentValues cv = new ContentValues();
             cv.put(ENTRY_DAY_ID, day.getId());
             cv.put(ENTRY_NOTE, note);
             cv.put(ENTRY_TIME, time_format.format(now.getTime()));
             if (mood == null) {
                 cv.putNull(ENTRY_MOOD_ID);
             }
             else {
                 cv.put(ENTRY_MOOD_ID, mood.getId());
             }
             long id = db.insert(Entry_TABLE, null, cv);
 
             //Create the Entry object to return
             return new Entry(id, now, note, mood);
         }
         else {
             // The Day can't be updated with a new Entry
             throw new InvalidOperationException();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Entry getEntry(long id) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         // Query the database for the Entry
         Cursor cur = db.rawQuery(
                 "SELECT " + Entry_TABLE + "." + ENTRY_ID        + ", " +
                             Entry_TABLE + "." + ENTRY_TIME      + ", " +
                             Entry_TABLE + "." + ENTRY_NOTE      + ", " +
                             Entry_TABLE + "." + ENTRY_MOOD_ID   + ", " +
                             Day_TABLE   + "." + DAY_DATE +
                 " FROM " + Entry_TABLE + " INNER JOIN " + Day_TABLE +
                         " ON " + Entry_TABLE + "." + ENTRY_DAY_ID + "=" +
                                  Day_TABLE   + "." + DAY_ID +
                 " WHERE " + Entry_TABLE + "." + ENTRY_ID + "=?",
                 new String[] {Long.toString(id)}
         );
 
         // If an Entry with the given id exists return it
         if (cur.moveToFirst()) {
             // Compute the Entry time from DAY_DATE and ENTRY_TIME
             Calendar entry_time = Calendar.getInstance();
             Calendar day_date = Calendar.getInstance();
             try {
                 entry_time.setTime(time_format.parse(cur.getString(1)));
                 day_date.setTime(date_format.parse(cur.getString(4)));
             } catch (ParseException e) {
                 throw new DatabaseError();
             }
             entry_time.set(Calendar.YEAR, day_date.get(Calendar.YEAR));
             entry_time.set(Calendar.MONTH, day_date.get(Calendar.MONTH));
             entry_time.set(Calendar.DATE, day_date.get(Calendar.DATE));
 
             // Get the Mood
             Mood mood;
             if (cur.isNull(3)) {
                 mood = null;
             }
             else {
                 mood = new Mood(cur.getLong(3));
             }
             // Create and return the Entry
             Entry entry = new Entry(
                     cur.getLong(0),     // ENTRY_ID
                     entry_time,         // DAY_DATE + ENTRY_TIME
                     cur.getString(2),   // ENTRY_NOTE
                     mood                // MOOD
             );
             cur.close();
             return entry;
         }
         // Entry not found
         else {
             return null;
         }
 
     }
     
     /**
      * {@inheritDoc}
      */
     public void setEntryNote(Entry entry, String new_note_text)  {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         // Get the sharedPreferences (for the Entry "grace period")
         SharedPreferences sharedPreferences = context.getSharedPreferences(
                 AppConstants.SHARED_PREFERENCES_FILENAME,
                 Context.MODE_PRIVATE
         );
 
         // Check if the Entry can be updated
         if (entry.canBeUpdated(sharedPreferences)) {
             // Update the database
             ContentValues cv = new ContentValues();
             cv.put(ENTRY_NOTE, new_note_text);
             long id = db.update(Entry_TABLE, cv, ENTRY_ID + "=?",
                     new String []{String.valueOf(entry.getId())}
             );
             // Update the Entry object
             entry.setNote(new_note_text);
         }
         else {
             // The Entry can't be updated
             throw new InvalidOperationException();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void setEntryMood(Entry entry, Mood new_mood)  {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         // Get the sharedPreferences (for the Entry "grace period")
         SharedPreferences sharedPreferences = context.getSharedPreferences(
                 AppConstants.SHARED_PREFERENCES_FILENAME,
                 Context.MODE_PRIVATE
         );
 
         // Check if the Entry can be updated
         if (entry.canBeUpdated(sharedPreferences)) {
             // Update the Entry
             ContentValues cv = new ContentValues();
             if (new_mood == null) {
                 cv.putNull(ENTRY_MOOD_ID);
             }
             else {
                 cv.put(ENTRY_MOOD_ID, new_mood.getId());
             }
             long id = db.update(Entry_TABLE, cv, ENTRY_ID + "=?",
                     new String []{String.valueOf(entry.getId())}
             );
             // Update the Entry object
             entry.setMood(new_mood);
         }
         else {
             // The Entry can't be updated
             throw new InvalidOperationException();
         }
     }
 
     /**
      * {@inheritDoc}
      * @param entry
      */
     public void deleteEntry(Entry entry) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         // Deletes the Entry from database
         db.delete(Entry_TABLE, ENTRY_ID + "=?",
                 new String [] {String.valueOf(entry.getId())});
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Mood> getAvailableMoods() {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         ArrayList<Mood> moods = new ArrayList<Mood>();
         // Query the database
         Cursor cur = db.rawQuery(
                 "SELECT " + Mood_TABLE + "." + MOOD_ID +
                 " FROM " + Mood_TABLE,
                 new String[] {}
         );
 
         // Read the mood information from the cursor
         if (cur.moveToFirst()) {
             do {
                 Mood md = new Mood(cur.getLong(0));
                 moods.add(md);
             }
             while (cur.moveToNext());
         }
         cur.close();
         return moods;
     }
 
     /**
      * {@inheritDoc}
      */
     public Photo setDayPhoto(Day day, String tmpPhotoPath) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         // If the Day can't be updated throw the InvalidOperationException
         if (!day.canBeUpdated()) {
             throw new InvalidOperationException();
         }
 
         // Get the Path to the Photo directory
         String photoDirPath = Environment.getExternalStorageDirectory()
                 .getAbsolutePath() + File.separator +
                 AppConstants.EXTERNAL_STORAGE_PHOTO_DIR;
         File photoDir = new File(photoDirPath);
         // Create the dir if it doesn't exist
         if (! photoDir.exists()){
             if (!photoDir.mkdirs()) {
                 // Throw an exception if unable to open/create the dir
                 // in the External Storage
                 throw new DatabaseError();
             }
         }
 
         // Move the full resolution photo to its final location
         File tmpPhoto = new File(tmpPhotoPath);
         String photoPath = photoDirPath + File.separator +
                 AppConstants.PHOTO_FILENAME_PREFIX + day.getId() + ".jpg";
         File photo = new File(photoPath);
         if (!tmpPhoto.renameTo(photo)) {
             // Throw an exception if unable to move the photo
             throw new DatabaseError();
         }
 
         // Save the thumbnail
         String thumbPhotoPath = photoDir + File.separator +
                 AppConstants.THUMB_FILENAME_PREFIX + day.getId() + ".jpg";
         File thumbPhoto = new File (thumbPhotoPath);
         // Resize the original image
         Bitmap fullResBitmap = BitmapFactory.decodeFile(photoPath);
         Bitmap thumbBitmap = Bitmap.createScaledBitmap(
                 fullResBitmap,
                 85,  // TODO use a value saved in dimens.xml
                 85,
                 false
         );
         // Save the thumb to disk
         try {
             FileOutputStream out = new FileOutputStream(thumbPhoto);
             thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
             out.flush();
             out.close();
         } catch (Exception e) {
             throw new DatabaseError();
         }
 
         // Saves photo and thumb path to the database
         ContentValues cv = new ContentValues();
         cv.put(DAY_PHOTO, photoPath);
         db.update(Day_TABLE, cv, DAY_ID + "=?",
                 new String []{String.valueOf(day.getId())}
         );
 
         return new Photo(photoPath, thumbPhotoPath);
     }
 
     /**
      * {@inheritDoc}
      */
     public void removePhoto(Day day) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
         
         // Check if the Photo can be deleted
         if (day.canBeUpdated()) {
             // Check if the day has a Photo
             Photo photo = day.getPhoto();
             if (photo != null) {
                 // Delete the photo from external storage
                 File photoFile = new File(photo.getPath());
                 photoFile.delete();
                 File thumbFile = new File(photo.getPathThumb());
                 thumbFile.delete();
                 // Remove the photo from the database
                 ContentValues cv = new ContentValues();
                 cv.putNull(DAY_PHOTO);
                 db.update(Day_TABLE, cv, DAY_ID + "=?",
                         new String []{String.valueOf(day.getId())}
                 );
             }
         }
         else {
             // The Photo can't be deleted
             throw new InvalidOperationException();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Photo> getPhotos() {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         // Query the photos
         Cursor cur = db.rawQuery(
             "SELECT " + Day_TABLE + "." + DAY_PHOTO +
             " FROM " + Day_TABLE +
             " WHERE " + Day_TABLE + "." + DAY_PHOTO + " IS NOT NULL " +
             " ORDER BY " + Day_TABLE + "." + DAY_DATE + " DESC",
             null
         );
 
         // Read all the photos
         ArrayList<Photo> photos = new ArrayList<Photo>();
         if (cur.moveToFirst()) {
             do {
                 photos.add(new Photo(cur.getString(0)));
             }
             while (cur.moveToNext());
         }
 
         return photos;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Photo> getPhotos(Calendar from, Calendar to) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         ArrayList<Photo> photos = new ArrayList<Photo>();
         // Query the database
         Cursor cur = db.rawQuery(
             "SELECT " + Day_TABLE + "." + DAY_PHOTO +
             " FROM " + Day_TABLE +
            " WHERE " + Day_TABLE + "." + DAY_DATE + " BETWEEN =? AND =? " +
                 " AND " + Day_TABLE + "." + DAY_PHOTO + " IS NOT NULL " +
             " ORDER BY " + Day_TABLE + "." + DAY_DATE + " DESC",
             new String[] {date_format.format(from.getTime()),
                           date_format.format(to.getTime()) }
         );
 
         if (cur.moveToFirst()) {
             do {
                 photos.add(new Photo(cur.getString(0)));
             }
             while (cur.moveToNext());
         }
 
         cur.close();
         return photos;
     }
 
 
     /**
      * Raises a ConnectionException if DB is not connected to a Database.
      * <p>
      * This private method should by the methods that performs database
      * queries at least once.
      * </p>
      */
     private void raiseConnectionExceptionIfNotConnected() {
         if (db == null || !db.isOpen()) {
             throw new ConnectionException();
         }
     }
 
     /**
      * This class implements an open helper extending the SQLiteOpenHelper
      * 
      * It creates, if not existing, a new database or updates the existing one
      */
     private class openHelper extends SQLiteOpenHelper {
 
         public openHelper(Context context) {
             super(context, DB_NAME, null, DB_VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
             // SQL statements used to create the tables
             String createMoodTable =
                 "CREATE TABLE IF NOT EXISTS " + Mood_TABLE + " ( " +
                 MOOD_ID     + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                 MOOD_NAME   + " TEXT" +
                 " );";
 
             String createDayTable =
                 "CREATE TABLE IF NOT EXISTS " + Day_TABLE + " ( " +
                     DAY_ID      + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                     DAY_DATE    + " TEXT UNIQUE NOT NULL," +
                     DAY_PHOTO   + " TEXT" +
                 " );";
             
             String createEntryTable =
                 "CREATE TABLE IF NOT EXISTS " + Entry_TABLE + " ( " +
                     ENTRY_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                     ENTRY_TIME      + " TEXT NOT NULL," +
                     ENTRY_NOTE      + " TEXT NOT NULL," +
                     ENTRY_MOOD_ID   + " INTEGER, " +
                     ENTRY_DAY_ID    + " INTEGER NOT NULL," +
                 " CONSTRAINT fk_Entry_Days FOREIGN KEY(" + ENTRY_DAY_ID + ") " +
                     " REFERENCES " + Day_TABLE + "(" + DAY_ID + ")" +
                     " ON DELETE CASCADE, " +
                 " CONSTRAINT fk_Entry_Mood FOREIGN KEY(" + ENTRY_MOOD_ID + ") " +
                     " REFERENCES " + Mood_TABLE + "(" + MOOD_ID + ")" +
                 " );";
             
             // Run all the CREATE TABLE ...
             db.execSQL(createMoodTable);
             db.execSQL(createDayTable);
             db.execSQL(createEntryTable);
             
             // Insert all the basic Moods
             for (String[] mood : MOODS_DB_VERSION_1) {
                 ContentValues cv = new ContentValues();
                 cv.put(MOOD_ID, mood[0]);
                 cv.put(MOOD_NAME, mood[1]);
                 db.insert(Mood_TABLE, null, cv);
             }
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
             //
         }
 
         @Override
         public void onOpen(SQLiteDatabase db) {
 
             super.onOpen(db);
             if (!db.isReadOnly()) {
                 // Enable foreign key constraints
                 db.execSQL("PRAGMA foreign_keys=ON;");
             }
         }
     }  // openHelper
 
 }
