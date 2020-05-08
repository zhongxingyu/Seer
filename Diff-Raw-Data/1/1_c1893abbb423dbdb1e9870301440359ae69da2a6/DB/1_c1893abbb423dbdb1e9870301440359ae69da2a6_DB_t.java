 package se.kth.ssvl.tslab.wsn.bpf;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import android.annotation.TargetApi;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.os.Environment;
 
 import se.kth.ssvl.tslab.wsn.general.bpf.BPFDB;
 import se.kth.ssvl.tslab.wsn.general.bpf.exceptions.BPFDBException;
import se.kth.ssvl.tslab.wsn.service.bpf.Logger;
 
 @TargetApi(11)
 public class DB implements BPFDB {
 
 	private static final String TAG = "DB";
 	private static final String DB_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
 	
 	
 	private Logger logger;
 	private SQLiteDatabase database;
 	private String databaseFilePath;
 
 	public DB(File dbFile, Logger logger) 
 	{  
 		this.databaseFilePath = DB_PATH+dbFile.getAbsolutePath();
 		
 		
 		// Init the logger
 	    this.logger = logger; 
 	    try
 	    {
 			database = SQLiteDatabase.openOrCreateDatabase(databaseFilePath, null);
 	    }
 	    catch (SQLiteException ex)
 	    {
 	        logger.error(TAG, "error -- " + ex.getMessage());
 	    }
 	    finally
 	    {
 	        close();
 	    }
 	    logger.debug(TAG, "The DB class has been initialized properly");
 	}
 	
 	public SQLiteDatabase getReadableDatabase()
 	{
 		database = SQLiteDatabase.openDatabase(databaseFilePath, null,
 												SQLiteDatabase.OPEN_READONLY);
 		return database;
 	}
 
 	public SQLiteDatabase getWritableDatabase()
 	{
 		database = SQLiteDatabase.openDatabase(databaseFilePath, null,
 												SQLiteDatabase.OPEN_READWRITE);
 
 		return database;
 	}
 	
 	public void closeCursor(Cursor cursor)
 	{
 		try {
 			if (cursor != null)
 			{
 				cursor.close();
 			}
 		}catch(SQLiteException e){
 			e.printStackTrace();
 		}
 	}
 	
 	public ContentValues MaptoContentValues(Map<String, Object> map){
 		ContentValues conValues = new ContentValues();
 		Iterator<Map.Entry<String, Object>> entries = map.entrySet().iterator();
 		
 		while (entries.hasNext()) {
 		    Map.Entry<String, Object> entry = entries.next();
 		    if(entry.getValue().getClass() == Integer.class)
 		    	conValues.put(entry.getKey(), (Integer) entry.getValue());
 		    else{
 		    	conValues.put(entry.getKey(),  entry.getValue().toString());
 		    }
 		}
 		return conValues;
 	}
 	
 
 	private String getCommaFromKey(Map<String, Object> map) {
 		StringBuilder result = new StringBuilder(100);
 		int c = 0;
 		for (Map.Entry<String, Object> item : map.entrySet()) {
 			c++;
 			
 			// Check if we need to add a comma or not
 			if (c < map.size()) {
 				result.append(item.getKey() + ", ");
 			} else {
 				result.append(item.getKey());
 			}
 		}
 		
 		return result.toString();
 	}
 
 	private String getCommaFromValue(Map<String, Object> map) {
 		StringBuilder result = new StringBuilder(100);
 		int c = 0;
 		for (Map.Entry<String, Object> item : map.entrySet()) {
 			c++;
 
 			// Check if we need to add a comma or not
 			if (c < map.keySet().size()) {
 				result.append(item.getValue() + ", ");
 			} else {
 				result.append(item.getValue());
 			}
 		}
 
 		return result.toString();
 	}
 	
 	private String getCommaFromArray(String[] array) {
 		StringBuilder result = new StringBuilder(100);
 		int c = 1;
 		for (String item : array) {
 			if (c < array.length) {
 				result.append(item + ", ");
 			} else {
 				result.append(item);
 			}
 			c++;
 		}
 		return result.toString();
 	}
 	
 	private String getUpdateStringFromMap(Map<String, Object> map) {
 		StringBuilder result = new StringBuilder(100);
 		int c = 1;
 		for (Map.Entry<String, Object> item : map.entrySet()) {
 			if (c < map.size()) {
 				result.append(item.getKey() + "='" + item.getValue() + "', ");
 			} else {
 				result.append(item.getKey() + "='" + item.getValue() + "'");
 			}
 			c++;
 		}
 		
 		return result.toString();
 	}
 	
 	private HashMap<String, Object> getNonEmptyEntries(Map<String, Object> map) {
 		HashMap<String, Object> res = new HashMap<String, Object>();
 		for (Map.Entry<String, Object> item : map.entrySet()) {
 			if (!item.getValue().toString().equals("")) {
 				res.put(item.getKey(), item.getValue());
 			}
 		}
 		return res;
 	}
 	
 	/* *************************** */
 
 	public void close()
 	{
 		try {
 			if (database != null)
 			{
 				database.close();
 			}
 		}catch(SQLiteException e){
 			e.printStackTrace();
 		}
 	}
 	
 
 	public int delete(String table, String whereClause, String[] whereArgs)
 			throws BPFDBException {
 		int numOfRowsUpdated = -1;
 		database = getWritableDatabase();
 		try{
 		
 			if (whereClause != null && !whereClause.equals("")) {
 				numOfRowsUpdated = database.delete(table,whereClause,whereArgs);
 				logger.debug(TAG, "Deleting with SQL: " + whereClause);
 			} else {
 				logger.warning(TAG, "Deleting all items in: " + table);
 			}
 		}catch(SQLiteException e) {
 			throw new BPFDBException("Couldn't delete, there was an SQLException: " + e.getMessage());
 		}
 		finally{
 			close();
 		}
 			return numOfRowsUpdated;
 		
 	}
 
 	public void execSQL(String sql) throws BPFDBException {
 		try {
 			database = getWritableDatabase();
 			logger.debug(TAG, "Executing SQL: " + sql);
 			database.execSQL(sql);
 		} catch (SQLiteException e) {
 			throw new BPFDBException("Error in executing sql: " + e.getMessage());	
 		}
 		finally{
 			close();
 		}
 	}
 
 	public int insert(String table, Map<String, Object> values)
 			throws BPFDBException {
 		ContentValues conValues = MaptoContentValues(values);
 		
 		logger.debug(TAG, "INSERT SQL: ");
 
 		try {
 			database = getWritableDatabase();
 			database.insert(table, null, conValues);
 		} catch (SQLiteException e) {
 			throw new BPFDBException("Unable to insert the new row, reason: "
 					+ e.getMessage());
 		}
 		finally{
 			close();
 		}
 		
 		return -1;
 	}
 
 	public List<Map<String, Object>> query(String table, String[] columns, String selection,
 			String[] selectionArgs, String groupBy, String having,
 			String orderBy, String limit) throws BPFDBException {
 		
 		// Error check
 		if (table == null || table.equals("")) {
 			throw new BPFDBException("The table was null or empty, cannot do this");
 		}
 		
 		// Start building the SQL
 		StringBuilder sql = new StringBuilder(150);
 		sql.append("SELECT");
 		
 		// Add selection for the specified columns (if specified)
 		if (columns != null && columns.length > 0) {
 			sql.append(" " + getCommaFromArray(columns) + " FROM " + table);
 		} else {
 			sql.append(" * FROM " + table);
 		}
 		
 		// Add the where selection but not the arguments quite yet (prepared statement will do this)
 		if (selection != null && !selection.equals("")) {
 			sql.append(" WHERE ");
 			sql.append(selection);
 		}
 		
 		// Group by the specified argument (if spcified)
 		if (groupBy != null && !groupBy.equals("")) {
 			sql.append(" GROUP BY ");
 			sql.append(groupBy);
 		}
 		
 		// Add the having operator (if specified)
 		if (having != null && !having.equals("")) {
 			sql.append(" HAVING ");
 			sql.append(having);
 		}
 		
 		// Add the group by operator (if specified)
 		if (orderBy != null && !orderBy.equals("")) {
 			sql.append(" GROUP BY ");
 			sql.append(groupBy);
 		}
 		
 		// Add the limit (if specified)
 		if (limit != null && !limit.equals("")) {
 			sql.append(" LIMIT ");
 			sql.append(limit);
 		}
 
 		
 		List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
 		try {
 			
 			logger.debug(TAG, "Query SQL: " + sql.toString());
 
 			// Open Database run the sql and get the Cursor
 			database = getWritableDatabase();
 			Cursor cursor = database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
 			
 			if (cursor == null) {
 				throw new BPFDBException(
 						"The result was null when querying the database");
 			}else{
 				cursor.moveToFirst();
 			}
 			// For each row
 		    while (!cursor.isAfterLast()) {
 				// Create a row object (HashMap)
 				HashMap<String, Object> row = new HashMap<String, Object>();
 
 				if (columns != null) {
 					for (int i = 0; i < columns.length; i++) {
 						// And put each column<->value pair
 						if(cursor.getType((cursor.getColumnIndex(columns[i])))==1)
 							row.put(columns[i], cursor.getInt(cursor.getColumnIndex(columns[i])));
 						else
 							row.put(columns[i], cursor.getString(cursor.getColumnIndex(columns[i])));
 					}
 				} else {
 					String [] columnNames = cursor.getColumnNames();
 					for (int i = 1; i <= columnNames.length; i++) {
 						if(cursor.getType((cursor.getColumnIndex(columnNames[i])))==Cursor.FIELD_TYPE_INTEGER)
 							row.put(columnNames[i], cursor.getInt(cursor.getColumnIndex(columnNames[i])));
 						else
 							row.put(columnNames[i], cursor.getString(cursor.getColumnIndex(columnNames[i])));
 					}
 				}
 
 				// Last but not least add the row to list with rows
 				result.add(row);
 				closeCursor(cursor);
 			}
 		} catch (SQLiteException e) {
 			e.printStackTrace();
 			throw new BPFDBException(
 					"There was an error in executing the SQL: "
 							+ e.getMessage() + "\nTried to run query: " + sql.toString());
 		} finally {
 			close();
 		}
 
 		return result;
 	}
 
 	public int update(String table, Map<String, Object> values, String where,
 			String[] whereArgs) throws BPFDBException {
 		
 		// Error check
 		if (table == null || table.equals("")) {
 			throw new BPFDBException("The table was null or empty, cannot do this");
 		}
 		 
 		StringBuffer sql = new StringBuffer(150);
 		
 		// Start building the sql
 		sql.append("UPDATE " + table + " SET ");
 		
 		// Add the update values
 		sql.append(getUpdateStringFromMap(values));
 		
 		// Add the where statement (if specified)
 		if (where != null && !where.equals("")) {
 			sql.append(" WHERE ");
 			sql.append(where);
 		}
 		//Convert Map to ContentValues
 		ContentValues conValues = MaptoContentValues(values);
 		try {
 			//open database
 			database = getWritableDatabase();
 			logger.debug(TAG, "Updating with SQL: " + sql.toString());
 
 			return database.update(table, conValues, where, whereArgs);
 		} catch (SQLiteException e) {
 			throw new BPFDBException(
 					"Couldn't update table, since there was an SQLException: "
 							+ e.getMessage());
 		}
 		finally{
 			close();
 		}
 	}
 
 }
