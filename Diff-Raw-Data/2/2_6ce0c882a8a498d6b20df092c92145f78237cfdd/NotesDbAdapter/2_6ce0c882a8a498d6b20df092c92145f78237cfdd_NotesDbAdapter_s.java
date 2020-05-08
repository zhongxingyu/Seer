 package com.zachlatta.frc_scout;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Notes database access helper class. Defines basic CRUD operations for the notes and gives the ability to list all
  * notes as well as retrieve or modify a specific note.
  */
 public class NotesDbAdapter
 {
     public static final String KEY_NAME = "name";
     public static final String KEY_NUMBER = "number";
     public static final String KEY_GAMEPLAY_SHOOTING = "gameplay_shooting";
     public static final String KEY_GAMEPLAY_CLIMBING = "gameplay_climbing";
     public static final String KEY_GAMEPLAY_DEFENSE = "gameplay_defense";
     public static final String KEY_NOTES = "notes";
     public static final String KEY_ROWID = "_id";
 
     private static final String TAG = "NotesDBAdapter";
     private DatabaseHelper mDbHelper;
     private SQLiteDatabase mDb;
 
     private static final String DATABASE_NAME = "data";
     private static final String DATABASE_TABLE = "notes";
     private static final int DATABASE_VERSION = 3;
     private static final String DATABASE_CREATE =
             DATABASE_TABLE + " (" + KEY_ROWID + " integer primary key autoincrement, "
             + KEY_NAME + " not null, "
             + KEY_NUMBER + " text not null, "
             + KEY_NOTES + " text not null, "
             + KEY_GAMEPLAY_SHOOTING + " integer not null, "
             + KEY_GAMEPLAY_CLIMBING + " integer not null, "
             + KEY_GAMEPLAY_DEFENSE + " integer not null);";
 
     private final Context mCtx;
 
     private static class DatabaseHelper extends SQLiteOpenHelper
     {
         DatabaseHelper(Context context)
         {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db)
         {
            db.execSQL(DATABASE_CREATE);
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
         {
             Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy "
                     + "all old data.") ;
 
             try
             {
                 db.execSQL("create table if not exists " + DATABASE_CREATE);
 
                 List<String> columns = GetColumns(db, DATABASE_TABLE);
                 db.execSQL("alter table " + DATABASE_TABLE + " rename to 'temp_" + DATABASE_TABLE + "'");
                 db.execSQL("create table " + DATABASE_CREATE);
                 columns.retainAll(GetColumns(db, DATABASE_TABLE));
                 String cols = join(columns, ",");
                 db.execSQL(String.format("insert into %s (%s) select %s from temp_%s", DATABASE_TABLE, cols, cols,
                         DATABASE_TABLE));
                 db.execSQL("drop table 'temp_" + DATABASE_TABLE + "'");
                 db.setTransactionSuccessful();
             }
             finally
             {
                 db.endTransaction();
             }
         }
 
         public static List<String> GetColumns(SQLiteDatabase db, String tableName)
         {
             List<String> ar = null;
             Cursor c = null;
 
             try
             {
                 c = db.rawQuery("select * from " + tableName + " limit 1", null);
 
                 if(c != null)
                 {
                     ar = new ArrayList<String>(Arrays.asList(c.getColumnNames()));
                 }
             }
             catch(Exception e)
             {
                 Log.v(tableName, e.getMessage(), e);
                 e.printStackTrace();
             }
             finally
             {
                 if(c != null)
                 {
                     c.close();
                 }
             }
 
             return ar;
         }
 
         public static String join(List<String> list, String delim)
         {
             StringBuilder buf = new StringBuilder();
             int num = list.size();
 
             for(int i = 0; i < num; i++)
             {
                 if(i != 0)
                 {
                     buf.append(delim);
                 }
 
                 buf.append((String) list.get(i));
             }
 
             return buf.toString();
         }
     }
 
     /**
      * Constructor - takes the context to allow the database to be opened/created.
      *
      * @param ctx The Context in which to work.
      */
     public NotesDbAdapter(Context ctx)
     {
         this.mCtx = ctx;
     }
 
     /**
      * Open the notes database. If it cannot be opened, try to create a new instance of the database. If it cannot be
      * created, throw an exception to signal the failure.
      *
      * @return this Self reference, allowing this to be chained in an initialization call.
      * @throws SQLException If the database could be neither opened or created.
      */
     public NotesDbAdapter open() throws SQLException
     {
         mDbHelper = new DatabaseHelper(mCtx);
         mDb = mDbHelper.getWritableDatabase();
         return this;
     }
 
     public void close()
     {
         mDbHelper.close();
     }
 
     /**
      * Create a new note using the team name, number, and notes provided. If the note is successfully created, then
      * return the new rowId for that note, otherwise return -1 to indicate failure.
      *
      * @param name   The name of the team.
      * @param number The team's number.
      * @param notes  The notes of the team.
      * @return rowId or -1 if failed.
      */
     public long createNote(String name, String number, boolean gameplayShooting, boolean gameplayClimbing,
                            boolean gameplayDefense, String notes)
     {
         ContentValues initialValues = new ContentValues();
         initialValues.put(KEY_NAME, name);
         initialValues.put(KEY_NUMBER, number);
         initialValues.put(KEY_GAMEPLAY_SHOOTING, gameplayShooting);
         initialValues.put(KEY_GAMEPLAY_CLIMBING, gameplayClimbing);
         initialValues.put(KEY_GAMEPLAY_DEFENSE, gameplayDefense);
         initialValues.put(KEY_NOTES, notes);
 
         return mDb.insert(DATABASE_TABLE, null, initialValues);
     }
 
     /**
      * Delete the note with the given rowId
      *
      * @param rowId Id of note to delete.
      * @return True if deleted, false otherwise.
      */
     public boolean deleteNote(long rowId)
     {
         return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
     }
 
     /**
      * Return a Cursor over the list of all notes in the database.
      *
      * @return Cursor over all notes.
      */
     public Cursor fetchAllNotes()
     {
         return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_NAME, KEY_NUMBER, KEY_GAMEPLAY_SHOOTING,
                 KEY_GAMEPLAY_CLIMBING, KEY_GAMEPLAY_DEFENSE, KEY_NOTES}, null, null, null, null, null);
     }
 
     /**
      * Return a Cursor positioned at the note that matches the given rowId
      *
      * @param rowId Id of note to retrieve.
      * @return Cursor positioned to match note, if found.
      * @throws SQLException If note could not be found/retrieved.
      */
     public Cursor fetchNote(long rowId) throws SQLException
     {
         Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[]{KEY_ROWID, KEY_NAME, KEY_NUMBER,
                 KEY_GAMEPLAY_SHOOTING, KEY_GAMEPLAY_CLIMBING, KEY_GAMEPLAY_DEFENSE, KEY_NOTES}, KEY_ROWID + "=" + rowId,
                 null, null, null, null, null);
 
         if (mCursor != null)
         {
             mCursor.moveToFirst();
         }
 
         return mCursor;
     }
 
     /**
      * Update the note using the details provided. The note to be updated is specified using the rowId, and is altered
      * to use the title and body values passed in.
      *
      * @param rowId            Id of note to update.
      * @param name             Value to set team name to.
      * @param number           Value to set team number to.
      * @param gameplayShooting Value to set gameplay_shooting to.
      * @param gameplayClimbing Value to set gameplay_climbing to.
      * @param gameplayDefense  Value to set gameplay_defense to.
      * @param notes            Values to set team notes to.
      * @return True if the note was successfully updated, false otherwise.
      */
     public boolean updateNote(long rowId, String name, String number, boolean gameplayShooting,
                               boolean gameplayClimbing, boolean gameplayDefense, String notes)
     {
         ContentValues args = new ContentValues();
         args.put(KEY_NAME, name);
         args.put(KEY_NUMBER, number);
         args.put(KEY_GAMEPLAY_SHOOTING, gameplayShooting);
         args.put(KEY_GAMEPLAY_CLIMBING, gameplayClimbing);
         args.put(KEY_GAMEPLAY_DEFENSE, gameplayDefense);
         args.put(KEY_NOTES, notes);
 
         return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
     }
 }
