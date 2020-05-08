 /**
  * @author David S Anderson
  *
  *
  * Copyright (C) 2012 David S Anderson
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.dsanderson.android.util;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.dsanderson.util.DatabaseObject;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 /**
  * 
  */
 public class GenericDatabase extends SQLiteOpenHelper {
 	private SQLiteDatabase database;
 	private final String tableName;
 	private final IDatabaseObjectFactory objectFactory;
 	private final String searchColumn;
 	private List<String> allColumns;
 	private List<String> allTypes;
 	private String columnArray[] = null;
 	private String sortOrder = null;
 	private String filterString = null;
 
 	public static final String COLUMN_ID = "_id";
 	protected static final String TYPE_ID = "integer primary key autoincrement";
 
 	public static final String COLUMN_TIMESTAMP = "_timestamp";
 	protected static final String TYPE_TIMESTAMP = "integer not null";
 
 	public GenericDatabase(Context context, String dataBaseName,
 			int dataBaseVersion, String tableName,
 			IDatabaseObjectFactory objectFactory, String searchColumn) {
 		super(context, dataBaseName, null, dataBaseVersion);
 
 		this.tableName = tableName;
 		this.objectFactory = objectFactory;
 
 		this.allColumns = new ArrayList<String>();
 		this.allTypes = new ArrayList<String>();
 
 		this.searchColumn = searchColumn;
 
 		addColumn(COLUMN_ID, TYPE_ID);
 		addColumn(COLUMN_TIMESTAMP, TYPE_TIMESTAMP);
 
 		objectFactory.registerColumns(this);
 	}
 
 	public GenericDatabase addColumn(String column, String type) {
 		assert (columnArray == null);
 		allColumns.add(column);
 		allTypes.add(type);
 		return this;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
 	 * .SQLiteDatabase)
 	 */
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 
 		String dataBaseCreate = "create table " + tableName + "( ";
 		for (int i = 0; i < allColumns.size(); i++) {
 			if (i != 0)
 				dataBaseCreate += ", ";
 
 			dataBaseCreate += allColumns.get(i) + " " + allTypes.get(i);
 		}
 		dataBaseCreate += ");";
 
 		db.execSQL(dataBaseCreate);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
 	 * .SQLiteDatabase, int, int)
 	 */
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		Log.w(GenericDatabase.class.getName(),
 				"Upgrading database from version " + oldVersion + " to "
 						+ newVersion + ", which will destroy all old data");
 		db.execSQL("DROP TABLE IF EXISTS " + tableName);
 		onCreate(db);
 	}
 
 	public void close() {
 		database.close();
 	}
 
 	public void remove(Cursor cursor) {
 		long id = cursor.getInt(0);
 		removeById(id);
 	}
 
 	public void remove(DatabaseObject object) {
 		long id = object.getId();
 		removeById(id);
 	}
 
 	public void remove(int index) {
 		Cursor cursor = getCursor(index);
 		remove(cursor);
 		cursor.close();
 	}
 
 	public void removeById(long id) {
 		System.out.println("Comment deleted with id: " + id);
 		database.delete(tableName, COLUMN_ID + " = " + id, null);
 	}
 
 	public List<DatabaseObject> getAllObjects() {
 		List<DatabaseObject> objects = new ArrayList<DatabaseObject>();
 		Cursor cursor = getCursor();
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			DatabaseObject object = null;
 			object = objectFactory.getObject(cursor, object);
 			object.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
 			object.setTimestamp(new Date().getTime());
 			objects.add(object);
 			cursor.moveToNext();
 		}
 		// Make sure to close the cursor
 		cursor.close();
 		return objects;
 	}
 
 	public Cursor getCursor() {
 		return database.query(tableName, columnArray, filterString, null, null,
 				null, sortOrder);
 	}
 
 	public Cursor getCursorById(long id) {
 		Cursor cursor = database.query(tableName, columnArray, COLUMN_ID
 				+ " = " + id, null, null, null, null);
 		cursor.moveToFirst();
 		return cursor;
 	}
 
 	public Cursor getCursor(int index) {
 		Cursor cursor = getCursor();
 		cursor.moveToPosition(index);
 		return cursor;
 	}
 	
 	public Cursor getUnfilteredCursor() {
 		return database.query(tableName, columnArray, null, null, null,
 				null, sortOrder);
 	}
 
 	public DatabaseObject getObject(String name, String column) {
 		DatabaseObject object = null;
 		Cursor cursor = null;
 		try {
 			cursor = database.query(tableName, columnArray,
 					column + "=" + name, null, null, null, null);
 			cursor.moveToFirst();
 			object = objectFactory.getObject(cursor, object);
 			object.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
 			object.setTimestamp(cursor.getLong(cursor
 					.getColumnIndex(COLUMN_TIMESTAMP)));
 
 		} catch (Exception e) {
 		} finally {
 			try {
 				cursor.close();
 			} catch (Exception e) {
 			}
 		}
 		return object;
 	}
 
 	public void update(DatabaseObject object) {
 		ContentValues values = new ContentValues();
 		objectFactory.buildContentValues(object, values);
 		long id = object.getId();
 		object.setTimestamp(new Date().getTime());
 		database.update(tableName, values, COLUMN_ID + " = " + id, null);
 	}
 
 	public void clear() {
 		database.delete(tableName, null, null);
 	}
 
 	public void clearSortOrder() {
 		sortOrder = null;
 	}
 
 	public void addSortOrder(String columnName, boolean ascending) {
 		if (sortOrder == null)
 			sortOrder = "";
 		else
 			sortOrder += ", ";
 
 		sortOrder += columnName;
 
 		if (ascending)
 			sortOrder += " ASC";
 		else
 			sortOrder += " DESC";
 	}
 
 	public void clearFilter() {
 		filterString = null;
 	}
 
 	public void addFilter(String filterString) {
 		if (this.filterString == null)
 			this.filterString = "";
 		else
 			this.filterString += ", ";
 
 		this.filterString += filterString;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.dsanderson.util.IList#add(java.lang.Object)
 	 */
 	public void add(DatabaseObject object) {
 		ContentValues values = new ContentValues();
 		objectFactory.buildContentValues(object, values);
 		values.put(COLUMN_TIMESTAMP, (new Date()).getTime());
 		database.insert(tableName, null, values);
 	}
 
 	public DatabaseObject getById(long id) {
 		Cursor cursor = getCursorById(id);
 		DatabaseObject object = null;
 		object = objectFactory.getObject(cursor, object);
 		object.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
 		object.setTimestamp(cursor.getLong(cursor
 				.getColumnIndex(COLUMN_TIMESTAMP)));
 		return object;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.dsanderson.util.IList#get(int)
 	 */
 	public DatabaseObject get(int index) {
 		Cursor cursor = getCursor(index);
 		DatabaseObject object = get(cursor);
 		cursor.close();
 		return object;
 	}
 
 	public DatabaseObject get(Cursor cursor) {
 		DatabaseObject object = null;
 		object = objectFactory.getObject(cursor, object);
 		object.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
 		object.setTimestamp(cursor.getLong(cursor
 				.getColumnIndex(COLUMN_TIMESTAMP)));
 		return object;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.dsanderson.util.IList#find(java.lang.String)
 	 */
 	public DatabaseObject find(String name) {
 		return getObject("\"" + name + "\"", searchColumn);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.dsanderson.util.IList#loadList()
 	 */
 	public void open() throws Exception {
 		if (isOpen())
 			return;
 
 		columnArray = new String[allColumns.size()];
 		for (int i = 0; i < allColumns.size(); i++)
 			columnArray[i] = allColumns.get(i);
 
 		database = getWritableDatabase();
 	}
 
 	public void load() throws Exception {
 		// nothing to do here, is always ready to read data
 	}
 
 	public boolean isOpen() {
 		if (database == null)
 			return false;
 		else
 			return database.isOpen();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.dsanderson.util.IList#saveList()
 	 */
 	public void save() throws Exception {
 		// nothing to do here, sql databases save on exit
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.dsanderson.util.IList#getTimestamp()
 	 */
 	public Date getTimestamp() {
 		final String maxQuery = "SELECT MAX(" + COLUMN_TIMESTAMP
 				+ ") AS NEWEST_TIMESTAMP FROM " + tableName;
 		Cursor cursor = database.rawQuery(maxQuery, null);
 		cursor.moveToFirst();
 		Date date = new Date(cursor.getLong(cursor
 				.getColumnIndex("NEWEST_TIMESTAMP")));
 		cursor.close();
 		return date;
 	}
 
 	public int size() {
 		String[] column = { COLUMN_ID };
 		Cursor cursor = database.query(tableName, column, null, null, null,
 				null, null);
 		int size = cursor.getCount();
 		cursor.close();
 		return size;
 	}
 
 	public SQLiteDatabase getDatabase() {
 		return database;
 	}
 
 }
