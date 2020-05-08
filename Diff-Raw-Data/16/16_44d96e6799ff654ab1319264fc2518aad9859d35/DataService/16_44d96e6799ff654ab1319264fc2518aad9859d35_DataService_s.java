 /*
  * Copyright 2012 Feedlr
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.chalmers.feedlr.service;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.chalmers.feedlr.activity.FeedActivity;
 import com.chalmers.feedlr.client.FacebookHelper;
 import com.chalmers.feedlr.client.TwitterHelper;
 import com.chalmers.feedlr.database.DatabaseHelper;
 import com.chalmers.feedlr.model.FacebookItem;
 
 import com.chalmers.feedlr.model.Feed;
 import com.chalmers.feedlr.model.TwitterItem;
 import com.chalmers.feedlr.model.User;
 import com.chalmers.feedlr.parser.FacebookJSONParser;
 import com.chalmers.feedlr.parser.TwitterJSONParser;
 import com.facebook.android.FacebookError;
 
 import android.app.Service;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 
 /**
  * The DataService class handles requests, response parsing and database storage
  * of client data. On call from an Activity the service makes the necessary
  * requests to client APIs, parses the received JSON response and stores all
  * relevant data to the application database. When a complete request has been
  * made and all data has successfully been saved to the database a broadcast is
  * made to inform listeners that new data is avalable for display.
  * 
  * @author Olle Werme
  */
 
 public class DataService extends Service {
 	private final IBinder binder = new FeedServiceBinder();
 
 	private LocalBroadcastManager lbm;
 
 	private TwitterHelper twitter;
 	private FacebookHelper facebook;
 
 	private DatabaseHelper db;
 
 	public class FeedServiceBinder extends Binder {
 		DataService getService() {
 			return DataService.this;
 		}
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		lbm = LocalBroadcastManager.getInstance(DataService.this);
 
 		twitter = new TwitterHelper(this);
 		facebook = new FacebookHelper();
 
 		db = new DatabaseHelper(this);
 
 		return START_STICKY;
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return binder;
 	}
 
 	/**
 	 * Simple method for running tasks asynchronous.
 	 * 
	 * @param runnable the runnable to be run
 	 */
 	private void runAsync(final Runnable runnable) {
 		new Thread() {
 			@Override
 			public void run() {
 				runnable.run();
 			}
 		}.start();
 	}
 
 	/**
 	 * Populates the application database ITEM table with the most recent tweets
	 * from the registered users twitter timeline.
	 * This method is currently not in use.
 	 */
 	public void updateTwitterTimeline() {
 		runAsync(new Runnable() {
 			@Override
 			public void run() {
 				long time = System.currentTimeMillis();
 
 				List<TwitterItem> twitterTimeline = twitter.getTimeline();
 
 				if (twitterTimeline != null) {
 					// Save to database
 					db.addListOfItems(twitterTimeline);
 				}
 
 				// Broadcast update to activity
 				Intent intent = new Intent();
 				intent.setAction(FeedActivity.FEED_UPDATED);
 				lbm.sendBroadcast(intent);
 
 				Log.i(TwitterJSONParser.class.getName(),
 						"Time in millis for complete Twitter timeline request: "
 								+ (System.currentTimeMillis() - time));
 			}
 		});
 	}
 
 	/**
 	 * Updates application database USER table with the registered users
 	 * "following users" from his or her Twitter account, also known as
 	 * "friends".
 	 */
 	public void updateTwitterUsers() {
 		runAsync(new Runnable() {
 			@Override
 			public void run() {
 				long time = System.currentTimeMillis();
 				Intent intent = new Intent();
 
 				List<User> users = twitter.getFollowing();
 
 				if (users != null) {
 					// Save to database
 					db.addUsers(users);
 					intent.setAction(FeedActivity.TWITTER_USERS_UPDATED);
 				} else {
 					intent.setAction(FeedActivity.TWITTER_USERS_PROBLEM_UPDATING);
 				}
 
 				// Broadcast update to activity
 				lbm.sendBroadcast(intent);
 
 				Log.i(TwitterJSONParser.class.getName(),
 						"Time in millis for complete Twitter following request: "
 								+ (System.currentTimeMillis() - time));
 			}
 		});
 	}
 
 	/**
 	 * Populates application database ITEM table with the most recent tweets
 	 * from the user with the given userID within a specific feed.
 	 * 
 	 * This method is currently not in use.
 	 * 
 	 * @param userID
 	 *            the id for the user
 	 * @param feed
 	 *            the feed that sent the request
 	 */
 	private void updateTweetsByUser(final String userID, final Feed feed) {
 		runAsync(new Runnable() {
 			@Override
 			public void run() {
 				long time = System.currentTimeMillis();
 				Intent intent = new Intent();
 
 				List<TwitterItem> userTweets = twitter.getUserTweets(userID);
 				if (userTweets != null) {
 					db.addListOfItems(userTweets);
 					intent.setAction(FeedActivity.FEED_UPDATED);
 				} else {
 					intent.setAction(FeedActivity.FEED_PROBLEM_UPDATING);
 				}
 
 				// Broadcast update to activity
 				Bundle b = new Bundle();
 				b.putString("feedTitle", feed.getTitle());
 				intent.putExtras(b);
 				lbm.sendBroadcast(intent);
 
 				Log.i(TwitterJSONParser.class.getName(),
 						"Time in millis for complete Twitter user tweets request: "
 								+ (System.currentTimeMillis() - time));
 			}
 		});
 	}
 
 	 * Populates application database ITEM table with the most recent tweets
 	 * from all the users in the specified feed through {@link
 	 * #updateTweetsByUser(final String userID, final Feed feed)
 	 * updateTweetsByUser}.
 	 * 
 	 * @param feed
 	 *            the feed to be updated
 	 */
 	public void updateFeedTwitterItems(final Feed feed) {
 		runAsync(new Runnable() {
 			@Override
 			public void run() {
 				final List<User> twitterUsersInFeed = new ArrayList<User>();
 
 				Cursor c = db.getUsers(feed, "twitter");
 				c.moveToFirst();
 				while (!c.isAfterLast()) {
 					User u = new User(
 							c.getString(c
 									.getColumnIndex(DatabaseHelper.USER_COLUMN_USERID)),
 							c.getString(c
 									.getColumnIndex(DatabaseHelper.USER_COLUMN_USERNAME)));
 					twitterUsersInFeed.add(u);
 					c.moveToNext();
 				}
 
 				for (User u : twitterUsersInFeed) {
 					updateTweetsByUser(u.getId(), feed);
 				}
 			}
 		});
 	}
 
 	/**
 	 * Updates application database USER table with all the friends the
 	 * registered user has on its facebook account.
 	 */
 
 	public void updateFacebookUsers() {
 		final long time = System.currentTimeMillis();
 
 		facebook.getFriends(new com.facebook.android.AsyncFacebookRunner.RequestListener() {
 
 			@Override
 			public void onComplete(String response, Object state) {
 				if (response != null) {
 					List<User> users = new FacebookJSONParser()
 							.parseUsers(response);
 
 					for (User u : users) {
 						u.setSource("facebook");
 						u.setProfileImageURL(facebook.getProfileImageURL(u
 								.getId()));
 					}
 					db.addUsers(users);
 
 					// Broadcast update to activity
 					Intent intent = new Intent();
 					intent.setAction(FeedActivity.FACEBOOK_USERS_UPDATED);
 					lbm.sendBroadcast(intent);
 
 					Log.i(FacebookJSONParser.class.getName(),
 							"Time in millis for complete Facebook friends request: "
 									+ (System.currentTimeMillis() - time));
 				}
 			}
 
 			@Override
 			public void onFacebookError(FacebookError e, final Object state) {
 				Log.e("Parse", "Facebook Error:" + e.getMessage());
 			}
 
 			@Override
 			public void onFileNotFoundException(FileNotFoundException e,
 					final Object state) {
 				Log.e("Parse", "Resource not found:" + e.getMessage());
 			}
 
 			@Override
 			public void onIOException(IOException e, final Object state) {
 				Log.e("Parse", "Network Error:" + e.getMessage());
 			}
 
 			@Override
 			public void onMalformedURLException(MalformedURLException e,
 					final Object state) {
 				Log.e("Parse", "Invalid URL:" + e.getMessage());
 			}
 		});
 	}
 
 	/**
 	 * Populates the application database ITEM table with the most recent feed
 	 * from the registered users facebook timeline.
 	 * 
 	 * This method is currently not in use.
 	 */
 	public void updateFacebookTimeline() {
 		final long time = System.currentTimeMillis();
 
 		facebook.getTimeline(new com.facebook.android.AsyncFacebookRunner.RequestListener() {
 
 			@Override
 			public void onComplete(String response, Object state) {
 				if (response != null) {
 					List<FacebookItem> facebookTimeline = new FacebookJSONParser()
 							.parseFeed(response);
 
 					// save to database
 
 					// Broadcast update to activity
 					Intent intent = new Intent();
 					intent.setAction(FeedActivity.FACEBOOK_USERS_UPDATED);
 					lbm.sendBroadcast(intent);
 
 					Log.i(FacebookJSONParser.class.getName(),
 							"Time in millis for complete Facebook friends request: "
 									+ (System.currentTimeMillis() - time));
 				}
 			}
 
 			@Override
 			public void onFacebookError(FacebookError e, final Object state) {
 				Log.e("Parse", "Facebook Error:" + e.getMessage());
 			}
 
 			@Override
 			public void onFileNotFoundException(FileNotFoundException e,
 					final Object state) {
 				Log.e("Parse", "Resource not found:" + e.getMessage());
 			}
 
 			@Override
 			public void onIOException(IOException e, final Object state) {
 				Log.e("Parse", "Network Error:" + e.getMessage());
 			}
 
 			@Override
 			public void onMalformedURLException(MalformedURLException e,
 					final Object state) {
 				Log.e("Parse", "Invalid URL:" + e.getMessage());
 			}
 		});
 
 		// Broadcast update to activity
 		Intent intent = new Intent();
 		intent.setAction(FeedActivity.FACEBOOK_TIMELINE_UPDATED);
 		lbm.sendBroadcast(intent);
 
 		Log.i(FacebookJSONParser.class.getName(),
 				"Time in millis for complete Facebook timeline request: "
 						+ (System.currentTimeMillis() - time));
 
 	}
 
 	/*
 	 * Populates application database ITEM table with the most recent facebook
 	 * feed updates from the users in the feed.
 	 */
 	public void updateFeedFacebookItems(final Feed feed) {
 		final long time = System.currentTimeMillis();
 
 		final List<User> facebookUsersInFeed = new ArrayList<User>();
 		final List<FacebookItem> facebookItemsForUsers = new ArrayList<FacebookItem>();
 
 		Cursor c = db.getUsers(feed, "facebook");
 
 		c.moveToFirst();
 		while (!c.isAfterLast()) {
 			facebookUsersInFeed
 					.add(new User(
 							c.getString(c
 									.getColumnIndex(DatabaseHelper.USER_COLUMN_USERID)),
 							c.getString(c
 									.getColumnIndex(DatabaseHelper.USER_COLUMN_USERNAME))));
 			c.moveToNext();
 		}
 
 		facebook.getFeedsForUsers(facebookUsersInFeed,
 				new com.facebook.android.AsyncFacebookRunner.RequestListener() {
 					private int responses = 0;
 
 					@Override
 					public void onComplete(String response, Object state) {
 
 						try {
 							if (response != null) {
 								facebookItemsForUsers
 										.addAll(new FacebookJSONParser()
 												.parseFeed(response));
 							}
 						} catch (Exception e) {
 							Log.e(getClass().getName(), e.getMessage());
 						}
 						responses++;
 
<<<<<<< HEAD
 						if (responses == facebookUsersInFeed.size()) {
=======
						if (responses == facebookUsersInFeed.size())
>>>>>>> origin/Documentation
 							onAllComplete();
 						}
 					}
 
 					private void onAllComplete() {
 
 						db.addListOfItems(facebookItemsForUsers);
 
 						// Broadcast update to activity
 						Intent intent = new Intent();
 						intent.setAction(FeedActivity.FEED_UPDATED);
 						Bundle b = new Bundle();
 						b.putString("feedTitle", feed.getTitle());
 						intent.putExtras(b);
 						lbm.sendBroadcast(intent);
 
 						Log.i(TwitterJSONParser.class.getName(),
 								"Time in millis for complete update of feed \""
 										+ feed.getTitle()
 										+ "\" facebook items request: "
 										+ (System.currentTimeMillis() - time));
 					}
 
 					@Override
 					public void onFacebookError(FacebookError e,
 							final Object state) {
 						Log.e("Parse", "Facebook Error:" + e.getMessage());
 					}
 
 					@Override
 					public void onFileNotFoundException(
 							FileNotFoundException e, final Object state) {
 						Log.e("Parse", "Resource not found:" + e.getMessage());
 					}
 
 					@Override
 					public void onIOException(IOException e, final Object state) {
 						Log.e("Parse", "Network Error:" + e.getMessage());
 					}
 
 					@Override
 					public void onMalformedURLException(
 							MalformedURLException e, final Object state) {
 						Log.e("Parse", "Invalid URL:" + e.getMessage());
 					}
 				});
 	}
 }
