 /*
 	Keep Score: keep track of player scores during a card game.
 	Copyright (C) 2009 Michael Elsdörfer <http://elsdoerfer.name>
 
 	This program is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 
 	You should have received a copy of the GNU General Public License
 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.elsdoerfer.keepscore;
 
 import java.util.Date;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DbAdapter {
 
 	///////////////////////////////////////////////////////////////
 
 	public static String SESSION_TABLE = "session";
 	public static String SESSION_ID_KEY = "_id";
 	public static String SESSION_NAME_KEY = "name";
 	public static String SESSION_LAST_PLAYED_AT_KEY = "last_played_at";
 	public static String SESSION_LABEL_VKEY = "label";
 
 	public static String PLAYER_TABLE = "player";
 	public static String PLAYER_ID_KEY = "_id";
 	public static String PLAYER_SESSION_KEY = "session_id";
 	public static String PLAYER_NAME_KEY = "name";
 	public static String PLAYER_INDEX_KEY = "idx";
 
 	public static String SCORE_TABLE = "score";
 	public static String SCORE_ID_KEY = "_id";
 	public static String SCORE_SESSION_KEY = "session_id";
 	public static String SCORE_ROW_KEY = "row";
 	public static String SCORE_PLAYER_INDEX_KEY = "player_index";
 	public static String SCORE_VALUE_KEY = "value";
 	public static String SCORE_CREATED_AT_KEY = "created_at";
 
 	private static final String[] DATABASE_CREATE = {
 		"CREATE TABLE session (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
 		"                      name TEXT NOT NULL," +
 		"                      last_played_at UNSIGNED INTEGER NOT NULL);",
 
 		"CREATE TABLE player (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
 		"                     session_id INTEGER NOT NULL," +
 		"                     name TEXT NOT NULL," +
 		"                     idx INTEGER NOT NULL);",
 
 		"CREATE TABLE score (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
 		"                    session_id INTEGER NOT NULL," +
 		"                    row INTEGER NOT NULL," +
 		"                    player_index INTEGER NOT NULL," +
 		"                    value INTEGER NOT NULL," +
 		"                    created_at UNSIGNED INTEGER NOT NULL);"};
 
 	private static class DatabaseHelper extends SQLiteOpenHelper {
 
 		private static final String DATABASE_NAME = "data";
 		private static final int DATABASE_VERSION = 2;
 
 		DatabaseHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.beginTransaction();
 			try {
 				for (String statement : DATABASE_CREATE)
 					db.execSQL(statement);
 				db.setTransactionSuccessful();
 			}
 			finally {
 				db.endTransaction();
 			}
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			if (oldVersion == 1) {
 				// upgrade to version 2
 				db.execSQL("ALTER TABLE session ADD COLUMN name TEXT NOT NULL DEFAULT \"\";");
 			}
 		}
 	}
 
 
 	///////////////////////////////////////////////////////////////
 
 
 	private DatabaseHelper mDbHelper;
 	private SQLiteDatabase mDb;
 	private Context mContext;
 
 
 	public DbAdapter(Context ctx) {
 		mContext = ctx;
 	}
 
 	public DbAdapter open() throws SQLException {
 		mDbHelper = new DatabaseHelper(mContext);
 		mDb = mDbHelper.getWritableDatabase();
 		return this;
 	}
 
 	public void close() {
 		mDbHelper.close();
 	}
 
 
 	///////////////////////////////////////////////////////////////
 
 
 	public long createSession(String[] players) {
 		mDb.beginTransaction();
 		try {
 			ContentValues values = new ContentValues();
 			values.put(SESSION_LAST_PLAYED_AT_KEY, new Date().getTime());
			values.put(SESSION_NAME_KEY, "");
 			long session_id = mDb.insert(SESSION_TABLE, null, values);
 			for (int i=0; i<players.length; i++) {
 				values = new ContentValues();
 				values.put(PLAYER_SESSION_KEY, session_id);
 				values.put(PLAYER_NAME_KEY, players[i]);
 				values.put(PLAYER_INDEX_KEY, i);
 				mDb.insert(PLAYER_TABLE, null, values);
 			}
 			mDb.setTransactionSuccessful();
 			return session_id;
 		}
 		finally {
 			mDb.endTransaction();
 		}
 	}
 
 	public Cursor fetchAllSessions() {
 		// TODO: this should probably use the table and column name constants.
 		return mDb.rawQuery(
 				"SELECT _id, IFNULL(NULLIF(session_name, ''), "+
 				"       GROUP_CONCAT(player_name, ', ')) AS label, last_played_at "+
 				"FROM (SELECT session._id AS _id, session.name as session_name, "+
 				"             last_played_at, player.name AS player_name "+
 				"      FROM session "+
 				"      LEFT OUTER JOIN player ON session._id = player.session_id "+
 				"      ORDER BY player.idx ASC) " +
 				"GROUP BY _id "+
 				"ORDER BY last_played_at DESC", null);
 	}
 
 	public String[] fetchSessionPlayerNames(long sessionId) {
 		Cursor cursor = mDb.query(PLAYER_TABLE, new String[]{PLAYER_NAME_KEY},
 				PLAYER_SESSION_KEY + "= ?", new String[]{String.valueOf(sessionId)},
 				null, null, PLAYER_INDEX_KEY + " ASC");
 		try {
 			String[] result = new String[cursor.getCount()];
 			cursor.moveToFirst();
 			do {
 				result[cursor.getPosition()] = cursor.getString(0);
 			} while (cursor.moveToNext());
 			return result;
 		}
 		finally {
 			cursor.close();
 		}
 	}
 
 	public boolean updateSessionTimestamp(long sessionId) {
 		ContentValues args = new ContentValues();
 		args.put(SESSION_LAST_PLAYED_AT_KEY, new Date().getTime());
 		return mDb.update(SESSION_TABLE, args, SESSION_ID_KEY + "=" + sessionId, null) > 0;
 	}
 
 	/**
 	 * Assign a manual session name.
 	 *
 	 * @param sessionId
 	 * @param newLabel
 	 */
 	public boolean setSessionName(long sessionId, String newLabel) {
 		ContentValues args = new ContentValues();
 		args.put(SESSION_NAME_KEY, newLabel);
 		return mDb.update(SESSION_TABLE, args, SESSION_ID_KEY + "=" + sessionId, null) > 0;
 	}
 
 	public String getSessionName(long sessionId) {
 		Cursor cursor = mDb.query(SESSION_TABLE, new String[]{SESSION_NAME_KEY},
 				SESSION_ID_KEY+"=?", new String[]{String.valueOf(sessionId)},
 				null, null, null);
 		cursor.moveToFirst();
 		return cursor.getString(0);
 	}
 
 	public void addSessionScores(long sessionId, Integer[] scores) {
 		// We trust that "scores" has the right size for this
 		// session, and that the corresponding player "index"
 		// values in the session go from 0 to [length].
 		mDb.beginTransaction();
 		try {
 			Cursor cursor = mDb.query(SCORE_TABLE, new String[] {"MAX("+SCORE_ROW_KEY+")"},
 					PLAYER_SESSION_KEY + "= ?", new String[]{String.valueOf(sessionId)},
 					null, null, null);
 			int nextRowNum;
 			try {
 				cursor.moveToFirst();
 				nextRowNum = cursor.getInt(0) + 1;
 			}
 			finally {
 				cursor.close();
 			}
 			for (int i=0; i<scores.length; i++) {
 				ContentValues values = new ContentValues();
 				values.put(SCORE_SESSION_KEY, sessionId);
 				values.put(SCORE_ROW_KEY, nextRowNum);
 				values.put(SCORE_PLAYER_INDEX_KEY, i);
 				values.put(SCORE_VALUE_KEY, scores[i]);
 				values.put(SCORE_CREATED_AT_KEY, new Date().getTime());
 				mDb.insert(SCORE_TABLE, null, values);
 			}
 			mDb.setTransactionSuccessful();
 		}
 		finally {
 			mDb.endTransaction();
 		}
 	}
 
 	public boolean removeSessionScores(long sessionId, Integer rowNum) {
 		return mDb.delete(SCORE_TABLE, SCORE_SESSION_KEY + "= ? AND " + SCORE_ROW_KEY + " = ?",
 				new String[]{String.valueOf(sessionId), String.valueOf(rowNum)})>0;
 	}
 
 	public Cursor fetchSessionScores(long sessionId) {
 		return mDb.query(SCORE_TABLE, new String[]{SCORE_VALUE_KEY},
 				SCORE_SESSION_KEY + "= ?", new String[]{String.valueOf(sessionId)},
 				null, null, SCORE_ROW_KEY + " ASC, " + SCORE_PLAYER_INDEX_KEY + " ASC");
 	}
 
 	public void deleteSession(long sessionId) {
 		mDb.beginTransaction();
 		try {
 			int rc = mDb.delete(SESSION_TABLE, SESSION_ID_KEY + " = " + sessionId, null);
 			assert rc > 0;
 			// Merging the previous two lines causes the session not be
 			// deleted, without the assert failing? I'd guess asserts are
 			// removed/ignored in production, but it even happened during
 			// debugging (i think)...
 
 			mDb.delete(PLAYER_TABLE, PLAYER_SESSION_KEY + " = " + sessionId, null);
 			mDb.delete(SCORE_TABLE, SCORE_SESSION_KEY + " = " + sessionId, null);
 			mDb.setTransactionSuccessful();
 		}
 		finally {
 			mDb.endTransaction();
 		}
 	}
 
 	public void clearSessions() {
 		mDb.beginTransaction();
 		try {
 			mDb.delete(SCORE_TABLE, null, null);
 			mDb.delete(PLAYER_TABLE, null, null);
 			mDb.delete(SESSION_TABLE, null, null);
 			mDb.setTransactionSuccessful();
 		}
 		finally {
 			mDb.endTransaction();
 		}
 	}
 }
