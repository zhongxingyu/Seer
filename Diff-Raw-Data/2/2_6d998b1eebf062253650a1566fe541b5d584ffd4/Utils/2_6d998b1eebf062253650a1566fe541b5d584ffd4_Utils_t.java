 package com.teamdev.database;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 import com.teamdev.beans.IState;
 import com.teamdev.beans.Task;
 
 public class Utils {
 	
 	private static volatile Utils instance;
 	private DataHandler handler;
 	
 	private Utils(DataHandler handler) {
 		this.handler = handler;
 	}
 	
 	public static Utils getInstance(DataHandler handler) {
 		if (instance == null) {
 			synchronized (Utils.class) {
 				if (instance == null) {
 					instance = new Utils(handler);
 				}
 			}
 		}
 		
 		return instance;
 	}
 	
 	@SuppressWarnings("static-access")
 	public void createNewTask(Task task) {
 		SQLiteDatabase database = handler.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put("title", task.getTitle());
 		values.put("description", task.getDescription());
 		
 		Calendar calendarCreate = task.getCreationDate();
 		values.put("creationDate", calendarCreate.getInstance().get(Calendar.YEAR)+ "-" 
 						 + calendarCreate.getInstance().get(Calendar.MONTH) + "-" 
 				         + calendarCreate.getInstance().get(Calendar.DAY_OF_MONTH));
 		
 		Calendar calendarChanged = task.getLastChangedDate();
 		values.put("lastChangedDate", calendarChanged.getInstance().get(Calendar.YEAR)+ "-" 
 				 + calendarChanged.getInstance().get(Calendar.MONTH) + "-" 
 		         + calendarChanged.getInstance().get(Calendar.DAY_OF_MONTH));
 		
 		values.put("state", IState.LBL_DONE.equals(task.getState()) ? 1 : 0);
 		
 		database.insert("tasks", null, values);
 		database.close();
 	}
 	
 	@SuppressWarnings("static-access")
 	public void editTask(Task task) {
 		SQLiteDatabase database = handler.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put("title", task.getTitle());
 		values.put("description", task.getDescription());
 		
 		Calendar calendarModify = task.getLastChangedDate();
 		values.put("lastChangedDate", calendarModify.getInstance().get(Calendar.YEAR)+ "-" 
 				         + calendarModify.getInstance().get(Calendar.MONTH) + "-" 
 				         + calendarModify.getInstance().get(Calendar.DAY_OF_MONTH));
 		
 		values.put("state", IState.LBL_DONE.equals(task.getState()) ? 1 : 0);
 		
		database.update("tasks", values, "_id='" + task.getId() + "'", null);		
 		database.close();
 	}
 	
 	public Task getTask(int id) {
 		SQLiteDatabase database = handler.getWritableDatabase();
 		Task task = new Task();
 		String query = "SELECT * FROM tasks WHERE _id=" + String.valueOf(id);
 		Cursor cursor = database.rawQuery(query, null);
 		while (cursor.moveToNext()) {
 			task.setTitle(cursor.getString(1));
 			task.setDescription(cursor.getString(2));
 			task.setCreationDate(dateParser(cursor, 3));
 			task.setLastChangedDate(dateParser(cursor, 4));
 			task.setState(cursor.getInt(5) == 1 ? IState.LBL_DONE : IState.LBL_NOT_DONE);
 		}
 		database.close();
 		
 		return task;
 	}
 	
 	public void deleteTask(Task task) {
 		SQLiteDatabase database = handler.getWritableDatabase();
 		database.delete("tasks", "_id", new String[]{String.valueOf(task.getId())});
 		database.close();
 		
 	}
 	
 	public List<Task> getAllTasks() {
 		SQLiteDatabase database = handler.getWritableDatabase();
 		List<Task> tasksList = new ArrayList<Task>();
 		String query = "SELECT * FROM tasks";
 		Cursor cursor = database.rawQuery(query, null);
 		while (cursor.moveToNext()) {
 			Task task = new Task();
 			task.setId(cursor.getInt(0));
 			task.setTitle(cursor.getString(1));
 			task.setDescription(cursor.getString(2));
 			task.setCreationDate(dateParser(cursor, 3));
 			task.setLastChangedDate(dateParser(cursor, 4));
 			task.setState(cursor.getInt(5) == 1 ? IState.LBL_DONE : IState.LBL_NOT_DONE);
 			tasksList.add(task);
 		}
 		
 		database.close();
 		
 		return tasksList;
 	}
 	
 	private Calendar dateParser(Cursor cursor, int columnIndex) {
 		String date = cursor.getString(columnIndex);
 		Calendar calendar = Calendar.getInstance();
 		
 		StringTokenizer st = new StringTokenizer(date, "-");
 		String dates[] = new String[3];
 		int i = 0;
 		while (st.hasMoreTokens()) {
 			dates[i] = st.nextToken();
 			i++;
 		}
 		
 		calendar.set(Calendar.YEAR, Integer.parseInt(dates[0]));
 		calendar.set(Calendar.MONTH, Integer.parseInt(dates[1]));
 		calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dates[2]));
 		
 		
 		return calendar;
 	}
 }
