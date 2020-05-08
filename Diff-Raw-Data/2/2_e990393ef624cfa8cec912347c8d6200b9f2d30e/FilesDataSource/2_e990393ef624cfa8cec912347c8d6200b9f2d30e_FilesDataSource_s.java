 package com.jpqr.listpad.db;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.InputMismatchException;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 
 public class FilesDataSource {
 
 	public class Type {
 		public static final int FAVOURITE = 0;
 		public static final int RECENT = 1;
 	}
 
 	// Database fields
 	private SQLiteDatabase database;
 	private MySQLiteHelper dbHelper;
 
 	public FilesDataSource(Context context) {
 		dbHelper = new MySQLiteHelper(context);
 	}
 
 	public void open() throws SQLException {
 		database = dbHelper.getWritableDatabase();
 	}
 
 	public void close() {
 		dbHelper.close();
 	}
 
 	public long addFile(String uri, int type) {
 		ContentValues values = new ContentValues();
 		values.put(MySQLiteHelper.COLUMN_URI, uri);
 		switch (type) {
 			case Type.RECENT:
 				return database.insert(MySQLiteHelper.TABLE_RECENT_FILES, null, values);
 			case Type.FAVOURITE:
 				return database.insert(MySQLiteHelper.TABLE_FAV_FILES, null, values);
 		}
 		return -1;
 	}
 
 	public void deleteFile(String uri, int type) {
 		String[] whereClause = new String[] { uri };
 		switch (type) {
 			case Type.RECENT:
 				database.delete(MySQLiteHelper.TABLE_RECENT_FILES, MySQLiteHelper.COLUMN_URI + " = ?", whereClause);
 			break;
 			case Type.FAVOURITE:
 				database.delete(MySQLiteHelper.TABLE_FAV_FILES, MySQLiteHelper.COLUMN_URI + " = ?", whereClause);
 			break;
 		}
 	}
 
 	public boolean isFavourite(String uri) {
 		Cursor cursor = database.query(MySQLiteHelper.TABLE_FAV_FILES, new String[] { MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_URI }, MySQLiteHelper.COLUMN_URI + " = ?", new String[] { uri },
 				null, null, null);
 		boolean isFavourite = cursor.getCount() > 0;
 		cursor.close();
 		return isFavourite;
 	}
 
 	public ArrayList<String> getAll(int type) {
 		String table = "";
 		switch (type) {
 			case Type.RECENT:
 				table = MySQLiteHelper.TABLE_RECENT_FILES;
 			break;
 			case Type.FAVOURITE:
 				table = MySQLiteHelper.TABLE_FAV_FILES;
 			break;
 			default:
 				throw new InputMismatchException("Invalid type of file reference");
 		}
 
 		Cursor cursor = database.query(table, new String[] { MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_URI }, null, null, null, null, MySQLiteHelper.COLUMN_ID + " DESC");
 		ArrayList<String> files = new ArrayList<String>();
 		if (cursor.moveToFirst()) {
 			int index = cursor.getColumnIndex(MySQLiteHelper.COLUMN_URI);
 			do {
 				files.add(cursor.getString(index));
 			} while (cursor.moveToNext());
 		}
 		cursor.close();
 		return files;
 	}
 
 	public ArrayList<File> getAllFiles(int type) {
 		String table = "";
 		switch (type) {
 			case Type.RECENT:
 				table = MySQLiteHelper.TABLE_RECENT_FILES;
 			break;
 			case Type.FAVOURITE:
 				table = MySQLiteHelper.TABLE_FAV_FILES;
 			break;
 			default:
 				throw new InputMismatchException("Invalid type of file reference");
 		}
 
		Cursor cursor = database.query(table, new String[] { MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_URI }, null, null, null, null, MySQLiteHelper.COLUMN_ID + " DESC", "LIMIT 20");
 		ArrayList<File> files = new ArrayList<File>();
 		if (cursor.moveToFirst()) {
 			int index = cursor.getColumnIndex(MySQLiteHelper.COLUMN_URI);
 			do {
 				files.add(new File(cursor.getString(index)));
 			} while (cursor.moveToNext());
 		}
 		cursor.close();
 		return files;
 	}
 
 }
