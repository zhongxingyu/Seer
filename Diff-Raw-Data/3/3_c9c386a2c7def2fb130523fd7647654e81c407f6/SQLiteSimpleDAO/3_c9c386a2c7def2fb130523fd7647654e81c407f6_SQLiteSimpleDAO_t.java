 package garin.artemiy.sqlitesimple.library;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import garin.artemiy.sqlitesimple.library.annotations.Column;
 import garin.artemiy.sqlitesimple.library.util.SimpleConstants;
 import garin.artemiy.sqlitesimple.library.util.SimpleDatabaseUtil;
 import garin.artemiy.sqlitesimple.library.util.SimplePreferencesUtil;
 
 import java.io.ByteArrayOutputStream;
 import java.io.ObjectOutputStream;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * author: Artemiy Garin
  * Copyright (C) 2013 SQLite Simple Project
  * *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * *
  * http://www.apache.org/licenses/LICENSE-2.0
  * *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 public abstract class SQLiteSimpleDAO<T> {
 
     private Class<T> tClass;
     private SQLiteSimpleHelper simpleHelper;
     private String primaryKeyColumnName;
     private String localDatabasePath;
     private String assetsDatabaseName;
 
     private static SQLiteDatabase internalDatabase;
     private static SQLiteDatabase assetsDatabase;
     private static SQLiteDatabase localDatabase;
 
     protected SQLiteSimpleDAO(Class<T> tClass, Context context) {
         simpleHelper = new SQLiteSimpleHelper(context, SimpleConstants.SHARED_LOCAL_PREFERENCES,
                 new SimplePreferencesUtil(context).getDatabaseVersion(SimpleConstants.SHARED_LOCAL_PREFERENCES), null, false);
         init(tClass);
     }
 
     protected SQLiteSimpleDAO(Class<T> tClass, Context context, String assetsDatabaseName) {
         simpleHelper = new SQLiteSimpleHelper(context, assetsDatabaseName,
                 new SimplePreferencesUtil(context).getDatabaseVersion(assetsDatabaseName), assetsDatabaseName, false);
         this.assetsDatabaseName = assetsDatabaseName;
         init(tClass);
     }
 
     protected SQLiteSimpleDAO(Class<T> tClass, String localDatabasePath) {
         this.localDatabasePath = localDatabasePath;
         init(tClass);
     }
 
     private void init(Class<T> tClass) {
         this.tClass = tClass;
         primaryKeyColumnName = getPrimaryKeyColumnName();
     }
 
     private SQLiteDatabase getDatabase() {
         if (assetsDatabaseName != null) {
 
             if (assetsDatabase == null || !assetsDatabase.isOpen()) {
                 assetsDatabase = simpleHelper.getWritableDatabase();
             }
             return assetsDatabase;
 
         } else if (localDatabasePath != null) {
 
             if (localDatabase == null || !localDatabase.isOpen()) {
                 localDatabase = SQLiteDatabase.openDatabase(localDatabasePath, null, SQLiteDatabase.OPEN_READWRITE);
             }
             return localDatabase;
 
         } else {
 
             if (internalDatabase == null || !internalDatabase.isOpen()) {
                 internalDatabase = simpleHelper.getWritableDatabase();
             }
             return internalDatabase;
 
         }
     }
 
     private String getPrimaryKeyColumnName() {
 
         for (Field field : tClass.getDeclaredFields()) {
 
             Column fieldEntityAnnotation = field.getAnnotation(Column.class);
 
             if (fieldEntityAnnotation != null) {
                 String columnName = SimpleDatabaseUtil.getColumnName(field);
 
                 if (columnName != null) {
                     Column annotationColumn = field.getAnnotation(Column.class);
 
                     if (annotationColumn.isPrimaryKey()) {
                         return columnName;
                     }
 
                 }
             }
         }
 
         return SimpleConstants.ID_COLUMN;
 
     }
 
     private String[] getColumns() {
         boolean isHaveAnyKey = false;
         List<String> columnsList = new ArrayList<String>();
 
         for (Field field : tClass.getDeclaredFields()) {
             Column fieldEntityAnnotation = field.getAnnotation(Column.class);
             if (fieldEntityAnnotation != null) {
                 String columnName = SimpleDatabaseUtil.getColumnName(field);
                 if (columnName != null)
                     columnsList.add(columnName);
                 if (fieldEntityAnnotation.isPrimaryKey()) {
                     isHaveAnyKey = true;
                 }
             }
         }
 
         if (!isHaveAnyKey) {
             columnsList.add(SimpleConstants.ID_COLUMN);
         }
 
         String[] columnsArray = new String[columnsList.size()];
 
         return columnsList.toArray(columnsArray);
     }
 
     private void bindObject(T newTObject, Cursor cursor) throws NoSuchFieldException, IllegalAccessException {
         for (Field field : tClass.getDeclaredFields()) {
             if (!field.isAccessible())
                 field.setAccessible(true); // for private variables
             Column fieldEntityAnnotation = field.getAnnotation(Column.class);
             if (fieldEntityAnnotation != null) {
                 field.set(newTObject, getValueFromCursor(cursor, field));
             }
         }
     }
 
     // Get content from specific types
     private Object getValueFromCursor(Cursor cursor, Field field) throws IllegalAccessException {
         Class<?> fieldType = field.getType();
         Object value = null;
 
         int columnIndex = cursor.getColumnIndex(SimpleDatabaseUtil.getColumnName(field));
 
         if (fieldType.isAssignableFrom(Long.class) || fieldType.isAssignableFrom(long.class)) {
             value = cursor.getLong(columnIndex);
         } else if (fieldType.isAssignableFrom(String.class)) {
             value = cursor.getString(columnIndex);
         } else if ((fieldType.isAssignableFrom(Integer.class) || fieldType.isAssignableFrom(int.class))) {
             value = cursor.getInt(columnIndex);
         } else if ((fieldType.isAssignableFrom(Byte[].class) || fieldType.isAssignableFrom(byte[].class))) {
             value = cursor.getBlob(columnIndex);
         } else if ((fieldType.isAssignableFrom(Double.class) || fieldType.isAssignableFrom(double.class))) {
             value = cursor.getDouble(columnIndex);
         } else if ((fieldType.isAssignableFrom(Float.class) || fieldType.isAssignableFrom(float.class))) {
             value = cursor.getFloat(columnIndex);
         } else if ((fieldType.isAssignableFrom(Short.class) || fieldType.isAssignableFrom(short.class))) {
             value = cursor.getShort(columnIndex);
         } else if (fieldType.isAssignableFrom(Byte.class) || fieldType.isAssignableFrom(byte.class)) {
             value = (byte) cursor.getShort(columnIndex);
         } else if (fieldType.isAssignableFrom(Boolean.class) || fieldType.isAssignableFrom(boolean.class)) {
             int booleanInteger = cursor.getInt(columnIndex);
             value = booleanInteger == 1;
         }
         return value;
     }
 
     // Put in content value from object to specific type
     private void putInContentValues(ContentValues contentValues, Field field, Object object)
             throws IllegalAccessException {
         if (!field.isAccessible())
             field.setAccessible(true); // for private variables
         Object fieldValue = field.get(object);
         String key = SimpleDatabaseUtil.getColumnName(field);
         if (fieldValue instanceof Long) {
             contentValues.put(key, Long.valueOf(fieldValue.toString()));
         } else if (fieldValue instanceof String) {
             contentValues.put(key, fieldValue.toString());
         } else if (fieldValue instanceof Integer) {
             contentValues.put(key, Integer.valueOf(fieldValue.toString()));
         } else if (fieldValue instanceof Float) {
             contentValues.put(key, Float.valueOf(fieldValue.toString()));
         } else if (fieldValue instanceof Byte) {
             contentValues.put(key, Byte.valueOf(fieldValue.toString()));
         } else if (fieldValue instanceof Short) {
             contentValues.put(key, Short.valueOf(fieldValue.toString()));
         } else if (fieldValue instanceof Boolean) {
             contentValues.put(key, Boolean.parseBoolean(fieldValue.toString()));
         } else if (fieldValue instanceof Double) {
             contentValues.put(key, Double.valueOf(fieldValue.toString()));
         } else if (fieldValue instanceof Byte[] || fieldValue instanceof byte[]) {
             try {
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                 objectOutputStream.writeObject(fieldValue);
                 contentValues.put(key, outputStream.toByteArray());
                 objectOutputStream.flush();
                 objectOutputStream.close();
                 outputStream.flush();
                 outputStream.close();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     private List<T> readAll(Cursor cursor) {
         try {
 
             List<T> list = new ArrayList<T>();
 
             for (int i = 0; i < cursor.getCount(); i++) {
                 list.add(read(cursor));
                 cursor.moveToNext();
             }
 
             return list;
 
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
     }
 
     @SuppressWarnings("unused")
     private T read(Cursor cursor) {
         if (cursor != null) {
             try {
 
                 T newTObject = tClass.newInstance();
                 bindObject(newTObject, cursor);
                 return newTObject;
 
             } catch (Exception e) {
                 e.printStackTrace();
                 return null;
             }
 
         } else {
             return null;
         }
     }
 
     @SuppressWarnings("unused")
     private Cursor selectCursorAscFromTable() {
         return selectCursorFromTable(null, null, null, null, null);
     }
 
     @SuppressWarnings("unused")
     private Cursor selectCursorDescFromTable() {
         return selectCursorFromTable(null, null, null, null,
                 String.format(SimpleConstants.FORMAT_TWINS, primaryKeyColumnName, SimpleConstants.DESC));
     }
 
     @SuppressWarnings("unused")
     private Cursor selectCursorFromTable(String selection, String[] selectionArgs,
                                          String groupBy, String having, String orderBy) {
         try {
             String table = SimpleDatabaseUtil.getTableName(tClass);
             String[] columns = getColumns();
 
             SQLiteDatabase database = getDatabase();
             Cursor cursor = database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
             cursor.moveToFirst();
             return cursor;
 
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
     }
 
     private float averageQuery(String query) {
         SQLiteDatabase database = getDatabase();
         Cursor cursor = database.rawQuery(query, null);
         cursor.moveToFirst();
 
         float averageResult = cursor.getFloat(SimpleConstants.FIRST_ELEMENT);
 
         cursor.close();
 
         return averageResult;
     }
 
     private ContentValues getFilledContentValues(Object object) throws IllegalAccessException {
         ContentValues contentValues = new ContentValues();
 
         for (Field field : object.getClass().getDeclaredFields()) {
             Column fieldEntityAnnotation = field.getAnnotation(Column.class);
             if (fieldEntityAnnotation != null) {
                 if (!fieldEntityAnnotation.isAutoincrement()) {
                     putInContentValues(contentValues, field, object);
                 }
             }
         }
         return contentValues;
     }
 
     @SuppressWarnings("unused")
     public static void updateDatabases() {
         if (assetsDatabase != null) {
             assetsDatabase.close();
             assetsDatabase = null;
         }
 
         if (localDatabase != null) {
             localDatabase.close();
             localDatabase = null;
         }
 
         if (internalDatabase != null) {
             internalDatabase.close();
             internalDatabase = null;
         }
     }
 
     @SuppressWarnings("unused")
     public int getCount() {
         Cursor cursor = selectCursorAscFromTable();
 
         int count = cursor.getCount();
         cursor.close();
 
         return count;
     }
 
     @SuppressWarnings("unused")
     public long getLastRowId() {
         Cursor cursor = selectCursorAscFromTable();
 
         cursor.moveToLast();
 
         long id;
         if (cursor.getPosition() == -1) {
             id = SimpleConstants.ZERO_RESULT;
         } else {
             id = cursor.getLong(cursor.getColumnIndex(primaryKeyColumnName));
         }
 
         cursor.close();
 
         return id;
     }
 
     @SuppressWarnings("unused")
     public long createIfNotExist(T object, String columnName, String columnValue) {
         long result = SimpleConstants.ZERO_RESULT;
 
         Cursor cursor = selectCursorFromTable(String.format(SimpleConstants.FORMAT_COLUMN,
                 columnName), new String[]{columnValue}, null, null, null);
 
         if (cursor.getCount() == 0) {
             result = create(object);
         }
 
         cursor.close();
 
         return result;
     }
 
     @SuppressWarnings("unused")
     public long createIfNotExist(T object, String firstColumnName, String firstColumnValue,
                                  String secondColumnName, String secondColumnValue) {
         long result = SimpleConstants.ZERO_RESULT;
 
         Cursor cursor =
                 selectCursorFromTable(String.format(SimpleConstants.FORMAT_COLUMNS_COMMA,
                         firstColumnName, secondColumnName),
                         new String[]{firstColumnValue, secondColumnValue}, null, null, null);
 
         if (cursor.getCount() == 0) {
             result = create(object);
         }
 
         cursor.close();
 
         return result;
     }
 
     @SuppressWarnings("unused")
     public void createAll(List<T> objects) {
         for (T object : objects) {
             create(object);
         }
     }
 
     @SuppressWarnings("unused")
     public long create(T object) {
         try {
             ContentValues contentValues = new ContentValues();
 
             for (Field field : object.getClass().getDeclaredFields()) {
                 Column fieldEntityAnnotation = field.getAnnotation(Column.class);
                 if (fieldEntityAnnotation != null) {
                     if (!fieldEntityAnnotation.isAutoincrement()) {
                         putInContentValues(contentValues, field, object);
                     }
                 }
             }
 
             SQLiteDatabase database = getDatabase();
             return database.insert(SimpleDatabaseUtil.getTableName(object.getClass()), null, contentValues);
 
         } catch (Exception e) {
             e.printStackTrace();
             return SimpleConstants.ZERO_RESULT;
         }
     }
 
     @SuppressWarnings("unused")
     public T read(long id) {
         Cursor cursor = selectCursorFromTable(
                 String.format(SimpleConstants.FORMAT_COLUMN, primaryKeyColumnName),
                 new String[]{Long.toString(id)}
                 , null, null, null);
 
         try {
             T newTObject = tClass.newInstance();
             bindObject(newTObject, cursor);
             return newTObject;
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         } finally {
             cursor.close();
         }
 
     }
 
     @SuppressWarnings("unused")
     public T readWhere(String columnName, String columnValue) {
         Cursor cursor = selectCursorFromTable(String.format(SimpleConstants.FORMAT_COLUMN, columnName),
                 new String[]{columnValue}, null, null, null);
         T object = null;
         if (cursor != null) {
             object = read(cursor);
             cursor.close();
         }
         return object;
     }
 
     @SuppressWarnings("unused")
     public T readWhere(String firstColumnName, String firstColumnValue,
                        String secondColumnName, String secondColumnValue) {
         Cursor cursor = selectCursorFromTable(String.format(SimpleConstants.FORMAT_COLUMNS_COMMA,
                 firstColumnName, secondColumnName),
                 new String[]{firstColumnValue, secondColumnValue}, null, null, null);
         T object = null;
         if (cursor != null) {
             object = read(cursor);
             cursor.close();
         }
         return object;
     }
 
     @SuppressWarnings("unused")
     public T readWhereWithOrder(String columnName, String columnValue, String column, String order) {
         Cursor cursor = selectCursorFromTable(String.format(SimpleConstants.FORMAT_COLUMN, columnName),
                 new String[]{columnValue}, null, null, String.format(SimpleConstants.FORMAT_TWINS, column, order));
         T object = null;
         if (cursor != null) {
             object = read(cursor);
             cursor.close();
         }
         return object;
     }
 
     @SuppressWarnings("unused")
     public List<T> readAllAsc() {
         Cursor cursor = selectCursorAscFromTable();
         List<T> objects = readAll(cursor);
         cursor.close();
         return objects;
     }
 
     @SuppressWarnings("unused")
     public List<T> readAllDesc() {
         Cursor cursor = selectCursorDescFromTable();
         List<T> objects = readAll(cursor);
         cursor.close();
         return objects;
     }
 
     @SuppressWarnings("unused")
     public List<T> readAllWithOrder(String column, String order) {
         Cursor cursor = selectCursorFromTable(null, null, null, null,
                 String.format(SimpleConstants.FORMAT_TWINS, column, order));
         List<T> objects = readAll(cursor);
         cursor.close();
         return objects;
     }
 
     @SuppressWarnings("unused")
     public List<T> readAllWhere(String columnName, String columnValue) {
         Cursor cursor = selectCursorFromTable(String.format(SimpleConstants.FORMAT_COLUMN, columnName),
                 new String[]{columnValue}, null, null, null);
         List<T> objects = readAll(cursor);
         cursor.close();
         return objects;
     }
 
     @SuppressWarnings("unused")
     public List<T> readAllWhereWithOrder(String columnName, String columnValue, String column, String order) {
         Cursor cursor = selectCursorFromTable(String.format(SimpleConstants.FORMAT_COLUMN, columnName),
                 new String[]{columnValue}, null, null, String.format(SimpleConstants.FORMAT_TWINS, column, order));
         List<T> objects = readAll(cursor);
         cursor.close();
         return objects;
     }
 
     @SuppressWarnings("unused")
     public List<T> readAllWhereInWithOrder(String columnName, String in, String column, String order) {
         Cursor cursor = selectCursorFromTable(String.format(SimpleConstants.SQL_IN, columnName, in),
                 null, null, null, String.format(SimpleConstants.FORMAT_TWINS, column, order));
         List<T> objects = readAll(cursor);
         cursor.close();
         return objects;
     }
 
     @SuppressWarnings("unused")
     public List<T> readAllWhereIn(String columnName, String in) {
         Cursor cursor = selectCursorFromTable(String.format(SimpleConstants.SQL_IN, columnName, in),
                 null, null, null, null);
         List<T> objects = readAll(cursor);
         cursor.close();
         return objects;
     }
 
     @SuppressWarnings("unused")
     public long update(String columnName, String columnValue, T newObject) {
         try {
 
             ContentValues contentValues = getFilledContentValues(newObject);
 
             SQLiteDatabase database = getDatabase();
             return database.update(SimpleDatabaseUtil.getTableName(newObject.getClass()), contentValues,
                     String.format(SimpleConstants.FORMAT_COLUMN, columnName), new String[]{columnValue});
 
         } catch (Exception e) {
             e.printStackTrace();
             return SimpleConstants.ZERO_RESULT;
         }
     }
 
     @SuppressWarnings("unused")
     public long update(String firstColumnName, String firstColumnValue,
                        String secondColumnName, String secondColumnValue, T newObject) {
         try {
 
             ContentValues contentValues = getFilledContentValues(newObject);
 
             SQLiteDatabase database = getDatabase();
             return database.update(SimpleDatabaseUtil.getTableName(newObject.getClass()), contentValues,
                     String.format(SimpleConstants.FORMAT_COLUMNS_COMMA, firstColumnName, secondColumnName),
                     new String[]{firstColumnValue, secondColumnValue});
 
         } catch (Exception e) {
             e.printStackTrace();
             return SimpleConstants.ZERO_RESULT;
         }
     }
 
     @SuppressWarnings("unused")
     public long update(long id, T newObject) {
         return update(primaryKeyColumnName, String.valueOf(id), newObject);
     }
 
     @SuppressWarnings("unused")
     public long delete(long id) {
         SQLiteDatabase database = getDatabase();
         return database.delete(
                 SimpleDatabaseUtil.getTableName(tClass),
                 String.format(SimpleConstants.FORMAT_COLUMN, primaryKeyColumnName),
                 new String[]{String.valueOf(id)});
     }
 
     @SuppressWarnings("unused")
     public long deleteWhere(String columnName, String columnValue) {
         SQLiteDatabase database = getDatabase();
         return database.delete(
                 SimpleDatabaseUtil.getTableName(tClass), String.format(SimpleConstants.FORMAT_COLUMN, columnName),
                 new String[]{columnValue});
     }
 
     @SuppressWarnings("unused")
     public long deleteWhere(String firstColumnName, String firstColumnValue,
                             String secondColumnName, String secondColumnValue) {
         SQLiteDatabase database = getDatabase();
         return database.delete(
                 SimpleDatabaseUtil.getTableName(tClass), String.format(SimpleConstants.FORMAT_COLUMNS_COMMA,
                 firstColumnName, secondColumnName),
                 new String[]{firstColumnValue, secondColumnValue});
     }
 
     @SuppressWarnings("unused")
     public long deleteAll() {
         SQLiteDatabase database = getDatabase();
         return database.delete(SimpleDatabaseUtil.getTableName(tClass), null, null);
     }
 
     @SuppressWarnings("unused")
     public float average(String columnName) {
         String query = String.format(SimpleConstants.SQL_AVG_QUERY, columnName,
                 SimpleDatabaseUtil.getTableName(tClass));
         return averageQuery(query);
     }
 
     @SuppressWarnings("unused")
     public float average(String columnName, String whereColumn, String whereValue) {
         String query = String.format(SimpleConstants.SQL_AVG_QUERY_WHERE,
                 columnName, SimpleDatabaseUtil.getTableName(tClass),
                 whereColumn, whereValue);
         return averageQuery(query);
     }
 
 }
