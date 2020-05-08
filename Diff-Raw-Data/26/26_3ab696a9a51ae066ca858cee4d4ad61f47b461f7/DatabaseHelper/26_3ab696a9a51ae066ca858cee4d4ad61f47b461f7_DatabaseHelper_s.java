 /**
  * Class description
  * 
  * @user David Goransson
  */
 
 package com.chalmers.feedlr.database;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.chalmers.feedlr.model.Feed;
 import com.chalmers.feedlr.model.Item;
 import com.chalmers.feedlr.model.User;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.sqlite.SQLiteConstraintException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 
 	// Database static variables
 
 	private static final int DATABASE_VERSION = 6;
 
 	private static final String DATABASE_NAME = "feedlrDatabase";
 
 	// Declaring feed table
 	public static final String TABLE_FEED = "feed";
 	public static final String FEED_COLUMN_ID = "_id";
 	public static final String FEED_COLUMN_NAME = "name";
 
 	// Declaring feed-user bridge table
 	public static final String TABLE_FEEDUSER = "feeduser";
 	public static final String FEEDUSER_COLUMN_FEED_ID = "feed_ID";
 	public static final String FEEDUSER_COLUMN_USER_ID = "user_ID";
 
 	// Declaring user table
 	public static final String TABLE_USER = "user";
 	private static final String USER_COLUMN_ID = "_id";
 	public static final String USER_COLUMN_USERNAME = "username";
 	public static final String USER_COLUMN_USERID = "userid";
 	public static final String USER_COLUMN_IMGURL = "ProfileImageURL";
 	public static final String USER_COLUMN_SOURCE = "source";
 
 	// Declaring item table
 	public static final String TABLE_ITEM = "item";
 	private static final String ITEM_COLUMN_ID = "_id";
 	public static final String ITEM_COLUMN_ITEMID = "itemid";
 	public static final String ITEM_COLUMN_TEXT = "text";
 	public static final String ITEM_COLUMN_TIMESTAMP = "timestamp";
 	private static final String ITEM_COLUMN_TYPE = "type";
 	private static final String ITEM_COLUMN_URL = "URL";
 	public static final String ITEM_COLUMN_IMGURL = "imgURL";
 	private static final String ITEM_COLUMN_USER_ID = "user_ID";
 	public static final String ITEM_COLUMN_USERNAME = "username";
 
 	private SQLiteDatabase db = this.getWritableDatabase();
 
 	public DatabaseHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	public void onCreate(SQLiteDatabase database) {
 		// @formatter:off
 		// Creating feed table
 		database.execSQL("CREATE TABLE " + TABLE_FEED + "(" + FEED_COLUMN_ID
 				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + FEED_COLUMN_NAME
 				+ " TEXT UNIQUE" + ")");
 
 		// Creating feed-user bridge table
 		database.execSQL("CREATE TABLE " + TABLE_FEEDUSER + "("
 				+ FEEDUSER_COLUMN_FEED_ID + " INT NOT NULL,"
 				+ FEEDUSER_COLUMN_USER_ID + " INT NOT NULL" + ")");
 
 		// Creating user table
 		// TODO Should username be the unique idenifier of a user?!
 		database.execSQL("CREATE TABLE " + TABLE_USER + "(" + USER_COLUMN_ID
 				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + USER_COLUMN_USERNAME
 				+ " TEXT NOT NULL," + USER_COLUMN_USERID + " TEXT UNIQUE,"
 				+ USER_COLUMN_IMGURL + " TEXT," + USER_COLUMN_SOURCE
 				+ " TEXT NOT NULL" + ")");
 
 		// Creating item table
 		database.execSQL("CREATE TABLE " + TABLE_ITEM + "(" + ITEM_COLUMN_ID
 				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + ITEM_COLUMN_ITEMID
 				+ " INT UNIQUE," + ITEM_COLUMN_TEXT + " TEXT,"
 				+ ITEM_COLUMN_TIMESTAMP + " INT," + ITEM_COLUMN_TYPE + " TEXT,"
 				+ ITEM_COLUMN_URL + " TEXT," + ITEM_COLUMN_IMGURL + " TEXT,"
 				+ ITEM_COLUMN_USER_ID + " INT NOT NULL," + ITEM_COLUMN_USERNAME
 				+ " TEXT" + ")");
 		// @formatter:on
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// Temporarily drops all tables
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_FEED);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_FEEDUSER);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEM);
 		onCreate(db);
 	}
 
 	// Feed related methods:
 
 	public void addFeed(Feed feed) throws SQLiteException {
 		db.insertOrThrow(TABLE_FEED, null, feedCV(feed));
 	}
 
 	public void removeFeed(Feed feed) throws IllegalArgumentException {
 		String title = feed.getTitle();
 		long id = getFeedID(feed);
 
 		removeFeedBridge(id);
 
 		db.delete(TABLE_FEED, FEED_COLUMN_NAME + "=?", new String[] { title });
 	}
 
 	public void clearFeeds() {
 		db.delete(TABLE_FEED, null, null);
 		db.delete(TABLE_FEEDUSER, null, null);
 	}
 
 	public ArrayList<String> listFeeds() {
 		final ArrayList<String> feeds = new ArrayList<String>();
 
 		Cursor c = db.rawQuery("SELECT * FROM " + TABLE_FEED, null);
 		while (c.moveToNext()) {
 			String s = c.getString(1);
 			feeds.add(s);
 		}
 		c.close();
 		return feeds;
 	}
 
 	public long getFeedID(Feed feed) throws IllegalArgumentException {
 		String feedTitle = feed.getTitle();
 		Long id;
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor c = db.query(TABLE_FEED, new String[] { FEED_COLUMN_ID },
 				FEED_COLUMN_NAME + "=?", new String[] { feedTitle }, null,
 				null, null);
 
 		if (c.moveToNext()) {
 			id = Long.parseLong(c.getString(0));
 		} else {
 			throw (new IllegalArgumentException("Feed does not exist!"));
 		}
 		c.close();
 		return id;
 	}
 
 	// User related methods
 
 	public long addUser(User user) {
 		Long userID;
 		try {
 			userID = getUserID(user);
 			updateUser(user);
 		} catch (IllegalArgumentException e) {
 			userID = db.insert(TABLE_USER, null, userCV(user));
 		}
 		return userID;
 	}
 
 	public void addUsers(List<? extends User> users) {
 
 		db.beginTransaction();

 		for (User u : users) {
 			db.insertWithOnConflict(TABLE_USER, null, userCV(u),
 					SQLiteDatabase.CONFLICT_IGNORE);
 		}
 		db.setTransactionSuccessful();
 		db.endTransaction();
 	}
 
 	public long updateUser(User user) {
 		return db.update(TABLE_USER, userCV(user), USER_COLUMN_ID + " = "
 				+ user.getId(), null);
 	}
 
 	private void removeUser(User user) {
 		long id = user.getId();
 		db.delete(TABLE_USER, USER_COLUMN_ID + "=?", new String[] { id + "" });
 	}
 
 	public void clearUserTable() {
 		db.delete(TABLE_USER, null, null);
 	}
 
 	public ArrayList<String> listUsers() {
 		final ArrayList<String> users = new ArrayList<String>();
 
 		Cursor c = db.rawQuery("SELECT * FROM " + TABLE_USER, null);
 
 		while (c.moveToNext()) {
 			String s = c.getString(1);
 			users.add(s);
 		}
 		return users;
 	}
 
 	private long getUserID(User user) throws IllegalArgumentException {
 		long id = user.getId();
 		long userID;
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor c = db.query(TABLE_USER, new String[] { USER_COLUMN_ID },
 				USER_COLUMN_ID + "=?", new String[] { id + "" }, null, null,
 				null);
 		if (c.moveToNext()) {
 			userID = Long.parseLong(c.getString(0));
 		} else {
 			throw (new IllegalArgumentException("User does not exist!"));
 		}
 		c.close();
 		return userID;
 	}
 
 	// Item related methods
 
 	public long addItem(Item item) {
 
 		return db.insert(TABLE_ITEM, null, itemCV(item));
 	}
 
 	public void addListOfItems(List<? extends Item> itemList) {
 		db.beginTransaction();
 		for (Item i : itemList) {
 			addItem(i);
 		}
 		db.setTransactionSuccessful();
 		db.endTransaction();
 	}
 
 	public void clearItemTable() {
 		db.delete(TABLE_ITEM, null, null);
 	}
 
 	// Bridge methods:
 
 	public void addUserToFeed(User user, Feed feed) {
 		long FeedID = getFeedID(feed);
 		long UserID = addUser(user);
 
 		// Add bridge connection
 		if (UserID != -1) {
 			addFeedUserBridge(FeedID, UserID);
 		}
 	}
 
 	public void removeUserFromFeed(Feed feed, User user) {
 		long feedID = getFeedID(feed);
 		long userID = getUserID(user);
 
 		removeFeedUserBridge(feedID, userID);
 	}
 
 	public void addFeedUserBridge(long feedID, long userID) {
 		ContentValues temp = new ContentValues();
 
 		temp.put(FEEDUSER_COLUMN_FEED_ID, feedID);
 		temp.put(FEEDUSER_COLUMN_USER_ID, userID);
 
 		db.insert(TABLE_FEEDUSER, null, temp);
 	}
 
 	private void removeFeedUserBridge(long feedID, long userID) {
 		db.delete(TABLE_FEEDUSER, FEEDUSER_COLUMN_FEED_ID + "=?" + " and "
 				+ FEEDUSER_COLUMN_USER_ID + "=?", new String[] { feedID + "",
 				userID + "" });
 	}
 
 	private void removeFeedBridge(Long id) {
 		db.delete(TABLE_FEEDUSER, FEEDUSER_COLUMN_FEED_ID + "=?",
 				new String[] { id + "" });
 	}
 
 	public Cursor getUsers(Feed feed, String source) {
 
 		Cursor c;
 		if (source.equals(null))
 			c = db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE "
 					+ USER_COLUMN_USERID + " IN (SELECT "
 					+ FEEDUSER_COLUMN_USER_ID + " FROM " + TABLE_FEEDUSER
 					+ " WHERE " + FEEDUSER_COLUMN_FEED_ID + " = "
 					+ getFeedID(feed) + ")", null);
 		else {
 			c = db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE "
 					+ USER_COLUMN_SOURCE + " = ?" + " AND "
 					+ USER_COLUMN_USERID + " IN (SELECT "
 					+ FEEDUSER_COLUMN_USER_ID + " FROM " + TABLE_FEEDUSER
 					+ " WHERE " + FEEDUSER_COLUMN_FEED_ID + " = "
					+ getFeedID(feed) + ")", new String[] { "source" });
 		}
 		return c;
 	}
 
 	public Cursor getItems(Feed feed) {
 		Cursor c = db.rawQuery("SELECT * FROM " + TABLE_ITEM + " WHERE "
 				+ ITEM_COLUMN_USER_ID + " IN (SELECT "
 				+ FEEDUSER_COLUMN_USER_ID + " FROM " + TABLE_FEEDUSER
 				+ " WHERE " + FEEDUSER_COLUMN_FEED_ID + " = " + getFeedID(feed)
 				+ ") ORDER BY " + ITEM_COLUMN_TIMESTAMP + " DESC", null);
 		return c;
 	}
 
 	public Cursor getAllItems() {
 		Cursor c = db.query(TABLE_ITEM, new String[] { ITEM_COLUMN_ID,
 				ITEM_COLUMN_TEXT, ITEM_COLUMN_TIMESTAMP, ITEM_COLUMN_TYPE,
 				ITEM_COLUMN_URL, ITEM_COLUMN_IMGURL, ITEM_COLUMN_USER_ID,
 				ITEM_COLUMN_USERNAME }, null, null, null, null, null);
 		// db.close();
 		return c;
 	}
 
 	public Cursor getAllUsers() {
 		Cursor c = db.query(TABLE_USER, new String[] { USER_COLUMN_ID,
 				USER_COLUMN_USERNAME, USER_COLUMN_USERID }, null, null, null,
 				null, null);
 		return c;
 	}
 
 	public long getItemTableSize() {
 		SQLiteDatabase database = this.getReadableDatabase();
 		long l = DatabaseUtils.queryNumEntries(database, TABLE_ITEM);
 		// database.close();
 		return l;
 	}
 
 	// Contentvalue creaters for each table
 
 	private ContentValues feedCV(Feed feed) {
 		ContentValues cv = new ContentValues();
 		cv.put(FEED_COLUMN_NAME, feed.getTitle());
 		return cv;
 	}
 
 	private ContentValues userCV(User user) {
 		ContentValues cv = new ContentValues();
 
 		cv.put(USER_COLUMN_USERNAME, user.getUserName());
 		cv.put(USER_COLUMN_USERID, user.getId());
 		cv.put(USER_COLUMN_IMGURL, user.getProfileImageURL());
 		cv.put(USER_COLUMN_SOURCE, user.getSource());
 		return cv;
 	}
 
 	private ContentValues itemCV(Item i) {
 		ContentValues cv = new ContentValues();
 		cv.put(ITEM_COLUMN_ITEMID, i.getId());
 		cv.put(ITEM_COLUMN_TEXT, i.getText());
 		cv.put(ITEM_COLUMN_TIMESTAMP, i.getTimestamp());
 		cv.put(ITEM_COLUMN_TYPE, i.getText());
 		cv.put(ITEM_COLUMN_URL, i.getURL());
 		cv.put(ITEM_COLUMN_IMGURL, i.getIMGURL());
 		cv.put(ITEM_COLUMN_USER_ID, i.getUser().getId());
 		return cv;
 	}
 }
