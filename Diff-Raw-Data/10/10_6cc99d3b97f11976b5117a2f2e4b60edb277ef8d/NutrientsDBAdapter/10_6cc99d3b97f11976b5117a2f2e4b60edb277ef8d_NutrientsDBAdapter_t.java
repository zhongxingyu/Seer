 package ru.fit4life.app;
 
 import android.database.Cursor;
 
 /**
  * Created by Ivanuch on 07.09.13.
  */
 public class NutrientsDBAdapter extends ParentDatabaseAdapter {
 
     public static final String KEY_ROWID = "_id";
     public static final String KEY_NAME = "name";
     public static final String KEY_PROTEINS = "proteins";
     public static final String KEY_FATS = "fats";
     public static final String KEY_CARBOHYDRATES = "carbohydrates";
     public static final String KEY_CALORIES = "calories";
    private static final String TABLE_NAME = "Nutrients";
 
     private static final String TAG = "NutrientsDBAdapter";
 
     public Cursor fetchAll() {
         return getCursorByQuery(TABLE_NAME, null, null, KEY_NAME);
     }
 
     public Cursor fetchByFilter(String s) {
         return getCursorByQuery(TABLE_NAME, KEY_NAME + " LIKE '%?%'", new String[]{s}, KEY_NAME);
     }
 
     public boolean foodAlreadyExists(String foodName) {
         return itemExists(TABLE_NAME, KEY_NAME + " = ?", new String[]{foodName});
     }
 
     public void insertNewRow(String name, float proteins, float fats, float carbs, float calories) {
         //Bind values with columns
         data.clear();
         data.put(KEY_NAME, name.toLowerCase());
         data.put(KEY_PROTEINS, proteins);
         data.put(KEY_FATS, fats);
         data.put(KEY_CARBOHYDRATES, carbs);
         data.put(KEY_CALORIES, calories);
 
         getDb().insert(TABLE_NAME, null, data);
     }
 
     public void updateRowById(int id, String name, float proteins, float fats, float carbs, float calories) {
         //Bind values with columns
         data.clear();
         data.put(KEY_NAME, name.toLowerCase());
         data.put(KEY_PROTEINS, proteins);
         data.put(KEY_FATS, fats);
         data.put(KEY_CARBOHYDRATES, carbs);
         data.put(KEY_CALORIES, calories);
 
         getDb().update(TABLE_NAME, data, KEY_ROWID + " = ?", new String[]{String.valueOf(id)});
     }
 
     public void deleteRowById(int id) {
         getDb().delete(TABLE_NAME, KEY_ROWID + " = ?", new String[]{String.valueOf(id)});
     }
 }
