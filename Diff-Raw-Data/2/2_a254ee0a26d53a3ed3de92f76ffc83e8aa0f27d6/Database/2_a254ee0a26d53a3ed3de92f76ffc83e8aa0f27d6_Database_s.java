 package org.touchirc.db;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.touchirc.TouchIrc;
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
 				+ DBConstants.SERVER_AUTOCONNECT + " BOOLEAN,"
 				+ DBConstants.SERVER_AUTOCONNECTED_CHANNELS + " TEXT,"
 				+ DBConstants.SERVER_IDPROFILE + " INTEGER DEFAULT '0' );");
 
 		db.execSQL("CREATE TABLE " + DBConstants.PROFILE_TABLE_NAME + "(" + DBConstants.PROFILE_ID
 				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
 				+ DBConstants.PROFILE_NAME + " TEXT NOT NULL UNIQUE,"
 				+ DBConstants.PROFILE_FIRST_NICKNAME + " TEXT NOT NULL,"
 				+ DBConstants.PROFILE_SCD_NICKNAME + " TEXT,"
 				+ DBConstants.PROFILE_THIRD_NICKNAME + " TEXT,"
 				+ DBConstants.PROFILE_USERNAME + " TEXT NOT NULL,"
 				+ DBConstants.PROFILE_REALNAME + " TEXT NOT NULL );");
 		
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
 
 	public int addServer(Server server) {
 
 		ContentValues values = new ContentValues();
 
 		values.put(DBConstants.SERVER_TITLE, server.getName());
 		values.put(DBConstants.SERVER_HOST, server.getHost());
 		values.put(DBConstants.SERVER_PORT, server.getPort());
 		values.put(DBConstants.SERVER_PASSWORD, server.getPassword());
 		values.put(DBConstants.SERVER_USE_SSL, server.useSSL());
 		values.put(DBConstants.SERVER_CHARSET, server.getEncoding());
 		values.put(DBConstants.SERVER_AUTOCONNECT, server.isAutoConnect());
 		if(server.getAutoConnectedChannels() != null){
 			ArrayList<String> channels = server.getAutoConnectedChannels();
 			String sChannels = "";
 			for(String s : channels)
 				sChannels += s + ",";
 			values.put(DBConstants.SERVER_AUTOCONNECTED_CHANNELS, sChannels);
 		}
 		if(server.hasAssociatedProfile())
 			values.put(DBConstants.SERVER_IDPROFILE, TouchIrc.getInstance().getAvailableProfiles().indexOfValue(server.getProfile()));
 		
 		this.getWritableDatabase().insert(
 											DBConstants.SERVER_TABLE_NAME,
 											null,
 											values);
 		
 		// return the id of the new 
 		Cursor c = this.getReadableDatabase().rawQuery("select seq from sqlite_sequence where name='servers';",null);
 		c.moveToNext();
 		return c.getInt(0);
 
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
 												DBConstants.SERVER_ID + "=" + idServer, 
 												null
 			) > 0;
 	}
 	
 	/**
 	 * Update the values of the current server
 	 * 
 	 * @param s
 	 * @return if the update succeeds
 	 */
 
 	public boolean updateServer(int idServ, Server server){
 		
 		
 		ContentValues newValues = new ContentValues();
 		newValues.put(DBConstants.SERVER_TITLE, server.getName());
 		newValues.put(DBConstants.SERVER_HOST, server.getHost());
 		newValues.put(DBConstants.SERVER_PORT, server.getPort());
 		newValues.put(DBConstants.SERVER_PASSWORD, server.getPassword());
 		newValues.put(DBConstants.SERVER_USE_SSL, server.useSSL());
 		newValues.put(DBConstants.SERVER_CHARSET, server.getEncoding());
 		newValues.put(DBConstants.SERVER_AUTOCONNECT, server.isAutoConnect());
 		if(server.getAutoConnectedChannels() != null){
 			ArrayList<String> channels = server.getAutoConnectedChannels();
 			String sChannels = "";
 			for(String s : channels)
 				sChannels += s + ",";
 			newValues.put(DBConstants.SERVER_AUTOCONNECTED_CHANNELS, sChannels);
 		}
 		if(server.getProfile() == null){
 			newValues.put(DBConstants.SERVER_IDPROFILE, "0");
 		}else{
 			SparseArray<Profile> profiles = TouchIrc.getInstance().getAvailableProfiles();
 			newValues.put(DBConstants.SERVER_IDPROFILE, profiles.keyAt(profiles.indexOfValue(server.getProfile())));
 		}
 		if(this.getWritableDatabase().update(	
 											DBConstants.SERVER_TABLE_NAME,
 											newValues, 
 											DBConstants.SERVER_ID + "=" + idServ,
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
 			cursor.getString(cursor.getColumnIndex(DBConstants.SERVER_CHARSET)),
 			Boolean.getBoolean(cursor.getString(cursor.getColumnIndex(DBConstants.SERVER_USE_SSL))));
 			
 		int autoConnect = cursor.getInt(cursor.getColumnIndex((DBConstants.SERVER_AUTOCONNECT)));
 		if(autoConnect == 1)
 			server.enableAutoConnect();
 
 		int idProfile = cursor.getInt(cursor.getColumnIndex((DBConstants.SERVER_IDPROFILE)));
 		if(idProfile != 0)
 			server.setProfile(TouchIrc.getInstance().getAvailableProfiles().get(idProfile));
 		
 		String sChannels = cursor.getString(cursor.getColumnIndex(DBConstants.SERVER_AUTOCONNECTED_CHANNELS));
		if(sChannels.length() > 0){
 			String[] tChannels = sChannels.split(",");
 			ArrayList<String> channels = new ArrayList<String>();
 			for(int i = 0 ; i < tChannels.length ; i++)
 				channels.add(tChannels[i]);
 			server.setAutoConnectedChannels(channels);
 		}
 			
 
 		// TODO SSL Support
 
 		return server;
 	}
 
 
 	/**
 	 * Add the given profile to the database
 	 * 
 	 * @param profile
 	 * @return 
 	 */
 
 	public int addProfile(Profile profile) {
 
 		ContentValues values = new ContentValues();
 
 		values.put(DBConstants.PROFILE_NAME, profile.getProfile_name());
 		values.put(DBConstants.PROFILE_FIRST_NICKNAME, profile.getFirstNick());
 		values.put(DBConstants.PROFILE_SCD_NICKNAME, profile.getSecondNick());
 		values.put(DBConstants.PROFILE_THIRD_NICKNAME, profile.getThirdNick());
 		values.put(DBConstants.PROFILE_USERNAME, profile.getUsername());
 		values.put(DBConstants.PROFILE_REALNAME, profile.getRealname());
 
 		this.getWritableDatabase().insert(
 										DBConstants.PROFILE_TABLE_NAME,
 										null,
 										values);
 		
 		// return the id of the new 
 		Cursor c = this.getReadableDatabase().rawQuery("select seq from sqlite_sequence where name='profiles';",null);
 		c.moveToNext();
 		return c.getInt(0);
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
 
 	public boolean updateProfile(int idProfile, Profile profile){
 		
 		ContentValues newValues = new ContentValues();
 		newValues.put(DBConstants.PROFILE_NAME, profile.getProfile_name());
 		newValues.put(DBConstants.PROFILE_FIRST_NICKNAME, profile.getFirstNick());
 		newValues.put(DBConstants.PROFILE_SCD_NICKNAME, profile.getSecondNick());
 		newValues.put(DBConstants.PROFILE_THIRD_NICKNAME, profile.getThirdNick());
 		newValues.put(DBConstants.PROFILE_USERNAME, profile.getUsername());
 		newValues.put(DBConstants.PROFILE_REALNAME, profile.getRealname());
 		
 		
 		if(this.getWritableDatabase().update(	
 												DBConstants.PROFILE_TABLE_NAME,
 												newValues, 
 												DBConstants.PROFILE_ID + "=" + idProfile,
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
