 package com.cs301w01.meatload.model;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 import com.cs301w01.meatload.model.querygenerators.AlbumQueryGenerator;
 import com.cs301w01.meatload.model.querygenerators.PictureQueryGenerator;
 import com.cs301w01.meatload.model.querygenerators.QueryGenerator;
 import com.cs301w01.meatload.model.querygenerators.TagQueryGenerator;
 
 
 /**
  * This class is helper for dealing with SQLite in Android. It provides a variety of useful 
  * methods for creating, updating, deleting, and selecting data. 
  * @see <a href="http://www.codeproject.com/Articles/119293/Using-SQLite-Database-with-Android">
    	http://www.codeproject.com/Articles/119293/Using-SQLite-Database-with-Android
  * </a>
  */
 public class SQLiteDBManager extends SQLiteOpenHelper implements DBManager /**implements Serializable*/ {
 
 	private static final long serialVersionUID = 1L;
     private static String logTag = "DBMANAGER";
     private static final String DB_NAME = "skindexDB";
     
     private static final int DATABASE_VERSION = 2;
     
     private Context myContext;
 
 
     public SQLiteDBManager(Context context) {
         super(context, DB_NAME, null, DATABASE_VERSION);
         
         myContext = context;
 
         //uncomment if any changes have been made to tables to reset
         //resetDB();
 
     }
 
     @Override
     public void onCreate(SQLiteDatabase newDB) {
 
         createTables(newDB);
 
     }
 
     private void createTables(SQLiteDatabase db) {
 
         db.execSQL(PictureQueryGenerator.CREATE_TABLE_QUERY);
         Log.d(logTag, PictureQueryGenerator.TABLE_NAME + " generated.");
 
         db.execSQL(AlbumQueryGenerator.CREATE_TABLE_QUERY);
         Log.d(logTag, AlbumQueryGenerator.TABLE_NAME + " generated.");
 
         db.execSQL(TagQueryGenerator.CREATE_TABLE_QUERY);
         Log.d(logTag, TagQueryGenerator.TABLE_NAME + " generated.");
         
         db.execSQL(QueryGenerator.CREATE_TABLE_ALBUMTAGS_TABLE);
         Log.d(logTag, QueryGenerator.TABLE_NAME_ALBUMTAGS + " generated.");
 
         Log.d(logTag, "DB generated.");
 
     }
 
     private void dropTables(SQLiteDatabase db) {
 
         db.execSQL("DROP TABLE IF EXISTS " + PictureQueryGenerator.TABLE_NAME);
         Log.d(logTag, PictureQueryGenerator.TABLE_NAME + " dropped.");
 
         db.execSQL("DROP TABLE IF EXISTS " + AlbumQueryGenerator.TABLE_NAME);
         Log.d(logTag, AlbumQueryGenerator.TABLE_NAME + " dropped.");
 
         db.execSQL("DROP TABLE IF EXISTS " + TagQueryGenerator.TABLE_NAME);
         Log.d(logTag, TagQueryGenerator.TABLE_NAME + " dropped.");
         
         db.execSQL("DROP TABLE IF EXISTS " + QueryGenerator.TABLE_NAME_ALBUMTAGS);
         Log.d(logTag, QueryGenerator.TABLE_NAME_ALBUMTAGS + " dropped.");
 
         Log.d(logTag, "DB generated.");
         
     }
     
     /**
      * Used to force changes to update in the sql table during testing
      */
     public void resetDB() {
 
         SQLiteDatabase db = this.getWritableDatabase();
 
         dropTables(db);
         createTables(db);
         
         db.close();
         
         Log.d(logTag, "TABLES RESET.");
 
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
 
         dropTables(sqLiteDatabase);
         onCreate(sqLiteDatabase);
 
     }
 
 
     
     /**
      * Performs a raw SQL Select query. Returns a cursor set to the first result.
      * @param query Query to be sent to database
      * @return Cursor to first result of query, or null if query is empty
      */
     public Cursor performRawQuery(String query) {
         
     	SQLiteDatabase db = this.getWritableDatabase();
 
         Cursor c = db.rawQuery(query, null);
 
         Log.d(logTag, "Raw query executed: " + query);
         	
         if (c.getCount() == 0) {
         	
         	db.close();
         	
         	return null;
         }
         
         c.moveToFirst();
 
         //TODO: test 
         db.close();
         
         return c;
 
     }
 
 	public Cursor query(boolean b, String tableName, String[] selectColumns,
 			String string, String selectionArgs[], String object2, String object3,
 			String object4, String object5) {
 
         SQLiteDatabase db = this.getWritableDatabase();
 
         Cursor c = db.query(b, tableName, selectColumns, string, selectionArgs,
                 object2, object3, object4, object5);
 
         Log.d(logTag, "Query Executed On " + tableName + "Selecting, " + selectColumns.toString());
         
         if (c.getCount() == 0) {
 
             db.close();
 
             return null;
         }
 
         c.moveToFirst();
 
         db.close();
 
 		return c;
 		
 	}
 
 	public int update(String tableName, ContentValues cv, String whereClause,
 			String whereArgs[]) {
 
         SQLiteDatabase db = this.getWritableDatabase();
 
         int id = db.update(tableName, cv, whereClause, whereArgs);
 
         db.close();
 
 		return id;
 		
 	}
 
 	public long insert(String tableName, String colId, ContentValues cv) {
 
         SQLiteDatabase db = this.getWritableDatabase();
 
         long id = db.insert(tableName, colId, cv);
        
 		return id;
 		
 	}
 
 }
