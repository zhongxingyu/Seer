 package net.nowatch.ui;
 
 // http://developer.android.com/reference/android/media/MediaPlayer.html
 // Based on packages/apps/Music/src/com/android/music/MediaPlaybackActivity.java
 
 import net.nowatch.Main;
 import net.nowatch.R;
 import net.nowatch.service.IMusicService;
 import net.nowatch.service.IMusicServiceCallback;
 import net.nowatch.service.MusicService;
 import net.nowatch.utils.Db;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 public class Player extends Activity {
 
     private static final String TAG = Main.TAG + "MusicPlayer";
     public static final String EXTRA_POSITION = "extra_position";
     public static final String EXTRA_ITEM_ID = "extra_item_id";
     private static final String REQ = "SELECT feeds.title, items.title, feeds.image, "
             + " items.image, items.duration FROM items INNER JOIN feeds ON items.feed_id=feeds._id WHERE items._id=";
     private static final int REFRESH = 1;
     private IMusicService mService;
     // private String mPath;
     private long mDuration;
     private long mPosition;
     private int mItemId = -1;
     // private long mBufferPosition;
     private Intent mIntent;
     private Bundle mExtras;
     private boolean isPaused = false;
     private boolean isAudio = true;
     private ProgressDialog mDialog;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setFileByIntent();
         if (isAudio) {
             startService(new Intent(Player.this, MusicService.class));
         }
     }
 
     @Override
     public void onResume() {
         super.onResume();
         if (isAudio) {
             bindService(new Intent(Player.this, MusicService.class), mConnection, 0);
         } else {
             playVideo();
         }
     }
 
     @Override
     public void onPause() {
         super.onPause();
         mHandler.removeMessages(REFRESH);
         if (mService != null) {
             unbindService(mConnection);
             mService = null;
         }
         // TODO: Pause video
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         // FIXME: Start new file
         setIntent(intent);
         setFileByIntent();
         if (isAudio) {
             setAudioUI();
             try {
                 playAudio();
             } catch (RemoteException e) {
                 mDialog.dismiss();
                 e.printStackTrace();
                 finish();
             }
         }
     }
 
     private void playVideo() {
         // Use foreground video player
         setContentView(R.layout.activity_player_video);
     }
 
     private void playAudio() throws RemoteException {
         if (mService != null) {
             if (!mService.isPlaying() && !isPaused) {
                 Log.v(TAG, "is not playing");
                 // Start playback
                 if (mItemId != -1) {
                     // Open and Play the file
                     if (mItemId != mService.getItemId()) {
                         mService.openFileId(mItemId, true);
                     } else {
                         mService.play();
                     }
                 } else {
                     // ContentResolver resolver = getContentResolver();
                     // TODO: get data from resolver if we want generic
                     // player
                     // if ("file".equals(mIntent.getScheme())) {
                     // mPath = mIntent.getData().getPath();
                     // } else {
                     // mPath = mIntent.getDataString();
                     // }
                 }
             } else {
                 mDialog.dismiss();
                 queueNextRefresh(refresh());
             }
         }
     }
 
     private void setFileByIntent() {
         mIntent = getIntent();
         mExtras = mIntent.getExtras();
 
         // Saved position
         if (!mIntent.hasExtra(EXTRA_POSITION)) {
             mPosition = 0;
         } else {
             mPosition = mExtras.getLong(EXTRA_POSITION);
         }
 
         // Item Id
         if (mIntent.hasExtra(EXTRA_ITEM_ID)) {
             mItemId = mExtras.getInt(EXTRA_ITEM_ID);
         }
 
         // File type
         String type = mIntent.getType();
         if (type.toLowerCase().startsWith("video/")) {
             isAudio = false;
         }
     }
 
     private void setAudioUI() {
         // Get podcast, episode and image
         String podcast = "Inconnu";
         String episode = "Inconnu (TODO)";
 
         if (mItemId != -1) {
             SQLiteDatabase db = (new Db(getApplicationContext())).openDb();
             Cursor c = db.rawQuery(REQ + mItemId, null);
             c.moveToFirst();
             podcast = c.getString(0);
             episode = c.getString(1);
             mDuration = c.getLong(4);
             // TODO: Get image
             c.close();
             db.close();
         }
 
         // Populate UI
         setContentView(R.layout.activity_player_audio);
         ((TextView) findViewById(R.id.player_time_total)).setText(getTime(mDuration));
         ((TextView) findViewById(R.id.player_show)).setText(podcast);
         ((TextView) findViewById(R.id.player_episode)).setText(episode);
         findViewById(R.id.btn_pause).setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 if (mService != null) {
                     isPaused = true;
                     try {
                         mService.pause();
                     } catch (RemoteException e) {
                         e.printStackTrace();
                     } finally {
                         unbindService(mConnection);
                         mService = null;
                     }
                 }
             }
         });
         findViewById(R.id.btn_play).setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 if (mService != null) {
                     isPaused = false;
                     try {
                         mService.play();
                     } catch (RemoteException e) {
                         e.printStackTrace();
                     }
                 } else {
                     Intent i = new Intent(Player.this, MusicService.class);
                     startService(i);
                     bindService(i, mConnection, 0);
                 }
             }
         });
         ((SeekBar) findViewById(R.id.seek)).setEnabled(false);
         ((SeekBar) findViewById(R.id.seek)).setMax(1000);
         ((SeekBar) findViewById(R.id.seek))
                 .setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                     public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                         if (fromUser) {
                             if (mService != null) {
                                 try {
                                     mHandler.removeMessages(REFRESH);
                                     mPosition = mDuration * progress / 1000;
                                     seekBar.setProgress((int) (1000 * mPosition / mDuration));
                                     mService.seek((int) mPosition);
                                     queueNextRefresh(refresh());
                                     // FIXME: Show to user that is it loading if
                                     // mPosition > mBufferPosition
                                 } catch (RemoteException e) {
                                     e.printStackTrace();
                                 }
                             }
                         }
                     }
 
                     public void onStartTrackingTouch(SeekBar seekBar) {
                     }
 
                     public void onStopTrackingTouch(SeekBar seekBar) {
                     }
                 });
 
         // Set loading dialog
         mDialog = ProgressDialog.show(Player.this, "", getString(R.string.dialog_loading));
     }
 
     private long refresh() {
         if (mService != null) {
             try {
                 if (!isPaused) {
                     mPosition = mService.getPosition();
                     long remaining = 1000 - (mPosition % 1000);
                     ((TextView) findViewById(R.id.player_time_current)).setText(getTime(mPosition));
                     ((SeekBar) findViewById(R.id.seek))
                             .setProgress((int) (1000 * mPosition / mDuration));
                     ((SeekBar) findViewById(R.id.seek)).setSecondaryProgress((int) (mService
                             .getBufferPercent() * 10));
                     return remaining;
                 }
             } catch (RemoteException e) {
                 e.printStackTrace();
             } catch (IllegalStateException e) {
                 e.printStackTrace();
             }
         }
         return 500;
     }
 
     private String getTime(long t) {
         return String.format("%02d:%02d:%02d", t / 3600, t % 3600 / 60, t % 3600 % 60);
     }
 
     private ServiceConnection mConnection = new ServiceConnection() {
 
         public void onServiceConnected(ComponentName className, IBinder service) {
             Log.v(TAG, "onServiceConnected()");
             mService = IMusicService.Stub.asInterface(service);
             try {
                 setAudioUI();
                 playAudio();
                 mService.registerCallback(mCallback);
             } catch (RemoteException e) {
                 e.printStackTrace();
             }
         }
 
         public void onServiceDisconnected(ComponentName className) {
             Log.v(TAG, "onServiceDisconnected");
             try {
                 mService.unregisterCallback(mCallback);
             } catch (RemoteException e) {
                 e.printStackTrace();
             } finally {
                 mService = null;
             }
         }
 
     };
 
     private IMusicServiceCallback mCallback = new IMusicServiceCallback.Stub() {
         @Override
         public void finishPrepared() throws RemoteException {
             runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     mDialog.dismiss();
                     queueNextRefresh(refresh());
                 }
             });
         }
     };
 
     private final Handler mHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
                 case REFRESH:
                     long delay = refresh();
                     queueNextRefresh(delay);
                     break;
             }
         }
     };
 
     private void queueNextRefresh(long delay) {
         Message msg = mHandler.obtainMessage(REFRESH);
         mHandler.removeMessages(REFRESH);
         mHandler.sendMessageDelayed(msg, refresh());
     }
 }
