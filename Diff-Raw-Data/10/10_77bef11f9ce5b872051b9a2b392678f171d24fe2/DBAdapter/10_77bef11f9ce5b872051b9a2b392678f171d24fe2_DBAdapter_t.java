 package illinois.sweng.sctracker;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DBAdapter {
 
 	private static final String TAG = "TrackerDatabaseAdapter";
 	
 	//COLUMNS OF PLAYER DATABASE
 	public static final String KEY_ROWID = "_id";
 	public static final String KEY_PK = "pk";
 	public static final String KEY_PICTURE = "picture";
 	public static final String KEY_HANDLE = "handle";
 	public static final String KEY_NAME = "name";
 	public static final String KEY_RACE = "race";
 	public static final String KEY_TEAM = "team";
 	public static final String KEY_NATIONALITY = "nationality";
 	public static final String KEY_ELO = "elo";
 	public static final String KEY_TAG = "tag";
 	public static final String KEY_STARTDATE = "startdate";
 	public static final String KEY_ENDDATE = "enddate";
 	
 	//DATABASE INFORMATION
 	private static final String DATABASE_NAME = "TrackerDatabase";
 	public static final String DATABASE_PLAYER_TABLE = "players";
 	public static final String DATABASE_TEAM_TABLE = "teams";
 	private static final String DATABASE_EVENT_TABLE = "events";
 	private static final int DATABASE_VERSION = 1;
 
 	//TABLE CREATION STRINGS
 	private static final String CREATE_PLAYER_TABLE = "create table "
 			+ DATABASE_PLAYER_TABLE + " ( "
 			+ KEY_ROWID
 			+ " integer primary key autoincrement, " + KEY_PK
 			+ " integer, " + KEY_PICTURE + " text, "
 			+ KEY_HANDLE + " text not null, "
 			+ KEY_NAME + " text not null, "
 			+ KEY_RACE + " text not null, "
 			+ KEY_TEAM + " text, "
 			+ KEY_NATIONALITY + " text not null, "
 			+ KEY_ELO + " text not null);";
 
 	private static final String CREATE_TEAM_TABLE = "create table "
 			+ DATABASE_TEAM_TABLE + " ( "
 			+ KEY_ROWID + " integer primary key autoincrement, "
 			+ KEY_PK + " integer, " + KEY_NAME
 			+ " text not null, " + KEY_TAG + " text not null);";
 	
 	private static final String CREATE_EVENT_TABLE =
 			"create table " + DATABASE_EVENT_TABLE + " ( " 
 			+ KEY_ROWID + " integer primary key autoincrement, "
 			+ KEY_PK + " integer, "
 			+ KEY_PICTURE + " text, "
 			+ KEY_NAME + " text not null, "
 			+ KEY_STARTDATE + " text not null, "
 			+ KEY_ENDDATE + " text not null);";
 
 	private final Context mContext;
 	private DatabaseHelper mDatabaseHelper;
 	private SQLiteDatabase mDatabase;
 
 	public DBAdapter(Context context) {
 		mContext = context;
 		mDatabaseHelper = new DatabaseHelper(mContext);
 	}
 
 	private static class DatabaseHelper extends SQLiteOpenHelper {
 		DatabaseHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(CREATE_PLAYER_TABLE);
 			db.execSQL(CREATE_TEAM_TABLE);
 			db.execSQL(CREATE_EVENT_TABLE);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			// do whatever needs tobe done to upgrade
 		}
 	}
 
 	public DBAdapter open() {
 		mDatabase = mDatabaseHelper.getWritableDatabase();
 		return this;
 	}
 
 	public void close() {
 		mDatabaseHelper.close();
 	}
 	
 	/**
 	 * Retrieves a cursor across all the players in the database.
 	 * @return A cursor across all players in the database.
 	 */
 	public Cursor getAllPlayers() {
 		Log.d(TAG, "Get all players query");
 		String[] columns = new String[] { KEY_ROWID, KEY_PK, KEY_PICTURE,
 				KEY_HANDLE, KEY_NAME, KEY_RACE, KEY_TEAM, KEY_NATIONALITY,
 				KEY_ELO };
 		return mDatabase.query(DATABASE_PLAYER_TABLE, columns, null, null,
 				null, null, KEY_NAME + " ASC");
 	}
 	
 	/**
 	 * getPlayer returns a player from the database based upon the supplied rowid.
 	 * @param rowid Integer specifying the rowid of the player in the database.
 	 * @return A cursor over the player returned from the database.
 	 */
 	public Cursor getPlayer(int rowid) {
 		return mDatabase.query(DATABASE_PLAYER_TABLE,
 				new String[] {KEY_ROWID, KEY_PICTURE, KEY_HANDLE, KEY_NAME,
 					KEY_RACE, KEY_TEAM, KEY_NATIONALITY, KEY_ELO},
 				KEY_ROWID + "=" + rowid, null, null, null, null);
 	}
 	
 	/**
 	 * getPlayerByPK finds a player based upon their (as guaranteed by the server)
 	 * unique PK.
 	 * @param pk The integer PK that uniquely identifies players (As used in serverside database).
 	 * @return A cursor over the player found.
 	 */
 	public Cursor getPlayerByPK(int pk) {
 		return mDatabase.query(DATABASE_PLAYER_TABLE,
 				new String[] {KEY_ROWID, KEY_PICTURE, KEY_HANDLE, KEY_NAME,
 					KEY_RACE, KEY_TEAM, KEY_NATIONALITY, KEY_ELO},
 				KEY_PK + "=" + pk, null, null, null, null);
 	}
 	
 	/**
 	 * Updates an individual player's data with the database.
 	 * @param pk The integer PK that can be used to uniquely identify players (as guaranteed by server).
 	 * @param playerData The JSONObject containing the data about the player.
 	 * @return Boolean; true if the operation has succeeded, false otherwise.
 	 */
 	public boolean updatePlayer(int pk, JSONObject playerData) {
 		ContentValues data = new ContentValues();
 		try{
 			data.put(KEY_PICTURE, playerData.getString("picture").toString());
 			data.put(KEY_HANDLE, playerData.getString("handle").toString());
 			data.put(KEY_NAME, playerData.getString("name").toString());
 			data.put(KEY_RACE, playerData.getString("race").toString());
 			data.put(KEY_TEAM, playerData.getString("team").toString());
 			data.put(KEY_NATIONALITY, playerData.getString("nationality").toString());
 			data.put(KEY_ELO, playerData.getString("elo").toString());
 			
 	        return mDatabase.update(DATABASE_PLAYER_TABLE, data, 
                     KEY_PK + "=" + pk, null) > 0;
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	/**
 	 * Updates the data in the database based upon the players supplied by the server.
 	 * @param players A JSONArray containing the JSONObjects for each player.
 	 * @return True if the operation succeeded, false otherwise.
 	 */
 	public boolean updatePlayerTable(JSONArray players) {
 		int numPlayers = players.length();
 		try{
 			for(int i = 0; i < numPlayers; i++) {
 				JSONObject player = (JSONObject) players.get(i);
 				int pk = player.getInt("pk");
 				if (hasPlayer(pk)) {
 					updatePlayer(pk, player.getJSONObject("fields"));
 				} else {
 					insertPlayer(pk, player.getJSONObject("fields"));
 				}
 			}
 			return true;
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return false;
 		}
 		
 	}
 	
 	/**
 	 * Checks to see whether a player with the supplied pk exists in the database.
 	 * @param pk An integer that uniquely identifies a player (as guaranteed by the server).
 	 * @return True if the database contains the player; false otherwise.
 	 */
 	public boolean hasPlayer(int pk) {
 		Cursor playerCursor = mDatabase.query(DATABASE_PLAYER_TABLE,
 				new String[] {KEY_ROWID, KEY_PICTURE, KEY_HANDLE, KEY_NAME,
 				KEY_RACE, KEY_TEAM, KEY_NATIONALITY, KEY_ELO},
 			KEY_PK + "=" + pk, null, null, null, null);
 		
 		int playerCount = playerCursor.getCount();
 		playerCursor.close();
 		return (playerCount == 1);
 	}
 	
 	/**
 	 * Inserts a player into the database.
 	 * @param pk Integer that uniquely identifies a player (as guaranteed by the server).
 	 * @param playerData The JSONObject containing the information about the player to insert.
 	 * @return True if the operation succeeds; false otherwise.
 	 */
 	public boolean insertPlayer(int pk, JSONObject playerData) {
 		ContentValues data = new ContentValues();
 		try{
 			data.put(KEY_PK, pk);
 			data.put(KEY_PICTURE, playerData.getString("picture").toString());
 			data.put(KEY_HANDLE, playerData.getString("handle").toString());
 			data.put(KEY_NAME, playerData.getString("name").toString());
 			data.put(KEY_RACE, playerData.getString("race").toString());
 			data.put(KEY_TEAM, playerData.getString("team").toString());
 			data.put(KEY_NATIONALITY, playerData.getString("nationality").toString());
 			data.put(KEY_ELO, playerData.getString("elo").toString());
 			
 	        return mDatabase.insert(DATABASE_PLAYER_TABLE, null, data) > 0; 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	
 	public Cursor getPlayersByTeam(int teamname){
 		return mDatabase.query(DATABASE_PLAYER_TABLE,
				new String[] {KEY_ROWID, KEY_PK, KEY_PICTURE, KEY_HANDLE, KEY_NAME,
 				KEY_RACE, KEY_TEAM, KEY_NATIONALITY, KEY_ELO}, 
 				KEY_TEAM + "=" + "1", null, null, null, null);
 	}
 	
 	public Cursor getTeam(int rowid) {
 		return mDatabase.query(DATABASE_TEAM_TABLE,
 				new String[] {KEY_ROWID, KEY_PK, KEY_NAME,
 					KEY_TAG},
 				KEY_ROWID + "=" + rowid, null, null, null, null);
 	}
 	
 	public Cursor getTeamByPK(int pk) {
 		return mDatabase.query(DATABASE_TEAM_TABLE,
 				new String[] {KEY_ROWID, KEY_PK, KEY_NAME,
 					KEY_TAG},
 				KEY_PK + "=" + pk, null, null, null, null);
 	}
 	
 	public Cursor getAllTeams() {
 		return mDatabase.query(DATABASE_TEAM_TABLE, 
 				new String[] {KEY_ROWID, KEY_PK, KEY_NAME, KEY_TAG},
 				null, null, null, null, KEY_NAME+" ASC");
 	}
 	
 	public boolean updateTeamTable(JSONArray teams) {
 		int numPlayers = teams.length();
 		try{
 			for(int i = 0; i < numPlayers; i++) {
 				JSONObject team = (JSONObject) teams.get(i);
 				int pk = team.getInt("pk");
 				if (hasTeam(pk)) {
 					updateTeam(pk, team.getJSONObject("fields"));
 				} else {
 					insertTeam(pk, team.getJSONObject("fields"));
 				}
 			}
 			return true;
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public boolean hasTeam(int pk) {
 		Cursor teamCursor = mDatabase.query(DATABASE_TEAM_TABLE,
 				new String[] {KEY_ROWID, KEY_PK, KEY_NAME, KEY_TAG},
 			KEY_PK + "=" + pk, null, null, null, null);
 		
 		int teamCount = teamCursor.getCount();
 		teamCursor.close();
 		return (teamCount == 1);
 	}
 	
 	public boolean updateTeam(int pk, JSONObject teamData) {
 		ContentValues data = new ContentValues();
 		try{
 			data.put(KEY_NAME, teamData.getString("name").toString());
 			data.put(KEY_TAG, teamData.getString("tag").toString());
 	        return mDatabase.update(DATABASE_TEAM_TABLE, data, 
                     KEY_PK + "=" + pk, null) > 0;
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public boolean insertTeam(int pk, JSONObject teamData) {
 		ContentValues data = new ContentValues();
 		try{
 			data.put(KEY_PK, pk);
 			data.put(KEY_NAME, teamData.getString("name").toString());
 			data.put(KEY_TAG, teamData.getString("tag").toString());
 	        return mDatabase.insert(DATABASE_TEAM_TABLE, null, data) > 0; 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 
 	/**
 	 * Retrieves a cursor across all the events in the database.
 	 * @return A cursor across all events in the database.
 	 */
 	public Cursor getAllEvents() {
 		return mDatabase.query(DATABASE_EVENT_TABLE, 
 				new String[] {KEY_ROWID, KEY_NAME, KEY_STARTDATE, KEY_ENDDATE},
 				null, null, null, null, KEY_NAME+" ASC");
 	}
 
 	/**
 	 * getEvent returns a event from the database based upon the supplied rowid.
 	 * @param rowid Integer specifying the rowid of the event in the database.
 	 * @return A cursor over the event returned from the database.
 	 */
 	public Cursor getEvent(int rowid) {
 		return mDatabase.query(DATABASE_EVENT_TABLE,
 				new String[] {KEY_ROWID, KEY_PICTURE, KEY_NAME, KEY_STARTDATE, KEY_ENDDATE},
 				KEY_ROWID + "=" + rowid, null, null, null, null);
 	}
 
 	/**
 	 * getEventByEK finds an event based upon their (as guaranteed by the server)
 	 * unique EK.
 	 * @param pk The integer PK that uniquely identifies events (As used in serverside database).
 	 * @return A cursor over the event found.
 	 */
 	public Cursor getEventByEK(int pk) {
 		return mDatabase.query(DATABASE_EVENT_TABLE,
 				new String[] {KEY_ROWID, KEY_PICTURE, KEY_NAME, KEY_STARTDATE, KEY_ENDDATE},
 				KEY_PK + "=" + pk, null, null, null, null);
 	}
 
 	/**
 	 * Updates an individual event's data with the database.
 	 * @param pk The integer PK that can be used to uniquely identify events (as guaranteed by server).
 	 * @param eventData The JSONObject containing the data about the events.
 	 * @return Boolean; true if the operation has succeeded, false otherwise.
 	 */
 	public boolean updateEvent(int pk, JSONObject eventData) {
 		ContentValues data = new ContentValues();
 		try{
 			//TODO: update with actual picture
 //			data.put(KEY_PICTURE, eventData.getString("picture").toString());
 			data.put(KEY_PICTURE, "picture");
 			data.put(KEY_NAME, eventData.getString("name").toString());
 			data.put(KEY_STARTDATE, eventData.getString("start_date").toString());
 			data.put(KEY_ENDDATE, eventData.getString("end_date").toString());
 
 			return mDatabase.update(DATABASE_EVENT_TABLE, data, 
 					KEY_PK + "=" + pk, null) > 0;
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	/**
 	 * Updates the data in the database based upon the events supplied by the server.
 	 * @param events A JSONArray containing the JSONObjects for each event.
 	 * @return True if the operation succeeded, false otherwise.
 	 */
 	public boolean updateEventTable(JSONArray events) {
 		int numEvents = events.length();
 		try{
 			for(int i = 0; i < numEvents; i++) {
 				JSONObject event = (JSONObject) events.get(i);
 				int pk = event.getInt("pk");
 				if (hasEvent(pk)) {
 					updateEvent(pk, event);
 				} else {
 					insertEvent(pk, event);
 				}
 			}
 			return true;
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return false;
 		}
 
 	}
 
 	/**
 	 * Checks to see whether an event with the supplied ek exists in the database.
 	 * @param ek An integer that uniquely identifies a player (as guaranteed by the server).
 	 * @return True if the database contains the player; false otherwise.
 	 */
 	public boolean hasEvent(int pk) {
 		Cursor eventCursor = mDatabase.query(DATABASE_EVENT_TABLE,
 				new String[] {KEY_ROWID, KEY_PICTURE, KEY_NAME, KEY_STARTDATE, KEY_ENDDATE},
 				KEY_PK + "=" + pk, null, null, null, null);
 		int eventCount = eventCursor.getCount();
 		eventCursor.close();
 
 		return eventCount == 1;
 	}
 
 	/**
 	 * Inserts an event into the database.
 	 * @param pk Integer that uniquely identifies an event (as guaranteed by the server).
 	 * @param eventData The JSONObject containing the information about the event to insert.
 	 * @return True if the operation succeeds; false otherwise.
 	 */
 	public boolean insertEvent(int pk, JSONObject eventData) {
 		ContentValues data = new ContentValues();
 		try{
 			data.put(KEY_PK, pk);
 			//TODO: use actual picture
 //			data.put(KEY_PICTURE, eventData.getString("picture").toString());
 			data.put(KEY_PICTURE, "picture");
 			data.put(KEY_NAME, eventData.getString("name").toString());
 			data.put(KEY_STARTDATE, eventData.getString("start_date").toString());
 			data.put(KEY_ENDDATE, eventData.getString("end_date").toString());
 
 			return mDatabase.insert(DATABASE_EVENT_TABLE, null, data) > 0; 
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	
 	
 	
 }
