 package dk.dtu.sensible.economicsgames;
 
 import dk.dtu.sensible.economicsgames.Message.Type;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 
     private static final int DATABASE_VERSION = 1;
     public static final String GAMES_TABLE_NAME = "current_games";
     public static final String GAME_ID = "id";
     public static final String GAME_TYPE = "type";
     public static final String GAME_PARTICIPANTS = "participants";
     public static final String GAME_STARTED = "started";
     public static final String GAME_OPENED = "opened";
     
     private static final String GAMES_TABLE_CREATE =
                 "CREATE TABLE " + GAMES_TABLE_NAME + " (" +
                     	GAME_ID + " TEXT PRIMARY KEY, " +
                     	GAME_TYPE + " TEXT, " +
                     	GAME_PARTICIPANTS + " INTEGER, " +
                     	GAME_STARTED + " INTEGER, " +
                     	GAME_OPENED + " INTEGER);";
     
     
 
     public static final String CODES_TABLE_NAME = "codes";
     public static final String CODES_CODE = "code";
     public static final String CODES_TIMESTAMP = "timestamp";
     
     private static final String CODES_TABLE_CREATE =
                 "CREATE TABLE " + CODES_TABLE_NAME + " (" +
                 		CODES_CODE + " TEXT PRIMARY KEY,"+
                 		CODES_TIMESTAMP + " INTEGER);";
     
     // TODO: Let this class handle the db - make all the methods non-static (or make non-static versions)
     
 	DatabaseHelper(Context context) {
         super(context, SharedConstants.DATABASE_NAME, null, DATABASE_VERSION);
     }
 	
 
     @Override
     public void onCreate(SQLiteDatabase db) {
         db.execSQL(GAMES_TABLE_CREATE);
         db.execSQL(CODES_TABLE_CREATE);
     }
 
     
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		
 	}
 	
 	
 	public static void insertCode(SQLiteDatabase db, String code, int timestamp) {
 		db.beginTransaction();
 		//SQLiteDatabase.CONFLICT_IGNORE means ignore if already there
 		db.insertWithOnConflict(CODES_TABLE_NAME, 
 				null, 
 				getCodeValues(code, timestamp), 
 				SQLiteDatabase.CONFLICT_IGNORE);
 		db.setTransactionSuccessful();
 		db.endTransaction();
 	}
 
 
 	public static Cursor getCodes(SQLiteDatabase db) {
 		Cursor results = db.query(CODES_TABLE_NAME, 
 				null, // select all cols
 				null, // Where clause - select all 
 				null, // Selection args 
 				null, // Group by
 				null, // Having
 				DatabaseHelper.CODES_TIMESTAMP + " ASC"); // Order by started ascending
 		
 		return results;
 	}
 	
 	public static void removeCodesNotIn(SQLiteDatabase db, String[] codes) {
 		String codeString = new String();
 		
 		for (String code : codes) {
 			if (codeString.length() > 0) {
 				codeString += "\",\"";
 			}
 			codeString += code;
 		}
 		
 		db.beginTransaction();
 		db.delete(CODES_TABLE_NAME, CODES_CODE+" NOT IN (\""+codeString+"\")", null);
 		db.setTransactionSuccessful();
 		db.endTransaction();
 	}
 
 	public static Game getGame(SQLiteDatabase db, String id) {
 		
 		Cursor cursor = db.query(GAMES_TABLE_NAME, null, "id=\""+id+"\"", null, null, null, null);
 		
 		if (cursor.getCount() < 1) {
 			return null;
 		}
 		return new Game(cursor);
 	}
 	
 
 	public static Cursor getGames(SQLiteDatabase db) {
 		
 		Cursor results = db.query(GAMES_TABLE_NAME, 
 //				new String[]{CurrentGamesDatabaseHelper.GAME_ID, // Selected cols
 //					CurrentGamesDatabaseHelper.GAME_TYPE,
 //					CurrentGamesDatabaseHelper.GAME_STARTED, 
 //					CurrentGamesDatabaseHelper.GAME_PARTICIPANTS}, 
 				null, // select all cols - easier, and only an int that is possibly read too much
 				null, // Where clause - select all 
 				null, // Selection args 
 				null, // Group by
 				null, // Having
 				DatabaseHelper.GAME_STARTED + " ASC"); // Order by started ascending
 		
 		return results;
 	}
 	
 
 	public static void removeGame(SQLiteDatabase db, String id) {
 		db.beginTransaction();
		db.delete(GAMES_TABLE_NAME, "id=\""+id+"\"", null);
 		db.setTransactionSuccessful();
 		db.endTransaction();
 	}
 	
 	public static void removeGamesNotIn(SQLiteDatabase db, String[] ids) {
 		String idString = new String();
 		
 		for (String id : ids) {
 			if (idString.length() > 0) {
				idString += "\",\"";
 			}
 			idString += id;
 		}
 		
 		db.beginTransaction();
		db.delete(GAMES_TABLE_NAME, GAME_ID+" NOT IN (\""+idString+"\")", null);
 		db.setTransactionSuccessful();
 		db.endTransaction();
 	}
 
 	public static void insertGame(SQLiteDatabase db, Game game) {
 		insertGame(db, game.id, game.typeToString(), game.started, game.opened, game.participants);
 	}
 	
 	public static void insertGame(SQLiteDatabase db, String id, Type type, int started, int opened, int participants) {
 		insertGame(db, id, Game.typeToString(type), started, opened, participants);
 	}
 	
 	public static void insertGame(SQLiteDatabase db, String id, String type, int started, int opened, int participants) {
 		db.beginTransaction();
 		//SQLiteDatabase.CONFLICT_IGNORE means ignore if already there
 		db.insertWithOnConflict(GAMES_TABLE_NAME, 
 				null, 
 				getGameValues(id, type, started, opened, participants), 
 				SQLiteDatabase.CONFLICT_IGNORE);
 		db.setTransactionSuccessful();
 		db.endTransaction();
 	}
 	
 	public static void updateGame(SQLiteDatabase db, String id, Type type, int started, int opened, int participants) {
 		updateGame(db, id, Game.typeToString(type), started, opened, participants);
 	}
 	
 	public static void updateGame(SQLiteDatabase db, String id, String type, int started, int opened, int participants) {
 		db.beginTransaction();
 		//SQLiteDatabase.CONFLICT_IGNORE means ignore if already there
 		db.update(GAMES_TABLE_NAME, 
 				getGameValues(id, type, started, opened, participants), 
				"id=\""+id+"\"",
 				null);
 		db.setTransactionSuccessful();
 		db.endTransaction();
 	}
 
 
 	public static ContentValues getGameValues(String id, String type, int started, int opened, int participants) {
 		ContentValues values = new ContentValues();
 		values.put(GAME_ID, id);
 		values.put(GAME_TYPE, type);
 		values.put(GAME_STARTED, started);
 		values.put(GAME_OPENED, opened);
 		values.put(GAME_PARTICIPANTS, participants);
 		return values;
 	}
 
 	
 	public static ContentValues getCodeValues(String code, int timestamp) {
 		ContentValues values = new ContentValues();
 		values.put(CODES_CODE, code);
 		values.put(CODES_TIMESTAMP, timestamp);
 		return values;
 	}
 }
