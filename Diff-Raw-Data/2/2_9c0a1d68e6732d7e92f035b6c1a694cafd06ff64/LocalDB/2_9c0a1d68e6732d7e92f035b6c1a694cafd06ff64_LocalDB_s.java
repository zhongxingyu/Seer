 package com.CMPUT301F12T07.crowdsource;
 
 import java.util.ArrayList; 
 import java.util.List; 
 
 import android.content.ContentValues; 
 import android.content.Context; 
 import android.database.Cursor; 
 import android.database.sqlite.SQLiteDatabase; 
 import android.database.sqlite.SQLiteOpenHelper; 
 import android.util.Log;
 
 public class LocalDB extends SQLiteOpenHelper { 
 
 	/** All Static variables */ 
 	/* Database Version */ 
 	private static final int DATABASE_VERSION = 1; 
 
 	/* Database Name */
 	private static final String DATABASE_NAME = "LocalDBManager"; 
 
 	/* Tasks table name */
 	private static final String TABLE_TASKS = "tasks"; 
 
 	/* Tasks Table Columns names */
 	private static final String KEY_TID = "_tid"; 
 	private static final String KEY_UID = "uid"; 
 	private static final String KEY_TITLE = "title"; 
 	private static final String KEY_DESCRIPTION = "description";
 	private static final String KEY_DATECREATE = "dateCreate";
 	private static final String KEY_DATEDUE = "dateDue";
 	private static final String KEY_TYPE = "type";
 	private static final String KEY_VISIBILITY = "visibility";
 	private static final String KEY_QUANTITY = "quantity";
 
 	/** Constructor */
 	public LocalDB(Context context) { 
 		super(context, DATABASE_NAME, null, DATABASE_VERSION); 
 	} 
 
 	/** Creating Table */ 
 	@Override
 	public void onCreate(SQLiteDatabase db) { 
 		String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
 				+ KEY_TID + " INTEGER PRIMARY KEY," 
 				+ KEY_UID + " INTEGER,"
 				+ KEY_TITLE + " TEXT," 
 				+ KEY_DESCRIPTION + " TEXT," 
 				+ KEY_DATECREATE + " TEXT,"
 				+ KEY_DATEDUE + " TEXT,"
 				+ KEY_TYPE + " TEXT,"
 				+ KEY_VISIBILITY + " NUMERIC,"
 				+ KEY_QUANTITY + " INTEGER" + ")"; 
 		
 		db.execSQL(CREATE_TASKS_TABLE); 
 	} 
 
 	/** Upgrading database */
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { 
 		// Drop older table if existed 
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS); 
 
 		// Create tables again 
 		onCreate(db); 
 	} 
 
 	/** 
 	 * All CRUD(Create, Read, Update, Delete) Operations 
 	 */
 
 	/* Adding new task */
 
 	public void createTask(Task task) { 
 		SQLiteDatabase db = this.getWritableDatabase(); 
 
 		ContentValues values = new ContentValues(); 
 		values.put(KEY_UID, task.get_uid());
 		values.put(KEY_TITLE, task.get_title());
 		values.put(KEY_DESCRIPTION, task.get_description());
 		values.put(KEY_DATECREATE, task.get_dateCreate());
 		values.put(KEY_DATEDUE, task.get_dateDue());
 		values.put(KEY_TYPE, task.get_type());
 		values.put(KEY_VISIBILITY, task.get_visibility());
 		values.put(KEY_QUANTITY, task.get_quantity());
 
 		// Inserting Row 
 		db.insert(TABLE_TASKS, null, values); 
 		db.close();
 	} 
 
 	/* Getting single task */
 	public Task getTask(int tid) { 
 		SQLiteDatabase db = this.getReadableDatabase(); 
 
 		Cursor cursor = db.query(TABLE_TASKS, new String[] { 
 				KEY_TID, KEY_UID, KEY_TITLE, KEY_DESCRIPTION, KEY_DATECREATE,
 				KEY_DATEDUE, KEY_TYPE, KEY_VISIBILITY, KEY_QUANTITY }, KEY_TID + "=?", 
 				new String[] { String.valueOf(tid) }, null, null, null, null); 
 
 		if (cursor != null) 
 			cursor.moveToFirst(); 
 
 		Task task = new Task(
 				Integer.parseInt(cursor.getString(1)), 
 				cursor.getString(2), cursor.getString(3), 
 				cursor.getString(4), cursor.getString(5),
 				cursor.getString(6), cursor.getInt(7), 
 				Integer.parseInt(cursor.getString(8))); 
 
 		return task; 
 	} 
 
 	/* Getting All tasks */
 	public List<Task> getAllTasks() { 
 		List<Task> taskList = new ArrayList<Task>(); 
 		// Select All Query 
 		String selectQuery = "SELECT  * FROM " + TABLE_TASKS; 
 
 		SQLiteDatabase db = this.getWritableDatabase(); 
 		Cursor cursor = db.rawQuery(selectQuery, null); 
 
 		// looping through all rows and adding to list 
 		if (cursor.moveToFirst()) { 
 			do { 
 				Task task = new Task(); 
 				task.set_tid(Integer.parseInt(cursor.getString(0))); 
 				task.set_uid(Integer.parseInt(cursor.getString(1))); 
 				task.set_title(cursor.getString(2)); 
 				task.set_description(cursor.getString(3)); 
 				task.set_dateCreate(cursor.getString(4)); 
 				task.set_dateDue(cursor.getString(5)); 
 				task.set_type(cursor.getString(6)); 
 				task.set_visibility(cursor.getInt(7));
 				task.set_quantity(cursor.getInt(8));
 				// Adding contact to list 
 				taskList.add(task); 
 			} while (cursor.moveToNext()); 
 		} 
 
 		return taskList; 
 	} 
 
 	/* Updating single task */
 	public int updateTask(Task task) { 
 		SQLiteDatabase db = this.getWritableDatabase(); 
 
 		ContentValues values = new ContentValues(); 
 		values.put(KEY_UID, task.get_uid());
 		values.put(KEY_TITLE, task.get_title());
 		values.put(KEY_DESCRIPTION, task.get_description());
 		values.put(KEY_DATECREATE, task.get_dateCreate());
 		values.put(KEY_DATEDUE, task.get_dateDue());
 		values.put(KEY_TYPE, task.get_type());
 		values.put(KEY_VISIBILITY, task.get_visibility());
 		values.put(KEY_QUANTITY, task.get_quantity());
 
 		// updating row 
 		return db.update(TABLE_TASKS, values, KEY_TID + " = ?", 
 				new String[] { String.valueOf(task.get_tid()) }); 
 	} 
 
 	/* Deleting single task */
 	public void deleteTask(int tid) {
 		SQLiteDatabase db = this.getWritableDatabase(); 
 		db.delete(TABLE_TASKS, KEY_TID + " = ?", 
 				new String[] { String.valueOf(tid) }); 
 		db.close(); 		
 	}
 		
 	/* Print all tasks to log (for debug purpose) */
 	public void printAllTasks(){
 		Log.d("Print: ", "Printing all tasks..."); 
 		List<Task> tasks = this.getAllTasks();        
 
 		for (Task n : tasks) { 
 			String log = "Tid: " + n.get_tid() 
 					+ ", Uid: " + n.get_uid()
 					+ ", Title: " + n.get_title() 
 					+ ", Description: " + n.get_description()
 					+ ", DateCreate: " + n.get_dateCreate()
 					+ ", DateDue: " + n.get_dateDue()
 					+ ", Type: " + n.get_type()
 					+ ", Visibility: " + n.get_visibility()
 					+ ", Quantity: " + n.get_quantity();
 			
 			// Printing Tasks to log 
 			Log.d("Task: ", log); 
 		}
 	}
 	
 	/* Creates a RandomTask with data, useful for testing */
 	public void createRandomTask() {
 		Task task = new Task(1234567890, "TITLE", "DESCRIPTION", "1", "1", "TYPE", 1, 1);
 		createTask(task);
 	}
 	
 	/* Deletes all data from Database, useful for testing */
 	public void emptyDatabase() {
		String emptyQuery = "DELETE * FROM " + TABLE_TASKS;
 		SQLiteDatabase db = this.getWritableDatabase();
 		db.rawQuery(emptyQuery, null);
 	}
 
 }
 
