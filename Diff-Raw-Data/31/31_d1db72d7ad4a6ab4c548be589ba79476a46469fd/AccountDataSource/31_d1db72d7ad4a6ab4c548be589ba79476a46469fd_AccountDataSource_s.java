 package com.example.locus.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.client.UserTokenHandler;
 
 import android.R.bool;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 import com.example.locus.entity.User;
 
 public class AccountDataSource {
 	private boolean isDatabaseOpen;
 	private SQLiteDatabase database;
 	private AccountSQLiteHelper dbHelper;
 	private String[] allColumns = { AccountSQLiteHelper.COLUMN_ID,
 			AccountSQLiteHelper.COLUMN_NAME, AccountSQLiteHelper.COLUMN_SEX,
 			AccountSQLiteHelper.COLUMN_INTEREST,
 			AccountSQLiteHelper.COLUMN_PIC, AccountSQLiteHelper.COLUMN_LATI,
 			AccountSQLiteHelper.COLUMN_LONGTI,
			AccountSQLiteHelper.COLUMN_LOGGEDIN};
 
 	public AccountDataSource(Context context) {
 		dbHelper = new AccountSQLiteHelper(context);
 		isDatabaseOpen = false;
 	}
 
 	public void open() throws SQLException {
 		if (!isDatabaseOpen) {
 			database = dbHelper.getWritableDatabase();
 			isDatabaseOpen = true;
 		}
 	}
 
 	public void close() {
 		if (isDatabaseOpen) {
 			dbHelper.close();
 			isDatabaseOpen = false;
 		}
 	}
 
 	public boolean isOpen() {
 		return isDatabaseOpen;
 	}
 
 	public void createUser(User user) {
 		ContentValues values = new ContentValues();
 		values.put(AccountSQLiteHelper.COLUMN_ID, user.getId());
 		values.put(AccountSQLiteHelper.COLUMN_NAME, user.getName());
 		values.put(AccountSQLiteHelper.COLUMN_SEX, user.getSex().getValue());
 		values.put(AccountSQLiteHelper.COLUMN_INTEREST, user.getInterests());
		values.put(AccountSQLiteHelper.COLUMN_PIC, user.getPic());
 		values.put(AccountSQLiteHelper.COLUMN_LATI, "" + user.getLatitude());
 		values.put(AccountSQLiteHelper.COLUMN_LONGTI, "" + user.getLongtitude());
 		Cursor cursor = database.query(AccountSQLiteHelper.TABLE_ACCOUNT,
 				allColumns,
 				AccountSQLiteHelper.COLUMN_ID + " = '" + user.getId() + "'",
 				null, null, null, null);
 		if (!cursor.moveToFirst()) {
 			database.insert(AccountSQLiteHelper.TABLE_ACCOUNT, null, values);
 		} else {
 			database.update(
 					AccountSQLiteHelper.TABLE_ACCOUNT,
 					values,
 					AccountSQLiteHelper.COLUMN_ID + " = '" + user.getId() + "'",
 					null);
 		}
 
 		Log.i(Constants.AppCoreTag, "new user created or updated = " + user);
 	}
 
 	public void loginUser(User user) {
 		ContentValues values = new ContentValues();
 		values.put(AccountSQLiteHelper.COLUMN_ID, user.getId());
 		values.put(AccountSQLiteHelper.COLUMN_NAME, user.getName());
 		values.put(AccountSQLiteHelper.COLUMN_SEX, user.getSex().getValue());
 		values.put(AccountSQLiteHelper.COLUMN_INTEREST, user.getInterests());
		values.put(AccountSQLiteHelper.COLUMN_PIC, user.getPic());
 		values.put(AccountSQLiteHelper.COLUMN_LATI, "" + user.getLatitude());
 		values.put(AccountSQLiteHelper.COLUMN_LONGTI, "" + user.getLongtitude());
 		values.put(AccountSQLiteHelper.COLUMN_LOGGEDIN, Constants.LoggedIn);
 		database.update(AccountSQLiteHelper.TABLE_ACCOUNT, values,
 				AccountSQLiteHelper.COLUMN_ID + " = '" + user.getId() + "'",
 				null);
		
 		Log.i(Constants.AppCoreTag, "new user loggedin = " + user);
 	}
 
 	public void logoutUser(User user) {
 		ContentValues values = new ContentValues();
 		values.put(AccountSQLiteHelper.COLUMN_ID, user.getId());
 		values.put(AccountSQLiteHelper.COLUMN_NAME, user.getName());
 		values.put(AccountSQLiteHelper.COLUMN_SEX, user.getSex().getValue());
 		values.put(AccountSQLiteHelper.COLUMN_INTEREST, user.getInterests());
		values.put(AccountSQLiteHelper.COLUMN_PIC, user.getPic());
 		values.put(AccountSQLiteHelper.COLUMN_LATI, "" + user.getLatitude());
 		values.put(AccountSQLiteHelper.COLUMN_LONGTI, "" + user.getLongtitude());
 		values.put(AccountSQLiteHelper.COLUMN_LOGGEDIN, Constants.LoggedOut);
 		database.update(AccountSQLiteHelper.TABLE_ACCOUNT, values,
 				AccountSQLiteHelper.COLUMN_ID + " = '" + user.getId() + "'",
 				null);
		
 		Log.i(Constants.AppCoreTag, "new user loggedout = " + user);
 	}
 
 	public void deleteUser(User user) {
 		String id = user.getId();
 		System.out.println("User deleted with id: " + id);
 		database.delete(AccountSQLiteHelper.TABLE_ACCOUNT,
 				AccountSQLiteHelper.COLUMN_ID + " = '" + id + "'", null);
 	}
 
 	public User getUserById(String id) {
 		User newUser = null;
 		Cursor cursor = database.query(AccountSQLiteHelper.TABLE_ACCOUNT,
 				allColumns, AccountSQLiteHelper.COLUMN_ID + " = '" + id + "'",
 				null, null, null, null);
 		if (cursor.moveToFirst()) {
 			newUser = cursorToUser(cursor);
 		}
 		cursor.close();
 		return newUser;
 	}
 
 	public List<User> getAllUsers() {
 		List<User> users = new ArrayList<User>();
 
 		Cursor cursor = database.query(AccountSQLiteHelper.TABLE_ACCOUNT,
 				allColumns, null, null, null, null, null);
 
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			User user = cursorToUser(cursor);
 			users.add(user);
 			cursor.moveToNext();
 		}
 		// Make sure to close the cursor
 		cursor.close();
 		return users;
 	}
 
 	private User cursorToUser(Cursor cursor) {
 		User user = new User(cursor.getString(1), cursor.getInt(2),
 				cursor.getString(3), null);
 		user.setId(cursor.getString(0));
 		user.setLatitude(Double.parseDouble(cursor.getString(5)));
 		user.setLongtitude(Double.parseDouble(cursor.getString(6)));
 		user.setLoggedIn(Boolean.parseBoolean(cursor.getString(7)));
 		user.setPic(cursor.getBlob(4));
 		return user;
 	}
 }
