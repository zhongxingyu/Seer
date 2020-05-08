 package se.kth.ssvl.tslab.wsn.service.bpf;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import se.kth.ssvl.tslab.wsn.general.bpf.BPFDB;
 import se.kth.ssvl.tslab.wsn.general.bpf.exceptions.BPFDBException;
 import android.annotation.TargetApi;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.util.Log;
 
 @TargetApi(11)
 public class DB implements BPFDB {
 
 	private static final String TAG = "DB";
 
 	private Logger logger;
 	private SQLiteDatabase database;
 	private String databaseFilePath;
 
 	public DB(File dbFile, Logger logger) {
 		this.databaseFilePath = dbFile.getAbsolutePath();
 
 		// Init the logger
 		this.logger = logger;
 		try {
 			database = SQLiteDatabase.openOrCreateDatabase(databaseFilePath,
 					null);
 		} catch (SQLiteException ex) {
 			logger.error(TAG, "error -- " + ex.getMessage());
 		} finally {
 			close();
 		}
 		logger.debug(TAG, "The DB class has been initialized properly");
 	}
 
 	public SQLiteDatabase getReadableDatabase() {
 		database = SQLiteDatabase.openDatabase(databaseFilePath, null,
 				SQLiteDatabase.OPEN_READONLY);
 		return database;
 	}
 
 	public SQLiteDatabase getWritableDatabase() {
 		database = SQLiteDatabase.openDatabase(databaseFilePath, null,
 				SQLiteDatabase.OPEN_READWRITE);
 
 		return database;
 	}
 
 	public void closeCursor(Cursor cursor) {
 		try {
 			if (cursor != null) {
 				cursor.close();
 			}
 		} catch (SQLiteException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public ContentValues mapToContentValues(Map<String, Object> map) {
 		ContentValues conValues = new ContentValues();
 		Iterator<Map.Entry<String, Object>> entries = map.entrySet().iterator();
 
 		while (entries.hasNext()) {
 			Map.Entry<String, Object> entry = entries.next();
 			if (entry.getValue() instanceof Integer) {
 				conValues.put(entry.getKey(), (Integer) entry.getValue());
 			} else if (entry.getValue() instanceof String) {
 				String entryString = entry.getValue().toString();
				if(entryString.length() > 2 && entryString.startsWith("'") ) {
					entryString = entryString.substring(1, entryString.length()-1);
				}
 				conValues.put(entry.getKey(), entryString);
 			} else {
 				Log.e(TAG, "There was an error converting object to a class");
 				return null;
 			}
 		}
 		return conValues;
 	}
 
 	/* *************************** */
 
 	@Override
 	public void close() {
 		try {
 			if (database != null) {
 				database.close();
 			}
 		} catch (SQLiteException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public int delete(String table, String whereClause, String[] whereArgs)
 			throws BPFDBException {
 		int numOfRowsUpdated = -1;
 		database = getWritableDatabase();
 
 		try {
 			numOfRowsUpdated = database.delete(table, whereClause,
 						whereArgs);
 		} catch (SQLiteException e) {
 			throw new BPFDBException(
 					"Couldn't delete, there was an SQLException: "
 							+ e.getMessage());
 		} finally {
 			close();
 		}
 		return numOfRowsUpdated;
 
 	}
 
 	@Override
 	public void execSQL(String sql) throws BPFDBException {
 		try {
 			database = getWritableDatabase();
 			logger.debug(TAG, "Executing SQL: " + sql);
 			database.execSQL(sql);
 		} catch (SQLiteException e) {
 			throw new BPFDBException("Error in executing sql: "
 					+ e.getMessage());
 		} finally {
 			close();
 		}
 	}
 
 	@Override
 	public int insert(String table, Map<String, Object> values)
 			throws BPFDBException {
 		ContentValues conValues = mapToContentValues(values);
 		
 		if (conValues == null) {
 			Log.e(TAG, "ContentValues conversion went bad!!");
 			return -1;
 		}
 
 		try {
 			database = getWritableDatabase();
 			return (int) database.insertWithOnConflict(table, null, conValues,
 					SQLiteDatabase.CONFLICT_IGNORE);
 		} catch (SQLiteException e) {
 			Log.e(TAG,
 					"Unable to insert the new row, reason: " + e.getMessage());
 		} finally {
 			close();
 		}
 
 		return -1;
 	}
 
 	@Override
 	public List<Map<String, Object>> query(String table, String[] columns,
 			String selection, String[] selectionArgs, String groupBy,
 			String having, String orderBy, String limit) throws BPFDBException {
 
 		// Error check
 		if (table == null || table.equals("")) {
 			throw new BPFDBException(
 					"The table was null or empty, cannot do this");
 		}
 
 		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
 		try {
 			// Open Database run the sql and get the Cursor
 			database = getWritableDatabase();
 			Cursor cursor = database.query(table, columns, selection,
 					selectionArgs, groupBy, having, orderBy);
 
 			if (cursor == null) {
 				throw new BPFDBException(
 						"The result was null when querying the database");
 			} else {
 				cursor.moveToFirst();
 			}
 			// For each row
 			while (!cursor.isAfterLast()) {
 				// Create a row object (HashMap)
 				HashMap<String, Object> row = new HashMap<String, Object>();
 
 				// If the columns were not specifed, get them all from db
 				if (columns == null) {
 					columns = cursor.getColumnNames();
 				}
 
 				for (int i = 0; i < columns.length; i++) {
 					// And put each column<->value pair
 					if (cursor.getType((cursor.getColumnIndex(columns[i]))) == Cursor.FIELD_TYPE_STRING ||
 							cursor.getType((cursor.getColumnIndex(columns[i]))) == Cursor.FIELD_TYPE_NULL) {
 						row.put(columns[i],cursor.getString(
 								cursor.getColumnIndex(columns[i])));
 					} else if (cursor.getType((cursor.getColumnIndex(columns[i]))) == Cursor.FIELD_TYPE_INTEGER) {
 						row.put(columns[i], cursor.getInt(cursor
 								.getColumnIndex(columns[i])));
 					} else {
 						Log.e(TAG, "Type of object is not supported. " +
 								"Trying to get the type from object: " + cursor.getString(cursor.getColumnIndex(columns[i])));
 					}
 				}
 
 				// Last but not least add the row to list with rows
 				result.add(row);
 				cursor.moveToNext();
 			}
 			closeCursor(cursor);
 		} catch (SQLiteException e) {
 			e.printStackTrace();
 			throw new BPFDBException(
 					"There was an error in executing the SQL: " + e.getMessage());
 		} finally {
 			close();
 		}
 
 		return result;
 	}
 
 	@Override
 	public int update(String table, Map<String, Object> values, String where,
 			String[] whereArgs) throws BPFDBException {
 
 		// Error check
 		if (table == null || table.equals("")) {
 			throw new BPFDBException(
 					"The table was null or empty, cannot do this");
 		}
 
 		// Convert Map to ContentValues
 		ContentValues conValues = mapToContentValues(values);
 		
 		if (conValues == null) {
 			Log.e(TAG, "ContentValues conversion went bad!!");
 			return -1;
 		}
 		
 		try {
 			// open database
 			database = getWritableDatabase();
 
 			return database.update(table, conValues, where, whereArgs);
 		} catch (SQLiteException e) {
 			throw new BPFDBException(
 					"Couldn't update table, since there was an SQLException: "
 							+ e.getMessage());
 		} finally {
 			close();
 		}
 	}
 
 }
