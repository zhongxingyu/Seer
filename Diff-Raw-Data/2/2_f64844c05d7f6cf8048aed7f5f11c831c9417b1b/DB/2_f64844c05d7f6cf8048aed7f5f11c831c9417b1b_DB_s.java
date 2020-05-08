 package com.github.groupENIGMA.journalEgocentrique.model;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.SQLException;
 import android.database.Cursor;
 import android.content.ContentValues;
 
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 /**
  * This class implements the Application Database.
  * It creates or updates the database, if already existing
  * it also implements methods to manage the stored data for each Entry
  * 
  * @version 0.1 
  * @author groupENIGMA
  */
 public class DB implements DBInterface {
 
     // Constants about the database
     private static final String DB_NAME = "JournalEgocentrique.db"; 
     private static final int DB_VERSION = 1; 
 
     // ENTRY table
     public static final String Entry_TABLE = "Entry"; 
     public static final String ENTRY_ID = "_id";
     public static final String ENTRY_DATE = "Date";
     public static final String ENTRY_PHOTO = "Photo";
     public static final String ENTRY_MOOD = "Mood";
 
     // NOTE table
     public static final String Notes_TABLE = "Note";
     public static final String NOTE_ID = "_id";
     public static final String NOTE_TEXT = "Text";
     public static final String NOTE_TIME = "Time";
     public static final String NOTE_ENTRY_ID = "Entry_id";
 
     // MOOD table
     public static final String Mood_TABLE = "Mood";
     public static final String MOOD_ID = "_id";
     public static final String MOOD_NAME = "name";
 
     // The Moods available in the first version of the database
     public static final String[][] MOODS_DB_VERSION_1 =  {
             {"0", "Happy"},
             {"1", "Sad"},
             {"2", "Angry"},
             {"3", "Bored"},
             {"4", "Depressed"},
             {"5", "Apathetic"}
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
 
     /** Reads the rows available in the given Cursor to create and return
      * an Entry object with its related Notes
      * <p>
      * This works correctly only if the rows available in the cursor have the
      * following columns in this exact order:
      *      ENTRY_ID, ENTRY_DATE, ENTRY_PHOTO, ENTRY_MOOD, NOTE_ID, NOTE_TEXT,
      *      NOTE_TIME
      * (it's basically the Entry table joined with the Note one)
      * 
      * @param cur The Cursor containing the rows with the Entry data
      * @return an Entry or, if the cur is "empty", null
      */
     private Entry createEntryWithNotesFromCursor(Cursor cur) {
         // Check if the Cursor has at least one row
         if (cur.getCount() == 0) {
             return null;
         }
         else {
             // Move to the first row
             cur.moveToFirst();
             // Get the Entry id
             long entry_id = cur.getLong(0);
             // Get the Entry date
             Calendar entry_date = Calendar.getInstance();
             try {
                 entry_date.setTime(date_format.parse(cur.getString(1)));
             } catch (ParseException e) {
                 e.printStackTrace();
             }
             // Get the Entry Photo (if exists)
             Photo entry_photo;
             if (cur.isNull(2)) {
                 entry_photo = null;
             }
             else {
                 entry_photo= new Photo(cur.getString(2));
             }
             // Get the Entry Mood (if exists)
             Mood entry_mood;
             if (cur.isNull(3)) {
                 entry_mood = null;
             }
             else {
                 entry_mood = new Mood(cur.getLong(3));
             }
             // Get all the Notes (if any)
             ArrayList<Note> note_list = new ArrayList<Note>();
             if (!cur.isNull(4)) {  // Notes are available only if NOTE_ID!=NULL
                 do {
                     // Parse the time from the database string
                     Calendar note_time = Calendar.getInstance();
                     try {
                         note_time.setTime(time_format.parse(cur.getString(6)));
                     } catch (ParseException e) {
                         e.printStackTrace();
                     }
                     // Set the year, month and day using the entry_date
                     note_time.set(Calendar.YEAR, entry_date.get(Calendar.YEAR));
                     note_time.set(Calendar.MONTH, entry_date.get(Calendar.MONTH));
                     note_time.set(Calendar.DATE, entry_date.get(Calendar.DATE));
                     // Create the Note and add it to the list
                     Note note = new Note(
                             cur.getLong(4),     // NOTE_ID
                             cur.getString(5),   // NOTE_TEXT
                             note_time
                             );
                     note_list.add(note);
                 }
                 while (cur.moveToNext());
             }
             // Create and return the Entry
             return new Entry(entry_id, entry_date, entry_photo, entry_mood,
                     note_list);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Entry getEntry() {
         Calendar cal = Calendar.getInstance();
         return this.getEntry(cal);
     }
 
     /** 
      * {@inheritDoc}
      */
     public Entry getEntry(Calendar day) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
         // Convert the Calendar object to a String in the same format
         // used in the database
         String date_string = date_format.format(day.getTime());
         // Query the database
         Cursor cur = db.rawQuery(
                 "SELECT " + Entry_TABLE + "." + ENTRY_ID    + ", " +
                             Entry_TABLE + "." + ENTRY_DATE  + ", " +
                             Entry_TABLE + "." + ENTRY_PHOTO + ", " +
                             Entry_TABLE + "." + ENTRY_MOOD  + ", " +
                             Notes_TABLE + "." + NOTE_ID     + ", " +
                             Notes_TABLE + "." + NOTE_TEXT   + ", " +
                             Notes_TABLE + "." + NOTE_TIME   +
                 " FROM " + Entry_TABLE + " LEFT OUTER JOIN " + Notes_TABLE +
                             " ON " + Entry_TABLE + "." + ENTRY_ID + "=" +
                                      Notes_TABLE + "." + NOTE_ENTRY_ID +
                 " WHERE " + Entry_TABLE + "." + ENTRY_DATE + "=?" +
                 " ORDER BY " + Notes_TABLE + "." + NOTE_TIME + " ASC ",
                 new String[] {date_string}
         );
 
         Entry e = createEntryWithNotesFromCursor(cur);
         cur.close();
         return e;
     }
 
     /**
      * {@inheritDoc}
      */
     public Entry getEntry(long id) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
         // Query the database
         Cursor cur = db.rawQuery(
                 "SELECT " + Entry_TABLE + "." + ENTRY_ID    + ", " +
                             Entry_TABLE + "." + ENTRY_DATE  + ", " +
                             Entry_TABLE + "." + ENTRY_PHOTO + ", " +
                             Entry_TABLE + "." + ENTRY_MOOD  + ", " +
                             Notes_TABLE + "." + NOTE_ID     + ", " +
                             Notes_TABLE + "." + NOTE_TEXT   + ", " +
                             Notes_TABLE + "." + NOTE_TIME   +
                 " FROM " + Entry_TABLE + " LEFT OUTER JOIN " + Notes_TABLE +
                             " ON " + Entry_TABLE + "." + ENTRY_ID + "=" +
                                      Notes_TABLE + "." + NOTE_ENTRY_ID +
                 " WHERE " + Entry_TABLE + "." + ENTRY_ID + "=?"+
                 " ORDER BY " + Notes_TABLE + "." + NOTE_TIME + " ASC ",
                 new String[] {Long.toString(id)}
         );
 
         Entry e = createEntryWithNotesFromCursor(cur);
         cur.close();
         return e;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Entry createEntry() {
         Calendar day = Calendar.getInstance();
         return createEntry(day);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Entry createEntry(Calendar day) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         // Throw InvalidOperationException if an Entry for day already exists
         if (existsEntry(day)) {
             throw new InvalidOperationException();
         }
         // Insert the new Entry
         else {
             ContentValues cv = new ContentValues();
             cv.put(ENTRY_DATE, date_format.format(day.getTime()));
             long newEntryId = db.insert(Entry_TABLE, null, cv);
             return getEntry(newEntryId);
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
                 "SELECT " + Entry_TABLE + "." + ENTRY_DATE +
                 " FROM " + Entry_TABLE +
                 " ORDER BY " + Entry_TABLE + "." + ENTRY_DATE + " DESC",
                 null
         );
 
         // Create the list to return
         List<Calendar> days = new ArrayList<Calendar>();
         //Create a calendar instance
         Calendar date = Calendar.getInstance();
         // Processes the query result with the cursor
         if (cur.moveToFirst()) {
             do {
                 try {
                     // Fill the list with the dates
                    date.setTime(date_format.parse(cur.getString(1)));
                     days.add(date);
                 } catch (ParseException e) {
                     e.printStackTrace();
                     return null;
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
     public Note insertNote(Entry entry, String note_text)  throws InvalidOperationException {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         long entryId = entry.getId();
         ContentValues cv=new ContentValues();
 
         //Insert the note text referred to the entry id
         cv.put(NOTE_TEXT, note_text);
         cv.put(NOTE_ENTRY_ID, entryId);
         long id = db.insert(Notes_TABLE, NOTE_ID, cv);
 
         Calendar cal = Calendar.getInstance();
 
         //Create the note to return
         return new Note(id, note_text, cal);
     }
 
     /**
      * {@inheritDoc}
      */
     public Note updateNote(Note note, String new_note_text) throws InvalidOperationException {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         ContentValues cv=new ContentValues();
 
         //Put the new text into the database at the chosen id
         cv.put(NOTE_TEXT, new_note_text);
         long id = db.update(Notes_TABLE, cv, NOTE_ID + "=?",
                 new String []{String.valueOf(note.getId())}
         );
 
         return new Note(id, new_note_text, note.getTime());
     }
 
     /**
      * {@inheritDoc}
      */
     public void deleteNote(Note note) throws InvalidOperationException{
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
         //Deletes the selected row from the Notes table in the database
         db.delete(Notes_TABLE, NOTE_ID + "=?",
                 new String [] {String.valueOf(note.getId())});
     }
 
     /**
      * {@inheritDoc}
      */
     public void setMood(Entry entry, Mood mood) throws InvalidOperationException {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         ContentValues cv=new ContentValues();
 
         //Put the new path String in the mood column
         cv.put(ENTRY_MOOD, mood.getId());
         db.update(Entry_TABLE, cv, ENTRY_ID + "=?", new String []{String.valueOf(entry.getId())});
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeMood(Entry entry) throws InvalidOperationException {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         ContentValues cv=new ContentValues();
 
         //Removes the image path from the mood column
         cv.putNull(ENTRY_MOOD);
         db.update(Entry_TABLE, cv, ENTRY_ID + "=?", new String []{String.valueOf(entry.getId())});
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
                 Mood md = new Mood(cur.getLong(1));
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
     public Photo setPhoto(Entry entry, String path) throws InvalidOperationException {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         ContentValues cv=new ContentValues();
 
         //Put the new path String in the Photo column
         cv.put(ENTRY_PHOTO, path);
         db.update(Entry_TABLE, cv, ENTRY_ID + "=?", new String []{String.valueOf(entry.getId())});
 
         return new Photo(path);
     }
 
     /**
      * {@inheritDoc}
      */
     public void deletePhoto(Photo photo) throws InvalidOperationException {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
         // TODO
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Photo> getPhotos() {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
         //Sets the "to" day on tomorrow to get all the Entries
         Calendar to = Calendar.getInstance();
         to.add(Calendar.DAY_OF_YEAR, 1);
         //sets the "from" day on 01-01-1970 to get all the Entries
         Calendar from = Calendar.getInstance();
         from.set(1970, 01, 01);
 
         return getPhotos(from, to);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Photo> getPhotos(Calendar from, Calendar to) {
         // Check if the Connection to the DB is open
         raiseConnectionExceptionIfNotConnected();
 
         ArrayList<Photo> photos = new ArrayList<Photo>();
         // Query the database
         Cursor cur = db.query(Entry_TABLE, new String[] {ENTRY_PHOTO}, ENTRY_DATE + " BETWEEN ? AND ?", new String[] {
                 from.toString(), to.toString() }, null, null, null, null);
         
         if (cur.moveToFirst()) {
             do {
                 photos.add(new Photo(cur.getString(1)));
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
             String newMoodTable =
                     "CREATE TABLE IF NOT EXISTS " + Mood_TABLE + " ( " +
                     MOOD_ID     + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                     MOOD_NAME   + " TEXT" +
                     " );";
             String newEntryTable = 
                     "CREATE TABLE IF NOT EXISTS " + Entry_TABLE + " ( " +
                     ENTRY_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                     ENTRY_DATE  + " TEXT UNIQUE NOT NULL," +
                     ENTRY_PHOTO + " TEXT," +
                     ENTRY_MOOD  + " INTEGER, " +
                     "CONSTRAINT fk_Mood FOREIGN KEY(" + ENTRY_MOOD + ")" +
                     "REFERENCES " + Mood_TABLE + "(" + MOOD_ID + ")" +
                     " );";
             
             String newNotesTable =
                     "CREATE TABLE IF NOT EXISTS " + Notes_TABLE + " ( " +
                     NOTE_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                     NOTE_TEXT       + " TEXT," +
                     NOTE_TIME       + " TEXT," +
                     NOTE_ENTRY_ID   + " INTEGER," +
                     "CONSTRAINT fk_Notes FOREIGN KEY(" + NOTE_ENTRY_ID + ") " +
                         "REFERENCES " + Entry_TABLE + "(" + ENTRY_ID + ")" +
                     " );";
             
             // Run all the CREATE TABLE ...
             db.execSQL(newMoodTable);
             db.execSQL(newEntryTable);
             db.execSQL(newNotesTable);
             
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
     }
 
 }
