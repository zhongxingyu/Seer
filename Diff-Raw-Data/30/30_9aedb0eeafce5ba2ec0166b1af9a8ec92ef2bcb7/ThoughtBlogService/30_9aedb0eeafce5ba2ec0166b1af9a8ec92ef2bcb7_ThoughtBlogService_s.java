 package com.tw.thoughtblogs.services;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Handler;
 import android.os.IBinder;
 import android.widget.Toast;
 import com.tw.thoughtblogs.BlogListActivity;
 import com.tw.thoughtblogs.R;
 import com.tw.thoughtblogs.RSSReader;
 import com.tw.thoughtblogs.model.Blog;
 import com.tw.thoughtblogs.model.BlogData;
 
 import java.util.Date;
 import java.util.List;
 
 import static com.tw.thoughtblogs.util.Constants.FEED_URL;
 
 public class ThoughtBlogService extends Service {
     private final Handler mHandler = new Handler();
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onCreate() {
         mHandler.postDelayed(contentFetchTask, 300000);
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         mHandler.removeCallbacks(contentFetchTask);
         Toast.makeText(this.getContext(), "ThoughtBlogs Service Stopped.", Toast.LENGTH_LONG).show();
     }
 
     private Runnable contentFetchTask = new Runnable() {
         public void run() {
             Toast.makeText(getContext(), "Loading ThoughtBlogs", Toast.LENGTH_SHORT).show();
             BlogData blogData = new BlogData(getContext());
             Date lastParsedDate = blogData.lastParsedDate();
             blogData.close();
             List<Blog> blogs = new RSSReader(FEED_URL).fetchLatestEntries(lastParsedDate);
             storeBlogs(blogs);
             mHandler.removeCallbacks(contentFetchTask);
             mHandler.postDelayed(contentFetchTask, 3600000);
         }
     };
 
     private Context getContext() {
         return this;
     }
 
     private void storeBlogs(List<Blog> blogs) {
         if (blogs.size() > 0) {
             BlogData blogData = new BlogData(this);
             blogData.store(blogs);
             blogData.close();
             notifyStatusBar(blogs.size());
         }
     }
 
     private void notifyStatusBar(int size) {
         String ns = Context.NOTIFICATION_SERVICE;
        int icon = R.drawable.notification_icon;
         CharSequence tickerText = " New ThoughtBlogs";
         long when = System.currentTimeMillis();
         Notification notification = new Notification(icon, tickerText, when);
         notification.flags = Notification.FLAG_AUTO_CANCEL;
 
         Context context = getApplicationContext();
         CharSequence contentText = size + " new entries posted on ThoughtBlogs.";
         Intent notificationIntent = new Intent(this, BlogListActivity.class);
         PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
         notification.setLatestEventInfo(context, tickerText, contentText, contentIntent);
 
         NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
         mNotificationManager.notify(1, notification);
     }
 }
