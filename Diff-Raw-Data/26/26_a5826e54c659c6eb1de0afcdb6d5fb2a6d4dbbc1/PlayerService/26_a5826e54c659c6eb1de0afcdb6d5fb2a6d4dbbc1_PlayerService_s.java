 package org.eatabrick.radio;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.BitmapFactory;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.IBinder;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 import org.bff.javampd.*;
 import org.bff.javampd.events.*;
 import org.bff.javampd.exception.*;
 import org.bff.javampd.monitor.MPDStandAloneMonitor;
 import org.bff.javampd.objects.*;
 
 public class PlayerService extends Service implements TrackPositionChangeListener, PlayerBasicChangeListener, PlaylistBasicChangeListener {
   private static final String TAG = "PlayerService";
 
   public static final String ACTION_PLAY = "org.eatabrick.radio.PlayerService.ACTION_PLAY";
   public static final String ACTION_SKIP = "org.eatabrick.radio.PlayerService.ACTION_SKIP";
   public static final String ACTION_STOP = "org.eatabrick.radio.PlayerService.ACTION_STOP";
 
   private final IBinder binder = new PlayerBinder();
   private final Uri streamUri = Uri.parse("http://radio.eatabrick.org:8000/radio.mp3");
 
   private MediaPlayer mPlayer;
   private MPD mServer;
   private MPDStandAloneMonitor mMonitor;
   private List<PlayerListener> mListeners;
 
   private List<MPDSong> mSongList;
   private int           mSongPos;
 
   private boolean mPlaying = false;
 
   private int    mSongId  = 0;
   private String mTitle   = "";
   private String mArtist  = "";
   private String mAlbum   = "";
   private int    mElapsed = 0;
   private int    mLength  = 0;
 
   public static final int SERVICE_ID = 1;
 
   public class PlayerBinder extends Binder {
     PlayerService getService() {
       return PlayerService.this;
     }
   }
 
   public interface PlayerListener {
     public void onSongChange(String title, String artist, String album, int elapsed, int length);
     public void onPositionChange(int elapsed);
     public void onStatusChange(boolean mPlaying);
     public void onQueueChange(List <MPDSong> songList, int pos);
   }
 
   private Handler handler = new Handler();
 
   @Override public void onCreate() {
     Log.d(TAG, "Create");
 
     mListeners = new ArrayList<PlayerListener>();
 
     new Thread(new Runnable() {
       public void run() {
         try {
           mServer = new MPD("radio.eatabrick.org");
           Log.d(TAG, "Connected to MPD version " + mServer.getVersion());
 
           MPDStandAloneMonitor mMonitor = new MPDStandAloneMonitor(mServer, 250);
           mMonitor.addTrackPositionChangeListener(PlayerService.this);
           mMonitor.addPlayerChangeListener(PlayerService.this);
           mMonitor.addPlaylistChangeListener(PlayerService.this);
 
           mMonitor.start();
 
           updateSongInformation();
 
         } catch (UnknownHostException e) {
           Log.d(TAG, "Could not connect - unknown host");
         } catch (MPDConnectionException e) {
           Log.d(TAG, "Could not connect - MPD connection error: " + e.getMessage());
         }
       }
     }).start();
   }
 
   @Override public void onDestroy() {
     Log.d(TAG, "Destroy");
 
     new Thread(new Runnable() {
       public void run() {
         try {
           mMonitor.stop();
           mServer.close();
         } catch (MPDConnectionException e) {
         } catch (MPDResponseException e) {
         } catch (NullPointerException e) {
         }
       }
     }).start();
 
     if (mPlaying) stopMusic();
   }
 
   @Override public int onStartCommand(Intent intent, int flags, int startId) {
     String action = intent.getAction();
 
     if (action == null) {
       Log.d(TAG, "Got intent with null action");
     } else if (action.equals(ACTION_PLAY)) {
       startMusic();
     } else if (action.equals(ACTION_SKIP)) {
 
       new Thread(new Runnable() {
         public void run() {
           try {
             mServer.getMPDPlayer().playNext();
           } catch (MPDConnectionException e) {
             Log.d(TAG, "Connection problem: " + e.getMessage());
           } catch (MPDPlayerException e) {
             Log.d(TAG, "Player problem: " + e.getMessage());
           }
         }
       }).start();
 
       if (!mPlaying) stopSelf();
     } else if (action.equals(ACTION_STOP)) {
       stopMusic();
 
     }
 
     return START_STICKY;
   }
 
   @Override public IBinder onBind(Intent intent) {
     return binder;
   }
 
   public void trackPositionChanged(TrackPositionChangeEvent event) {
     mElapsed = (int) event.getElapsedTime();
 
     sendPositionChange();
   }
 
   public void playerBasicChange(PlayerBasicChangeEvent event) {
     // Nothing useful here really
   }
 
   public void playlistBasicChange(PlaylistBasicChangeEvent event) {
     updateSongInformation();
   }
 
   public boolean isPlaying() {
     return mPlaying;
   }
 
   public String getTitle() {
     return mTitle;
   }
 
   public String getArtist() {
     return mArtist;
   }
 
   public String getAlbum() {
     return mAlbum;
   }
 
   public int getElapsed() {
     return mElapsed;
   }
 
   public int getLength() {
     return mLength;
   }
 
   public synchronized void addPlayerListener(PlayerListener listener) {
     mListeners.add(listener);
   }
 
   public synchronized void removePlayerListener(PlayerListener listener) {
     mListeners.remove(listener);
   }
 
   private void updateSongInformation() {
     MPDPlayer player = mServer.getMPDPlayer();
     MPDPlaylist playlist = mServer.getMPDPlaylist();
 
     try {
       MPDSong song = player.getCurrentSong();
 
       int id = song.getId();
       if (id != mSongId) {
         mSongId = id;
 
         mTitle = song.getTitle();
        mArtist = song.getArtist().toString();
        mAlbum = song.getAlbum().toString();
 
         mElapsed = (int) player.getElapsedTime();
         mLength = song.getLength();
 
         if (mPlaying) showNotification();
 
         Log.d(TAG, "Song changed: " + mTitle);
 
         sendSongChange();
       }
 
       mSongList = playlist.getSongList();
       mSongPos = playlist.getCurrentSong().getPosition();
       sendQueueChange();
     } catch (MPDConnectionException e) {
       Log.d(TAG, "Error connecting to MPD: " + e.getMessage());
     } catch (MPDPlayerException e) {
       Log.d(TAG, "MPD Player error: " + e.getMessage());
     } catch (MPDPlaylistException e) {
       Log.d(TAG, "MPD Playlist error: " + e.getMessage());
    } catch (NullPointerException e) {
      e.printStackTrace();

      // Keep calm and carry on
     }
   }
 
   private void showNotification() {
     PendingIntent showIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
     PendingIntent stopIntent = PendingIntent.getBroadcast(this, 0, new Intent(PlayerReceiver.ACTION_STOP, null, this, PlayerReceiver.class), 0);
     PendingIntent skipIntent = PendingIntent.getBroadcast(this, 0, new Intent(PlayerReceiver.ACTION_SKIP, null, this, PlayerReceiver.class), 0);
 
     NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
 
     builder.setSmallIcon(R.drawable.ic_notification);
     builder.setContentIntent(showIntent);
     builder.setWhen(0);
     builder.setOngoing(true);
 
     builder.addAction(R.drawable.ic_menu_pause, getString(R.string.menu_stop), stopIntent);
     builder.addAction(R.drawable.ic_menu_skip, getString(R.string.menu_skip), skipIntent);
 
     builder.setTicker(mArtist + " - " + mTitle);
     builder.setContentTitle(mTitle);
     builder.setContentText(mArtist);
     builder.setSubText(mAlbum);
 
     builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
 
     startForeground(SERVICE_ID, builder.build());
   }
 
   private synchronized void sendPositionChange() {
     for (PlayerListener listener : mListeners) {
       listener.onPositionChange(mElapsed);
     }
   }
 
   private synchronized void sendSongChange() {
     for (PlayerListener listener : mListeners) {
       listener.onSongChange(mTitle, mArtist, mAlbum, mElapsed, mLength);
     }
   }
 
   private synchronized void sendStatusChange() {
     for (PlayerListener listener : mListeners) {
       listener.onStatusChange(mPlaying);
     }
   }
 
   private synchronized void sendQueueChange() {
     for (PlayerListener listener : mListeners) {
       listener.onQueueChange(mSongList, mSongPos);
     }
   }
 
   private void startMusic() {
     mPlayer = new MediaPlayer();
 
     mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
       public boolean onError(MediaPlayer player, int what, int extra) {
         Log.d(TAG, "Media player error: " + what + " : " + extra);
 
         // TODO show errors
 
         return false;
       }
     });
 
     mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
       public void onPrepared(MediaPlayer player) {
         player.start();
       }
     });
 
     try {
       mPlayer.setDataSource(this, streamUri);
       mPlayer.prepareAsync();
 
       mPlaying = true;
       sendStatusChange();
 
       showNotification();
     } catch (IOException e) {
       Log.d(TAG, "I/O exception: " + e.getMessage());
     } catch (IllegalStateException e) {
       Log.d(TAG, "Illegal state: " + e.getMessage());
     }
   }
 
   private void stopMusic() {
     if (mPlayer != null) {
       mPlayer.stop();
       mPlayer.release();
     }
 
     mPlaying = false;
     sendStatusChange();
 
     stopForeground(true);
     stopSelf();
   }
 }
