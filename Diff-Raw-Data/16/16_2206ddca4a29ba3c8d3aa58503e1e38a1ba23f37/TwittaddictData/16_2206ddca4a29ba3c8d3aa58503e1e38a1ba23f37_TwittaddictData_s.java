 package com.rushdevo.twittaddict.data;
 
 import static android.provider.BaseColumns._ID;
 import static com.rushdevo.twittaddict.data.FriendStats.FRIEND_STATS_ATTEMPTS;
 import static com.rushdevo.twittaddict.data.FriendStats.FRIEND_STATS_CORRECT;
 import static com.rushdevo.twittaddict.data.FriendStats.FRIEND_STATS_FRIEND;
 import static com.rushdevo.twittaddict.data.FriendStats.FRIEND_STATS_PERCENT;
 import static com.rushdevo.twittaddict.data.FriendStats.FRIEND_STATS_TABLE_NAME;
 import static com.rushdevo.twittaddict.data.FriendStats.FRIEND_STATS_USER;
 import static com.rushdevo.twittaddict.data.HighScoreColumns.HIGH_SCORES_MODE;
 import static com.rushdevo.twittaddict.data.HighScoreColumns.HIGH_SCORES_SCORE;
 import static com.rushdevo.twittaddict.data.HighScoreColumns.HIGH_SCORES_TABLE_NAME;
 import static com.rushdevo.twittaddict.data.HighScoreColumns.HIGH_SCORES_TIMESTAMP;
 import static com.rushdevo.twittaddict.data.HighScoreColumns.HIGH_SCORES_USER;
 import static com.rushdevo.twittaddict.data.UserColumns.USERS_TABLE_NAME;
 import static com.rushdevo.twittaddict.data.UserColumns.USERS_TOKEN;
 import static com.rushdevo.twittaddict.data.UserColumns.USERS_TOKEN_SECRET;
 
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 import com.rushdevo.twittaddict.Game;
 import com.rushdevo.twittaddict.constants.Twitter;
 import com.rushdevo.twittaddict.exceptions.TwitterCommunicationException;
 import com.rushdevo.twittaddict.exceptions.TwitterException;
 import com.rushdevo.twittaddict.exceptions.TwitterOAuthException;
 import com.rushdevo.twittaddict.twitter.TwitterUser;
 
 public class TwittaddictData extends SQLiteOpenHelper {
 	private static final String DATABASE_NAME = "twittaddict.db";
 	private static final int DATABASE_VERSION = 2;
 	
 	private static final String[] USER_COLUMNS = { _ID, USERS_TOKEN, USERS_TOKEN_SECRET };
 	private static final String[] FRIEND_STAT_COLUMNS = { _ID, FRIEND_STATS_USER, FRIEND_STATS_FRIEND, FRIEND_STATS_ATTEMPTS, FRIEND_STATS_CORRECT, FRIEND_STATS_PERCENT };
 	private static final String[] HIGH_SCORE_COLUMNS = { _ID, HIGH_SCORES_USER, HIGH_SCORES_MODE, HIGH_SCORES_SCORE, HIGH_SCORES_TIMESTAMP };
 	
 	private Activity ctx;
 
 	public TwittaddictData(Context ctx) {
 		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
 		this.ctx = (Activity)ctx;
 	}
 	
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		// Users Table
 		StringBuilder sql = new StringBuilder();
 		sql.append("CREATE TABLE " + USERS_TABLE_NAME + " (");
 		sql.append(_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
 		sql.append(USERS_TOKEN_SECRET + " TEXT, ");
 		sql.append(USERS_TOKEN + " TEXT");
 		sql.append(");");
 		db.execSQL(sql.toString());
 		// High Scores Table
 		sql = new StringBuilder();
 		sql.append("CREATE TABLE " + HIGH_SCORES_TABLE_NAME + " (");
 		sql.append(_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
 		sql.append(HIGH_SCORES_USER + " INTEGER, ");
 		sql.append(HIGH_SCORES_MODE + " TEXT, ");
 		sql.append(HIGH_SCORES_SCORE + " INTEGER, ");
 		sql.append(HIGH_SCORES_TIMESTAMP + " INTEGER");
 		sql.append(");");
 		db.execSQL(sql.toString());
 		// Friend Stats Table
 		sql = new StringBuilder();
 		sql.append("CREATE TABLE " + FRIEND_STATS_TABLE_NAME + " (");
 		sql.append(_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
 		sql.append(FRIEND_STATS_USER + " INTEGER, ");
 		sql.append(FRIEND_STATS_FRIEND + " INTEGER, ");
 		sql.append(FRIEND_STATS_ATTEMPTS + " INTEGER, ");
 		sql.append(FRIEND_STATS_CORRECT + " INTEGER, ");
 		sql.append(FRIEND_STATS_PERCENT + " REAL");
 		sql.append(");");
 		db.execSQL(sql.toString());
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// TODO: This should be smarter about migrations
 		db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE_NAME);
 		db.execSQL("DROP TABLE IF EXISTS " + HIGH_SCORES_TABLE_NAME);
 		db.execSQL("DROP TABLE IF EXISTS " + FRIEND_STATS_TABLE_NAME);
 		onCreate(db);
 	}
 	
 	public Cursor getUsers() {
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor cursor = db.query(USERS_TABLE_NAME, USER_COLUMNS, null, null, null, null, null);
 		ctx.startManagingCursor(cursor);
 		return cursor;
 	}
 	
 	public Integer getLoggedInUserId() {
 		// For now, it is assumed that the user table is a singleton table
 		Cursor cursor = getUsers();
 		if (cursor.moveToNext()) {
 			return cursor.getInt(0);
 		}
 		return null;
 	}
 	
 	public Integer addOrUpdateUser(String token, String tokenSecret) {
 		Integer id = getLoggedInUserId();
 		if (id == null) {
 			return addUser(token, tokenSecret);
 		} else {
 			updateUser(id, token, tokenSecret);
 			return id;
 		}
 	}
 	
 	public Integer addUser(String token, String tokenSecret) {
 		SQLiteDatabase db = getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put(USERS_TOKEN, token);
 		values.put(USERS_TOKEN_SECRET, tokenSecret);
 		try {
 			return (int)db.insertOrThrow(USERS_TABLE_NAME, null, values);
 		} catch (SQLException e) {
 			return -1;
 		}
 	}
 	
 	public void updateUser(Integer id, String token, String tokenSecret) {
 		SQLiteDatabase db = getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put(USERS_TOKEN, token);
 		values.put(USERS_TOKEN_SECRET, tokenSecret);
 		String whereClause = _ID + " = ?";
 		String[] whereArgs = { id.toString() };
 		db.update(USERS_TABLE_NAME, values, whereClause, whereArgs);
 	}
 		
 	public Cursor getFriendStat(TwitterUser user) {
 		SQLiteDatabase db = getReadableDatabase();
 		String selection = FRIEND_STATS_FRIEND + " = ?";
 		String[] selectionArgs = { user.getId().toString() }; 
 		Cursor cursor = db.query(FRIEND_STATS_TABLE_NAME, FRIEND_STAT_COLUMNS, selection, selectionArgs, null, null, null);
 		ctx.startManagingCursor(cursor);
 		return cursor;
 	}
 	
 	public Cursor getFriendStats() {
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor cursor = db.query(FRIEND_STATS_TABLE_NAME, FRIEND_STAT_COLUMNS, null, null, null, null, null);
 		ctx.startManagingCursor(cursor);
 		return cursor;
 	}
 	
 	public Long getBFFId() {
 		Long id = null;
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor cursor = db.query(FRIEND_STATS_TABLE_NAME, FRIEND_STAT_COLUMNS, null, null, null, null, (FRIEND_STATS_PERCENT + " DESC"), "1");
 		if (cursor.moveToNext()) {
 			// Find everyone with this percent correct and then choose a random one
 			Integer percentCorrect = cursor.getInt(5);
 			String selection = FRIEND_STATS_PERCENT + " = ?";
 			String[] selectionArgs = { percentCorrect.toString() };
			cursor = db.query(FRIEND_STATS_TABLE_NAME, FRIEND_STAT_COLUMNS, selection, selectionArgs, null, null, null);
 			// If there are more than one, return a random one
 			int index = new Random().nextInt(cursor.getCount());
			while (cursor.moveToNext()) {
 				// Return the id of the matching user
				if (cursor.getPosition() == index) id = cursor.getLong(2);
 			}
 		}
 		cursor.close();
 		return id;
 	}
 	
 	public TwitterUser getBFF(Game game) {
 		Long id = getBFFId();
 		// Try to get it from the list of current friends
 		TwitterUser bff = game.getFriendById(id);
 		if (bff == null) {
 			// If they are no longer friends with this person, get them from Twitter
 			try {
 				bff = Twitter.getUserById(id, ctx);
 			} catch (TwitterException e) {
 				e.printStackTrace();
 			} catch (TwitterOAuthException e) {
 				e.printStackTrace();
 			} catch (TwitterCommunicationException e) {
 				e.printStackTrace();
 			}
 		}
 		return bff;
 	}
 	
 	public void updateFriendStat(Long id, Integer currentAttempts, Integer currentCorrect, Boolean isCorrect) {
 		Integer newCorrect = (isCorrect) ? currentCorrect : currentCorrect + 1;
 		Integer newAttempts = currentAttempts + 1;
 		Float newPercent = newCorrect.floatValue() / newAttempts.floatValue();
 		SQLiteDatabase db = getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put(FRIEND_STATS_ATTEMPTS, newAttempts);
 		values.put(FRIEND_STATS_CORRECT, newCorrect);
 		values.put(FRIEND_STATS_PERCENT, newPercent);
 		try {
 			String whereClause = _ID + " = ?";
 			String[] whereArgs = { id.toString() };
 			db.update(FRIEND_STATS_TABLE_NAME, values, whereClause, whereArgs);
 		} catch (SQLException e) {
 			// NOOP
 		}
 	}
 	
 	public void createFriendStat(TwitterUser friend, Integer userId, Boolean isCorrect) {
 		SQLiteDatabase db = getWritableDatabase();
 		// Then add the new one
 		ContentValues values = new ContentValues();
 		values.put(FRIEND_STATS_USER, userId);
 		values.put(FRIEND_STATS_FRIEND, friend.getId());
 		values.put(FRIEND_STATS_ATTEMPTS, 1);
 		if (isCorrect) {
 			values.put(FRIEND_STATS_CORRECT, 1);
 			values.put(FRIEND_STATS_PERCENT, 1.0);
 		} else {
 			values.put(FRIEND_STATS_CORRECT, 0);
 			values.put(FRIEND_STATS_PERCENT, 0.0);
 		}
 		try {
 			db.insertOrThrow(FRIEND_STATS_TABLE_NAME, null, values);
 		} catch (SQLException e) {
 			// NOOP
 		}
 	}
 	
 	public void saveHighScore(Integer userId, Integer score) {
 		SQLiteDatabase db = getWritableDatabase();
 		// Then add the new one
 		ContentValues values = new ContentValues();
 		values.put(HIGH_SCORES_MODE, "MATCH");
 		values.put(HIGH_SCORES_SCORE, score);
 		values.put(HIGH_SCORES_USER, userId);
 		values.put(HIGH_SCORES_TIMESTAMP, System.currentTimeMillis());
 		try {
 			db.insertOrThrow(HIGH_SCORES_TABLE_NAME, null, values);
 		} catch (SQLException e) {
 			// NOOP
 		}
 	}
 	
 	public Cursor getHighScores() {
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor cursor = db.query(HIGH_SCORES_TABLE_NAME, HIGH_SCORE_COLUMNS, null, null, null, null, "score DESC", "5");
 		ctx.startManagingCursor(cursor);
 		return cursor;
 	}
 	
 	public void truncateUsersTable(SQLiteDatabase db) {
 		db.execSQL("DELETE FROM " + USERS_TABLE_NAME);
 	}
 
 }
