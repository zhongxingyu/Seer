 package org.touchirc.db;
 
 import org.touchirc.model.Profile;
 import org.touchirc.model.Server;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.SparseArray;
 
 public class Database extends SQLiteOpenHelper {
 
 	private static final String DB_NAME = "touchirc.db";
 	private static final int DB_VERSION = 1;
 
 	public Database(Context context) {
 		super(context, DB_NAME, null, DB_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 
 		db.execSQL("CREATE TABLE " + DBConstants.SERVER_TABLE_NAME +"(" + DBConstants.SERVER_ID
 				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
 				+ DBConstants.SERVER_TITLE + " TEXT NOT NULL UNIQUE,"
 				+ DBConstants.SERVER_HOST + " TEXT NOT NULL,"
 				+ DBConstants.SERVER_PORT + " INTEGER,"
 				+ DBConstants.SERVER_PASSWORD + " TEXT, "
 				+ DBConstants.SERVER_USE_SSL + " BOOLEAN, "
 				+ DBConstants.SERVER_CHARSET + " TEXT,"
 				+ DBConstants.SERVER_AUTOCONNECT + " BOOLEAN );");
 
 		db.execSQL("CREATE TABLE " + DBConstants.PROFILE_TABLE_NAME + "(" + DBConstants.PROFILE_ID
 				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
 				+ DBConstants.PROFILE_NAME + " TEXT NOT NULL UNIQUE,"
 				+ DBConstants.PROFILE_FIRST_NICKNAME + " TEXT NOT NULL,"
 				+ DBConstants.PROFILE_SCD_NICKNAME + " TEXT,"
 				+ DBConstants.PROFILE_THIRD_NICKNAME + " TEXT,"
 				+ DBConstants.PROFILE_USERNAME + " TEXT NOT NULL,"
 				+ DBConstants.PROFILE_REALNAME + " TEXT NOT NULL,"
 				+ DBConstants.DEFAULT_PROFILE + " BOOLEAN );");
 		
 		db.execSQL("CREATE TABLE " + DBConstants.LINKED_SERVERS_TO_PROFILE_NAME + "("
 				+ DBConstants.LINKED_PROFILE_ID + " INTEGER,"
 				+ DBConstants.LINKED_SERVER_ID + " INTEGER PRIMARY KEY,"
 				+ " FOREIGN KEY (" + DBConstants.LINKED_PROFILE_ID + ") REFERENCES " 
 					+ DBConstants.PROFILE_TABLE_NAME + "(" + DBConstants.PROFILE_ID + "),"
 				+ " FOREIGN KEY (" + DBConstants.LINKED_SERVER_ID + ") REFERENCES " 
 					+ DBConstants.SERVER_TABLE_NAME + "(" + DBConstants.SERVER_ID + "));");
 		
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * Add the given server to the database
 	 * 
 	 * @param server
 	 */
 
 	public void addServer(Server server) {
 
 		ContentValues values = new ContentValues();
 
 		values.put(DBConstants.SERVER_TITLE, server.getName());
 		values.put(DBConstants.SERVER_HOST, server.getHost());
 		values.put(DBConstants.SERVER_PORT, server.getPort());
 		values.put(DBConstants.SERVER_PASSWORD, server.getPassword());
 		values.put(DBConstants.SERVER_USE_SSL, server.useSSL());
 		values.put(DBConstants.SERVER_CHARSET, server.getEncoding());
 		values.put(DBConstants.SERVER_AUTOCONNECT, false);
 
 		this.getWritableDatabase().insert(
 											DBConstants.SERVER_TABLE_NAME,
 											null,
 											values);
 
 	}
 	
 	/**
 	 * Delete the Server from the database
 	 * the method delete returns the number of rows deleted inside the database
 	 * 
 	 * @param idServer
 	 * @return if the deletion has succeeded
 	 */
 
 	public boolean deleteServer(int idServer){
 		
 		return this.getWritableDatabase().delete(
 												DBConstants.SERVER_TABLE_NAME, 
 												DBConstants.SERVER_ID + "=\"" + idServer + "\"", 
 												null
 			) > 0;
 	}
 	
 	/**
 	 * Update the values of the current server
 	 * 
 	 * @param s
 	 * @return if the update succeeds
 	 */
 
 	public boolean updateServer(Server s, String oldNameServer){
 		ContentValues newValues = new ContentValues();
 		newValues.put(DBConstants.SERVER_TITLE, s.getName());
 		newValues.put(DBConstants.SERVER_HOST, s.getHost());
 		newValues.put(DBConstants.SERVER_PORT, s.getPort());
 		newValues.put(DBConstants.SERVER_PASSWORD, s.getPassword());
 
 		if(this.getWritableDatabase().update(	
 											DBConstants.SERVER_TABLE_NAME,
 											newValues, 
 											DBConstants.SERVER_TITLE + "=" + "\"" + oldNameServer + "\"",
 											null
 		) > 0){
 			return true;
 		}
 
 		return false;
 	}
 	
 		
 	/**
 	 * Allows to collect the servers'list
 	 * 
 	 * @return An ArrayList of all the available Server inside the Database
 	 */
 
 	public SparseArray<Server> getServerList() {
 
 		SparseArray<Server> listServer = new SparseArray<Server>();
 
 		// SELECT ALL FROM servers ORDER BY title ASC
 		Cursor cursor = this.getReadableDatabase().query(
 														DBConstants.SERVER_TABLE_NAME, 
 														DBConstants.SERVER_ALL, 
 														null,
 														null, 
 														null, 
 														null, 
 														DBConstants.SERVER_TITLE + " ASC");
 
 		while (cursor.moveToNext()) {
 			listServer.put(cursor.getInt(cursor.getColumnIndex(DBConstants.SERVER_ID)),createServer(cursor));
 		}
 
 		cursor.close();
 
 		return listServer;
 	}
 
 	/**
 	 * Create a server object with the given database cursor
 	 * 
 	 * @param cursor
 	 * @return A Server object with the cursor spent in arguments
 	 */
 
 	private Server createServer(Cursor cursor) {
 
 		Server server = new Server(
 			cursor.getString(cursor.getColumnIndex((DBConstants.SERVER_TITLE))),
 			cursor.getString(cursor.getColumnIndex((DBConstants.SERVER_HOST))),
 			cursor.getInt(cursor.getColumnIndex((DBConstants.SERVER_PORT))),
 			cursor.getString(cursor.getColumnIndex(DBConstants.SERVER_PASSWORD)),
 			cursor.getString(cursor.getColumnIndex(DBConstants.SERVER_CHARSET)));
 
 		// TODO SSL Support
 
 		return server;
 	}
 
 
 	/**
 	 * Add the given profile to the database
 	 * 
 	 * @param profile
 	 */
 
 	public void addProfile(Profile profile) {
 
 		ContentValues values = new ContentValues();
 
 		values.put(DBConstants.PROFILE_NAME, profile.getProfile_name());
 		values.put(DBConstants.PROFILE_FIRST_NICKNAME, profile.getFirstNick());
 		values.put(DBConstants.PROFILE_SCD_NICKNAME, profile.getSecondNick());
 		values.put(DBConstants.PROFILE_THIRD_NICKNAME, profile.getThirdNick());
 		values.put(DBConstants.PROFILE_USERNAME, profile.getUsername());
 		values.put(DBConstants.PROFILE_REALNAME, profile.getRealname());
 		values.put(DBConstants.DEFAULT_PROFILE, "false");
 
 		this.getWritableDatabase().insert(
 										DBConstants.PROFILE_TABLE_NAME,
 										null,
 										values);
 
 	}
 	
 	/**
 	 * Delete the Profile from the database
 	 * the method delete returns the number of rows deleted inside the database
 	 * 
 	 * @param profileName
 	 * @return if the deletion has succeeded
 	 */
 
 	public boolean deleteProfile(int idProfile){
 		
 		return this.getWritableDatabase().delete(
 												DBConstants.PROFILE_TABLE_NAME, 
 												DBConstants.PROFILE_ID + "=\"" + idProfile + "\"", 
 												null
 				) > 0;
 	}
 	
 	/**
 	 * Update the values of the current profile
 	 * 
 	 * @param p
 	 * @return if the update has succeeded
 	 */
 
 	public boolean updateProfile(Profile p, String oldeNameProfile){
 		
 		ContentValues newValues = new ContentValues();
 		newValues.put(DBConstants.PROFILE_NAME, p.getProfile_name());
 		newValues.put(DBConstants.PROFILE_FIRST_NICKNAME, p.getFirstNick());
 		newValues.put(DBConstants.PROFILE_SCD_NICKNAME, p.getSecondNick());
 		newValues.put(DBConstants.PROFILE_THIRD_NICKNAME, p.getThirdNick());
 		newValues.put(DBConstants.PROFILE_USERNAME, p.getUsername());
 		newValues.put(DBConstants.PROFILE_REALNAME, p.getRealname());
 		
 		if(this.getWritableDatabase().update(	
 												DBConstants.PROFILE_TABLE_NAME,
 												newValues, 
 												DBConstants.PROFILE_NAME + "=" + "\"" + oldeNameProfile + "\"",
 												null
 		) > 0){
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Return a profile object with the given profileId
 	 * 
 	 * @param profileId
 	 * @return The corresponding Profile
 	 */
 
 	
 	/**
 	 * Allows to collect the profiles' list
 	 * 
 	 * @return The list of available Profiles inside the Database
 	 */
 
 	public SparseArray<Profile> getProfileList() {
 
 		SparseArray<Profile> listProfile = new SparseArray<Profile>();
 
 		// SELECT ALL FROM profiles ORDER BY name ASC
 		Cursor cursor = this.getReadableDatabase().query(
 														DBConstants.PROFILE_TABLE_NAME, 
 														DBConstants.PROFILE_ALL, 
 														null,
 														null, 
 														null, 
 														null, 
 														DBConstants.PROFILE_NAME + " ASC");
 
 		while (cursor.moveToNext()) {
 			listProfile.put(cursor.getInt(cursor.getColumnIndex(DBConstants.PROFILE_ID)),createProfile(cursor));
 		}
 		cursor.close();
 
 		return listProfile;
 	}
 
 	/**
 	 * Create a profile object with the given database cursor
 	 * 
 	 * @param cursor
 	 * @return Profile created thanks to the cursor spend in arguments
 	 */
 
 	private Profile createProfile(Cursor cursor) {
 
 		Profile profile = new Profile(
 				cursor.getString(cursor.getColumnIndex(DBConstants.PROFILE_NAME)),
 				cursor.getString(cursor.getColumnIndex(DBConstants.PROFILE_FIRST_NICKNAME)),
 				cursor.getString(cursor.getColumnIndex(DBConstants.PROFILE_SCD_NICKNAME)),
 				cursor.getString(cursor.getColumnIndex(DBConstants.PROFILE_THIRD_NICKNAME)),
 				cursor.getString(cursor.getColumnIndex(DBConstants.PROFILE_USERNAME)),
				cursor.getString(cursor.getColumnIndex(DBConstants.PROFILE_REALNAME))
 				);
 		return profile;
 	}
 
 
 	
 
 }
