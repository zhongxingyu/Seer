 package gupta.app.assignmentmanager;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class TaskDB extends SQLiteOpenHelper{
 
 	private static final String DATABASE_NAME = "TaskManager";
 	private static final String TABLE_TASKS = "tasks";
 	private static final int DATABASE_VERSION = 1;
 	
 	private static final String KEY_ID = "id";
 	private static final String KEY_SUBJECT = "subject";
 	private static final String KEY_TITLE = "title";
 	private static final String KEY_DATE = "date";
 
 	public TaskDB(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		// TODO Auto-generated method stub
 		String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + 
 				"(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_SUBJECT + 
 				" TEXT," + KEY_TITLE + " TEXT," + KEY_DATE + " INTEGER" +
 				")";
 		db.execSQL(CREATE_TASKS_TABLE);
 		
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// TODO Auto-generated method stub
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
 		
 		onCreate(db);
 		
 	}
 	
 	public void addTask (Task task) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues cv = new ContentValues();
 		
 		Long epoch = task.getDate();
 		//Long calLong = task.getIntegerAsDate(td);
 		
 		cv.put(KEY_DATE, epoch);
 		cv.put(KEY_SUBJECT, task.getSubject());
 		cv.put(KEY_TITLE, task.getTitle());
 		
 		db.insert(TABLE_TASKS, null, cv);
 		db.close();
 	}
 	
 	public Task getTask(int id) {
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Cursor cursor = db.query(TABLE_TASKS, new String[] {KEY_ID, 
 				KEY_SUBJECT, KEY_TITLE, KEY_DATE }, KEY_ID + "=?",
 				new String[] {String.valueOf(id) }, null, null, null, null);
 		
 		if (cursor != null) {
 			cursor.moveToFirst();
 		}
 		
 		int taskId = Integer.parseInt(cursor.getString(0));
 		String taskSubject = cursor.getString(1);
 		String taskTitle = cursor.getString(2);
 		long taskDate = Long.parseLong(cursor.getString(3));
 		//Calendar taskDateCalendar = getCalendarAsInteger(taskDate);
 		
 		Task task = new Task(taskId, taskSubject, taskTitle, taskDate);
 		return task;
 	}
 	
 	public Calendar getCalendarAsInteger(long date) {
 		Calendar cal = Calendar.getInstance();
 		cal.setTimeInMillis(date);
 		return cal;
 	}
 	
 	public List<Task> getAllTasks() {
 		List<Task> taskList = new ArrayList<Task>();
 		
 		// Select All Query
 		String selectQuery = "SELECT  * FROM " + TABLE_TASKS + " ORDER BY " + KEY_DATE + " ASC";
 		
 		SQLiteDatabase db = this.getWritableDatabase();
 		Cursor cursor = db.rawQuery(selectQuery, null);
 		
 		if (cursor.moveToFirst()) {
 			do {
 				long taskDate = Long.parseLong(cursor.getString(3));
 				int taskId = Integer.parseInt(cursor.getString(0));
 				Task task = new Task(taskId, cursor.getString(1), cursor.getString(2), taskDate);
 				taskList.add(task);
 			}
 			while (cursor.moveToNext());
 			
 		}
 		
		if (taskList.get(0) != null) {
			return taskList;
		}
		else {
			return null;
		}
 	}
 	
 	public int updateTask(Task task) {
 		return 0;
 	}
 	
 	public void deleteTask(Task task) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		db.delete (TABLE_TASKS, KEY_ID + " = ?",
 				new String[] { String.valueOf(task.getId())});
 		db.close();
 	}
 
 }
