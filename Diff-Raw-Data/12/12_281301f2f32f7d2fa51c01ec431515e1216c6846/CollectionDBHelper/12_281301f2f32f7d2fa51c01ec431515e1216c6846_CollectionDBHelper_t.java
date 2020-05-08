 package de.hsbremen.android.dribl.provider;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 /**
  * This is a class that allows the user to store specific images into a collection
  * so that the user can look at those pictures again later. 
  * 
 * This class has default visibility which means it can not be used outside
 * of this package. It is intended only to be used by the ContentProvider.
 * 
  * @author jannes
  */
 class CollectionDBHelper extends SQLiteOpenHelper {
 
 	private final static String DATABASE_NAME = "collection.sqlite";
 	private final static int DATABASE_VERSION = 1;
 	private final static String TABLE_NAME = "collection";
 	
 	/**
 	 * ID column name
 	 * Don't use something generic like just "id" to prevent potential
 	 * column name clashes with BaseColumns._ID
 	 */
 	private final static String COLUMN_ID = "id_collection";
 	
 	/**
 	 * Database creation SQL statement
 	 * 
 	 * Most column names are taken from DribbbleContract.Image
 	 */
 	private final static String DATABASE_CREATE = "create table " + TABLE_NAME + "(" +
 			COLUMN_ID + " integer primary key autoincrement, " +
 			DribbbleContract.Image._ID + " integer, " +
 	        DribbbleContract.Image.URL + " text not null, " +
 	        DribbbleContract.Image.IMAGE_URL + " text not null, " +
 	        DribbbleContract.Image.TITLE + " text not null, " +
 	        DribbbleContract.Image.AUTHOR + " text not null, " +
 	        DribbbleContract.Image.LIKES_COUNT + " integer, " +
 	        DribbbleContract.Image.REBOUNDS_COUNT + " integer, " +
 	        DribbbleContract.Image.COMMENTS_COUNT + " integer" +
 	        ");";
 
 	/**
 	 * Constructor for CollectionDBHelper
 	 * 
 	 * This doesn't open a database connection yet. The actual database opening
 	 * is deferred by SQLiteOpenHelper until needed.
 	 * 
 	 * @param context
 	 * @param name
 	 * @param factory
 	 * @param version
 	 */
 	public CollectionDBHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	/**
 	 * This method called if the database is going to be used, but doesn't exist yet.
 	 * So we have to create the dataase;
 	 */
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		// Create the database that stores the user's collection
 		db.execSQL(DATABASE_CREATE);
 		
 		// Create an index on the Dribbble ID column for faster searches
 		db.execSQL("CREATE INDEX collection_id_idx ON " + TABLE_NAME + "(" + DribbbleContract.Image._ID + ");");
 	}
 
 	/**
 	 * Upgrades an old database to a newer schema version
 	 */
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// There is only a single database schema version out there, so no upgrade code is needed yet
 	   Log.d("Dribl", "Upgrading database from version " + oldVersion + " to " + newVersion);
 	}
 	
 	/**
      * Add a row to the database
      * 
 	 * @param id
 	 * @param url
 	 * @param imageUrl
 	 * @param title
 	 * @param author
 	 * @return id_collection of the created row (Not Dribbble's ID!)
 	 */
 	public long addToCollection(long id, String url, String imageUrl, String title, String author) {
         ContentValues values = new ContentValues();   
         values.put(DribbbleContract.Image._ID, id);
         values.put(DribbbleContract.Image.URL, url);
         values.put(DribbbleContract.Image.IMAGE_URL, imageUrl);
         values.put(DribbbleContract.Image.TITLE, title);
         values.put(DribbbleContract.Image.AUTHOR, author);
         
         // TODO: Add the actual values for these? They can change very quickly,
         // so actually a request to the server would be required to retrieve those
         // values
         values.put(DribbbleContract.Image.LIKES_COUNT, 0);
         values.put(DribbbleContract.Image.REBOUNDS_COUNT, 0);
         values.put(DribbbleContract.Image.COMMENTS_COUNT, 0);
         
         return addToCollection(values);
     }
     
     /**
      * Add a row to the database
      * 
      * @param values ContentValues object
      * @return id_collection of the created row (Not Dribbble's ID!)
      */
     public long addToCollection(ContentValues values) {
     	SQLiteDatabase db = getWritableDatabase();
     	long id = db.insert(TABLE_NAME, null, values);
     	db.close();
     	
     	return id;
     }
     
     /**
      * Removes a row from the database
      * 
      * @param id Dribble's image ID
      * @return success
      */
 	public int removeFromCollection(long id) {
 		SQLiteDatabase db = getWritableDatabase();
     	int rowsAffected = db.delete(
     			TABLE_NAME,
     			DribbbleContract.Image._ID + "=?",   // where clause
     			new String[] { Long.toString(id) }   // where arguments
     		);
     	db.close();
     	
 		return rowsAffected;
 	}
     
     /**
      * Returns a cursor that contains all items in the user's collection
      * 
      * @return Cursor
      */
     public Cursor getCollection() {
     	SQLiteDatabase db = getReadableDatabase();
     	
     	Cursor cursor = db.query(TABLE_NAME,
     	        null,    // All columns
     	        null,    // No selection
     	        null,    // No selectionArgs
     	        null,    // No group by
     	        null,    // No having
     	        COLUMN_ID + " DESC"
     	        );
     	
     	// TODO: The cursor is closed by the CursorLoader, but who is closing the db?
 //    	cursor.close();
 //    	db.close();
     	
     	return cursor;
     }
 	
     /**
      * Checks whether an item already is in the collection.
      * 
      * @param id Dribbble's id for this image
      * @return
      *   Cursor of length 1 if this id is in the collection,
      *   or of length 0 if it isn't
      */
     public Cursor exists(long id) {
     	Log.d("Dribl", "Checking if " + Long.toString(id) + " already exists in the database");
     	SQLiteDatabase db = getReadableDatabase();
     	
     	// Check if this item already is in the collection
     	Cursor cursor = db.rawQuery("select 1 from " + TABLE_NAME +
     			" where " + DribbbleContract.Image._ID + "=?;",
     			new String[] { Long.toString(id) });
     	
     	// TODO: Who is closing the cursor and the db?
 //    	cursor.close();
 //    	db.close();
     	
     	return cursor;
     }
     
 }
