 package com.cs301w01.meatload.model.querygenerators;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import com.cs301w01.meatload.model.DBManager;
 import com.cs301w01.meatload.model.SQLiteDBManager;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.util.Log;
 
 public abstract class QueryGenerator {
 	
 	//common db vars
     public static final String COL_ID = "_id";
     public static final String COL_NAME = "name";
     public static final String COL_PICTUREID = "pictureID";
     public static final String COL_ALBUMID = "albumID";
 
 	
 	protected DBManager db;
 	protected Context context;
 	
 	public QueryGenerator(Context context) {
 		
 		this.context = context;
 		db = new SQLiteDBManager(context);
 		
 	}
 	
 	/**
      * Parses a string into a usable Date object.
      * @param date String representation of Date to be parsed
      * @return Parsed Date object
      */
     public Date stringToDate(String date) {
 
         SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
         try {
             return df.parse(date);
         } catch (ParseException e) {
             e.printStackTrace();  
         }
 
         return null;
         
     }
     
     
 
     /**
      * Useful method for converting from date to String for db insertion.
      * @param date Date object to be converted to String object
      * @return String object from converted Date object
      */
     public String dateToString(Date date) {
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         return dateFormat.format(date);
     }
     
     /**
      * This method is used to select rows from a table where a specific id is known. Returns a 
      * cursor that is preset to the first tuple.
      * @param selectColumns array of columns you wish to have returned
      * @param tableName table name
      * @param idValue
      * @return
      */
     protected Cursor selectRowByID(String[] selectColumns, String tableName, String idValue) {
     	
         Cursor mCursor =
                 db.query(true, tableName, selectColumns, COL_ID + " = " + idValue, 
                 		null, null, null, null, null);
         
         if (mCursor.getCount() == 0) {
             return null;
         }
 
         mCursor.moveToFirst();
         
         return mCursor;
 
     }
     
     /**
      * Returns the int value corresponding to the tuple ID by the tuple name.
      * @param name
      * @param tableName
      * @return
      * @link selectAlbumIDByName
      */
     protected int selectIDByName(String name, String tableName) {
     	//query to execute
         String select = "SELECT " + COL_ID + 
                         " FROM " + tableName + 
                         " WHERE " + COL_NAME + 
                         " = '" + name + "'";
         
         //execute query and get cursor
         Cursor c = db.performRawQuery(select);
         
         //return id of picture
         int id = c.getInt(c.getColumnIndex(COL_ID));
         
         c.close();
         
         return id;
     }
     
     /**
      * Use this method if you want to do a "SELECT *" style query on a table for a specific ID
      * @param tableName Name of table
      * @param id Row in table
      * @return
      */
     protected Cursor selectAllColsByID(String tableName, long id) {
 
         //build the query
         String selectionQuery = "SELECT * FROM " + tableName + " WHERE " + COL_ID +
                 " = '" + id + "'";
 
         return db.performRawQuery(selectionQuery);
 
     }
     
     /**
      * Removes a tuple based on its id value.
      * @param id Tuple to delete
      * @param tableName
      */
     protected void deleteByID(long id, String tableName) {
 
         String dQuery = "DELETE FROM " + tableName + " WHERE " + COL_ID  + " = '" + id + "'";
 
         Log.d(tableName, "Performing delete: " + dQuery);
 
         //db.execSQL(dQuery);
         
        db.performRawQuery(dQuery).close();
 
     }
 
 }
