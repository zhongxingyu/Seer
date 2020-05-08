 /**
  * Copyright (c) 2006-2011 Floggy Open Source Group. All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.floggy.persistence.android;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.floggy.persistence.android.core.impl.DatabaseHelper;
 import org.floggy.persistence.android.core.impl.ObjectSetImpl;
 import org.floggy.persistence.android.core.impl.Utils;
 
 import android.content.ContentValues;
 import android.content.Context;
 
 import android.database.Cursor;
 
 import android.database.sqlite.SQLiteDatabase;
 
 import android.util.Log;
 
 /**
 * This is the main class of the framework. All persistence operations
 * methods (such as loading, saving, deleting and searching for objects) are
 * declared in this class.
  */
 public class PersistableManager {
 	private static final String TAG = "floggy.PersistableManager";
 	public static final String BATCH_MODE = "BATCH_MODE";
 
 	/** The single instance of PersistableManager. */
 	private static PersistableManager instance;
 
 	/** DOCUMENT ME! */
 	protected DatabaseHelper databaseHelper;
 
 	/** DOCUMENT ME! */
 	protected Set<String> tables = new HashSet<String>();
 
 	private PersistableManager(Context context) {
 		this.databaseHelper = new DatabaseHelper(null, context);
 		this.init();
 	}
 
 	/**
 	* Returns the current instance of PersistableManager.
 	*
 	* @param context DOCUMENT ME!
 	*
 	* @return The current instance of PersistableManager.
 	*
 	* @throws IllegalArgumentException DOCUMENT ME!
 	*/
 	public static PersistableManager getInstance(Context context) {
 		if (context == null) {
 			throw new IllegalArgumentException("Context cannot be null");
 		}
 
 		if (instance == null) {
 			instance = new PersistableManager(context);
 		}
 
 		return instance;
 	}
 
 	/**
 	* Removes an object from the repository. If the object is not stored in
 	* the repository then a <code>FloggyException</code> will be thrown.
 	*
 	* @param objectClass Object to be removed.
 	* @param id DOCUMENT ME!
 	*
 	* @return DOCUMENT ME!
 	*
 	* @throws FloggyException Exception thrown if an error occurs while removing
 	* 				the object.
 	*/
 	public int delete(Class objectClass, long id) throws FloggyException {
 		String tableName = getTableName(objectClass);
 
 		SQLiteDatabase database = databaseHelper.getWritableDatabase();
 
 		return database.delete(tableName, "rowid=?",
 			new String[] { String.valueOf(id) });
 	}
 
 	/**
 	* Removes an object from the repository. If the object is not stored in
 	* the repository then a <code>FloggyException</code> will be thrown.
 	*
 	* @param object Object to be removed.\
 	*
 	* @return DOCUMENT ME!
 	*
 	* @throws FloggyException Exception thrown if an error occurs while removing
 	* 				the object.
 	* @throws IllegalArgumentException DOCUMENT ME!
 	*/
 	public int delete(Object object) throws FloggyException {
 		String tableName = getTableName(object.getClass());
 		Field field = Utils.getIDField(object.getClass());
 
 		if (field != null) {
 			SQLiteDatabase database = databaseHelper.getWritableDatabase();
 
 			String id = null;
 
 			try {
 				id = String.valueOf(field.getLong(object));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 			return database.delete(tableName, "rowid=?", new String[] { id });
 		} else {
 			throw new IllegalArgumentException(
 				"You cannot use this method to delete a non IDable class");
 		}
 	}
 
 	/**
 	* Removes all objects from the repository.
 	*
 	* @throws FloggyException Exception thrown if an error occurs while removing
 	* 				the objects.
 	*/
 	public void deleteAll() throws FloggyException {
 	}
 
 	/**
 	* Removes all objects that belongs to the class passed as parameter
 	* from the repository.
 	*
 	* @param objectClass The object class to search the objects.
 	*
 	* @return DOCUMENT ME!
 	*
 	* @throws FloggyException Exception thrown if an error occurs while removing
 	* 				the objects.
 	*/
 	public int deleteAll(Class objectClass) throws FloggyException {
 		String tableName = getTableName(objectClass);
 
 		SQLiteDatabase database = databaseHelper.getWritableDatabase();
 
 		return database.delete(tableName, null, null);
 	}
 
 	/**
 	* Searches objects of an especific object class from the repository. <br>
 	* <br>
 	* An optional application-defined search criteria can be defined using a <code>Filter</code>.<br>
 	* <br>
 	* An optional application-defined sort order can be defined using a
 	* <code>Comparator</code>.
 	*
 	* @param objectClass The object class to search the objects.
 	* @param filter An optional application-defined criteria for searching
 	* 			 objects.
 	* @param comparator An optional application-defined criteria for sorting
 	* 			 objects.
 	*
 	* @return List of objects that matches the defined criteria.
 	*
 	* @throws FloggyException DOCUMENT ME!
 	*/
 	public ObjectSet find(Class objectClass, Filter filter, Comparator comparator)
 		throws FloggyException {
 		return find(objectClass, filter, comparator, false);
 	}
 
 	/**
 	* Searches objects of an especific object class from the repository. <br>
 	* <br>
 	* An optional application-defined search criteria can be defined using a <code>Filter</code>.<br>
 	* <br>
 	* An optional application-defined sort order can be defined using a
 	* <code>Comparator</code>.
 	*
 	* @param objectClass The object class to search the objects.
 	* @param filter An optional application-defined criteria for searching
 	* 			 objects.
 	* @param comparator An optional application-defined criteria for sorting
 	* 			 objects.
 	* @param lazy A flag indicating to load or not all composite relationships.
 	*
 	* @return List of objects that matches the defined criteria.
 	*
 	* @throws FloggyException DOCUMENT ME!
 	*/
 	public ObjectSet find(Class objectClass, Filter filter,
 		Comparator comparator, boolean lazy) throws FloggyException {
 		String tableName = getTableName(objectClass);
 
 		SQLiteDatabase database = databaseHelper.getReadableDatabase();
 
 		Cursor cursor =
 			database.query(tableName, new String[] { "*" }, null, null, null, null,
 				null);
 
 		return new ObjectSetImpl(objectClass, cursor);
 	}
 
 	/**
 	* Gets the id under the object is stored. <br>
 	*
 	* @param object Object to be retrieved the id.
 	*
 	* @return the id under the object is stored
 	*
 	* @throws IllegalArgumentException DOCUMENT ME!
 	*/
 	public long getId(Object object) {
 		Field field = Utils.getIDField(object.getClass());
 
 		if (field != null) {
 			try {
 				return field.getLong(object);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} else {
 			throw new IllegalArgumentException(
 				"You cannot use this method to delete a non IDable class");
 		}
 
 		return -1;
 	}
 
 	/**
 	* Check if the object is already persisted. <br>
 	* <b>WARNING</b> The method only checks if the underline system has an entry
 	* for the given object object. The method doesn't checks if the fields have
 	* changed.
 	*
 	* @param object Object to be checked the object state.
 	*
 	* @return true if the object is already persisted in the underline system,
 	* 				false otherwise.
 	*/
 	public boolean isPersisted(Object object) {
 		return getId(object) > 0;
 	}
 
 	/**
 	* Load an previously stored object from the repository using the object ID.<br>
 	* The object ID is the result of a save operation or you can obtain it
 	* executing a search.
 	*
 	* @param object An instance where the object data will be loaded into.
 	* 			 Cannot be <code>null</code>.
 	* @param id The ID of the object to be loaded from the repository.
 	*
 	* @throws FloggyException Exception thrown if an error occurs while loading
 	* 				the object.
 	*
 	* @see #save(Persistable)
 	*/
 	public void load(Object object, long id) throws FloggyException {
 		load(object, id, false);
 	}
 
 	/**
 	* Load an previously stored object from the repository using the object ID.<br>
 	* The object ID is the result of a save operation or you can obtain it
 	* executing a search.
 	*
 	* @param object An instance where the object data will be loaded into.
 	* 			 Cannot be <code>null</code>.
 	* @param id The ID of the object to be loaded from the repository.
 	* @param lazy A flag indicating to load or not all composite relationships.
 	*
 	* @throws FloggyException Exception thrown if an error occurs while loading
 	* 				the object.
 	* @throws IllegalArgumentException DOCUMENT ME!
 	*
 	* @see #save(Persistable)
 	*/
 	public void load(Object object, long id, boolean lazy)
 		throws FloggyException {
 		if (object == null) {
 			throw new IllegalArgumentException(
 				"The persistable object cannot be null!");
 		}
 
 		String tableName = getTableName(object.getClass());
 
 		SQLiteDatabase database = databaseHelper.getReadableDatabase();
 
 		Cursor cursor = null;
 
 		try {
 			cursor = database.query(tableName, new String[] { "*" }, "rowid=" + id,
 					null, null, null, null);
 
 			if (cursor.moveToFirst()) {
 				Utils.setValues(cursor, object);
 
 				Field field = Utils.getIDField(object.getClass());
 
				try {
					field.set(object, Long.valueOf(id));
				} catch (Exception ex) {
					throw new FloggyException(ex.getMessage(), ex);
 				}
 			} else {
 				throw new FloggyException("Object not found for id: " + id);
 			}
 		} finally {
 			if (cursor != null) {
 				cursor.close();
 			}
 		}
 	}
 
 	/**
 	* Store an object in the repository. If the object is already in the
 	* repository, the object data will be overwritten.<br>
 	* The object ID obtained from this operation can be used in the load
 	* operations.
 	*
 	* @param object Object to be stored.
 	*
 	* @return The ID of the object.
 	*
 	* @throws FloggyException Exception thrown if an error occurs while storing
 	* 				the object.
 	* @throws IllegalArgumentException DOCUMENT ME!
 	*
 	* @see #load(Persistable, int)
 	*/
 	public long save(Object object) throws FloggyException {
 		if (object == null) {
 			throw new IllegalArgumentException(
 				"The persistable object cannot be null!");
 		}
 
 		Class objectClass = object.getClass();
 
 		SQLiteDatabase database = databaseHelper.getWritableDatabase();
 
 		try {
 			createTable(objectClass, database);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		Field field = Utils.getIDField(object.getClass());
 
 		long id = 0;
 
 		if (field != null) {
 			try {
 				id = field.getLong(object);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		if (id > 0) {
 			Log.d(TAG, "Updating object: " + object);
 			database.update(objectClass.getSimpleName(), getValues(object),
 				"rowid=?", new String[] { String.valueOf(id) });
 		} else {
 			Log.d(TAG, "Inserting object: " + object);
 			id = database.insert(objectClass.getSimpleName(), null, getValues(object));
 
 			if (field != null) {
 				try {
 					field.set(object, Long.valueOf(id));
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		return id;
 	}
 
 	/**
 	* Set a property
 	*
 	* @param name the property's name
 	* @param value the property's value
 	*/
 	public void setProperty(String name, Object value) {
 	}
 
 	/**
 	* Shutdown the PersistableManager.
 	*
 	* @throws FloggyException DOCUMENT ME!
 	*/
 	public void shutdown() throws FloggyException {
 	}
 
 	/**
 	* DOCUMENT ME!
 	*
 	* @param objectClass DOCUMENT ME!
 	* @param database DOCUMENT ME!
 	*
 	* @throws Exception DOCUMENT ME!
 	*/
 	protected void createTable(Class objectClass, SQLiteDatabase database)
 		throws Exception {
 		String tableName = getTableName(objectClass);
 
 		if (!tables.contains(tableName)) {
 			StringBuilder builder = new StringBuilder();
 
 			builder.append("create table ");
 			builder.append(tableName);
 			builder.append(" (");
 
 			int initialLength = builder.length();
 
 			Field[] fields = objectClass.getDeclaredFields();
 
 			for (Field field : fields) {
 				int modifier = field.getModifiers();
 
 				if (!(Modifier.isStatic(modifier) || Modifier.isTransient(modifier))) {
 					if (builder.length() != initialLength) {
 						builder.append(", ");
 					}
 
 					String fieldName = field.getName();
 					Class fieldType = field.getType();
 
 					if (fieldType.equals(boolean.class)
 						 || fieldType.equals(Boolean.class) || fieldType.equals(byte.class)
 						 || fieldType.equals(Byte.class) || fieldType.equals(int.class)
 						 || fieldType.equals(Integer.class) || fieldType.equals(long.class)
 						 || fieldType.equals(Long.class) || fieldType.equals(short.class)
 						 || fieldType.equals(Short.class)) {
 						builder.append(fieldName);
 						builder.append(" integer");
 
 						org.floggy.persistence.android.Field fieldAnnotation =
 							(org.floggy.persistence.android.Field) field.getAnnotation(org.floggy.persistence.android.Field.class);
 
 						if ((fieldAnnotation != null) && fieldAnnotation.id()) {
 							builder.append(" primary key autoincrement");
 						}
 					} else if (fieldType.equals(double.class)
 						 || fieldType.equals(Double.class) || fieldType.equals(float.class)
 						 || fieldType.equals(Float.class)) {
 						builder.append(fieldName);
 						builder.append(" real");
 					} else if (fieldType.equals(String.class)) {
 						builder.append(fieldName);
 						builder.append(" text");
 					}
 				}
 			}
 
 			builder.append(")");
 
 			Log.v(TAG, builder.toString());
 
 			database.execSQL(builder.toString());
 
 			tables.add(tableName);
 		}
 	}
 
 	/**
 	* DOCUMENT ME!
 	*
 	* @param objectClass DOCUMENT ME!
 	*
 	* @return DOCUMENT ME!
 	*
 	* @throws IllegalArgumentException DOCUMENT ME!
 	*/
 	protected String getTableName(Class objectClass) {
 		if (objectClass == null) {
 			throw new IllegalArgumentException("The class object cannot be null!");
 		}
 
 		Persistable annotation =
 			(Persistable) objectClass.getAnnotation(Persistable.class);
 
 		String tableName;
 
 		if (annotation != null) {
 			tableName = annotation.table();
 
 			if ("".equals(tableName)) {
 				tableName = objectClass.getSimpleName();
 			}
 		} else {
 			throw new IllegalArgumentException(objectClass
 				+ " is not a valid Persistable class.");
 		}
 
 		return tableName;
 	}
 
 	/**
 	* DOCUMENT ME!
 	*
 	* @param object DOCUMENT ME!
 	*
 	* @return DOCUMENT ME!
 	*
 	* @throws FloggyException DOCUMENT ME!
 	*/
 	protected ContentValues getValues(Object object) throws FloggyException {
 		ContentValues values = new ContentValues();
 
 		Field[] fields = object.getClass().getDeclaredFields();
 
 		for (Field field : fields) {
 			try {
 				int modifier = field.getModifiers();
 
 				if (!(Modifier.isStatic(modifier) || Modifier.isTransient(modifier))) {
 					String fieldName = field.getName();
 					Class fieldType = field.getType();
 
 					try {
 						Object value = Utils.getProperty(object, fieldName);
 						System.out.println(value);
 
 						if (fieldType.equals(boolean.class)
 							 || fieldType.equals(Boolean.class)) {
 							values.put(field.getName(), (Boolean) value);
 						} else if (fieldType.equals(byte.class)
 							 || fieldType.equals(Byte.class)) {
 							values.put(field.getName(), (Byte) value);
 						} else if (fieldType.equals(double.class)
 							 || fieldType.equals(Double.class)) {
 							values.put(field.getName(), (Double) value);
 						} else if (fieldType.equals(float.class)
 							 || fieldType.equals(Float.class)) {
 							values.put(field.getName(), (Float) value);
 						} else if (fieldType.equals(int.class)
 							 || fieldType.equals(Integer.class)) {
 							values.put(field.getName(), (Integer) value);
 						} else if (fieldType.equals(long.class)
 							 || fieldType.equals(Long.class)) {
 							values.put(field.getName(), (Long) value);
 						} else if (fieldType.equals(short.class)
 							 || fieldType.equals(Short.class)) {
 							values.put(field.getName(), (Short) value);
 						} else if (fieldType.equals(String.class)) {
 							values.put(field.getName(), (String) value);
 						}
 					} catch (NoSuchMethodException nsmex) {
 						continue;
 					}
 				}
 			} catch (Exception ex) {
 				throw Utils.handleException(ex);
 			}
 		}
 
 		System.out.println(values.toString());
 
 		return values;
 	}
 
 	/**
 	* DOCUMENT ME!
 	*/
 	protected void init() {
 		String sql =
 			"SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;";
 
 		SQLiteDatabase database = databaseHelper.getReadableDatabase();
 
 		Cursor cursor = database.rawQuery(sql, null);
 
 		while (cursor.moveToNext()) {
 			tables.add(cursor.getString(0));
 		}
 
 		cursor.close();
 
 		Log.v(TAG, String.valueOf(tables));
 	}
 }
