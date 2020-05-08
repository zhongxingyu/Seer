 package com.falconware.falconcatcher;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.apache.http.util.ByteArrayBuffer;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDoneException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 import android.os.Environment;
 
 
//TODO: Use ids for everything.  A feed with the same episode twice will wreak havoc otherwise.
//TODO: This file needs A LOT of help
 public class Database {
 	public class TableSettings {
 		private TableSettings() {}
 		public static final String TABLE_NAME="settings";
 		public static final String KEY="key";
 		public static final String VALUE="value";
 		public static final String APPLICATION_DIRECTORY="applicationDirectory";
 		public static final String CURRENT_FILE="currentFile";
 	}
 	
 	public class TableFeed {
 		private TableFeed() {}
 		public static final String TABLE_NAME="feed";
 		public static final String ID="_id";
 		public static final String URL="url";
 		public static final String TITLE="title";
 		public static final String IMAGE="image";
 	}
 	
 	public class TableEpisode {
 		private TableEpisode() {}
 		public static final String TABLE_NAME="episode";
 		public static final String ID="_id";
 		public static final String FEED_TITLE="feedTitle";
 		public static final String URL="url";
 		public static final String TITLE="title";
 		public static final String DESCRIPTION="description";
 		public static final String AUTHOR="author";
 		public static final String PUBLISHED_DATE="publishedDate";
 		public static final String LOCAL_FILE="localFile";
 	}
 	
 	public class TableFile {
 		private TableFile() {}
 		public static final String TABLE_NAME="file";
 		public static final String ID="_id";
 		public static final String EPISODE_TITLE="episodeTitle";
 		public static final String PATH="path";
 		public static final String DISPLAY_ORDER="displayOrder";
 		public static final String PLAY_INDEX="playIndex";
 	}
 	
 	
 //	public enum TableEpisode {
 //		TABLE_NAME("episode"), ID("_id"), FEED_ID("feedId"), URL("url"), 
 //		TITLE("title"), DESCRIPTION("description"), AUTHOR("author"), 
 //		PUBLISHED_DATE("publishedDate"), LOCAL_FILE("localFile");
 //		
 //		private String mText;
 //		
 //		private TableEpisode(String text) {
 //			mText = text;
 //		}
 //		
 //		@Override 
 //		public String toString() {
 //			return mText;
 //		}
 //	}
 //	public enum TableQueue {
 //		ID, EPISODE_ID, PLAY_INDEX
 //	}
 	
 	//private SQLiteDatabase mDb;
 	private DictionaryOpenHelper mHelper;
 	
 	private class DictionaryOpenHelper extends SQLiteOpenHelper {
 		private static final int DATABASE_VERSION = 1;
 		private static final String DATABASE_NAME = "FalconCatcher";
 		private static final String SETTINGS_CREATE =
 				"CREATE TABLE settings (" +
 						"key TEXT, " +
 						"value TEXT);";
 		private static final String FEED_CREATE =
 				"CREATE TABLE feed (" +
 						"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
 						"url TEXT, " +
 						"title TEXT, " +
 						"image TEXT);";
 		
 		private static final String EPISODE_CREATE =
 				"CREATE TABLE episode (" +
 						"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
 						"feedTitle String, " +
 						"url TEXT, " +
 						"title TEXT, " +
 						"description TEXT, " +
 						"author TEXT, " +
 						"publishedDate TEXT);";
 		
 		private static final String FILE_CREATE = 
 				"CREATE TABLE file (" +
 						"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
 						"episodeTitle INTEGER, " +
 						"path STRING, " +
 						"displayOrder INTEGER, " +
 						"playIndex INTEGER);";
 		
 		//Queue: localPath, playIndex, order? 
 
 		DictionaryOpenHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(SETTINGS_CREATE);
 			db.execSQL(FEED_CREATE);
 			db.execSQL(EPISODE_CREATE);
 			db.execSQL(FILE_CREATE);
 			//ContentValues insertValues = new ContentValues();
 			//insertValues.put("key", "applicationDirectory");
 			//db.insert("settings", "value", insertValues);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			// TODO Auto-generated method stub
 
 		}  
 	}
 	
 	public Database(Context context) {
 		mHelper = new DictionaryOpenHelper(context);
 		//mDb = helper.getWritableDatabase();
 	}
 	
 	//public void close() {
 	//	mDb.close();
 	//}
 	
 	public long addFeed(String url, String title, String imageUrl) {
 		ContentValues insertValues = new ContentValues();
 		insertValues.put(TableFeed.URL, url);
 		insertValues.put(TableFeed.TITLE, title);
 		insertValues.put(TableFeed.IMAGE, imageToBuffer(imageUrl));
 		long id = mHelper.getWritableDatabase().insert(TableFeed.TABLE_NAME, null, insertValues);
 		return id;
 	}
 	
 	public long addEpisode(String feedTitle, String episodeUrl, String title, String description,
 			String author, String publishedDate) {
 		ContentValues insertValues = new ContentValues();
 		insertValues.put(TableEpisode.FEED_TITLE, feedTitle);
 		insertValues.put(TableEpisode.URL, episodeUrl);
 		insertValues.put(TableEpisode.TITLE, title);
 		insertValues.put(TableEpisode.DESCRIPTION, description);
 		insertValues.put(TableEpisode.AUTHOR, author);
 		insertValues.put(TableEpisode.PUBLISHED_DATE, publishedDate);
 		long id = mHelper.getWritableDatabase().insert(TableEpisode.TABLE_NAME, null, insertValues);
 		return id;
 	}
 	
 	public long addFile(String episodeTitle, String path) {
 		ContentValues insertValues = new ContentValues();
 		insertValues.put(TableFile.EPISODE_TITLE, episodeTitle);
 		insertValues.put(TableFile.PATH, path);
 		
 		SQLiteDatabase db = mHelper.getWritableDatabase();
 		Cursor cursor = db.rawQuery("SELECT MAX(displayOrder) FROM file", null);
 		if (cursor.getCount() > 0) {
 			cursor.moveToFirst();
 			int displayOrder = cursor.getInt(0) + 1;
 			insertValues.put(TableFile.DISPLAY_ORDER, displayOrder);
 		}
 		else {
 			insertValues.put(TableFile.DISPLAY_ORDER, 0);
 		}
 		//insertValues.put(TableFile.PLAY_INDEX, null);
 		return db.insert(TableFile.TABLE_NAME, null, insertValues);
 	}
 	
 	public long addFile(String episodeTitle, String path, int order) {
 		ContentValues insertValues = new ContentValues();
 		insertValues.put(TableFile.EPISODE_TITLE, episodeTitle);
 		insertValues.put(TableFile.PATH, path);
 		
 		SQLiteDatabase db = mHelper.getWritableDatabase();
 		Cursor cursor = db.rawQuery("SELECT _id, displayOrder FROM file WHERE displayOrder>=?", new String[] {Integer.toString(order)});
 		cursor.moveToFirst();
 		for (int index=0; index<cursor.getCount(); index++) {
 			long id = cursor.getLong(cursor.getColumnIndex(TableFile.ID));
 			int displayOrder = cursor.getInt(cursor.getColumnIndex(TableFile.DISPLAY_ORDER));
 			System.out.println("Add: Moving from " + displayOrder + " to " + (displayOrder+1));
 			SQLiteStatement statement = db.compileStatement("UPDATE file SET displayOrder=? WHERE _id=?");
 			statement.bindLong(1, displayOrder+1);
 			statement.bindLong(2, id);
 			statement.executeUpdateDelete();
 			cursor.moveToNext();
 		}
 		insertValues.put(TableFile.DISPLAY_ORDER, order);
 
 		//insertValues.put(TableFile.PLAY_INDEX, null);
 		return db.insert(TableFile.TABLE_NAME, null, insertValues);
 	}
 	
 	public Cursor getFeeds() {
 		String[] columns = new String[] {TableFeed.ID, TableFeed.URL, TableFeed.TITLE, TableFeed.IMAGE};
 		Cursor cursor = mHelper.getReadableDatabase().query(TableFeed.TABLE_NAME, columns, null, null, null, null, null);
 		cursor.moveToFirst();
 		return cursor;
 	}
 	
 	public Cursor getFeed(String feedId) {
 		String[] columns = new String[] {TableFeed.ID, TableFeed.URL, TableFeed.TITLE, TableFeed.IMAGE};
 		String where = TableFeed.ID + "=?";
 		String[] whereArgs = new String[] {feedId};
 		Cursor cursor = mHelper.getReadableDatabase().query(TableFeed.TABLE_NAME, columns, where, whereArgs, null, null, null);
 		cursor.moveToFirst();
 		return cursor;
 	}
 	
 	public Cursor getEpisodes(String feedId) {
 		String[] columns = new String[] {TableEpisode.ID, TableEpisode.FEED_TITLE, TableEpisode.URL, TableEpisode.TITLE, 
 				TableEpisode.DESCRIPTION, TableEpisode.AUTHOR, TableEpisode.PUBLISHED_DATE};
 		String where = TableEpisode.FEED_TITLE + "=?";
 		String[] whereArgs = new String[] {feedId};
 		Cursor cursor = mHelper.getReadableDatabase().query(TableEpisode.TABLE_NAME, columns, where, whereArgs, null, null, null);
 		cursor.moveToFirst();
 		return cursor;
 	}
 	
 	public Cursor getEpisode(String episodeTitle) {
 		String[] columns = new String[] {TableEpisode.ID, TableEpisode.FEED_TITLE, TableEpisode.URL, TableEpisode.TITLE, 
 				TableEpisode.DESCRIPTION, TableEpisode.AUTHOR, TableEpisode.PUBLISHED_DATE};
 		String where = TableEpisode.TITLE + "=?";
 		String[] whereArgs = new String[] {episodeTitle};
 		Cursor cursor = mHelper.getReadableDatabase().query(TableEpisode.TABLE_NAME, columns, where, whereArgs, null, null, null);
 		cursor.moveToFirst();
 		return cursor;
 	}
 	
 	public Cursor getQueue() {
		//TODO: SELECT DISTINCT is a hack.  Title can't function as a primary key
		Cursor c = mHelper.getReadableDatabase().rawQuery("SELECT DISTINCT f._id, f.path, f.episodeTitle, e.feedTitle " +
 				"FROM file f " +
 				"INNER JOIN episode e on e.title=f.episodeTitle " +
 				"ORDER BY displayOrder", null);
 		//c.moveToFirst();
 		return c;
 	}
 	
 	public Cursor getFile(String episodeTitle) {
 		Cursor c = mHelper.getReadableDatabase().rawQuery("SELECT _id, episodeTitle, path, displayOrder, playIndex " +
 				"FROM file WHERE episodeTitle=?", new String[] {episodeTitle});
 		c.moveToFirst();
 		return c;
 	}
 	
 	public Cursor getFile(long id) {
 		Cursor c = mHelper.getReadableDatabase().rawQuery("SELECT _id, episodeTitle, path, displayOrder, playIndex " +
 				"FROM file WHERE _id=?", new String[] {Long.toString(id)});
 		c.moveToFirst();
 		return c;
 	}
 	
 	public long getFileIdByDisplayOrder(long displayOrder) {
 		Cursor c = mHelper.getReadableDatabase().rawQuery("SELECT _id FROM file where displayOrder=?", new String[] {Long.toString(displayOrder)});
 		if (c == null) {
 			return -1;
 		}
 		c.moveToFirst();
 		long id = c.getLong(c.getColumnIndex(TableFile.ID));
 		c.close();
 		return id;
 	}
 	
 	public void setFilePosition(long fileId, int pos) {
 		SQLiteDatabase db = mHelper.getWritableDatabase();
 		SQLiteStatement statement = db.compileStatement("UPDATE file SET playIndex=? WHERE _id=?");
 		statement.bindLong(1, pos);
 		statement.bindLong(2, fileId);
 		statement.executeUpdateDelete();
 	}
 	
 	public int getFilePosition(long fileId) {
 		Cursor c = mHelper.getReadableDatabase().rawQuery("SELECT playIndex FROM file WHERE _id=?", new String[] {Long.toString(fileId)});
 		if (c.getCount() < 1) {
 			return -1;
 		}
 		c.moveToFirst();
 //		if (c.isNull(c.getColumnIndex(TableFile.PLAY_INDEX))) {
 //			return -1;
 //		}
 		int pos = c.getInt(c.getColumnIndex(TableFile.PLAY_INDEX));
 		c.close();
 		return pos;
 	}
 	
 	public void removeFile(String episodeTitle) {
 		//TODO: Need lots more error handling here
 		SQLiteDatabase db = mHelper.getWritableDatabase();
 		Cursor c = db.rawQuery("SELECT path, displayOrder FROM file WHERE episodeTitle=?", new String[] {episodeTitle});
 		c.moveToFirst();
 		String episodePath = c.getString(c.getColumnIndex(TableFile.PATH));
 		String orderNumber = c.getString(c.getColumnIndex(TableFile.DISPLAY_ORDER));
 		c.close();
 		new File(episodePath).delete();
 		SQLiteStatement statement = db.compileStatement("DELETE FROM file WHERE episodeTitle=?");
 		statement.bindString(1, episodeTitle);
 		statement.executeUpdateDelete();
 		
 		c = db.rawQuery("SELECT _id, displayOrder FROM file WHERE displayOrder>?", new String[] {orderNumber});
 		c.moveToFirst();
 		for (int index=0; index<c.getCount(); index++) {			
 			long id = c.getLong(c.getColumnIndex(TableFile.ID));
 			int initialOrder = c.getInt(c.getColumnIndex(TableFile.DISPLAY_ORDER));
 			System.out.println("Remove: Moving from " + initialOrder + " to " + (initialOrder-1));
 			statement = db.compileStatement("UPDATE file SET displayOrder=? WHERE _id=?");
 			statement.bindLong(1, initialOrder-1);
 			statement.bindLong(2, id);
 			statement.executeUpdateDelete();
 			c.moveToNext();
 		}
 		c.close();
 	}
 	
 	
 	public void removeFeed(String feedTitle) {
 		//TODO: REMOVE FROM QUEUE AND DELETE LOCAL FILE
 		SQLiteDatabase db = mHelper.getWritableDatabase();
 		Cursor episodeTitles = db.rawQuery("SELECT episodeTitle FROM episode WHERE feedTitle=?", new String[] {feedTitle});
 		episodeTitles.moveToFirst();
 		for (int index=0; index<episodeTitles.getCount(); index++) {
 			removeFile(episodeTitles.getString(0));
 			episodeTitles.moveToNext();
 		}
 		
 		
 		SQLiteStatement statement = db.compileStatement("DELETE FROM " + TableEpisode.TABLE_NAME + " WHERE " +
 				TableEpisode.FEED_TITLE + "=?");
 		statement.bindString(1, feedTitle);
 		statement.executeUpdateDelete();
 		statement = db.compileStatement("DELETE FROM " + TableFeed.TABLE_NAME + " WHERE " + TableFeed.TITLE + "=?");
 		statement.bindString(1, feedTitle);
 		statement.executeUpdateDelete();
 	}
 	
 	private byte[] imageToBuffer(String imageUrl) {
 		try {
 			URL url = new URL(imageUrl);  //http://example.com/image.jpg
 			URLConnection con = url.openConnection();
 			InputStream is = con.getInputStream();
 			BufferedInputStream bis = new BufferedInputStream(is,128);
 			ByteArrayBuffer baf = new ByteArrayBuffer(128);
 
 			int current = 0;
 			while ((current = bis.read()) != -1) {
 				baf.append((byte) current);
 			}
 
 			return baf.toByteArray();
 		} catch (Exception e) {
 			//TODO: Do better exception handling
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	
 	public String getApplicationDirectory() {
 		SQLiteDatabase db = mHelper.getWritableDatabase();
 		SQLiteStatement statement = db.compileStatement("SELECT " + TableSettings.VALUE + 
 				" FROM " + TableSettings.TABLE_NAME + " WHERE " + TableSettings.KEY + "='" +
 				TableSettings.APPLICATION_DIRECTORY + "'");
 		String dir;
 		try {
 			dir = statement.simpleQueryForString();
 		} catch (SQLiteDoneException e) {
 			dir = decipherApplicationDirectory();
 			ContentValues insertValues = new ContentValues();
 			insertValues.put(TableSettings.KEY, TableSettings.APPLICATION_DIRECTORY);
 			insertValues.put(TableSettings.VALUE, dir);
 			db.insert(TableSettings.TABLE_NAME, null, insertValues);
 		}
 		return dir;
 	}
 	
 	public void setCurrentFile(long id) {
 		SQLiteDatabase db = mHelper.getWritableDatabase();
 		int currentFileCount = db.rawQuery("SELECT value FROM settings WHERE key='currentFile'", null).getCount();
 		if (currentFileCount < 1) {
 			ContentValues insertValues = new ContentValues();
 			insertValues.put(TableSettings.KEY, "currentFile");
 			insertValues.put(TableSettings.VALUE, id);
 			db.insert(TableSettings.TABLE_NAME, null, insertValues);
 		}
 		else if (currentFileCount == 1) {
 			SQLiteStatement statement = db.compileStatement("UPDATE settings SET value=? WHERE key='currentFile'");
 			statement.bindLong(1, id);
 			statement.executeUpdateDelete();
 		}
 		else {
 			System.err.println("Can't set current file.  Too many entries in the database.");
 		}
 		
 	}
 	
 	public long getCurrentFile() {
 		Cursor c = mHelper.getReadableDatabase().rawQuery("SELECT value FROM settings WHERE key='currentFile'", null);
 		c.moveToFirst();
 		if (c.getCount() < 1) {
 			System.err.println("getCurrentFile returned less than 1");
 			return -1;
 		}
 		long id = c.getLong(c.getColumnIndex(TableSettings.VALUE));
 		return id;
 	}
 	
 	private String decipherApplicationDirectory() {
 		boolean externalAvailable = false;
 		boolean externalWritable = false;
 		String state = Environment.getExternalStorageState();
 		
 		if (Environment.MEDIA_MOUNTED.equals(state)) {
 		    // We can read and write the media
 		    externalAvailable = externalWritable = true;
 		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 		    // We can only read the media
 		    externalAvailable = true;
 		    externalWritable = false;
 		} else {
 		    // Something else is wrong. It may be one of many other states, but all we need
 		    //  to know is we can neither read nor write
 		    externalAvailable = externalWritable = false;
 		}
 		
 		if (externalAvailable && externalWritable) {
 			return Environment.getExternalStorageDirectory().toString() + "/FalconCatcher/";
 		}
 		return Environment.getDataDirectory().toString();
 	}
 }
