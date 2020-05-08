 /*
  * Copyright (C) 2008 Google Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package org.crazythink.TimeTrackerv1;
 
 import java.util.Calendar;
 import java.util.UUID;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DbAdapter {
 
 	public static final String KEY_ROWID = "_id";
 	public static final String KEY_UUID = "uuid";
     public static final String KEY_NAME = "name";
     public static final String KEY_DESCRIPTION = "description";
     public static final String KEY_LATEST = "latest_update";
     public static final String KEY_PROJECT_ID = "project_id";
     public static final String KEY_CHANGED = "changed";
     public static final String KEY_START_TIME = "start_time";
     public static final String KEY_START_DATE_MMDD = "start_date_mmyy";
     public static final String KEY_START_TIME_HHMM = "start_time_hhmm";
     public static final String KEY_MINUTES = "minutes";
     public static final String KEY_TASK_ID = "task_id";
     public static final String KEY_MINUTES_FORMATTED = "minutes_formatted";
     public static final String KEY_DURATION	= "duration_sum";
     
 
     private static final String TAG = "TasksDbAdapter";
     private DatabaseHelper mDbHelper;
     private SQLiteDatabase mDb;
 
     /**
      * Database creation sql statement
      */
     private static final String DATABASE_CREATE_PROJECTS =
         "CREATE TABLE IF NOT EXISTS projects ("
     	+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
         + "uuid CHAR(36) NOT NULL, " 
         + "name VARCHAR(30) NOT NULL, "
         + "latest_update LONG NOT NULL, "
         + "changed BOOL NOT NULL"
         + ")";
     
     private static final String DATABASE_CREATE_TASKS =
         "CREATE TABLE IF NOT EXISTS tasks ("
     	+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
         + "uuid CHAR(36) NOT NULL, "
         + "name VARCHAR(30) NOT NULL, "
         + "description TEXT, "
         + "latest_update LONG NOT NULL, "
         + "changed BOOL NOT NULL, "
         + "project_id INTEGER, "
         + "FOREIGN KEY(project_id) REFERENCES projects(_id) ON DELETE CASCADE"
         + ")";
 
     
     private static final String DATABASE_CREATE_ENTRIES =
         "CREATE TABLE IF NOT EXISTS entries ("
     	+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
         + "uuid CHAR(36) NOT NULL, "
         + "description TEXT, "
         + "start_time LONG NOT NULL, "
         + "latest_update LONG NOT NULL, "
         + "minutes INTEGER NOT NULL, "
         + "changed BOOL NOT NULL, "
         + "task_id INTEGER, "
         + "FOREIGN KEY (task_id) REFERENCES tasks(_id) ON DELETE CASCADE"
         + ")";
     
     private static final String DATABASE_CREATE_RUNNINGTIMES =
     	"CREATE TABLE IF NOT EXISTS runningtimes ("
     	+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
     	+ "uuid CHAR(36) NOT NULL, "
     	+ "start_time LONG NOT NULL, "
     	+ "description TEXT, "
     	+ "changed BOOL NOT NULL, "
     	+ "task_id INTEGER, "
     	+ "FOREIGN KEY (task_id) REFERENCES tasks(_id) ON DELETE CASCADE"
     	+ ")";
 
     private static final String DATABASE_NAME = "data";
     private static final String DATABASE_TABLE_TASKS = "tasks";
     private static final String DATABASE_TABLE_PROJECTS = "projects";
     private static final String DATABASE_TABLE_ENTRIES = "entries";
     private static final String DATABASE_TABLE_RUNNINGTIMES = "runningtimes";
     private static final int DATABASE_VERSION = 4;
 
     private final Context mCtx;
 
     private static class DatabaseHelper extends SQLiteOpenHelper {
 
         DatabaseHelper(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
             Log.v("DEBUG: ", "DbAdapter: Databasehelper constructor");
         }
         
         @Override
         public void onOpen(SQLiteDatabase db)
         {
           super.onOpen(db);
           if (!db.isReadOnly())
           {
             // Enable foreign key constraints
             db.execSQL("PRAGMA foreign_keys=ON;");
           }
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
             Log.v("DEBUG: ", "DbAdapter: onCreate projects: " + DATABASE_CREATE_PROJECTS);
             db.execSQL(DATABASE_CREATE_PROJECTS);
         	Log.v("DEBUG: ", "DbAdapter: onCreate tasks: " + DATABASE_CREATE_TASKS);
             db.execSQL(DATABASE_CREATE_TASKS);
             Log.v("DEBUG: ", "DbAdapter: onCreate entries: " + DATABASE_CREATE_ENTRIES);
             db.execSQL(DATABASE_CREATE_ENTRIES);
             Log.v("DEBUG: ", "DbAdapter: onCreate runningitems: " + DATABASE_CREATE_RUNNINGTIMES);
             db.execSQL(DATABASE_CREATE_RUNNINGTIMES);
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         	if (oldVersion == 3 && newVersion == 4) {
         		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
 	                    + newVersion + ", which will destroy running time data");
         		db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_RUNNINGTIMES);
         		Log.v("DEBUG: ", "DbAdapter: onCreate runningitems: " + DATABASE_CREATE_RUNNINGTIMES);
                 db.execSQL(DATABASE_CREATE_RUNNINGTIMES);
         	}
         	else {
 	            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
 	                    + newVersion + ", which will destroy all old data");
 	            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_RUNNINGTIMES);
 	            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_ENTRIES);
 	            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_TASKS);
 	            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_PROJECTS);
 	            onCreate(db);
         	}
         }
     }
 
     /**
      * Constructor - takes the context to allow the database to be
      * opened/created
      * 
      * @param ctx the Context within which to work
      */
     public DbAdapter(Context ctx) {
         this.mCtx = ctx;
     }
 
     /**
      * Open the tasks database. If it cannot be opened, try to create a new
      * instance of the database. If it cannot be created, throw an exception to
      * signal the failure
      * 
      * @return this (self reference, allowing this to be chained in an
      *         initialization call)
      * @throws SQLException if the database could be neither opened or created
      */
     public DbAdapter open() throws SQLException {
         mDbHelper = new DatabaseHelper(mCtx);
         mDb = mDbHelper.getWritableDatabase();
         return this;
     }
 
     public void close() {
         mDbHelper.close();
     }
     
     
 /**                         TASKS                            **/
     /**
      * Create a new task using the title and body provided. If the task is
      * successfully created return the new rowId for that task, otherwise return
      * a -1 to indicate failure.
      * 
      * @param name the name of the task
      * @param description the description of the task
      * @param projectId the foreign key project id of the task
      * @return rowId or -1 if failed
      */
     public long createTask(String name, String description, Long projectId) {
         ContentValues initialValues = new ContentValues();
         initialValues.put(KEY_NAME, name);
         initialValues.put(KEY_DESCRIPTION, description);
         initialValues.put(KEY_PROJECT_ID, projectId);
         
         String uuidString = UUID.randomUUID().toString();
         initialValues.put(KEY_UUID, uuidString);
         
         Long now = Calendar.getInstance().getTime().getTime();
         initialValues.put(KEY_LATEST, now);
         
         initialValues.put(KEY_CHANGED, 0);
 
         return mDb.insert(DATABASE_TABLE_TASKS, null, initialValues);
     }
 
     /**
      * Delete the task with the given rowId
      * 
      * @param rowId id of task to delete
      * @return true if deleted, false otherwise
      */
     public boolean deleteTask(long rowId) {
 
         return mDb.delete(DATABASE_TABLE_TASKS, KEY_ROWID + "=" + rowId, null) > 0;
     }
 
     /**
      * Return a Cursor over the list of all tasks in the database
      * 
      * @return Cursor over all tasks
      */
     public Cursor fetchAllTasks() {
 
         return mDb.query(DATABASE_TABLE_TASKS, new String[] {KEY_ROWID, KEY_UUID,
                 KEY_NAME, KEY_DESCRIPTION, KEY_LATEST, 
                 KEY_PROJECT_ID}, null, null, null, null, KEY_PROJECT_ID + " ASC, " + KEY_NAME + " ASC");
     }
     
     /**
      * Return a Cursor over the list of all tasks in the database with duration sum
      * 
      * @return Cursor over all tasks
      */
     public Cursor fetchAllTasksWithDuration() {
     	return mDb.rawQuery("SELECT t.*, SUM(e.minutes) AS " + KEY_DURATION + " " +
     			"FROM tasks AS t LEFT OUTER JOIN entries AS e " +
     			"ON t._id=e.task_id " +
     			"GROUP BY t._id " +
     			"ORDER BY project_id ASC, name ASC;", null);
     }
 
     /**
      * Return a Cursor positioned at the task that matches the given rowId
      * 
      * @param rowId id of task to retrieve
      * @return Cursor positioned to matching task, if found
      * @throws SQLException if task could not be found/retrieved
      */
     public Cursor fetchTask(long rowId) throws SQLException {
 
         Cursor mCursor =
 
             mDb.query(true, DATABASE_TABLE_TASKS, new String[] {KEY_ROWID,
                     KEY_UUID, KEY_NAME, KEY_DESCRIPTION, KEY_LATEST,
                     KEY_PROJECT_ID}, KEY_ROWID + "=" + rowId, null,
                     null, null, null, null);
         if (mCursor != null) {
             mCursor.moveToFirst();
         }
         return mCursor;
 
     }
 
     /**
      * Update the task using the details provided. The task to be updated is
      * specified using the rowId
      * 
      * @param rowId id of task to update
      * @param name the name of the task
      * @param description the description of the task
      * @param projectId the foreign key project id of the task
      * @return true if the task was successfully updated, false otherwise
      */
     public boolean updateTask(long rowId, String name, String description, long projectId) {
         ContentValues args = new ContentValues();
         args.put(KEY_NAME, name);
         args.put(KEY_DESCRIPTION, description);
         args.put(KEY_PROJECT_ID, projectId);
         
         long now = Calendar.getInstance().getTime().getTime();
         args.put(KEY_LATEST, now);
 
         return mDb.update(DATABASE_TABLE_TASKS, args, KEY_ROWID + "=" + rowId, null) > 0;
     }
 
     
     /**                         PROJECTS                            **/
     /**
      * Create a new project using the title and body provided. If the project is
      * successfully created return the new rowId for that project, otherwise return
      * a -1 to indicate failure.
      * 
      * @param name the name of the project
      * @return rowId or -1 if failed
      */
     public long createProject(String name) {
         ContentValues initialValues = new ContentValues();
         initialValues.put(KEY_NAME, name);
         
         String uuidString = UUID.randomUUID().toString();
         initialValues.put(KEY_UUID, uuidString);
         
         Long now = Calendar.getInstance().getTime().getTime();
         initialValues.put(KEY_LATEST, now);
         
         initialValues.put(KEY_CHANGED, 0);
 
         return mDb.insert(DATABASE_TABLE_PROJECTS, null, initialValues);
     }
 
     /**
      * Delete the project with the given rowId
      * 
      * @param rowId id of project to delete
      * @return true if deleted, false otherwise
      */
     public boolean deleteProject(long rowId) {
 
         return mDb.delete(DATABASE_TABLE_PROJECTS, KEY_ROWID + "=" + rowId, null) > 0;
     }
 
     /**
      * Return a Cursor over the list of all projects in the database
      * 
      * @return Cursor over all projects
      */
     public Cursor fetchAllProjects() {
 
         return mDb.query(DATABASE_TABLE_PROJECTS, new String[] {KEY_ROWID, KEY_UUID,
                 KEY_NAME, KEY_LATEST}, 
                 null, null, null, null, KEY_NAME + " ASC");
     }
     
     /**
      * Return a Cursor over the list of all projects in the database
      * including a count of all children tasks
      * 
      * @return Cursor over all projects
      */
     public Cursor fetchAllProjectsWithChildCount() {
     	return mDb.rawQuery("SELECT p._id, p.uuid, p.name, p.latest_update, COUNT(t._id) AS child_count " +
     			"FROM projects AS p " +
     			"LEFT OUTER JOIN tasks AS t ON p._id = t.project_id " +
     			"GROUP BY p._id", null);
     }
 
     /**
      * Return a Cursor positioned at the project that matches the given rowId
      * 
      * @param rowId id of project to retrieve
      * @return Cursor positioned to matching project, if found
      * @throws SQLException if project could not be found/retrieved
      */
     public Cursor fetchProject(long rowId) throws SQLException {
 
         Cursor mCursor =
 
             mDb.query(true, DATABASE_TABLE_PROJECTS, new String[] {KEY_ROWID,
                     KEY_UUID, KEY_NAME, KEY_LATEST}, 
                     KEY_ROWID + "=" + rowId, null,
                     null, null, null, null);
         if (mCursor != null) {
             mCursor.moveToFirst();
         }
         return mCursor;
 
     }
 
     /**
      * Update the project using the details provided. The project to be updated is
      * specified using the rowId
      * 
      * @param rowId id of project to update
      * @param name value to set project name to
      * @return true if the project was successfully updated, false otherwise
      */
     public boolean updateProject(long rowId, String name) {
         ContentValues args = new ContentValues();
         args.put(KEY_NAME, name);
         
         long now = Calendar.getInstance().getTime().getTime();
         args.put(KEY_LATEST, now);
 
         return mDb.update(DATABASE_TABLE_PROJECTS, args, KEY_ROWID + "=" + rowId, null) > 0;
     }
     
     /**                         ENTRIES                            **/
     /**
      * Create a new entry using the description and start time provided. If the entry is
      * successfully created return the new rowId for that project, otherwise return
      * a -1 to indicate failure.
      * 
      * @param description the description of the entry
      * @param startTime the start time of the entry
      * @param taskId the task of the entry
      * @return rowId or -1 if failed
      */
     public long createEntry(String description, Long startTime, Long taskId) {
         ContentValues initialValues = new ContentValues();
         initialValues.put(KEY_DESCRIPTION, description);
         initialValues.put(KEY_START_TIME, startTime);
         initialValues.put(KEY_TASK_ID, taskId);
         
         String uuidString = UUID.randomUUID().toString();
         initialValues.put(KEY_UUID, uuidString);
         
         //Calendar calendar = Calendar.getInstance();
         //java.util.Date now = calendar.getTime();
         Long now = Calendar.getInstance().getTime().getTime();
         Long minutes = ((now - startTime) / 1000) / 60;
         initialValues.put(KEY_LATEST, now);
         initialValues.put(KEY_MINUTES, minutes);
         
         initialValues.put(KEY_CHANGED, 0);
 
         return mDb.insert(DATABASE_TABLE_ENTRIES, null, initialValues);
     }
 
     /**
      * Delete the entry with the given rowId
      * 
      * @param rowId id of entry to delete
      * @return true if deleted, false otherwise
      */
     public boolean deleteEntry(long rowId) {
 
         return mDb.delete(DATABASE_TABLE_ENTRIES, KEY_ROWID + "=" + rowId, null) > 0;
     }
 
     /**
      * Return a Cursor over the list of all entries in the database
      * 
      * @return Cursor over all entries
      */
     public Cursor fetchAllEntries() {
 
         return mDb.query(DATABASE_TABLE_ENTRIES, new String[] {KEY_ROWID, KEY_UUID,
                 KEY_DESCRIPTION, KEY_START_TIME, KEY_LATEST, KEY_MINUTES, KEY_TASK_ID}, 
                 null, null, null, null, KEY_START_TIME + " DESC");
     }
 
     /**
      * Return a Cursor positioned at the entry that matches the given rowId
      * 
      * @param rowId id of entry to retrieve
      * @return Cursor positioned to matching entry, if found
      * @throws SQLException if entry could not be found/retrieved
      */
     public Cursor fetchEntry(long rowId) throws SQLException {
 
         Cursor mCursor =
 
             mDb.query(true, DATABASE_TABLE_ENTRIES, new String[] {KEY_ROWID, KEY_UUID,
                     KEY_DESCRIPTION, KEY_START_TIME, KEY_LATEST, KEY_MINUTES, KEY_TASK_ID}, 
                     KEY_ROWID + "=" + rowId, null,
                     null, null, null, null);
         if (mCursor != null) {
             mCursor.moveToFirst();
         }
         return mCursor;
 
     }
     
     /**
      * Return a Cursor over the list of entries that match the given taskId
      * 
      * @param taskId id of task entries to retrieve
      * @return Cursor positioned to matching entries, if found
      * @throws SQLException if entry could not be found/retrieved
      */
     public Cursor fetchEntriesByTask(long taskId) throws SQLException {
 
         Cursor mCursor =
 
             mDb.query(true, DATABASE_TABLE_ENTRIES, new String[] {KEY_ROWID, KEY_UUID,
                     KEY_DESCRIPTION, KEY_LATEST, KEY_MINUTES, KEY_TASK_ID, KEY_START_TIME,
                     "strftime('%m-%d',(start_time / 1000), 'unixepoch', 'localtime') as " + KEY_START_DATE_MMDD, 
                     "time((start_time / 1000), 'unixepoch', 'localtime') as " + KEY_START_TIME_HHMM,
                     "minutes || 'm' as " + KEY_MINUTES_FORMATTED}, 
                     KEY_TASK_ID + "=" + taskId, null, null, null, 
                     KEY_START_TIME + " DESC", null);
         if (mCursor != null) {
             mCursor.moveToFirst();
         }
         return mCursor;
 
     }
 
     /**
      * Update the entry using the details provided. The entry to be updated is
      * specified using the rowId
      * 
      * @param rowId id of entry to update
      * @param description value to set entry description to
      * @param start_time value to set entry start time to
      * @return true if the entry was successfully updated, false otherwise
      */
     public boolean updateEntry(long rowId, String description, Long startTime) {
         ContentValues args = new ContentValues();
         args.put(KEY_DESCRIPTION, description);
         args.put(KEY_START_TIME, startTime);
         
         //Calendar calendar = Calendar.getInstance();
         //java.util.Date now = calendar.getTime();
         //args.put(KEY_LATEST, now.getTime());
         Long now = Calendar.getInstance().getTime().getTime();
         args.put(KEY_LATEST, now);
 
         return mDb.update(DATABASE_TABLE_ENTRIES, args, KEY_ROWID + "=" + rowId, null) > 0;
     }
     
     /**                         RUNNINGTIMES                            **/
     /**
      * Create a new runningtime connected to the task provided. If the runningtime is
      * successfully created return the new rowId for that runningtime, otherwise return
      * a -1 to indicate failure.
      * 
      * @param taskId the task id of the runningtime
      * @return rowId or -1 if failed
      */
     public long createRunningTime(Long taskId) {
         ContentValues initialValues = new ContentValues();
         initialValues.put(KEY_TASK_ID, taskId);
         
         String uuidString = UUID.randomUUID().toString();
         initialValues.put(KEY_UUID, uuidString);
         
         Long now = Calendar.getInstance().getTime().getTime();
         initialValues.put(KEY_START_TIME, now);
         initialValues.put(KEY_DESCRIPTION, "");
         
         initialValues.put(KEY_CHANGED, 0);
 
         return mDb.insert(DATABASE_TABLE_RUNNINGTIMES, null, initialValues);
     }
     
     /**
      * Update the running time using the details provided. The entry to be updated is
      * specified using the rowId
      * 
      * @param rowId id of running time to update
      * @param description value to set running time description to
      * @return true if the running time was successfully updated, false otherwise
      */
     public boolean updateRunningTimeDescription(long rowId, String description) {
         ContentValues args = new ContentValues();
         args.put(KEY_DESCRIPTION, description);
 
        return mDb.update(DATABASE_TABLE_RUNNINGTIMES, args, KEY_ROWID + "=" + rowId, null) > 0;
     }
 
     /**
      * Delete the runningtime with the given rowId
      * 
      * @param rowId id of runningtime to delete
      * @return true if deleted, false otherwise
      */
     public boolean deleteRunningTime(Long rowId) {
 
         return mDb.delete(DATABASE_TABLE_RUNNINGTIMES, KEY_ROWID + "=" + rowId, null) > 0;
     }
 
     /**
      * Return a Cursor over the list of all runningtimes in the database
      * 
      * @return Cursor over all runningtimes
      */
     public Cursor fetchAllRunningTimes() {
 
         return mDb.query(DATABASE_TABLE_RUNNINGTIMES, new String[] {KEY_ROWID, KEY_UUID,
                 KEY_START_TIME, KEY_DESCRIPTION, KEY_TASK_ID}, 
                 null, null, null, null, KEY_NAME + " ASC");
     }
     /**
      * Return a Cursor positioned at the runningtime that matches the given rowId
      * 
      * @param rowId id of runningtime to retrieve
      * @return Cursor positioned to matching runningtime, if found
      * @throws SQLException if runningtime could not be found/retrieved
      */
     public Cursor fetchRunningTime(long rowId) throws SQLException {
 
         Cursor mCursor =
 
             mDb.query(true, DATABASE_TABLE_RUNNINGTIMES, new String[] {KEY_ROWID,
                     KEY_UUID, KEY_START_TIME, KEY_DESCRIPTION, KEY_TASK_ID}, 
                     KEY_ROWID + "=" + rowId, null,
                     null, null, null, null);
         if (mCursor != null) {
             mCursor.moveToFirst();
         }
         return mCursor;
     }
     
     /**
      * Return a Cursor positioned at the runningtime that matches the given rowId
      * 
      * @param taskId task id of runningtime to retrieve
      * @return Cursor positioned to matching runningtime, if found
      * @throws SQLException if runningtime could not be found/retrieved
      */
     public Cursor fetchRunningTimeByTask(long taskId) throws SQLException {
 
         Cursor mCursor =
 
             mDb.query(true, DATABASE_TABLE_RUNNINGTIMES, new String[] {KEY_ROWID,
                     KEY_UUID, KEY_START_TIME, KEY_DESCRIPTION, KEY_TASK_ID}, 
                     KEY_TASK_ID + "=" + taskId, null,
                     null, null, null, null);
         if (mCursor != null) {
             mCursor.moveToFirst();
         }
         return mCursor;
     }
 }
