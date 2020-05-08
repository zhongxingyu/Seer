 package org.apps.notsorandom;
 
 import android.app.Activity;
 import android.content.Context;
 import android.media.MediaPlayer;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.MediaController;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import java.io.IOException;
 
 /**
  * Implements the main player controls, as well as the MusicMapWidget.
  *
  * The parent activity must implement the MusicPlayer.OnPlayerListener interface.
  */
 public class MusicPlayer extends Fragment implements MediaController.MediaPlayerControl,
                                                         MediaPlayer.OnCompletionListener,
                                                         MediaPlayer.OnPreparedListener  {
     private static final String TAG = "MusicPlayer";
 
     // Place in layout to attach (floating) media controller
     private static MusicMapWidget musicMapWidget_ = null;
     private static MediaController controller_ = null;
 
     private static MediaPlayer player_ = null;
     private static SongInfo currSong_ = null;
 
     // Used to call back the Activity that attached us.
     private OnPlayerListener callback_ = null;
 
     private MusicMapView musicMapView_ = null;
     private TextView title_ = null;
     private TextView trackCounter_ = null;
     private TextView artist_ = null;
 
     private View anchorView_ = null;
 
     private Handler handler_ = new Handler();
 
     private boolean isFirstTime_;
 
 
     /**
      * Interface that an Activity must implement in order to attach this Fragment.
      * This Fragment will call into the interface's methods.
      */
     public interface OnPlayerListener {
         /**
          * Gives the current numeric position in the queue as well as the queue length
          * @param outta array of 2 int's: queue position / total in queue
          * @return True if the current position is known, otherwise false.
          */
         public boolean getCurrQueuePos(int[] outta);
 
         public SongInfo[] getQueue();
 
         /**
          * Called to retrieve the current song to play.
          * @return Info of next song to play. The implementation should
          *     return null when no more songs are in the queue.
          */
         public SongInfo getCurrSong();
 
         public NSRMediaLibrary getLibrary();
 
         public MusicPlayerApp.LibraryCategory getLibCategory();
 
         /**
          * Called to retrieve the next song to play.
          * TODO: rewrite this to Iterator interface.
          * @param first If true will return the first in list
          * @return Info of next song to play. The implementation should
          *     return null when no more songs are in the queue.
          */
         public SongInfo getNextSong(boolean first);
 
         /**
          * Called to retrieve the previous song to play.
          * TODO: rewrite this to Iterator interface.
          * @param first If true will return the last song in list
          * @return Info of next song to play. The implementation should
          *     return null when no more songs are in the queue.
          */
         public SongInfo getPrevSong(boolean first);
 
         /**
          * Retrieve the song info at index
          * @param ii song index
          * @return song info
          */
         //public SongInfo getSongInfo(int ii);
 
         /**
          * Called when a new song starts playing
          * @param song The info of the song.
          */
         public void onNewSong(SongInfo song);
 
         /**
          * Call to get new items into the queue
          * @param count Number of items to place into queue
          * @return Number of items actually placed into queue.
          */
         public int refreshQueue(int count);
 
         /**
          * Immediately start playing song.  Used from the library when a song
          * is selected.
          */
         public boolean playSong(SongInfo song);
 
         public void setCurrSong(SongInfo song);
     }
 
     /**
      * A slightly subclassed version of MediaController that will stay on the UI.
      * MediaController seems to autohide regardless of any settings.
      */
     private class MyMediaController extends MediaController {
         public MyMediaController(Context context, AttributeSet attrs) {
             super(context, attrs);
         }
 
         public MyMediaController(Context context, boolean useFastForward) {
             super(context, useFastForward);
         }
 
         public MyMediaController(Context context) {
             super(context, true);
         }
 
         @Override
         public boolean dispatchKeyEvent(KeyEvent event) {
             int keyCode = event.getKeyCode();
             if (event.getAction() == KeyEvent.ACTION_UP &&
                     (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU)) {
                 MusicPlayerApp.log(TAG, "dispatch key event from MyMediaController.");
                 if (isPlaying()) {
                     getActivity().moveTaskToBack(true);
                 } else {
                     hide_();
                     getActivity().finish();
                 }
                 return false;
             }
 
             return super.dispatchKeyEvent(event);
         }
 
         public void hide() {
             // Nope
         }
 
         public void hide_() {
             super.hide();   // OK, I'll hide
         }
     }
 
 
     public static void shutdown() {
         if (player_ != null) {
             try {
                 player_.reset();
                 player_.release();
             } catch (Exception ex) {
                 //
             }
         }
         if (controller_ != null) {
             controller_.setEnabled(false);
             controller_ = null;
         }
         player_ = null;
     }
 
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         Log.d(TAG, "onAttach in MusicPlayer");    // On attach is called before onCreateView
 
         try {
             callback_ = (OnPlayerListener) activity;
         } catch (ClassCastException ex) {
             throw new ClassCastException(activity.toString()
                                 + " must implement OnPlayerListener interface");
         }
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         MusicPlayerApp.log(TAG, "+onCreateView in MusicPlayer");
         View view = inflater.inflate(R.layout.fragment_music_player, container, false);
 
         musicMapWidget_ = (MusicMapWidget) view.findViewById(R.id.controlView);
         title_ = (TextView) view.findViewById(R.id.current_song);
         trackCounter_ = (TextView) view.findViewById(R.id.trackCounter);
         artist_ = (TextView) view.findViewById(R.id.artist);
 
         musicMapWidget_.setListener(callback_);
         musicMapView_ = musicMapWidget_.getMusicMapView();
         musicMapView_.setLibrary(callback_.getLibrary());
 
         anchorView_ = view.findViewById(R.id.anchorView);
         /*
         if (Build.VERSION.SDK_INT > 17) {
             anchorView_ = title_; // gets out of way for s4
         } else {
             anchorView_ = musicMapWidget_;
         }
         */
 
         if (controller_ == null) {
             isFirstTime_ = true;
             controller_  = new MyMediaController(getActivity());
             View.OnClickListener prev = new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     // TODO if current seek position > 2 secs, go to beginning of current song
                     SongInfo song = callback_.getPrevSong(false);
                     playSong(song);
                 }
             };
             View.OnClickListener next = new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     SongInfo song = callback_.getNextSong(false);
                     playSong(song);
                 }
             };
             controller_.setPrevNextListeners(next, prev);
         } else {
             isFirstTime_ = false;
             MusicPlayerApp.log(TAG, " Set AnchorView with control " + anchorView_);
             controller_.setAnchorView(anchorView_);
         }
         controller_.setMediaPlayer(this);
 
         if (player_ == null) {
             player_ = new MediaPlayer();
             player_.setOnPreparedListener(this);
             player_.setOnCompletionListener(this);
             MusicPlayerApp.log(TAG, "Created new MediaPlayer");
         }
 
         SongInfo song = null;
         if (isFirstTime_) {
             musicMapView_.initLibrary(callback_.getLibCategory());
             musicMapView_.getShuffledList(false);
 
             callback_.refreshQueue(MusicSettings.getQueueSizeLimit());
             song = callback_.getNextSong(true);
             MusicPlayerApp.log(TAG, " first time Song.");
         } else {
             song = callback_.getCurrSong();
             if (song == null)
                 song = callback_.getNextSong(true);
             MusicPlayerApp.log(TAG, " resuming on Curr/Next Song.");
             setTrackAndTitle(song);
         }
 
         if (queueSong(song)) {
 //            if (!isFirstTime_)
             //if (getView() != null) {
                 MusicPlayerApp.log(TAG, "Show controller. player is in view.");
                 controller_.show(0);
             //}
         }
         else
             MusicPlayerApp.log(TAG, "Unable to queue song " + song);
 
         return view;
     }
 
     @Override
     public void onDestroyView() {
         super.onDestroyView();
         Log.d(TAG, "onDestroyView in MusicPlayer");
         if (controller_ != null)
             ((MyMediaController) controller_).hide_();
         if (musicMapWidget_ != null)
             musicMapWidget_.cleanupViews();
 /*
         if (player_ != null) {
             try {
                 if (player_.isPlaying()) {    // Maybe want to keep playing in background?
                     player_.stop();
                     player_.release();
                 }
             } catch (Exception ex) {
                 //
             }
         }
 */
     }
 
     @Override
     public void onResume() {
         super.onResume();
         musicMapWidget_.getGlView().onResume();
     }
 
     @Override
     public void onPause() {
         super.onPause();
         musicMapWidget_.getGlView().onPause();
     }
 
     private void setTrackAndTitle(SongInfo song) {
         String track = "-/-";
         String title = "";
         String artist = "";
 
         if (song != null) {
             int[] qpos = new int[2];
             if (callback_ != null && callback_.getCurrQueuePos(qpos))
                 track = "" + qpos[0] + "/" + qpos[1];
 
             title = song.getTitle();
             artist = song.getArtist();
             if (artist.length() < 34)
                 artist += song.getAlbum();
             MusicPlayerApp.log(TAG, "setTrackAndTitle with " + track + " " + title);
         }
 
         if (trackCounter_ != null)
             trackCounter_.setText(track);
         if (title_ != null)
             title_.setText(title.length() > 94 ? title.substring(0,94) : title);
         if (artist_ != null)
             artist_.setText(artist);
     }
 
 
     public void initLibrary(MusicPlayerApp.LibraryCategory libCat) {
         if (musicMapView_ != null) {
             musicMapView_.initLibrary(libCat);
             musicMapView_.getShuffledList(true);
         }
     }
 
     public boolean playSong(SongInfo song) {
         if (player_ != null) {
             try {
                 if (player_.isPlaying()) {
                     Log.d(TAG, "stop player");
                     player_.stop();
                     Log.d(TAG, "reset player");
                     player_.reset();
                 }
             } catch (Exception ex) {
                 // at least I tried...
             }
         }
 
         if (!queueSong(song))
             return false;
 
         start();
 
         return true;
     }
 
     public boolean queueSong(SongInfo song) {
         if (song == null) return false;
 
         if (player_ == null) {
             MusicPlayerApp.log(TAG, "In queueSong, player_ is STILL null!");
             player_ = new MediaPlayer();
             player_.setOnPreparedListener(this);
             player_.setOnCompletionListener(this);
             controller_.setMediaPlayer(this);
         }
 
         if (currSong_ != null)
             currSong_.setLongForm(true);
 
         boolean ret = true;
         try {
             if (!isFirstTime_ && player_.isPlaying()) {
                 MusicPlayerApp.log(TAG, "queueSong, current song is still playing.");
             } else {
                 Log.d(TAG, "queueSong 1 " + song.getFileName());
                 player_.reset();
                 Log.d(TAG, "queueSong 2");
                 player_.setDataSource(song.getFileName());
                 player_.prepare();
                if (callback_ != null) callback_.onNewSong(song);

                 if (isVisible()) {   // only show the controller from the player fragment
                     controller_.setEnabled(true);
                     MusicPlayerApp.log(TAG, "controller enabled isVisible");
                 }
                 Log.d(TAG, "queueSong 3");
                 setTrackAndTitle(song);
                 currSong_ = song;
                 currSong_.setLongForm(true);
                 musicMapView_.redrawMap();     // make the map redraw
             }
         } catch (IllegalStateException ise) {
             Log.e(TAG, "Illegal state in queueSong: ", ise);
             ret = false;
         } catch (IllegalArgumentException iae) {
             Log.e(TAG, "Illegal arg in queueSong: ", iae);
             ret = false;
         } catch (IOException ioe) {
             Log.e(TAG, "I/O exception in queueSong: ", ioe);
             ret = false;
         }
 
         return ret;
     }
 
     public void stopSong() {
         pause();
         title_.setText("Stopped.");
     }
 /*
     public void showPlaylist() {
         int songs[] = musicMapView_.getShuffleList();
         for (int song : songs) {
 //            status_.append(musicMapView_.getFilename(song) + "\n");
         }
     }*/
 
     @Override
     public boolean canPause() {
         return true;
     }
 
     @Override
     public boolean canSeekBackward() {
         return true;
     }
 
     @Override
     public boolean canSeekForward() {
         return true;
     }
 
     @Override
     public int getAudioSessionId() {
         return 0;
     }
 
     @Override
     public int getBufferPercentage() {
         return 0;
     }
 
     @Override
     public int getCurrentPosition() {
         if (player_ != null)
             return player_.getCurrentPosition();
         return 0;
     }
 
     @Override
     public int getDuration() {
         return player_.getDuration();
     }
 
     @Override
     public boolean isPlaying() {
         return player_.isPlaying();
     }
 
     @Override
     public void onCompletion(MediaPlayer mediaPlayer) {
         Log.d(TAG, "onCompletion in MusicPlayer");
 
         SongInfo song = callback_.getNextSong(false);
         playSong(song);
     }
 
     @Override
     public void onPrepared(MediaPlayer mediaPlayer) {
         Log.d(TAG, "onPrepared in MusicPlayer");
         if (getView() == null) {
             MusicPlayerApp.log(TAG, "Player not in view, return.");
             return;
         }
         Log.d(TAG, "onPrepared in MusicPlayer - show player controls.");
         controller_.setMediaPlayer(this);
         controller_.setAnchorView(anchorView_);
 
         handler_.getLooper().getThread().setPriority(Thread.NORM_PRIORITY - 1);
         handler_.post(new Runnable() {
             public void run() {
                 controller_.setEnabled(true);
                 controller_.show(0);
             }
         });
     }
 
     @Override
     public void pause() {
         player_.pause();
     }
 
     @Override
     public void seekTo(int i) {
         player_.seekTo(i);
     }
 
     @Override
     public void start() {
         player_.start();
     }
 
 }
