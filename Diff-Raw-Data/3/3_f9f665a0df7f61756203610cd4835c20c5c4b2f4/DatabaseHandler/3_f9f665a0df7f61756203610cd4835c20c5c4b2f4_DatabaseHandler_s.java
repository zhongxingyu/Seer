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
 
 	
 	// Columns for Facilities_Table
 	private static final String ID = "id";
 	private static final String FAC_NAME = "Facility_Name";
 	private static final String LAT = "Latitude";
 	private static final String LONG = "Longitude";
 	private static final String FAC_TYPE = "Facility_Type";
 	private static final String ADDRESS = "Address";
 	private static final String PHONE = "Phone_Number";
 	private static final String DESC = "Description";
 	private static final String IMAGE  = "Image";
 	
 	// Columns for Activities_Table
 	//ID
 	private static final String ACT_NAME = "Activity_Name";
 	private static final String SUB_TYPE = "Sub_Activity";
 	//Description
 	//image
 	
 	//Columns for Sub_Activities_Table
 	//ID
 	private static final String SUB_ACT_NAME = "Sub_Activity_Name";
 	//description
 	private static final String SUPER_TYPE = "Super_Activity";
 	
 	//Columns for Facility_Type_Table
 	//ID
 	private static final String FAC_TYPE_NAME = "Type_of_Facility";
 	//desc
 	
 	//Columns for Facility_Activity_Table
 	private static final String F_ID = "Facility_ID";
 	private static final String A_ID = "Sub_Activity_ID";
 	private static final String S_HOURS = "Start_Time";
 	private static final String E_HOURS = "End_Time";
 	
 	//Columns for Facility_Super_Activity_Table
 	//F_ID
 	private static final String SUPER_ACT_ID = "Super_Activity_ID";
 	//DESC
 	
 	//Private structures
 	private ArrayList<Activity> activities = new ArrayList<Activity>();
 	private ArrayList<SubActivity> subActivities = new ArrayList<SubActivity>();
 	private ArrayList<Facility> facilities = new ArrayList<Facility>();
 
 	// Constructor
 	public DatabaseHandler(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		context.deleteDatabase(DATABASE_NAME);
 	}
 
 	private static void dropAllTables(SQLiteDatabase db){
 		db.execSQL("DROP TABLE IF EXISTS " + FAC_TABLE);
 		db.execSQL("DROP TABLE IF EXISTS " + ACT_TABLE);
 		db.execSQL("DROP TABLE IF EXISTS " + SUB_ACT_TABLE);
 		db.execSQL("DROP TABLE IF EXISTS " + FAC_ACT_TABLE);
 		db.execSQL("DROP TABLE IF EXISTS " + FAC_TYPE_TABLE);
 		db.execSQL("DROP TABLE IF EXISTS " + SUPER_ACT_TABLE);		
 	}
 	
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
 				+ ")";
 		db.execSQL(CREAT_TABLE_STATEMENT);
 	}
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
 				+ ")";
 		db.execSQL(CREAT_TABLE_STATEMENT);
 	}
 	private static void addSubActivitiesTable(SQLiteDatabase db){
 		String CREAT_TABLE_STATEMENT = 
 				"Create table "
 				+ SUB_ACT_TABLE
 				+ " ("
 				+ ID + " Integer, "
 				+ SUB_ACT_NAME + " Text, "
 				+ DESC + " BLOB, "
 				+ SUPER_TYPE + " Integer "
 				+ ")";
 		db.execSQL(CREAT_TABLE_STATEMENT);
 	}
 	private static void addFacActTable(SQLiteDatabase db){
 		String CREAT_TABLE_STATEMENT = 
 				"Create table "
 				+ FAC_ACT_TABLE
 				+ " ("
 				+ F_ID + " Integer, "
 				+ A_ID + " Integer, "
 				+ S_HOURS + " Text, "
 				+ E_HOURS + " Text "
 				+ ")";
 		db.execSQL(CREAT_TABLE_STATEMENT);
 	}
 	private static void addFacTypeTable(SQLiteDatabase db){
 		String CREAT_TABLE_STATEMENT = 
 				"Create table "
 				+ FAC_TYPE_TABLE
 				+ " ("
 				+ ID + " Integer, "
 				+ FAC_TYPE_NAME + " Text, "
 				+ DESC + " BLOB "
 				+ ")";
 		db.execSQL(CREAT_TABLE_STATEMENT);
 	}
 	private static void addSuperActTable(SQLiteDatabase db){
 		String CREAT_TABLE_STATEMENT = 
 				"Create table "
 				+ SUPER_ACT_TABLE
 				+ " ("
 				+ F_ID + " Integer, "
 				+ SUPER_ACT_ID + " Integer, "
 				+ DESC + " BLOB "
 				+ ")";
 		db.execSQL(CREAT_TABLE_STATEMENT);
 	}
 	
 
 	
 	// Create tables
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		// TODO Auto-generated method stub
 		System.out.println("******** DO I EVER GET CALLED ***********");
 		//dropAllTables(db);
 		
 		addFacilitiesTable(db);
 		addActivitiesTable(db);
 		addFacActTable(db);
 		addFacTypeTable(db);
 		addSubActivitiesTable(db);
 		addSuperActTable(db);
 	}
 
 	// Upgrading database
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		dropAllTables(db);
 		// Create table again
 		onCreate(db);
 	}
 	
 	/**
 	 * CRUD operations
 	 */
 	
 	public Cursor getCursorToAllTuples(String table_name){
 		String selectQuery = "Select * From " + table_name;
 		
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(selectQuery, null);
 		
 		return cursor;
 	}
 	
 	public void removeAllTuples(String table_name){
 	SQLiteDatabase db = this.getReadableDatabase();
 	db.delete(table_name, null, null);
 	}
 	
 	/**
 	 * Facility CRUD ops
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
 		}
 	}
 	
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
 	}
 	
 	public ArrayList<Facility> getAllFacilities(){
 		SQLiteDatabase db = this.getReadableDatabase();
 		ArrayList<Facility> facs = new ArrayList<Facility>();
 		Facility fac = new Facility();
 		
 		String selectQuery = "Select * From " + FAC_TABLE;
 		Cursor cursor = db.rawQuery(selectQuery, null);
 		
 		if(cursor != null){
 		cursor.moveToFirst();
 			while(cursor.isAfterLast() != true){
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
 	}
 	
 	public Facility getFacility(int id){
 	SQLiteDatabase db = this.getReadableDatabase();
 	Facility fac = new Facility();
 	
 	String selectQuery = "Select * From " + FAC_TABLE + " where id = " + id;
 	Cursor cursor = db.rawQuery(selectQuery, null);
 
 	if (cursor != null){
 		cursor.moveToFirst();
 		fac.setId(cursor.getInt(0));
 		fac.setName(cursor.getString(1));
 		fac.setLatitude(cursor.getFloat(2));
 		fac.setLongitude(cursor.getFloat(3));
 		fac.setFacType(cursor.getInt(4));
 		fac.setAddress(cursor.getString(5));
 		fac.setPhone(cursor.getString(6));
 		fac.setDescription(cursor.getString(7));
 		fac.setImage(cursor.getString(8));
 		}//end of if
 	return fac;
 	}
 
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
 		}
 	}
 	
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
 	}
 	
 	public ArrayList<Activity> getAllActivities(){
 		SQLiteDatabase db = this.getReadableDatabase();
 		ArrayList<Activity> acts = new ArrayList<Activity>();
		Activity act = new Activity();
 		
 		String selectQuery = "Select * From " + ACT_TABLE;
 		Cursor cursor = db.rawQuery(selectQuery, null);
 		
 		if(cursor != null){
 		cursor.moveToFirst();
 			while(cursor.isAfterLast() != true){
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
 	}
 	
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
 		}
 	}
 	
 	public void addSubActivity(SubActivity subAct){
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		
 		values.put(ID, subAct.getId());
 		values.put(SUB_ACT_NAME, subAct.getName());
 		values.put(SUPER_TYPE, subAct.getSuperType());
 		values.put(DESC, subAct.getDescription());
 		
 		db.insert(SUB_ACT_TABLE, null, values);
 		db.close();
 	}
 	
 	public ArrayList<SubActivity> getAllSubActivities(){
 		SQLiteDatabase db = this.getReadableDatabase();
 		ArrayList<SubActivity> subActs = new ArrayList<SubActivity>();
 		SubActivity subAct = new SubActivity();
 		
 		String selectQuery = "Select * From " + SUB_ACT_TABLE;
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
 	}
 	
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
 								+ "\"";
 		
 		Cursor cursor = db.rawQuery(selectQuery, null);
 		
 		if(cursor != null){
 			cursor.moveToFirst();
 			while(cursor.isAfterLast() != true){
 				id = cursor.getInt(0);
 				cursor.moveToNext();
 			}
 		}
 		cursor.close();
 		return id;
 	}
 	
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
 								+ "\"";
 		
 		Cursor cursor = db.rawQuery(selectQuery, null);
 		
 		if(cursor != null){
 			cursor.moveToFirst();
 			while(cursor.isAfterLast() != true){
 				id = cursor.getInt(0);
 				cursor.moveToNext();
 			}
 		}
 		cursor.close();
 		return id;
 	}
 	
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
 		}
 	}
 	
 	public void addActivityHours(Integer f_id, Integer a_id, String start, String end){
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 				
 		values.put(F_ID, f_id);
 		values.put(A_ID, a_id);
 		values.put(S_HOURS, start);
 		values.put(E_HOURS, end);
 		
 		db.insert(FAC_ACT_TABLE, null, values);
 		db.close();
 	}
 	
 	public ArrayList<Integer> getAllProvidingFacilities(){
 		SQLiteDatabase db = this.getReadableDatabase();
 		ArrayList<Integer> listOfFacs = new ArrayList<Integer>();
 		
 		String selectQuery = "Select Distinct Facility_ID From " + FAC_ACT_TABLE;
 		Cursor cursor = db.rawQuery(selectQuery, null);
 		
 		if(cursor != null){
 		cursor.moveToFirst();
 			while(cursor.isAfterLast() != true){
 				listOfFacs.add(cursor.getInt(0));
 				cursor.moveToNext();
 			}//end of while
 		}//end of if
 		return listOfFacs;
 	}
 	
 	/**
 	 * SQL Ops
 	
 	/**
 	 * Activity queries 
 	 */
 	//Get all the activities in the database
 	public void getActivities(){
 		activities = getAllActivities();
 	}
 
 	//get all the activities that a particular facility offers
 	public void getActivities(Facility facility){
 
 	}
 	
 	/**
 	 * Sub_Activity queries
 	 */
 	//get all the sub activities for the inputed database
 	public void getSubActivities(Activity activity){
 		subActivities = getAllSubActivities();
 	}
 	
 	/**
 	 * Facility queries
 	 */
 	//Get all the facilities in the database
 	public void getFacilities(){
 		facilities = getAllFacilities();
 	}
 	//Get all facilities that support a particular activity
 	public void getFacilities(Activity activity){
 
 	}
 	//Get all facilities that support a particular activity
 	public void getFacilities(SubActivity subActivity){
 
 	}
 	
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
         
         facList = rfm.ReadFacFile(facFile, context);
         actList = rfm.ReadActFile(ActFile, context);
         subActList = rfm.ReadSubActFile(SubActFile, context);
         actHList = rfm.ReadActHoursFile(ActHoursFile, context);
         superActList = rfm.ReadFacSuperActFile(facSuperAct, context);
         
         //addAllFacilities(facList);
         addAllActivities(actList);
         //addAllSubActivities(subActList);
         //addAllActivityHours(actHList);
         System.out.println("ALL DONE"); 
 	}
 	
 }//end of class
