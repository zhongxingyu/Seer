 package appEd.getDirectEd.database;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import appEd.getDirectEd.model.Facility;
 import appEd.getDirectEd.model.Activity;
 import appEd.getDirectEd.model.SubActivity;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 
 public class DatabaseHandler extends SQLiteOpenHelper {
 	// Database version
 	private static final int DATABASE_VERSION = 1;
 		
 	// Database name
 	private static final String DATABASE_NAME = "GetDirectED";
 	
 	// Table names	
 	public static final String FAC_TABLE = "Facilities_Table";
 	public static final String ACT_TABLE = "Activities_Table";
 	public static final String SUB_ACT_TABLE = "Sub_Activities_Table";
 	public static final String FAC_ACT_TABLE = "Facility_Activity_Table";
 	public static final String FAC_TYPE_TABLE = "Facility_Type";
 	public static final String SUPER_ACT_TABLE = "Fac_Super_Act_Table";
 
 	/*
 	 * Columns for Facilities_Table
 	 */
 	private static final String ID = "id";
 	private static final String FAC_NAME = "Facility_Name";
 	private static final String LAT = "Latitude";
 	private static final String LONG = "Longitude";
 	private static final String FAC_TYPE = "Facility_Type";
 	private static final String ADDRESS = "Address";
 	private static final String PHONE = "Phone_Number";
 	private static final String DESC = "Description";
 	private static final String IMAGE  = "Image";
 	
 	/*
 	 * Columns for Activities_Table
 	 */
 //	private static final String ID = "id";
 	private static final String ACT_NAME = "Activity_Name";
 	private static final String SUB_TYPE = "Sub_Activity";
 //	private static final String DESC = "Description";
 //	private static final String IMAGE  = "Image";
 	
 	/*
 	 * Columns for Sub_Activities_Table
 	 */
 //	private static final String ID = "id";
 	private static final String SUB_ACT_NAME = "Sub_Activity_Name";
 //	private static final String DESC = "Description";
 	private static final String SUPER_TYPE = "Super_Activity";
 	
 	/*
 	 * Columns for Facility_Type_Table
 	 */
 //	private static final String ID = "id";
 	private static final String FAC_TYPE_NAME = "Type_of_Facility";
 //	private static final String DESC = "Description";
 	
 	/*
 	 * Columns for Facility_Activity_Table
 	 */
 	private static final String F_ID = "Facility_ID";
 	private static final String A_ID = "Sub_Activity_ID";
 	private static final String S_HOURS = "Start_Time";
 	private static final String E_HOURS = "End_Time";
 	
 	/*
 	 * Columns for Facility_Super_Activity_Table
 	 */
 //	private static final String F_ID = "Facility_ID";
 	private static final String SUPER_ACT_ID = "Super_Activity_ID";
 //	private static final String DESC = "Description";
 	
 	//Private structures for holding and returning data between views
 	private ArrayList<Activity> activities = new ArrayList<Activity>();
 	private ArrayList<SubActivity> subActivities = new ArrayList<SubActivity>();
 	private ArrayList<Facility> facilities = new ArrayList<Facility>();
 	private ArrayList<String[]> hours = new ArrayList<String[]>();
 	
 	private Facility facility = new Facility();
 
 	// Constructor
 	/**
 	 * DatabaseHandler
 	 * 
 	 * Default constructor, sets up based on the super and removes the old DB whenever called
 	 * @param context
 	 */
 	public DatabaseHandler(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		//TODO Remove this and instead check to see if a DB file exists
 		context.deleteDatabase(DATABASE_NAME);
 	}//end of DatabaseHandler
 
 	/**
 	 * dropAllTables
 	 * 
 	 * Removes all the tables in the DB if they exist
 	 * @param db
 	 */
 	private static void dropAllTables(SQLiteDatabase db){
 		db.execSQL("DROP TABLE IF EXISTS " + FAC_TABLE + ";");
 		db.execSQL("DROP TABLE IF EXISTS " + ACT_TABLE + ";");
 		db.execSQL("DROP TABLE IF EXISTS " + SUB_ACT_TABLE + ";");
 		db.execSQL("DROP TABLE IF EXISTS " + FAC_ACT_TABLE + ";");
 		db.execSQL("DROP TABLE IF EXISTS " + FAC_TYPE_TABLE + ";");
 		db.execSQL("DROP TABLE IF EXISTS " + SUPER_ACT_TABLE + ";");		
 	}//end of dropAllTables
 	
 	/**
 	 * addFacilitiesTable
 	 * 
 	 * Adds the facilities table to the database
 	 * Table is empty and ready to be populated with data 
 	 * @param db
 	 */
 	private static void addFacilitiesTable(SQLiteDatabase db){
 		//+ KEY_ID + " Integer Primary key, "
 		String CREAT_TABLE_STATEMENT = 
 				"Create table "
 				+ FAC_TABLE
 				+ " ("
 				//+ "Key" + "Integer Primary key, "
 				+ ID + " Integer, "
 				+ FAC_NAME + " Text, "
 				+ LAT + " Real, "
 				+ LONG + " Real, "
 				+ FAC_TYPE + " Integer, "
 				+ ADDRESS + " Text, "
 				+ PHONE + " Text, "
 				+ DESC + " BLOB, "
 				+ IMAGE + " Text "
 				+ ");";
 		db.execSQL(CREAT_TABLE_STATEMENT);
 	}//end of allFacilitiesTable
 	
 	/**
 	 * addActivitiesTable
 	 * 
 	 * Adds the activities table to the database
 	 * Table is empty and ready to be populated with data 
 	 * @param db
 	 */
 	private static void addActivitiesTable(SQLiteDatabase db){
 		String CREAT_TABLE_STATEMENT = 
 				"Create table "
 				+ ACT_TABLE
 				+ " ("
 				+ ID + " Integer, "
 				+ ACT_NAME + " Text, "
 				+ SUB_TYPE + " Integer, "
 				+ DESC + " BLOB, "
 				+ IMAGE + " Text "
 				+ ");";
 		db.execSQL(CREAT_TABLE_STATEMENT);
 	}//end of addActivitiesTable
 	
 	/**
 	 * addSubActivities Table
 	 * 
 	 * Adds the sub-activities table to the database
 	 * Table is empty and ready to be populated with data 
 	 * @param db
 	 */
 	private static void addSubActivitiesTable(SQLiteDatabase db){
 		String CREAT_TABLE_STATEMENT = 
 				"Create table "
 				+ SUB_ACT_TABLE
 				+ " ("
 				+ ID + " Integer, "
 				+ SUB_ACT_NAME + " Text, "
 				+ DESC + " BLOB, "
 				+ SUPER_TYPE + " Integer "
 				+ ");";
 		db.execSQL(CREAT_TABLE_STATEMENT);
 	}//end of addSubActivitiesTable
 	
 	/**
 	 * addFacActTable
 	 * 
 	 * Adds the facilities-activities relation table to the database
 	 * Table is empty and ready to be populated with data 
 	 * @param db
 	 */
 	//TODO add the hours into this table to make those lookups quick and efficient
 	private static void addFacActTable(SQLiteDatabase db){
 		String CREAT_TABLE_STATEMENT = 
 				"Create table "
 				+ FAC_ACT_TABLE
 				+ " ("
 				+ F_ID + " Integer, "
 				+ A_ID + " Integer, "
 				+ S_HOURS + " Text, "
 				+ E_HOURS + " Text "
 				+ ");";
 		db.execSQL(CREAT_TABLE_STATEMENT);
 	}//end of addFacActTable
 	
 	/**
 	 * addFacTypeTable
 	 * 
 	 * Adds the facility type table to the database
 	 * Table is empty and ready to be populated with data 
 	 * @param db
 	 */
 	private static void addFacTypeTable(SQLiteDatabase db){
 		String CREAT_TABLE_STATEMENT = 
 				"Create table "
 				+ FAC_TYPE_TABLE
 				+ " ("
 				+ ID + " Integer, "
 				+ FAC_TYPE_NAME + " Text, "
 				+ DESC + " BLOB "
 				+ ");";
 		db.execSQL(CREAT_TABLE_STATEMENT);
 	}//end of addFacTypeTable
 	
 	/**
 	 * addSuperActTable
 	 * 
 	 * Adds the activities-sub-activities table to the database
 	 * Table is empty and ready to be populated with data 
 	 * @param db
 	 */
 	private static void addSuperActTable(SQLiteDatabase db){
 		String CREAT_TABLE_STATEMENT = 
 				"Create table "
 				+ SUPER_ACT_TABLE
 				+ " ("
 				+ F_ID + " Integer, "
 				+ SUPER_ACT_ID + " Integer, "
 				+ DESC + " BLOB "
 				+ ");";
 		db.execSQL(CREAT_TABLE_STATEMENT);
 	}//end of addSuperActTable
 	
 	/**
 	 * onCreate
 	 * 
 	 * This is an override of an abstract DB method
 	 * It creates all the tables in the database by the methods above
 	 * @param db
 	 */
 	@Override
 	public void onCreate(SQLiteDatabase db) {		
 		addFacilitiesTable(db);
 		addActivitiesTable(db);
 		addFacActTable(db);
 		addFacTypeTable(db);
 		addSubActivitiesTable(db);
 		addSuperActTable(db);
 	}//end of onCreate
 
 	/**
 	 * onUpgrade
 	 * 
 	 * This is an override of an abstract DB method
 	 * If the DB ever needs to be upgraded to a new version all the tables
 	 * are dropped and empty ones are created again
 	 * @param db
 	 * @param oldVersion
 	 * @param newVersion
 	 */
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		dropAllTables(db);
 		onCreate(db);
 	}//end of onUpgrade
 	
 	/**
 	 * Facility CRUD ops
 	 */
 	
 	/**
 	 * addAllFacilities
 	 * 
 	 * Takes a list of facilities from .csv files and places them into the DB
 	 * @param facs
 	 */
 	public void addAllFacilities(List<String[]> facs){
 		Facility fac = new Facility();
 		Iterator<String[]> iterator = facs.iterator();
 		
 		while(iterator.hasNext()){
 			String[] facList = iterator.next();
 	        fac.setId(new Integer(facList[0]));
 	        fac.setName(facList[1]);
 	        fac.setLatitude(new Float(facList[2]));
 	        fac.setLongitude(new Float(facList[3]));	
 	        fac.setFacType(new Integer(facList[5]));
 	        fac.setAddress(facList[6]);
 	        fac.setPhone("311");
 	        fac.setDescription(facList[7]);
 	        fac.setImage("Default_Image.jpg");
 	     
 	        addFacility(fac);
 		}//end of while
 	}//end of addAllFacilities
 	
 	/**
 	 * addFacility
 	 * 
 	 * Takes a populated facility object and places its information
 	 * into a tuple in the DB
 	 * @param fac
 	 */
 	public void addFacility(Facility fac){
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		
 		values.put(ID, fac.getId());
 		values.put(FAC_NAME, fac.getName());
 		values.put(LAT, fac.getLatitude());
 		values.put(LONG, fac.getLongitude());
 		values.put(FAC_TYPE, fac.getFacType());
 		values.put(ADDRESS, fac.getAddress());
 		values.put(PHONE, fac.getPhone());
 		values.put(DESC, fac.getDescription());
 		values.put(IMAGE, fac.getImage());
 		
 		db.insert(FAC_TABLE, null, values);
 		db.close();
 	}//end of addFacility
 	
 	/**
 	 * allAllFacilityTypes
 	 * 
 	 * Takes a list of facilities and their types and adds them to the DB
 	 * @param facTypes
 	 */
 	public void addAllFacilityTypes(List<String[]> facTypes){
 		Iterator<String[]> iterator = facTypes.iterator();
 		
 		while(iterator.hasNext()){
 			String[] entry = iterator.next();
 			addFacilityType(entry);
 		}//end of while
 	}//end of addAllFacilityTypes
 	
 	/**
 	 * allFacilityType
 	 * 
 	 * Takes the info for one facility and its type and added it to the DB
 	 * @param entry
 	 */
 	public void addFacilityType(String[] entry){
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		
 		values.put(ID, entry[0]);
 		values.put(FAC_TYPE_NAME, entry[1]);
 		values.put(DESC, entry[2]);
 		
 		db.insert(FAC_TYPE_TABLE, null, values);
 		db.close();
 	}//end of addFacilityType
 	
 	/**
 	 * getAllFacilities
 	 * 
 	 * Searches and returns all the facilities in the database
 	 * @return
 	 */
 	public ArrayList<Facility> getAllFacilities(){
 		SQLiteDatabase db = this.getReadableDatabase();
 		ArrayList<Facility> facs = new ArrayList<Facility>();
 		
 		String selectQuery = "Select * From " + FAC_TABLE + ";";
 		Cursor cursor = db.rawQuery(selectQuery, null);
 		
 		if(cursor != null){
 		cursor.moveToFirst();
 			while(cursor.isAfterLast() != true){
 				Facility fac = new Facility();
 
 				fac.setId(cursor.getInt(0));
 				fac.setName(cursor.getString(1));
 				fac.setLatitude(cursor.getFloat(2));
 				fac.setLongitude(cursor.getFloat(3));
 				fac.setFacType(cursor.getInt(4));
 				fac.setAddress(cursor.getString(5));
 				fac.setPhone(cursor.getString(6));
 				fac.setDescription(cursor.getString(7));
 				fac.setImage(cursor.getString(8));
 				facs.add(fac);
 				cursor.moveToNext();
 			}//end of while
 		}//end of if
 		return facs;
 	}//end of getAllFacilities
 	
 	/**
 	 * getAllFacilities
 	 * 
 	 * Searches and returns all the facilities in the database that
 	 * support the sibmitted activity
 	 * @param activity
 	 * @return
 	 */
 	public ArrayList<Facility> getAllFacilities(Activity activity){
 		SQLiteDatabase db = this.getReadableDatabase();
 		ArrayList<Facility> facs = new ArrayList<Facility>();
 		ArrayList<Long> facilityIds = new ArrayList<Long>();
 		Long activityId = activity.getId();
 				
 		String selectQuery = "Select * From " 
 							+ SUPER_ACT_TABLE
 							+ " Where "
 							+ SUPER_ACT_TABLE
 							+ "."
 							+ A_ID 
 							+ "=" 
 							+ activityId
 							+ ";";
 
 		Cursor cursor = db.rawQuery(selectQuery, null);
 		
 		if(cursor != null){
 		cursor.moveToFirst();
 			while(cursor.isAfterLast() != true){
 				System.out.println(cursor.getLong(0) +" "+ cursor.getLong(1));
 					facilityIds.add(cursor.getLong(0));
 					cursor.moveToNext();
 			}//end of while
 		}//end of if
 		cursor.close();
 		
 		Iterator<Long> iterator = facilityIds.iterator();
 		
 		while(iterator.hasNext()){
 			Long facId = iterator.next();
 			selectQuery = "Select * From " 
 							+ FAC_TABLE 
 							+ " Where "
 							+ FAC_TABLE
 							+ "."
 							+ ID 
 							+ "=" 
 							+ facId
 							+ ";";
 		
 			cursor = db.rawQuery(selectQuery, null);
 			
 			if(cursor != null){
 			cursor.moveToFirst();
 				while(cursor.isAfterLast() != true){
 					Facility fac = new Facility();
 					
 					fac.setId(cursor.getInt(0));
 					fac.setName(cursor.getString(1));
 					fac.setLatitude(cursor.getFloat(2));
 					fac.setLongitude(cursor.getFloat(3));
 					fac.setFacType(cursor.getInt(4));
 					fac.setAddress(cursor.getString(5));
 					fac.setPhone(cursor.getString(6));
 					fac.setDescription(cursor.getString(7));
 					fac.setImage(cursor.getString(8));
 					
 					facs.add(fac);
 					cursor.moveToNext();
 				}//end of while
 			}//end of if
 			cursor.close();
 		}//end of while
 		return facs;
 	}//end of getAllFacilities
 	
 	/**
 	 * getFacility
 	 * 
 	 * Searches and returns the facility with the matching ID
 	 * @param id
 	 * @return
 	 */
 	public void setFacility(long id){
 		SQLiteDatabase db = this.getReadableDatabase();
 		facility = new Facility();
 		
 		String selectQuery = "Select * From " + FAC_TABLE + " where id = " + id + ";";
 		Cursor cursor = db.rawQuery(selectQuery, null);
 	
 		if (cursor != null){
 			cursor.moveToFirst();
 			facility.setId(cursor.getInt(0));
 			facility.setName(cursor.getString(1));
 			facility.setLatitude(cursor.getFloat(2));
 			facility.setLongitude(cursor.getFloat(3));
 			facility.setFacType(cursor.getInt(4));
 			facility.setAddress(cursor.getString(5));
 			facility.setPhone(cursor.getString(6));
 			facility.setDescription(cursor.getString(7));
 			facility.setImage(cursor.getString(8));
 			}//end of if
 		
 		//TODO call method to populate hours
 		setHours(id);
 	}//end of getFacility
 
 	public void setHours(Long id){
 		
 	}
 	
 	public ArrayList<String[]> getHours(){
 		return hours;
 	}
 	
 	/**
 	 * End of Facility CRUD ops
 	 */
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/**
 	 * Activity CRUD ops
 	 */
 	
 	public void addAllActivities(List<String[]> acts){
 		Activity act = new Activity();
 		Iterator<String[]> iterator = acts.iterator();
 		
 		while(iterator.hasNext()){
 			String[] actList = iterator.next();
 			
 			act.setId(new Integer(actList[0]));
 			act.setName(actList[1]);
 			act.setSubType(new Integer(actList[2]));
 			act.setImage(actList[3]);
 			act.setDescription(actList[4]);
 	        
 	        addActivity(act);
 		}//end of while
 	}//end of addAllActivities
 	
 	public void addActivity(Activity act){
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		
 		values.put(ID, act.getId());
 		values.put(ACT_NAME, act.getName());
 		values.put(SUB_TYPE, act.getSubType());
 		values.put(DESC, act.getDescription());
 		values.put(IMAGE, act.getImage());
 		
 		db.insert(ACT_TABLE, null, values);
 		db.close();
 	}//end of addActivity
 	
 	public ArrayList<Activity> getAllActivities(){
 		SQLiteDatabase db = this.getReadableDatabase();
 		ArrayList<Activity> acts = new ArrayList<Activity>();
 		
 		String selectQuery = "Select * From " + ACT_TABLE + ";";
 		Cursor cursor = db.rawQuery(selectQuery, null);
 		
 		if(cursor != null){
 		cursor.moveToFirst();
 			while(cursor.isAfterLast() != true){
 				Activity act = new Activity();
 				act.setId(cursor.getInt(0));
 				act.setName(cursor.getString(1));
 				act.setSubType(cursor.getInt(2));
 				act.setDescription(cursor.getString(3));
 				act.setImage(cursor.getString(4));
 				acts.add(act);
 				cursor.moveToNext();
 			}//end of while
 		}//end of if
 		return acts;
 	}//end of getAllActivities
 	
 	public ArrayList<Activity> getAllActivities(Facility facility){
 		SQLiteDatabase db = this.getReadableDatabase();
 		ArrayList<Activity> acts = new ArrayList<Activity>();
 		ArrayList<Long> activityIds = new ArrayList<Long>();
 		Long facId = facility.getId();
 		
 		String selectQuery = "Select * From " 
 								+ SUPER_ACT_TABLE 
 								+ " Where "
 								+ SUPER_ACT_TABLE
 								+ "."
 								+ F_ID 
 								+ "=" 
 								+ facId
 								+ ";";
 
 		Cursor cursor = db.rawQuery(selectQuery, null);
 		
 		if(cursor != null){
 		cursor.moveToFirst();
 			while(cursor.isAfterLast() != true){
 				System.out.println(cursor.getLong(0) +" "+ cursor.getLong(1));
 				activityIds.add(cursor.getLong(1));
 				cursor.moveToNext();
 			}//end of while
 		}//end of if
 		cursor.close();
 
 		Iterator<Long> iterator = activityIds.iterator();
 		
 		while(iterator.hasNext()){
 			Long actID = iterator.next();
 			selectQuery = "Select * From " 
 							+ ACT_TABLE 
 							+ " Where "
 							+ ACT_TABLE
 							+ "."
 							+ ID 
 							+ "=" 
 							+ actID
 							+ ";";
 			
 			cursor = db.rawQuery(selectQuery, null);
 			
 			if(cursor != null){
 			cursor.moveToFirst();
 				while(cursor.isAfterLast() != true){
 					Activity act = new Activity();
 					act.setId(cursor.getInt(0));
 					System.out.println(" *** " + cursor.getString(1));
 					act.setName(cursor.getString(1));
 					act.setSubType(cursor.getInt(2));
 					act.setDescription(cursor.getString(3));
 					act.setImage(cursor.getString(4));
 					acts.add(act);
 					cursor.moveToNext();
 				}//end of while
 			}//end of if
 			cursor.close();
 		}//end of while
 		return acts;
 	}//end of getAllActivities
 	
 	/**
 	 * End of Activity CRUD ops
 	 */
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/**
 	 * SubActivity CRUD ops
 	 */
 	
 	public void addAllSubActivities(List<String[]> subActs){
 		SubActivity subAct = new SubActivity();
 		Iterator<String[]> iterator = subActs.iterator();
 		
 		while(iterator.hasNext()){
 			String[] actList = iterator.next();
 			
 			subAct.setId(new Integer(actList[0]));
 			subAct.setName(actList[1]);
 			subAct.setSuperType(new Integer(actList[2]));
 			subAct.setDescription(actList[3]);
 
 	        addSubActivity(subAct);
 		}//end of while
 	}//end of addAllSubActivities
 	
 	public void addSubActivity(SubActivity subAct){
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		
 		values.put(ID, subAct.getId());
 		values.put(SUB_ACT_NAME, subAct.getName());
 		values.put(SUPER_TYPE, subAct.getSuperType());
 		values.put(DESC, subAct.getDescription());
 		
 		db.insert(SUB_ACT_TABLE, null, values);
 		db.close();
 	}//end of addSubActivity
 	
 	public ArrayList<SubActivity> getAllSubActivities(){
 		SQLiteDatabase db = this.getReadableDatabase();
 		ArrayList<SubActivity> subActs = new ArrayList<SubActivity>();
 		SubActivity subAct = new SubActivity();
 		
 		String selectQuery = "Select * From " + SUB_ACT_TABLE + ";";
 		Cursor cursor = db.rawQuery(selectQuery, null);
 		
 		if(cursor != null){
 		cursor.moveToFirst();
 			while(cursor.isAfterLast() != true){
 				subAct.setId(cursor.getInt(0));
 				subAct.setName(cursor.getString(1));
 				subAct.setSuperType(cursor.getInt(2));
 				subAct.setDescription(cursor.getString(3));
 				subActs.add(subAct);
 				cursor.moveToNext();
 			}//end of while
 		}//end of if
 		return subActs;
 	}//end of getAllSubActivities
 	
 	public ArrayList<SubActivity> getAllSubActivities(Activity activity){
 		SQLiteDatabase db = this.getReadableDatabase();
 		ArrayList<SubActivity> subActs = new ArrayList<SubActivity>();
 		SubActivity subAct = new SubActivity();
 		Long actId = activity.getId();
 		
 		String selectQuery = "Select * From " 
 								+ SUB_ACT_TABLE 
 								+ " Where "
 								+ SUB_ACT_TABLE
 								+ "."
 								+ SUPER_TYPE
 								+ "="
 								+ actId
 								+ ";";
 		
 		Cursor cursor = db.rawQuery(selectQuery, null);
 		
 		if(cursor != null){
 		cursor.moveToFirst();
 			while(cursor.isAfterLast() != true){
 				subAct.setId(cursor.getInt(0));
 				subAct.setName(cursor.getString(1));
 				subAct.setSuperType(cursor.getInt(2));
 				subAct.setDescription(cursor.getString(3));
 				subActs.add(subAct);
 				cursor.moveToNext();
 			}//end of while
 		}//end of if
 		return subActs;
 	}//end of getAllSubActivities
 	
 	/**
 	 * Hours CRUD ops
 	 */
 	
 	public Integer getFacilityID(String facilityName){
 		SQLiteDatabase db = this.getReadableDatabase();
 		Integer id = 0;
 		
 		String selectQuery = "Select " 
 								+ ID 
 								+ " From " 
 								+ FAC_TABLE + " Where Facility_Name = "
 								+ " \""
 								+ facilityName
 								+ "\";";
 		
 		Cursor cursor = db.rawQuery(selectQuery, null);
 		
 		if(cursor != null){
 			cursor.moveToFirst();
 			while(cursor.isAfterLast() != true){
 				id = cursor.getInt(0);
 				cursor.moveToNext();
 			}//end of while
 		}//end of if
 		cursor.close();
 		return id;
 	}//end of getFacilityID
 	
 	public Integer getActivityID(String activityName){
 		SQLiteDatabase db = this.getReadableDatabase();
 		Integer id = 0;
 		
 		if(activityName == ""){
 			return id = 0;
 		}
 		
 		String selectQuery = "Select " 
 								+ ID 
 								+ " From " 
 								+ SUB_ACT_TABLE + " Where Sub_Activity_Name = "
 								+ " \""
 								+ activityName
 								+ "\";";
 		
 		Cursor cursor = db.rawQuery(selectQuery, null);
 		
 		if(cursor != null){
 			cursor.moveToFirst();
 			while(cursor.isAfterLast() != true){
 				id = cursor.getInt(0);
 				cursor.moveToNext();
 			}//end of while
 		}//end of if
 		cursor.close();
 		return id;
 	}//end of getActivtyID
 	
 	public void addAllActivityHours(List<String[]> hours){
 		Iterator<String[]> iterator = hours.iterator();
 		Integer f_id = null;
 		Integer a_id = null;
 		String startHours = null;
 		String endHours = null;
 		Integer i = 0;
 		
 		while(iterator.hasNext()){
 			String[] fields = iterator.next();
 
 			f_id = getFacilityID(fields[0]);
 			a_id = getActivityID(fields[2]);
 			startHours = fields[3];
 			endHours = fields[4];
 
 			addActivityHours(f_id, a_id, startHours, endHours);
 			i++;
 		}//end of while
 	}//end of addAllActivityHours
 	
 	public void addActivityHours(Integer f_id, Integer a_id, String start, String end){
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 				
 		values.put(F_ID, f_id);
 		values.put(A_ID, a_id);
 		values.put(S_HOURS, start);
 		values.put(E_HOURS, end);
 		
 		db.insert(FAC_ACT_TABLE, null, values);
 		db.close();
 	}//end of addActivityHours
 	
 	public ArrayList<Integer> getAllProvidingFacilities(){
 		SQLiteDatabase db = this.getReadableDatabase();
 		ArrayList<Integer> listOfFacs = new ArrayList<Integer>();
 		
 		String selectQuery = "Select Distinct Facility_ID From " + FAC_ACT_TABLE + ";";
 		Cursor cursor = db.rawQuery(selectQuery, null);
 		
 		if(cursor != null){
 		cursor.moveToFirst();
 			while(cursor.isAfterLast() != true){
 				listOfFacs.add(cursor.getInt(0));
 				cursor.moveToNext();
 			}//end of while
 		}//end of if
 		return listOfFacs;
 	}//end of getAllProvidingFacilities
 	
 	/**
 	 * Adding relation table for facilities and activities 
 	 */
 	
 	public void addAllRelations(List<String[]> rels){
 		Iterator<String[]> iterator = rels.iterator();
 		
 		System.out.println("In AddAllRelations");
 		while(iterator.hasNext()){
 			String[] relEntry = iterator.next();
 	        addRelation(relEntry);
 		}//end of while
 	}//end of addAllRelations
 	
 	public void addRelation(String[] relEntry){
 		System.out.println("In AddRelation");
 		
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		
 		values.put(F_ID, relEntry[0]);
 		values.put(SUPER_ACT_ID, relEntry[1]);
 
 		db.insert(SUPER_ACT_TABLE, null, values);
 		db.close();
 	}//end of addRelation
 	
 	/**
 	 * SQL Ops
 	 */
 	
 	public void databaseSetUp(Context context){
         ReadFileManager rfm = new ReadFileManager();
         
         int facFile = appEd.getDirectEd.main.R.raw.facilities;
         List<String[]> facList = null;
         int ActHoursFile = appEd.getDirectEd.main.R.raw.rec_hours;
         List<String[]> actHList = null;
         int ActFile = appEd.getDirectEd.main.R.raw.activities;
         List<String[]> actList = null;
         int SubActFile = appEd.getDirectEd.main.R.raw.sub_activities;
         List<String[]> subActList = null;
         int facSuperAct = appEd.getDirectEd.main.R.raw.faci_super_act;
         List<String[]> superActList = null;
         int facType = appEd.getDirectEd.main.R.raw.fac_type;
         List<String[]> facTypeList = null;
         
         System.out.println("*** Starting Read File test ***");
         
         facList = rfm.ReadFile(facFile, context, 8);
         actList = rfm.ReadFile(ActFile, context, 5);
         subActList = rfm.ReadFile(SubActFile, context, 5);
         actHList = rfm.ReadFile(ActHoursFile, context, 7);
         superActList = rfm.ReadFile(facSuperAct, context, 2);
         facTypeList = rfm.ReadFile(facType, context, 3);
         
         System.out.println("*** Ending Read File test ***");
         
         addAllFacilities(facList);
         addAllActivities(actList);
         addAllSubActivities(subActList);
         //addAllActivityHours(actHList);
         addAllRelations(superActList);
         addAllFacilityTypes(facTypeList);
         System.out.println("ALL DONE"); 
 	}//end of databaseSetUp
 	
 	/*
 	 * Facilities
 	 */
 	//Get all facilities in the DB
 	public void setFacilities(){
 		facilities = getAllFacilities();
 	}//end of setFacilities
 
 	//Get all the facilities that support the inputed activity
 	public void setFacilities(Activity activity){
 		//TODO finish implementation by completing query
 		facilities = getAllFacilities(activity);
 	}//end of setFacilities
 
 	//Get all the facilities that support the inputed activity
 	public void setFacilities(SubActivity activity){
 		//TODO: create a getAll Facilities for SubActivities
 		//facilities = getAllFacilities(activity);
 		facilities = new ArrayList<Facility>();
 	}//end of setFacilities
 	
 	//return the list of facilities that was populated by one of the above
 	public ArrayList<Facility> getFacilities(){
 		return facilities;
 	}//end of setFacilities
 	
 	public Facility getFacility() {
 		return facility;
 	}
 	
 	/*
 	 * Activities 
 	 */
 	//Get all the activities offered by facilities in edmonton
 	public void setActivities(){
 		activities = getAllActivities();
 	}//end of getActivities
 	
 	//Get all the activities that a facility has to offer
 	public void setActivities(Facility facility){
 		//TODO finish query with fac_id and fac_act table 
 		activities = getAllActivities(facility);
 	}//end of getActivities
 	
 	//return the list of activities that was populated by one of the above
 	public ArrayList<Activity> getActivities(){
 		return activities;
 	}//end of getActivities
 	
 	/*
 	 * SubActivities
 	 */
 	//Gets all the sub activities that the activity has
 	public void setSubActivities(Activity activity){
		subActivities = getAllSubActivities(activity);
 	}//end of getSubActivities
 	
 	//return the list of sub activities that was populated by one of the above
 	public ArrayList<SubActivity> getSubActivities(){
 		return subActivities;
 	}//end of getSubActivities
 }//end of class
