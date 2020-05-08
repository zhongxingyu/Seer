 package com.komodo.tagin;
 
 /**
  * Komodo Lab: Tagin! Project: 3D Tag Cloud
  * Google Summer of Code 2011
  * @author Primal Pappachan
  */
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import ca.idi.taginsdk.Helper;
import ca.idi.taginsdk.TaginDatabase;
import ca.idi.taginsdk.TaginProvider;
 
 public class TagsDatabase {
 	
 	private static final int DATABASE_VERSION = 2; //increment to update Database
 	
 	private static final String DATABASE_NAME = "tags.db";
 	
 	public static final String _ID = "_id"; //Identifier Column of tables
 	
 	private DataBaseHelper mDbHelper; //Database Helper Object
 	private SQLiteDatabase mDb; //Variable to hold database instance
 	
 	//Table Columns
 	public static final String TAG_NAME = "tag_name";
 	public static final String CREATED_AT ="created_at";
 	public static final String BSSID = "bssid";
 	public static final String POPULARITY = "popularity";
 	
 	public static final String TAG_ID = "tag_id";
 	public static final String URN = "urn";
 	
 	//Table Names
 	public static final String TABLE_TAGS = "tags";
 	public static final String TABLE_TAG_DETAILS = "tag_details";
 	
 	//Table Creation Statements
 	 
 	private static final String CREATE_TABLE_TAG_DETAILS = "create table tag_details " +
 			"(_id integer primary key autoincrement, " + "tag_name text not null, "+ "created_at text not null, " +
 					"bssid text not null, " + "popularity integer not null);";
 	
 	private static final String CREATE_TABLE_TAGS = "create table tags "+
 			"(_id integer primary key autoincrement, " + "tag_id integer not null, " + "urn text not null);";
 	
 	private final Context mCtx;
 	
 	private static class DataBaseHelper extends SQLiteOpenHelper{
 		
 		
 		public DataBaseHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 	
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			db.execSQL("DROP TABLE IF EXISTS tag_details");
 			db.execSQL("DROP TABLE IF EXISTS tags");
 			onCreate(db);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(CREATE_TABLE_TAGS);
 			db.execSQL(CREATE_TABLE_TAG_DETAILS);
 		}
 		
 	}
 	
 	public TagsDatabase(Context ctx){
 		this.mCtx = ctx;
 	}
 	
 	public TagsDatabase open() throws SQLException{
 		mDbHelper = new DataBaseHelper(mCtx);
 		mDb = mDbHelper.getWritableDatabase();
 		return this;
 	}
 	
 	public void close(){
 		mDbHelper.close();
 	}
 
 	public long addTagDetails(String tag_name, String time, String bssid, int popularity){
 		ContentValues values = new ContentValues();
 		values.put(TAG_NAME, tag_name);
 		values.put(CREATED_AT, time);
 		values.put(BSSID, bssid);
 		values.put(POPULARITY, popularity);
 		return mDb.insert(TABLE_TAG_DETAILS, null, values);
 	}
 	
 	public void addTag(long tag_id, String urn){
 		ContentValues values = new ContentValues();
 		values.put(TAG_ID, tag_id);
 		values.put(URN, urn);
 		Long row = mDb.insert(TABLE_TAGS, null, values);
 		Log.i(Helper.TAG, "Tag Added" + Long.toString(row));
 		printTableContents();
 		printTagDetails();
 	}
 	
 	public Cursor fetchTagId(String urn){
 		Cursor mCursor;
 		mCursor = mDb.query(TABLE_TAGS, null, URN + "=" + "?", new String[]{urn}, null, null, null);
 		return mCursor.moveToFirst()? mCursor: null;
 	}
 	
 	public Cursor fetchTagDetails(Long tag_id){
 		Cursor mCursor;
 		mCursor = mDb.query(TABLE_TAG_DETAILS, null, _ID + "=" + tag_id, null, null, null, null);
 		return mCursor.moveToFirst()? mCursor: null;
 	}
 	
 	public Cursor fetchTags(Long tag_id){
 		Cursor mCursor;
 		mCursor = mDb.query(TABLE_TAG_DETAILS, new String[]{TAG_NAME}, _ID + "=" + tag_id, null, null, null, null);
 		return mCursor.moveToFirst()? mCursor: null;
 	}
 	
 	private void printTableContents(){
 		Cursor c = mDb.query(TABLE_TAGS, null, null, null, null, null, null);
 		Long id, tag_id;
 		String urn;
 		if(c.moveToFirst()){
 			do{
 				id = c.getLong(c.getColumnIndexOrThrow(TagsDatabase._ID));
 				tag_id = c.getLong(c.getColumnIndexOrThrow(TagsDatabase.TAG_ID));
 				urn = c.getString(c.getColumnIndexOrThrow(TagsDatabase.URN));
 				Log.i(Helper.TAG, "*Tags* : " + id + "," + tag_id +  "," + urn);
 			}while(c.moveToNext());	
 		}
 		c.close();
 	}
 	
 	private void printTagDetails(){
 		Cursor c = mDb.query(TABLE_TAG_DETAILS, null, null, null, null, null, null);
 		Long id;
 		String bssid, tag;
 		int popularity;
 		if(c.moveToFirst()){
 			do{
 				id = c.getLong(c.getColumnIndexOrThrow(TagsDatabase._ID));
 				popularity = c.getInt(c.getColumnIndexOrThrow(TagsDatabase.POPULARITY));
 				bssid = c.getString(c.getColumnIndexOrThrow(TagsDatabase.BSSID));
 				tag = c.getString(c.getColumnIndexOrThrow(TagsDatabase.TAG_NAME));
 				Log.i(Helper.TAG, "*Tag Details* : " + id + "," + tag + ","  + bssid +  "," + popularity);
 			}while(c.moveToNext());	
 		}
 		c.close();
 	}
 }
