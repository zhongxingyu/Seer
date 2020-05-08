 /*
  * Copyright (c) Mattia Barbon <mattia@barbon.org>
  * distributed under the terms of the MIT license
  */
 
 package org.barbon.mangaget;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 
 import android.database.Cursor;
 
 import android.net.ConnectivityManager;
 
 import android.os.Binder;
 import android.os.Environment;
 import android.os.IBinder;
 
 import android.view.View;
 
 import android.widget.RemoteViews;
 
 import java.io.File;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Formatter;
 
 import org.barbon.mangaget.data.DB;
 
 import org.barbon.mangaget.scrape.Downloader;
 
 import org.barbon.mangaget.scrape.Scraper;
 
 public class Download extends Service {
     private static final int COMMAND_DOWNLOAD_CHAPTER = 1;
     private static final int COMMAND_STOP_DOWNLOAD_CHAPTER = 3;
     private static final int COMMAND_UPDATE_MANGA = 2;
     private static final int COMMAND_RESUME_DOWNLOADS = 4;
 
     private static final String COMMAND = "command";
     private static final String MANGA_ID = "mangaId";
     private static final String CHAPTER_ID = "chapterId";
 
     private DB db;
     private File downloadTemp;
     private Map<Long, PendingTask> chapterDownloads =
         new HashMap<Long, PendingTask>();
     private int operationCount;
     private static boolean initialized;
 
     private class DownloadBinder extends Binder {
         public Download getService() {
             return Download.this;
         }
     }
 
     private static class ConnectivityReceiver extends BroadcastReceiver {
         @Override
         public void onReceive(Context context, Intent intent) {
             boolean noConnectivity = intent.getBooleanExtra(
                 ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
 
             if (noConnectivity)
                 return;
 
             context.startService(resumeDownloadsIntent(context));
         }
     }
 
     public static class ServiceManager implements ServiceConnection {
         private Download service;
 
         public void connect(Context context) {
             context.bindService(new Intent(context, Download.class), this, 0);
         }
 
         public void disconnect(Context context) {
             context.unbindService(this);
         }
 
         public Download getService() {
             return service;
         }
 
         @Override
         public void onServiceConnected(ComponentName name, IBinder binder) {
             DownloadBinder downloadBinder = (DownloadBinder) binder;
             service = downloadBinder.getService();
         }
 
         @Override
         public void onServiceDisconnected(ComponentName name) {
             service = null;
         }
     }
 
     @Override
     public void onCreate() {
         db = DB.getInstance(this);
 
         File externalStorage = Environment.getExternalStorageDirectory();
 
         downloadTemp = new File(externalStorage, "MangaGet");
 
         if (!downloadTemp.exists())
             downloadTemp.mkdir();
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         // resume pending downloads on restart
         if (intent == null) {
             if (Utils.isNetworkConnected(this))
                 resumeDownloads();
 
             // should never happen
             stopIfIdle();
 
             return START_STICKY;
         }
 
         int command = intent.getIntExtra(COMMAND, -1);
 
         switch (command) {
         case COMMAND_UPDATE_MANGA:
             updateManga(intent.getLongExtra(MANGA_ID, -1L));
             break;
         case COMMAND_DOWNLOAD_CHAPTER:
             downloadChapter(intent.getLongExtra(CHAPTER_ID, -1L));
             break;
         case COMMAND_STOP_DOWNLOAD_CHAPTER:
             stopDownloadChapter(intent.getLongExtra(CHAPTER_ID, -1L));
             break;
         case COMMAND_RESUME_DOWNLOADS:
             resumeDownloads();
             break;
         }
 
         return START_STICKY;
     }
 
     private final IBinder binder = new DownloadBinder();
 
     @Override
     public IBinder onBind(Intent intent) {
         return binder;
     }
 
     // public interface
 
     public static void startChapterDownload(Context context, long chapterId) {
         context.startService(chapterDownloadIntent(context, chapterId));
     }
 
     public static void stopChapterDownload(Context context, long chapterId) {
         context.startService(chapterStopDownloadIntent(context, chapterId));
     }
 
     public static void startMangaUpdate(Context context, long mangaId) {
         Intent intent = new Intent(context, Download.class);
 
         intent.putExtra(COMMAND, COMMAND_UPDATE_MANGA);
         intent.putExtra(MANGA_ID, mangaId);
 
         context.startService(intent);
     }
 
     public static void initialize(Context context) {
         if (initialized)
             return;
 
         // listen for connectivity status changes
         IntentFilter filter =
             new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
 
         context.registerReceiver(new ConnectivityReceiver(), filter);
 
         // resume pending dowloads
         if (Utils.isNetworkConnected(context))
             context.startService(resumeDownloadsIntent(context));
     }
 
     // implementation
 
     private static Intent chapterDownloadIntent(
             Context context, long chapterId) {
         Intent intent = new Intent(context, Download.class);
 
         intent.putExtra(COMMAND, COMMAND_DOWNLOAD_CHAPTER);
         intent.putExtra(CHAPTER_ID, chapterId);
 
         return intent;
     }
 
     private static Intent chapterStopDownloadIntent(
             Context context, long chapterId) {
         Intent intent = new Intent(context, Download.class);
 
         intent.putExtra(COMMAND, COMMAND_STOP_DOWNLOAD_CHAPTER);
         intent.putExtra(CHAPTER_ID, chapterId);
 
         return intent;
     }
 
     private static Intent resumeDownloadsIntent(Context context) {
         Intent intent = new Intent(context, Download.class);
 
         intent.putExtra(COMMAND, COMMAND_RESUME_DOWNLOADS);
 
         return intent;
     }
 
     private class MangaUpdateProgress implements Scraper.OnOperationStatus {
         private long mangaId;
 
         public MangaUpdateProgress(long _mangaId) {
             mangaId = _mangaId;
         }
 
         @Override
         public void operationStarted() {
             Notifier.getInstance().notifyMangaUpdateStarted(mangaId);
         }
 
         @Override
         public void operationComplete(boolean success) {
             Notifier.getInstance().notifyMangaUpdateComplete(mangaId, success);
 
             if (success) {
                 // check already downloaded chapter before notifying
                 Utils.updateChapterStatus(Download.this, mangaId);
                 Notifier.getInstance().notifyChapterListUpdate(mangaId);
             }
 
             --operationCount;
 
             stopIfIdle();
         }
     }
 
     private void updateManga(long mangaId) {
         Scraper scraper = Scraper.getInstance(this);
 
         ++operationCount;
 
         scraper.updateManga(mangaId, new MangaUpdateProgress(mangaId));
     }
 
     private class DownloadProgress
             implements Scraper.OnChapterDownloadProgress {
         private Notification notification;
         private NotificationManager manager;
         private RemoteViews contentView;
         private ContentValues manga, chapter;
 
         public DownloadProgress(ContentValues _manga, ContentValues _chapter) {
             manga = _manga;
             chapter = _chapter;
         }
 
         @Override
         public void downloadStarted() {
             long chapterId = chapter.getAsLong(DB.ID);
             String ticker =
                 getResources().getString(R.string.manga_downloading_ticker);
             Intent stopDownload = chapterStopDownloadIntent(
                 Download.this, chapterId);
 
             manager = (NotificationManager)
                 getSystemService(Context.NOTIFICATION_SERVICE);
             notification = new Notification(R.drawable.stat_download_anim,
                                             ticker,
                                             System.currentTimeMillis());
             notification.flags |= Notification.FLAG_ONGOING_EVENT;
             notification.contentView = contentView =
                 new RemoteViews(getPackageName(), R.layout.download_progress);
             notification.contentIntent = PendingIntent.getService(
                 Download.this, (int) chapterId, stopDownload,
                 PendingIntent.FLAG_CANCEL_CURRENT);
 
             contentView.setTextViewText(
                 R.id.download_description,
                 formatMsg(R.string.manga_downloading_progress));
             contentView.setProgressBar(R.id.download_progress, 0, 0, true);
 
             manager.notify(chapterNotificationId(chapterId), notification);
         }
 
         @Override
         public void downloadProgress(int current, int total) {
             long chapterId = chapter.getAsLong(DB.ID);
 
             notification.iconLevel = current % 6;
             contentView.setProgressBar(
                 R.id.download_progress, total, current, false);
 
             manager.notify(chapterNotificationId(chapterId), notification);
         }
 
         @Override
         public void downloadComplete(boolean success) {
             chapterDownloads.remove(chapter.getAsLong(DB.ID));
 
             notification.iconLevel = 0;
             notification.flags &= ~Notification.FLAG_ONGOING_EVENT;
 
             int tickerId, progressId;
             long chapterId = chapter.getAsLong(DB.ID);
 
             if (success) {
                 Intent viewChapter = Utils.viewChapterIntent(
                     Download.this, chapterId);
 
                 // display chapter when success notification clicked
                 notification.contentIntent = PendingIntent.getActivity(
                     Download.this, (int) chapterId, viewChapter,
                     PendingIntent.FLAG_CANCEL_CURRENT);
                 tickerId = R.string.manga_downloaded_ticker;
                 progressId = R.string.manga_downloaded_progress;
             }
             else {
                 Intent startDownload = chapterDownloadIntent(
                     Download.this, chapterId);
 
                 // re-download when fail notification clicked
                 notification.contentIntent = PendingIntent.getService(
                     Download.this, (int) chapterId, startDownload,
                     PendingIntent.FLAG_CANCEL_CURRENT);
                 tickerId = R.string.manga_download_error_ticker;
                 progressId = R.string.manga_download_error_progress;
             }
 
             notification.tickerText = getResources().getString(tickerId);
             contentView.setTextViewText(
                 R.id.download_description,
                 formatMsg(progressId));
             contentView.setViewVisibility(
                 R.id.download_progress_parent, View.INVISIBLE);
 
             manager.notify(chapterNotificationId(chapterId), notification);
 
             stopIfIdle();
         }
 
         // implementation
 
         private String formatMsg(int id) {
             String pattern = getResources().getString(id);
             String result = new Formatter()
                 .format(pattern,
                         manga.getAsString(DB.MANGA_TITLE),
                         chapter.getAsInteger(DB.CHAPTER_NUMBER))
                 .toString();
 
             return result;
         }
     }
 
     private void stopDownloadChapter(long chapterId) {
         if (!chapterDownloads.containsKey(chapterId))
             return;
 
         chapterDownloads.get(chapterId).cancel();
     }
 
     private void downloadChapter(long chapterId) {
         if (chapterDownloads.containsKey(chapterId))
             return;
 
         ContentValues chapter = db.getChapter(chapterId);
         ContentValues manga = db.getManga(chapter.getAsLong(
                                               DB.CHAPTER_MANGA_ID));
         Scraper scraper = Scraper.getInstance(this);
         File externalStorage = Environment.getExternalStorageDirectory();
         String targetPath = new Formatter()
             .format(manga.getAsString(DB.MANGA_PATTERN),
                     chapter.getAsInteger(DB.CHAPTER_NUMBER))
             .toString();
         File fullPath = new File(externalStorage, targetPath);
         DownloadProgress progress = new DownloadProgress(manga, chapter);
 
         PendingTask task = scraper.downloadChapter(
             chapterId, fullPath.getAbsolutePath(),
             downloadTemp.getAbsolutePath(), progress);
 
         chapterDownloads.put(chapterId, task);
     }
 
     private void resumeDownloads() {
         Cursor chapters = db.getAllChapterList();
         int statusI = chapters.getColumnIndex(DB.DOWNLOAD_STATUS);
         int idI = chapters.getColumnIndex(DB.ID);
 
         while (chapters.moveToNext()) {
             int status = chapters.getInt(statusI);
             long chapterId = chapters.getLong(idI);
 
             if (status != DB.DOWNLOAD_REQUESTED &&
                     status != DB.DOWNLOAD_STARTED)
                 continue;
 
             downloadChapter(chapterId);
         }
 
         chapters.close();
     }
 
     private boolean isOperationInProgress() {
         return (operationCount + chapterDownloads.size()) > 0;
     }
 
     private void stopIfIdle() {
         if (!isOperationInProgress())
             stopSelf();
     }
 
     private static int chapterNotificationId(long chapterId) {
         return (int) chapterId;
     }
 }
