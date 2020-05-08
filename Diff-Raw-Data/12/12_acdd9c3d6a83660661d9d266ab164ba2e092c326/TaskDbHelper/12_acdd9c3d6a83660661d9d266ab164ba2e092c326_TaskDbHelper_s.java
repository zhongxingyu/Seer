 package net.teerapap.whatnext.model;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.provider.BaseColumns;
 import android.util.Log;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * The SQLite database helper for Task database.
  * <p/>
  * Currently it contains 1 tables.
  * * tasks
  * <p/>
  * Created by teerapap on 10/30/13.
  */
 public class TaskDbHelper extends SQLiteOpenHelper {
 
     public static final String TAG = "TaskDbHelper";
 
     // If you change the database schema, you must increment the database version.
     public static final int DATABASE_VERSION = 1;
     public static final String DATABASE_NAME = "Tasks.db";
     private static final String COMMA_SEP = ",";
 
     public TaskDbHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
     public void onCreate(SQLiteDatabase db) {
         // Create database
         Log.i(TAG, "Create a table(" + TaskEntry.TABLE_NAME + ") in tasks database.");
         db.execSQL(TaskEntry.SQL_CREATE_TABLE);
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         // No version 2 yet.
     }
 
     /**
      * @return the task with database id
      */
     public void addTask(Task task) throws Exception {
         Log.v(TAG, "Add a task to database.");
         if (task.getId() > 0) {
             // It already has an id.
             throw new Exception("Task id " + task.getId() + " has already been added.");
         }
 
         // Gets the db in write mode
         SQLiteDatabase db = this.getWritableDatabase();
 
         // Create a new map of values, where column names are the keys
         ContentValues values = new ContentValues();
         values.put(TaskEntry.COLUMN_NAME_TITLE, task.getTitle());
         values.put(TaskEntry.COLUMN_NAME_WHEN_HOME, task.isAtHome());
         values.put(TaskEntry.COLUMN_NAME_WHEN_FREE, task.isFreeTime());
         values.put(TaskEntry.COLUMN_NAME_WHEN_WORK, task.isAtWork());
         values.put(TaskEntry.COLUMN_NAME_WHEN_SHOPPING, task.isShopping());
 
         // Insert the new row, returning the primary key value of the new row
         long newRowId = db.insertOrThrow(TaskEntry.TABLE_NAME, null, values);
         if (newRowId < 0) {
             throw new Exception("cannot add to database");
         }
         task.setId(newRowId);
         Log.v(TAG, "Task id(" + newRowId + ") is added.");
     }
 
     /**
      * @return all the ongoing tasks. normal tasks.
      */
     public List<Task> listOngoingTasks() {
         Log.v(TAG, "List all ongoing tasks.");
         // Get the db in read mode
         SQLiteDatabase db = this.getReadableDatabase();
 
         // Select these columns
         String[] projection = {
                 TaskEntry._ID,
                 TaskEntry.COLUMN_NAME_TITLE,
                 TaskEntry.COLUMN_NAME_WHEN_HOME,
                 TaskEntry.COLUMN_NAME_WHEN_FREE,
                 TaskEntry.COLUMN_NAME_WHEN_WORK,
                 TaskEntry.COLUMN_NAME_WHEN_SHOPPING
         };
         // Where the task is normal
         String where = TaskEntry.COLUMN_NAME_STATUS + "=?";
         String[] selectArgs = new String[]{String.valueOf(TaskEntry.STATUS.NORMAL.val())};
 
         // Query
         Cursor c = db.query(TaskEntry.TABLE_NAME, projection, where, selectArgs, null, null, null);
 
         // Iterate to create task objects.
         LinkedList<Task> listTasks = new LinkedList<Task>();
         c.moveToFirst();
         while (!c.isAfterLast()) {
             // Title
             String title = c.getString(c.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_TITLE));
 
             // When condition
             WhenCondition con = new WhenCondition();
             con.atHome(c.getShort(c.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_WHEN_HOME)) > 0);
             con.freeTime(c.getShort(c.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_WHEN_FREE)) > 0);
             con.atWork(c.getShort(c.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_WHEN_WORK)) > 0);
             con.shopping(c.getShort(c.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_WHEN_SHOPPING)) > 0);
 
             // Task
             Task t = new Task(title, con);
             t.setId(c.getLong(c.getColumnIndexOrThrow(TaskEntry._ID)));
 
             listTasks.add(t);
 
             // Move to next row
             c.moveToNext();
         }
 
         // TODO: Change log level to info
         Log.v(TAG, "Found " + listTasks.size() + " ongoing tasks.");
 
         return listTasks;
     }
 
     /* Inner class that defines the table contents */
     public static abstract class TaskEntry implements BaseColumns {
         public static final String TABLE_NAME = "tasks";
         public static final String COLUMN_NAME_TITLE = "title";
         public static final String COLUMN_NAME_WHEN_HOME = "when_home";
         public static final String COLUMN_NAME_WHEN_FREE = "when_free";
         public static final String COLUMN_NAME_WHEN_WORK = "when_work";
         public static final String COLUMN_NAME_WHEN_SHOPPING = "when_shopping";
         public static final String COLUMN_NAME_STATUS = "status";
 
         public static enum STATUS {
             NORMAL(0),
             DONE(1);
 
             private final int val;
 
             private STATUS(int v) {
                 this.val = v;
             }
 
             public int val() {
                 return this.val;
             }
         }
 
         private static final String SQL_CREATE_TABLE =
                 "CREATE TABLE " + TABLE_NAME + " (" +
                         _ID + " INTEGER PRIMARY KEY" +
                         COMMA_SEP + COLUMN_NAME_TITLE + " TEXT" +
                        COMMA_SEP + COLUMN_NAME_STATUS + " INTEGER" +
                        COMMA_SEP + COLUMN_NAME_WHEN_HOME + " INTEGER" +
                        COMMA_SEP + COLUMN_NAME_WHEN_WORK + " INTEGER" +
                        COMMA_SEP + COLUMN_NAME_WHEN_FREE + " INTEGER" +
                        COMMA_SEP + COLUMN_NAME_WHEN_SHOPPING + " INTEGER" +
                         " )";
     }
 
 }
