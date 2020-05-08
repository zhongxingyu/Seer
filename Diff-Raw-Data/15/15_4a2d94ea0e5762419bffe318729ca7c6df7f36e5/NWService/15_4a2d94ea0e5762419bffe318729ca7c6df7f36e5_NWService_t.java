 package net.nowatch.service;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.ref.WeakReference;
 import java.net.MalformedURLException;
 import java.net.UnknownHostException;
 import java.util.Iterator;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import net.nowatch.IService;
 import net.nowatch.IServiceCallback;
 import net.nowatch.Main;
 import net.nowatch.R;
 import net.nowatch.network.GetFile;
 import net.nowatch.network.Network;
 import net.nowatch.ui.ItemInfo;
 import net.nowatch.ui.ListItems;
 import net.nowatch.ui.Manage;
 import net.nowatch.utils.Db;
 import net.nowatch.utils.Item;
 import net.nowatch.utils.Prefs;
 
 import org.apache.http.client.ClientProtocolException;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.IBinder;
 import android.os.RemoteCallbackList;
 import android.os.RemoteException;
 import android.os.StatFs;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.RemoteViews;
 import android.widget.Toast;
 
 public class NWService extends Service {
 
     private static final String TAG = Main.TAG + "NWService";
     private static final int NOTIFICATION_UPDATE = -1;
     private static final long PROGRESS_UPDATE = 3000000000L;
     private static final String REQ_NEW = "select items._id from items where status="
             + Item.STATUS_NEW;
    private final String REQ_ITEM = "select title,file_uri,file_size,status,type from items where _id=? limit 1";
     private final String REQ_CLEAN = "update items set status=" + Item.STATUS_UNREAD
             + " where status=" + Item.STATUS_DOWNLOADING;
     private final RemoteCallbackList<IServiceCallback> mCallbacks = new RemoteCallbackList<IServiceCallback>();
     private final ConcurrentLinkedQueue<Integer> downloadQueue = new ConcurrentLinkedQueue<Integer>();
     private final ConcurrentHashMap<Integer, DownloadTask> downloadTasks = new ConcurrentHashMap<Integer, DownloadTask>();
     private UpdateTaskNotif updateTask;
     private Context ctxt;
 
     public static final String ACTION_UPDATE = "action_update";
     public static final String ACTION_ADD = "action_add";
     public static final String ACTION_CANCEL = "action_cancel";
     public static final String ACTION_PAUSE = "action_pause";
     public static final int TYPE_CURRENT = 1;
     public static final int TYPE_PENDING = 2;
     public NotificationManager notificationManager;
 
     @Override
     public void onCreate() {
         Log.i(TAG, "onCreate()");
         ctxt = getApplicationContext();
         notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         IntentFilter filter = new IntentFilter();
         filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
         registerReceiver(receiver, filter);
     }
 
     @Override
     public void onDestroy() {
         Log.i(TAG, "onDestroy()");
         pauseAll();
         clean();
         mCallbacks.kill();
         unregisterReceiver(receiver);
     }
 
     @Override
     public void onLowMemory() {
         Log.i(TAG, "onLowMemory()");
         pauseAll();
         clean();
         mCallbacks.kill();
         unregisterReceiver(receiver);
     }
 
     @Override
     public void onStart(Intent intent, int startId) {
         // onStart() is deprecated, but used for backward compatibility!
         handleCommand(intent);
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         handleCommand(intent);
         return START_NOT_STICKY;
     }
 
     private void handleCommand(Intent intent) {
         // Clean stuff
         clean();
         // Handle intentions
         String action = null;
         if (intent != null && (action = intent.getAction()) != null) {
             Log.v(TAG, "action=" + action);
             Bundle extras = intent.getExtras();
             if (ACTION_ADD.equals(action)) {
                 // Add item to download queue
                 Integer id = new Integer(extras.getInt(Item.EXTRA_ITEM_ID));
                 if (!downloadQueue.contains(id)) {
                     downloadQueue.add(id);
                 }
                 stopOrContinue();
             } else if (ACTION_CANCEL.equals(action)) {
                 // Cancel download
                 cancelDownload(extras.getInt(Item.EXTRA_ITEM_TYPE), extras
                         .getInt(Item.EXTRA_ITEM_ID));
                 stopOrContinue();
             } else if (ACTION_PAUSE.equals(action)) {
                 pauseDownload(extras.getInt(Item.EXTRA_ITEM_ID));
                 stopOrContinue();
             } else if (ACTION_UPDATE.equals(action)) {
                 // Check for updates
                 updateTask = new UpdateTaskNotif(NWService.this);
                 updateTask.execute();
             } else {
                 // Nothing to do
                 stopOrContinue();
             }
         } else {
             // Nothing to do
             stopOrContinue();
         }
     }
 
     private BroadcastReceiver receiver = new BroadcastReceiver() {
         public void onReceive(Context ctxt, Intent intent) {
             ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
             final NetworkInfo ni = connMgr.getActiveNetworkInfo();
             if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED
                     && ni.getType() == ConnectivityManager.TYPE_MOBILE) {
                 if (!new Network(ctxt).isMobileAllowed()) {
                     pauseAll();
                 }
             }
         }
     };
 
     private void clean() {
         // Clean current tasks
         if (updateTask != null) {
             updateTask.cancel(true);
         }
         // Clean failed downloads
         if (downloadTasks.size() == 0) {
             // Reset state
             SQLiteDatabase db = (new Db(ctxt)).openDb();
             db.execSQL(REQ_CLEAN);
             db.close();
         }
     }
 
     private void pauseAll() {
         downloadQueue.clear();
         // Cancel current downloads
         if (downloadTasks != null && downloadTasks.size() > 0) {
             Iterator<DownloadTask> iterator = downloadTasks.values().iterator();
             while (iterator.hasNext()) {
                 DownloadTask task = iterator.next();
                 synchronized (task) {
                     if (task.cancel(true)) {
                         downloadTasks.remove(task.item_id);
                         notificationManager.cancel(task.item_id);
                         ItemInfo.changeStatus(ctxt, task.item_id, Item.STATUS_INCOMPLETE);
                     }
                 }
             }
         }
     }
 
     private void cancelDownload(int type, Integer id) {
         if (type == TYPE_PENDING) {
             synchronized (downloadQueue) {
                 if (downloadQueue.contains(id)) {
                     ItemInfo.changeStatus(ctxt, id, Item.STATUS_UNREAD);
                     downloadQueue.remove(id);
                 }
             }
         } else if (type == TYPE_CURRENT) {
             synchronized (downloadTasks) {
                 if (downloadTasks.containsKey(id)) {
                     DownloadTask task = downloadTasks.get(id);
                     synchronized (task.task) {
                         task.task.deleteOnFinish = true;
                         task.task.isCancelled = true;
                     }
                     if (task != null && AsyncTask.Status.RUNNING.equals(task.getStatus())
                             && task.cancel(true)) {
                         downloadTasks.remove(id);
                         ItemInfo.changeStatus(ctxt, id, Item.STATUS_UNREAD);
                     }
                 }
             }
         }
         // clientCallback();
     }
 
     private void pauseDownload(int id) {
         if (downloadTasks.containsKey(id)) {
             DownloadTask task = downloadTasks.get(id);
             synchronized (task.task) {
                 task.task.deleteOnFinish = false;
                 task.task.isCancelled = true;
             }
             if (AsyncTask.Status.RUNNING.equals(task.getStatus()) && task.cancel(true)) {
                 downloadTasks.remove(id);
                 ItemInfo.changeStatus(ctxt, id, Item.STATUS_INCOMPLETE);
                 // clientCallback();
             }
         }
     }
 
     private void clientCallback() {
         // Broadcast to all clients the new value.
         final int N = mCallbacks.beginBroadcast();
         for (int i = 0; i < N; i++) {
             try {
                 mCallbacks.getBroadcastItem(i)._valueChanged();
             } catch (RemoteException e) {
             }
         }
         mCallbacks.finishBroadcast();
     }
 
     private void stopOrContinue() {
         if (downloadTasks.size() < 1 && downloadQueue.peek() == null) {
             stopSelf();
         } else {
             startDownloadTask();
         }
     }
 
     private void startDownloadTask() {
         Network net = new Network(this);
         if (net.isConnected()) {
             if (net.isMobileAllowed()) {
                 SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
                 if (downloadTasks.size() < Integer.parseInt(prefs.getString(
                         Prefs.KEY_SIMULTANEOUS_DL, Prefs.DEFAULT_SIMULTANEOUS_DL))
                         && downloadQueue.size() > 0) {
                     Integer itemId = downloadQueue.poll();
                     if (itemId != null) {
                         // Get available space on sdcard
                         StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
                                 .getPath());
                         long bytesFree = (long) stat.getBlockSize()
                                 * (long) stat.getAvailableBlocks();
                         // Get item information and start DownloadTask
                         SQLiteDatabase db = (new Db(ctxt)).openDb();
                         Cursor c = db.rawQuery(REQ_ITEM, new String[] { "" + itemId });
                         c.moveToFirst();
                         if (bytesFree > c.getLong(2)) {
                             DownloadTask task = new DownloadTask(NWService.this, c.getString(0),
                                    itemId, c.getInt(3), c.getInt(4));
                             task.execute(c.getString(1), c.getString(2));
                             downloadTasks.put(itemId, task);
                             c.close();
                             db.close();
                             ItemInfo.changeStatus(ctxt, itemId, Item.STATUS_DOWNLOADING);
                             clientCallback();
                         } else {
                             Toast.makeText(ctxt, R.string.toast_sdcardfreespace, Toast.LENGTH_LONG)
                                     .show();
                             Log.v(TAG, "free space=" + bytesFree);
                             Log.v(TAG, "file size=" + c.getLong(2));
                             stopOrContinue();
                         }
                     }
                 }
             } else {
                 Toast.makeText(ctxt, R.string.toast_nomobiletraffic, Toast.LENGTH_LONG).show();
             }
         } else {
             Toast.makeText(ctxt, R.string.toast_notconnected, Toast.LENGTH_LONG).show();
         }
     }
 
     static class DownloadTask extends AsyncTask<String, Integer, Void> {
 
         private RemoteViews rv;
         private Notification nf;
         private int item_id;
         private int status;
        private int type;
         private String title;
         private WeakReference<NWService> mService;
         private String error_msg = null;
         private getPodcastFile task = null;
         private String dest = null;
 
        public DownloadTask(NWService activity, String _title, int _item_id, int _status, int _type) {
             super();
             mService = new WeakReference<NWService>(activity);
             title = _title;
             item_id = _item_id;
             status = _status;
            type = _type;
         }
 
         @Override
         protected void onPreExecute() {
             final NWService service = mService.get();
             nf = new Notification(android.R.drawable.stat_sys_download, service
                     .getString(R.string.notif_dl_started), System.currentTimeMillis());
             rv = new RemoteViews(service.getPackageName(), R.layout.notification_download);
             // FIXME: Use feed/item image from Db
             rv.setImageViewResource(R.id.download_icon, R.drawable.icon);
             rv.setTextViewText(R.id.download_title, title);
             rv.setProgressBar(R.id.download_progress, 0, 0, true);
             Intent i = new Intent(service, Manage.class);
             i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             nf.contentIntent = PendingIntent.getActivity(service, 0, i, 0);
             nf.contentView = rv;
             nf.flags |= Notification.FLAG_ONGOING_EVENT;
             nf.flags |= Notification.FLAG_NO_CLEAR;
             service.notificationManager.notify(item_id, nf);
         }
 
         @Override
         protected Void doInBackground(String... str) {
             int fs = 1;
             try {
                 fs = Integer.parseInt(str[1]);
             } catch (NumberFormatException e) {
             }
             // Get Context
             Context ctxt = null;
             if (mService != null) {
                 final NWService service = mService.get();
                 if (service != null) {
                     ctxt = service.getApplicationContext();
                 }
             }
             if (ctxt == null) {
                 cancel(false);
                 return null;
             }
             // Download file
             try {
                 String state = Environment.getExternalStorageState();
                 if (Environment.MEDIA_MOUNTED.equals(state) && !str[0].equals(new String(""))) {
                     File dst = new File(Environment.getExternalStorageDirectory()
                             .getCanonicalPath()
                             + "/" + GetFile.PATH_PODCASTS);
                     dst.mkdirs();
                     task = new getPodcastFile(ctxt, DownloadTask.this, fs);
                     dest = dst.getCanonicalPath() + "/" + new File(str[0]).getName();
                     if (status == Item.STATUS_INCOMPLETE) {
                         Log.v(TAG, "resume download");
                         ItemInfo.changeStatus(ctxt, item_id, Item.STATUS_DOWNLOADING);
                         task.getChannel(str[0], dest, true);
                     } else {
                         ItemInfo.changeStatus(ctxt, item_id, Item.STATUS_DOWNLOADING);
                         task.getChannel(str[0], dest, false);
                     }
                 } else {
                     cancel(false);
                 }
             } catch (MalformedURLException e) {
                 error_msg = e.getLocalizedMessage();
                 Log.e(TAG, e.getMessage());
             } catch (ClientProtocolException e) {
                 error_msg = e.getLocalizedMessage();
                 Log.e(TAG, e.getMessage());
             } catch (UnknownHostException e) {
                 error_msg = e.getLocalizedMessage();
                 Log.e(TAG, e.getMessage());
             } catch (IOException e) {
                 error_msg = e.getLocalizedMessage();
                 Log.e(TAG, e.getMessage());
             }
             return null;
         }
 
         @Override
         protected void onProgressUpdate(Integer... values) {
             if (mService != null) {
                 final NWService service = mService.get();
                 if (service != null) {
                     String status;
                     if (values[1] < 1024) {
                         status = values[0] + "% " + values[1] + "ko/s";
                     } else {
                         status = values[0] + "% " + (values[1] / 1024) + "mo/s";
                     }
                     rv.setProgressBar(R.id.download_progress, 100, values[0], false);
                     rv.setTextViewText(R.id.download_status, status);
                     service.notificationManager.notify(item_id, nf);
                 }
             }
         }
 
         @Override
         protected void onPostExecute(Void unused) {
             Log.v(TAG, "onPostExecute()");
             super.onPostExecute(unused);
             if (mService != null) {
                 final NWService service = mService.get();
                 if (service != null) {
                     if (error_msg != null) {
                         Toast.makeText(service.getApplicationContext(), error_msg,
                                 Toast.LENGTH_LONG).show();
                     }
                     ItemInfo.changeStatus(service, item_id, Item.STATUS_DL_UNREAD);
                     try {
                         service.notificationManager.cancel(item_id);
                     } catch (Exception e) {
                         Log.v(TAG, e.getMessage());
                     } finally {
                         nf = new Notification(android.R.drawable.stat_sys_download_done,
                                 service.getString(R.string.notif_dl_complete), System.currentTimeMillis());
                         nf.flags = Notification.FLAG_AUTO_CANCEL;
                         nf.setLatestEventInfo(service, title, service
                                 .getString(R.string.notif_dl_complete), PendingIntent.getActivity(
                                service, 0, new Intent(service, ListItems.class).putExtra(Main.EXTRA_TYPE, type), 0));
                         service.notificationManager.notify(item_id, nf);
                         service.stopOrContinue();
                     }
                 }
             }
         }
 
         @Override
         protected void onCancelled() {
             Log.v(TAG, "onCancelled()");
             if (mService != null) {
                 final NWService service = mService.get();
                 if (service != null) {
                     Toast.makeText(service.getApplicationContext(), R.string.toast_dl_canceled,
                             Toast.LENGTH_LONG).show();
                     if (error_msg != null) {
                         Toast.makeText(service.getApplicationContext(), error_msg,
                                 Toast.LENGTH_LONG).show();
                     }
                     try {
                         service.notificationManager.cancel(item_id);
                     } catch (Exception e) {
                         Log.v(TAG, e.getMessage());
                     } finally {
                         service.stopOrContinue();
                     }
                 }
             }
             super.onCancelled();
         }
 
         public void publish(Integer... values) {
             publishProgress(values);
         }
 
         static class getPodcastFile extends GetFile {
 
             private static final long PROGRESS_MAX = 1000000;
             private static final long PERCENT = 100;
             private DownloadTask task;
             private long current_bytes = 0;
             private long start;
             private long now;
             private int percent;
             private int speed;
 
             public getPodcastFile(final Context ctxt, final DownloadTask task,
                     final long file_remote_size) {
                 super(ctxt);
                 this.task = task;
                 if (file_remote_size > 0) {
                     this.file_remote_size = file_remote_size;
                 }
                 start = System.nanoTime();
             }
 
             public void getChannel(String src, String dst, boolean resume) throws IOException {
                 getChannel(src, dst, null, false, resume);
             }
 
             // public void getBlocking(String src, String dst, boolean resume)
             // throws IOException {
             // getBlocking(src, dst, null, false, resume);
             // }
 
             @Override
             protected void update(final long count) {
                 now = System.nanoTime();
                 // Speed of the last seconds
                 if ((now - start) > PROGRESS_UPDATE && file_remote_size > 0) {
                     percent = (int) (file_local_size * PERCENT / file_remote_size);
                     speed = (int) (current_bytes / Math.abs((now - start) / PROGRESS_MAX));
                     task.publish(percent, speed);
                     start = now;
                     current_bytes = count;
                 } else {
                     current_bytes += count;
                 }
                 file_local_size += count;
             }
         }
     }
 
     /**
      * Update
      */
     private static class UpdateTaskNotif extends UpdateTask {
 
         public UpdateTaskNotif(NWService s) {
             super(s);
         }
 
         @Override
         protected void onPostExecute(Void unused) {
             super.onPostExecute(unused);
             final NWService service = getService();
             if (service != null) {
                 final Context ctxt = service.getApplicationContext();
                 SQLiteDatabase db = (new Db(ctxt)).openDb();
                 Cursor c = db.rawQuery(REQ_NEW, null);
                 try {
                     c.moveToFirst();
                     int nb = c.getCount();
                     if (nb > 0) {
                         // Show notification about new items
                         Notification nf = new Notification(R.drawable.icon_scream_48,
                                 service.getString(R.string.notif_update_new_title), System.currentTimeMillis());
                         nf.flags = Notification.FLAG_AUTO_CANCEL;
                         nf.setLatestEventInfo(service, service.getString(R.string.notif_update_new_desc),
                             String.format(service.getString(R.string.notif_update_new_info), nb), PendingIntent.getActivity(service, 0,
                                 new Intent(service, ListItems.class), 0));
                         service.notificationManager.notify(NOTIFICATION_UPDATE, nf);
                         // Auto-download items
                         if (PreferenceManager.getDefaultSharedPreferences(ctxt).getBoolean(
                                 Prefs.KEY_AUTO_DL, Prefs.DEFAULT_AUTO_DL)) {
                             Log.v(TAG, "auto-download");
                             do {
                                 service.downloadQueue.add(c.getInt(0));
                                 service.stopOrContinue();
                             } while (c.moveToNext());
                         }
                         service.stopOrContinue();
                     }
                 } finally {
                     c.close();
                     db.close();
                 }
             }
         }
 
         @Override
         protected void onCancelled() {
             super.onCancelled();
             final NWService service = getService();
             if (service != null) {
                 service.stopOrContinue();
             }
         }
 
     }
 
     /**
      * Service control (IPC) using AIDL interface
      */
     private final IService.Stub mBinder = new IService.Stub() {
 
         public void _registerCallback(IServiceCallback cb) {
             if (cb != null) {
                 mCallbacks.register(cb);
             }
         }
 
         public void _unregisterCallback(IServiceCallback cb) {
             if (cb != null) {
                 mCallbacks.unregister(cb);
             }
         }
 
         public int[] _getCurrentDownloads() throws RemoteException {
             int[] current = new int[downloadTasks.size()];
             Iterator<Integer> iterator = downloadTasks.keySet().iterator();
             int i = 0;
             while (iterator.hasNext()) {
                 current[i] = iterator.next();
                 i++;
             }
             return current;
         }
 
         public int[] _getPendingDownloads() throws RemoteException {
             int[] pending = new int[downloadQueue.size()];
             Iterator<Integer> iterator = downloadQueue.iterator();
             int i = 0;
             while (iterator.hasNext()) {
                 pending[i] = iterator.next();
                 i++;
             }
             return pending;
         }
     };
 
     @Override
     public IBinder onBind(Intent intent) {
         Log.v(TAG, "onBind()");
         return mBinder;
     }
 
     @Override
     public boolean onUnbind(Intent intent) {
         Log.v(TAG, "onUnbind()");
         return true;
     }
 }
