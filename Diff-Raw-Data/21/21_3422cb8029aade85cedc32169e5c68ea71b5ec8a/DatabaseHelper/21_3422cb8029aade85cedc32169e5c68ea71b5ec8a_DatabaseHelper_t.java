 package com.andrewma.vision.database;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.andrewma.vision.database.core.DbColumn;
 import com.andrewma.vision.database.core.DbTable;
 import com.andrewma.vision.database.core.annotations.Column;
 import com.andrewma.vision.database.core.annotations.PrimaryKey;
 import com.andrewma.vision.database.core.annotations.Table;
 import com.andrewma.vision.models.Batch;
 import com.andrewma.vision.models.Glasses;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 
 	private static final String TAG = "SQLite";
 
 	private static final String DATABASE_NAME = "vision.db";
 	private static final int DATABASE_VERSION = 1;
 
 	private static DatabaseHelper instance = null;
 	private final Map<Class<?>, DbTable> tables = new HashMap<Class<?>, DbTable>();
 
 	public static DatabaseHelper getInstance(Context context) {
 		if (instance == null) {
 			instance = new DatabaseHelper(context.getApplicationContext());
 		}
 		return instance;
 	}
 
 	private DatabaseHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		registerModel(Glasses.class);
 		registerModel(Batch.class);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		for (DbTable table : tables.values()) {
 			final String createTableSql = table.generateCreateTableSql();
 			Log.v(TAG, createTableSql);
 			db.execSQL(createTableSql);
 		}
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 	}
 
 	private void registerModel(Class<?> clazz) {
 		if (clazz.isAnnotationPresent(Table.class)) {
 			final DbTable table = new DbTable(clazz,
 					(Table) clazz.getAnnotation(Table.class));
 
 			final Field[] fields = clazz.getFields();
 			for (Field field : fields) {
 				if (field.isAnnotationPresent(PrimaryKey.class)) {
 					final PrimaryKey annotation = (PrimaryKey) field
 							.getAnnotation(PrimaryKey.class);
 					table.setPrimarykey(field, annotation);
 				} else if (field.isAnnotationPresent(Column.class)) {
 					final Column annotation = (Column) field
 							.getAnnotation(Column.class);
 					table.columns().add(new DbColumn(field, annotation));
 				}
 			}
 
 			tables.put(clazz, table);
 		}
 	}
 
 	private DbTable lookupModelTable(Class<?> modelClass) {
 		if (!tables.containsKey(modelClass)) {
 			Log.e(TAG, "Could not insert object of class " + modelClass);
 			return null;
 		} else {
 			return tables.get(modelClass);
 		}
 	}
 
 	/**
 	 * @param model
 	 * @return returns -1 if there was an error
 	 */
 	public long insert(Object model) {
 		long rowid = -1;
 		final DbTable table = lookupModelTable(model.getClass());
 		if (table == null) {
 			return rowid;
 		}
 
 		final ContentValues contentValues = table.getContentValues(model);
 		final SQLiteDatabase db = getWritableDatabase();
 		try {
 			rowid = db.insert(table.getTableName(), null, contentValues);
 		} finally {
 			if (db != null) {
 				db.close();
 			}
 		}
 		return rowid;
 	}
 
 	public <E> List<E> getAll(Class<?> modelClass) {
 		final DbTable table = lookupModelTable(modelClass);
 		if (table == null) {
 			return null;
 		}
 
 		final List<E> list = new ArrayList<E>();
 		final SQLiteDatabase db = getReadableDatabase();
 		try {
 			final Cursor cursor = db.query(table.getTableName(), null, null,
 					null, null, null, null);
 			while (cursor.moveToNext()) {
 				final E row = cursorToObject(table, cursor);
 				if (row != null) {
 					list.add(row);
 				}
 			}
 			cursor.close();
 		} finally {
 			if (db != null) {
 				db.close();
 			}
 		}
 		return list;
 	}
 	
 	public <E> List<E> executeSql(Class<?> modelClass, String sqlStmt) {
 		Log.v(TAG, "Executing SQL: " + sqlStmt);
 		final DbTable table = lookupModelTable(modelClass);
 		if (table == null) {
 			return null;
 		}
 
 		final List<E> list = new ArrayList<E>();
 		final SQLiteDatabase db = getReadableDatabase();
 		try {
 			final Cursor cursor = db.rawQuery(sqlStmt, null);
 			while (cursor.moveToNext()) {
 				final E row = cursorToObject(table, cursor);
 				if (row != null) {
 					list.add(row);
 				}
 			}
 			cursor.close();
 		} finally {
 			if (db != null) {
 				db.close();
 			}
 		}
 		return list;
 	}
 
 	public <E> E get(Class<?> modelClass, int id) {
 		final DbTable table = lookupModelTable(modelClass);
 		if (table == null) {
 			return null;
 		}
 
 		E result = null;
 		final SQLiteDatabase db = getReadableDatabase();
 		try {
 			final String selection = table.getPrimaryKeyName() + " = " + id;
 			final Cursor cursor = db.query(table.getTableName(), null, selection,
 					null, null, null, null);
 			if (cursor.moveToNext()) {
 				result = cursorToObject(table, cursor);
 			}
 			cursor.close();
 		} finally {
 			if (db != null) {
 				db.close();
 			}
 		}
 		return result;
 	}
 
 	@SuppressWarnings("unchecked")
 	private <E> E cursorToObject(DbTable table, Cursor cursor) {
 		E row = null;
 		try {
 			row = (E) table.getModelClass().newInstance();
 
 			final int primaryKeyColumnIndex = cursor.getColumnIndex(table
 					.getPrimaryKeyName());
 			table.getPrimaryKeyField().setInt(row,
 					cursor.getInt(primaryKeyColumnIndex));
 
 			for (DbColumn column : table.columns()) {
 				final Field columnField = column.columnField;
 				final String columnName = column.getColumnName();
 				
 				final int cursorColumnindex = cursor.getColumnIndex(columnName);
				final Class<?> columnFieldClass = columnField.getType();
 				switch (column.columnAnnotation.dataType()) {
 				case INTEGER:
 					final int value = cursor.getInt(cursorColumnindex);
 					if (boolean.class.equals(columnFieldClass)) {
 						columnField.setBoolean(row, (value == 1));
 					} else if (Date.class.equals(columnFieldClass)) {
 						columnField.set(row, new Date((long)value * 1000));
 					} else {
 						columnField.setInt(row, value);
 					}
 					break;
 				case REAL:
 					columnField.setFloat(row,
 							cursor.getFloat(cursorColumnindex));
 					break;
 				case TEXT:
					final String cursorString = cursor.getString(cursorColumnindex);
					if(char.class.equals(columnFieldClass))
						columnField.set(row, cursorString.equals("") ? ' ' : cursorString.charAt(0));
					else
						columnField.set(row, cursorString);
 					break;
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return row;
 	}
 
 	public void delete(Class<?> modelClass, int id) {
 		final DbTable table = lookupModelTable(modelClass);
 		if (table == null) {
 			return;
 		}
 
 		final SQLiteDatabase db = getWritableDatabase();
 		try {
 			final String where = table.getPrimaryKeyName() + "=" + id;
 			db.delete(table.getTableName(), where, null);
 		} finally {
 			if (db != null) {
 				db.close();
 			}
 		}
 	}
 }
