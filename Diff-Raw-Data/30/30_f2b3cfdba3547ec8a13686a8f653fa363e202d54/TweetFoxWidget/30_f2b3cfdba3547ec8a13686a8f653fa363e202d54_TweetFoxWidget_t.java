 package com.jeremyfox.TweetFox.Widgets;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.widget.RemoteViews;
 import com.jeremyfox.TweetFox.Activities.TweetListActivity;
 import com.jeremyfox.TweetFox.Helpers.PrefsHelper;
 import com.jeremyfox.TweetFox.Interfaces.TweetsRequestCallback;
 import com.jeremyfox.TweetFox.Interfaces.UsernameCallback;
 import com.jeremyfox.TweetFox.Managers.TwitterManager;
 import com.jeremyfox.TweetFox.R;
 import twitter4j.Status;
 import twitter4j.TwitterException;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jeremy
  * Date: 5/21/13
  * Time: 9:04 AM
  */
 public class TweetFoxWidget extends AppWidgetProvider {
 
     public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
         final Context finalContext = context;
         Log.d("TweetFoxWidget", "onUpdate called!");
         final int widgets = appWidgetIds.length;
         for (int i = 0; i < widgets; i++) {
             int appWidgetId = appWidgetIds[i];
 
             Intent intent = new Intent(context, TweetListActivity.class);
             PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
             RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
             views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);
            final TwitterManager tm = TwitterManager.getInstance(context);
 
             String latestTweet = PrefsHelper.getPref(context, PrefsHelper.LATEST_TWEET_KEY);
             String username = PrefsHelper.getPref(context, PrefsHelper.USERNAME_KEY);
 
             if (null != latestTweet) {
                 views.setTextViewText(R.id.widget_tweet, latestTweet);
                 Log.d("TweetFoxWidget", "latestTweet != null");
             } else {
                 views.setTextViewText(R.id.widget_tweet, "");
                 Log.d("TweetFoxWidget", "latestTweet == null");
             }
 
             if (null != username) {
                 views.setTextViewText(R.id.widget_username, username);
                 Log.d("TweetFoxWidget", "username != null");
             } else {
                 views.setTextViewText(R.id.widget_username, "Loading...");
                 Log.d("TweetFoxWidget", "username == null");
             }
 
             tm.getLatestTweet(context, new TweetsRequestCallback() {
                 @Override
                 public void onSuccess(List<Status> tweets) {
                     for (Status status : tweets) {
                         PrefsHelper.setPref(finalContext, PrefsHelper.LATEST_TWEET_KEY, status.getText());
                        tm.getUsername(finalContext, status, new UsernameCallback() {
                            @Override
                            public void onSuccess(String username) {
                                PrefsHelper.setPref(finalContext, PrefsHelper.USERNAME_KEY, username);
                            }

                            @Override
                            public void onFailure(int statusCode) {}
                        });
                     }
                 }
 
                 @Override
                public void onFailure(int statusCode) {}
             });
 
             appWidgetManager.updateAppWidget(appWidgetId, views);
         }
     }
 }
