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
      * an Day object with its related Notes
      * <p>
      * This works correctly only if the rows available in the cursor have the
      * following columns in this exact order:
      *      DAY_ID, DAY_DATE, DAY_PHOTO, ENTRY_MOOD_ID, ENTRY_ID, ENTRY_NOTE,
      *      ENTRY_TIME
      * (it's basically the Day table joined with the Entry one)
      * 
      * @param cur The Cursor containing the rows with the Day data
      * @return an Day or, if the cur is "empty", null
      */
     private Day createEntryWithNotesFromCursor(Cursor cur) {
         // Check if the Cursor has at least one row
         if (cur.getCount() == 0) {
             return null;
         }
         else {
             // Move to the first row
             cur.moveToFirst();
             // Get the Day id
             long entry_id = cur.getLong(0);
             // Get the Day date
             Calendar entry_date = Calendar.getInstance();
             try {
                 entry_date.setTime(date_format.parse(cur.getString(1)));
             } catch (ParseException e) {
                 throw new DatabaseError();
             }
             // Get the Day Photo (if exists)
             Photo entry_photo;
             if (cur.isNull(2)) {
                 entry_photo = null;
             }
             else {
                 entry_photo= new Photo(cur.getString(2));
             }
             // Get the Day Mood (if exists)
             Mood entry_mood;
             if (cur.isNull(3)) {
                 entry_mood = null;
             }
             else {
                 entry_mood = new Mood(cur.getLong(3));
             }
             // Get all the Notes (if any)
             ArrayList<Entry> note_list = new ArrayList<Entry>();
             if (!cur.isNull(4)) {  // Notes are available only if ENTRY_ID!=NULL
                 do {
                     // Parse the time from the database string
                     Calendar note_time = Calendar.getInstance();
                     try {
                         note_time.setTime(time_format.parse(cur.getString(6)));
                     } catch (ParseException e) {
                     	throw new DatabaseError();
                     }
                     // Set the year, month and day using the entry_date
                     note_time.set(Calendar.YEAR, entry_date.get(Calendar.YEAR));
                     note_time.set(Calendar.MONTH, entry_date.get(Calendar.MONTH));
                     note_time.set(Calendar.DATE, entry_date.get(Calendar.DATE));
                     // Create the Entry and add it to the list
                     Entry note = new Entry(
                             cur.getLong(4),     // ENTRY_ID
                             cur.getString(5),   // ENTRY_NOTE
                             note_time
                             );
                     note_list.add(note);
                 }
                 while (cur.moveToNext());
             }
             // Create and return the Day
             return new Day(entry_id, entry_date, entry_photo, entry_mood,
                     note_list);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Day getEntry() {
         Calendar cal = Calendar.getInstance();
         return this.getEntry(cal);
     }
 
     /** 
      * {@inheritDoc}
      */
     public Day getEntry(Calendar day) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
         // Convert the Calendar object to a String in the same format
         // used in the database
         String date_string = date_format.format(day.getTime());
         // Query the database
         Cursor cur = db.rawQuery(
                 "SELECT " + Day_TABLE + "." + DAY_ID + ", " +
                         Day_TABLE + "." + DAY_DATE + ", " +
                         Day_TABLE + "." + DAY_PHOTO + ", " +
                         Day_TABLE + "." + ENTRY_MOOD_ID + ", " +
                         Entry_TABLE + "." + ENTRY_ID + ", " +
                         Entry_TABLE + "." + ENTRY_NOTE + ", " +
                         Entry_TABLE + "." + ENTRY_TIME +
                 " FROM " + Day_TABLE + " LEFT OUTER JOIN " + Entry_TABLE +
                             " ON " + Day_TABLE + "." + DAY_ID + "=" +
                         Entry_TABLE + "." + ENTRY_DAY_ID +
                 " WHERE " + Day_TABLE + "." + DAY_DATE + "=?" +
                 " ORDER BY " + Entry_TABLE + "." + ENTRY_TIME + " ASC ",
                 new String[] {date_string}
         );
 
         Day e = createEntryWithNotesFromCursor(cur);
         cur.close();
         return e;
     }
 
     /**
      * {@inheritDoc}
      */
     public Day getEntry(long id) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
         // Query the database
         Cursor cur = db.rawQuery(
                 "SELECT " + Day_TABLE + "." + DAY_ID + ", " +
                         Day_TABLE + "." + DAY_DATE + ", " +
                         Day_TABLE + "." + DAY_PHOTO + ", " +
                         Day_TABLE + "." + ENTRY_MOOD_ID + ", " +
                         Entry_TABLE + "." + ENTRY_ID + ", " +
                         Entry_TABLE + "." + ENTRY_NOTE + ", " +
                         Entry_TABLE + "." + ENTRY_TIME +
                 " FROM " + Day_TABLE + " LEFT OUTER JOIN " + Entry_TABLE +
                             " ON " + Day_TABLE + "." + DAY_ID + "=" +
                         Entry_TABLE + "." + ENTRY_DAY_ID +
                 " WHERE " + Day_TABLE + "." + DAY_ID + "=?"+
                 " ORDER BY " + Entry_TABLE + "." + ENTRY_TIME + " ASC ",
                 new String[] {Long.toString(id)}
         );
 
         Day e = createEntryWithNotesFromCursor(cur);
         cur.close();
         return e;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Day createEntry() {
         Calendar day = Calendar.getInstance();
         return createEntry(day);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Day createEntry(Calendar day) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         // Throw InvalidOperationException if an Day for day already exists
         if (existsEntry(day)) {
             throw new InvalidOperationException();
         }
         // Insert the new Day
         else {
             ContentValues cv = new ContentValues();
             cv.put(DAY_DATE, date_format.format(day.getTime()));
             long newEntryId = db.insert(Day_TABLE, null, cv);
             return getEntry(newEntryId);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void deleteEntry(Day day){
     	
     	// Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         // Check if the Day is todays Day and can be deleted
         if (day.canBeDeleted()) {
             //Deletes the selected day
             db.delete(Day_TABLE, DAY_ID + "=?",
                     new String [] {String.valueOf(day.getId())});
         }
         else {
             // The Entry can't be deleted
             throw new InvalidOperationException();
         }	
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean existsEntry() {
         Calendar day = Calendar.getInstance();
         return existsEntry(day);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean existsEntry(Calendar day) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
         return (getEntry(day) == null) ? false : true;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Calendar> getDays() {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         // Select all the days stored in the database (they are UNIQUE)
         Cursor cur = db.rawQuery(
                 "SELECT " + Day_TABLE + "." + DAY_DATE +
                 " FROM " + Day_TABLE +
                 " ORDER BY " + Day_TABLE + "." + DAY_DATE + " DESC",
                 null
         );
 
         // Create the list to return
         List<Calendar> days = new ArrayList<Calendar>();
         // Create a calendar instance
         Calendar date;
         // Processes the query result with the cursor
         if (cur.moveToFirst()) {
             do {
                 try {
                     // Fill the list with the dates
                     date = Calendar.getInstance();
                     date.setTime(date_format.parse(cur.getString(0)));
                     days.add(date);
                 } catch (ParseException e) {
                 	throw new DatabaseError();
                 }
             }
             while (cur.moveToNext());
         }
         cur.close();
         return days;
     }
 
     /**
      * {@inheritDoc}
      */
     public Entry insertNote(Day day, String note_text) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         // Check if it's possible to add a Entry to the given Day
         if (day.canBeUpdated()) {
             // Insert the Entry in the database
             Calendar now = Calendar.getInstance();
             ContentValues cv = new ContentValues();
             cv.put(ENTRY_DAY_ID, day.getId());
             cv.put(ENTRY_NOTE, note_text);
             cv.put(ENTRY_TIME, time_format.format(now.getTime()));
             long id = db.insert(Entry_TABLE, null, cv);
 
             //Create the Entry object to return
             return new Entry(id, note_text, now);
         }
         else {
             // The Day can't be updated with a new Entry
             throw new InvalidOperationException();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Entry getNote(long id) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         // Query the database for the note
         Cursor cur = db.rawQuery(
                 "SELECT " + Entry_TABLE + "." + ENTRY_ID + ", " +
                         Entry_TABLE + "." + ENTRY_NOTE + ", " +
                         Entry_TABLE + "." + ENTRY_TIME + ", " +
                         Day_TABLE + "." + DAY_DATE +
                 " FROM " + Entry_TABLE + " INNER JOIN " + Day_TABLE +
                         " ON " + Entry_TABLE + "." + ENTRY_DAY_ID + "=" +
                         Day_TABLE + "." + DAY_ID +
                 " WHERE " + Entry_TABLE + "." + ENTRY_ID + "=?",
                 new String[] {Long.toString(id)}
         );
 
         // If a Entry with the given id exists return it
         if (cur.moveToFirst()) {
             // Compute the Entry time from DAY_DATE and ENTRY_TIME
             Calendar note_time = Calendar.getInstance();
             Calendar entry_date = Calendar.getInstance();
             try {
                 note_time.setTime(time_format.parse(cur.getString(2)));
                 entry_date.setTime(date_format.parse(cur.getString(3)));
             } catch (ParseException e) {
             	throw new DatabaseError();
             }
             note_time.set(Calendar.YEAR, entry_date.get(Calendar.YEAR));
             note_time.set(Calendar.MONTH, entry_date.get(Calendar.MONTH));
             note_time.set(Calendar.DATE, entry_date.get(Calendar.DATE));
 
             // Create and return the Entry
             Entry n = new Entry(
                     cur.getLong(0),     // ENTRY_ID
                     cur.getString(1),   // ENTRY_NOTE
                     note_time           // DAY_DATE + ENTRY_TIME
             );
             cur.close();
             return n;
         }
         // Entry not found
         else {
             return null;
         }
 
     }
     
     /**
      * {@inheritDoc}
      */
     public Entry updateNote(Entry note, String new_note_text)  {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         // Get the sharedPreferences (for the Entry "grace period")
         SharedPreferences sharedPreferences = context.getSharedPreferences(
                 AppConstants.SHARED_PREFERENCES_FILENAME,
                 Context.MODE_PRIVATE
         );
 
         // Check if the Entry can be updated
         if (note.canBeUpdated(sharedPreferences)) {
             // Update the Entry
             ContentValues cv = new ContentValues();
             cv.put(ENTRY_NOTE, new_note_text);
             long id = db.update(Entry_TABLE, cv, ENTRY_ID + "=?",
                     new String []{String.valueOf(note.getId())}
             );
 
             return new Entry(id, new_note_text, note.getTime());
         }
         else {
             // The Entry can't be updated
             throw new InvalidOperationException();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void deleteNote(Entry note) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         // Get the sharedPreferences (for the Entry "grace period")
         SharedPreferences sharedPreferences = context.getSharedPreferences(
                 AppConstants.SHARED_PREFERENCES_FILENAME,
                 Context.MODE_PRIVATE
         );
 
         // Check if the Entry can be deleted
         if (note.canBeDeleted(sharedPreferences)) {
             //Deletes the selected row from the Notes table in the database
             db.delete(Entry_TABLE, ENTRY_ID + "=?",
                     new String [] {String.valueOf(note.getId())});
         }
         else {
             // The Entry can't be deleted
             throw new InvalidOperationException();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void setMood(Day day, Mood mood) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         // Check if the Day can be updated
         if (day.canBeUpdated()) {
             // Update the Mood of the Day
             ContentValues cv = new ContentValues();
             cv.put(ENTRY_MOOD_ID, mood.getId());
             db.update(Day_TABLE, cv, DAY_ID + "=?",
                     new String [] {String.valueOf(day.getId())}
             );
         }
         else {
             // The Day can't be updated
             throw new InvalidOperationException();
         }
 
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeMood(Day day) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         // Check if the Day can be updated
         if (day.canBeUpdated()) {
             // Set to NULL the Mood Column of day
             ContentValues cv = new ContentValues();
             cv.putNull(ENTRY_MOOD_ID);
             db.update(Day_TABLE, cv, DAY_ID + "=?",
                     new String []{String.valueOf(day.getId())}
             );
         }
         else {
             // Day can't be updated
             throw new InvalidOperationException();
         }
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
     public Photo setPhoto(Day day, Bitmap btmp) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         // If the Day can't be updated throw the InvalidOperationException
         if (!day.canBeUpdated()) {
             throw new InvalidOperationException();
         }
 
         //Gets the path and the directory name where the Photo is going to be saved
         String path = Environment.getExternalStorageDirectory().getAbsolutePath();
         File photoDir = new File(
                 path + File.separator + AppConstants.EXTERNAL_STORAGE_PHOTO_DIR
         );
         //Creates the file
         String fileName = "Photo_" + day.getId() + ".jpg";
         File file = new File (photoDir, fileName);
         //Delete if already exists
         if (file.exists ()) {
             file.delete ();
         }
         if (! photoDir.exists()){
             photoDir.mkdirs();
         }
         //Writes the file with the picture in the selected path
         try {
             file.createNewFile();
             FileOutputStream out = new FileOutputStream(file);
             btmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
             out.flush();
             out.close();
 
         } catch (Exception e) {
                e.printStackTrace();
         }
 
         // Update the path to the file in the database
         ContentValues cv = new ContentValues();
         cv.put(DAY_PHOTO, file.getAbsolutePath());
         db.update(Day_TABLE, cv, DAY_ID + "=?",
                 new String []{String.valueOf(day.getId())}
         );
 
         return new Photo(file.getAbsolutePath());
     }
 
     /**
      * {@inheritDoc}
      */
     public void removePhoto(Day day) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
         
         // Check if the Photo can be deleted
         if (day.canBeUpdated()) {
             // Remove the photo and his thumb from external storage
             File photo = new File(day.getPhoto().getPath());
             photo.delete();
             File thumb = new File(day.getPhoto().getPathThumb());
             thumb.delete();
             // Remove the photo from the database
             ContentValues cv = new ContentValues();
             cv.putNull(DAY_PHOTO);
             db.update(Day_TABLE, cv, DAY_ID + "=?",
                     new String []{String.valueOf(day.getId())}
             );
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
