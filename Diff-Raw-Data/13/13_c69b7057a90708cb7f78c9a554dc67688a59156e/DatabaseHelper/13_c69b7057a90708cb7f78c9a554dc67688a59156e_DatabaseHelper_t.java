 package com.binoy.vibhinna;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.provider.BaseColumns;
 import android.widget.Toast;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 
     protected static final String TAG = "DatabaseHelper";
     protected Context mContext;
     private static final int DATABASE_VERSION = 2;
     public static final String VFS_DATABASE_NAME = "vs_db";
     public static final String VFS_DATABASE_TABLE = "vs_table";
     public static final String VIRTUAL_SYSTEM_COLUMN_DESCRIPTION = "vsdesc";
     public static final String VIRTUAL_SYSTEM_COLUMN_NAME = "vsname";
     public static final String VIRTUAL_SYSTEM_COLUMN_PATH = "vspath";
     public static final String VIRTUAL_SYSTEM_COLUMN_TYPE = "vstype";
     private static final String VFS_DATABASE_CREATE_STATEMENT = "CREATE TABLE IF NOT EXISTS "
             + VFS_DATABASE_TABLE + " ( " + BaseColumns._ID
             + " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, " + VIRTUAL_SYSTEM_COLUMN_NAME
             + " VARCHAR(50), " + VIRTUAL_SYSTEM_COLUMN_PATH + " VARCHAR(50) UNIQUE, "
             + VIRTUAL_SYSTEM_COLUMN_TYPE + " INTEGER, " + VIRTUAL_SYSTEM_COLUMN_DESCRIPTION
             + " VARCHAR(200))";
     public static final String TASK_DATABASE_TABLE = "task_db";
     public static final String TASK_TYPE = "task_type";
     public static final String TASK_STATUS = "task_status";
     public static final String TASK_MESSAGE = "task_message";
     public static final String TASK_PROGRESS = "task_progress";
     public static final String TASK_VS = "task_vs";
     public static final String TASK_DATABASE_CREATE_STATEMENT = "CREATE TABLE IF NOT EXISTS "
             + TASK_DATABASE_TABLE + " ( " + BaseColumns._ID
             + " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, " + TASK_VS + " VARCHAR(100), "
             + TASK_TYPE + " INTEGER, " + TASK_STATUS + " INTEGER, " + TASK_PROGRESS + " INTEGER, "
             + TASK_MESSAGE + " VARCHAR(100))";
 
     public DatabaseHelper(Context mContext) {
         super(mContext, VFS_DATABASE_NAME, null, DATABASE_VERSION);
         this.mContext = mContext;
     }
 
     @Override
     public void onCreate(SQLiteDatabase database) {
        Toast.makeText(mContext, R.string.creating_database, Toast.LENGTH_SHORT).show();
         database.execSQL(VFS_DATABASE_CREATE_STATEMENT);
         database.execSQL(TASK_DATABASE_CREATE_STATEMENT);
         DatabaseUtils.scanFolder(database, mContext);
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Toast.makeText(mContext, R.string.upgrading_database, Toast.LENGTH_SHORT).show();
         database.execSQL(TASK_DATABASE_CREATE_STATEMENT);
     }
 }
