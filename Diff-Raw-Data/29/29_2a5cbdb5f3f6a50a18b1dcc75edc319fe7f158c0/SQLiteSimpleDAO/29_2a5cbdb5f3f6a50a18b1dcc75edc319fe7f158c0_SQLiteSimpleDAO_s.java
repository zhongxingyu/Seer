 package garin.artemiy.simple.sqlite;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import garin.artemiy.simple.sqlite.util.Constants;
 import garin.artemiy.simple.sqlite.util.DatabaseUtil;
 import garin.artemiy.simple.sqlite.util.SharedPreferencesUtil;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * author: Artemiy Garin
  * date: 17.12.2012
  */
 public abstract class SQLiteSimpleDAO<T> {
 
     private static final String COLUMN_ID = "_id";
     private static final String DESC = "DESC";
     private static final String FORMAT_ARGUMENT = "%s = %s";
     private Class<T> tClass;
     private SQLiteSimpleHelper simpleHelper;
 
     public SQLiteSimpleDAO(Class<T> tClass, Context context) {
         simpleHelper = new SQLiteSimpleHelper(context, new SharedPreferencesUtil(context).getDatabaseVersion());
         this.tClass = tClass;
     }
 
     private String[] getAllColumns() {
         List<String> columnsList = new ArrayList<String>();
 
         columnsList.add(COLUMN_ID); // Default first column in Android
         for (Field field : tClass.getDeclaredFields()) {
            String columnName = DatabaseUtil.getColumnName(field);
            if (columnName != null)
                columnsList.add(columnName);
         }
 
         String[] columnsArray = new String[columnsList.size()];
 
         return columnsList.toArray(columnsArray);
     }
 
     private void bindObject(T newTObject, Cursor cursor) throws NoSuchFieldException, IllegalAccessException {
         for (Field field : tClass.getDeclaredFields()) {
             Field classField = tClass.getDeclaredField(field.getName());
            classField.set(newTObject, getValueFromCursor(cursor, field));
         }
     }
 
     // Get content from specific types
     private Object getValueFromCursor(Cursor cursor, Field field) throws IllegalAccessException {
         Class<?> fieldType = field.getType();
         Object value = null;
         int columnIndex = 0; // _id
         if (!field.getName().equals(COLUMN_ID)) {
             columnIndex = cursor.getColumnIndex(DatabaseUtil.getColumnName(field));
         }
 
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
     private void putInContentValues(ContentValues contentValues, Field field, Object object) throws IllegalAccessException {
         Object fieldValue = field.get(object);
         String key = DatabaseUtil.getColumnName(field);
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
         } else if (fieldValue instanceof Byte[]) {
             contentValues.put(key, fieldValue.toString().getBytes());
         }
     }
 
     @SuppressWarnings("unused")
     public long getLastRowId() {
         SQLiteDatabase database = simpleHelper.getReadableDatabase();
         String[] columns = getAllColumns();
         String table = DatabaseUtil.getTableName(tClass);
         Cursor cursor = database.query(table, columns, null, null, null, null, null);
         cursor.moveToLast();
 
         long id;
         if (cursor.getPosition() == -1) {
             id = -1;
         } else {
             id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
         }
         cursor.close();
         return id;
     }
 
     @SuppressWarnings("unused")
     public Cursor selectCursorAscFromTable() {
         return selectCursorFromTable(null, null, null, null, null);
     }
 
     @SuppressWarnings("unused")
     public Cursor selectCursorDescFromTable() {
         return selectCursorFromTable(null, null, null, null, String.format(Constants.FORMAT_TWINS, COLUMN_ID, DESC));
     }
 
     @SuppressWarnings("unused")
     public Cursor selectCursorFromTable(String selection, String[] selectionArgs,
                                         String groupBy, String having, String orderBy) {
         SQLiteDatabase database = simpleHelper.getReadableDatabase();
         String[] columns = getAllColumns();
         String table = DatabaseUtil.getTableName(tClass);
         Cursor cursor = database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
         cursor.moveToFirst();
         database.close();
 
         return cursor;
     }
 
     @SuppressWarnings("unused")
     public long create(T object) {
         SQLiteDatabase database = simpleHelper.getWritableDatabase();
         try {
             ContentValues contentValues = new ContentValues();
 
             for (Field field : object.getClass().getDeclaredFields()) {
                putInContentValues(contentValues, field, object);
             }
 
             return database.insert(DatabaseUtil.getTableName(object.getClass()), null, contentValues);
         } catch (Exception e) {
             e.printStackTrace();
             return -1;
         } finally {
             database.close();
         }
     }
 
     @SuppressWarnings("unused")
     public T read(long id) {
         SQLiteDatabase database = simpleHelper.getReadableDatabase();
         Cursor cursor = database.query(DatabaseUtil.getTableName(tClass), getAllColumns(),
                 String.format(FORMAT_ARGUMENT, COLUMN_ID, Long.toString(id)), null, null, null, null);
         try {
             T newTObject = tClass.newInstance();
             bindObject(newTObject, cursor);
 
             return newTObject;
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         } finally {
             database.close();
             cursor.close();
         } // in this case we close cursor, because we use database.query, NOT incoming cursor
     }
 
     @SuppressWarnings("unused")
     public T read(Cursor cursor) {
         try {
             T newTObject = tClass.newInstance();
             bindObject(newTObject, cursor);
             return newTObject;
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         } // don't close cursor, because CursorAdapter use it!
     }
 
     @SuppressWarnings("unused")
     public List<T> readAllAsc() {
         Cursor cursor = selectCursorAscFromTable();
         return readAll(cursor);
     }
 
     @SuppressWarnings("unused")
     public List<T> readAllDesc() {
         Cursor cursor = selectCursorDescFromTable();
         return readAll(cursor);
     }
 
     private List<T> readAll(Cursor cursor) {
         try {
             List<T> list = new ArrayList<T>();
             for (int i = 0; i < cursor.getCount(); i++) {
                 T newTObject = tClass.newInstance();
                 bindObject(newTObject, cursor);
                 list.add(newTObject);
                 cursor.moveToNext();
             }
 
             return list;
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         } finally {
             cursor.close();
         }
     }
 
     @SuppressWarnings("unused")
     public long update(long id, T newObject) {
         SQLiteDatabase database = simpleHelper.getWritableDatabase();
         try {
             ContentValues contentValues = new ContentValues();
 
             for (Field field : newObject.getClass().getDeclaredFields()) {
                putInContentValues(contentValues, field, newObject);
             }
 
             return database.update(DatabaseUtil.getTableName(newObject.getClass()), contentValues,
                     String.format(FORMAT_ARGUMENT, COLUMN_ID, id), null);
 
         } catch (Exception e) {
             e.printStackTrace();
             return -1;
         } finally {
             database.close();
         }
     }
 
     @SuppressWarnings("unused")
     public int delete(long id) {
         SQLiteDatabase database = simpleHelper.getWritableDatabase();
 
         int deletedRow = database.delete(
                 DatabaseUtil.getTableName(tClass), String.format(FORMAT_ARGUMENT, COLUMN_ID, id), null);
 
         database.close();
         return deletedRow;
     }
 
     @SuppressWarnings("unused")
     public int deleteAll() {
         SQLiteDatabase database = simpleHelper.getWritableDatabase();
 
         int deletedRow = database.delete(DatabaseUtil.getTableName(tClass), null, null);
 
         database.close();
         return deletedRow;
     }
 
 }
