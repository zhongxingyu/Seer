 package edu.grinnell.sandb.data;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class ImageStorageHelper extends SQLiteOpenHelper {
 
 	public static final String TABLE_IMAGES = "images";
 	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_ARTICLEID = "article_id";
	public static final String COLUMN_URL = "url";
 	public static final String COLUMN_IMAGE = "image";
	public static final String COLUMN_IMGTITLE = "image_title";
 
 
 	private static final String DATABASE_NAME = "images.db";
 	private static final int DATABASE_VERSION = 3;
 
 	
 	// Database creation sql statement
 	public static final String DATABASE_CREATE = "create table "
 	    + TABLE_IMAGES + "(" + COLUMN_ID
	    + " integer primary key, " 
	    + COLUMN_ARTICLEID + " integer, "
	    + COLUMN_URL + " text, "
 	    + COLUMN_IMAGE + " blob, " 
	    + COLUMN_IMGTITLE + " text "
 	    + ");";
 
 	public ImageStorageHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(DATABASE_CREATE);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		Log.w(ImageStorageHelper.class.getName(),
 		        "Upgrading database from version " + oldVersion + " to "
 		            + newVersion + ", which will destroy all old data");
 		    db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
 		    onCreate(db);
 	}
 
 }
