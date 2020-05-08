 package andex.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import andex.Constants;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 /**
  * <p>一个Model层的DAO基类，提供访问数据库的各种方法，继承以实现更多的功能。
  * <p>它不关心数据如何存储，而是交给SQLiteOpenHelper来处理，覆盖getDbHelper()方法可以实现更多的SQLiteOpenHelper。
  * <p>
  *   Override getDB() if you want to provide other ways to SQLite database like system built-in.
  *   Call connect() before invoking any database operation method.
  *   Don't forget init table schema before everything.
  * </p>
  * @author 
  * @see DefaultSQLiteOpenHelper
  */
 public abstract class BaseDataSource {
 	
	private static final String LOG_TAG = "andex.db";
 
 	private static final String ERR_DB_NOT_CONNECTED = "Database instance is not correctly initilized.";
 
 	// Inject by setter
 	protected Context context;
 
 	protected String dbName;
 
 	DefaultSQLiteOpenHelper dbHelper;
 
 	protected SQLiteDatabase db;
 
 	// 常用SQL语句模板
 	protected final String SQL_DROP_TABLE = "drop table ${tableName}";
 
 	protected final String SQL_FIND_ALL = "select * from ${tableName}";
 	
 	
 	/** 是否自动关闭连接，默认是true；如果启用事务则设为false，所有方法实现时根据这个标识来决定是否在操作结束时关闭连接。*/
 	protected boolean isAutoDisconnect = true;
 	
 	/**
 	 * 用给定的名称初始化数据库。
 	 * @param dbName
 	 */
 	public BaseDataSource(Context context, String dbName) {
 		super();
 		this.context = context;
 		this.dbName = dbName;
 		dbHelper = getDbHelper();
 	}
 	
 	/**
 	 * 默认使用系统提供的方式，子类可以继承已修改DB的创建来源，
 	 * Inject new implemented db helper here.
 	 * @return
 	 */
 	protected DefaultSQLiteOpenHelper getDbHelper() {
 		if (context == null) {
 			Log.e(LOG_TAG, "System Context didn't set properly.");
 			return null;
 		}
 		return new DefaultSQLiteOpenHelper(context, this.dbName);
 	}
 

 	/**
 	 * Connect to DB, create it if not exist.
 	 */
 	public void connect() {
 		if(db != null && db.isOpen()) {
 			return;
 		}
 		db = getDB();
 	}
 
 	/**
 	 * 关闭数据库连接
 	 */
 	public void disconnect() {
		Log.v("db", "Disconnect db");
 		if (db != null && db.isOpen()) {
 			db.close();
 		}
 	}
 
 	/**
 	 * 获取可写的数据实例。
 	 * @return
 	 */
 	protected SQLiteDatabase getDB() {
		Log.i(LOG_TAG, "Access database from system storage");
		// 没有则存系统的数据库。
		if (context == null) {
			Log.e(LOG_TAG, "System Context didn't set properly.");
			return null;
		}
		Log.v(LOG_TAG, "DBHelper is: " + dbHelper.getClass().getSimpleName());
 		return dbHelper.getWritableDatabase();
 	}
 	
 	/**
 	 * Create table by sql if not exist.
 	 * @param sql
 	 */
 	public boolean createTable(String sql) {
 //		if(db == null || !db.isOpen()) {
 //			Log.e(LOG_TAG, ERR_DB_NOT_CONNECTED);
 //			return false;
 //		}
 		connect();
 		try {
 			db.execSQL(sql);
 			Log.i(LOG_TAG, "Table created: " + sql);
 		} catch (SQLException e) {
 			Log.w(LOG_TAG, e.getLocalizedMessage());
 			return false;
 		} finally {
 			if (isAutoDisconnect)
 				this.disconnect();
 		}
 		return true;
 	}
 	
 	/**
 	 * 废弃一个表。
 	 * @param tableName
 	 */
 	public void dropTable(String tableName) {
 //		if (db == null || !db.isOpen()) {
 //			Log.e(LOG_TAG, ERR_DB_NOT_CONNECTED);
 //			return;
 //		}
 		connect();
 		try {
 			db.execSQL(SQL_DROP_TABLE.replace("${tableName}", tableName));
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			Log.i(LOG_TAG, "Table " + tableName + " has been droped");
 			if (isAutoDisconnect)
 				this.disconnect();
 		}
 	}
 	
 	/**
 	 * 检测一个记录是否存在。
 	 * @param tableName 表
 	 * @param uniqueCol 唯一标识记录的字段名
 	 * @param colValue 欲检测的字段值
 	 * @return
 	 */
 	public boolean isExists(String tableName, String uniqueCol, String colValue) {
 		String sql = "select * from " + tableName + " where " + uniqueCol + "='" + colValue + "'";
 		if (Constants.debugMode) {
 			// Log.v(LOG_TAG, "SQL:" + sql);
 		}
 		Cursor cur = null;
 		try {
 			cur = db.rawQuery(sql, null);
 			if (cur.moveToNext()) {
 				return true;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		} finally {
 			if (cur != null)
 				cur.close();
 		}
 		return false;
 	}
 	
 	/**
 	 * 查询一条记录。
 	 * Find unique data row.
 	 * @param tableName
 	 * @param uniqueColName
 	 * @param value
 	 * @return
 	 */
 	public Map findUnique(String tableName, String uniqueColName, Object value) {
 		List<Map> data = findAll(tableName, uniqueColName + "=" + value);
 		if(data == null || data.size() == 0) {
 			return null;
 		}
 		return data.get(0);
 	}
 	
 	/**
 	 * 查询表中所有记录。
 	 * Find all data rows in a table.
 	 * @param tableName
 	 * @return
 	 */
 	public List<Map> findAll(String tableName) {
 		return findAll(tableName, null,  null);
 	}
 	
 	/**
 	 * 按照Where条件查询记录。
 	 * Find filtered data rows in a table.
 	 * @param tableName
 	 * @param whereClause
 	 * @return
 	 */
 	public List<Map> findAll(String tableName, String whereClause) {
 		return findAll(tableName, whereClause,  null);
 	}
 	
 	/**
 	 * 查询指定表的所有记录。
 	 * @param tableName
 	 * @param orderBy
 	 * @return 一个List，每一项表示一条记录，每项用Map表示，key为字段名，value为字段值。
 	 */
 	public List<Map> findAll(String tableName, String whereClause, String orderByClause) {
 		if (db == null) {
 			Log.e(LOG_TAG, ERR_DB_NOT_CONNECTED);
 			return null;
 		}
 		connect();
 		Log.v(LOG_TAG, "Find all in table " + tableName);
 		Cursor cursor = db.query(tableName, null, whereClause, null, null, null, orderByClause);
 		try {
 			return cursorToMapList(cursor);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return new ArrayList();
 		} finally {
 			if (isAutoDisconnect)
 				this.disconnect();
 		}
 	}
 
 	/**
 	 * SQL语句查询记录。
 	 * @param sql
 	 * @return
 	 */
 	public List<Map> find(String sql) {
 		connect();
 		Cursor cursor = db.rawQuery(sql, null);
 		try {
 			return cursorToMapList(cursor);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return new ArrayList();
 		} finally {
 			if (isAutoDisconnect)
 				this.disconnect();
 		}
 	}
 	
 	private List<Map> cursorToMapList(Cursor cursor) {
 		List<Map> result = new ArrayList();
 		int n = 0;
 		for(;cursor.moveToNext();) {
 //			Log.v(LOG_TAG, "Row" + n++);
 			int count = cursor.getColumnCount();
 			Map row = new HashMap();
 			for (int i = 0; i < count; i++) {
 				String colName = cursor.getColumnName(i);
 				if("ID".equals(colName)) {
 					row.put(colName, cursor.getLong(i));	
 				}
 				else {
 					row.put(colName, cursor.getString(i));
 				}
 			}
 			result.add(row);
 		}
 		cursor.close();
 		Log.v(LOG_TAG, "Result with " + result.size() + " records.");
 		return result;
 	}
 	
 	public long countTableAll(String table) {
 		return countTable(table, null, null);
 	}
 	
 	public long countTable(String table, String filter, String[] values) {
 		//TODO 暂时用query替代实现，如果有性能问题，再改成用rawQuery实现
 		prepareToConnect();
 		Cursor cursor = db.query(table, null, filter, values, null, null, null, null);
 		long i=0;
 		for(;cursor.moveToNext();i++){}
 		cursor.close();
 		return i;
 	}
 	
 	/**
 	 * 
 	 * @param tbName
 	 * @param pkID
 	 * @return
 	 */
 	public boolean deleteRow(String tbName, long pkID) {
 		prepareToConnect();
 		try {
 			int rows = db.delete(tbName, "ID=?", new String[]{"" + pkID});
 			Log.i(LOG_TAG, "" + rows  + " rows in " + tbName + " deleted.");
 			return (rows > 0);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 //		} finally {
 //			this.disconnect();
 		}
 	}
 	
 	/**
 	 * 删除表中所有记录
 	 * @param tbName
 	 * @return
 	 */
 	public boolean deleteAllRows(String tbName) {
 		prepareToConnect();
 		try {
 			int rows = db.delete(tbName, null, null);
 			Log.i(LOG_TAG, "all " + rows  + " rows in " + tbName + " deleted.");
 			return (rows > 0);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	// TODO use this method for every operation.
 	// @deprecated?
 	protected void prepareToConnect() {
 //		if(db == null) {
 //			Log.e(LOG_TAG, "Database instance is not correctly initilized.");
 //			throw new RuntimeException("Database instance is not correctly initilized.");
 //		}
 		if(db == null || !db.isOpen()) {
 			connect();
 		}
 	}
 
 	public SQLiteDatabase getDb() {
 		return db;
 	}
 	
 	/**
 	 * 开始事务（用endTransaction()结束）
 	 */
 	public void beginTransaction() {
 		isAutoDisconnect = false; // 关闭自动模式
 		this.connect();
 		this.db.beginTransaction();
 	}
 	
 	public void commit() {
 		this.db.setTransactionSuccessful();
 	}
 	
 	public void rollback() {
 		// NOTHING NEED TO DO FOR ROLLBACK
 	}
 	
 	/**
 	 * 结束一个用beginTransaction()开始的事务。
 	 */
 	public void endTransaction() {
 		this.db.endTransaction();
 		this.disconnect();
 		isAutoDisconnect = true; // 恢复自动模式
 	}
 
 	protected ContentValues fromMap(Map<String, Object> map) {
 		ContentValues values = new ContentValues();
 
 		for (Iterator it = map.keySet().iterator(); it.hasNext();) {
 			String colName = (String) it.next();
 			Object value = map.get(colName);
 			if (value instanceof String) {
 				String typedValue = (String) value;
 				values.put(colName, typedValue);
 			}
 			else if (value instanceof Integer) {
 				Integer typedValue = (Integer) value;
 				values.put(colName, typedValue);
 			}
 			else if (value instanceof Long) {
 				Long typedValue = (Long) value;
 				values.put(colName, typedValue);
 			}
 			else if (value instanceof Float) {
 				Float typedValue = (Float) value;
 				values.put(colName, typedValue);
 			}
 			else if (value instanceof Double) {
 				Double typedValue = (Double) value;
 				values.put(colName, typedValue);
 			}
 		}
 
 		return values;
 	}
 }
