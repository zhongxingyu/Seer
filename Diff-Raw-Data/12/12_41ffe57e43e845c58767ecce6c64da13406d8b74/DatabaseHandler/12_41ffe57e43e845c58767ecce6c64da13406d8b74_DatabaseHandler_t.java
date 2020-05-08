 package com.bums.library;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 import java.util.HashMap;
 
 public class DatabaseHandler extends SQLiteOpenHelper {
 
     // All Static variables
     // Database Version
     private static final int DATABASE_VERSION = 1;
 
     // Database Name
     private static final String DATABASE_NAME = "small";
 
     // Login table name
     private static final String TABLE_LOGIN = "login";
 
     // Login Table Columns names
    private static final String KEY_ID = "id";
     private static final String KEY_USERNAME = "username";
 
     public DatabaseHandler(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
     // Creating Tables
     @Override
     public void onCreate(SQLiteDatabase db) {
         String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_LOGIN + "("
                 + KEY_ID + " INTEGER PRIMARY KEY,"
                 + KEY_USERNAME + " TEXT" + ")";
         db.execSQL(CREATE_LOGIN_TABLE);
     }
 
     // Upgrading database
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         // Drop older table if existed
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);
 
         // Create tables again
         onCreate(db);
     }
 
     /**
      * Storing user details in database
      * */
     public void addUser(String id, String username) {
         SQLiteDatabase db = this.getWritableDatabase();
 
         ContentValues values = new ContentValues();
         values.put(KEY_USERNAME, username); // UserName
        values.put(KEY_ID, id); // ID
 
         // Inserting Row
         db.insert(TABLE_LOGIN, null, values);
         db.close(); // Closing database connection
     }
 
 
     /**
      * Getting user data from database
      * */
     public HashMap<String, String> getUserDetails(){
         HashMap<String,String> user = new HashMap<String,String>();
        String selectQuery = "SELECT rowid _id, * FROM " + TABLE_LOGIN;
 
         SQLiteDatabase db = this.getReadableDatabase();
         Cursor cursor = db.rawQuery(selectQuery, null);
         // Move to first row
         cursor.moveToFirst();
         if(cursor.getCount() > 0){
             user.put("id", cursor.getString(1));
             user.put("username", cursor.getString(2));
         }
         cursor.close();
         db.close();
         // return user
         return user;
     }
 
     /**
      * Getting user login status
      * return true if rows are there in table
      * */
     public int getRowCount() {
        String countQuery = "SELECT rowid _id, * FROM " + TABLE_LOGIN;
         SQLiteDatabase db = this.getReadableDatabase();
         Cursor cursor = db.rawQuery(countQuery, null);
         int rowCount = cursor.getCount();
         db.close();
         cursor.close();
 
         // return row count
         return rowCount;
     }
 
 
     /**
      * Re create database
      * Delete all tables and create them again
      * */
     public void resetTables(){
         SQLiteDatabase db = this.getWritableDatabase();
         // Delete All Rows
         db.delete(TABLE_LOGIN, null, null);
         db.close();
     }
 
 }
