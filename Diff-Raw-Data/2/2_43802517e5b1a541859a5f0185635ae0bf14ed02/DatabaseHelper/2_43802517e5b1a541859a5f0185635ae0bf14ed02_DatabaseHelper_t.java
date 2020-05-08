 package com.thornton.k3spring;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DatabaseHelper extends SQLiteOpenHelper{
 
 	public static final String DATABASE_NAME = "tasks.db";
 
 	public static final int  DATABASE_VERSION = 1;
 
 	public DatabaseHelper(final Context context){
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(final SQLiteDatabase db) {
 		db.execSQL(TaskTable.CREATE);
 	}
 
 	@Override
 	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
 		db.execSQL(TaskTable.DROP);
 		onCreate(db);
 	}
 
 	public long addTask(final Task task){
 		final ContentValues cv = new ContentValues();
 		cv.put(TaskTable.TASK_ID, task.getId());
 		cv.put(TaskTable.DESC, task.getTasks());
 		cv.put(TaskTable.START, task.getStart());
 		cv.put(TaskTable.END, task.getEnd());
 		cv.put(TaskTable.STATUS, task.getStatus());
 
 		final SQLiteDatabase db = getWritableDatabase();
 		return db.insert(TaskTable.NAME, TaskTable.TASK_ID, cv);
 
 	}
 
 	public void updateAllTasksToComplete(final Task task){
 		final String where = TaskTable.TASK_ID + "=? and " + TaskTable.STATUS + "=" + Task.IN_PROGRESS ;
 		final String[] whereArgs = new String[] {task.getId()};
 
 
 		final ContentValues cv = new ContentValues();
 		cv.put(TaskTable.END, task.getEnd());
 		cv.put(TaskTable.STATUS, task.getStatus());
 
 		final SQLiteDatabase db = getWritableDatabase();
 		db.update(TaskTable.NAME, cv, where, whereArgs);
 	}
 
 	public List<Task> getTasksFromId(final String id){
 		final SQLiteDatabase db = getWritableDatabase();
		final String where = TaskTable.TASK_ID + "=? and (" + TaskTable.END + " IS NULL OR " + TaskTable.END + "= '')";
 		final String[] whereArgs = new String[] {id};
 
 		final String[] cols = new String[]{TaskTable.ID, TaskTable.TASK_ID, TaskTable.DESC, TaskTable.START, TaskTable.END, TaskTable.STATUS};
 
 		final Cursor c = db.query(TaskTable.NAME, cols, where, whereArgs, null, null, TaskTable.TASK_ID);
 
 		final List<Task> tasks = new ArrayList<Task>();
 
 		while(c.moveToNext()){
 			tasks.add(extractTaskFromCursor(c));
 		}
 
 		c.close();
 
 		return tasks;
 	}
 
 	public List<Task> getAllTasksByDay(final String day){
 		final SQLiteDatabase db = getWritableDatabase();
 		final String where = TaskTable.START + " like '" + day + "%'";
 
 		final String[] cols = new String[]{TaskTable.ID, TaskTable.TASK_ID, TaskTable.DESC, TaskTable.START, TaskTable.END, TaskTable.STATUS};
 
 		final Cursor c = db.query(TaskTable.NAME, cols, where, null, null, null, TaskTable.TASK_ID);
 
 		final List<Task> tasks = new ArrayList<Task>();
 
 		while(c.moveToNext()){
 			tasks.add(extractTaskFromCursor(c));
 		}
 
 		c.close();
 
 		return tasks;
 	}
 
 	private Task extractTaskFromCursor(final Cursor c) {
 		final Task task = new Task(c.getInt(c.getColumnIndex(TaskTable.ID)));
 		task.setId(c.getString(c.getColumnIndex(TaskTable.TASK_ID)));
 		task.setStart(c.getString(c.getColumnIndex(TaskTable.START)));
 		task.setTasks(c.getString(c.getColumnIndex(TaskTable.DESC)));
 		task.setEnd(c.getString(c.getColumnIndex(TaskTable.END)));
 		task.setStatus(c.getInt(c.getColumnIndex(TaskTable.STATUS)));
 		return task;
 	}
 }
