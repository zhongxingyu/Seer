 package com.calendar.reporter.database.project;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 import com.calendar.reporter.database.DataBaseHelper;
 import com.calendar.reporter.database.task.TaskDataSource;
 import com.calendar.reporter.database.user.UserStructure;
 import com.calendar.reporter.helper.Messenger;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ProjectDataSource {
     private DataBaseHelper dbHelper;
 
     private String[] allColumns = {
             ProjectStructure.COLUMN_ID,
             ProjectStructure.COLUMN_NAME,
             ProjectStructure.COLUMN_DESCRIPTION,
             ProjectStructure.USER_ID
     };
     private Context context;
 
     public ProjectDataSource(Context context) {
         this.dbHelper = new DataBaseHelper(context);
         this.context = context;
     }
 
     public ProjectStructure createProject(String name, String description, long userId) {
         SQLiteDatabase database = dbHelper.getWritableDatabase();
         try {
             ContentValues values = new ContentValues();
             values.put(ProjectStructure.USER_ID, userId);
             values.put(ProjectStructure.COLUMN_NAME, name);
             values.put(ProjectStructure.COLUMN_DESCRIPTION, description);
             long insertId = database.insert(ProjectStructure.TABLE_NAME, null,
                     values);
             Cursor cursor = database.query(ProjectStructure.TABLE_NAME,
                     allColumns, ProjectStructure.COLUMN_ID + " = " + insertId, null,
                     null, null, null);
             cursor.moveToFirst();
             Messenger messenger = new Messenger(ProjectDataSource.class.getName());
             ProjectStructure project = cursorToProject(cursor);
             messenger.debug("id " + project.getId() + " user_id " + project.getUserId());
             return project;
         } finally {
             database.close();
         }
     }
 
     private ProjectStructure cursorToProject(Cursor cursor) {
         ProjectStructure project = new ProjectStructure();
         project.setId(cursor.getLong(0));
         project.setName(cursor.getString(1));
         project.setDescription(cursor.getString(2));
         project.setUserId(cursor.getLong(3));
         return project;
     }
 
     public List<ProjectStructure> getAllProjects(long userId) {
         SQLiteDatabase database = dbHelper.getWritableDatabase();
         List<ProjectStructure> projects = null;
         try {
             projects = new ArrayList<ProjectStructure>();
             Cursor cursor = database.query(ProjectStructure.TABLE_NAME,
                     allColumns, ("user_id = " + userId), null, null, null, null);
             cursor.moveToFirst();
             while (!cursor.isAfterLast()) {
                 ProjectStructure project = cursorToProject(cursor);
                 projects.add(project);
                 cursor.moveToNext();
             }
             cursor.close();
         } finally {
             database.close();
         }
         return projects;
     }
 
     public void deleteProject(long id) {
         SQLiteDatabase database = dbHelper.getWritableDatabase();
             int status = database.delete(ProjectStructure.TABLE_NAME, ProjectStructure.COLUMN_ID + " = " + id, null);
             if(status == 1){
                 database.close();
                 TaskDataSource taskDataSource = new TaskDataSource(context);
                 taskDataSource.deleteTasks(id);
             }else{
                 Log.e(ProjectStructure.class.getName(), "Can`t delete project id: " + id);
             }
     }
 
     public ProjectStructure getNAProject(long userId) {
         SQLiteDatabase database = dbHelper.getReadableDatabase();
         try {
             Cursor cursor = database.query(ProjectStructure.TABLE_NAME,
                     allColumns, ("name is 'N/A' and user_id = " + userId), null, null, null, null);
             cursor.moveToFirst();
             ProjectStructure project = cursorToProject(cursor);
             cursor.close();
             return project;
         } finally {
             database.close();
         }
     }
 
     public ProjectStructure getProject(long projectId) {
         SQLiteDatabase database = dbHelper.getReadableDatabase();
         try {
             Cursor cursor = database.query(ProjectStructure.TABLE_NAME,
                     allColumns, ("_id = " + projectId), null, null, null, null);
            cursor.moveToFirst();
            ProjectStructure project = cursorToProject(cursor);
             cursor.close();
             return project;
         } finally {
             database.close();
         }
     }
 
     public int updateProject(String nameProject, String descriptionProject, long projectId) {
         SQLiteDatabase database = dbHelper.getReadableDatabase();
         try {
             ContentValues values = new ContentValues();
             values.put(ProjectStructure.COLUMN_NAME, nameProject);
             values.put(ProjectStructure.COLUMN_DESCRIPTION, descriptionProject);
             String whereClause = ("_id = " + projectId);
             return database.update(ProjectStructure.TABLE_NAME, values, whereClause, null);
         } finally {
             database.close();
         }
 
     }
 
     public boolean projectExist(String nameProject) {
         SQLiteDatabase database = dbHelper.getReadableDatabase();
         try{
             String whereSequence = "name = '" + nameProject + "'";
             Cursor cursor = database.query(ProjectStructure.TABLE_NAME,
                     allColumns, whereSequence, null, null, null, null);
 
             ProjectStructure project = null;
             if(cursor.moveToFirst()){
                 project = cursorToProject(cursor);
             }
             return project != null;
         }finally {
             database.close();
         }
     }
 }
