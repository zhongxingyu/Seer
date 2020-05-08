 package com.morphoss.acal.database;
 
 import java.util.ArrayList;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteMisuseException;
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
 	private boolean sucTx = false;
 
 	public static final int OPEN_READ = 1;
 	public static final int OPEN_READTX = 2;
 	public static final int OPEN_WRITE = 3;
 	public static final int OPEN_WRITETX = 4;
 
 	public static final int CLOSE = 5;
 	public static final int CLOSE_TX = 6;
 	
 	private ArrayList<DataChangeEvent> changes;
 	
 	protected SQLiteDatabase db;
 	protected AcalDBHelper dbHelper;
 	protected Context context;
 
 	protected DatabaseTableManager(Context context) {
 		this.context = context;
 	}
 	protected void printStackTraceInfo() {
 		int base = 3;
 		int depth = 5;
 		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
 		String info = "\t"+stack[base].toString();
 		for (int i = base+1; i < stack.length && i< base+depth; i++)
 			info += "\n\t\t"+stack[i].toString(); 
		if (Constants.debugDatabaseManager) Log.println(Constants.LOGD, TAG, info);
 	}
 	
 	protected abstract String getTableName();
 
 	public ArrayList<ContentValues> query(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
 		ArrayList<ContentValues> result = new ArrayList<ContentValues>();
 		int count = 0;
		if ( Constants.debugDatabaseManager ) printStackTraceInfo();
		while ( db == null || !db.isOpen() ) {
			try { Thread.sleep(10); } catch ( InterruptedException e ) {}
			beginReadQuery();
		}
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
 		}
 		endQuery();
 		return result;
 	}
 
 	public enum QUERY_ACTION { INSERT, UPDATE, DELETE };
 	
 	public static final String TAG = "aCal DatabaseManager";
 
 	public abstract void dataChanged(ArrayList<DataChangeEvent> changes);
 
 	protected void openDB(final int type) {
		if ( db != null && db.isOpen() ) {
			if ( type == OPEN_READTX || type == OPEN_WRITETX ) {
				inTx = true;
				if ( type == OPEN_WRITETX && !db.inTransaction() ) db.beginTransaction();
			}
			return;
		}
		if (inTx || sucTx ) throw new SQLiteMisuseException("Tried to open DB when already open");
 		dbHelper = new AcalDBHelper(context);
 		changes = new ArrayList<DataChangeEvent>();
 		switch (type) {
 		case OPEN_READ:
 			if (Constants.debugDatabaseManager) Log.d(TAG,"DB:"+this.getTableName()+" OPEN_READ:");
 			printStackTraceInfo();
 			db = dbHelper.getReadableDatabase();
 			break;
 		case OPEN_WRITE:
 			if (Constants.debugDatabaseManager) Log.d(TAG,"DB:"+this.getTableName()+" OPEN_WRITE:");
 			printStackTraceInfo();
 			db = dbHelper.getWritableDatabase();
 			break;
 		case OPEN_READTX:
 			if (Constants.debugDatabaseManager) Log.d(TAG,"DB:"+this.getTableName()+" OPEN_READTX:");
 			printStackTraceInfo();
 			inTx = true;
 			db = dbHelper.getReadableDatabase();
 			break;
 		case OPEN_WRITETX:
 			if (Constants.debugDatabaseManager) Log.d(TAG,"DB:"+this.getTableName()+" OPEN_WRITETX:");
 			printStackTraceInfo();
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
 		dbHelper.close();
 		db = null;
 		dbHelper = null;
 		switch (type) {
 		case CLOSE:
 			if (Constants.debugDatabaseManager) Log.d(TAG,"DB:"+this.getTableName()+" CLOSE:");
 			printStackTraceInfo();
 			break;
 		case CLOSE_TX:
 			if (Constants.debugDatabaseManager) Log.d(TAG,"DB:"+this.getTableName()+" CLOSETX:");
 			printStackTraceInfo();
 			if (!inTx) throw new IllegalStateException("Tried to close a db transaction when not in one!");
 			inTx = false;
 			sucTx = false;
 			break;
 		default:
 			throw new IllegalArgumentException("Invalid argument provided for openDB");
 		}
 		this.dataChanged(changes);
 		changes = null;
 	}
 
 	protected void beginReadQuery() {
		if (!inTx || db == null || !db.isOpen()) openDB(OPEN_READ);
 	}
 
 	protected void endQuery() {
 		if (!inTx) closeDB(CLOSE);
 	}
 
 	protected void beginWriteQuery() {
 		if (!inTx) openDB(OPEN_WRITE);
 	}
 
 
 	public void beginTransaction() {
 		openDB(OPEN_WRITETX);
 	}
 
 	public void beginReadTransaction() {
 		openDB(OPEN_READTX);
 	}
 
 	public void setTxSuccessful() {
 		if (!inTx || db == null || !db.isOpen()) throw new IllegalStateException("Tried to set Tx Successful when not in TX");
 		db.setTransactionSuccessful();
 		this.sucTx = true;
 	}
 
 	public void endTransaction() {
 		if ( db.inTransaction() ) db.endTransaction();
 		closeDB(CLOSE_TX);
 	}
 
 	//Some useful generic methods
 
 	public int delete(String whereClause, String[] whereArgs) {
 		if (Constants.debugDatabaseManager) Log.d(TAG, "Deleting Row on "+this.getTableName()+":\n\tWhere: "+whereClause);
 		beginWriteQuery();
 		//First select or the row i'ds
 		ArrayList<ContentValues> rows = this.query(null, whereClause, whereArgs, null,null,null);
 		int count = db.delete(getTableName(), whereClause, whereArgs);
 		if (count != rows.size()) {
 			if (Constants.debugDatabaseManager) Log.w(TAG, "Inconsistant number of rows deleted!");
 		}
 		for (ContentValues cv : rows) {
 			changes.add(new DataChangeEvent(QUERY_ACTION.DELETE,cv));
 		}
 		endQuery();
 		return count;
 	}
 
 	public int update(ContentValues values, String whereClause,
 			String[] whereArgs) {
 		if (Constants.debugDatabaseManager) Log.d(TAG, "Updating Row on "+this.getTableName()+":\n\t"+values.toString());
 		beginWriteQuery();
 		int count = db.update(getTableName(), values, whereClause,
 				whereArgs);
 		endQuery();
 		changes.add(new DataChangeEvent(QUERY_ACTION.UPDATE, new ContentValues(values)));
 		return count;
 	}
 
 	public long insert(String nullColumnHack, ContentValues values) {
 		if (Constants.debugDatabaseManager) Log.d(TAG, "Inserting Row on "+this.getTableName()+":\n\t"+values.toString());
 		beginWriteQuery();
 		long count = db.insert(getTableName(), nullColumnHack, values);
 		endQuery();
 		changes.add(new DataChangeEvent(QUERY_ACTION.INSERT, new ContentValues(values)));
 		return count;
 	}
 
 	public class DMQueryList {
 		private ArrayList<DMAction> actions = new ArrayList<DMAction>();
 		public void addAction(DMAction action) { actions.add(action); }
 		public boolean process(DatabaseTableManager dm) {
 			boolean res = false;
 			boolean openDb = false;
 			try {
 				//Queries are always done as in a transaction - we need to see if we are already in one or not.
 				if (DatabaseTableManager.this.inTx) {
 					for (DMAction action : actions) action.process(dm);
 				} else {
 					dm.beginTransaction();
 					openDb = true;
 					for (DMAction action : actions) action.process(dm);
 					dm.setTxSuccessful();
 					
 				}
 				res = true;
 			} catch (Exception e) {
 				Log.e(TAG, "Exception processing request: "+e+Log.getStackTraceString(e));
 			} finally { if (openDb) dm.endTransaction(); }
 			return res;
 		}
		public boolean isEmpty() {
			return actions.isEmpty();
		}
 	}
 
 
 	public interface DMAction {
 		public void process(DatabaseTableManager dm);
 	}
 
 	public final class DMInsertQuery implements DMAction {
 
 		private final String nullColumnHack;
 		private final ContentValues values;
 
 		public DMInsertQuery(String nullColumnHack, ContentValues values) {
 			this.nullColumnHack = nullColumnHack;
 			this.values = values;
 		}
 
 		public void process(DatabaseTableManager dm) {
 			dm.insert(nullColumnHack, values);
 		}
 	}
 
 	public final class DMUpdateQuery implements DMAction {
 
 		private final ContentValues values;
 		private final String whereClause;
 		private final String[] whereArgs;
 
 		public DMUpdateQuery(ContentValues values, String whereClause, String[] whereArgs) {
 			this.values = values;
 			this.whereClause = whereClause;
 			this.whereArgs = whereArgs;
 		}
 
 		@Override
 		public void process(DatabaseTableManager dm) {
 			dm.update(values, whereClause, whereArgs);
 		}
 	}
 
 	public final class DMDeleteQuery implements DMAction {
 
 		private final String whereClause;
 		private final String[] whereArgs;
 
 		public DMDeleteQuery(String whereClause, String[] whereArgs) {
 			this.whereClause = whereClause;
 			this.whereArgs = whereArgs;
 		}
 
 		@Override
 		public void process(DatabaseTableManager dm) {
 			dm.delete(whereClause, whereArgs);			
 		}
 	}
 
 	public final class DMQueryBuilder {
 		private QUERY_ACTION action = null;
 		private String nullColumnHack = null;
 		private ContentValues values = null;
 		private String whereClause = null;
 		private String[] whereArgs = null;
 
 		public DMQueryBuilder setAction(QUERY_ACTION action) {
 			this.action = action;
 			return this;
 		}
 
 		public QUERY_ACTION getAction() {
 			return this.action;
 		}
 
 		public DMQueryBuilder setNullColumnHack(String nullColumnHack) {
 			this.nullColumnHack = nullColumnHack;
 			return this;
 		}
 
 		public DMQueryBuilder setValues(ContentValues values) {
 			this.values = values;
 			return this;
 		}
 
 		public DMQueryBuilder setWhereClause(String whereClause) {
 			this.whereClause = whereClause;
 			return this;
 		}
 
 		public DMQueryBuilder setwhereArgs(String whereArgs[]) {
 			this.whereArgs = whereArgs;
 			return this;
 		}
 
 		public DMAction build() throws IllegalArgumentException {
 			if (action == null) throw new IllegalArgumentException("Can not build query without action set.");
 			switch (action) {
 			case INSERT:
 				if (values == null) throw new IllegalArgumentException("Can not build INSERT query without content values");
 				return new DMInsertQuery(nullColumnHack,values);
 			case UPDATE:
 				if (values == null) throw new IllegalArgumentException("Can not build UPDATE query without content values");
 				return new DMUpdateQuery(values, whereClause, whereArgs);
 			case DELETE:
 				return new DMDeleteQuery(whereClause, whereArgs);
 			default:
 				throw new IllegalStateException("Invalid action specified!");
 			}
 		}
 	}
 	
 	
 	
 	
 }
