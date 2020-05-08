 package com.islamsharabash.clock_of_you;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 import android.util.Log;
 
 public class MovementOpenHelper extends SQLiteOpenHelper {
 
   private static final String DATABASE_NAME = "clock_of_you.db";
   private static final int DATABASE_VERSION = 1;
   private static final String MOVEMENT_TABLE_NAME = "movements";
   private static final String MOVEMENT_CREATED_AT_INDEX_NAME = "created_at_index";
   private static final String MOVEMENT_CREATED_AT_MAGNITUDE_INDEX_NAME = "created_at_magnitude_index";
   private static final String COLUMN_NAME_ID = "_id";
   private static final String COLUMN_NAME_CREATED_AT = "created_at";
   private static final String COLUMN_NAME_MAGNITUDE = "magnitude";
 
   private static final String MOVEMENT_TABLE_CREATE =
     "CREATE TABLE " + MOVEMENT_TABLE_NAME + " (" +
     COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
     COLUMN_NAME_CREATED_AT + " INTEGER, " +
     COLUMN_NAME_MAGNITUDE + " REAL);";
 
   private static final String MOVEMENT_CREATED_AT_INDEX_CREATE =
     "CREATE INDEX " + MOVEMENT_CREATED_AT_INDEX_NAME + " ON " +
     MOVEMENT_TABLE_NAME + " (" + COLUMN_NAME_CREATED_AT + ");";
 
   private static final String MOVEMENT_CREATED_AT_MAGNITUDE_INDEX_CREATE =
     "CREATE INDEX " + MOVEMENT_CREATED_AT_MAGNITUDE_INDEX_NAME + " ON " +
     MOVEMENT_TABLE_NAME + " (" + COLUMN_NAME_CREATED_AT + ", " +
     COLUMN_NAME_MAGNITUDE + ");";
 
   private static final String INSERT_MOVEMENT =
     "INSERT INTO " + MOVEMENT_TABLE_NAME + " (" + COLUMN_NAME_CREATED_AT + ", "
     + COLUMN_NAME_MAGNITUDE + ") VALUES (?, ?);";
 
   private static final String GET_MAGNITUDE_TOTAL_IN_TIME =
     "SELECT TOTAL(" + COLUMN_NAME_MAGNITUDE + ") FROM " + MOVEMENT_TABLE_NAME +
     " WHERE " + COLUMN_NAME_CREATED_AT + " BETWEEN ? AND ?;";
 
   private SQLiteDatabase db;
   private SQLiteStatement insert_movement_statement;
 
   MovementOpenHelper(Context context) {
     super(context, DATABASE_NAME, null, DATABASE_VERSION);
 
     db = getWritableDatabase();
     insert_movement_statement = db.compileStatement(INSERT_MOVEMENT);
   }
 
   @Override
   public void onCreate(SQLiteDatabase db) {
     Log.d("DATABASE", "Creating the database table.");
     db.execSQL(MOVEMENT_TABLE_CREATE);
     db.execSQL(MOVEMENT_CREATED_AT_INDEX_CREATE);
     db.execSQL(MOVEMENT_CREATED_AT_MAGNITUDE_INDEX_CREATE);
   }
 
   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
     // drop and recreate the table, we are lazy.
     Log.d("DATABASE", "Dropping and recreating the database table.");
 
     db.execSQL("DROP TABLE IF EXISTS " + MOVEMENT_TABLE_NAME);
   }
 
   public void insertMovement(long created_at, float magnitude) {
     insert_movement_statement.bindLong(1, created_at);
     insert_movement_statement.bindDouble(2, magnitude);
     insert_movement_statement.executeInsert();
     insert_movement_statement.clearBindings();
   }
 
   public double getTotalMagnitude(long start_time, long end_time) {
     String[] args = {Long.toString(start_time), Long.toString(end_time)};
     Cursor cursor = db.rawQuery(GET_MAGNITUDE_TOTAL_IN_TIME, args);
     cursor.moveToFirst();
     double result = cursor.getDouble(0);
    cursor.close();
     return result;
   }
 }
