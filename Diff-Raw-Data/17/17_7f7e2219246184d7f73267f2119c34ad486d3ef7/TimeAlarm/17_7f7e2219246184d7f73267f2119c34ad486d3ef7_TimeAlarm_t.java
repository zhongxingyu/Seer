 package se.mah.kd330a.project.itsl;
 
 import se.mah.kd330a.project.R;
 
 import java.text.SimpleDateFormat;
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
 			FeedManager feedManager = new FeedManager(this, getApplicationContext());
 			feedManager.processFeeds();
 		}
 		else
 		{
 			Log.e(TAG, "getApplicationContext(); is null");
 		}
 	}
 
 	/* we don't care about progress here */
 	public void onFeedManagerProgress(FeedManager fm, int progress, int max) {}
 	
 	public void onFeedManagerDone(FeedManager fm, ArrayList<Article> articles)
 	{
 		latestUpdate = Util.getLatestUpdate(getApplicationContext());
 
 		if (articles.isEmpty())
 		{
 			Log.e(TAG, "Got no articles from onFeedManagerDone, are we online?");
 		}
 		else
 		{
 			ArrayList<Article> newArticles = new ArrayList<Article>();
 			for (Article a : articles)
 				if (a.getArticlePubDate().compareTo(latestUpdate) > 0)
 					newArticles.add(a);
 			
 			if (newArticles.size() > 0)
 				createNotification(newArticles);
 		}
 	}
 
 	private void createNotification(ArrayList<Article> articles)
 	{
 		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
 		stackBuilder.addParentStack(se.mah.kd330a.project.framework.MainActivity.class);
 		stackBuilder.addNextIntent(new Intent(this, se.mah.kd330a.project.framework.MainActivity.class));
 		
 		NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
 		inboxStyle.setBigContentTitle("Itslearning updates");
 		inboxStyle.setSummaryText("Number of updates");
 		
 		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
 		mBuilder.setSmallIcon(R.drawable.ic_menu_itsl);
 		mBuilder.setAutoCancel(true);
 		mBuilder.setContentInfo(Integer.toString(articles.size()));
 		mBuilder.setWhen(System.currentTimeMillis());
 		mBuilder.setContentIntent(stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT));
 		mBuilder.setStyle(inboxStyle);
 		
 		for (Article a : articles)
 		{
 			inboxStyle.addLine(String.format("%5.5s %s" , 
 					new SimpleDateFormat("dd/MM").format(a.getArticlePubDate()), 
 					a.getArticleHeader()));
 		}
 
 		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		mNotificationManager.notify(0, mBuilder.build());
 	}
 }
