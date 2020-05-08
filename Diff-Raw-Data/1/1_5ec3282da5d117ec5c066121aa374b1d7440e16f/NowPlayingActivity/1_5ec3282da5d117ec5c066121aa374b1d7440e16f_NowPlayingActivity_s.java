 package cc.rainwave.android;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.PixelFormat;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SlidingDrawer;
 import android.widget.TextView;
 import android.widget.Toast;
 import cc.rainwave.android.adapters.SongListAdapter;
 import cc.rainwave.android.adapters.StationListAdapter;
 import cc.rainwave.android.api.Session;
 import cc.rainwave.android.api.types.Album;
 import cc.rainwave.android.api.types.RainwaveException;
 import cc.rainwave.android.api.types.Song;
 import cc.rainwave.android.api.types.SongRating;
 import cc.rainwave.android.api.types.Station;
 import cc.rainwave.android.views.CountdownView;
 import cc.rainwave.android.views.HorizontalRatingBar;
 import cc.rainwave.android.views.PagerWidget;
 
 import com.android.music.TouchInterceptor;
 import com.google.android.apps.iosched.ui.widget.Workspace;
 import com.google.android.apps.iosched.ui.widget.Workspace.OnScreenChangeListener;
 
 /**
  * This is the primary activity for this application. It announces
  * which song is playing, handles ratings, and also elections.
  * @author pkilgo
  *
  */
 public class NowPlayingActivity extends Activity {
     /** Debug tag */
     private static final String TAG = "NowPlaying";
 
     /** This manages our connection with the Rainwave server */
     private Session mSession;
     private RainwavePreferences mPreferences;
 
     /** AsyncTask for song ratings */
     private ActionTask mRateTask, mReorderTask, mRemoveTask;
 
     /** Reference to song countdown timer. */
     private CountDownTimer mCountdown;
 
     /** True if device supports Window.FEATURE_INDETERMINATE_PROGRESS. */
     private boolean mHasIndeterminateProgress;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         mHasIndeterminateProgress = requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         super.onCreate(savedInstanceState);
         setup();
         initializeSession();
         setContentView(R.layout.activity_main);
         setListeners();
 
         this.registerReceiver(mEventUpdateReceiver,
                 new IntentFilter(SyncService.BROADCAST_EVENT_UPDATE)
         );
     }
 
     @Override
     public void onStart() {
         super.onStart();
     }
 
     /**
      * Our strategy here is to attempt to re-initialize the
      * app as much as possible. This helps us to catch preference
      * changes, and to not have lingering song data lying around.
      */
     @Override
     public void onResume() {
         super.onResume();
         initializeSession();
         fetchSchedules();
     }
 
     public void onPause() {
         super.onPause();
         stopTasks();
         if(mCountdown != null) {
             mCountdown.cancel();
         }
     }
 
     public void onStop() {
         super.onStop();
     }
 
     public void onDestroy() {
         super.onDestroy();
         stopSyncService();
         unregisterReceiver(mEventUpdateReceiver);
     }
 
     @Override
     public void onAttachedToWindow() {
         super.onAttachedToWindow();
         Window window = getWindow();
         window.setFormat(PixelFormat.RGBA_8888);
     }
 
     public boolean onKeyDown(int keyCode, KeyEvent ev) {
         switch(keyCode) {
         case KeyEvent.KEYCODE_BACK:
             SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.np_drawer);
             if(drawer.isOpened()) {
                 drawer.animateClose();
                 return true;
             }
             else if(drawer.isMoving()) {
                 drawer.close();
                 return true;
             }
         }
         return super.onKeyDown(keyCode, ev);
     }
 
     /**
      * Dialog manufacturer.
      */
     public Dialog onCreateDialog(int id) {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
         switch(id) {
 
         case DIALOG_STATION_PICKER:
             builder.setTitle(R.string.label_pickStation)
                    .setNegativeButton(R.string.label_cancel, null);
 
             if(!mSession.hasStations()) {
                 return builder.setMessage(R.string.msg_noStations).create();
             }
 
             Station stations[] = mSession.cloneStations();
 
             final ListView listView = new ListView(this);
             listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                 @Override
                 public void onItemClick(AdapterView<?> parent, View view,
                         int index, long id) {
                     Station s = (Station) listView.getItemAtPosition(index);
                     mSession.setStation(s.getId());
                     NowPlayingActivity.this.dismissDialog(DIALOG_STATION_PICKER);
                     refresh();
                 }
             });
 
             listView.setAdapter(new StationListAdapter(this, stations));
 
             return builder.setView(listView)
                 .create();
 
         default:
             // Assume the number must be a string resource id.
             return builder.setTitle(R.string.label_error)
                     .setMessage(id)
                     .setPositiveButton(R.string.label_ok, null)
                     .create();
         }
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inflater = getMenuInflater();
         TouchInterceptor list = (TouchInterceptor) findViewById(R.id.np_request_list);
         inflater.inflate(R.menu.queue_menu, menu);
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
         Song s = (Song) list.getItemAtPosition(info.position);
         menu.setHeaderTitle(s.getTitle());
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
         switch (item.getItemId()) {
         case R.id.remove:
             TouchInterceptor list = (TouchInterceptor) findViewById(R.id.np_request_list);
             SongListAdapter adapter = (SongListAdapter) list.getAdapter();
             Song s = adapter.removeSong(info.position);
             requestRemove(s);
             resyncRequests();
             return true;
         default:
             return super.onContextItemSelected(item);
         }
     }
 
     private void setup() {
         getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
     }
 
     /**
      * Sets up listeners for this activity.
      */
     private void setListeners() {
         // The rating dialog should show up if the Song rating view is clicked.        
         findViewById(R.id.np_songRating).setOnTouchListener(
         new OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent e) {
                 Workspace w = (Workspace) findViewById(R.id.np_workspace);
                 HorizontalRatingBar b = (HorizontalRatingBar) findViewById(R.id.np_songRating);
 
                 if(mSession == null || !mSession.isTunedIn() || !mSession.hasCredentials()) {
                     if(e.getAction() == MotionEvent.ACTION_DOWN) {
                         w.lockCurrentScreen();
                         b.setLabel(R.string.msg_tuneInFirst);
                     }
                     else if(e.getAction() == MotionEvent.ACTION_UP) {
                         w.unlockCurrentScreen();
                         b.setLabel(R.string.label_song);
                     }
                     return true;
                 }
 
 
                 HorizontalRatingBar hrb = (HorizontalRatingBar) v;
                 float rating = 0.0f;
                 float max = 5.0f;
                 switch(e.getAction()) {
                 case MotionEvent.ACTION_DOWN:
                     w.lockCurrentScreen();
                 case MotionEvent.ACTION_MOVE:
                 case MotionEvent.ACTION_UP:
                     rating = hrb.snapPositionToMinorIncrement(e.getX());
                     rating = Math.max(1.0f, Math.min(rating, 5.0f));
                     max = hrb.getMax();
                     hrb.setPrimaryValue(rating);
                     String label = String.format(Locale.US, "%.1f/%.1f",rating,max);
                     hrb.setLabel(label);
 
                     if(e.getAction() == MotionEvent.ACTION_UP) {
                         w.unlockCurrentScreen();
                         ActionTask t = new ActionTask();
                         Song s = mSession.getCurrentEvent().getCurrentSong();
                         t.execute(ActionTask.RATE, s.getId(), rating);
                         b.setLabel(R.string.label_song);
                     }
                 }
                 return true;
             }
         });
 
         findViewById(R.id.np_albumRating).setOnTouchListener(
         new OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent e) {
                 Workspace w = (Workspace) findViewById(R.id.np_workspace);
                 HorizontalRatingBar b = (HorizontalRatingBar) findViewById(R.id.np_albumRating);
 
                 if(mSession == null || !mSession.isTunedIn() || !mSession.hasCredentials()) {
                     if(e.getAction() == MotionEvent.ACTION_DOWN) {
                         w.lockCurrentScreen();
                         b.setLabel(R.string.msg_tuneInFirst);
                     }
                     else if(e.getAction() == MotionEvent.ACTION_UP) {
                         w.unlockCurrentScreen();
                         b.setLabel(R.string.label_album);
                     }
                     return true;
                 }
 
 
                 HorizontalRatingBar hrb = (HorizontalRatingBar) v;
                 float rating = 0.0f;
                 float max = 5.0f;
                 switch(e.getAction()) {
                 case MotionEvent.ACTION_DOWN:
                     w.lockCurrentScreen();
                 case MotionEvent.ACTION_MOVE:
                 case MotionEvent.ACTION_UP:
                     rating = hrb.getPrimary();
                     max = hrb.getMax();
                     String label = String.format(Locale.US, "%.1f/%.1f",rating,max);
                     hrb.setLabel(label);
 
                     if(e.getAction() == MotionEvent.ACTION_UP) {
                         w.unlockCurrentScreen();
                         b.setLabel(R.string.label_album);
                     }
                 }
                 return true;
             }
         });
 
 
         final ListView election = (ListView) findViewById(R.id.np_electionList);
         election.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View v, int i, long id) {
                 SongListAdapter adapter = (SongListAdapter) election.getAdapter();
                 Song song = adapter.getSong(i);
 
                 // Do nothing if they selected the item we last voted for.
                 if(song.getElectionEntryId() == mSession.getLastVoteId()) {
                     return;
                 }
 
                 // Show a message if they are not tuned in.
                 if(mSession.isTunedIn() && mSession.hasCredentials()) {
                     new VoteTask(i).execute();
                 }
                 else {
                     showDialog(R.string.msg_tunedInVote);
                 }
             }
         });
 
         final TouchInterceptor requestList = ((TouchInterceptor) findViewById(R.id.np_request_list));
         requestList.setDropListener(new TouchInterceptor.DropListener() {
             @Override
             public void drop(int from, int to) {
                 if(from == to) return;
                 SongListAdapter adapter = (SongListAdapter) requestList.getAdapter();
                 adapter.moveSong(from, to);
 
                 ArrayList<Song> songs = adapter.getSongs();
                 requestReorder( songs.toArray(new Song[songs.size()]) );
             }
         });
 
         requestList.setOnTouchListener(new OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent ev) {
                 if(requestList.getCount() == 0) return false;
                 Workspace w = (Workspace) findViewById(R.id.np_workspace);
 
                 // Benefit of the doubt: unlock in case we locked earlier.
                 if(ev.getAction() == MotionEvent.ACTION_UP) {
                     w.unlockCurrentScreen();
                 }
 
                 float x = ev.getX();
                 if(ev.getAction() == MotionEvent.ACTION_DOWN && x < 64) {
                     w.lockCurrentScreen();
                 }
                 return false;
             }
         });
 
         Workspace w = (Workspace) findViewById(R.id.np_workspace);
         w.setOnScreenChangeListener(new OnScreenChangeListener() {
             @Override
             public void onScreenChanged(View newScreen, int newScreenIndex) {
                 PagerWidget pw = (PagerWidget) findViewById(R.id.pager);
                 pw.setCurrent(newScreenIndex);
             }
 
             @Override
             public void onScreenChanging(View newScreen, int newScreenIndex) {
 
             }
         });
 
 
         // Button Listeners.
         ImageButton play = (ImageButton) findViewById(R.id.np_play);
         ImageButton station = (ImageButton) findViewById(R.id.np_stationPick);
         ImageButton request = (ImageButton) findViewById(R.id.np_makeRequest);
 
         play.setEnabled(false);
         station.setEnabled(false);
 
         play.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 startPlayer();
             }
         });
 
         station.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 showDialog(DIALOG_STATION_PICKER);
             }
         });
 
         request.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 startPlaylist();
             }
         });
 
         registerForContextMenu(findViewById(R.id.np_request_list));
     }
 
     /**
      * Stops ALL AsyncTasks and removes
      * all references to them.
      */
     private void stopTasks() {
         if(mRateTask != null) {
             mRateTask.cancel(true);
             mRateTask = null;
         }
 
         if(mReorderTask != null) {
             mReorderTask.cancel(true);
             mReorderTask = null;
         }
 
         if(mRemoveTask != null) {
             mRemoveTask.cancel(true);
             mRemoveTask = null;
         }
     }
 
     /** Tell a SyncService that it should stop. */
     private void stopSyncService() {
         stopService(new Intent(NowPlayingActivity.this, SyncService.class)
                 .setAction(SyncService.ACTION_INFO)
         );
         stopService(new Intent(NowPlayingActivity.this, SyncService.class)
                 .setAction(SyncService.ACTION_SYNC)
         );
     }
 
     /**
      * Force an update of the schedule information.
      */
     private void refresh() {
         stopSyncService();
         Intent local = new Intent(this, SyncService.class);
         local.setAction(SyncService.ACTION_INFO);
         startService(local);
         if(mHasIndeterminateProgress) {
             setProgressBarIndeterminateVisibility(true);
             setProgressBarIndeterminate(true);
         }
     }
 
     /**
      * Performs an update of song info.
      * @param init flag to indicate this
      *   is an initial (non-long-poll) fetch.
      */
     private void fetchSchedules() {
         // Some really bad thing happened and we don't
         // have a connection at all.
         if(mSession == null) {
             Rainwave.showError(NowPlayingActivity.this, R.string.msg_sessionError);
             return;
         }
 
         // Only do an immediate fetch from here.
         if(mSession.requiresSync()) {
             refresh();
         }
         else {
             // already have one so just sync
             onScheduleSync();
         }
     }
 
     private void requestReorder(Song requests[]) {
         if(mSession == null) {
             Rainwave.showError(NowPlayingActivity.this, R.string.msg_sessionError);
             return;
         }
 
         if(mReorderTask == null) {
             mReorderTask = new ActionTask();
             mReorderTask.execute(ActionTask.REORDER, requests);
         }
     }
 
     private void requestRemove(Song s) {
         if(mSession == null) {
             Rainwave.showError(NowPlayingActivity.this, R.string.msg_sessionError);
             return;
         }
 
         if(mRemoveTask == null) {
             mRemoveTask = new ActionTask();
             mRemoveTask.execute(ActionTask.REMOVE, s);
         }
     }
 
     /**
      * Sets the vote drawer to opened or closed. Does nothing if the auto-slide preference is disabled.
      * 
      * @param state, true for open, false for closed
      */
     private void setDrawerState(boolean state) {
         if(!mPreferences.getAutoshowElection()) {
             return;
         }
         SlidingDrawer v = (SlidingDrawer) this.findViewById(R.id.np_drawer);
         if(state && !v.isOpened()) {
             v.animateOpen();
         }
         else if(v.isOpened()) {
             v.animateClose();
         }
     }
 
     /** Shows the menu */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     /** Responds to menu selection */
     public boolean onOptionsItemSelected(MenuItem item) {
         switch(item.getItemId()) {
 
         // Start RainwavePreferenceActivity.
         case R.id.menu_preferences:
             startPreferences();
             break;
 
         case R.id.menu_refresh:
             refresh();
             break;
         }
 
         return false;
     }
 
     private void startPlayer() {
         int stationId = mSession.getStationId();
         Station s = mSession.getStation(stationId);
         if(s != null) {
             Intent i = new Intent(Intent.ACTION_VIEW);
             i.setDataAndType(Uri.parse(s.getMainStream()), "audio/*");
             startActivity(i);    
         }
         else {
             Toast.makeText(this, R.string.msg_streamNotKnown, Toast.LENGTH_SHORT).show();
         }
     }
 
     private void startPlaylist() {
         Intent i = new Intent(this, PlaylistActivity.class);
         startActivity(i);
     }
 
     private void startPreferences() {
         Intent i = new Intent(this, RainwavePreferenceActivity.class);
         startActivity(i);
     }
 
     /**
      * Destroys any existing Session and creates
      * a new Session object for us to use, pulling
      * the user_id and key attributes from the default
      * Preference store.
      */
     private void initializeSession() {
         handleIntent();
         mSession = Session.getInstance(this);
         mPreferences = RainwavePreferences.getInstance(this);
 
         View playlistButton = findViewById(R.id.np_makeRequest);
         if(playlistButton != null) {
             playlistButton.setVisibility(
                 (mSession != null && mSession.hasCredentials()) ? View.VISIBLE : View.GONE
             );
         }
     }
 
 
     /**
      * Handle activity intent. This activity is configured to handle rw:// URL's
      * if triggered from elsewhere in the OS.
      */
     private void handleIntent() {
         final Intent i = getIntent();
 
         if(i == null) {
             return;
         }
         Bundle b = i.getExtras();
         Uri uri = i.getData();
 
         if(uri == null){
             return;
         }
 
         // check if this Intent was previously handled
         boolean handled = (b != null) && b.getBoolean(Rainwave.HANDLED_URI, false);
         if(handled) {
             return;
         }
 
         // store in preferences if all is well
         final String parts[] = Rainwave.parseUrl(uri, this);
         if(parts != null) {
             mPreferences.setUserInfo(parts[0], parts[1]);
         }
 
         i.putExtra(Rainwave.HANDLED_URI, true);
     }
 
     /**
      * Executes when a schedule sync finished.
      * @param response the response the server issued
      */
     private void onScheduleSync() {
         // We should enable the buttons now.
         ImageButton play = (ImageButton) findViewById(R.id.np_play);
         ImageButton station = (ImageButton) findViewById(R.id.np_stationPick);
 
         play.setEnabled(mSession.hasStations());
         station.setEnabled(mSession.hasStations());
 
         // Updates title, album, and artists.
         updateSongInfo(mSession.getCurrentEvent().getCurrentSong());
 
         // Updates song, album ratings.
         setRatings(mSession.getCurrentEvent().getCurrentSong());
 
         // Updates election info.
         updateElection();
 
         // Refresh clock and title bar state.
         refreshTitle();
 
         // Updates request lsit.
         updateRequests();
 
         // Update album art if there is any
         updateAlbumArt(mSession.getCurrentAlbumArt());
 
         // Cancel old countdown if there is one.
         if(mCountdown != null) {
             mCountdown.cancel();
         }
 
         // Start a countdown task for the event.
         if(mSession.getCurrentEvent() != null) {
             mCountdown = new CountDownTimer(mSession.getCurrentEvent().getEnd() - mSession.getDrift(), 1000) {
                 public void onFinish() { }
 
                 public void onTick(long millisUntilFinished) {
                     refreshTitle();
                 }
             }.start();
         }
     }
 
     private void refreshTitle() {
         long end = mSession.getCurrentEvent().getEnd();
         long utc = System.currentTimeMillis() / 1000;
         long seconds = (end - utc) - mSession.getDrift();
 
         seconds = Math.max(0, seconds);
         long minutes = seconds / 60;
         seconds %= 60;
 
         Resources r = getResources();
         int id = mSession.getStationId();
         String stationName = (mSession.hasStations()) ? mSession.getStation(id).getName() : null;
         String title = (stationName != null) ? stationName : r.getString(R.string.app_name);
         String state = r.getString(R.string.label_nottunedin);
 
         if(!mSession.hasCredentials()) {
             state = r.getString(R.string.label_anonymous);
         }
         else if(mSession.isTunedIn()) {
             state = r.getString(R.string.label_tunedin);
         }
 
         setTitle(String.format("[%2d:%02d] %s (%s)", minutes, seconds, title, state));
     }
 
     private void updateElection() {
         SongListAdapter adapter = new SongListAdapter(
                 this,
                 R.layout.item_song_election,
                 new ArrayList<Song>(Arrays.asList(mSession.getNextEvent().cloneSongs()))
         );
         ((ListView)findViewById(R.id.np_electionList))
            .setAdapter(adapter);
 
         // Set vote deadline for when the song ends.
         adapter.setDeadline(mSession.getCurrentEvent().getEnd() - mSession.getDrift());
 
         // Open the drawer if the user can vote.
         boolean canVote = !mSession.hasLastVote() && mSession.isTunedIn();
         setDrawerState(canVote);
 
         if(mSession.hasLastVote()) {
             adapter.updateVoteState(mSession.getLastVoteId());
         }
     }
 
     private void updateRequests() {
         Song songs[];
 
         if(mSession.hasRequests()){
             songs = mSession.cloneRequests();
         }
         else {
             songs = new Song[0];
         }
 
         TouchInterceptor requestList = (TouchInterceptor) findViewById(R.id.np_request_list);
         requestList.setAdapter(
             new SongListAdapter(
                 this,
                 R.layout.item_song_request,
                 new ArrayList<Song>(Arrays.asList(songs))
             )
         );
 
         resyncRequests();
     }
 
     private void resyncRequests() {
         TouchInterceptor requestList = (TouchInterceptor) findViewById(R.id.np_request_list);
         SongListAdapter adapter = (SongListAdapter) requestList.getAdapter();
         if(adapter != null) {
             int visibility = (adapter.getCount()) > 0 ? View.GONE : View.VISIBLE;
                findViewById(R.id.np_request_overlay).setVisibility(visibility);
         }
     }
 
     /**
      * Updates the song title, album title, and
      * artists in the user interface.
      * @param current the current song that's playing.
      */
     private void updateSongInfo(Song current) {
         ((TextView) findViewById(R.id.np_songTitle)).setText(current.getTitle());
         ((TextView) findViewById(R.id.np_albumTitle)).setText(current.getDefaultAlbum().getName());
         ((TextView) findViewById(R.id.np_artist)).setText(current.collapseArtists());
 
         ImageView accent = (ImageView)findViewById(R.id.np_accent);
         TextView requestor = (TextView)findViewById(R.id.np_requestor);
         Resources r = getResources();
 
         if(current.isRequest()) {
             accent.setImageResource(R.drawable.accent_song_hilight);
             requestor.setVisibility(View.VISIBLE);
             requestor.setText(String.format(r.getString(R.string.label_requestor), current.getRequestor()));
         }
         else {
             accent.setImageResource(R.drawable.accent_song);
             requestor.setVisibility(View.GONE);
         }
     }
 
     /**
      * Updates the song and album ratings.
      * @param current the current song playing
      */
     private void setRatings(Song current) {
         final Album album = current.getDefaultAlbum();
         ((HorizontalRatingBar) findViewById(R.id.np_songRating))
            .setBothValues(current.getUserRating(), current.getCommunityRating());
 
         ((HorizontalRatingBar) findViewById(R.id.np_albumRating))
             .setBothValues(album.getUserRating(), album.getCommunityRating());
     }
 
     /**
      * Executes when a "rate song" request has finished.
      * @param result the result the server issued
      */
     private void onRateSong(SongRating rating) {
         ((HorizontalRatingBar) findViewById(R.id.np_songRating))
                 .setPrimaryValue(rating.getUserRating());
 
         ((HorizontalRatingBar) findViewById(R.id.np_albumRating))
             .setPrimaryValue(rating.getDefaultAlbumRating().getUserRating());
     }
 
     /**
      * Sets the album art to the provided Bitmap, or
      * a default image if art is null.
      * @param art desired album art
      */
     private void updateAlbumArt(Bitmap art) {
         if(art == null) {
             art = BitmapFactory.decodeResource(getResources(), R.drawable.noart);
         }
 
         ((ImageView) findViewById(R.id.np_albumArt)).setImageBitmap(art);
     }
 
     /** Receives schedule updates from the sync service. */
     private BroadcastReceiver mEventUpdateReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             setProgressBarIndeterminateVisibility(false);
             onScheduleSync();
 
             // Let the service know we would like updates.
             if(mSession.hasCredentials()) {
                 Intent local = new Intent(NowPlayingActivity.this, SyncService.class);
                 local.setAction(SyncService.ACTION_SYNC);
                 startService(local);
             }
         }
     };
 
     /**
      * AsyncTask for submitting a rating for a song.
      * Expects two arguments to <code>execute(Object...params)</code>,
      * which are song_id (int), and rating (float).
      * @author pkilgo
      *
      */
     protected class ActionTask extends AsyncTask<Object, Integer, Object> {
         private int mAction;
 
         @Override
         protected Object doInBackground(Object ... params) {
             Log.d(TAG, "Beginning ActionTask.");
             mAction = (Integer) params[0];
             Thread currentThread = Thread.currentThread();
 
             try {
                 switch(mAction) {
                 case RATE:
                     currentThread.setName("RateTask");
                     int songId = (Integer) params[1];
                     float rating = (Float) params[2];
                     return mSession.rateSong(songId, rating);
 
                 case REMOVE:
                     currentThread.setName("RemoveTask");
                     Song s = (Song) params[1];
                     mSession.deleteRequest(s);
                     break;
 
                 case REORDER:
                     currentThread.setName("ReorderTask");
                     Song songs[] = (Song[]) params[1];
                     return mSession.reorderRequests(songs);
 
                 }
             } catch (RainwaveException e) {
                 Log.e(TAG, "API error: " + e.getMessage());
                 Rainwave.showError(NowPlayingActivity.this, e);
             }
             return null;
         }
 
         protected void onPostExecute(Object result) {
             Log.d(TAG, "ActionTask ended.");
 
             switch(mAction) {
             case RATE:
                 mRateTask = null;
                 if(result == null) return;
                 onRateSong((SongRating) result);
                 break;
 
             case REORDER:
                 mReorderTask = null;
                 break;
 
             case REMOVE:
                 mRemoveTask = null;
                 break;
 
             }
         }
 
         public static final int
             REMOVE = 0x439023,
             RATE = 0x4A73,
             REORDER = 0x4304D34;
     }
     
     private class VoteTask extends AsyncTask<Song, Integer, Boolean> {
 
         private Song mSong;
 
         private int mSelection;
 
         public VoteTask(int selection) {
             ListView electionList = (ListView) findViewById(R.id.np_electionList);
             SongListAdapter adapter = (SongListAdapter) electionList.getAdapter();
             mSelection = selection;
             mSong = adapter.getSong(selection);
         }
 
         @Override
         protected void onPreExecute() {
             ListView electionList = (ListView) findViewById(R.id.np_electionList);
             SongListAdapter adapter = (SongListAdapter) electionList.getAdapter();
             adapter.setVoting(mSelection);
         }
 
         protected Boolean doInBackground(Song...params) {
             try {
                 mSession.vote(mSong.getElectionEntryId());
                 return true;
             } catch (RainwaveException e) {
                 Rainwave.showError(NowPlayingActivity.this, e);
                 Log.e(TAG, "API Error: " + e);
             }
 
             return false;
         }
 
         protected void onPostExecute(Boolean result) {
             ListView electionList = (ListView) findViewById(R.id.np_electionList);
             SongListAdapter adapter = (SongListAdapter) electionList.getAdapter();
             adapter.resyncVoteState(mSession.getLastVoteId());
         }
     }
 
     /** Handler keys */
     private static final String
         BOOL_STATUS = "bool_status";
 
     /** Dialog identifiers */
     public static final int
         DIALOG_STATION_PICKER = 0xb1c7;
 }
