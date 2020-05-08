 package com.novel.reader.service;
 
 import java.util.ArrayList;
 
 import android.app.IntentService;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.widget.RemoteViews;
 
 import com.novel.reader.MyNovelActivity;
 import com.novel.reader.R;
 import com.novel.reader.api.NovelAPI;
 import com.novel.reader.entity.Article;
 
 public class DownloadService extends IntentService {
 
     private static final String Tag                     = "DownloadService";
     private Context             context;
     private final int           STATUS_BAR_NOTIFICATION = 10883;
     private NotificationManager nm;
     private long                startTime;
     static ArrayList<Article>   articles                = new ArrayList<Article>(10);
 
     public DownloadService() {
         super("NovelReaderArticleDownloadService");
     }
 
     @Override
     public void onCreate() {
         Log.d(Tag, "onCreate()");
         super.onCreate();
     }
 
     @Override
     public void onDestroy() {
         Log.d(Tag, "onDestroy()");
         super.onDestroy();
     }
 
     @Override
     public void onStart(Intent intent, int startId) {
         Log.d(Tag, "onStart()");
         super.onStart(intent, startId);
     }
 
     public static void addArticle(Article article) {
         articles.add(article);
     }
 
     public static void addArticles(ArrayList<Article> downlodArticles) {
         for (int i = 0; i < downlodArticles.size(); i++) {
             articles.add(downlodArticles.get(i));
         }
     }
 
     @Override
     protected void onHandleIntent(Intent intent) {
         // download article
 
         // String msg = intent.getStringExtra("NovelName");
 
         nm = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
         CharSequence tickerText = "下載中 ...";
         long when = System.currentTimeMillis();
        Notification noti = new Notification(R.drawable.ic_stat_notify, tickerText, when);
         context = this.getApplicationContext();
         Intent notiIntent = new Intent(context, MyNovelActivity.class);
         notiIntent.putExtra("noti", true);
         PendingIntent pi = PendingIntent.getActivity(context, 0, notiIntent, PendingIntent.FLAG_CANCEL_CURRENT);
         noti.flags |= Notification.FLAG_AUTO_CANCEL;
         RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification);
         contentView.setImageViewResource(R.id.status_icon, R.drawable.app_icon);
         contentView.setTextViewText(R.id.status_text, tickerText);
         contentView.setProgressBar(R.id.status_progress, 100, 0, false);
         noti.contentView = contentView;
         noti.contentIntent = pi;
         nm.notify(STATUS_BAR_NOTIFICATION, noti);
         startTime = System.currentTimeMillis();
 
         for (int i = 0; i < articles.size(); i++) {
             if (NovelAPI.downloadArticle(articles.get(i).getNovelId(), articles.get(i), context)) {
                 if (i == articles.size() - 1) {
                     noti.contentView.setTextViewText(R.id.status_text, "下載完成");
                     noti.contentView.setProgressBar(R.id.status_progress, 100, 100, false);
                     nm.notify(STATUS_BAR_NOTIFICATION, noti);
                     articles = new ArrayList<Article>(10);
                 } else {
                     long nowTime = System.currentTimeMillis();
                     if ((nowTime - startTime) < 4000) {
                         continue;
                     } else {
                         startTime = nowTime;
                     }
                     int downloadPercent = (int) (((float) (i + 1) / articles.size() * 100));
                     CharSequence title = "Downloading: " + downloadPercent + "%   " + articles.get(i).getTitle();
                     noti.contentView.setTextViewText(R.id.status_text, title);
                     noti.contentView.setProgressBar(R.id.status_progress, 100, downloadPercent, false);
                     nm.notify(STATUS_BAR_NOTIFICATION, noti);
                 }
             }
         }
 
     }
 }
