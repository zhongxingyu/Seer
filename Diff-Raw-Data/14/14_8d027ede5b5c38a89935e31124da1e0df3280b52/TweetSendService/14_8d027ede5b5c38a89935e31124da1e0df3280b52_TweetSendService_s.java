 package de.geotweeter.services;
 
 import java.util.LinkedList;
 
import de.geotweeter.Constants;
import de.geotweeter.R;
import de.geotweeter.SendableTweet;
import de.geotweeter.exceptions.TemporaryTweetSendException;
import de.geotweeter.exceptions.TweetSendException;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.Service;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Log;
 
 public class TweetSendService extends Service {
 	private NotificationManager notificationManager;
 	private static final String LOG = "TweetSendService";
 	private final IBinder binder = new TweetSendBinder();
 	private LinkedList<SendableTweet> tweets = new LinkedList<SendableTweet>();
 	private Notification notification;
 	private int i = 0;
 	private TweetSenderThread tweetSenderThread;
 	private int bindCount = 0;
 
 	public class TweetSendBinder extends Binder {
 		public TweetSendService getService() {
 			return TweetSendService.this;
 		}
 	}
 	
 	@Override
 	public void onCreate() {
 		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		Log.d(LOG, "Started.");
 	}
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startID) {
 		Log.d(LOG, "Received startID " + startID + ": " + intent);
 		return START_STICKY;
 	}
 	
 	@Override
 	public void onDestroy() {
 		Log.d(LOG, "Destroyed.");
 	}
 	
 	public IBinder onBind(Intent intent) {
 		bindCount++;
 		return binder;
 	}
 	
 	@Override
 	public boolean onUnbind(Intent intent) {
 		bindCount--;
 		return super.onUnbind(intent);
 	}
 	
 	public void addSendableTweet(SendableTweet tweet) {
 		tweets.add(tweet);
 		updateNotification();
 		if (tweetSenderThread == null) {
 			tweetSenderThread = new TweetSenderThread();
 			new Thread(tweetSenderThread, "TweetSenderThread").start();
 		}
 	}
 	
 	private void updateNotification() {
 		if (notification == null) {
 			notification = new Notification();
 		}
 		notification.icon = R.drawable.ic_launcher;
 		notification.tickerText = "Sende Tweet " + (i+1) + "/" + tweets.size();
 		notification.when = System.currentTimeMillis();
 		notification.flags |= Notification.FLAG_ONGOING_EVENT;
 		notification.flags |= Notification.FLAG_NO_CLEAR;
 		notification.setLatestEventInfo(getApplicationContext(), notification.tickerText, "Sending...", null);
 		notificationManager.notify(Constants.SENDING_TWEET_STATUS_NOTIFICATION_ID, notification);
 	}
 	
 	private void removeNotification() {
 		Log.d(LOG, "Removing notification.");
 		notificationManager.cancel(Constants.SENDING_TWEET_STATUS_NOTIFICATION_ID);
 	}
 	
 	private class TweetSenderThread implements Runnable {
 		@Override
 		public void run() {
 			SendableTweet tweet;
 			while (i < tweets.size()) {
 				updateNotification();
 				tweet = tweets.get(i);
 				try {
 					if (tweet.imagePath != null) {
 						tweet.account.sendTweetWithPic(tweet);
 					} else {
 						tweet.account.sendTweet(tweet);
 					}
 				} catch (TemporaryTweetSendException e) {
 					Log.d(LOG, "TemporaryTweetSendException fired. Sleeping 60 seconds.");
 					try { Thread.sleep(60000); } catch(InterruptedException e1) {}
 					i--;
 				} catch (Exception e) {
 					// TODO Inform the user
 					Log.e(LOG, "PermanentTweetException (or another exception) fired. Stopping.");
 					e.printStackTrace();
 					break;
 				}
 				i++;
 			}
 			tweets.clear();
 			i = 0;
 			
 			removeNotification();
 			
 			if (bindCount == 0) {
 				stopSelf();
 			}
 		}
 	}
 }
