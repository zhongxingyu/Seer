 package com.android.apptime.database;
 
 import android.content.Context;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class MainDBAdapter {
	public static final String DATABASE_NAME = "appTimeDBB1.db"; //$NON-NLS-1$
 
     public static final int DATABASE_VERSION = 1;
     
     // TASK TABLE
     private static final String DATABASE_TABLE_TASK = "Tasks_Table";
     private static final String TASKDB_KEY_ID = "taskdb_task_id";
     private static final String TASKDB_TITLE = "taskdb_task_title";
     private static final String TASKDB_DESCRIPTION = "taskdb_description";
     private static final String TASKDB_LOCATION = "taskdb_location";
     private static final String TASKDB_PRIORITY = "taskdb_priority";
     private static final String TASKDB_REPEAT = "taskdb_repeat";
     private static final String TASKDB_CATEGORY = "taskdb_category";
     private static final String TASKDB_COMPLETED = "taskdb_completed";
     private static final String TASKDB_COLOR = "taskdb_color";
     private static final String TASKDB_DEADLINE = "taskdb_deadline";
     private static final String TASKDB_ALERTTIME = "taskdb_alert_time";
     private static final String TASKDB_TYPE = "taskdb_type";
     // EVENT TABLE
     private static final String DATABASE_TABLE_EVENT = "Events_Table";
     private static final String EVENTDB_KEY_ID = "eventdb_event_id";
     private static final String EVENTDB_TITLE = "eventdb_event_title";
     private static final String EVENTDB_DESCRIPTION = "eventdb_description";
     private static final String EVENTDB_LOCATION = "eventdb_location";
     private static final String EVENTDB_PRIORITY = "eventdb_priority";
     private static final String EVENTDB_REPEAT = "eventdb_repeat";
     private static final String EVENTDB_CATEGORY = "eventdb_category";
     private static final String EVENTDB_COMPLETED = "eventdb_completed";
     private static final String EVENTDB_COLOR = "eventdb_color";
     private static final String EVENTDB_STARTTIME = "eventdb_start_time";
     private static final String EVENTDB_ENDTIME = "eventdb_end_time";
     private static final String EVENTDB_ALERTTIME = "eventdb_alert_time";
     private static final String EVENTDB_TYPE = "eventdb_type"; 
     // ITEM TABLE 
     private static final String DATABASE_TABLE_ITEM = "Items_Table";
     private static final String ITEMDB_KEY_ID = "itemdb_itemid";
     private static final String ITEMDB_KEY_TASK = "itemdb_taskid";
     private static final String ITEMDB_KEY_EVENT = "itemdb_eventid";
     
     // CATEGORY TABLE
     private static final String DATABASE_TABLE_CATEGORY = "Categories_Table";
     private static final String CATEGORYDB_KEY_ID = "categorydb_category_id";
     private static final String CATEGORYDB_TITLE = "categorydb_title";
     
     // ALERTTYPE TABLE
     private static final String DATABASE_TABLE_ALERTTYPE = "Alerttype_Table";
     private static final String ALERTTYPEDB_KEY_ID = "alerttypedb_alerttype_id";
     private static final String ALERTTYPEDB_TITLE = "alerttypedb_title";
     
     
       
     
     
     
     // SQL COMMAND TO CREATE TASK TABLE
     private static final String DATABASE_CREATE_TASK = "create table " + DATABASE_TABLE_TASK
     + " (" + TASKDB_KEY_ID + " integer primary key autoincrement, " + 
     TASKDB_TITLE + " text, " +
     TASKDB_DESCRIPTION + " text, " +
     TASKDB_LOCATION + " text, " +
     TASKDB_PRIORITY + " text, " +
     TASKDB_REPEAT + " text not null, " +
     TASKDB_CATEGORY + " text, " +
     TASKDB_COMPLETED + " text not null, " +
     TASKDB_COLOR + " integer not null, " +
     TASKDB_DEADLINE + " real not null, " +
    TASKDB_ALERTTIME + " real not null, " +  
     TASKDB_TYPE + " text not null" + ");";
     
     
     // SQL COMMAND TO CREATE EVENT TABLE
     private static final String DATABASE_CREATE_EVENT = "create table " + DATABASE_TABLE_EVENT
     + " (" + EVENTDB_KEY_ID + " integer primary key autoincrement, " + 
     EVENTDB_TITLE + " text, " +
     EVENTDB_DESCRIPTION + " text, " +
     EVENTDB_LOCATION + " text, " +
     EVENTDB_PRIORITY + " text, " +
     EVENTDB_REPEAT + " text not null, " +
     EVENTDB_CATEGORY + " text, " +
     EVENTDB_COMPLETED + " text not null, " +
     EVENTDB_COLOR + " integer not null, " +
     EVENTDB_STARTTIME + " real not null, " +
     EVENTDB_ENDTIME + " real not null, " +
    EVENTDB_ALERTTIME + " real not null," + 
     EVENTDB_TYPE + " text not null" + ");";
     
     
     
     // SQL COMMAND TO CREATE ITEM TABLE
     private static final String DATABASE_CREATE_ITEM = "create table " + 
     DATABASE_TABLE_ITEM + " (" + ITEMDB_KEY_ID + " integer primary key autoincrement, " +
     ITEMDB_KEY_TASK + " integer not null, " +  ITEMDB_KEY_EVENT + " integer not null," + " FOREIGN KEY ("+ ITEMDB_KEY_TASK +") REFERENCES " + DATABASE_TABLE_TASK + " ("+ TASKDB_KEY_ID +"), "
     + " FOREIGN KEY ("+ ITEMDB_KEY_EVENT +") REFERENCES " + DATABASE_TABLE_EVENT + " ("+ EVENTDB_KEY_ID +"));";
 
 
 	// SQL COMMAND TO CREATE CATEGORY TABLE
     private static final String DATABASE_CREATE_CATEGORY = "create table " +
     DATABASE_TABLE_CATEGORY + " (" + CATEGORYDB_KEY_ID + " integer primary key autoincrement, " +
     CATEGORYDB_TITLE + " text not null);";
     
  // SQL COMMAND TO CREATE ALERTTYPE TABLE
     private static final String DATABASE_CREATE_ALERTTYPE = "create table " +
     DATABASE_TABLE_ALERTTYPE + " (" + ALERTTYPEDB_KEY_ID + " integer primary key autoincrement, " +
     ALERTTYPEDB_TITLE + " text not null);";
     
 
     private final Context context; 
     private DatabaseHelper DBHelper;
     private SQLiteDatabase db;
     
     public MainDBAdapter(Context ctx)
     {
         this.context = ctx;
         this.DBHelper = new DatabaseHelper(this.context);
     }
 
     private static class DatabaseHelper extends SQLiteOpenHelper 
     {
         DatabaseHelper(Context context) 
         {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) 
         {
             db.execSQL(DATABASE_CREATE_TASK);
             db.execSQL(DATABASE_CREATE_EVENT);
             db.execSQL(DATABASE_CREATE_ITEM);  
             db.execSQL(DATABASE_CREATE_CATEGORY);
             db.execSQL(DATABASE_CREATE_ALERTTYPE);
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, 
         int newVersion) 
         {               
             // Adding any table mods to this guy here
         }
     } 
 	
     public MainDBAdapter open() throws SQLException 
     {
         this.db = this.DBHelper.getWritableDatabase();
         return this;
     }
 
     /**
      * close the db 
      * return type: void
      */
     public void close() 
     {
         this.DBHelper.close();
     }
 
 	
 	
 }
 
