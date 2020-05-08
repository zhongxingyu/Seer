 package com.corsework.notepad.entities.dao;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import com.corsework.notepad.entities.program.Bell;
 import com.corsework.notepad.entities.program.Note;
 import com.corsework.notepad.entities.program.Reminder;
 import com.corsework.notepad.entities.program.Sys;
 import com.corsework.notepad.errors.Err;
 import com.corsework.notepad.errors.LastErrors;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 public class ReminderDao {
 	
 	/**
 	 * Helper for current class.
 	 */
 	private ReminderHelper dbHelper;
 	
 	private ReminderToBellDao reminderToBellDao;
 	private BellDao bellDao;
 	
 	public ReminderDao(Context context) {
 		this.dbHelper = new ReminderHelper(context);
 		this.reminderToBellDao = new ReminderToBellDao(context);
 		this.bellDao = new BellDao(context);
 	}
 	
 	/**
 	 * Creates new reminder in db-table and gets its id.
 	 * @param reminder Reminder to save into table.
 	 * @return Reminder with its id from the table,
 	 * 		or null, if record was not created.
 	 */
 	public Reminder create(Reminder reminder) {
 		SQLiteDatabase db = this.dbHelper.getWritableDatabase();		 
         ContentValues values = this.reminderToValues(reminder);
         long id = db.insert(dbHelper.TABLE_NAME, null, values);
         db.close();
         
         if (this.createBells(reminder) == false) {
         	Log.e("error!!! Reminder.create:", "Error while creating bells.");
         	return null;
         }
         
         if (id == -1) {
         	Log.e("error!!! Reminder.create:", "New record was not created.");
         	return null;
         } else {
         	Reminder rem = this.getById(id);
         	Log.d("Reminder created: ", rem.toString());
             return rem;
         }
 	}
 	
 	/**
 	 * Creates bells of the reminder and connections between them.
 	 * @param reminder
 	 * @return True, if all operations were successful.
 	 */
 	private boolean createBells(Reminder reminder) {
 		boolean res = true;
 		for (int i = 0; i < reminder.getBells().size(); ++i) {
 			Bell bell = this.bellDao.create(reminder.getBells().get(i));
 			if (bell == null) {
 				res = false;
 			} else if (this.reminderToBellDao.create(reminder, bell) == -1) {
 				res = false;
 			}
 		}
 		return res;
 	}
 	
 	/**
 	 * Updates or creates new record in db.
 	 * @param reminder Reminder to update.
 	 * @return Updated Reminder or null, if something was wrong.
 	 */
 	public Reminder update(Reminder reminder) {
 		LastErrors.getInstance().clean();
 		if (reminder.getId() == null) {
 			return this.create(reminder);
 		} else {
 			this.deleteBells(reminder);
 			
 			SQLiteDatabase db = this.dbHelper.getWritableDatabase();			 
 			ContentValues values = this.reminderToValues(reminder);	 
 	        int res = db.update(dbHelper.TABLE_NAME, values, dbHelper.COLUMN_ID + " = ?",
 	                new String[] { String.valueOf(reminder.getId()) });
 	        db.close();
 	        
 	        if (this.createBells(reminder) == false) {
 	        	Log.e("error!!! Reminder.update:", "Error while creating bells.");
 	        	return null;
 	        }
 	        
 	        if (res != 1) {
 	        	Log.e("error!!! Reminder.update:", "Wrong number of rows were modified.");
 	        	return null;
 	        } else {
 	        	Reminder rem = this.getById(reminder.getId());
 	        	Log.d("good. Reminder updated: ", rem.toString());
 	        	return rem;
 	        }
 		}
 	}
 	
 	/**
 	 * Gets the note from the table by its id.
 	 * @param id The id of the note to get.
 	 * @return Note with the id. Or null, if no note was found.
 	 */
 	public Reminder getById(Long id) {
 		SQLiteDatabase db = this.dbHelper.getReadableDatabase();		
 		Cursor cursor = db.query(dbHelper.TABLE_NAME, null, dbHelper.COLUMN_ID + "=?",
                 new String[] { String.valueOf(id) }, null, null, null, null);
 		
 		if (cursor == null) {
 			Log.e("error!!! Reminder.getById:", "No Bell with such id was found.");
 			return null;
 		} else if (cursor.getCount() != 1) {
 			Log.e("error!!! Reminder.getById:", "More then one reminder was found by id.");
 			return null;
 		} else {
 	        cursor.moveToFirst();        
 	        Reminder reminder = this.cursorToReminder(cursor);        
 	        Log.d("Reminder getById", reminder.toString());        
 	        return reminder;
 		}
 	}
 	
 	/**
 	 * Deletes reminder from the database by its id.
 	 * @param id Id of the reminder to delete.
 	 * @return Number of deleted rows.
 	 */
 	public long deleteById(Long id) {
 		this.deleteBells(id);
 		
 		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
         long res = db.delete(dbHelper.TABLE_NAME, dbHelper.COLUMN_ID + " = ?",
                 new String[] { String.valueOf(id) });
         db.close();
         
         if (res != 1) {
         	Log.e("error!!! Reminder.deleteById:", "Must delete only one row at once");
         } else {
         	Log.d("good. Reminder deleted:", "id = " + id);
         }
         return res;
 	}
 	
 	/**
 	 * Deletes reminder from the database.
 	 * Or do nothing, if reminder's id == null.
 	 * @param reminder Reminder to delete (needs only its id).
 	 * @return Number of deleted rows.
 	 */
 	public long delete(Reminder reminder) {
 		if (reminder.getId() != null) {
 			return this.deleteById(reminder.getId());
 		} else {
 			Log.e("error!!! Reminder.delete:", "Trying to delete reminder without id");
 			return 0;
 		}
 	}
 	
 	/**
 	 * Deletes bells and connections between bells and reminder.
 	 * @param reminderId Id of the reminder.
 	 */
 	private void deleteBells(Long reminderId) {
 		ArrayList<Long> bellsIds = this.reminderToBellDao.getBellsIds(reminderId);
 		this.bellDao.deleteByIds(bellsIds);
 		this.reminderToBellDao.delete(reminderId);
 	}
 	
 	/**
 	 * Deletes bells and connections between bells and reminder.
 	 * @param reminder
 	 */
 	private void deleteBells(Reminder reminder) {
 		this.deleteBells(reminder.getId());
 	}
 	
 	/**
 	 * Gets all reminders from database.
 	 * @return ArrayList of Reminders.
 	 */
 	public ArrayList<Reminder> getAll() {
 		LastErrors.getInstance().clean();
 		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
 		ArrayList<Reminder> reminders = new ArrayList<Reminder>();
 		
 		Cursor cursor = db.query(dbHelper.TABLE_NAME, null, null,
                 null, null, null, null, null);		
 		if (cursor.moveToFirst()) {
             do {
             	Reminder reminder = this.cursorToReminder(cursor);
             	reminders.add(reminder);
             } while (cursor.moveToNext());
         }
                 
         return reminders;
 	}
 	
 	/**
 	 * Gets all reminders with type among types.
 	 * @param types Necessary types of reminders.
 	 * @return Array of reminders.
 	 */
 	public ArrayList<Reminder> getByType(ArrayList<String> types) {
 		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
 		ArrayList<Reminder> reminders = new ArrayList<Reminder>();
 		
 		for (int i = 0; i < types.size(); ++i) {
 			Cursor cursor = db.query(dbHelper.TABLE_NAME, null, dbHelper.COLUMN_TYPE + "=?",
 					new String[] { types.get(i) }, null, null, null, null);		
 			if (cursor.moveToFirst()) {
 	            do {
 	            	Reminder reminder = this.cursorToReminder(cursor);
 	            	reminders.add(reminder);
 	            } while (cursor.moveToNext());
 	        }
 		}
 		
 		return reminders;
 	}
 	
 	/**
 	 * Gets all reminders, created in period of time.
 	 * @param from Period start date.
 	 * @param to Period end date.
 	 * @return Array of appropriate reminders.
 	 */
 	public ArrayList<Reminder> getByCrDate(Date from, Date to) {
 		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
 		ArrayList<Reminder> reminders = new ArrayList<Reminder>();
 		
 		Cursor cursor = db.query(dbHelper.TABLE_NAME, null,
 				dbHelper.COLUMN_CREATED + ">=?" + " and "
 				+ dbHelper.COLUMN_CREATED + "<=?",
 				new String[] { String.valueOf(from.getTime()),
				String.valueOf(to.getTime()) }, null, null, dbHelper.COLUMN_CREATED + " DESC", null);		
 		if (cursor.moveToFirst()) {
             do {
                 Reminder reminder = this.cursorToReminder(cursor);
             	reminders.add(reminder);
             } while (cursor.moveToNext());
         }
 		
 		return reminders;
 	}
 
 	/**
 	 * Gets all reminders, modified in period of time.
 	 * @param from Period start date.
 	 * @param to Period end date.
 	 * @return Array of appropriate reminders.
 	 */
 	public ArrayList<Reminder> getByMdDate(Date from, Date to) {
 		LastErrors.getInstance().clean();
 		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
 		ArrayList<Reminder> reminders = new ArrayList<Reminder>();
 		
 		Cursor cursor = db.query(dbHelper.TABLE_NAME, null,
 				dbHelper.COLUMN_MODIFIED + ">=?" + " and "
 				+ dbHelper.COLUMN_MODIFIED + "<=?",
 				new String[] { String.valueOf(from.getTime()),
				String.valueOf(to.getTime()) }, null, null, dbHelper.COLUMN_MODIFIED + " DESC", null);		
 		if (cursor.moveToFirst()) {
             do {
                 Reminder reminder = this.cursorToReminder(cursor);
             	reminders.add(reminder);
             } while (cursor.moveToNext());
         }
 		
 		return reminders;
 	}
 	
 	/**
 	 * Transforms Reminder into ContentValues for writing in the database.
 	 * @param reminder Reminder to transform.
 	 * @return ContentValues with fields from reminder.
 	 */
 	private ContentValues reminderToValues(Reminder reminder) {
 		ContentValues values = new ContentValues();
     	values.put(dbHelper.COLUMN_CREATED, reminder.getSys().getCr().getTime());
     	values.put(dbHelper.COLUMN_MODIFIED, reminder.getSys().getMd().getTime());
     	values.put(dbHelper.COLUMN_TYPE, reminder.getType());
     	values.put(dbHelper.COLUMN_DESCRIPTION, reminder.getDescr());
     	values.put(dbHelper.COLUMN_START_DATE, reminder.getStrDate().getTime());
     	values.put(dbHelper.COLUMN_END_DATE, reminder.getEndDate().getTime());
     	values.put(dbHelper.COLUMN_PRIORITY, reminder.getPrior());
     	values.put(dbHelper.COLUMN_REPETITION, reminder.getRepetition());
         return values;
 	}
 	
 	/**
 	 * Transforms cursor into Reminder.
 	 * @param cursor Cursor to transform.
 	 * @return Reminder with fields from cursor.
 	 */
 	private Reminder cursorToReminder(Cursor cursor) {
 		Long id = cursor.getLong(0);
 		
 		Date cr = new Date(cursor.getLong(1));
 		Date md = new Date(cursor.getLong(2));
 		Sys sys = new Sys(cr, md);
 		
 		String type = cursor.getString(3);
 		String descr = cursor.getString(4);
 		Date strDate = new Date(cursor.getLong(5));
 		Date endDate = new Date(cursor.getLong(6));
 		long prior = cursor.getLong(7);
 		String repetition = cursor.getString(8);
 		
 		ArrayList<Long> bellsIds = this.reminderToBellDao.getBellsIds(id);
 		ArrayList<Bell> bells = this.bellDao.getByIds(bellsIds);
 		
 		Reminder reminder = new Reminder(id, sys, type, descr, strDate,
 				endDate, bells, prior, repetition);
 		return reminder;
 	}
 
 }
