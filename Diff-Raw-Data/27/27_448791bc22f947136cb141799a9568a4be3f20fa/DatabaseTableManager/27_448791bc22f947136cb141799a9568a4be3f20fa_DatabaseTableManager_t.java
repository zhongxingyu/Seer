 package com.morphoss.acal.database;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteMisuseException;
 import android.os.Process;
 import android.util.Log;
 
 import com.morphoss.acal.Constants;
 
 
 /**
  * Some useful code for DB managers. Extend this and database state can be maintained.
  * call beginQuery before starting any internal queries and end query when finished any internal querys.
  * transactions are exposed and maintained.
  * 
  * @author Chris Noldus
  *
  */
 public abstract class DatabaseTableManager {
 
 	protected boolean inTx = false;
 	protected boolean inReadTx = false;
 
 	public static final int OPEN_READ = 1;
 	public static final int OPEN_READTX = 2;
 	public static final int OPEN_WRITE = 3;
 	public static final int OPEN_WRITETX = 4;
 
 	public static final int CLOSE = 5;
 	public static final int CLOSE_TX = 6;
 	
 	private ArrayList<DataChangeEvent> changes;
 	
 	private int initialPriority;
 	private static final int preferredPriority = Process.THREAD_PRIORITY_DISPLAY + (2*Process.THREAD_PRIORITY_LESS_FAVORABLE);
 	
 	protected SQLiteDatabase db;
 	protected AcalDBHelper dbHelper;
 	protected Context context;
 
 	
 	private long dbOpened;
 	private long dbYielded;
 	
 	public enum QUERY_ACTION { INSERT, UPDATE, DELETE, PENDING_RESOURCE };
 	
 	private static final String TAG = "aCal DatabaseManager";
 
 	public abstract void dataChanged(ArrayList<DataChangeEvent> changes);
 	protected abstract String getTableName();
 	
 	protected DatabaseTableManager(Context context) {
 		this.context = context;
 	}
 
 	private String openStackTraceInfo = null;
 	
 	protected void saveStackTraceInfo() {
 		int base = 3;
 		int depth = 12;
 		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
 		openStackTraceInfo = "\t"+stack[base].toString();
 		for (int i = base+1; i < stack.length && i< base+depth; i++)
 			openStackTraceInfo += "\n\t\t"+stack[i].toString(); 
 	}
 	
 	protected void printStackTraceInfo(int logLevel) {
 			int base = 3;
 			int depth = 10;
 			StackTraceElement[] stack = Thread.currentThread().getStackTrace();
 			String info = "\t"+stack[base].toString();
 			for (int i = base+1; i < stack.length && i< base+depth; i++)
 				info += "\n\t\t"+stack[i].toString(); 
 			Log.println(logLevel, TAG, info);
 	}
 	
 	public ArrayList<ContentValues> query(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
 		ArrayList<ContentValues> result = new ArrayList<ContentValues>();
 		int count = 0;
 		if (Constants.debugDatabaseManager && Constants.LOG_DEBUG) Log.println(Constants.LOGD,TAG,"DB: "+this.getTableName()+" query:");
 		if (Constants.debugDatabaseManager && Constants.LOG_VERBOSE) printStackTraceInfo(Log.VERBOSE);
 		beginReadQuery();
 		Cursor c = db.query(getTableName(), columns, selection, selectionArgs, groupBy, having, orderBy);
 		try {
 			if (c.getCount() > 0) {
 				for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
 					result.add(new ContentValues());
 					DatabaseUtils.cursorRowToContentValues(c, result.get(count++));
 				}
 			}
 		}
 		catch( Exception e ) {
 			Log.e(TAG,Log.getStackTraceString(e));
 		}
 		finally {
 			if ( c != null ) c.close();
			while (c!=null) {
				if (c.isClosed()) c = null;
				try { Thread.sleep(10); } catch (Exception e) {}
			}
 		}
 		endQuery();
		
 		return result;
 	}
 
 	protected void addChange(DataChangeEvent e) {
 		if (changes != null) changes.add(e);
 		else throw new IllegalStateException("Can not add change when db is closed!");
 	}
 
 	protected void openDB(final int type) {
 		if (inTx) {
 			if ( openStackTraceInfo != null ) {
 				Log.e(TAG,"Tried to open DB when already open.  Database was previously opened here:\n"+openStackTraceInfo);
 			}
 			throw new SQLiteMisuseException("Tried to open DB when already open");
 		}
 		this.initialPriority = Process.getThreadPriority(Process.myTid());
 		Process.setThreadPriority(preferredPriority);
 		dbHelper = new AcalDBHelper(context);
 		changes = new ArrayList<DataChangeEvent>();
 		this.dbOpened = System.currentTimeMillis();
 		this.dbYielded = dbOpened;
 		switch (type) {
 		case OPEN_READ:
 			if (Constants.debugDatabaseManager && Constants.LOG_DEBUG) Log.println(Constants.LOGD,TAG,"DB:"+this.getTableName()+" OPEN_READ:");
 			if (Constants.debugDatabaseManager && Constants.LOG_VERBOSE) printStackTraceInfo(Log.VERBOSE);
 			saveStackTraceInfo();
 			db = dbHelper.getReadableDatabase();
 			break;
 		case OPEN_WRITE:
 			if (Constants.debugDatabaseManager && Constants.LOG_DEBUG) Log.println(Constants.LOGD,TAG,"DB:"+this.getTableName()+" OPEN_WRITE:");
 			if (Constants.debugDatabaseManager && Constants.LOG_VERBOSE) printStackTraceInfo(Log.VERBOSE);
 			saveStackTraceInfo();
 			db = dbHelper.getWritableDatabase();
 			break;
 		case OPEN_READTX:
 			if (Constants.debugDatabaseManager && Constants.LOG_DEBUG) Log.println(Constants.LOGD,TAG,"DB:"+this.getTableName()+" OPEN_READTX:");
 			saveStackTraceInfo();
 			if (Constants.debugDatabaseManager && Constants.LOG_VERBOSE) printStackTraceInfo(Log.VERBOSE);
 			inTx = true;
 			inReadTx = true;
 			db = dbHelper.getReadableDatabase();
 			break;
 		case OPEN_WRITETX:
 			if (Constants.debugDatabaseManager && Constants.LOG_DEBUG) Log.println(Constants.LOGD,TAG,"DB:"+this.getTableName()+" OPEN_WRITETX:");
 			saveStackTraceInfo();
 			if (Constants.debugDatabaseManager && Constants.LOG_VERBOSE) printStackTraceInfo(Log.VERBOSE);
 			inTx = true;
 			db = dbHelper.getWritableDatabase();
 			db.beginTransaction();
 			break;
 		default:
 			dbHelper.close();
 			dbHelper = null;
 			changes = null;
 			throw new IllegalArgumentException("Invalid argument provided for openDB");		
 		}
 	}
 
 	protected void closeDB(final int type) {
 		if (db == null) throw new SQLiteMisuseException("Tried to close a DB that wasn't opened");
 		db.close();
		while (db.isOpen()) {
			try {
				Thread.sleep(10); 
			} catch (Exception e) {}
		}
 		dbHelper.close();
 		db = null;
 		dbHelper = null;
 		Process.setThreadPriority(this.initialPriority);
 		switch (type) {
 		case CLOSE:
 			if (Constants.debugDatabaseManager && Constants.LOG_DEBUG) Log.println(Constants.LOGD,TAG,"DB:"+this.getTableName()+" CLOSE:");
 			if (Constants.debugDatabaseManager && Constants.LOG_VERBOSE) printStackTraceInfo(Log.VERBOSE);
 			break;
 		case CLOSE_TX:
 			if (Constants.debugDatabaseManager && Constants.LOG_DEBUG) Log.println(Constants.LOGD,TAG,"DB:"+this.getTableName()+" CLOSETX:");
 			if (Constants.debugDatabaseManager && Constants.LOG_VERBOSE) printStackTraceInfo(Log.VERBOSE);
 			if (!inTx) throw new IllegalStateException("Tried to close a db transaction when not in one!");
 			inTx = false;
 			inReadTx = false;
 			openStackTraceInfo = null;
 			break;
 		default:
 			throw new IllegalArgumentException("Invalid argument provided for openDB");
 		}
 		long time = System.currentTimeMillis()-this.dbYielded;
 		//Metric checking to make sure that the database is being used correctly.
 		if ( (Constants.DEBUG_MODE && time > 700) || time > 2000 ) {
 			Log.w(TAG, "Database opened for excessive period of time ("+time+"ms) Without yield!:");
 			this.printStackTraceInfo(Log.WARN);
 		}
 		if (Constants.debugDatabaseManager && Constants.LOG_DEBUG ) {
 			time = System.currentTimeMillis()- this.dbOpened;
 			Log.println(Constants.LOGD, TAG, "Database opened for "+time+"ms");
 		}
 		this.dataChanged(changes);
 		changes = null;
 	}
 	
 	public void yield() {
 		if (Constants.debugDatabaseManager) Log.d(TAG, "Yield Called.");
 		if (db != null) {
 			this.dbYielded = System.currentTimeMillis();
 			db.yieldIfContendedSafely();
 		}
 	}
 
 	protected void beginReadQuery() {
 		if (!inTx && !inReadTx && db == null) 
 			openDB(OPEN_READ);
 		else if (!inTx && !inReadTx && db != null) throw new IllegalStateException("BeginRead Query called in invalid state");
 	}
 
 	protected void endQuery() {
 		if (db == null) throw new IllegalStateException("End query called on closed db");
 		if (!inTx && !inReadTx) closeDB(CLOSE);
 	}
 
 	protected void beginWriteQuery() {
 		if (!inTx && db == null) openDB(OPEN_WRITE);
 		else if (inReadTx) throw new IllegalStateException("Can not begin write query while in ReadOnly tx");
 		else if (!inTx && db != null) throw new IllegalStateException("BeginWrite called when db already open and not in tx");
 	}
 
 
 	public void beginTransaction() {
 		openDB(OPEN_WRITETX);
 	}
 
 	public void beginReadTransaction() {
 		openDB(OPEN_READTX);
 	}
 
 	public void setTxSuccessful() {
 		if (!inTx || inReadTx || db == null) throw new IllegalStateException("Tried to set Tx Successful when not in (writeable) TX");
 		db.setTransactionSuccessful();
 	}
 
 	public void endTransaction() {
 		if (!inTx)  throw new IllegalStateException("Tried to end Tx when not in TX");
 		if (!inReadTx) db.endTransaction();
 		closeDB(CLOSE_TX);
 	}
 
 	//Some useful generic methods
 
 	public int delete(String whereClause, String[] whereArgs) {
 		if (Constants.debugDatabaseManager && Constants.LOG_DEBUG) Log.println(Constants.LOGD,TAG,
 				"Deleting Row on "+this.getTableName()+":\n\tWhere: "+whereClause);
 		beginWriteQuery();
 		//First select or the row id's
 		ArrayList<ContentValues> rows = this.query(null, whereClause, whereArgs, null,null,null);
 		int count = db.delete(getTableName(), whereClause, whereArgs);
 		endQuery();
 		if (count != rows.size()) {
 			if (Constants.debugDatabaseManager) Log.w(TAG, "Inconsistent number of rows deleted!");
 		}
 		if (count == 0) {
 			if (Constants.debugDatabaseManager) Log.w(TAG, "No rows deleted for '"+whereClause+"' args: "+whereArgs);
 		}
 		else {
 			for (ContentValues cv : rows) {
 				changes.add(new DataChangeEvent(QUERY_ACTION.DELETE,cv));
 			}
 		}
 		return count;
 	}
 
 	public int update(ContentValues values, String whereClause,
 			String[] whereArgs) {
 		if (Constants.debugDatabaseManager && Constants.LOG_VERBOSE) Log.println(Constants.LOGV,TAG,
 				"Updating Row on "+this.getTableName()+":\n\t"+values.toString());
 		beginWriteQuery();
 		int count = db.update(getTableName(), values, whereClause,
 				whereArgs);
 		endQuery();
 		changes.add(new DataChangeEvent(QUERY_ACTION.UPDATE, new ContentValues(values)));
 		return count;
 	}
 
 	public long insert(String nullColumnHack, ContentValues values) {
 		if (Constants.debugDatabaseManager && Constants.LOG_VERBOSE) Log.println(Constants.LOGV, TAG, 
 				"Inserting Row on "+this.getTableName()+":\n\t"+values.toString());
 		beginWriteQuery();
 		long newId = db.insert(getTableName(), nullColumnHack, values);
 		endQuery();
 		values.put("_id", newId);
 		changes.add(new DataChangeEvent(QUERY_ACTION.INSERT, new ContentValues(values)));
 		return newId;
 	}
 
 	
 	public boolean processActions(DMQueryList queryList) {
 		if (inReadTx) throw new IllegalStateException("Can not process a query list if we are in a read only transaction!");
 		List<DMAction> actions = queryList.getActions();
 		boolean res = false;
 		boolean openDb = false;
 		try {
 			//Queries are always done as in a transaction - we need to see if we are already in one or not.
 			if ( DatabaseTableManager.this.inTx ) {
 				for (DMAction action : actions) {
 					action.process(this);
 					this.yield();
 				}
 			}
 			else {
 				beginTransaction();
 				openDb = true;
 				for (DMAction action : actions) {
 					action.process(this);
 					this.yield();
 				}
 				setTxSuccessful();
 			}
 			res = true;
 		}
 		catch ( Exception e ) {
 			Log.e(TAG, "Exception processing request: " + e + Log.getStackTraceString(e));
 		}
 		finally {
 			if ( openDb ) endTransaction();
 		}
 		return res;
 	}
 }
