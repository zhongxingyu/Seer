 package mole.finder;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 
 /**
  * DbController is the database controller for the MoleFinder application,
  * the database contains two tables. One table is for storing the users images
  * and the other table is the tags of the images used for sorting. This class gives the 
  * database functionality such as deleting, inserting and fetching database entries.
  * @author jletourn
  *
  */
 
 /*
  * To store the images in the database they must be stored as a blob. I searched the Internet and
  * found that you convert the image into a byte[] and then can insert it into the DB. once in the DB
  * to turn it back into an image a method needs to be called. These are the methods.
  * 
  * private byte[] getBitmapAsByteArray(Bitmap bitmap) { 
  * 
  * ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); 
  * // Middle parameter is quality, but since PNG is lossless, it s
  * doesn't matter 
  * bitmap.compress(CompressFormat.PNG, 0, outputStream); 
  * return outputStream.toByteArray(); 
  * } 
  * 
  * This method returns the byte[] that can be stored in the DB.
  * 
  * to read the blob from the DB use this:
  * byte[] bitmapData = cursor.getBlob(cursor.getColumnIndex("data")); 
  * 
  * to convert the byte[] back to an image that can be viewed us this:
  * BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length); 
  */
 
 
 public class DatabaseManager{
 
 	/* declaration of global variables used in this class*/
 	public static final String KEY_TAG = "tag";	
 	public static final String KEY_DATE = "date";
 	public static final String KEY_COMMENTS = "comments";
 	public static final String KEY_IMAGE = "image";
 	public static final String KEY_ROWID = "_id";
 
 	private static final String TAG = "DbController";
 	private DatabaseHelper mDbHelper;
 	private SQLiteDatabase mDb;
 
 	private static final String DATABASE_NAME = "ImageLog";
 	private static final String DATABASE_IMAGE_TABLE = "ImageTable";
 	private static final String DATABASE_TAG_TABLE = "TagTable";
 	private static final int DATABASE_VERSION = 2;
 
 	/*
 	 * Database creation sql statement
 	 */
 	private static final String DATABASE_CREATE_IMAGE =
 			"create table " + DATABASE_IMAGE_TABLE + " (_id integer primary key autoincrement, "
 					+ "tag text not null, date text not null, comments text, image BLOB not null);";
 	
 	private static final String DATABASE_CREATE_TAG =
 			"create table " + DATABASE_TAG_TABLE + " (_id integer primary key autoincrement, "
 					+ "tag text not null, comments text);";
 	
 	private final Context mCtx;
 
 	/*
 	 * Helper class that initializes the DatabaseManager object
 	 */
 	private static class DatabaseHelper extends SQLiteOpenHelper {
 
 		DatabaseHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(DATABASE_CREATE_IMAGE);	//runs the create table statement
 			db.execSQL(DATABASE_CREATE_TAG);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
 					+ newVersion + ", which will destroy all old data");
 			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_IMAGE_TABLE);
 			onCreate(db);
 		}
 	}
 
 	/**
 	 * Constructor - takes the context to allow the database to be
 	 * opened/created
 	 * 
 	 * @param ctx the Context within which to work
 	 */
 	public DatabaseManager(Context ctx) {
 		this.mCtx = ctx;
 	}
 
 	/**
 	 * Open the database. If it cannot be opened, try to create a new
 	 * instance of the database. If it cannot be created, throw an exception to
 	 * signal the failure
 	 * 
 	 * @return this (self reference, allowing this to be chained in an
 	 *         initialization call)
 	 * @throws SQLException if the database could be neither opened or created
 	 */
 	public DatabaseManager open() throws SQLException {
 		mDbHelper = new DatabaseHelper(mCtx);
 		mDb = mDbHelper.getWritableDatabase();
 		return this;
 	}
 
 	/**
 	 * Closes the database.
 	 */
 	public void close() {
 		mDbHelper.close();
 	}
 
 
 	/**
 	 * Create a new Image Entry using the information the user provided. If the entry is
 	 * successfully created return the new rowId for that note, otherwise return
 	 * a -1 to indicate failure.
 	 * 
 	 * @param title the title of the note
 	 * @param body the body of the note
 	 * @return rowId or -1 if failed
 	 */
 	public long createImageEntry(String tag, String date, String comments, byte[] image) {
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_TAG, tag);		
 		initialValues.put(KEY_DATE, date);
 		initialValues.put(KEY_COMMENTS, comments);
 		initialValues.put(KEY_IMAGE, image);
 
 		return mDb.insert(DATABASE_IMAGE_TABLE, null, initialValues);
 	}
 	
 	/**
 	 * Create a new Tag Entry using the information the user provided. If the entry is
 	 * successfully created return the new rowId for that note, otherwise return
 	 * a -1 to indicate failure.
 	 * 
 	 * @param title the title of the note
 	 * @param body the body of the note
 	 * @return rowId or -1 if failed
 	 */
 	public long createTagEntry(String tag, String comments) {
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_TAG, tag);		
 		initialValues.put(KEY_COMMENTS, comments);
 
 		return mDb.insert(DATABASE_TAG_TABLE, null, initialValues);
 	}
 	
 
 	/**
 	 * Delete the entry with the given rowId
 	 * 
 	 * @param rowId id of note to delete
 	 * @return true if deleted, false otherwise
 	 */
 	public boolean deleteImageEntry(long rowId) {
 
 		return mDb.delete(DATABASE_IMAGE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
 	}
 
 	
 	/**
 	 * Delete all entries with the specified tag in the database
 	 * 
 	 * @param tag used to categorize the images
 	 * @return true if deleted, false otherwise
 	 */
 	public boolean deleteAllEntries(String tag) {
 
		return (mDb.delete(DATABASE_IMAGE_TABLE, KEY_TAG + " = " + "'" + tag + "'", null) > 0) || 
 				(mDb.delete(DATABASE_TAG_TABLE, KEY_TAG + " = " + "'" + tag + "'", null) > 0);
 	}
 
 	/**
 	 * Return a Cursor over the list of all Images in the database
 	 * 
 	 * @param tag used to categorize the images
 	 * @return Cursor over all notes
 	 */
 	public Cursor fetchAllImages(String tag) {
 		
 
 		return mDb.query(DATABASE_IMAGE_TABLE, new String[] {KEY_TAG, KEY_DATE,
 				KEY_COMMENTS, KEY_IMAGE}, KEY_TAG + " = " + "'" + tag + "'", null, null, null, null);
 	}
 	
 	/**
 	 * Return a Cursor over the list of all tags in the database
 	 * 
 	 * @return Cursor over all tags
 	 */
 	public Cursor fetchTags(){
 		
 		return mDb.query(DATABASE_TAG_TABLE, new String[] {KEY_TAG, KEY_COMMENTS}, null, null, null, null, null);
 	}
 
 }
