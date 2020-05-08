 package com.andrewma.vision.database.core;
 
 import java.lang.reflect.Field;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.util.Log;
 
import com.andrewma.vision.database.DatabaseHelper;
 import com.andrewma.vision.database.core.annotations.Column;
 import com.andrewma.vision.database.core.annotations.PrimaryKey;
 import com.andrewma.vision.database.core.annotations.Table;
 
 public class DbTable {
 	private final String TAG;
 	private final List<DbColumn> columns = new LinkedList<DbColumn>();
 	private Field primaryKeyField;
 	private PrimaryKey primaryKeyAnnotation;
 	private Class<?> modelClass;
 	private Table tableAnnotation;
 
 	public DbTable(Class<?> model, Table annotation) {
 		TAG = "DbTable" + model.getSimpleName();
 		modelClass = model;
 		tableAnnotation = annotation;
 	}
 	
 	public Class<?> getModelClass() {
 		return modelClass;
 	}
 
 	public Field getPrimaryKeyField() {
 		return primaryKeyField;
 	}
 
 	public PrimaryKey getPrimaryKeyAnnotation() {
 		return primaryKeyAnnotation;
 	}
 
 	public void setPrimarykey(Field field, PrimaryKey annotation) {
 		primaryKeyField = field;
 		primaryKeyAnnotation = annotation;
 	}
 
 	public List<DbColumn> columns() {
 		return columns;
 	}
 
 	public String generateCreateTableSql() {
 		final StringBuilder sb = new StringBuilder("create table ")
 				.append(getTableName()).append("([")
 				.append(primaryKeyField.getName())
 				.append("] integer primary key autoincrement");
 
 		for (DbColumn column : columns) {
 			final Column annotation = column.columnAnnotation;
 			sb.append(", [");
 			sb.append(column.getColumnName()).append("] ")
 					.append(annotation.dataType().toString().toLowerCase());
 			if (!column.columnAnnotation.nullable()) {
 				sb.append(" not null");
 			}
 		}
 		sb.append(");");
 		return sb.toString();
 	}
 
 	public String getTableName() {
 		if (tableAnnotation.tableName() != "") {
 			return tableAnnotation.tableName();
 		} else {
 			return modelClass.getSimpleName();
 		}
 	}
 	
 	public String getPrimaryKeyName() {
 		if (primaryKeyAnnotation.primaryKeyName() != "") {
 			return primaryKeyAnnotation.primaryKeyName();
 		} else {
 			return primaryKeyField.getName();
 		}
 	}
 
 	public ContentValues getContentValues(Object model) {
 		if (!modelClass.equals(model.getClass())) {
 			Log.e(TAG, "getContentValues called with a "
 					+ model.getClass().getSimpleName()
 					+ " object when expecting " + modelClass.getSimpleName());
 			return null;
 		}
 		final ContentValues result = new ContentValues(columns.size());
 		for (DbColumn column : columns) {
 			try {
 				final String columnName = "[" + column.getColumnName() + "]";
 				switch (column.columnAnnotation.dataType()) {
 				case INTEGER:
 					final Class<?> columnFieldClass = column.columnField.getType();
 					if(boolean.class.equals(columnFieldClass)) {
 						result.put(columnName, column.columnField.getBoolean(model) ? 1 : 0);
 					} else if(Date.class.equals(columnFieldClass)) {
 						final Date date = (Date)column.columnField.get(model);
 						if(date != null) {
 							result.put(columnName, date.getTime() / 1000);
						} else 
 							result.put(columnName, 0);
 					} else {
 						result.put(columnName, column.columnField.getInt(model));
 					}
 					break;
 				case REAL:
 					result.put(columnName, column.columnField.getFloat(model));
 					break;
 				case TEXT:
 					final Object value = column.columnField.get(model);
 					
 					if (value == null) {
 						result.putNull(columnName);
 					} else {
 						result.put(columnName, value.toString());
 					}
 					break;
 				}
 			} catch (Exception e) {
 				Log.e(TAG,
 						column.getColumnName() + ". Exception:" + e.getMessage());
 				e.printStackTrace();
 				return null;
 			}
 		}
 		return result;
 	}
 }
