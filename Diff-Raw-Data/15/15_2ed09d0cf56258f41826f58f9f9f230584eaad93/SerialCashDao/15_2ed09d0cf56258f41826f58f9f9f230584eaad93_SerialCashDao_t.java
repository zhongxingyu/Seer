 package com.zebia.dao;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.provider.BaseColumns;
 import android.util.Log;
 import com.google.gson.Gson;
 import com.zebia.model.ZebiaResponse;
 
 public class SerialCashDao<T> {
     private Gson gson;
 
     private StorageItemsHelper storageItemsHelper;
     private Class<? extends T> type;
 
     public SerialCashDao(StorageItemsHelper storageItemsHelper, Class<T> type) {
         this.storageItemsHelper = storageItemsHelper;
         this.gson = new Gson();
         this.type = type;
     }
 
     public interface ItemEntry extends BaseColumns {
         String TABLE_NAME = "items";
 
         String COLUMN_JSON = "json";
 
         int COLUMN_INDEX_JSON = 1;
 
         String[] COLUMNS = {_ID, COLUMN_JSON};
     }
 
     public static final String SQL_CREATE_ENTRIES =
             "CREATE TABLE " + ItemEntry.TABLE_NAME + " (" +
                     ItemEntry._ID + " INTEGER PRIMARY KEY," +
                     ItemEntry.COLUMN_JSON + " TEXT" +
                     " )";
 
     private ContentValues serialize(T zebiaResponse) {
         ContentValues values = new ContentValues();
         values.put(ItemEntry.COLUMN_JSON, gson.toJson(zebiaResponse));
         return values;
     }
 
     private T deserialize(Cursor cursor) {
         String json = cursor.getString(ItemEntry.COLUMN_INDEX_JSON);
         return gson.fromJson(json, type);
     }
 
     public void save(T zebiaResponse) {
         SQLiteDatabase sqLiteDatabase = storageItemsHelper.getWritableDatabase();
        sqLiteDatabase.delete(ItemEntry.TABLE_NAME, null, null);
         sqLiteDatabase.insert(ItemEntry.TABLE_NAME, null, serialize(zebiaResponse));
     }
 
     public T restore() {
         Cursor cursor = null;
         try {
             cursor = storageItemsHelper.getReadableDatabase().query(ItemEntry.TABLE_NAME, ItemEntry.COLUMNS, null, null, null, null, null);
         } catch (Exception e) {
             Log.e("Zebia", e.getStackTrace().toString());
         }
 
         if (cursor == null || !cursor.moveToFirst()) {
             return null;
         }
 
         return deserialize(cursor);
     }
 
 }
