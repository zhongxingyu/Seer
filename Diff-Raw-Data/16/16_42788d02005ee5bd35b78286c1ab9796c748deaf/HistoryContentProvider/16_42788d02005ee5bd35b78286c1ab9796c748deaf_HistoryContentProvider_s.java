 package dk.ilios.influencecounter.history;
 /**
  * Content provider for history information.
  * Switching to using a ContentProvider after trying with async DB helpers, which
  * caused a lot of trouble handling the db connection across fragments including
  * database lock errors.
  * 
  * Accessing the ContentProvider should ideally be done using AsyncQueryHandler
  * to avoid locking the UI-thread.
  * 
  * Christian Melchior <christian@ilios.dk>
  */
 import android.content.ContentProvider;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.database.sqlite.SQLiteStatement;
 import android.net.Uri;
 import dk.ilios.influencecounter.Database;
 import dk.ilios.influencecounter.R;
 import dk.ilios.influencecounter.utils.Logger;
 
 public class HistoryContentProvider extends ContentProvider {
 
 	// Setup URI's
 	public static final String AUTHORITY = "dk.ilios.influencecounter"; 
 	private static final int GAMES = 1;
 	private static final int GAME_ID = 2;
 	private static final int GAMES_CHANGES = 3;
 	private static final int GAMES_CHANGE_ID = 4;
 	private static final int GAMES_MAXID = 5;
 	private static final int GAMES_NEWGAME = 6;
 	private static final int GAMES_DELETE = 7;
 	private static final int GAMES_DELETE_TYPE = 8;
 	private static final int GAMES_CLEANUP = 9;
 	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
 	static {
 		sUriMatcher.addURI(AUTHORITY, Database.TABLE_GAMES, GAMES);				 // Games table
 		sUriMatcher.addURI(AUTHORITY, Database.TABLE_GAMES+"/#", GAME_ID);			 // Games row
 		sUriMatcher.addURI(AUTHORITY, Database.TABLE_GAMES + "/maxId", GAMES_MAXID);		 // Returns current max id
 		sUriMatcher.addURI(AUTHORITY, Database.TABLE_GAMES + "/newGame", GAMES_NEWGAME);	// Create a new game
 		sUriMatcher.addURI(AUTHORITY, Database.TABLE_GAMES + "/deleteGame", GAMES_DELETE);
 		sUriMatcher.addURI(AUTHORITY, Database.TABLE_GAMES + "/deleteType", GAMES_DELETE_TYPE);
 		sUriMatcher.addURI(AUTHORITY, Database.TABLE_GAMES + "/cleanup", GAMES_CLEANUP);
 		sUriMatcher.addURI(AUTHORITY, Database.TABLE_GAME_STATE, GAMES_CHANGES);	 // Game history table
 		sUriMatcher.addURI(AUTHORITY, Database.TABLE_GAME_STATE+"/#", GAMES_CHANGE_ID); // Game history table row
 	}
 
 	// Content URIs
 	public static Uri GAMES_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/" + Database.TABLE_GAMES);
 	public static Uri GAMES_MAX_ID_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/" + Database.TABLE_GAMES + "/maxId");
 	public static Uri GAMES_NEWGAME_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/" + Database.TABLE_GAMES + "/newGame");
 	public static Uri GAMES_DELETE_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/" + Database.TABLE_GAMES + "/deleteGame");
 	public static Uri GAMES_DELETE_TYPE_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/" + Database.TABLE_GAMES + "/deleteType");
 	public static Uri GAMES_CLEANUP_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/" + Database.TABLE_GAMES + "/cleanup");
 	public static Uri GAMES_STATE_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/" + Database.TABLE_GAME_STATE);
 	
 	// Private members
 	private Database mDbHelper;
 	
 	@Override
 	public boolean onCreate() {
 		mDbHelper = new Database(getContext());
 		return true;
 	}
 
 	/***************************************************************************
 	 * Public utility functions for the content provider                       *
 	 **************************************************************************/
 
 	/**
 	 * Returns a 1x1 matrix with the current max game id
 	 */
 	public Cursor getMaxGameId(SQLiteDatabase db) {
 		String sql = "SELECT MAX(_id) FROM " + Database.TABLE_GAMES;
 	    SQLiteStatement statement = db.compileStatement(sql);
 	    long currentMaxGameId = statement.simpleQueryForLong();			
 	    MatrixCursor c = new MatrixCursor(new String[] { "maxId"});
 	    c.addRow(new Long[] { currentMaxGameId });
 	    return c;
 	}	
 	
 	/**
 	 * Create a new game with starting influence. Returns id for new game row 
 	 * or -1 if it failed.
 	 * 
 	 * if provided starting values are given as well, this means 
 	 */
 	public long createNewGame(SQLiteDatabase db, ContentValues config) {
 
 		long gameId = -1;
 		
 		// Determine number of players
 		int players = 1;
 		if (config.containsKey("player2")) {
 			players = 2;
 		}
 
 		db.beginTransaction();
 		try {
 
 			// Insert game
 			ContentValues values = new ContentValues();
 			values.put(Database.COLUMN_GAME_NAME, String.format(getContext().getString(R.string.game_name), "?"));
 			values.put(Database.COLUMN_PLAYERS, players);
 			
 			gameId = db.insert(Database.TABLE_GAMES, null, values);
 			Logger.i("InfluenceCounter", "New game: " + gameId);
 			
 			// Rename game name to match id
 			ContentValues updatedValues = new ContentValues();
 			updatedValues.put(Database.COLUMN_GAME_NAME, String.format(getContext().getString(R.string.game_name), gameId));
 			db.update(Database.TABLE_GAMES, updatedValues, Database.COLUMN__ID+"="+gameId, null);
 
 			// Player 1 values
 			ContentValues player1Values = new ContentValues();
 			player1Values.put(Database.COLUMN_GAME_ID, gameId);
 			player1Values.put(Database.COLUMN_PLAYER_ID, 0);
 			player1Values.put(Database.COLUMN_INFLUENCE, config.getAsInteger("player1"));
 			player1Values.put(Database.COLUMN_TIMESTAMP, config.getAsLong("timestamp"));
 			db.insert(Database.TABLE_GAME_STATE, null, player1Values);
 			
 			// Player 2 values
 			if (players == 2) {
 				ContentValues player2Values = new ContentValues();
 				player2Values.put(Database.COLUMN_GAME_ID, gameId);
				player2Values.put(Database.COLUMN_PLAYER_ID, 0);
 				player2Values.put(Database.COLUMN_INFLUENCE, config.getAsInteger("player2"));
 				player2Values.put(Database.COLUMN_TIMESTAMP, config.getAsLong("timestamp"));
 				db.insert(Database.TABLE_GAME_STATE, null, player2Values);
 			}
 
 			db.setTransactionSuccessful();
 		} finally {
 			db.endTransaction();
 		}
 		
 		return gameId;
 	}
 	
 	
 	public int deleteGame(SQLiteDatabase db, String gameId) {
 		int rowsDeleted = 0;
 		
 		db.beginTransaction();
 		try {
 			rowsDeleted = db.delete(Database.TABLE_GAMES, Database.COLUMN__ID+"="+gameId, null);
 			db.delete(Database.TABLE_GAME_STATE, Database.COLUMN_GAME_ID+"="+gameId, null);
 			db.setTransactionSuccessful();
 		} finally {
 			db.endTransaction();
 		}
 		
 		return rowsDeleted;
 	}
 	
 	/**
 	 * Delete all games with specified number of players
 	 */
 	public int deleteGameType(SQLiteDatabase db, String players) {
 
 		int gamesDeleted = 0;
 		db.beginTransaction();
 		Cursor c = null;
 		try {
 			
 			// Find all games with the right
 			c = db.query(Database.TABLE_GAMES, 
 					new String[] {Database.COLUMN__ID}, 
 					Database.COLUMN_PLAYERS+"=?", 
 					new String[] {players}, 
 					null, 
 					null, 
 					null);
 			
 			if (c.getCount() > 0) {
 				StringBuilder sb = new StringBuilder();
 				
 				while(c.moveToNext()) {
 					sb.append(c.getInt(0) + ",");
 				}
 
 				sb.delete(sb.length()-1, sb.length());
 			
 				gamesDeleted = db.delete(Database.TABLE_GAMES, Database.COLUMN__ID+" IN (" + sb.toString() + ")", null);
 				db.delete(Database.TABLE_GAME_STATE, Database.COLUMN_GAME_ID+" IN (" + sb.toString() + ")", null);
 			}
 			
 			db.setTransactionSuccessful();
 		} finally {
 			db.endTransaction();
 			c.close();
 		}
 		
 		return gamesDeleted;
 	}
 
 	/**
 	 * Remove all games with only starting influence registered
 	 */
 	public int cleanup(SQLiteDatabase db) {
 		return 0; // Implement this if needed
 //		Cursor c = db.query(Database.TABLE_GAME_STATE, 
 //				new String[] {Database.COLUMN_GAME_ID , "COUNT(*)"}, 
 //				null, 
 //				null, 
 //				Database.COLUMN_GAME_ID, 
 //				"COUNT(*) <= 2", 
 //				Database.COLUMN_GAME_ID);
 	}
 	
 	/**
 	
 	
 
 	/***************************************************************************
 	 * Standard content provider access                                        *
 	 **************************************************************************/
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
 
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
 		String finalSortOrder = "";
 
 		switch(sUriMatcher.match(uri)) {
 		case GAMES:
 			sqlBuilder.setTables(Database.TABLE_GAMES);
 			finalSortOrder = setSortOrder(sortOrder, Database.COLUMN__ID);
 			break;
 
 		case GAME_ID:
 			sqlBuilder.setTables(Database.TABLE_GAMES);
 			finalSortOrder = null;
 			sqlBuilder.appendWhere(Database.COLUMN__ID + "=" + ContentUris.parseId(uri));
 			break;
 
 		case GAMES_MAXID:
 			return getMaxGameId(db); // Special case
 			
 		case GAMES_CHANGES:
 			sqlBuilder.setTables(Database.TABLE_GAME_STATE);
 			finalSortOrder = setSortOrder(sortOrder, Database.COLUMN_TIMESTAMP);
 			break;
 
 		case GAMES_CHANGE_ID:
 			sqlBuilder.setTables(Database.TABLE_GAME_STATE);
 			finalSortOrder = null;
 			sqlBuilder.appendWhere(Database.COLUMN__ID + "=" + ContentUris.parseId(uri));
 			break;
 
 		default:
 			throw new IllegalArgumentException("Unsupported query URI: " + uri);        
 		}   
 
 
 		Cursor c = sqlBuilder.query(
 				db, 
 				projection, 
 				selection, 
 				selectionArgs, 
 				null, 
 				null, 
 				finalSortOrder);
 
 		c.setNotificationUri(getContext().getContentResolver(), uri);
 		return c;
 	}	
 	
 	private String setSortOrder(String userSortOrder, String defaultSortOrder) {
 		if (userSortOrder == null || userSortOrder.equals("")) {
 			return defaultSortOrder;
 		} else {
 			return userSortOrder;
 		}
 	}
 	
 	@Override
 	public String getType(Uri uri) {
 		switch (sUriMatcher.match(uri)){
 		case GAMES:
 			return "vnd.android.cursor.dir/" + AUTHORITY + "." + Database.TABLE_GAMES;
 		case GAME_ID:
 			return "vnd.android.cursor.item/" + AUTHORITY + "." + Database.TABLE_GAMES;
 		case GAMES_CHANGES:
 			return "vnd.android.cursor.dir/" + AUTHORITY + "." + Database.TABLE_GAME_STATE;
 		case GAMES_CHANGE_ID:
 			return "vnd.android.cursor.item/" + AUTHORITY + "." + Database.TABLE_GAME_STATE;
 		default:
 			throw new IllegalArgumentException("Unsupported URI: " + uri);        
 		}   
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		SQLiteDatabase db = mDbHelper.getWritableDatabase();
 		long rowId = -1;
 		Uri contentUri = null;
 		
 		switch(sUriMatcher.match(uri)) {
 		case GAMES:
 			rowId = db.insert(Database.TABLE_GAMES, null, values);
 			contentUri = Uri.parse(ContentResolver.SCHEME_CONTENT +"://"+ AUTHORITY + "/" + Database.TABLE_GAMES + "/");
 			break;
 			
 		case GAMES_CHANGES:
 			rowId = db.insert(Database.TABLE_GAME_STATE, null, values);
 			contentUri = Uri.parse(ContentResolver.SCHEME_CONTENT + "://"  + AUTHORITY + "/" + Database.TABLE_GAME_STATE + "/");
 			break;
 
 		case GAMES_NEWGAME:
 			rowId = createNewGame(db, values);
 			contentUri = Uri.parse(ContentResolver.SCHEME_CONTENT +"://"+ AUTHORITY + "/" + Database.TABLE_GAMES + "/");
 			break;
 			
 		default:
 			throw new IllegalArgumentException("Unsupported insert URI: " + uri);        
 		}
 
 		// Notify that a new row has been inserted
 		if (rowId > 0) {
 	         Uri resultUri = ContentUris.withAppendedId(contentUri, rowId);
 	         getContext().getContentResolver().notifyChange(resultUri, null);    
 	         return resultUri;                
 		} 
 		
         throw new SQLException("Failed to insert row into " + uri);
 	}
 
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 		SQLiteDatabase db = mDbHelper.getWritableDatabase();
 		int rowsDeleted = 0;
 		
 		switch(sUriMatcher.match(uri)) {
 		case GAMES:
 			rowsDeleted = db.delete(Database.TABLE_GAMES, selection, selectionArgs);
 			break;
 		case GAME_ID:
 			rowsDeleted = db.delete(Database.TABLE_GAMES, Database.COLUMN__ID+"="+ContentUris.parseId(uri), null);
 			break;
 		case GAMES_DELETE:
 			rowsDeleted = deleteGame(db, selectionArgs[0]);
 			break; 
 		
 		case GAMES_DELETE_TYPE:
 			rowsDeleted = deleteGameType(db, selectionArgs[0]);
 			break;
 		
 		case GAMES_CLEANUP:
 			rowsDeleted = cleanup(db);
 			break;
 			
 		case GAMES_CHANGES:
 			rowsDeleted = db.delete(Database.TABLE_GAME_STATE, selection, selectionArgs);
 			break;
 
 		case GAMES_CHANGE_ID:
 			rowsDeleted = db.delete(Database.TABLE_GAME_STATE, Database.COLUMN__ID+"="+ContentUris.parseId(uri), null);
 			break;
 			
 		default:
 			throw new IllegalArgumentException("Unsupported delete URI: " + uri);        
 		
 		
 		}
 		
 		// Notify that a new row has been inserted
 		if (rowsDeleted > 0) {
 	         getContext().getContentResolver().notifyChange(uri, null);    
 		} 
 
         return rowsDeleted;                
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
 		SQLiteDatabase db = mDbHelper.getWritableDatabase();
 		int rowsUpdated = 0;
 		boolean notify = true;
 		
 		switch(sUriMatcher.match(uri)) {
 		case GAME_ID:
 			rowsUpdated = db.update(Database.TABLE_GAMES, values, Database.COLUMN__ID+"="+ContentUris.parseId(uri), null);
 			break;
 		case GAMES: 
 			rowsUpdated = db.update(Database.TABLE_GAMES, values, selection, selectionArgs);
 			notify = false;
 			break;
 			
 		default:
 			throw new IllegalArgumentException("Unsupported delete URI: " + uri);        
 		}
 		
 		// Notify that a new row has been inserted
 		if (rowsUpdated > 0 && notify) {
 	         getContext().getContentResolver().notifyChange(uri, null);    
 		} 
 
         return rowsUpdated;                
 	}
 }
