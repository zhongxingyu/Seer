 package com.thebitisland.campamentosdiaper.auxClasses;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 import android.preference.PreferenceManager;
 import android.util.FloatMath;
 import android.util.Log;
 
 public class DBManager {
 	/**
 	 * Fields
 	 */
 	// ---------- Database params and declarations ----------
 	// DB name and tables
 	private static final String DATABASE_NAME = "CampsDB.db";
 	private static final String DATABASE_USER_TABLE = "userTable";
 	private static final String DATABASE_ACTIVITY_TABLE = "activityTable";
 	private static final int DATABASE_VERSION = 1;
 	// Class declaration
 	private SQLiteDatabase ourDB;
 	private DbHelper ourHelper;
 	private final Context ourContext;
 
 	// ---------- Table field declarations ----------
 	// Table 1 - Users (Stores all USEFUL users and their details)
 	public static final String KEY_U_RACEID = "_id";
 	public static final String KEY_U_UNAME = "username";
 	public static final String KEY_U_FNAME = "first_name";
 	public static final String KEY_U_LNAME = "last_name";
 	public static final String KEY_U_PHOTO = "photo";
 	public static final String KEY_U_EMAIL = "email";
 	public static final String KEY_U_PHONE = "phone";
 	
 	// Table 2 - Camp activities (Stores all USEFUL activities and their details)
 	public static final String KEY_A_RACEID = "_id";
 	public static final String KEY_A_NAME = "name";
 	public static final String KEY_A_URL = "file_url";
 	public static final String KEY_A_PICTURE = "photo";
 
 	
 
 
 	/**
 	 * DbHelper class, creates a simple way to add and remove data from the
 	 * database.
 	 */
 	private static class DbHelper extends SQLiteOpenHelper {
 
 		public DbHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 			// TODO Auto-generated constructor stub
 		}
 		/**
 		 * onCreate functions creates both tables.
 		 */
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			// TODO Auto-generated method stub
 			//db.execSQL("DROP TABLE IF EXISTS " + DATABASE_SESSION_TABLE);
 			db.execSQL("CREATE TABLE " + DATABASE_USER_TABLE + " ("
 					+ KEY_U_RACEID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
 					+ KEY_U_UNAME + " VARCHAR,"
 					+ KEY_U_FNAME + " VARCHAR," 
 					+ KEY_U_LNAME + " VARCHAR, " 
 					+ KEY_U_PHOTO + " TEXT," 
 					+ KEY_U_EMAIL + " VARCHAR," 
 					+ KEY_U_PHONE + " VARCHAR"
 					+ ");");
 			db.execSQL("CREATE TABLE "+ DATABASE_ACTIVITY_TABLE + " ("
 					+ KEY_A_RACEID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
 					+ KEY_A_NAME + " TEXT, "
 					+ KEY_A_URL + " TEXT, "
					+ KEY_A_PICTURE + " TEXT" 
					+ ");");
 
 		}
 
 		/**
 		 * OnUpgrade function, set to delete old tables on upgrade.
 		 * To be modified further.
 		 */
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			// TODO Auto-generated method stub
 			//db.execSQL("DROP TABLE IF EXISTS " + DATABASE_USER_TABLE);
 			//onCreate(db);
 		}
 	}
 
 	/**
 	 * Basic constructor.
 	 * @param c: Context to be initialized
 	 */
 	public DBManager(Context c) {
 		ourContext = c;
 	}
 
 	/**
 	 * Open function. Initializes the Database based on the context init'd
 	 * in the constructor.
 	 * @return The initialized DB class
 	 * @throws SQLException
 	 */
 	public DBManager open() throws SQLException {
 		ourHelper = new DbHelper(ourContext);
 		ourDB = ourHelper.getWritableDatabase();
 		return this;
 	}
 
 	/**
 	 * Close function. No further comment necessary.
 	 */
 	public void close() {
 		ourHelper.close();
 	}
 	
 	/**
 	 * Check username. Given a string, the method searches it in the 'username' column
 	 * of the database. 
 	 * @return True if the user exists in the table. False otherwise.
 	 * @param The username String representation
 	 */
 	public int checkUsername(String username) {
 		String query = "SELECT "+ KEY_U_RACEID + " FROM "+ DATABASE_USER_TABLE + " WHERE username= " + "\""+username+"\"";
 		Cursor cursor = ourDB.rawQuery(query, null);
 		
 		if(cursor.moveToFirst())
 			return cursor.getInt(0);
 		
 		return -1;
 	}
 	
 	/**
 	 * Get username first and last name. Given a username, the method retrieves his/her 
 	 * first name and last name.
 	 * @return The first and last name of the user in a String array of length 2.
 	 * @param The username String representation
 	 */
 	public String[] getFullname(String username) {
 		
 		String[] fullname = new String[2];
 		String query = "SELECT " + KEY_U_FNAME + ", " + KEY_U_LNAME + " FROM " + DATABASE_USER_TABLE + " WHERE username= " + "\""+username+"\""; 
 		Cursor cursor  = ourDB.rawQuery(query, null);
 		if(cursor.moveToFirst()) {
 			fullname[0] = cursor.getString(0);
 			fullname[1] = cursor.getString(1);
 		}
 		
 		cursor.close();
 		
 		return fullname;
 	}
 	
 	/**
 	 * Get birtdate. Retrieve's a user's birthdate in the format dd/mm/yyyy.
 	 * @param The username
 	 * @return The user's birthdate String represenation
 	 */
 	/*public String getBirthdate(String username) {
 		
 		int date = 0;
 		String stringDate = "";
 		String query = "SELECT " + KEY_U_BORN + " FROM " + DATABASE_USER_TABLE + " WHERE username= " + "\""+username+"\"";
 		Cursor cursor = ourDB.rawQuery(query, null);
 		
 		if(cursor.moveToFirst()) {
 			date = cursor.getInt(0);
 			stringDate = convertIntToStringDate(date);
 		}
 		
 		cursor.close();
 		return stringDate;
 	}*/
 	
 	/**
 	 * Get a list of ALL the system's users and their corresponding information.
 	 * @param None
 	 * @return A bidimensional String array with ALL the database users
 	 */
 	public String[][] getAllUsers() {
 		
 		String query = "SELECT " + KEY_U_RACEID + ", " + KEY_U_UNAME + ", " + KEY_U_FNAME + ", " + 
 		KEY_U_LNAME + ", " + KEY_U_PHOTO + "," + KEY_U_EMAIL + ", " + 
 				KEY_U_PHONE + " FROM " +DATABASE_USER_TABLE;
 		Cursor cursor = ourDB.rawQuery(query, null);
 		int numColumns = cursor.getColumnCount();
 		int numRows = cursor.getCount();
 		String[][] users = new String[numRows][numColumns];
 		cursor.moveToFirst();
 		int i = 0;
 		while(!cursor.isAfterLast()) {
 			users[i][0] = String.valueOf(cursor.getInt(0)); //ID
 			users[i][1] = cursor.getString(1); //Username
 			users[i][2] = cursor.getString(2); //First name
 			users[i][3] = cursor.getString(3); //Last name
 			users[i][4] = cursor.getString(4); //Photo
 			users[i][5] = cursor.getString(5); //Email
 			users[i][6] = cursor.getString(6); //Phone number
 			i++;
 			cursor.moveToNext();
 		}
 		
 		cursor.close();
 		return users;
 	}
 	
 	/**
 	 * Get a user's information given his/her table ID.
 	 * @param The user's ID number
 	 * @return A String array with the user information in each item
 	 */
 	public String[] getUserInfo(int userID) {
 		
 		String query = "SELECT " + KEY_U_FNAME + ", " + 
 		KEY_U_LNAME + ", " + KEY_U_EMAIL + ", " + 
 				KEY_U_PHONE + " FROM " +DATABASE_USER_TABLE + " WHERE " + KEY_U_RACEID +"= "+userID;
 		Cursor cursor = ourDB.rawQuery(query, null);
 		int numColumns = cursor.getColumnCount();
 		String[] user = new String[numColumns];
 		cursor.moveToFirst();
 		int i = 0;
 		if(cursor.moveToFirst()) {
 			user[0] = cursor.getString(0)+ " "+cursor.getString(1);; //First name
 			//user[1] = convertIntToStringDate(cursor.getInt(2)); //Birthdate
 			user[1] = cursor.getString(2); //Email
 			user[2] = cursor.getString(3); //Phone number
 		}
 		
 		cursor.close();
 		return user;
 	}
 	
 	
 	
 	/**
 	 * Converts an int date to its String representation. This method takes a date in Integer
 	 * format and returns to its String equivalent using the format dd/mm/yyyy.
 	 * @param The date in Integer format
 	 * @return The date in String format (dd/mm/yyyy)
 	 */
 	public String convertIntToStringDate(int date) {
 		
 		String dateToString = String.valueOf(date);
 		
 		return dateToString.charAt(0)+""+dateToString.charAt(1)+ "/" + dateToString.charAt(2) + 
 				""+dateToString.charAt(3) + "/" + dateToString.charAt(4) + ""+ dateToString.charAt(5) + 
 				""+dateToString.charAt(6) + ""+dateToString.charAt(7);
 	}
 	
 	/**
 	 * Retrieves a user's phone number.
 	 * @param The username to be looked up
 	 * @return The user's phone number
 	 */
 	public String getPhoneNumber(int userID) {
 		
 		String phoneNumber = "";
 		String query = "SELECT " + KEY_U_PHONE + " FROM " + DATABASE_USER_TABLE + 
 				" WHERE " + KEY_U_RACEID+"= " + userID;      
 		Cursor cursor = ourDB.rawQuery(query, null);
 		if(cursor.moveToFirst()) {
 			phoneNumber = cursor.getString(0);
 		}
 		return phoneNumber;
 	}
 	
 	public String[][] getCampActivities(int campID) {
 		
 		String query = "SELECT " + KEY_A_RACEID + ", " + KEY_A_NAME + ", " + KEY_A_URL + ", " + 
 				KEY_A_PICTURE + " FROM " +DATABASE_ACTIVITY_TABLE;
 		
 				Cursor cursor = ourDB.rawQuery(query, null);
 				int numColumns = cursor.getColumnCount();
 				int numRows = cursor.getCount();
 				String[][] activities = new String[numRows][numColumns];
 				cursor.moveToFirst();
 				int i = 0;
 				while(!cursor.isAfterLast()) {
 					activities[i][0] = String.valueOf(cursor.getInt(0)); //ID
 					activities[i][1] = cursor.getString(1); //Activity name
 					activities[i][2] = cursor.getString(2); //File URL
 					activities[i][3] = cursor.getString(3); //Picture
 					i++;
 					cursor.moveToNext();
 				}
 				
 				cursor.close();
 				return activities;
 
 	}
 	
 	public String getActivityFileURL(long activityID) {
 		
 		String url = null;
 		String query = "SELECT " + KEY_A_URL + " FROM " + DATABASE_ACTIVITY_TABLE + 
 				" WHERE " + KEY_A_RACEID + "= " + activityID;
 		Cursor cursor = ourDB.rawQuery(query, null);
 		
 		if(cursor.moveToFirst()) {
 			url = cursor.getString(0);
 		}
 		
 		return url;
 	}
 	
 	
 	
 	
 	
 	
 
 }
