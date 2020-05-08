 package com.feedme.dao;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import com.feedme.Baby;
 import com.feedme.DatabaseHandler;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * This DAO class handles all CRUD operations on the Babies table in the SQLLite database.
  */
 public class BabyDao {
 
     // Contacts table name
     private static final String TABLE_DATA = "babies";
 
     // Contacts Table Columns names
     private static final String KEY_ID = "id";
     private static final String KEY_NAME = "name";
     private static final String KEY_SEX = "sex";
     private static final String KEY_HEIGHT = "height";
     private static final String KEY_WEIGHT = "weight";
     private static final String KEY_DOB = "dob";
 
     private SQLiteDatabase database;
     private DatabaseHandler databaseHandler;
 
     public BabyDao(Context context) {
         databaseHandler = new DatabaseHandler(context);
     }
 
     public void open() throws SQLException {
         database = databaseHandler.getWritableDatabase();
     }
 
     public void close() {
         databaseHandler.close();
     }
 
     /**
      * Adds a new Baby to the Database.
      *
      * @param baby - Baby POJO
      */
    void addBaby(Baby baby) {
 
         ContentValues values = new ContentValues();
         values.put(KEY_NAME, baby.getName()); // Baby name
         values.put(KEY_SEX, baby.getSex()); // Baby sex
         values.put(KEY_HEIGHT, baby.getHeight()); // Baby height
         values.put(KEY_WEIGHT, baby.getWeight()); // Baby weight
         values.put(KEY_DOB, baby.getDob()); // Baby dob
 
         // Inserting Row
         database.insert(TABLE_DATA, null, values);
     }
 
     /**
      * Getting single baby from the database by its ID.
      *
      * @param id - The ID of the Baby in the database.
      *
      * @return - Baby POJO representation of a specific Baby in the database.
      */
     Baby getBaby(int id) {
 
         Cursor cursor = database.query(TABLE_DATA, new String[] { KEY_ID,
                 KEY_NAME, KEY_SEX, KEY_HEIGHT, KEY_WEIGHT, KEY_DOB }, KEY_ID + "=?",
                 new String[] { String.valueOf(id) }, null, null, null, null);
         if (cursor != null)
             cursor.moveToFirst();
 
         // return baby
         return cursorToBaby(cursor);
     }
 
     /**
      * Get All Babies from the Database
      *
      * @return - Collection of all Babies located in the database.
      */
     public List<Baby> getAllBabies() {
         List<Baby> babyList = new ArrayList<Baby>();
 
         // Select All Query
         String selectQuery = "SELECT  * FROM " + TABLE_DATA;
         Cursor cursor = database.rawQuery(selectQuery, null);
 
         // looping through all rows and adding to list
         if (cursor.moveToFirst()) {
             do {
                 babyList.add(cursorToBaby(cursor));
             } while (cursor.moveToNext());
         }
 
         return babyList;
     }
 
     /**
      * Returns a String[] of all babies by name.
      *
      * @return
      */
     public String[] getAllBabiesAsStringArray(){
         List<Baby> babies = getAllBabies();
         
         String[] babiesAsStrings = new String[babies.size()];
         for(int i=0 ; i<babies.size() ; i++){
             Baby baby = babies.get(i);
             babiesAsStrings[i] = baby.getName();
         }
 
         return babiesAsStrings;
     }
 
     /**
      * Updates an existing Baby in the database.
      *
      * @return
      */
     public int updateBaby(Baby baby) {
 
         ContentValues values = new ContentValues();
         values.put(KEY_NAME, baby.getName());
         values.put(KEY_SEX, baby.getSex());
         values.put(KEY_HEIGHT, baby.getHeight());
         values.put(KEY_WEIGHT, baby.getWeight());
         values.put(KEY_DOB, baby.getDob());
 
         // updating row
         return database.update(TABLE_DATA, values, KEY_ID + " = ?", new String[] { String.valueOf(baby.getID()) });
     }
 
     /**
      * Deletes a Baby from the Database
      */
     public void deleteBaby(Baby baby) {
         database.delete(TABLE_DATA, KEY_ID + " = ?",
                 new String[]{String.valueOf(baby.getID())});
     }
 
     /**
      * Returns the total number of Babies that exist in the Database.
      *
      * @return - Total number of babies in the Babies table.
      */
     public int getBabiesCount() {
         String countQuery = "SELECT  * FROM " + TABLE_DATA;
         SQLiteDatabase db = databaseHandler.getReadableDatabase();
         Cursor cursor = db.rawQuery(countQuery, null);
         cursor.close();
 
         // return count
         return cursor.getCount();
     }
 
     /**
      * Converts a Cursor from the Babies Table to a Baby Object.
      *
      * @param cursor - Cursor object that contains the Baby data from the Database
      *
      * @return - Baby
      */
     private Baby cursorToBaby(Cursor cursor) {
         return new Baby(Integer.parseInt(cursor.getString(0)),
                 cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5));
     }
 }
