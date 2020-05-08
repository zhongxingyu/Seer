 package com.rushdevo.twittaddict.db;
 
 import static android.provider.BaseColumns._ID;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 
 import com.rushdevo.twittaddict.db.model.User;
 
 public class UserDataSource {
 	public static final String USERS_TABLE_NAME = "users";
 	
	public static final String USERS_TOKEN = "token";
 	public static final String USERS_TOKEN_SECRET = "token_secret";
 	
	public static final String[] USER_COLUMNS = { _ID, USERS_TOKEN, USERS_TOKEN_SECRET };
 	
 	public static final int ID_INDEX = 0;
 	public static final int TOKEN_SECRET_INDEX = 1;
 	public static final int TOKEN_INDEX = 2;
 	
 	private TwittaddictDatabase dbHelper;
 	private SQLiteDatabase database;
 	
 	public UserDataSource(Context context) {
 		dbHelper = new TwittaddictDatabase(context);
 	}
 	
 	public void open() throws SQLException {
 		database = dbHelper.getWritableDatabase();
 	}
 	
 	public void close() throws SQLException {
 		dbHelper.close();
 	}
 	
 	/**
 	 * Gets the singleton user record
 	 * 
 	 * @return The User object from the first record in the users table, or null if none exists
 	 */
 	public User getUser() {
 		Cursor cursor = database.query(USERS_TABLE_NAME, USER_COLUMNS, null, null, null, null, null);
 		cursor.moveToFirst();
 		User user = null;
 		if (!cursor.isAfterLast()) user = cursorToUser(cursor);
 		cursor.close();
 		return user;
 	}
 	
 	/**
 	 * Saves a new user to the database. If the user is not
 	 * a new record, delegates to updateUser(user)
 	 * 
 	 * @param user The user to save
 	 */
 	public void createUser(User user) {
 		if (!user.isNewRecord()) {
 			updateUser(user);
 		} else {
 			ContentValues values = new ContentValues();
 			values.put(USERS_TOKEN, user.getToken());
 			values.put(USERS_TOKEN_SECRET, user.getTokenSecret());
 		    int insertId = (int)database.insert(USERS_TABLE_NAME, null, values);
 		    user.setId(insertId);
 		}
 	}
 	
 	/**
 	 * Saves an existing user to the database. If the user is
 	 * a new record, delegates to createUser(user)
 	 * 
 	 * @param user The user to save
 	 */
 	public void updateUser(User user) {
 		if (user.isNewRecord()) {
 			createUser(user);
 		} else {
 			ContentValues values = new ContentValues();
 			values.put(USERS_TOKEN, user.getToken());
 			values.put(USERS_TOKEN_SECRET, user.getTokenSecret());
 			String whereClause = _ID + " = ?";
 			String[] whereArgs = { user.getId().toString() };
 			database.update(USERS_TABLE_NAME, values, whereClause, whereArgs);
 		}
 	}
 	
 	//////// PRIVATE HELPER METHODS /////////
 	
 	private User cursorToUser(Cursor cursor) {
 		Integer id = cursor.getInt(ID_INDEX);
 		String tokenSecret = cursor.getString(TOKEN_SECRET_INDEX);
 		String token = cursor.getString(TOKEN_INDEX);
 		return new User(id, token, tokenSecret);
 	}
 }
