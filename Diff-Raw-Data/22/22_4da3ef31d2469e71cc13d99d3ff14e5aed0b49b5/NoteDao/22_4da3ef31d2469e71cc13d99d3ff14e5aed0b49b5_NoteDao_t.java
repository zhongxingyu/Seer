 package com.corsework.notepad.entities.dao;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Set;
 
 import com.corsework.notepad.entities.program.Bell;
 import com.corsework.notepad.entities.program.Note;
 import com.corsework.notepad.entities.program.Sys;
 import com.corsework.notepad.errors.Err;
 import com.corsework.notepad.errors.LastErrors;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class NoteDao {
 	
 	/**
 	 * Helper for current class.
 	 */
 	private NoteHelper dbHelper;
 	
 	public NoteDao(Context context) {
 		this.dbHelper = new NoteHelper(context);
 	}
 	
 	/**
 	 * Creates new record in db-table and gets its id.
 	 * @param note Note to save into table.
 	 * @return Note with its id from the table,
 	 * 		or null, if record was not created.
 	 */
 	public Note create(Note note) {
 		SQLiteDatabase db = this.dbHelper.getWritableDatabase();		 
         ContentValues values = this.noteToValues(note); 
         long id = db.insert(dbHelper.TABLE_NAME, null, values);
         db.close();
         
         if (id == -1) {
         	Log.e("error!!! Note.create:", "New record was not created.");
         	return null;
         } else {
         	Note nt = this.getById(id);
         	Log.d("good. Note created: ", nt.toString());
             return nt;
         }
 	}
 	
 	/**
 	 * Updates or creates new record in db.
 	 * @param note Note to update.
 	 * @return Updated Note or null, if something was wrong.
 	 */
 	public Note update(Note note) {
 		LastErrors.getInstance().clean();
 		if (note.getId() == null) {
 			return this.create(note);
 		} else {
 			SQLiteDatabase db = this.dbHelper.getWritableDatabase();			 
 			ContentValues values = this.noteToValues(note);	 
 	        int res = db.update(dbHelper.TABLE_NAME, values, dbHelper.COLUMN_ID + " = ?",
 	                new String[] { String.valueOf(note.getId()) });
 	        db.close();
 	        
 	        if (res != 1) {
 	        	Log.e("error!!! Note.update:", "Wrong number of rows were modified.");
 	        	return null;
 	        } else {
 	        	Note nt = this.getById(note.getId());
 	        	Log.d("good. Note updated: ", nt.toString());
 	        	return nt;
 	        }
 		}
 	}
 	
 	/**
 	 * Gets the note from the table by its id.
 	 * @param id The id of the note to get.
 	 * @return Note with the id. Or null, if no note was found.
 	 */
 	public Note getById(long id) {
 		SQLiteDatabase db = this.dbHelper.getReadableDatabase();		
 		Cursor cursor = db.query(dbHelper.TABLE_NAME, null, dbHelper.COLUMN_ID + "=?",
                 new String[] { String.valueOf(id) }, null, null, null, null);
 		
 		if (cursor == null) {
 			Log.e("error!!! Note.getById:", "No Bell with such id was found");
 			return null;
 		} else if (cursor.getCount() != 1) {
 			Log.e("error!!! Note.getById:", "More then one note was found by id.");
 			return null;
 		} else {
 			cursor.moveToFirst();        
 			Note note = this.cursorToNote(cursor);        
 			Log.d("good. Bell getById:", note.toString());        
 			return note;
 		}
 	}
 	
 	/**
 	 * Deletes note from the database by its id.
 	 * @param id Id of the note to delete.
 	 * @return Number of deleted rows.
 	 */
 	public int deleteById(Long id) {
 		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
         int res = db.delete(dbHelper.TABLE_NAME, dbHelper.COLUMN_ID + " = ?",
                 new String[] { String.valueOf(id) });
         db.close();
         
         if (res != 1) {
         	Log.e("error!!! Note.deleteById:", "Must delete only one row at once");
         } else {
         	Log.d("good. Note deleted:", "id = " + id);
         }
         return res;
 	}
 	
 	/**
 	 * Deletes note from the database.
 	 * Or do nothing, if note's id == null.
 	 * @param note Note to delete (needs only its id).
 	 * @return Number of deleted rows.
 	 */
 	public int delete(Note note) {
 		if (note.getId() != null) {
 			return this.deleteById(note.getId());
 		} else {
 			Log.e("error!!! Note.delete:", "Trying to delete note without id");
 			return 0;
 		}
 	}
 
 	/**
 	 * Gets all notes from database.
 	 * @return ArrayList of Bells.
 	 */
 	public ArrayList<Note> getAll() {
 		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
 		ArrayList<Note> notes = new ArrayList<Note>();
 		
 		Cursor cursor = db.query(dbHelper.TABLE_NAME, null, null,
                 null, null, null, null, null);		
 		if (cursor.moveToFirst()) {
             do {
                 Note note = this.cursorToNote(cursor);
                 notes.add(note);
             } while (cursor.moveToNext());
         }
                 
         return notes;
 	}
 	
 	/**
 	 * Gets all notes with type among types.
 	 * @param types Necessary types of notes.
 	 * @return Array of notes.
 	 */
 	public ArrayList<Note> getByType(ArrayList<String> types) {
 		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
 		ArrayList<Note> notes = new ArrayList<Note>();
 		
 		for (int i = 0; i < types.size(); ++i) {
 			Cursor cursor = db.query(dbHelper.TABLE_NAME, null, dbHelper.COLUMN_TYPE + "=?",
 					new String[] { types.get(i) }, null, null, null, null);		
 			if (cursor.moveToFirst()) {
 	            do {
 	                Note note = this.cursorToNote(cursor);
 	                notes.add(note);
 	            } while (cursor.moveToNext());
 	        }
 		}
 		
 		return notes;
 	}
 	
 	/**
 	 * Gets all notes, created in period of time.
 	 * @param from Period start date.
 	 * @param to Period end date.
 	 * @return Array of appropriate notes.
 	 */
 	public ArrayList<Note> getByCrDate(Date from, Date to) {
 		LastErrors.getInstance().clean();
 		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
 		ArrayList<Note> notes = new ArrayList<Note>();
 		
 		Cursor cursor = db.query(dbHelper.TABLE_NAME, null,
 				dbHelper.COLUMN_CREATED + ">=?" + " and "
 				+ dbHelper.COLUMN_CREATED + "<=?",
 				new String[] { String.valueOf(from.getTime()),
				String.valueOf(to.getTime()) }, null, null, dbHelper.COLUMN_CREATED + " DESC", null);		
 		if (cursor.moveToFirst()) {
             do {
                 Note note = this.cursorToNote(cursor);
                 notes.add(note);
             } while (cursor.moveToNext());
         }
 		
 		return notes;
 	}
 	
 	/**
 	 * Gets all notes, modified in period of time.
 	 * @param from Period start date.
 	 * @param to Period end date.
 	 * @return Array of appropriate notes.
 	 */
 	public ArrayList<Note> getByMdDate(Date from, Date to) {
 		LastErrors.getInstance().clean();
 		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
 		ArrayList<Note> notes = new ArrayList<Note>();
 		
 		Cursor cursor = db.query(dbHelper.TABLE_NAME, null,
 				dbHelper.COLUMN_MODIFIED + ">=?" + " and "
 				+ dbHelper.COLUMN_MODIFIED + "<=?",
 				new String[] { String.valueOf(from.getTime()),
				String.valueOf(to.getTime()) }, null, null, dbHelper.COLUMN_MODIFIED + " DESC", null);		
 		if (cursor.moveToFirst()) {
             do {
                 Note note = this.cursorToNote(cursor);
                 notes.add(note);
             } while (cursor.moveToNext());
         }
 		
 		return notes;
 	}
 		
 	/**
 	 * Transforms Note into ContentValues for writing in the database.
 	 * @param note Note to transform.
 	 * @return ContentValues with fields from note.
 	 */
 	private ContentValues noteToValues(Note note) {
 		ContentValues values = new ContentValues();
     	values.put(dbHelper.COLUMN_CREATED, note.getSys().getCr().getTime());
     	values.put(dbHelper.COLUMN_MODIFIED, note.getSys().getMd().getTime());
     	values.put(dbHelper.COLUMN_TITLE, note.getTitle());
     	values.put(dbHelper.COLUMN_TYPE, note.getType());
     	values.put(dbHelper.COLUMN_CONTENT, note.getCont());
         return values;
 	}
 	
 	/**
 	 * Transforms cursor into Note.
 	 * @param cursor Cursor to transform.
 	 * @return Note with fields from cursor.
 	 */
 	private Note cursorToNote(Cursor cursor) {
 		Long id = cursor.getLong(0);
 		
 		Date cr = new Date(cursor.getLong(1));
 		Date md = new Date(cursor.getLong(2));
 		Sys sys = new Sys(cr, md);
 		
 		String title = cursor.getString(3);
 		String type = cursor.getString(4);
 		String cont = cursor.getString(5);
 		
 		Note note = new Note(id, sys, title, type, cont);        
         return note;
 	}
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
