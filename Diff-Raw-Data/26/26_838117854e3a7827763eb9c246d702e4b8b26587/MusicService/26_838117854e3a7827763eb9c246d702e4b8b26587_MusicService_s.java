 package net.nowatch.service;
 
 /**
  * Heavily based on the stock Android Music Player
  * packages/apps/Music
  */
 
 import java.io.IOException;
 
 import net.nowatch.Main;
 import net.nowatch.R;
 import net.nowatch.ui.Player;
 import net.nowatch.utils.Db;
 import net.nowatch.utils.Item;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.net.Uri;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.RemoteCallbackList;
 import android.os.RemoteException;
 import android.util.Log;
 
 public class MusicService extends Service {
 
     private static final String TAG = Main.TAG + "MusicService";
     private static final String REQ = "SELECT feeds.title, items.title, items.file_uri, items.file_type FROM items "
             + "INNER JOIN feeds ON items.feed_id=feeds._id WHERE items._id=";
     private final RemoteCallbackList<IMusicServiceCallback> mCallbacks = new RemoteCallbackList<IMusicServiceCallback>();
     private static final int SERVICE_ID = 1;
     private static final int CB_PREPARED = 1;
     private static final int MSG_STOP = 0;
     private static final int DELAY = 5000; // ms
     private boolean isPaused = false;
     private int mBufferPercent = 0;
     private int mItemId;
     private MediaPlayer mp = new MediaPlayer();
     private NotificationManager nm;
 
     @Override
     public IBinder onBind(Intent intent) {
         Log.v(TAG, "onBind()");
         return mBinder;
     }
 
     @Override
     public boolean onUnbind(Intent intent) {
         Log.v(TAG, "onUnBind()");
 
         // Nothing if is playing or isPaused
         if (mp.isPlaying()) {
             return true;
         }
 
         // Stop the service after some time
         Log.v(TAG, "audio is not running, so prepare to stop self");
         nm.cancelAll();
         Message msg = mHandler.obtainMessage(MSG_STOP);
         mHandler.removeMessages(MSG_STOP);
         mHandler.sendMessageDelayed(msg, DELAY);
 
         return true;
     }
 
     @Override
     public void onCreate() {
         Log.v(TAG, "onCreate()");
         super.onCreate();
         nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         Log.v(TAG, "onStartCommand()");
         handleCommand(intent);
         return START_NOT_STICKY;
     }
 
     @Override
     public void onStart(Intent intent, int startId) {
         Log.v(TAG, "onStart()");
         handleCommand(intent);
     }
 
     @Override
     public void onDestroy() {
         Log.v(TAG, "onDestroy()");
         mp.stop();
         mp.release();
         nm.cancelAll();
         mCallbacks.kill();
     }
 
     @Override
     public void onLowMemory() {
         mp.stop();
         mp.release();
         nm.cancelAll();
         mCallbacks.kill();
     }
 
     private void handleCommand(Intent intent) {
         Log.v(TAG, "handleCommand()");
     }
 
     private void clientCallback(int type) {
         final int N = mCallbacks.beginBroadcast();
         for (int i = 0; i < N; i++) {
             try {
                 switch (type) {
                     case CB_PREPARED:
                         mCallbacks.getBroadcastItem(i).finishPrepared();
                         break;
                 }
             } catch (RemoteException e) {
             }
         }
         mCallbacks.finishBroadcast();
     }
 
     /**
      * Service control (IPC) using AIDL interface
      */
     private final IMusicService.Stub mBinder = new IMusicService.Stub() {
 
         public void openFileId(int id, boolean autoplay) throws RemoteException {
             mItemId = id;
             // Get podcast info
             SQLiteDatabase db = (new Db(getApplicationContext())).openDb();
             Cursor c = db.rawQuery(REQ + id, null);
             c.moveToFirst();
             String msg = c.getString(0) + " - " + c.getString(1);
             String url = c.getString(2);
             String type = c.getString(3);
             c.close();
             db.close();
             openFile(msg, url, type, id, autoplay);
         }
 
         public void openFile(final String msg, final String path, final String type,
                 final int item_id, final boolean autoplay) throws RemoteException {
             try {
                 mp.setOnPreparedListener(new OnPreparedListener() {
                     public void onPrepared(MediaPlayer mp) {
                         clientCallback(CB_PREPARED);
                         // Notification
                         Intent i = new Intent(MusicService.this, Player.class).putExtra(
                                 Item.EXTRA_ITEM_ID, item_id).setDataAndType(Uri.parse(path), type)
                                 .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                         Notification nf = new Notification(R.drawable.stat_notify_musicplayer,
                                 "Lecture en cours ... (FIXME)", System.currentTimeMillis());
                        nf.flags = Notification.FLAG_ONGOING_EVENT
                                | Notification.FLAG_FOREGROUND_SERVICE;
                         nf.setLatestEventInfo(MusicService.this, "Lecture en cours", msg,
                                 PendingIntent.getActivity(MusicService.this, 0, i, 0));
                         nm.notify(SERVICE_ID, nf);
                         // Auto-play
                         if (autoplay) {
                             try {
                                 seek(0);
                                 play();
                             } catch (RemoteException e) {
                                 e.printStackTrace();
                             }
                         }
                     }
                 });
                 // On complete
                 mp.setOnCompletionListener(new OnCompletionListener() {
                     public void onCompletion(MediaPlayer mp) {
                         mp.stop();
                         stopSelf();
                     }
                 });
                 // Buffer info
                 mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                     public void onBufferingUpdate(MediaPlayer mp, int percent) {
                         mBufferPercent = percent;
                     }
                 });
 
                 // Preparation
                 mp.reset();
                 mp.setDataSource(path);
                 mp.prepareAsync();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
 
         public void seek(int position) {
             mp.seekTo(position * 1000);
             mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                 public void onSeekComplete(MediaPlayer mp) {
                     // TODO: Callback to player
                 }
             });
         }
 
         public void play() throws RemoteException {
             mp.start();
             isPaused = false;
         }
 
         public void pause() throws RemoteException {
             mp.pause();
             isPaused = true;
         }
 
         public long getPosition() throws RemoteException {
             return mp.getCurrentPosition() / 1000;
         }
 
         public int getBufferPercent() throws RemoteException {
             return mBufferPercent;
         }
 
         public boolean isPlaying() throws RemoteException {
             return mp.isPlaying() || isPaused;
         }
 
         public void registerCallback(IMusicServiceCallback cb) throws RemoteException {
             if (cb != null) {
                 mCallbacks.register(cb);
             }
         }
 
         public void unregisterCallback(IMusicServiceCallback cb) throws RemoteException {
             if (cb != null) {
                 mCallbacks.unregister(cb);
             }
         }
 
         @Override
         public int getItemId() throws RemoteException {
             return mItemId;
         }
 
     };
 
     private final Handler mHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
                 case MSG_STOP:
                     stopSelf();
                     break;
             }
         }
     };
 
 }
