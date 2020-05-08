 package com.example.gomotion;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.example.gomotion.BodyWeightExercise.BodyWeightType;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class OfflineDatabase extends SQLiteOpenHelper implements Database
 {
 	// All Static variables
     // Database Version
     private final static int DATABASE_VERSION = 1;
  
     // Database Name
     protected final static String DATABASE_NAME = "offlineDB";
  
     // Table names
     private final String TABLE_BODYWEIGHT = "bodyweight";
     private final String TABLE_CARDIO = "cardio";
  
     // Duplicate column names
     private final String KEY_ID = "_id";
     private final String KEY_TIMESTAMP = "timestamp";
     private final String KEY_TYPE = "type";
 
     // Body Weight Exercise Table Column names
     private final String KEY_SETS = "sets";
     private final String KEY_REPS = "reps";
     
     // Cardio Exercise Table Column names
     private final String KEY_TIMELENGTH = "timelength";
     private final String KEY_DISTANCE = "distance";
 
     public OfflineDatabase(Context context)
     {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
 	@Override
 	public void onCreate(SQLiteDatabase db)
 	{
 		// Create Body Weight Exercise table
 		String CREATE_BODYWEIGHT_TABLE = "CREATE TABLE " + TABLE_BODYWEIGHT + "("
                 + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TIMESTAMP + " INTEGER,"
                 + KEY_SETS + " INTEGER," + KEY_REPS + " INTEGER," +  KEY_TYPE + " TEXT" +")";
 		
         db.execSQL(CREATE_BODYWEIGHT_TABLE);
         
         // Create Cardio Exercise table
  		String CREATE_CARDIO_TABLE = "CREATE TABLE " + TABLE_CARDIO + "("
                  + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TIMESTAMP + " INTEGER,"
                  + KEY_DISTANCE + " INTEGER," + KEY_TIMELENGTH + " INTEGER," +  KEY_TYPE + " TEXT" +")";
  		
          db.execSQL(CREATE_CARDIO_TABLE);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
 	{
 		 // Drop older tables if exist
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_BODYWEIGHT);
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARDIO);
 
         // Create tables again
         onCreate(db);
 	}
 	
 	/****************************** Body Weight exercise methods ***********************************************/
 	
 	// Add a new body weight exercise
 	public boolean add(BodyWeightExercise exercise)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 
 		ContentValues values = new ContentValues();
 		values.put(KEY_TIMESTAMP, exercise.getTimeStamp());
 		values.put(KEY_SETS, exercise.getSets());
 		values.put(KEY_REPS, exercise.getReps());
 		values.put(KEY_TYPE, exercise.getType().name());
 		
 		// Insert into database
 		db.insert(TABLE_BODYWEIGHT, null, values);
 		db.close();
 		return true;
 	}
 	
 	// Get a single body weight exercise
 	public BodyWeightExercise getBodyWeightExercise(int id)
 	{
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Cursor cursor = db.query(TABLE_BODYWEIGHT, new String[] { KEY_ID, KEY_TIMESTAMP, 
 				KEY_SETS, KEY_REPS, KEY_TYPE }, KEY_ID + "=?",
 	            new String[] { String.valueOf(id) }, null, null, null, null);
 
 		if(cursor.moveToFirst()) 
 		{		
 			BodyWeightExercise exercise = new BodyWeightExercise(
 					Integer.parseInt(cursor.getString(0)),
					Long.parseLong(cursor.getString(1)), 
 					Integer.parseInt(cursor.getString(2)),
 					Integer.parseInt(cursor.getString(3)),
 					BodyWeightExercise.BodyWeightType.valueOf(cursor.getString(4)));
 			
 			cursor.close();
 			
 			return exercise;
 		}
 		else return null;
 	}
 	
 	// Returns a list of all body weight exercises
 	public List<BodyWeightExercise> getAllBodyWeightExercises()
 	{
 		List<BodyWeightExercise> exerciseList = new ArrayList<BodyWeightExercise>();
 		
 		String query = "SELECT * FROM " + TABLE_BODYWEIGHT;		
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(query, null);
 		
 		if(cursor.moveToFirst())
 		{
 			do {
 				BodyWeightExercise exercise = new BodyWeightExercise(
 						Integer.parseInt(cursor.getString(0)),
 						Long.parseLong(cursor.getString(1)),
 						Integer.parseInt(cursor.getString(2)),
 						Integer.parseInt(cursor.getString(3)),
 						BodyWeightExercise.BodyWeightType.valueOf(cursor.getString(4))	
 				);
 				exerciseList.add(exercise);
 			} while(cursor.moveToNext());
 		}
 		
 		return exerciseList;
 	}
 	
 	// Get count of all body weight exercises
 	public int getBodyWeightExercisesCount() 
 	{
 		String query = "SELECT * FROM " + TABLE_BODYWEIGHT;		
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(query, null);
 		cursor.close();
 		
 		return cursor.getCount();
 	}
 	
 	// Updates a single body weight exercise
 	public int updateBodyWeightExercise(BodyWeightExercise exercise)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		values.put(KEY_TIMESTAMP, exercise.getTimeStamp());
 		values.put(KEY_SETS, exercise.getSets());
 		values.put(KEY_REPS, exercise.getReps());
 		values.put(KEY_TYPE, exercise.getType().toString());
 		
 		return db.update(TABLE_BODYWEIGHT, values, KEY_ID + " = ?", new String[] { String.valueOf(exercise.getID())});
 	}
 	
 	// Delete a single body weight exercise
 	public void deleteBodyWeightExercise(BodyWeightExercise exercise) 
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		db.delete(TABLE_BODYWEIGHT, KEY_ID + " = ?", new String[] { String.valueOf(exercise.getID())});
 		db.close();
 	}
 	
 	/****************************** Cardio exercise methods ***********************************************/
 	
 	// Add a single cardio exercise to the database
 	public boolean add(CardioExercise exercise)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 
 		ContentValues values = new ContentValues();
 		values.put(KEY_TIMESTAMP, exercise.getTimeStamp());
 		values.put(KEY_DISTANCE, exercise.getDistance());
 		values.put(KEY_TIMELENGTH, exercise.getTimeLength());
 		values.put(KEY_TYPE, exercise.getType().name());
 		
 		// Insert into database
 		db.insert(TABLE_CARDIO, null, values);
 		db.close();
 		return true;
 	}
 	
 	// Get a single cardio exercise
 	public CardioExercise getCardioExercise(int id) 
 	{
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Cursor cursor = db.query(TABLE_CARDIO, new String[] { KEY_ID, KEY_TIMESTAMP, 
 				KEY_DISTANCE, KEY_TIMELENGTH, KEY_TYPE }, KEY_ID + "=?",
 	            new String[] { String.valueOf(id) }, null, null, null, null);
 
 		if(cursor.moveToFirst()) 
 		{		
 			CardioExercise exercise = new CardioExercise(
 					Integer.parseInt(cursor.getString(0)),
 					Long.parseLong(cursor.getString(1)),
 					Integer.parseInt(cursor.getString(2)),
 					Integer.parseInt(cursor.getString(3)), 
 					CardioExercise.CardioType.valueOf(cursor.getString(4)));
 			
 			cursor.close();
 			
 			return exercise;
 		}
 		else return null;
 	}
 	
 	// Returns a list of all cardio exercises
 	public List<CardioExercise> getAllCardioExercises() 
 	{
 		List<CardioExercise> exerciseList = new ArrayList<CardioExercise>();
 		
 		String query = "SELECT * FROM " + TABLE_CARDIO;		
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(query, null);
 		
 		if(cursor.moveToFirst())
 		{
 			do {
				CardioExercise exercise = new CardioExercise(
						Integer.parseInt(cursor.getString(0)),
						Long.parseLong(cursor.getString(1)),
						Integer.parseInt(cursor.getString(2)),
						Integer.parseInt(cursor.getString(3)),
						CardioExercise.CardioType.valueOf(cursor.getString(4))	
				);	
 				
 				exerciseList.add(exercise);
 			} while(cursor.moveToNext());
 		}
 		
 		return exerciseList;
 	}
 	
 	// Get count of all cardio exercises
 	public int getCardioExercisesCount() 
 	{
 		String query = "SELECT * FROM " + TABLE_CARDIO;		
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(query, null);
 		cursor.close();
 		
 		return cursor.getCount();
 	}
 	
 	// Updates a single cardio exercise
 	public int updateCardioExercise(CardioExercise exercise)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		values.put(KEY_TIMESTAMP, exercise.getTimeStamp());
 		values.put(KEY_DISTANCE, exercise.getDistance());
 		values.put(KEY_TIMELENGTH, exercise.getTimeLength());
 		values.put(KEY_TYPE, exercise.getType().toString());
 		
 		return db.update(TABLE_CARDIO, values, KEY_ID + " = ?", new String[] { String.valueOf(exercise.getID())});
 	}
 	
 	// Delete a single cardio exercise
 	public void deleteCardioExercise(CardioExercise exercise)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		db.delete(TABLE_CARDIO, KEY_ID + " = ?", new String[] { String.valueOf(exercise.getID())});
 		db.close();
 	}
 }
