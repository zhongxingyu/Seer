 package com.cs301w01.meatload.model;
 
 import com.cs301w01.meatload.activities.FView;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import com.cs301w01.meatload.model.Photo;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 
 /**
  * This class is helper for dealing with SQLite in Android. It provides a variety of useful methods for
  * creating, updating, deleting, and selecting data.
  *
  * Great resource: http://www.codeproject.com/Articles/119293/Using-SQLite-Database-with-Android
  */
 public class DBManager extends SQLiteOpenHelper {
 
     private SQLiteDatabase db;
     private static String logTag = "DBMANAGER";
     private static final String DB_NAME = "skindexDB";
 
     //common db vars
     public static final String COL_ID = "_id";
     public static final String COL_NAME = "name";
     public static final String COL_PHOTOID = "photoID";
     public static final String COL_ALBUMID = "albumID";
     
     //vars for photos table
     public static final String TABLE_NAME_PHOTOS = "photos";
     public static final String PHOTOS_COL_PATH = "path";
     public static final String PHOTOS_COL_DATE = "date";
     
     //vars for tags table
     public static final String TABLE_NAME_TAGS = "tags";
     
     //vars for albums table
     public static final String TABLE_NAME_ALBUMS = "albums";
     
     //vars for album tags table
     public static final String TABLE_NAME_ALBUMTAGS = "albumTags";
 
     private static final int DATABASE_VERSION = 2;
     
     private static final String CREATE_TABLE_PHOTOSTABLE =
             "CREATE TABLE " + TABLE_NAME_PHOTOS + " (" +
                     COL_ID + " INTEGER PRIMARY KEY, " +
                     PHOTOS_COL_DATE + " Date, " +
                     PHOTOS_COL_PATH + " TEXT, " +
                     COL_NAME + " TEXT, " +
                     COL_ALBUMID + " INTEGER, " +
                     "FOREIGN KEY(" + COL_ALBUMID + ") REFERENCES " +
                     TABLE_NAME_ALBUMS + "( " + COL_ID + "));";
     
     private static final String CREATE_TABLE_TAGSTABLE =
             "CREATE TABLE " + TABLE_NAME_TAGS + " (" +
                 COL_ID + " INTEGER PRIMARY KEY, " +
                 COL_PHOTOID + " INTEGER, " +
                 COL_NAME + " TEXT, " +
                 "FOREIGN KEY(" + COL_PHOTOID + ") REFERENCES " +
                                                     TABLE_NAME_PHOTOS + "( " + COL_ID + "));";
 
     private static final String CREATE_TABLE_ALBUMSTABLE =
             "CREATE TABLE " + TABLE_NAME_ALBUMS + " (" +
                     COL_ID + " INTEGER PRIMARY KEY, " +
                     COL_PHOTOID + " INTEGER, " +
                     COL_NAME + " TEXT, " +
                     "FOREIGN KEY(" + COL_PHOTOID + ") REFERENCES " +
                     TABLE_NAME_PHOTOS + "( " + COL_ID + "));";
     
     private static final String CREATE_TABLE_ALBUMTAGSTABLE =
         "CREATE TABLE " + TABLE_NAME_ALBUMTAGS + " (" +
             COL_ID + " INTEGER PRIMARY KEY, " +
             COL_ALBUMID + " INTEGER, " +
             COL_NAME + " TEXT, " +
             "FOREIGN KEY(" + COL_ALBUMID + ") REFERENCES " +
             										TABLE_NAME_ALBUMS + "( " + COL_ID + "));";
 
 
     public DBManager(Context context) {
         super(context, DB_NAME, null, DATABASE_VERSION);
 
     }
 
     @Override
     public void onCreate(SQLiteDatabase newDB) {
 
         db = newDB;
 
     }
 
     private void createTables(){
 
         db.execSQL(CREATE_TABLE_PHOTOSTABLE);
         Log.d(logTag, CREATE_TABLE_PHOTOSTABLE + " generated.");
 
         db.execSQL(CREATE_TABLE_ALBUMSTABLE);
         Log.d(logTag, CREATE_TABLE_ALBUMSTABLE + " generated.");
 
         db.execSQL(CREATE_TABLE_TAGSTABLE);
         Log.d(logTag, CREATE_TABLE_TAGSTABLE + " generated.");
         
         db.execSQL(CREATE_TABLE_ALBUMTAGSTABLE);
         Log.d(logTag, CREATE_TABLE_ALBUMTAGSTABLE + " generated.");
 
         Log.d(logTag, "DB generated.");
 
     }
 
     private void dropTables(){
 
         db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_PHOTOSTABLE);
         Log.d(logTag, TABLE_NAME_PHOTOS + " dropped.");
 
         db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_ALBUMSTABLE);
         Log.d(logTag, TABLE_NAME_ALBUMS + " dropped.");
 
         db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_TAGSTABLE);
         Log.d(logTag, TABLE_NAME_TAGS + " dropped.");
         
         db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_ALBUMTAGSTABLE);
         Log.d(logTag, CREATE_TABLE_ALBUMTAGSTABLE + " dropped.");
 
         Log.d(logTag, "DB generated.");
         
     }
     
     /**
      * Used to force changes to update in the sql table during testing
      */
     public void resetDB() {
 
         SQLiteDatabase db = this.getWritableDatabase();
 
         dropTables();
         createTables();
         
         Log.d(logTag, "TABLES RESET.");
 
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
 
         dropTables();
         onCreate(sqLiteDatabase);
 
     }
 
     /**
      * This method is used to select rows from a table where a specific id is known. Returns a cursor
      * that is preset to the first tuple.
      *
      * @param selectColumns array of columns you wish to have returned
      * @param tableName table name
      * @param idValue
      * @return
      */
     public Cursor selectRowByID(String[] selectColumns, String tableName, String idValue) {
 
         SQLiteDatabase db = this.getWritableDatabase();
 
         Cursor mCursor =
                 db.query(true, tableName, selectColumns, COL_ID + " = " + idValue, null,null,null,null,null);
         if (mCursor != null) {
             mCursor.moveToFirst();
         }
 
         mCursor.moveToFirst();
 
         return mCursor;
 
     }
 
     /**
      * Use this method if you want to do a "SELECT *" style query on a table for a specific ID
      * @param tableName name of table
      * @param id row in table
      * @return
      */
     public Cursor selectAllColsByID(String tableName, long id){
 
         //build the query
         String selectionQuery = "SELECT * FROM " + tableName + " WHERE " + DBManager.COL_ID +
                 " = '" + id + "'";
 
         return performRawQuery(selectionQuery);
 
     }
 
     /**
      * Removes a tuple based on its id value.
      * @param id tuple to delete
      * @param tableName
      */
     public void deleteByID(long id, String tableName){
 
         SQLiteDatabase db = this.getWritableDatabase();
 
         String dQuery = "DELETE FROM " + tableName + " WHERE " + COL_ID  + " = '" + id + "'";
 
         Log.d(logTag, "Performing delete: " + dQuery);
 
         db.execSQL(dQuery);
 
         db.close();
 
     }
 
     /**TODO
      * use the following function to update tags as well
      */
     /**
      * The update method has the following parameters:
      String Table: The table to update a value in
      ContentValues cv: The content values object that has the new values
      String where clause: The WHERE clause to specify which record to update
      String[] args: The arguments of the WHERE clause
      * @param p Fuel Entry object
      * @param tableName
      * @param id
      * @return
      */
     public int updatePhotoByID(Photo p, String tableName, int id)
     {
         SQLiteDatabase db = this.getWritableDatabase();
         ContentValues cv = new ContentValues();
 
         //add photo info to cv
         cv.put(PHOTOS_COL_DATE, dateToString(p.getDate()));
         cv.put(PHOTOS_COL_PATH, p.getPath());
         cv.put(COL_NAME, p.getName());
 
         int uVal = db.update(tableName, cv, COL_ID + "=" + id, null);
 
         db.close();
 
         return uVal;
     }
     
     private int selectIDByName(String name, String tableName){
     	//query to execute
         String select = "SELECT " + COL_ID + 
                         " FROM " + tableName + 
                         " WHERE " + COL_NAME + 
                         " = '" + name + "'";
         
         //execute query and get cursor
         Cursor c = performRawQuery(select);
         
         //return id of photo
         return c.getInt(c.getColumnIndex(COL_ID));
     }
 
     /**
      * Used to find the id of a photo in the photos table for insertion into the albums and tags tables.
      * @param name
      * @return
      */
     private int selectPhotoIDByName(String name){
         return selectIDByName(name, TABLE_NAME_PHOTOS);
     }
     
     private int selectAlbumIDByName(String name){
         return selectIDByName(name, TABLE_NAME_ALBUMS);
     }
     
 
     /**
      * Inserts a photo into the photos table and corresponding tags into the tag table.
      * @param p Photo object
      */
     public void insertPhoto(Photo p) {
 
         SQLiteDatabase db = this.getWritableDatabase();
         ContentValues cv = new ContentValues();
 
         //add photo info to cv
         cv.put(PHOTOS_COL_DATE, dateToString(p.getDate()));
         cv.put(PHOTOS_COL_PATH, p.getPath());
         cv.put(COL_NAME, p.getName());
 
         //insert photo into photo tables
         db.insert(TABLE_NAME_PHOTOS, COL_ID, cv);
 
         //get newly inserted photo's id from photos table
         int pid = selectPhotoIDByName(p.getName());
 
         addPhotoToAlbum(p.getAlbumName(), pid);
 
         //insert photo's tags into tags table
         for(String tag : p.getTags()){
            
             ContentValues tcv = new ContentValues();
             
             cv.put(COL_NAME, tag);
             cv.put(COL_PHOTOID, pid);
 
             //insert tag tuple into tags table
             db.insert(TABLE_NAME_TAGS, COL_ID, tcv);
             
             Log.d(logTag, "Tag inserted: " + tag + " w/ pid: " + pid);
             
         }
 
         db.close();
 
     }
     
     public Collection<String> selectTagsByQuery(String tagQuery){
 
     	Cursor c = performRawQuery(tagQuery);
 
     	Collection<String> tags = new ArrayList<String>();
 
     	while(!c.isLast()){
 
     		tags.add(c.getString(c.getColumnIndex(COL_NAME)));
 
     		c.moveToNext();
     	}
 
 return tags;
     }
 
     public Collection<String> selectAllTags(){
 
         String getTags = "SELECT " + COL_NAME +
                          " FROM " + TABLE_NAME_TAGS;
         
         return selectTagsByQuery(getTags);
 
     }
 
     public Collection<String> selectPhotoTags(int photoID){
         
         String getTags = "SELECT " + COL_NAME +
         					" FROM " + TABLE_NAME_TAGS +
         					" WHERE " + COL_PHOTOID + " = '" + photoID + "'";
         
         return selectTagsByQuery(getTags);
         
     }
     
     public Collection<Photo> selectPhotosByQuery(String photoQuery){
         
         Cursor c = performRawQuery(photoQuery);
 
         Collection<Photo> photos = new ArrayList<Photo>();
 
         while(!c.isLast()){
             
             String photoName = c.getString(c.getColumnIndex(COL_NAME));
             String path = c.getString(c.getColumnIndex(PHOTOS_COL_PATH));
             String albumName = getAlbumNameOfPhoto(c.getInt(c.getColumnIndex(COL_ID)));
             Date date = stringToDate(c.getString(c.getColumnIndex(PHOTOS_COL_DATE)));
                     
             Photo p = new Photo(photoName, path, albumName, date, selectPhotoTags(c.getInt(c.getColumnIndex(COL_ID))));
             
             photos.add(p);
 
             c.moveToNext();
         }
 
         return photos;
     }
     
     public Collection<Photo> selectAllPhotos(){
     	return selectPhotosByQuery("SELECT * FROM " + 
     								TABLE_NAME_PHOTOS + 
     								" ORDER BY " + PHOTOS_COL_DATE);
     }
     
     public Photo selectPhotoByName(String name){
     	Cursor c = performRawQuery("SELECT * FROM " +
 									TABLE_NAME_PHOTOS +
 									" WHERE " + COL_NAME + " = '" + name + "'");
     	
 
         String photoName = c.getString(c.getColumnIndex(COL_NAME));
         String path = c.getString(c.getColumnIndex(PHOTOS_COL_PATH));
         String albumName = getAlbumNameOfPhoto(c.getInt(c.getColumnIndex(COL_ID)));
         Date date = stringToDate(c.getString(c.getColumnIndex(PHOTOS_COL_DATE)));
                 
         return new Photo(photoName, path, albumName, date, selectPhotoTags(c.getInt(c.getColumnIndex(COL_ID))));
     }
     
     public void deletePhotoByName(String name){
     	deleteByID(selectIDByName(name, TABLE_NAME_PHOTOS), TABLE_NAME_PHOTOS);
     }
     
     public void deleteAlbumByName(String name){
     	deleteByID(selectIDByName(name, TABLE_NAME_ALBUMS), TABLE_NAME_ALBUMS);
     }
     
     public Collection<String> selectAllAlbums(){
     	
     	String albumQuery = "SELECT " + COL_NAME + 
     						" FROM " + TABLE_NAME_ALBUMS;
     	
     	Cursor c = performRawQuery(albumQuery);
     	
     	Collection<String> albums = new ArrayList<String>();
     	
    	if(c.getCount() == 0){
     		return albums;
     	}
 
         while(!c.isLast()){
             
             String albumName = c.getString(c.getColumnIndex(COL_NAME));
             
             albums.add(albumName);
 
             c.moveToNext();
         }
 
         return albums;
     }
 
     /**
      * Use this to find the name of the album that a photo is contained in by passing the photo's id as
      * an input parameter.
      * @param photoID
      * @return albumName as a string
      */
     public String getAlbumNameOfPhoto(int photoID) {
         
         Cursor c = performRawQuery("SELECT " + COL_NAME +
                                    " FROM " + TABLE_NAME_ALBUMS +
                                    " WHERE " + COL_PHOTOID + " = " +
                                    "'" + photoID + "'");
         
         return c.getString(c.getColumnIndex(COL_NAME));
         
     }
 
     public void addPhotoToAlbum(String albumName, int photoID){
 
         SQLiteDatabase db = this.getWritableDatabase();
         ContentValues cv = new ContentValues();
 
         cv.put(COL_PHOTOID, photoID);
         cv.put(COL_NAME, albumName);
 
         //insert photo into photo tables
         db.insert(TABLE_NAME_ALBUMS, COL_ID, cv);
         
     }
     
     //TODO fix this function to work properly once the table set ups have been finalized
     public Collection<Photo> selectPhotosFromAlbum(String albumName){
     	int albumID = selectAlbumIDByName(albumName);
     	String query = "SELECT * FROM " + 
     					TABLE_NAME_PHOTOS + 
     					" WHERE " + 
     					COL_ALBUMID + " = " + "'" + albumID + "'" + 
     					" ORDERBY " + PHOTOS_COL_DATE;
     	return selectPhotosByQuery(query);
     }
     
     //TODO fix this function to work properly once the table set ups have been finalized
     public Collection<Photo> selectPhotosByTag(Collection<String> tags){
     	String query = "SELECT p.* FROM " + 
     					"p " + TABLE_NAME_PHOTOS + " LEFT JOIN " + 
     					"t " + TABLE_NAME_TAGS + " ON (p." + COL_ID + " = t." + COL_PHOTOID + ") " +
     					"WHERE ";
     	boolean loopedOnce = false;
     	for(String tag : tags){
     		if(loopedOnce){
     			query += " OR ";
     			loopedOnce = true;
     		}
     		query += "t." + COL_NAME + " = '" + tag + "'";
     	}
     	query += " GROUP BY p.*" + 
     			" ORDER BY COUNT(p.*), p." + PHOTOS_COL_DATE;
     	return selectPhotosByQuery(query);
     }
 
     /**
      * Useful method for converting from date to String for db insertion
      * @param date
      * @return
      */
     public String dateToString(Date date) {
 
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         return dateFormat.format(date);
 
     }
     
     public Date stringToDate(String date){
 
         SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
         Date d = null;
         try {
             return df.parse(date);
         } catch (ParseException e) {
             e.printStackTrace();  
         }
 
         return null;
 
     }
     
     /**
      * Performs a raw SQL Select query. Returns a cursor set to the first result.
      * @param query
      * @return
      */
     public Cursor performRawQuery(String query)
     {
         SQLiteDatabase db = this.getWritableDatabase();
 
         Cursor c = db.rawQuery(query, null);
 
         Log.d(logTag, "Raw query executed: " + query);

         c.moveToFirst();
 
         return c;
 
     }
 
 }
