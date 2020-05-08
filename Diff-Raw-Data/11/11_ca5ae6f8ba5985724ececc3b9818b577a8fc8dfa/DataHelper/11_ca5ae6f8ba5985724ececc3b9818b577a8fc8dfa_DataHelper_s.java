 package org.ags.lparchive;
 
 import org.ags.lparchive.LPArchiveApplication.LPTypes;
 import org.ags.lparchive.model.Chapter;
 import org.ags.lparchive.model.LetsPlay;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 import android.util.Log;
 
 /**
  * Handles data storage / retrieval operations for the application.
  */
 public class DataHelper {
 	private static final String TAG = "DataHelper";
 	
 	private static final String DATABASE_NAME = "lparchive.db";
 	private static final int DATABASE_VERSION = 1;
 	
 	// tables
 	public static final String TABLE_ARCHIVE = "archive";
 	public static final String TABLE_TAGS = "tags";
 	public static final String TABLE_CHAPTERS = "chapters";
 	public static final String TABLE_LATEST = "latest";
 	public static final String TABLE_FAVORITES = "favorites";
 	
 	// keys
 	public static final String KEY_ID = "_id"; // 0
 	public static final int INDEX_ID = 0;
 	
 	public static final String KEY_ARCHIVE_GAME = "game"; // 1
 	public static final int INDEX_ARCHIVE_GAME = 1;
 	public static final String KEY_ARCHIVE_AUTHOR = "author"; // 2 
 	public static final int INDEX_ARCHIVE_AUTHOR = 2;
 	public static final String KEY_ARCHIVE_URL = "url"; // 3
 	public static final int INDEX_ARCHIVE_URL = 3;
 	public static final String KEY_ARCHIVE_TYPE = "type"; // 4
 	public static final int INDEX_ARCHIVE_TYPE = 4;
 	
 	public static final String KEY_LATEST_LP_ID = "lpId"; // 1
 	public static final int INDEX_LATEST_LP_ID = 1;
 	
 	
 	public static final String KEY_TAG_LP_ID = "lpId"; // 1
 	public static final int INDEX_TAG_LP_ID = 1;
 	public static final String KEY_TAG = "tag"; // 2
 	public static final int INDEX_TAG = 2;
 	
 	public static final String KEY_CHAPTER_LP_ID = "lpId"; // 1
 	public static final int INDEX_CHAPTER_LP_ID = 1;
 	public static final String KEY_CHAPTER_URL = "url"; // 2
 	public static final int INDEX_CHAPTER_URL = 2;
 	public static final String KEY_CHAPTER_TITLE = "title"; // 3
 	public static final int INDEX_CHAPTER_TITLE = 3;
 	
 	public static final String KEY_FAVS_LP_ID = "lpId"; // 1
 	public static final int INDEX_FAVS_LP_ID = 1;
 	
 	// sorting
 	public static final String SORT_GAME_ASC = String.format("%s asc", 
 			KEY_ARCHIVE_GAME);
 	
 	// schema
 	private static final String SCHEMA_TABLE_ARCHIVE = String.format(
 		"CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s TEXT, %s TEXT, %s TEXT, " +
 		"%s INTEGER)", TABLE_ARCHIVE, KEY_ID, KEY_ARCHIVE_GAME, KEY_ARCHIVE_AUTHOR, 
 		KEY_ARCHIVE_URL, KEY_ARCHIVE_TYPE);
 	
 	private static final String SCHEMA_TABLE_TAGS = String.format(
 		"CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s INTEGER, %s TEXT)",
 		TABLE_TAGS, KEY_ID, KEY_TAG_LP_ID, KEY_TAG);
 	
 	private static final String SCHEMA_TABLE_CHAPTERS = String.format(
 		"CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s INTEGER, %s TEXT, %s TEXT)",
 		TABLE_CHAPTERS, KEY_ID, KEY_CHAPTER_LP_ID, KEY_CHAPTER_URL, 
 		KEY_CHAPTER_TITLE);
 	
 	private static final String SCHEMA_TABLE_LATEST = String.format(
 		"CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s INTEGER)",
 		TABLE_LATEST, KEY_ID, KEY_LATEST_LP_ID);
 			
 	private static final String SCHEMA_TABLE_FAVORITES = String.format(
 		"CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s INTEGER)",
 		TABLE_FAVORITES, KEY_ID, KEY_FAVS_LP_ID);
 	
 	// projections
 	private static final String[] projectArchive = new String[] { 
 		KEY_ID, 
 		KEY_ARCHIVE_GAME, 
 		KEY_ARCHIVE_AUTHOR, 
 		KEY_ARCHIVE_URL, 
 		KEY_ARCHIVE_TYPE 
 	};
 	
 	private static final String[] projectChapter = new String[] { 
 		KEY_ID, 
 		KEY_CHAPTER_LP_ID, 
 		KEY_CHAPTER_URL, 
 		KEY_CHAPTER_TITLE 
 	};
 	
 	private static final String[] projectFavorite = new String[] {
 		KEY_ID, 
 		KEY_FAVS_LP_ID 
 	};
 	
 	// selections
 	private static final String SELECTION_RECENT_LP = String.format(
 		"%s=? AND %s=?", KEY_ARCHIVE_GAME, KEY_ARCHIVE_AUTHOR);
 	
 	private static final String SELECTION_CHAPTER = KEY_CHAPTER_LP_ID+"=?";
 	
 	private static final String SELECTION_LP_NAME_SEARCH = KEY_ARCHIVE_GAME + 
 		" LIKE ?";
 	
 	private static final String SELECTION_FAVORITE = KEY_FAVS_LP_ID+"=?";
 	
 	// prepared statments
 	private SQLiteStatement insertLpStmnt;
 	private static final String INSERT_LP = String.format(
 		"insert into %s (%s, %s, %s, %s) values (?, ?, ?, ?)", 
 		TABLE_ARCHIVE, KEY_ARCHIVE_GAME, KEY_ARCHIVE_AUTHOR, 
 		KEY_ARCHIVE_URL, KEY_ARCHIVE_TYPE);
 
 	private SQLiteStatement insertTagStmnt;
 	private static final String INSERT_TAG = String.format(
 		"insert into %s (%s, %s) values (?, ?)", 
 		TABLE_TAGS, KEY_TAG_LP_ID, KEY_TAG);
 	
 	private SQLiteStatement insertChapterStmnt;
 	private static final String INSERT_CHAPTER = String.format(
 		"insert into %s (%s, %s, %s) values (?, ?, ?)", 
 		TABLE_CHAPTERS, KEY_CHAPTER_LP_ID, KEY_CHAPTER_URL, KEY_CHAPTER_TITLE);
 	
 	private SQLiteStatement insertFavStmnt;
 	private static final String INSERT_FAV = String.format(
 		"insert into %s (%s) values (?)", TABLE_FAVORITES, KEY_FAVS_LP_ID);
 	
 	private SQLiteStatement deleteFavStmnt;
 	private static final String DELETE_FAV = String.format(
 		"delete from %s WHERE %s = ?", TABLE_FAVORITES, KEY_FAVS_LP_ID);
 	
 	private SQLiteStatement latestLpStmnt;
 	private static final String INSERT_LATEST = String.format(
 		"insert into %s (%s) values (?)", TABLE_LATEST, KEY_LATEST_LP_ID);
 	
 	// archive project as string for convenience
 	private static final String ARCHIVE_PROJECT = String.format(
 		"%s.%s, %s.%s, %s.%s, %s.%s, %s.%s",
 		TABLE_ARCHIVE, KEY_ID, 
 		TABLE_ARCHIVE, KEY_ARCHIVE_GAME, 
 		TABLE_ARCHIVE, KEY_ARCHIVE_AUTHOR,
 		TABLE_ARCHIVE, KEY_ARCHIVE_URL,
 		TABLE_ARCHIVE, KEY_ARCHIVE_TYPE);
 	
 	// joins
 	private static final String LATEST_JOIN = String.format(
 		"SELECT %s FROM %s, %s WHERE (%s.%s = %s.%s) ORDER BY %s",
 		ARCHIVE_PROJECT, TABLE_ARCHIVE, TABLE_LATEST, TABLE_ARCHIVE, 
 		KEY_ID, TABLE_LATEST, KEY_LATEST_LP_ID, SORT_GAME_ASC);
 	
 	private static final String RECENT_JOIN_FILTERED = String.format(
 		"SELECT %s FROM %s, %s WHERE (%s.%s = %s.%s) AND %s.%s LIKE ? " +
 		"ORDER BY %s",
 		ARCHIVE_PROJECT, TABLE_ARCHIVE, TABLE_LATEST, TABLE_ARCHIVE, 
 		KEY_ID, TABLE_LATEST, KEY_LATEST_LP_ID, TABLE_ARCHIVE, 
 		KEY_ARCHIVE_GAME, SORT_GAME_ASC);
 	
 	private static final String FAV_JOIN = String.format(
 		"SELECT %s FROM %s, %s WHERE (%s.%s = %s.%s) ORDER BY %s",
 		ARCHIVE_PROJECT, TABLE_ARCHIVE, TABLE_FAVORITES, TABLE_ARCHIVE, 
 		KEY_ID, TABLE_FAVORITES, KEY_FAVS_LP_ID, SORT_GAME_ASC);
 	
 	private static final String TAG_SEARCH = String.format(
 		"SELECT %s, %s.%s FROM %s, %s WHERE %s.%s = %s.%s AND" +
 		" %s.%s = ? GROUP BY %s.%s ORDER BY %s",
 		ARCHIVE_PROJECT, TABLE_TAGS, KEY_TAG, TABLE_ARCHIVE, TABLE_TAGS,
 		TABLE_ARCHIVE, KEY_ID, TABLE_TAGS, KEY_TAG_LP_ID,
 		TABLE_TAGS, KEY_TAG, TABLE_ARCHIVE, KEY_ID, SORT_GAME_ASC); 
 	
 	private SQLiteDatabase db;
 
 	public DataHelper(Context context) {
 		OpenHelper openHelper = new OpenHelper(context);
 		db = openHelper.getWritableDatabase();
 
 		insertLpStmnt = db.compileStatement(INSERT_LP);
 		latestLpStmnt = db.compileStatement(INSERT_LATEST);
 		insertTagStmnt = db.compileStatement(INSERT_TAG);
 		insertChapterStmnt = db.compileStatement(INSERT_CHAPTER);
 		insertFavStmnt = db.compileStatement(INSERT_FAV);
 		deleteFavStmnt = db.compileStatement(DELETE_FAV);
 	}
 	
 	/** Returns the database. Preferred access is through helper methods. */
 	public SQLiteDatabase getDb() {
 		return this.db;
 	}
 	
 	/**
 	 * Inserts a Let's Play with given attributes into the DB. If a LP with the
 	 * same game name / author exists, returns its ID.
 	 * 
 	 * @return Inserted records LP ID, or -1 on failure.
 	 * @throws DuplicateLPException
 	 *             If the inserted game/author attributes match an existing
 	 *             record.
 	 */
 	public long insertLetsPlay(String game, String author, String url, LPTypes type) 
 		throws DuplicateLPException {
 		long id = getID(game, author);
 		if(id != -1) 
 			throw new DuplicateLPException(id);
 		insertLpStmnt.bindString(INDEX_ARCHIVE_GAME, game);
 		insertLpStmnt.bindString(INDEX_ARCHIVE_AUTHOR, author);
 		insertLpStmnt.bindString(INDEX_ARCHIVE_URL, url);
 		insertLpStmnt.bindLong(INDEX_ARCHIVE_TYPE, type.ordinal());
 		return insertLpStmnt.executeInsert();
 	}
 	
 	/**
 	 * Given a game and author, attempts to retrieve LP ID.
 	 * 
 	 * @return ID of LP or -1 if no matches are found.
 	 */
 	public long getID(String game, String author) {
 		Cursor cursor = this.db.query(TABLE_ARCHIVE, new String[] { KEY_ID },
 				SELECTION_RECENT_LP, new String[] { game, author }, 
 				null, null, null);
 		long id = -1;
 		if (cursor.moveToFirst())
 			id = cursor.getInt(INDEX_ID);
 		if (cursor != null && !cursor.isClosed())
 			cursor.close();
 		return id;
 	}
 	
 	/** 
 	 * Marks a given LP as recent addition to the archive.
 	 * @return Row ID on success, -1 otherwise.
 	 */
 	public long markRecentLetsPlay(long id) {
 		latestLpStmnt.bindLong(1, id);
 		return latestLpStmnt.executeInsert();
 	}
 	
 	/**
 	 * Adds the given tag to the LP with the given ID.
 	 */
 	public void addTag(long lp_id, String tag) {
 		insertTagStmnt.bindLong(INDEX_TAG_LP_ID, lp_id);
 		insertTagStmnt.bindString(INDEX_TAG, tag);
 		insertTagStmnt.executeInsert();
 	}
 
 	/**
 	 * @return A Cursor to access the LP data of all latest LPs
 	 */
 	public Cursor getLatestLPs() {
 		// joins don't appear to work unless using raw query
 		return db.rawQuery(LATEST_JOIN, null);
 	}
 	
 	/** 
 	 * @return A Cursor to access all LP data
 	 */
 	public Cursor getArchive() {
 		return db.query(TABLE_ARCHIVE, projectArchive, null,
 				null, null, null, SORT_GAME_ASC);
 	}
 	
 	/**
 	 * Given an LP ID, returns all Chapters associated with it.
 	 * 
 	 * @param lpId
 	 *            The LP to retrieve chapters for
 	 * @return Cursor to Chapter data for given LP.
 	 */
 	public Cursor getChapters(long lpId) {
 		return this.db.query(TABLE_CHAPTERS, projectChapter, SELECTION_CHAPTER,
 				new String[] { String.valueOf(lpId) }, null, null, null);
 	}
 
 	/**
 	 * Inserts a chapter with given attributes for the given LP.
 	 * 
 	 * @return ID of chapter on success, -1 otherwise.
 	 */
 	public long insertChapter(long lpId, String url, String title) {
 		insertChapterStmnt.bindLong(INDEX_CHAPTER_LP_ID, lpId);
 		insertChapterStmnt.bindString(INDEX_CHAPTER_URL, url);
 		insertChapterStmnt.bindString(INDEX_CHAPTER_TITLE, title);
 		return insertChapterStmnt.executeInsert();
 	}
 	
 	/**
 	 * @return LetsPlay matching the given ID or null if none found. 
 	 */
 	public LetsPlay getLP(long id) {
 		Cursor cursor = db.query(TABLE_ARCHIVE, projectArchive, "_id=?", 
 				new String[] { String.valueOf(id)}, null, null, null);
 		LetsPlay lp = null;
 		LPTypes[] types = LPTypes.values();
 		if (cursor.moveToFirst()) {
 			do {
 				lp = new LetsPlay(cursor.getLong(INDEX_ID), 
 						cursor.getString(INDEX_ARCHIVE_GAME), 
 						cursor.getString(INDEX_ARCHIVE_AUTHOR),
 						cursor.getString(INDEX_ARCHIVE_URL), 
 						types[cursor.getInt(INDEX_ARCHIVE_TYPE)]);
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();
 		}
 		return lp;
 	}
 	
 	/**
 	 * @return Chapter matching the given ID or null if none found.
 	 */
 	public Chapter getChapter(long id) {
 		Cursor cursor = db.query(TABLE_CHAPTERS, projectChapter, "_id=?", 
 				new String[] { String.valueOf(id)}, null, null, null);
 		Chapter c = null;
 		if (cursor.moveToFirst()) {
 			do {
				c = new Chapter(cursor.getString(INDEX_CHAPTER_TITLE), 
						cursor.getString(INDEX_CHAPTER_URL));
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();
 		}
 		return c;
 	}
 	
 	/**
 	 * Searches archive LPs for games containing a given name.
 	 * 
 	 * @param name
 	 *            Query on 'game' attribute.
 	 * @return Cursor for matching records. This may be empty.
 	 */
 	public Cursor lpNameSearch(String name) {
         String[] args = new String[] { "%" + name + "%"};
         return db.query(DataHelper.TABLE_ARCHIVE, DataHelper.projectArchive,
         		SELECTION_LP_NAME_SEARCH, args, null, null, SORT_GAME_ASC);
 	}
 	
 	/**
 	 * Searches latest LPs for games containing a given name.
 	 * 
 	 * @param name
 	 *            Query on 'game' attribute.
 	 * @return Cursor for matching records. This may be empty.
 	 */
 	public Cursor lpLatestNameSearch(String name) {
 		String[] args = new String[] { "%" + name + "%" };
 		return db.rawQuery(RECENT_JOIN_FILTERED, args);
 	}
 	
 	/**
 	 * Searches archive LPs for games with a given tag.
 	 * 
 	 * @param tag
 	 *            Tag to match on.
 	 * @return Cursor for matching records. This may be empty.
 	 */
 	public Cursor tagSearch(String tag) {
 		String[] args = new String[] { tag };
 		return db.rawQuery(TAG_SEARCH, args);
 	}
 
 	/**
 	 * Given an ID, will mark the corresponding LP as a favorite if it isn't, or
 	 * unmark if it already is.
 	 * 
 	 * @param id
 	 *            LP to mark/unmark.
 	 */
 	public void toggleFavoriteLP(long id) {
 		if (isFavoriteLP(id)) {
 			deleteFavStmnt.bindLong(INDEX_FAVS_LP_ID, id);
 			// executeUpdateDelete is API level 11, targeting 7
 			deleteFavStmnt.execute();
 		} else {
 			insertFavStmnt.bindLong(INDEX_FAVS_LP_ID, id);
 			insertFavStmnt.executeInsert();
 		}
 	}
 	
 	/** Returns true if the LP with given ID is marked a favorite. */
 	public boolean isFavoriteLP(long id) {
 		Cursor cursor = this.db.query(TABLE_FAVORITES, projectFavorite, 
 				SELECTION_FAVORITE, new String[] { String.valueOf(id)}, 
 				null, null, null);
 		return cursor.moveToFirst();
 	}
 	
 	/** Returns a Cursor to the archive attributes of favorite LPs */ 
 	public Cursor getFavoriteLPs() {
 		return this.db.rawQuery(FAV_JOIN, null);
 	}
 	
 	/** Clears the list of latest LPs */
 	public void clearLatest() {
 		db.execSQL("DELETE FROM " + TABLE_LATEST);
 	}
 	
 	/**
 	 * Assists in the creation/modification the SQLite DB structure.
 	 */
 	private static class OpenHelper extends SQLiteOpenHelper {
 
 		OpenHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(SCHEMA_TABLE_ARCHIVE);
 			db.execSQL(SCHEMA_TABLE_TAGS);
 			db.execSQL(SCHEMA_TABLE_CHAPTERS);
 			db.execSQL(SCHEMA_TABLE_LATEST);
 			db.execSQL(SCHEMA_TABLE_FAVORITES);
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.w(TAG, "upgrading database - dropping tables");
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARCHIVE);
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAGS);
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAPTERS);
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_LATEST);
 			onCreate(db);
 		}
 	}
 
 	@SuppressWarnings("serial")
 	public class DuplicateLPException extends Exception {
 		DuplicateLPException(long id) {
 			super(id + " already exists");
 		}
 	}
 }
