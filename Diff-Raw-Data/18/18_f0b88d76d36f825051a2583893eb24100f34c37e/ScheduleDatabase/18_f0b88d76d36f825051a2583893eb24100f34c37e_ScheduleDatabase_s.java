 package utils;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import fi.vincit.babyschedule.R;
 
 public class ScheduleDatabase {
    private static final String DATABASE_CREATE_BABY_NAMES = "create table if not exists babynames (_id integer primary key autoincrement, babyname text not null );";
    private static final String DATABASE_CREATE_BABY_SCHEDULE = "(_id integer primary key autoincrement, babyname text not null, activityname text not null, time timestamp not null, duration int, freevalue int, freevalue2 int, freeText text, freeData image );";
    
    private static final int BABY_NAME_COLUMN = 1;
    private static final int ACTIVITY_NAME_COLUMN = 2;
    private static final int TIME_COLUMN = 3;
    private static final int DURATION_COLUMN = 4;
    private static final int FREEVALUE = 5;
    private static final int FREEVALUE2 = 6;
    private static final int FREETEXT = 7;
    private static final int FREEDATA = 8;
    
    private static final String DATABASE_NAME = "BabySchedule";
    private static final int DATABASE_VERSION = 13;
    private static final String TABLE_BABY_NAMES = "babynames";
       
    private static Context mCtx = null;
    private static DatabaseHelper mDbHelper = null;
    private static SQLiteDatabase mDb = null;   
    
    private static boolean mOpen = false;
    
 	public static void open(Context ctx) throws SQLException {		
 		if( !mOpen )
 		{
 			mCtx = ctx;		
 		    mDbHelper = new DatabaseHelper(mCtx);
 	    	mDb = mDbHelper.getWritableDatabase();
 	    	mOpen = true; 
 		}
 	}
 	
 	public static boolean isOpen() {
 		return mOpen;
 	}
 	
 	public static void close() {
 	    mDbHelper.close();
 	    mOpen = false;
 	}
    
 	private static class DatabaseHelper extends SQLiteOpenHelper {
 
         DatabaseHelper(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
         	// Create tables
         	Log.w("Babyschedule", "Creating database!!!");
         	db.execSQL(DATABASE_CREATE_BABY_NAMES);
         };
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
             Log.w("Babyschedule", "Upgrading database from version " + oldVersion + " to "
                     + newVersion);
             
             db.execSQL("DROP TABLE IF EXISTS Verneri");
             db.execSQL("DROP TABLE IF EXISTS testk");
             
 
             db.execSQL("DROP TABLE IF EXISTS babynames");
             db.execSQL(DATABASE_CREATE_BABY_NAMES);
         }
     }
 	   
 	public static ArrayList<String> getBabyNames() {
 		Log.w("Babyschedule", "getBabyNames()");
 		ArrayList<String> nameList = new ArrayList<String>();
     	Cursor names = mDb.query(TABLE_BABY_NAMES, new String[] {"_id", "babyname"},  null, null, null, null, null);
     	
     	if( names.moveToFirst() ) {
 	    	while( !names.isAfterLast() ) {
 	    		Log.w("Babyschedule", "getBabyNames(): " + names.getString(BABY_NAME_COLUMN));
 	    		nameList.add(names.getString(BABY_NAME_COLUMN));	    				
 	    		names.moveToNext();
 	    	}
     	}
     	names.close();
     	return nameList;
 	}
 	
 	public static void addNewBaby(String babyName) {
 		Log.w("Babyschedule", "Adding new baby: " + babyName);
 		Cursor nameCursor = getBabyNameCursor(babyName);
 		
 		if( nameCursor.getCount() == 0 ) {
 			ContentValues values = new ContentValues();
 			values.put("babyname", babyName);
 			mDb.insert(TABLE_BABY_NAMES, null, values);
 			mDb.execSQL("create table " + babyName + " " + DATABASE_CREATE_BABY_SCHEDULE);
 		}
 	}
 	
 	public static void removeBaby(String babyName) {
 		Log.w("Babyschedule", "Removing baby: " + babyName);		
 		Cursor nameCursor = getBabyNameCursor(babyName);
 		
 		if( nameCursor.getCount() > 0 ) {
 			int deleted = mDb.delete(TABLE_BABY_NAMES, "babyname = '" + babyName + "'", null);
 			Log.i("Babyschedule", "Deleted " + deleted + " rows from db.");
 			mDb.execSQL("DROP TABLE IF EXISTS " + babyName);
 		}
 		
 		nameCursor.close();
 	}
 	
 	private static Cursor getBabyNameCursor(String babyName) {
 		return mDb.query(TABLE_BABY_NAMES, new String[] {"_id", "babyname"}, 
 				"babyname = '" + babyName + "'", null, null, null, null);
 	}
 	
     public static long insertBabyAction(String babyName, String actionname, Date time) {
     	Log.i("Babyschedule", "creating baby schedule to with activity name: " + actionname);
     	ContentValues values = createContentValuesFor(babyName, actionname, time);
         return mDb.insert(babyName, null, values);
     }	
     
     public static long insertBabyActionWithDuration(String babyName, String actionname, Date time, int durationInSeconds) {
     	Log.i("Babyschedule", "creating baby schedule to with activity name: " + actionname + " and duration: " + durationInSeconds);
     	ContentValues values = createContentValuesFor(babyName, actionname, time);
         values.put("duration", durationInSeconds);
         
         return mDb.insert(babyName, null, values);
     }
     
     public static long insertBabyActionWithFreeVal(String babyName, String actionname, Date time, int freeValue) {
     	Log.i("Babyschedule", "creating baby schedule to with activity name: " + actionname + " and freeValue: " + freeValue);
     	ContentValues values = createContentValuesFor(babyName, actionname, time);
         values.put("freevalue", freeValue);
         
         return mDb.insert(babyName, null, values);
     }
     
     private static ContentValues createContentValuesFor(String babyName, String actionname, Date time) {
     	ContentValues values = new ContentValues();
         values.put("babyname", babyName);
         values.put("activityname", actionname);
         values.put("time", time.getTime());
         return values;
     }
     
     public static Cursor fetchEntireTable(String tableName){
     	return mDb.query(tableName, new String[] {"_id", "babyname", "activityname", "time", "duration", "freevalue"},  null, null, null, null, null);
     }   
     
     public static void removeFromDb(String tableName, Cursor cursor) {
     	int amountDeleted = mDb.delete(tableName, ""+cursor.getPosition(), null);
     	Log.i("Babyschedule", "Deleted " + amountDeleted + " rows from db.");
     }        
     
     public static void deleteEntryBasedOnDate(String babyName, Date date) {
    	long datems = date.getTime();
    	int deleted = mDb.delete(babyName, "time = '" + datems + "'", null);
   
    	Log.i("Babyschedule", "found " + deleted + " rows to be deleted in db.");						  
     }
     
     public static BabyEvent getEventBasedOnDateTime(String babyName, Date date) {
     	long datems = date.getTime();
     	BabyEvent event = null;
     	Cursor cursor = mDb.query(babyName, 
 				  new String[] {"_id", "babyname", "activityname", "time", "duration", "freevalue"}, 
 				  "time = '" + datems + "'", 
 				  null, null, null, null);
     	
     	if( cursor.moveToFirst() ) {
     		event = new BabyEvent(getRowActionName(cursor), getRowTime(cursor), getRowDuration(cursor), getRowFreeValue(cursor));
     	} 
     	
     	cursor.close();
     	return event;
     }
     
     public static int getFreeValueAttachedToEvent(String babyName, Date date) {
     	long datems = date.getTime();
     	int freeVal = 0;
     	Cursor cursor = mDb.query(babyName, 
 				  new String[] {"_id", "freeValue"}, 
 				  "time = '" + datems + "'", 
 				  null, null, null, null);
     	
     	if( cursor.moveToFirst() ) {
     		freeVal = cursor.getInt(1);
     	} 
     	
     	cursor.close();
     	return freeVal;
     }
     
     public static ArrayList<BabyEvent> getAllDbActionsSortedByDate(String babyName) {
     	Cursor everything = fetchEntireTable(babyName);
     	
     	ArrayList<BabyEvent> actions = new ArrayList<BabyEvent>();
     	if( everything.moveToFirst() ) {
 	    	while( !everything.isAfterLast() ) {
 	    		actions.add(new BabyEvent(getRowActionName(everything), getRowTime(everything), getRowDuration(everything), getRowFreeValue(everything)));
 	    		everything.moveToNext();
 	    	}
     	}
     	
     	Collections.sort(actions);
     	everything.close();
     	return actions;
     }
     
     public static ArrayList<BabyEvent> getAllDbActionsByActionName(String babyName, String actionName) {
     	Log.i("Babyschedule", "requesting actions for actionName " + actionName);
     	Cursor cursor = mDb.query(babyName, 
     							  new String[] {"_id", "babyname", "activityname", "time", "duration", "freevalue"}, 
     							  "activityname = '" + actionName + "'", 
     							  null, null, null, null);
     	
     	ArrayList<BabyEvent> actions = new ArrayList<BabyEvent>();
     	Log.i("Babyschedule", "found " + cursor.getCount() + " rows in db.");
     	
     	if( cursor.moveToFirst() ) {
 	    	while( !cursor.isAfterLast() ) {
 	    		actions.add(new BabyEvent(getRowActionName(cursor), getRowTime(cursor), getRowDuration(cursor), getRowFreeValue(cursor)));
 	    		cursor.moveToNext();
 	    	}
     	}
     	
     	Collections.sort(actions);
     	cursor.close();
     	return actions;
     }
     
     public static ArrayList<BabyEvent> getAllDbActionsFromNumberOfDaysAgo(String babyName, String actionName, long daysAgo) {
     	Log.i("Babyschedule", "requesting actions for actionName " + actionName + "from " + daysAgo + " days ago.");
     	Date tomorrowAtMidnight = new Date();
     	tomorrowAtMidnight.setHours(0);
     	tomorrowAtMidnight.setMinutes(0);
     	tomorrowAtMidnight.setSeconds(0);
     	tomorrowAtMidnight.setTime(tomorrowAtMidnight.getTime()+24*60*60*1000);
     	long tomorrowAtMidnightMs = tomorrowAtMidnight.getTime();
     	long upperLimitTimestamp = tomorrowAtMidnightMs - daysAgo*24*60*60*1000;
     	long lowerLimitTimeStamp = upperLimitTimestamp - 24*60*60*1000;
     	
     	String where = " activityname = '" + actionName + "' " +
 		" AND time > " + lowerLimitTimeStamp +
 		" AND time < " + upperLimitTimestamp;
     	Log.i("BabySchedule", "Query Where: " + where);
     	
     	Cursor cursor = mDb.query(babyName,
     			new String[] {"_id", "babyname", "activityname", "time", "duration", "freevalue"}, 
     			where, 
     			null, null, null, null);
     	
     	ArrayList<BabyEvent> actions = new ArrayList<BabyEvent>();
     	Log.i("Babyschedule", "found " + cursor.getCount() + " rows in db.");
     	
     	if( cursor.moveToFirst() ) {
 	    	while( !cursor.isAfterLast() ) {
 	    		actions.add(new BabyEvent(getRowActionName(cursor), getRowTime(cursor), getRowDuration(cursor), getRowFreeValue(cursor)));
 	    		cursor.moveToNext();
 	    	}
     	}
     	
     	Collections.sort(actions);
     	cursor.close();
     	return actions;
     }
     
     public static ArrayList<Date> getActionDatesForAction(String babyName, String activityName){
     	Log.i("Babyschedule", "requesting dates for activity " + activityName);
     	Cursor cursor = mDb.query(babyName, 
     							  new String[] {"_id", "babyname", "activityname", "time", "duration", "freevalue"}, 
     							  "activityname = '" + activityName + "'", 
     							  null, null, null, null);
     	
     	ArrayList<Date> dateList = new ArrayList<Date>();
     	Log.i("Babyschedule", "found " + cursor.getCount() + " rows in db.");
     	
     	if( cursor.moveToFirst() ) {
 	    	while( !cursor.isAfterLast() ) {
 	    		dateList.add(getRowTime(cursor));
 	    		cursor.moveToNext();
 	    	}
     	}
     	
     	Collections.sort(dateList);
     	cursor.close();
     	return dateList;
     }           
     
     public static Date getLastActionOfType(String babyName, String actionName){
     	ArrayList<Date> dates = getActionDatesForAction(babyName, actionName);
     	if( dates.size() > 0 ) {    		
     		return (Date) dates.get(dates.size()-1);
     	} else {
     		return null;
     	}
     }
     
     public static int getDurationOfNursingStartedAt(String babyName, Date nursingStarted) {
     	BabyEvent event = getEventBasedOnDateTime(babyName, nursingStarted);
     	return event.getDurationInSeconds();
     }
     
     public static ConsumedTime getDurationOfSleepStartedAt(String babyName, Date sleepStartTime) {
     	Date wakeUpDate = getWakeUpDateFromSleepDate(babyName, sleepStartTime);
     	if( wakeUpDate == null ) {
     		return null;
     		//wakeUpDate = new Date();
     	}
     	BabyEvent sleepOrNapEvent = getEventBasedOnDateTime(babyName, sleepStartTime);
 		assert(sleepOrNapEvent != null);
     	
 		return new ConsumedTime(wakeUpDate, sleepStartTime);
     }
     
     public static Date getWakeUpDateFromSleepDate(String babyname, Date sleepStartDate) {
     	ArrayList<Date> dates = getActionDatesForAction(babyname, mCtx.getString(R.string.woke_up));
     	
     	for( Date current : dates ) {
     		if( current.after(sleepStartDate) ) {
     			return current;
     		}
     	}
     	
     	return null;
     }
     
     private static Date getRowTime(Cursor cursor) {	
     	long timems = cursor.getLong(TIME_COLUMN);
     	Log.i("Babyschedule", "trying to parse row time with time string val: " + timems);
     	Date date = new Date();
     	date.setTime(timems);
     	return date;
     }    
     
     private static int getRowDuration(Cursor cursor) {
     	int duration = 0;
     	try {
     		duration = cursor.getInt(DURATION_COLUMN);
     	} catch(Exception e) {
     		// do nothing, just return 0 as default
     	}
     	return duration;
     }
     
     private static String getRowActionName(Cursor cursor) {
     	return cursor.getString(ACTIVITY_NAME_COLUMN);
     }
     
     private static int getRowFreeValue(Cursor cursor) {
     	int freeVal = 0;
     	try {
     		freeVal = cursor.getInt(FREEVALUE);
     	} catch(Exception e) {
     		// do nothing, just return 0 as default
     	}
     	return freeVal;
     }
 }
