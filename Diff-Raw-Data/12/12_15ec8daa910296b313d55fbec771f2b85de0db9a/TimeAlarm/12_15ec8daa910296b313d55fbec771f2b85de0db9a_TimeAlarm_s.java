 package se.mah.kd330a.project.itsl;
 
 import se.mah.kd330a.project.R;
 import java.util.ArrayList;
 import java.util.Date;
 import android.app.IntentService;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.TaskStackBuilder;
 import android.util.Log;
 
 public class TimeAlarm extends IntentService implements FeedManager.FeedManagerDoneListener
 {
 	private static final String TAG = "TimeAlarm";
 	private Date latestUpdate;
 	
 	public TimeAlarm()
 	{
 		super(TAG);
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent)
 	{
 		if (getApplicationContext() != null)
 		{
			Log.i(TAG, "Updating content...");
			
 			latestUpdate = Util.getLatestUpdate(getApplicationContext());
 			FeedManager feedManager = new FeedManager(this, getApplicationContext());
 			feedManager.processFeeds();
 		}
 		else
 		{
 			Log.e(TAG, "getApplicationContext(); is null");
 		}
 	}
 
 	public void onFeedManagerProgress(FeedManager fm, int progress, int max)
 	{
 		/*
 		 * we don't care about progress here
 		 */
 	}
 
 	public void onFeedManagerDone(FeedManager fm, ArrayList<Article> articles)
 	{
 		if (articles.isEmpty())
 		{
 			Log.e(TAG, fm.getClass().toString() + " returned 0 articles, are we online?");
 		}
 		else
 		{
 			ArrayList<Article> newArticles = new ArrayList<Article>();
 			for (Article a : articles)
 				if (a.getArticlePubDate().compareTo(latestUpdate) > 0)
 					newArticles.add(a);
 	
 			createNotification(newArticles);
 		}
 	}
 
 	private void createNotification(ArrayList<Article> articles)
 	{
 		//invoking the default notification service
 		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
 		mBuilder.setContentTitle("New Message");
 		mBuilder.setTicker("New Itslearning post");
 		mBuilder.setSmallIcon(R.drawable.ic_launcher);
 		mBuilder.setAutoCancel(true);
 
 		// Add Big View Specific Configuration 
 		NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
 
 		// Sets a title for the Inbox style big view
 		inboxStyle.setBigContentTitle("News from Itslearning");
 
 		// Moves events into the big view
 		for (Article a : articles)
 			inboxStyle.addLine(a.getArticleHeader());
 
 		mBuilder.setStyle(inboxStyle);
 
 		// Creates an explicit intent in the app
 		Intent resultIntent = new Intent(this, se.mah.kd330a.project.framework.MainActivity.class);
 
 		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
 		stackBuilder.addParentStack(se.mah.kd330a.project.framework.MainActivity.class);
 
 		// ads the intent that starts the activity to the top of the stack
 		stackBuilder.addNextIntent(resultIntent);
 		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
 
 		mBuilder.setContentIntent(resultPendingIntent);
 
 		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 
 		// mId allows you to update the notification later on.
 		mNotificationManager.notify(0, mBuilder.build());
 
 	}
 }
