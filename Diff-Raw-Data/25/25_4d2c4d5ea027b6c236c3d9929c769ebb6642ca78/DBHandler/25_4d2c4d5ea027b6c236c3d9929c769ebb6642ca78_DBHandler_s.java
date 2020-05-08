 package com.mobileproto.lab2;
 
 /**
  * Created by mmay on 9/17/13.
  */
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteOpenHelper;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class DBHandler extends SQLiteOpenHelper {
 
     private static final int DATABASE_VERSION = 1;
     private static final String DATABASE_NAME = "noteDB.db";
     private static final String TABLE_NOTES = "notes";
 
     public static final String COLUMN_ID = "_id";
     public static final String COLUMN_NOTETITLE = "noteTitle";
     public static final String COLUMN_NOTETEXT = "noteText";
 
     public DBHandler(Context context, String name,
                        CursorFactory factory, int version) {
         super(context, DATABASE_NAME, factory, DATABASE_VERSION);
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
         // TODO Auto-generated method stub
         String CREATE_PRODUCTS_TABLE = "CREATE TABLE " +
                 TABLE_NOTES + "("
                 + COLUMN_ID + " INTEGER PRIMARY KEY," + COLUMN_NOTETITLE
                 + " TEXT," + COLUMN_NOTETEXT + " STRING" + ")";
         db.execSQL(CREATE_PRODUCTS_TABLE);
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
         onCreate(db);
     }
 
     public ArrayList<Note> allFullNotes() {
         ArrayList<Note> notes = new ArrayList<Note>();
 
         String query = "Select * FROM " + TABLE_NOTES;
 
         SQLiteDatabase db = this.getWritableDatabase();
 
         Cursor cursor = db.rawQuery(query, null);
 
 
         if (cursor.moveToFirst()) {
             cursor.moveToFirst();
             do {
                 Note note = new Note();
                 note.setID(Integer.parseInt(cursor.getString(0)));
                 note.setNoteTitle(cursor.getString(1));
                 note.setNoteText(cursor.getString(2));
                 notes.add(note);
             } while (cursor.moveToNext());
         }
 
         cursor.close();
         db.close();
         return notes;
     }
 
     public ArrayList<String> allNotes() {
         ArrayList<String> notes = new ArrayList<String>();
 
         String query = "Select * FROM " + TABLE_NOTES;
 
         SQLiteDatabase db = this.getWritableDatabase();
 
         Cursor cursor = db.rawQuery(query, null);
 
 
         if (cursor.moveToFirst()) {
             cursor.moveToFirst();
             do {
                 notes.add(cursor.getString(1));
             } while (cursor.moveToNext());
         }
 
         cursor.close();
         db.close();
         return notes;
     }
 
     public void addNote(Note note) {
 
         ContentValues values = new ContentValues();
         values.put(COLUMN_NOTETITLE, note.getNoteTitle());
         values.put(COLUMN_NOTETEXT, note.getNoteText());
 
         SQLiteDatabase db = this.getWritableDatabase();
 
         db.insert(TABLE_NOTES, null, values);
         db.close();
     }
 
     public Note findNote(String noteTitle) {
        ArrayList<Note> notes = allFullNotes();
        for (int i=0; i<notes.size(); i++) {
           Note n = notes.get(i);
           if (n.getNoteTitle().toString().equals(noteTitle.toString())) {
               return n;
           }
         }
        return null;
     }
 
     public boolean deleteNote(String noteTitle) {
 
         boolean result = false;
 
         String query = "Select * FROM " + TABLE_NOTES + " WHERE " + COLUMN_NOTETITLE + " =  \"" + noteTitle + "\"";
 
         SQLiteDatabase db = this.getWritableDatabase();
 
         Cursor cursor = db.rawQuery(query, null);
 
         Note note = new Note();
 
         if (cursor.moveToFirst()) {
             note.setID(Integer.parseInt(cursor.getString(0)));
             db.delete(TABLE_NOTES, COLUMN_ID + " = ?",
                     new String[] { String.valueOf(note.getID()) });
             cursor.close();
             result = true;
         }
         db.close();
         return result;
     }
 }
