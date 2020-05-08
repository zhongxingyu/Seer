 package com.calendar.reporter.database;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import com.calendar.reporter.helper.Messenger;
 
 import java.io.*;
 import java.util.Scanner;
 
 public class DataBaseHelper extends SQLiteOpenHelper {
     private static final String DATABASE_NAME = "reporter.db";
    private static final int DATABASE_VERSION = 12;
     private static final String POPULATOR = "populate.sql";
     private Context context;
 
     public DataBaseHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
         this.context = context;
     }
 
     @Override
     public void onCreate(SQLiteDatabase database) {
         BufferedReader br = null;
         try {
             br = new BufferedReader(new InputStreamReader(context.getAssets().open(POPULATOR)));
         } catch (IOException e) {
             throw new Error(e.getMessage());
         }
         String line;
         try {
             while((line=br.readLine()) != null){
                 database.execSQL(line);
             }
         } catch (IOException e) {
             throw new Error(e.getMessage());
 
         }
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         Log.d(DataBaseHelper.class.getName(),"Upgrading database from version " + oldVersion + " to "
                 + newVersion + ", which will destroy all old data");
         db.execSQL("DROP TABLE IF EXISTS tasks");
         db.execSQL("DROP TABLE IF EXISTS activities");
         db.execSQL("DROP TABLE IF EXISTS users");
         db.execSQL("DROP TABLE IF EXISTS projects");
         onCreate(db);
     }
 
 }
