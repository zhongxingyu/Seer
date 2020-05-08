 package com.ksj.bamft;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
  
 public class DatabaseHandler extends SQLiteOpenHelper {
  
     // All Static variables
     // Database Version
     private static final int DATABASE_VERSION = 1;
  
     // Database Name
     private static final String DATABASE_NAME = "bamftRecords";
  
     // Tables name
     private static final String TABLE_LANDMARKS = "landmarks";
     private static final String TABLE_TRUCKS = "trucks";
     private static final String TABLE_SCHEDULES = "schedules";
  
 
     // Table column names
     private static final String KEY_LANDMARK_ID = "id";
     private static final String KEY_LANDMARK_NAME = "name";
     private static final String KEY_LANDMARK_XCOORD = "xcoord";
     private static final String KEY_LANDMARK_YCOORD = "ycoord";
 
     private static final String KEY_TRUCK_ID = "id";
     private static final String KEY_TRUCK_NAME = "name";
     private static final String KEY_TRUCK_CUISINE = "cuisine";
     private static final String KEY_TRUCK_DESCRIPTION = "description";
     
     private static final String KEY_SCHEDULE_ID = "id";
     private static final String KEY_SCHEDULE_DAY_OF_WEEK = "day_of_week";
     private static final String KEY_SCHEDULE_TIME_OF_DAY = "time_of_day";
     private static final String KEY_SCHEDULE_TRUCK_ID = "truck_id";
     private static final String KEY_SCHEDULE_LANDMARK_ID = "landmark_id";
     
  
     public DatabaseHandler(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
  
     // Creating Tables
     @Override
     public void onCreate(SQLiteDatabase db) {
         
 
     	String CREATE_LANDMARKS_TABLE = "CREATE TABLE " + TABLE_LANDMARKS
     			+ "(" 
     			+ KEY_LANDMARK_ID + " INTEGER PRIMARY KEY,"
     			+ KEY_LANDMARK_NAME + " TEXT,"
     			+ KEY_LANDMARK_XCOORD + " TEXT,"
     			+ KEY_LANDMARK_YCOORD + " TEXT"
     			+ ")";
     	
     	String CREATE_TRUCKS_TABLE = "CREATE TABLE " + TABLE_TRUCKS
     			+ "(" 
     			+ KEY_TRUCK_ID + " INTEGER PRIMARY KEY,"
     			+ KEY_TRUCK_NAME + " TEXT,"
     			+ KEY_TRUCK_CUISINE + " TEXT,"
     			+ KEY_TRUCK_DESCRIPTION + " TEXT"
     			+ ")";
     	
     	String CREATE_SCHEDULES_TABLE = "CREATE TABLE " + TABLE_SCHEDULES
     			+ "("
     			+ KEY_SCHEDULE_ID + " INTEGER PRIMARY KEY,"
     			+ KEY_SCHEDULE_DAY_OF_WEEK + " TEXT,"
     			+ KEY_SCHEDULE_TIME_OF_DAY + " TEXT,"
     			+ KEY_SCHEDULE_TRUCK_ID + " TEXT,"
     			+ KEY_SCHEDULE_LANDMARK_ID + " TEXT"
     			+ ")";
     	
     	
     	db.execSQL(CREATE_LANDMARKS_TABLE);
     	db.execSQL(CREATE_TRUCKS_TABLE);
     	db.execSQL(CREATE_SCHEDULES_TABLE);
     	
     	
     }
  
     // Upgrading database
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         // Drop older table if existed
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_LANDMARKS);
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRUCKS);
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULES);
  
         // Create tables again
         onCreate(db);
     }
     
     public void recreateTables() {
     	SQLiteDatabase db = this.getWritableDatabase();
     	
     	db.execSQL("DROP TABLE IF EXISTS " + TABLE_LANDMARKS);
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRUCKS);
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULES);
  
         // Create tables again
         onCreate(db);
     	
     }
     
     /*
      * Begin custom functions
      */
     
     /**
      * Gets all schedules based upon a giving time and day (use string format, ie "Monday", "Evening");
      * Days: "Monday" to "Sunday";
      * Time: "Morning", "Afternoon", "Evening"
      * @param dayOfWeek
      * @param timeOfDay
      * @return List<Schedule> of schedules matching parameters
      */
     
     
     public List<Schedule> getSchedulesByDayAndTime(String dayOfWeek, String timeOfDay) {
     	String query = "SELECT \"schedules\".* FROM \"" + TABLE_SCHEDULES + "\""
     			+ "WHERE (\"schedules\".\"day_of_week\" = '" + dayOfWeek + "')"
     			+ "AND (\"schedules\".\"time_of_day\" = '" + timeOfDay + "')";
     	return getSchedulesListByQuery(query);
     }
     
     
     
    
     
     
     /*
      * End custom functions
      */
     
     
     /*
      * All CRUD(Create, Read, Update, Delete) Operations
      */
  
     void addLandmark(Landmark landmark) {
     	SQLiteDatabase db = this.getWritableDatabase();
     	
     	ContentValues values = new ContentValues();
     	values.put(KEY_LANDMARK_ID, landmark.getId()); // Landmark ID
     	values.put(KEY_LANDMARK_NAME, landmark.getName()); // Landmark Name
     	values.put(KEY_LANDMARK_XCOORD, landmark.getXcoord()); // Landmark Xcoord
     	values.put(KEY_LANDMARK_YCOORD, landmark.getYcoord()); // Landmark Ycoord
     	
     	// Insert row
     	db.insert(TABLE_LANDMARKS, null, values);
     	db.close();
     }
     
     void addTruck(Truck truck) {
     	SQLiteDatabase db = this.getWritableDatabase();
     	
     	ContentValues values = new ContentValues();
     	values.put(KEY_TRUCK_ID, truck.getId()); // Truck ID
     	values.put(KEY_TRUCK_NAME, truck.getName()); // Truck Name
     	values.put(KEY_TRUCK_CUISINE, truck.getCuisine()); // Truck cuisine
     	values.put(KEY_TRUCK_DESCRIPTION, truck.getDescription()); // Truck description
     	
     	// Insert row
     	db.insert(TABLE_TRUCKS, null, values);
     	db.close();
     }
     
     void addSchedule(Schedule schedule) {
     	SQLiteDatabase db = this.getWritableDatabase();
     	
     	ContentValues values = new ContentValues();
     	values.put(KEY_SCHEDULE_ID, schedule.getId()); // Schedule ID
     	values.put(KEY_SCHEDULE_DAY_OF_WEEK, schedule.getDayOfWeek()); // Schedule Day of Week
     	values.put(KEY_SCHEDULE_TIME_OF_DAY, schedule.getTimeOfDay()); // Schedule Time of Day
     	values.put(KEY_SCHEDULE_TRUCK_ID, schedule.getTruckId()); // Schedule truck_id
    	values.put(KEY_SCHEDULE_LANDMARK_ID, schedule.getLandmarkId()); // Schedule landmark_id
     	
     	
     	// Insert row
     	db.insert(TABLE_SCHEDULES, null, values);
     	db.close();
     }
     
     Landmark getLandmark(int id) {
     	SQLiteDatabase db = this.getReadableDatabase();
     	
     	Cursor cursor = db.query(TABLE_LANDMARKS, 
     			new String[] { KEY_LANDMARK_ID, KEY_LANDMARK_NAME, KEY_LANDMARK_XCOORD, KEY_LANDMARK_YCOORD }, 
     			KEY_LANDMARK_ID + "=?",
     			new String[] { String.valueOf(id) }, null, null, null, null);
     	if (cursor != null)
     		cursor.moveToFirst();
     	
     	Landmark landmark = new Landmark(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3));
     	db.close();
     	return landmark;
     }
     
     Truck getTruck(int id) {
     	SQLiteDatabase db = this.getReadableDatabase();
     	
     	Cursor cursor = db.query(TABLE_TRUCKS, 
     			new String[] { KEY_TRUCK_ID, KEY_TRUCK_NAME, KEY_TRUCK_CUISINE, KEY_TRUCK_DESCRIPTION }, 
     			KEY_TRUCK_ID + "=?",
     			new String[] { String.valueOf(id) }, null, null, null, null);
     	if (cursor != null)
     		cursor.moveToFirst();
     	
     	Truck truck = new Truck(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3));
     	db.close();
     	return truck;
     }
     
     Schedule getSchedule(int id) {
     	SQLiteDatabase db = this.getReadableDatabase();
     	
     	Cursor cursor = db.query(TABLE_SCHEDULES, 
     			new String[] { KEY_SCHEDULE_ID, KEY_SCHEDULE_DAY_OF_WEEK, KEY_SCHEDULE_TIME_OF_DAY, KEY_SCHEDULE_TRUCK_ID, KEY_SCHEDULE_LANDMARK_ID }, 
     			KEY_SCHEDULE_ID + "=?",
     			new String[] { String.valueOf(id) }, null, null, null, null);
     	if (cursor != null)
     		cursor.moveToFirst();
     	
     	Schedule schedule = new Schedule(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), Integer.parseInt(cursor.getString(3)), Integer.parseInt(cursor.getString(4)));
     	db.close();
     	return schedule;
     }
     
     public List<Landmark> getAllLandmarks() {
     	
     	String query = "SELECT * FROM " + TABLE_LANDMARKS;
     	return getLandmarksListByQuery(query);
     }
     
     public List<Truck> getAllTrucks() {
     	
     	String query = "SELECT * FROM " + TABLE_TRUCKS;
     	return getTrucksListByQuery(query);
     }
     
     public List<Schedule> getAllSchedules() {
     	
     	String query = "SELECT * FROM " + TABLE_SCHEDULES;
     	return getSchedulesListByQuery(query);
     }
     
     public int updateLandmark(Landmark landmark) {
     	SQLiteDatabase db = this.getWritableDatabase();
     	
     	ContentValues values = new ContentValues();
     	values.put(KEY_LANDMARK_NAME, landmark.getName());
     	values.put(KEY_LANDMARK_XCOORD, landmark.getXcoord());
     	values.put(KEY_LANDMARK_YCOORD, landmark.getYcoord());
     	
     	// update row
     	int return_val = db.update(TABLE_LANDMARKS, values, KEY_LANDMARK_ID + " = ?",
     			new String[] { String.valueOf(landmark.getId()) });
     	db.close();
     	return return_val;
     	
     }
     
     public int updateTruck(Truck truck) {
     	SQLiteDatabase db = this.getWritableDatabase();
     	
     	ContentValues values = new ContentValues();
 
     	values.put(KEY_TRUCK_NAME, truck.getName());
     	values.put(KEY_TRUCK_CUISINE, truck.getCuisine());
     	values.put(KEY_TRUCK_DESCRIPTION, truck.getDescription());
     	
     	// update row
     	int return_val =  db.update(TABLE_TRUCKS, values, KEY_TRUCK_ID + " = ?",
     			new String[] { String.valueOf(truck.getId()) });
     	db.close();
     	return return_val;
     	
     }
     
     public int updateSchedule(Schedule schedule) {
     	SQLiteDatabase db = this.getWritableDatabase();
     	
     	ContentValues values = new ContentValues();
 
     	values.put(KEY_SCHEDULE_DAY_OF_WEEK, schedule.getDayOfWeek());
     	values.put(KEY_SCHEDULE_TIME_OF_DAY, schedule.getTimeOfDay());
     	values.put(KEY_SCHEDULE_TRUCK_ID, schedule.getTruckId());
     	values.put(KEY_SCHEDULE_LANDMARK_ID, schedule.getLandmarkId());
     	
     	// update row
     	int return_val = db.update(TABLE_SCHEDULES, values, KEY_SCHEDULE_ID + " = ?",
     			new String[] { String.valueOf(schedule.getId()) });
     	db.close();
     	return return_val;
     	
     }
     
     // Deleting a single landmark
     public void deleteLandmark(Landmark landmark) {
     	SQLiteDatabase db = this.getWritableDatabase();
     	db.delete(TABLE_LANDMARKS, KEY_LANDMARK_ID + " = ?",
     			new String[] { String.valueOf(landmark.getId()) } );
     	db.close();
     	
     }
     
     public void deleteTruck(Truck truck) {
     	SQLiteDatabase db = this.getWritableDatabase();
     	db.delete(TABLE_TRUCKS, KEY_TRUCK_ID + " = ?",
     			new String[] { String.valueOf(truck.getId()) } );
     	db.close();
     	
     }
     
     public void deleteSchedule(Schedule schedule) {
     	SQLiteDatabase db = this.getWritableDatabase();
     	db.delete(TABLE_SCHEDULES, KEY_SCHEDULE_ID + " = ?",
     			new String[] { String.valueOf(schedule.getId()) } );
     	db.close();
     	
     }
     
     // Get landmark count
     public int getLandmarksCount() {
     	String countQuery = "SELECT * FROM " + TABLE_LANDMARKS;
     	SQLiteDatabase db = this.getReadableDatabase();
     	Cursor cursor = db.rawQuery(countQuery, null);
     	cursor.close();
     	
     	// return count
     	db.close();
     	return cursor.getCount();
     	
     }
     
     public int getTrucksCount() {
     	String countQuery = "SELECT * FROM " + TABLE_TRUCKS;
     	SQLiteDatabase db = this.getReadableDatabase();
     	Cursor cursor = db.rawQuery(countQuery, null);
     	cursor.close();
     	
     	// return count
     	db.close();
     	return cursor.getCount();
     	
     }
     
     public int getSchedulesCount() {
     	String countQuery = "SELECT * FROM " + TABLE_SCHEDULES;
     	SQLiteDatabase db = this.getReadableDatabase();
     	Cursor cursor = db.rawQuery(countQuery, null);
     	cursor.close();
     	
     	// return count
     	db.close();
     	return cursor.getCount();
     	
     }
     
     private List<Landmark> getLandmarksListByQuery(String selectQuery) {
     	List<Landmark> landmarkList = new ArrayList<Landmark>();
     	
     	SQLiteDatabase db = this.getWritableDatabase();
     	Cursor cursor = db.rawQuery(selectQuery, null);
     	
     	// looping through all rows and adding to list
     	if (cursor.moveToFirst()) {
     		do {
     			Landmark landmark = new Landmark();
     			landmark.setId(Integer.parseInt(cursor.getString(0)));
     			landmark.setName(cursor.getString(1));
     			landmark.setXcoord(cursor.getString(2));
     			landmark.setYcoord(cursor.getString(3));
     			
     			// add back to the list
     			landmarkList.add(landmark);
     		} while (cursor.moveToNext());
     	}
     	
     	// return
     	db.close();
     	return landmarkList;
     }
     
     private List<Truck> getTrucksListByQuery(String selectQuery) {
     	List<Truck> truckList = new ArrayList<Truck>();
     	
     	// Select ALL Query
     	//String selectQuery = "SELECT * FROM " + TABLE_TRUCKS;
     	//String selectQuery = query;
     	
     	SQLiteDatabase db = this.getWritableDatabase();
     	Cursor cursor = db.rawQuery(selectQuery, null);
     	
     	// looping through all rows and adding to list
     	if (cursor.moveToFirst()) {
     		do {
     			Truck truck = new Truck();
     			truck.setId(Integer.parseInt(cursor.getString(0)));
     			truck.setName(cursor.getString(1));
     			truck.setCuisine(cursor.getString(2));
     			truck.setDescription(cursor.getString(3));
     			
     			// add back to the list
     			truckList.add(truck);
     		} while (cursor.moveToNext());
     	}
     	
     	// return
     	db.close();
     	return truckList;
     }
     
     private List<Schedule> getSchedulesListByQuery(String selectQuery) {
     	List<Schedule> scheduleList = new ArrayList<Schedule>();
     	
     	
     	SQLiteDatabase db = this.getWritableDatabase();
     	Cursor cursor = db.rawQuery(selectQuery, null);
     	
     	// looping through all rows and adding to list
     	if (cursor.moveToFirst()) {
     		do {
     			Schedule schedule = new Schedule();
     			schedule.setId(Integer.parseInt(cursor.getString(0)));
     			schedule.setDayOfWeek(cursor.getString(1));
     			schedule.setTimeOfDay(cursor.getString(2));
     			schedule.setTruckId(Integer.parseInt(cursor.getString(3)));
     			schedule.setLandmarkId(Integer.parseInt(cursor.getString(4)));
 
     			
     			// add back to the list
     			scheduleList.add(schedule);
     		} while (cursor.moveToNext());
     	}
     	
     	// return
     	db.close();
     	return scheduleList;
     }
     
     
     
    
  
 }
