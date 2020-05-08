 package com.DGSD.TweeterTweeter.Services;
 
 import java.util.HashSet;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.util.Log;
 
 import com.DGSD.TweeterTweeter.TTApplication;
 
 public class UpdaterService extends Service {
 	private static final String TAG = UpdaterService.class.getSimpleName();
 
 	public static final String SEND_DATA= 
 		"com.DGSD.TweeterTweeter.SEND_DATA";
 
 	private static final int TIMELINE_DELAY = 60000; // wait a minute
 
 	private static final int FAVOURITES_DELAY = 60000; // wait a minute
 
 	private static final int FOLLOWERS_DELAY = 60000; // wait a minute
 
 	private static final int RETWEET_DELAY = 60000; // wait a minute
 
 	private boolean runFlag = false;
 
 	private TimelineUpdater mTimelineUpdater;
 
 	private FavouritesUpdater mFavouritesUpdater;
 
 	private FollowersUpdater mFollowersUpdater;
 
 	private RetweetUpdater mRetweetUpdater;
 
 	private TTApplication mApplication;
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		this.mApplication = (TTApplication) getApplication(); 
 
 		Log.d(TAG, "onCreated");
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		super.onStartCommand(intent, flags, startId);
 
 		this.runFlag = true;
 
		mTimelineUpdater = new TimelineUpdater();
		mFollowersUpdater = new FollowersUpdater();
		mFavouritesUpdater = new FavouritesUpdater();
		mRetweetUpdater = new RetweetUpdater();
		
 		//Start all updating threads
 		mTimelineUpdater.start();
 
 		mFavouritesUpdater.start();
 
 		mFollowersUpdater.start();
 
 		mRetweetUpdater.start();
 
 		mApplication.setServiceRunning(true); 
 
 		Log.d(TAG, "onStarted");
 		return START_STICKY;
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 
 		this.runFlag = false;
 		mTimelineUpdater.interrupt();
 		mTimelineUpdater = null;
 
 		mFavouritesUpdater.interrupt();
 		mFavouritesUpdater = null;
 
 		mFollowersUpdater.interrupt();
 		mFollowersUpdater = null;
 
 		mRetweetUpdater.interrupt();
 		mRetweetUpdater = null;
 
 		mApplication.setServiceRunning(false); 
 
 		Log.d(TAG, "onDestroyed");
 	}
 
 	/**
 	 * Threads that performs the actual update from the online service
 	 */
 	private class TimelineUpdater extends Thread {
 		public TimelineUpdater() {
 			super("UpdaterService-TimelineUpdater");
 		}
 
 		@Override
 		public void run() {
 			UpdaterService updaterService = UpdaterService.this;
 
 			while (updaterService.runFlag) {
 				Log.d(TAG, "Timeline Updater running");
 				try {
 					HashSet<String> accounts = mApplication.getTwitterSession().getAccountList();
 
 					if(accounts != null) {
 						for(String a : accounts) {
 							// Get the timeline from the cloud & save to db
 							int newUpdates = mApplication.fetchStatusUpdates(a); 
 
 							//Get any new mentions..
 							mApplication.fetchMentions(a);
 
 							if (newUpdates > 0) { 
 								Log.d(TAG, "We have new stat-i");
 								
 								Intent intent = new Intent(SEND_DATA); 
 								//intent.putExtra(NEW_STATUS_EXTRA_COUNT, newUpdates); 
 								updaterService.sendBroadcast(intent); 
 
 							}
 						}
 					}
 
 					Log.d(TAG, "Timeline Updater ran");
 					Thread.sleep(TIMELINE_DELAY);
 				} catch (InterruptedException e) {
 					updaterService.runFlag = false;
 				}
 			}
 		}
 	} //Timeline Updater
 
 	private class FavouritesUpdater extends Thread {
 		public FavouritesUpdater() {
 			super("UpdaterService-FavouritesUpdater");
 		}
 
 		@Override
 		public void run() {
 			UpdaterService updaterService = UpdaterService.this;
 
 			while (updaterService.runFlag) {
 				Log.d(TAG, "Favourites Updater running");
 				try {
 					HashSet<String> accounts = mApplication.getTwitterSession().getAccountList();
 
 					if(accounts != null) {
 						for(String a : accounts) {
 							// Get favourites from the cloud & save to db
 							mApplication.fetchFavourites(a);
 						}
 					}
 
 					Log.d(TAG, "Favourites Updater ran");
 					Thread.sleep(FAVOURITES_DELAY);
 				} catch (InterruptedException e) {
 					updaterService.runFlag = false;
 				}
 			}
 		}
 	} //Favourites Updater
 
 	private class RetweetUpdater extends Thread {
 		public RetweetUpdater() {
 			super("UpdaterService-RetweetUpdater");
 		}
 
 		@Override
 		public void run() {
 			UpdaterService updaterService = UpdaterService.this;
 
 			while (updaterService.runFlag) {
 				Log.d(TAG, "Retweets Updater running");
 				try {
 					HashSet<String> accounts = mApplication.getTwitterSession().getAccountList();
 
 					if(accounts != null) {
 						for(String a : accounts) {
 							mApplication.fetchRetweetsByMe(a);
 
 							mApplication.fetchRetweetsOfMe(a);
 						}
 					}
 
 					Log.d(TAG, "Retweets Updater ran");
 					Thread.sleep(RETWEET_DELAY);
 				} catch (InterruptedException e) {
 					updaterService.runFlag = false;
 				}
 			}
 		}
 	} //Retweet Updater
 
 	private class FollowersUpdater extends Thread {
 		public FollowersUpdater() {
 			super("UpdaterService-FollowersUpdater");
 		}
 
 		@Override
 		public void run() {
 			UpdaterService updaterService = UpdaterService.this;
 
 			while (updaterService.runFlag) {
 				Log.d(TAG, "Followers Updater running");
 				try {
 
 					HashSet<String> accounts = mApplication.getTwitterSession().getAccountList();
 
 					if(accounts != null) {
 						for(String a : accounts) {
 							// Get followers from the cloud & save to db
 							mApplication.fetchFollowers(a);
 
 							// Get following from the cloud & save to db
 							mApplication.fetchFollowing(a);
 
 							// Get information for the current user..
 							mApplication.fetchProfileInfo(a);
 						}
 					}
 					else {
 						Log.d(TAG, "Accounts List was null");
 					}
 					Log.d(TAG, "Followers Updater ran");
 					Thread.sleep(FOLLOWERS_DELAY);
 				} catch (InterruptedException e) {
 					updaterService.runFlag = false;
 				}
 			}
 		}
 	} //Favourites Updater
 }
