 /*
  * Copyright (C) 2012 Jimmy Theis. Licensed under the MIT License:
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.jetheis.android.grades.storage;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 
     private static final String DATABASE_NAME = "gradecalc-data.db.sqlite";
     private static final int DATABASE_VERSION = 1;
 
     private static DatabaseHelper sInstance;
 
     private static final String COURSES_TABLE = "courses";
     private static final String COURSES_TABLE_CREATION_STATEMENT;
 
     static {
         StringBuilder builder = new StringBuilder();
         builder.append("CREATE TABLE ");
         builder.append(COURSES_TABLE);
         builder.append(" (_id INTEGER PRIMARY KEY AUTOINCREMENT");
         builder.append(", name TEXT NOT NULL");
         builder.append(", grade_type INTEGER NOT NULL");
         builder.append(")");
         COURSES_TABLE_CREATION_STATEMENT = builder.toString();
     }
 
     private static final String GRADE_COMPONENTS_TABLE = "grade_components";
     private static final String GRADE_COMPONENTS_TABLE_CREATION_STATEMENT;
 
     static {
         StringBuilder builder = new StringBuilder();
         builder.append("CREATE TABLE ");
         builder.append(GRADE_COMPONENTS_TABLE);
         builder.append(" (_id INTEGER PRIMARY KEY AUTOINCREMENT");
         builder.append(", _course_id INTEGER NOT NULL");
         builder.append(", name TEXT NOT NULL");
         builder.append(", earned REAL NOT NULL");
         builder.append(", total REAL NOT NULL");
         builder.append(")");
         GRADE_COMPONENTS_TABLE_CREATION_STATEMENT = builder.toString();
     }
 
     private SQLiteDatabase mDb;
 
     /**
      * Default constructor. Takes a {@link Context} only, because the database
      * name and version are populated from private static variables.
      * 
      * @param context
      *            The {@link Context} to initialize this {@link DatabaseHelper}
      *            with.
      */
     private DatabaseHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
         mDb = getWritableDatabase();
     }
 
     public static void initializeDatabaseHelper(Context context) {
        if (sInstance != null && sInstance.getDb() != null) {
             // Close the database connection in a DatabaseHelper instance
             // already existed.
             sInstance.getDb().close();
         }
 
         sInstance = new DatabaseHelper(context);
     }
 
     public static DatabaseHelper getInstance() {
         if (sInstance == null) {
             throw new IllegalStateException("DatabaseHelper has not been initialized yet");
         }
 
         return sInstance;
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
         db.execSQL(COURSES_TABLE_CREATION_STATEMENT);
         db.execSQL(GRADE_COMPONENTS_TABLE_CREATION_STATEMENT);
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         db.execSQL("DROP TABLE " + COURSES_TABLE);
         db.execSQL("DROP TABLE " + GRADE_COMPONENTS_TABLE);
 
         onCreate(db);
     }
 
     /**
      * Get this instance's {@link SQLiteDatabase} instance. Because there should
      * only be one instance of {@link DatabaseHelper}, this database should be
      * the only one in existance in the app.
      * 
      * @return The {@link SQLiteDatabase}.
      */
     public SQLiteDatabase getDb() {
         return mDb;
     }
 
     /**
      * Close the connection to the database. This method should only be called
      * once, as the application completely exits.
      */
     public void close() {
         mDb.close();
         mDb = null;
     }
 }
