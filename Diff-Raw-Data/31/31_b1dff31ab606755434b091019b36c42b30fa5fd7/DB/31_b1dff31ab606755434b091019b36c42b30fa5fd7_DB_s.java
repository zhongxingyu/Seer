 package com.github.groupENIGMA.journalEgocentrique.model;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.SQLException;
 import android.database.Cursor;
 
 import java.text.SimpleDateFormat;
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
 
     // Constants specified for the Entry table and its fields
     public static final String Entry_TABLE = "Entry"; 
     public static final String ENTRY_ID = "_id";
     public static final String DATE = "Date";
     public static final String PHOTO = "Photo";
     public static final String MOOD = "Mood";
 
     // Constants specified for the Notes table and its fields
     public static final String Notes_TABLE = "Notes";
     public static final String NOTE_ID = "_id";
     public static final String NOTE_TEXT = "NoteText";
     public static final String NOTE_DATE = "NoteDate";
 
     // Format used by the dates saved in the database
     public static final String DB_DATE_FORMAT= "yyyy-MM-dd";
 
     // A reference to the database used by the application
     private SQLiteDatabase db;
 
     // the Activity or Application that is creating an object from this class.
     Context context;
     openHelper helper;
     
     // Used to convert Calendar to String and String to Calendar
     SimpleDateFormat date_format;
 
     //The database manager constructor
     public DB(Context context) {
 
         this.context = context;
         // create or open the database
         helper = new openHelper(context);
         date_format = new SimpleDateFormat(DB_DATE_FORMAT);
     }
 
     /**
      * Open the connection to the database. You MUST call this method
      * before calling any other method that interacts with the database.
      * Should be called in the onResume() method of the Activity.
      * 
      * @throws SQLException
      */
     public void open() throws SQLException {
 
         db = helper.getWritableDatabase();
     }
 
     /**
      * Closes all the remaining active connections to the database.
      * You should call this method in the onPause() method of the Activity
      */
     public void close() {
 
         db.close();
     }
 
     /**
      * Returns the diary's Entry for today.
      * <p>
      * Only one Entry per day can exists so, if the database already has the
      * Entry for today, the existing one will be returned. Otherwise a new
      * Entry will be created, inserted in the database and then returned.
      * 
      * @return The Entry for today.
      */
     public Entry getEntryOfTheDay() {
 
         Calendar cal = Calendar.getInstance();
         return this.getEntryOfTheDay(cal);
     }
 
     /**
      * Returns the diary's Entry for a given day.
      * <p>
      * Only one Entry per day can exists so, if the database already has the
      * Entry for day, the existing one will be returned. Otherwise a new
      * Entry will be created, inserted in the database and then returned.
      * 
      * @param day
      * @return The Entry for day.
      */
     public Entry getEntryOfTheDay(Calendar day) {
 
         this.open();
 
         String date_string = date_format.format(day.getTime());
 
         Cursor curE = db.rawQuery(
                 "SELECT * " +
                 " FROM " + Entry_TABLE +
                 " WHERE " + DATE + "=?",
                 new String[] {date_string}
         );
         Cursor curN = db.rawQuery(
                 "SELECT *" +
                 " FROM " + Notes_TABLE +
                 " WHERE " + NOTE_DATE + "=?", 
                 new String [] {date_string}
         );
 
         if(curE == null){
             db.close();
             return new Entry(day);
         }
 
         long id = curE.getLong(1);                  //First column is ID
         Photo ph = new Photo(curE.getString(3));    //Third column is Photo
         Mood mo = new Mood(curE.getInt(4));         //Fourth column is Mood
         curE.close();
 
         if(curN != null){
             // code that fills a list of notes and passes it to the Entry constructor
             // Note [] no      ... non array ma arrayList o List
         }
         db.close();
         return new Entry(id, day, ph, mo, no);
 
 
     }
 
     /** Returns the list of days that have an Entry associated
      * <p>
      * @return the list of days in the diary, can be empty on a brand new
      *         database
      */
     public List<Calendar> getDays() {
 
     }
 
     /** Insert a new text Note to the given Entry.
      * <p>
      * It's possible to add a Note to an Entry only during the day of creation
      * of the note.
      * 
      * @param entry
      * @param note_text The text of the Note
      * @return the newly created Note
      * @throws InvalidOperationException When adding a Note to an Entry that
      *         is wasn't created today
      */
     public Note insertNote(Entry entry, String note_text) throws InvalidOperationException {
 
     }
 
     /**
      * Updates the text note of the given Note
      * <p>
      * Each Note has a "grace period" during which it can be updated: calling
      * this method on a Note after the end of the grace period will result
      * in an error.
      *
      * @param note, the Node you want to update
      * @param new_note_text, the new text of the note
      * @return the new version of the Note
      * @throws InvalidOperationException When note's grace period is already
      *         ended.
      */
     public Note updateNote(Note note, String new_note_text) throws InvalidOperationException {
 
     }
 
     /**
      * Deletes the given Note from the database
      * 
      * @param note the Note to be deleted
      * @throws InvalidOperationException If the given Note isn't in the database or if 
      * the operation is not permitted
      */
     public void deleteNote(Note note) throws InvalidOperationException {
 
     }
 
     /**
      * Sets the mood for the given Entry
      * <p>
      * The Mood of an Entry can be modified only during the day the Entry was
      * created.
      * 
      * @param entry the Entry to update
      * @param mood the new Mood
      * @throws InvalidOperationException If the Entry's mood can't be modified
      */
     public void setMood(Entry entry, Mood mood) throws InvalidOperationException {
 
     }
 
     /**
      * Sets the Mood for given Entry to null
      * 
      * @param entry
      * @throws InvalidOperationException if the Entry's mood can't be removed
      */
     public void removeMood(Entry entry) throws InvalidOperationException {
 
     }
 
     /** Returns the list of available Moods.
      * <p>
      * The user can choose to assign to the entries one of these Moods.
      * 
      * @return the list of available Moods
      */
     public List<Mood> getAvailableMoods() {
 
     }
 
     /**
      * Sets the Photo for the given Entry
      * 
      * @param entry the Entry to be updated
      * @param path the URI of the file containing the photo
      * @return the newly created Photo object
      * 
      * @throws InvalidOperationException if the Entry's photo can't be insered
      */
     public Photo setPhoto(Entry entry, String path) throws InvalidOperationException {
 
     }
 
     /**
      * Deletes the given photo from the database
      * 
      * A Photo can be deleted only during the same day it was took.
      * 
      * @param photo the Photo to be deleted
      * @throws InvalidOperationException When deleting a Photo that can't be
      *         deleted.
      */
     public void deletePhoto(Photo photo) throws InvalidOperationException {
 
     }
 
     /**
      * Returns the list of all the Photo saved in the database.
      * 
      * @return the List with all the Photo
      */
     public List<Photo> getPhotos() {
 
     }
 
     /**
      * Return the list of all the Photo taken in the given time range
      * 
      * @param from the starting time of the range
      * @param to the ending time of the range
      * @return the List (can be empty) of matching Photo
      */
     public List<Photo> getPhotos(Calendar from, Calendar to) {
 
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
 
            // This string is used to define the command to create the databases tables.
            String newEntryTable = " create table IF NOT EXISTS" +
                    Entry_TABLE +
                    " (" +
                    ENTRY_ID + " INTEGER PRYMARY KEY autoincrement not null," +
                    DATE + " text UNIQUE not null," +
                    PHOTO + " text," +
                    MOOD + " integer " +
                     " );";
            String newNotesTable = " create table " +
                    Notes_TABLE +
                    " (" +
                    NOTE_ID + " INTEGER PRYMARY KEY autoincrement not null," +
                    NOTE_TEXT + " text," +
                    "FOREIGN KEY(" + NOTE_DATE + ") REFERENCES " +
                     Entry_TABLE + "(" + DATE + ") ON DELETE CASCADE" +
                     " );";
            // execute the query string to the database.
             db.execSQL(newEntryTable);
             db.execSQL(newNotesTable);
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
 
