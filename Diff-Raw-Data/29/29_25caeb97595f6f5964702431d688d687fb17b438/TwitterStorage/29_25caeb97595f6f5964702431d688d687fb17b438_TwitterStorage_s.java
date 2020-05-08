 package com.outofjungle.apps.quack;
 
 import java.util.ArrayList;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 
 import com.outofjungle.apps.quack.models.Tweet;
 
 public class TwitterStorage {
 	
 	private SQLiteDatabase twitterDatabase;
 	private TwitterStorageHelper twitterHelper;
 	private String[] allColumns = { 
 			TwitterStorageHelper.TWEET_ID,
 			TwitterStorageHelper.TWEET_TEXT,
 			TwitterStorageHelper.USER_ID,
 			TwitterStorageHelper.USER_NAME,
 			TwitterStorageHelper.USER_SCREEN_NAME,
 			TwitterStorageHelper.USER_IMG_URL
 	};
 
 
 	public TwitterStorage(Context context) {
 		twitterHelper = new TwitterStorageHelper(context);
 	}
 	
 	public void open() throws SQLException {
 		twitterDatabase = twitterHelper.getWritableDatabase();
 	}
 	
 	public void close() {
 		twitterDatabase.close();
 	}
 	
 	public void save(ArrayList<Tweet> tweets) {
 		for(Tweet tweet : tweets) {
 			ContentValues values = new ContentValues();
 			values.put(TwitterStorageHelper.TWEET_ID, tweet.getId());
 			values.put(TwitterStorageHelper.TWEET_TEXT, tweet.getText());
 			values.put(TwitterStorageHelper.USER_ID, tweet.getUser().getId());
 			values.put(TwitterStorageHelper.USER_SCREEN_NAME, tweet.getUser().getScreenName());
 			values.put(TwitterStorageHelper.USER_NAME, tweet.getUser().getName());
 			values.put(TwitterStorageHelper.USER_IMG_URL, tweet.getUser().getProfileImageUrl());
 			twitterDatabase.insert(TwitterStorageHelper.TABLE_TWEETS, null, values);
 		}
 	}
 	
 	public ArrayList<Tweet> fetch() {
 		ArrayList<Tweet> tweets = new ArrayList<Tweet>();
 		
 		Cursor cursor = twitterDatabase.query(TwitterStorageHelper.TABLE_TWEETS,
 		        allColumns, null, null, null, null, null);
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			
 			try {
 				JSONObject userJson = new JSONObject();
 				userJson.put("id", cursor.getLong(2));
 				userJson.put("name", cursor.getString(3));
 				userJson.put("screen_name", cursor.getString(4));
 				userJson.put("profile_image_url", cursor.getString(5));
 				
 				JSONObject tweetJson = new JSONObject();
 				tweetJson.put("id", cursor.getLong(0));
 				tweetJson.put("text", cursor.getString(1));
 				tweetJson.put("user", userJson);
 				
 	 			tweets.add(Tweet.fromJson(tweetJson));
 			} catch (JSONException e) {
 				e.printStackTrace();
 				continue;
 			}
 			cursor.moveToNext();
 	    }
 		return tweets;
 	}
 }
