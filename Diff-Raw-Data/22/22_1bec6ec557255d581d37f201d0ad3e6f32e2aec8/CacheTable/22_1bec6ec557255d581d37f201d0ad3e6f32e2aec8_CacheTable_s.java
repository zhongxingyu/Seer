 package jbeer.dev.twittermonitor.persistence;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import jbeer.dev.twittermonitor.data.domain.Tweet;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 public class CacheTable {
 
 	private static final String DATABASE_TABLE  = "tweet_cache";
 
 	public static final String KEY_AVATAR 		= "avatar";
 	public static final String KEY_USER_NAME 	= "user_name";
 	public static final String KEY_TIMESTAMP 	= "timestamp";
 	public static final String KEY_TWEET 		= "tweet";
 	public static final String KEY_ROWID 		= "_id";
 	private DatabaseHelper mDbHelper;
 	private SQLiteDatabase mDb;
 
 	/**
 	 * Database creation sql statement
 	 */
 	public static final String DATABASE_CREATE =
 			"create table "+DATABASE_TABLE+" ("+KEY_ROWID+" integer primary key autoincrement, "
 					+ KEY_TWEET+" text not null,"
 					+ KEY_TIMESTAMP+" text not null,"
 					+ KEY_USER_NAME+" text not null,"
 					+ KEY_AVATAR+" text not null);";
 
 
 
 
 	private final Context mCtx;
 
 
 	public CacheTable(Context ctx) {
 		this.mCtx = ctx;
 		mDbHelper = new DatabaseHelper(mCtx);
 		mDb = mDbHelper.getWritableDatabase();
 	}
 
 
 	public void close() {
 		mDbHelper.close();
 	}
 
 
 
 	public long addTweet(Tweet tweet) {
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_AVATAR, tweet.getAvatarURl());
 		initialValues.put(KEY_USER_NAME, tweet.getUserName());
 		initialValues.put(KEY_TIMESTAMP, tweet.getTime());
 		initialValues.put(KEY_TWEET, tweet.getTweet());
 		return mDb.insert(DATABASE_TABLE, null, initialValues);
 	}
 
 
 	public List<Tweet> getTweets(){
 		List<Tweet> tweetList = new ArrayList<Tweet>();
 		Cursor tweetCursor = fetchAllTweets();
 
 		if (tweetCursor.moveToFirst() == false){
			return tweetList;
 		}
 		int avatarColumnIndex = tweetCursor.getColumnIndex(KEY_AVATAR);
 		int userNameColumIndex = tweetCursor.getColumnIndex(KEY_USER_NAME);
 		int timestamColumIndex = tweetCursor.getColumnIndex(KEY_TIMESTAMP);
 		int tweetColumIndex = tweetCursor.getColumnIndex(KEY_TWEET);
 
 
 		while(tweetCursor.moveToNext()){
 			Tweet tweet = new Tweet();
 			tweet.setAvatarURl(tweetCursor.getString(avatarColumnIndex));
 			tweet.setTime(tweetCursor.getString(timestamColumIndex));
 			tweet.setUserName(tweetCursor.getString(userNameColumIndex));
 			tweet.setTweet(tweetCursor.getString(tweetColumIndex));
 			tweetList.add(tweet);
 		}
 
 		return tweetList;
 	}
 
 
 	private Cursor fetchAllTweets() {
 		return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TWEET,KEY_TIMESTAMP,KEY_USER_NAME,KEY_AVATAR}, null, null, null, null, null);
 	}
 
 }
 
