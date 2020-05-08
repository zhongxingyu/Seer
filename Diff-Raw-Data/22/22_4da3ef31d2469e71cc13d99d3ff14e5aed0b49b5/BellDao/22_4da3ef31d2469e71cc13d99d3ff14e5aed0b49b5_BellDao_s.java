 package com.corsework.notepad.entities.dao;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 import com.corsework.notepad.entities.program.Bell;
 import com.corsework.notepad.entities.program.Sys;
 import com.corsework.notepad.errors.Err;
 import com.corsework.notepad.errors.LastErrors;
 
 public class BellDao {
 	
 	/**
 	 * Helper for current class.
 	 */
 	private BellHelper dbHelper;
 	
 	public BellDao(Context context) {
 		this.dbHelper = new BellHelper(context);
 	}
 	
 	/**
 	 * Creates new record in db-table and gets its id.
 	 * @param bell Bell to save into table.
 	 * @return The bell with it's id from the table,
 	 * 				or null if record was not created.
 	 */
 	public Bell create(Bell bell) {
 		SQLiteDatabase db = this.dbHelper.getWritableDatabase();		 
         ContentValues values = this.bellToValues(bell); 
         long id = db.insert(dbHelper.TABLE_NAME, null, values);
         db.close();
         
         if (id == -1) {
         	Log.d("error!!!", "New Bell was not created.");
         	return null;
         } else {
         	Bell bl = this.getById(id);
         	Log.d("good. Bell created: ", bl.toString());
             return bl;
         }
 	}
 	
 	/**
 	 * Updates or creates new record in db.
 	 * @param bell Bell to update.
 	 * @return Updated bell or null, if something was wrong.
 	 */
 	public Bell update(Bell bell) {
 		LastErrors.getInstance().clean();
 		if (bell.getId() == null) {
 			return this.create(bell);
 		} else {
 			SQLiteDatabase db = this.dbHelper.getWritableDatabase();			 
 			ContentValues values = this.bellToValues(bell);	 
 	        int res = db.update(dbHelper.TABLE_NAME, values, dbHelper.COLUMN_ID + " = ?",
 	                new String[] { String.valueOf(bell.getId()) });	        
 	        db.close();
 	        
 	        if (res != 1) {
 	        	Log.d("error!!! Bell.update:", "Wrong number of rows were modified.");
 	        	return null;
 	        } else {
 	        	Bell bl = this.getById(bell.getId());
 	        	Log.d("good. Bell updated:", bl.toString());
 	        	return bl;
 	        }	        
 		}
 	}
 	
 	/**
 	 * Gets the bell from the table by its id.
 	 * @param id The id of the bell to get.
 	 * @return Bell with the id. Or null, if no bell was found.
 	 */
 	public Bell getById(long id) {
 		SQLiteDatabase db = this.dbHelper.getReadableDatabase();		
 		Cursor cursor = db.query(dbHelper.TABLE_NAME, null, dbHelper.COLUMN_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
 		
 		if (cursor == null) {
 			Log.d("error!!! Bell.getById:", "No Bell with such id was found.");
 			return null;
 		} else if (cursor.getCount() != 1) {
 			Log.d("error!!! Bell.getById:", "More then one bell was found by id.");
 			return null;
 		} else {
 			cursor.moveToFirst();        
 			Bell bell = this.cursorToBell(cursor);        
 			Log.d("good. Bell getById:", bell.toString());        
 			return bell;
 		}
 	}
 	
 	/**
 	 * Gets bells by its ids. Null bells are ignored.
 	 * @param ids Array of bells ids.
 	 * @return ArrayList of not null Bells.
 	 */
 	public ArrayList<Bell> getByIds(ArrayList<Long> ids) {
 		ArrayList<Bell> bells = new ArrayList<Bell>();
 		for (int i = 0; i < ids.size(); ++i) {
 			Bell bell = this.getById(ids.get(i));
 			if (bell != null)
 				bells.add(bell);
 		}
 		return bells;
 	}
 	
 	/**
 	 * Deletes bell from the database by its id.
 	 * @param id Id of the bell to delete.
 	 * @return Number of deleted rows.
 	 */
 	public int deleteById(Long id) {
 		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
         int res = db.delete(dbHelper.TABLE_NAME, dbHelper.COLUMN_ID + " = ?",
                 new String[] { String.valueOf(id) });
         db.close();
         
         if (res != 1) {
         	Log.d("error!!! Bell.deleteById:", "Must delete only one row at once");
         } else {
         	Log.d("good. Bell deleted:", "id = " + id);
         }
         return res;
 	}
 	
 	/**
 	 * Deletes bell from the database.
 	 * Or do nothing, if bell's id == null.
 	 * @param bell Bell to delete (needs only its id).
 	 * @return Number of deleted rows.
 	 */
 	public int delete(Bell bell) {
 		if (bell.getId() != null) {
 			return this.deleteById(bell.getId());
 		} else {
 			Log.e("error!!! Note.delete:", "Trying to delete bell without id");
 			return 0;
 		}
 	}
 	
 	/**
 	 * Deletes bells with ids from the database.
 	 * Do nothing, if there is no such id in table.
 	 * @param ids Array of ids.
 	 * @return Number of deleted rows.
 	 */
 	public int deleteByIds(ArrayList<Long> ids) {
 		int sum = 0;
 		for (int i = 0; i < ids.size(); ++i) {
 			sum += this.deleteById(ids.get(i));
 		}
 		return sum;
 	}
 	
 	/**
 	 * Gets all bells from db.
 	 * @return ArrayList of Bells.
 	 */
 	public ArrayList<Bell> getAll() {
 		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
 		ArrayList<Bell> bells = new ArrayList<Bell>();
 		
 		Cursor cursor = db.query(dbHelper.TABLE_NAME, null, null,
                 null, null, null, null, null);		
 		if (cursor.moveToFirst()) {
             do {
                 Bell bell = this.cursorToBell(cursor);
                 bells.add(bell);
             } while (cursor.moveToNext());
         }
                 
         return bells;
 	}
 	
 	/**
 	 * Transforms cursor into Bell.
 	 * @param cursor Cursor to transform.
 	 * @return Bell with fields from cursor.
 	 */
 	private Bell cursorToBell(Cursor cursor) {
 		Long id = cursor.getLong(0);
 		
 		Date cr = new Date(cursor.getLong(1));
 		Date md = new Date(cursor.getLong(2));
 		Sys sys = new Sys(cr, md);
 		
 		Date date = new Date(cursor.getLong(3));
 		
 		Bell bell = new Bell(id, sys, date);
         return bell;
 	}
 	
 	/**
 	 * Transforms Bell into ContentValues for writing in the database.
 	 * @param bell Bell to transform.
 	 * @return ContentValues with fields from bell.
 	 */
 	private ContentValues bellToValues(Bell bell) {
 		ContentValues values = new ContentValues();
     	values.put(dbHelper.COLUMN_CREATED, bell.getSys().getCr().getTime());
     	values.put(dbHelper.COLUMN_MODIFIED, bell.getSys().getMd().getTime());
         values.put(dbHelper.COLUMN_DATE, bell.getDate().getTime());
         return values;
 	}
 
 }
