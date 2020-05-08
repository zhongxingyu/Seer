 package fm.krui.kruifm;
 
 import android.app.ProgressDialog;
 import android.content.*;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import android.view.*;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.*;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 
 public class StreamFragment extends Fragment {
 
     private static String TAG = StreamFragment.class.getName();
 
     /* Constants */
     final static int MAIN_STUDIO = 0;
     final static int THE_LAB = 1;
 
     // HashMap Constants
     final private String KEY_ARTIST = "artist";
     final private String KEY_TRACK = "name";
     final private String KEY_ALBUM = "album";
 
     /* Class members */
     private String streamUrl ="";
     private int stationTag;
     private Timer updateTimer = new Timer();
     private TimerTask updateTimerTask;
     private PreferenceManager prefManager;
     private FavoriteTrackManager favTrackManager;
     private int stationSpinnerPosition;
     private boolean isPlaying = false;
     ProgressDialog pd;
 
     // FIXME: This member isn't efficient
     SharedPreferences trackPrefs;
 
     // Service broadcast receiver
     private BroadcastReceiver broadcastReceiver;
 
     // Temporary
     private boolean trackIsFavorite = false;
 
     /**
      * Stream Fragments are constructed with an integer tag to determine which URL to stream.
      * @param stationTag (0 => MAIN_STUDIO, 1 => LAB)
      */
     public StreamFragment(int stationTag) {
         this.stationTag = stationTag;
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         // Inflate the menu; this adds items to the action bar if it is present.
         inflater.inflate(R.menu.stream_activity_menu, menu);
         super.onCreateOptionsMenu(menu, inflater);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.call_studio_button:
                 IntentManager im = new IntentManager(getActivity());
                 im.dialStudio();
                 break;
 
         }
         return true;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.stream_layout, container, false);
         return view;
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         // Instantiate class members
         prefManager = new PreferenceManager(getActivity());
         favTrackManager = new FavoriteTrackManager(getActivity());
 
         // FIXME: Move these to PreferenceManager after expanding its scope for cleaner code
         trackPrefs = getActivity().getSharedPreferences(StreamService.PREFS_NAME, 0);
 
         // Instantiate broadcast receiver
         broadcastReceiver = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 processBroadcastCommand(intent);
             }
         };
 
         // Determine the URL we need to use to stream based on the station tag and quality preferences
         streamUrl = getStreamUrl(stationTag);
         Log.v(TAG, "streamUrl is now set to: " + streamUrl);
 
         // Perform initial configuration of audio server
         changeUrl(stationTag);
 
         // Begin buffering the audio
         startAudio((ImageView)getActivity().findViewById(R.id.play_audio_imageview));
 
         // Build play button listener
         final ImageView playButton = (ImageView)getActivity().findViewById(R.id.play_audio_imageview);
         playButton.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 handleAudio(playButton);
             }
         });
 
         // Build volume seek bar listener
         // ** DISABLED FOR NOW -- This might be completely thrown out. **
         /*final SeekBar volumeSeekBar = (SeekBar)getActivity().findViewById(R.id.volume_seekbar);
         volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 
             // When seek bar progress is changed, change the audio of the media player appropriately.
             @Override
             public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                 // Send new volume via intent? Will this be slow?
             }
 
             @Override
             public void onStartTrackingTouch(SeekBar seekBar) {
             }
 
             @Override
             public void onStopTrackingTouch(SeekBar seekBar) {
             }
         }); */
 
         // Build settings button listener and apply it to settings icon and submit button
         View.OnClickListener flipListener = new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Card flip animation which toggles between stream controls and settings views
                 flipCard();
             }
         };
 
         final ImageView settingsButton = (ImageView)getActivity().findViewById(R.id.stream_settings_imageview);
         final Button saveSettingsButton = (Button)getActivity().findViewById(R.id.set_stream_settings_button);
         settingsButton.setOnClickListener(flipListener);
         saveSettingsButton.setOnClickListener(flipListener);
 
         // Build favorite button listener
         final ImageView favoriteButton = (ImageView)getActivity().findViewById(R.id.stream_favorite_imageview);
         favoriteButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (trackIsFavorite) {
                     favoriteButton.setImageResource(R.drawable.star_unfilled_white);
                     trackIsFavorite = false;
                     removeTrackFromFavorites();
                 } else {
                     favoriteButton.setImageResource(R.drawable.star_filled_white);
                     trackIsFavorite = true;
                     addTrackToFavorites();
                 }
             }
         });
 
         // Build settings switches
         final Switch streamQualitySwitch = (Switch)getActivity().findViewById(R.id.stream_quality_switch);
         final Switch albumArtSwitch = (Switch)getActivity().findViewById(R.id.stream_album_art_switch);
 
         // Set initial state of switches
         albumArtSwitch.setChecked(prefManager.getAlbumArtDownloadPreference());
         if (prefManager.getStreamQuality() == prefManager.HIGH_QUALITY) {
             streamQualitySwitch.setChecked(true);
         } else {
             streamQualitySwitch.setChecked(false);
         }
 
         // Assign listeners to switches
         streamQualitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 if (isChecked) {
                     prefManager.setStreamQuality(prefManager.HIGH_QUALITY);
                     Log.v(TAG, "Stream quality setting is now: " + prefManager.getStreamQuality());
                 } else {
                     prefManager.setStreamQuality(prefManager.LOW_QUALITY);
                     Log.v(TAG, "Stream quality setting is now: " + prefManager.getStreamQuality());
                 }
                 changeUrl(stationSpinnerPosition);
             }
         });
 
         albumArtSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 if (isChecked) {
                     prefManager.setAlbumArtDownloadPreference(true);
                     Log.v(TAG, "Album Art Download setting is now " + prefManager.getAlbumArtDownloadPreference());
                 } else {
                     prefManager.setAlbumArtDownloadPreference(false);
                     Log.v(TAG, "Album Art Download setting is now " + prefManager.getAlbumArtDownloadPreference());
                 }
             }
         });
 
     }
 
     /**
      * Performs animation between album art pane and settings view.
      */
     private void flipCard() {
         View rootLayout = (View)getActivity().findViewById(R.id.stream_now_playing_container_relativelayout);
         View cardFace = (View)getActivity().findViewById(R.id.album_art_pane);
         View cardBack = (View)getActivity().findViewById(R.id.player_settings_tablelayout);
 
         FlipAnimation flipAnimation = new FlipAnimation(cardFace, cardBack);
 
         if (cardFace.getVisibility() == View.GONE)
         {
             flipAnimation.reverse();
         }
         rootLayout.startAnimation(flipAnimation);
     }
 
     private void addTrackToFavorites() {
         String trackName = "";
         String artistAlbumName = "";
         TextView trackNameTextView = (TextView)getActivity().findViewById(R.id.song_name_textview);
         TextView artistAlbumNameTextView = (TextView)getActivity().findViewById(R.id.artist_album_name_textview);
 
         // Extract text from their views
         try {
             trackName = trackNameTextView.getText().toString();
             artistAlbumName = artistAlbumNameTextView.getText().toString();
 
         } catch (NullPointerException e) {
             Log.e(TAG, "Caught NullPointerException when extracting values from StreamFragment textviews!");
             e.printStackTrace();
         }
 
         // Build a Track from currently playing track.
         Track track = new Track();
         track.setTitle(trackName);
 
         // Separate Artist/Album Name
         String[] arr =artistAlbumName.split("-");
 
         track.setAlbum(arr[1]);
         track.setArtist(arr[0]);
 
         // Add this track to favorites
         favTrackManager.addTrackToFavorites(track);
         String message = getString(R.string.add_favorite);
         showStreamStatusBar(message, false);
     }
 
     private void removeTrackFromFavorites() {
         favTrackManager.removeTrackFromFavorites();
         String message = getString(R.string.remove_favorite);
         showStreamStatusBar(message, false);
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Initialize screen lock/wake receiver
         IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
         filter.addAction(Intent.ACTION_SCREEN_OFF);
         BroadcastReceiver screenReceiver = new ScreenReceiver();
 
         // Attach it to this activity
         getActivity().getApplicationContext().registerReceiver(screenReceiver, filter);
 
     }
 
     @Override
     public void onResume() {
 
         if (!ScreenReceiver.wasScreenOn) {
             // NOTE: this.onResume() is called BEFORE ScreenReceiver.onReceive()
             // When onResume is called due to a screen state change, perform a track update to ensure all info is up to date.
             Intent intent = new Intent(getActivity(), StreamService.class);
             intent.setAction(StreamService.ACTION_START_UPDATES);
             getActivity().startService(intent);
             Log.v(TAG, "Track updates resumed.");
 
         } else {
             // When onResume is called when the screen is NOT being unlocked.
         }
 
         super.onResume();
 
         Log.v(TAG, "StreamFragment onResume called!");
 
         // Retrieve favorites list from memory
         favTrackManager.loadFavoriteTracks();
 
         // Restore UI state
         restoreFavoriteState();
 
         // Register broadcast receiver to receive updates
         LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(StreamService.BROADCAST_MESSAGE));
     }
 
     @Override
     public void onPause() {
         super.onPause();
 
         // When the screen is about to turn off, handle state changes
         if (ScreenReceiver.wasScreenOn) {
             // NOTE: this.onPause() is called BEFORE ScreenReceiver.onReceive()
             // If onPause is called by the system due to a screen state change
             Log.i(TAG, "Screen is about to turn off! Stop track updates!");
             Intent intent = new Intent();
 
         } else {
             // If onPause is called when the screen state has NOT changed
         }
 
         // Store favoriteMap into memory
         Log.v(TAG, "Attempting to save favorite tracks...");
         favTrackManager.storeFavoriteTracks();
 
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
 
     }
 
     /**
      * Handles all broadcast commands received from audio service
      * @param intent Broadcasted intent
      */
     private void processBroadcastCommand(Intent intent) {
 
         // Get the specific command from intent
         String broadcastCommand = intent.getStringExtra(StreamService.BROADCAST_KEY);
 
         if (broadcastCommand != null) {
 
             // If play command is received, store state and update UI appropriately.
             if (broadcastCommand.equals(StreamService.BROADCAST_COMMAND_PLAY)) {
                 isPlaying = true;
 
                 // Change image of button to pause icon.
                 ImageView playButton = (ImageView)getActivity().findViewById(R.id.play_audio_imageview);
                 playButton.setImageResource(R.drawable.pause_icon_white);
             }
 
             // If pause command is received, store state and update UI appropriately.
             else if (broadcastCommand.equals(StreamService.BROADCAST_COMMAND_PAUSE)) {
                 isPlaying = false;
 
                 // Change image of button to play icon.
                 ImageView playButton = (ImageView)getActivity().findViewById(R.id.play_audio_imageview);
                 playButton.setImageResource(R.drawable.play_icon_white);
             }
 
             // If stop command is received, store state and clear UI elements.
             // FIXME: Is having a separate stop command necessary when a pause command exists? Maybe or maybe not. Right now it just does the same thing...
             else if (broadcastCommand.equals(StreamService.BROADCAST_COMMAND_STOP)) {
                 isPlaying = false;
 
                 // Change image of button to play icon.
                 ImageView playButton = (ImageView)getActivity().findViewById(R.id.play_audio_imageview);
                 playButton.setImageResource(R.drawable.play_icon_white);
             }
 
             else if (broadcastCommand.equals(StreamService.BROADCAST_COMMAND_UPDATE_PENDING)) {
                 setLoadingIndicator(true);
             }
 
             // If track update command is received, retrieve playing track information and apply it to UI.
             else if (broadcastCommand.equals(StreamService.BROADCAST_COMMAND_UPDATE)) {
                 // Update UI with new information
                 updateTrackInfo();
             }
 
             // If a status message is received, show the status bar and display the message.
             else if (broadcastCommand.equals(StreamService.BROADCAST_COMMAND_STATUS_MESSAGE)) {
 
                 // Extract message and determine if this message should stay until cancelled.
                 String streamStatus = intent.getStringExtra(StreamService.STREAM_STATUS_KEY);
                 boolean indefiniteMessage = intent.getBooleanExtra(StreamService.STREAM_STATUS_DISPLAY_LENGTH, false);
 
                 // Write this message to the status bar
                 if (indefiniteMessage) {
                     Log.v(TAG, "Status received: " + streamStatus);
                     showStreamStatusBar(streamStatus, true);
                 } else {
                     Log.v(TAG, "Indefinite status received: " + streamStatus);
                     showStreamStatusBar(streamStatus, false);
                 }
             }
 
             // If a status hide message is received, an indefinite status is being displayed. Manually hide the status bar.
             else if (broadcastCommand.equals(StreamService.BROADCAST_COMMAND_STATUS_HIDE)) {
                 hideStatusBar();
                 Log.v(TAG, "Broadcast received to hide status bar. Manually hiding.");
             }
         } else {
             Log.e(TAG, "ERROR: Broadcast received but could not extract command.");
         }
 
     }
 
     protected void changeUrl(int stationTag) {
         // URL depends on spinner position!
         // This turned out to be kind of cool, but this could cause problems if this order ever changes!
         streamUrl = getStreamUrl(stationTag);
 
         Log.v(TAG, "Requesting audio service to change url to " + streamUrl);
         Intent intent = new Intent(getActivity(), StreamService.class);
         intent.putExtra(StreamService.INTENT_STREAM_URL, streamUrl);
         intent.setAction(StreamService.ACTION_CHANGE_URL);
         getActivity().startService(intent);
     }
 
     /**
      * Determines the URL of the correct quality (based on user prefs) when passed a station identifier.
      * @param station Integer tag representing KRUI (0) or The Lab (1)
      * @return URL of correct stream
      */
     private String getStreamUrl(int station) {
         String URL = "http://krui.student-services.uiowa.edu:";
         String mainLowPort = "8200";
         String mainHighPort = "8000";
         String labLowPort = "8105";
         String labHighPort = "8103";
 
         // Based on station passed, change the URL and display a notice to the user.
         prefManager = new PreferenceManager(getActivity());
         String mainMessage = "";
         switch (station) {
             case MAIN_STUDIO:
                 if (prefManager.getStreamQuality() == prefManager.HIGH_QUALITY) {
                     URL += mainHighPort;
                     mainMessage = getString(R.string.changing_high_quality);
                 } else {
                     URL += mainLowPort;
                     mainMessage = getString(R.string.changing_low_quality);
                 }
                 showStreamStatusBar(mainMessage, false);
                 break;
 
             case THE_LAB:
                 if (prefManager.getStreamQuality() == prefManager.HIGH_QUALITY) {
                     URL += labHighPort;
                     mainMessage = getString(R.string.changing_high_quality);
                 } else {
                     URL += labLowPort;
                     mainMessage = getString(R.string.changing_low_quality);
                 }
                 break;
         }
         showStreamStatusBar(mainMessage, false);
         return URL;
     }
 
     public void pauseAudio(ImageView playButton) {
 
         isPlaying = false;
 
         // Kill the audio service
         Intent stopIntent = new Intent(getActivity(), StreamService.class);
         getActivity().stopService(stopIntent);
         Log.v(TAG, "Audio service stopped!");
 
         // Change image of button to play icon.
         playButton.setImageResource(R.drawable.play_icon_white);
     }
 
     public void startAudio(ImageView playButton) {
 
        // FIXME: WHY WHY WHY do I have to call changeUrl BEFORE I can start the audio? The URL is stored in StreamService, right? I don't FUCKING UNDERSTAND THIS!
         changeUrl(stationTag);
 
         // Request service to start audio
         Intent intent = new Intent(getActivity(), StreamService.class);
         intent.putExtra(StreamService.INTENT_STREAM_URL, streamUrl);
         intent.setAction(StreamService.ACTION_PLAY);
         getActivity().startService(intent);
         isPlaying = true;
 
         // Change image of button to pause icon.
         playButton.setImageResource(R.drawable.pause_icon_white);
 
         // Hide the "no audio playing" overlay
         RelativeLayout noAudioContainer = (RelativeLayout)getActivity().findViewById(R.id.no_audio_playing_container_relativelayout);
         RelativeLayout albumArtContainer = (RelativeLayout)getActivity().findViewById(R.id.stream_album_art_container);
         noAudioContainer.setVisibility(View.INVISIBLE);
         albumArtContainer.setVisibility(View.VISIBLE);
     }
 
     /**
      * Handles changes in audio behavior when play button is pressed by user. Depending on the context, this function
      * will either resume paused audio, pause the audio, or buffer and play the stream.
      * @param playButton
      */
     public void handleAudio(ImageView playButton) {
 
         // If media player is playing, pause the audio.
         if (isPlaying) {
             pauseAudio(playButton);
         } else {
             startAudio(playButton);
         }
     }
 
     /**
      * Refreshes the title, album name, art, and other info from SharedPreferences and updates the UI.
      */
     public void updateTrackInfo() {
         TextView songNameTextView = (TextView)getActivity().findViewById(R.id.song_name_textview);
         TextView artistAlbumTextView = (TextView)getActivity().findViewById(R.id.artist_album_name_textview);
         ImageView albumArtImageView = (ImageView)getActivity().findViewById(R.id.album_art_pane);
 
         // Get current track information and write it to textviews
         String trackName = trackPrefs.getString(StreamService.PREFKEY_TRACK, "");
         String artistName = trackPrefs.getString(StreamService.PREFKEY_ARTIST, "");
         String albumName = trackPrefs.getString(StreamService.PREFKEY_ALBUM, "");
         songNameTextView.setText(trackName);
         artistAlbumTextView.setText(artistName + " - " + albumName);
 
 
         // Get album art if it has been saved
         Bitmap bitmap = BitmapFactory.decodeFile(getActivity().getFilesDir() + "/" + TrackUpdateHandler.ALBUM_ART_FILENAME);
         if (bitmap != null) {
             // If art was retrieved, load it into the album art pane.
             albumArtImageView.setImageBitmap(bitmap);
         } else {
             // If nothing could be retrieved, load the placeholder album art image.
             albumArtImageView.setImageResource(R.drawable.noalbumart);
             Log.w(TAG, "Album art could not be decoded from SharedPreferences.");
         }
 
         // Since a new track is now on the screen, reset the status of the favorite button so the new track is addable.
         final ImageView favoriteButton = (ImageView)getActivity().findViewById(R.id.stream_favorite_imageview);
         trackIsFavorite = false;
         favoriteButton.setImageResource(R.drawable.star_unfilled_white);
         Log.v(TAG, "New track has been updated. Favorite status has been reset.");
 
         // Hide the loading indicator
         setLoadingIndicator(false);
     }
 
     /**
      * Enables and disables the loading indicator for UI views.
      * @param showLoadingIndicator True to enable, false to disable.
      */
     private void setLoadingIndicator(final boolean showLoadingIndicator) {
         Log.v(TAG, "setLoadingIndicator called!");
         Log.v(TAG, "Called with " + showLoadingIndicator + " parameter.");
         // Instantiate views
         ProgressBar progressBar = (ProgressBar)getActivity().findViewById(R.id.album_art_progressbar);
         ImageView albumArtPaneImageView = (ImageView)getActivity().findViewById(R.id.album_art_pane);
         ImageView albumArtLoadingPaneImageView = (ImageView)getActivity().findViewById(R.id.album_art_loading_pane);
         if (showLoadingIndicator) {
             // Show loading indicators
             progressBar.setVisibility(View.VISIBLE);
             albumArtLoadingPaneImageView.setVisibility(View.VISIBLE);
             albumArtPaneImageView.setVisibility(View.INVISIBLE);
 
         } else {
             // Hide progressBar and placeholder image and re-show album art pane.
             progressBar.setVisibility(View.INVISIBLE);
             albumArtLoadingPaneImageView.setVisibility(View.INVISIBLE);
             albumArtPaneImageView.setVisibility(View.VISIBLE);
         }
     }
 
     /**
      * Returns the current track information as a Track object
      * @return
      */
     private Track getCurrentTrack() {
         Track track = new Track();
         SharedPreferences prefs = getActivity().getApplicationContext().getSharedPreferences(StreamService.PREFS_NAME, 0);
         track.setTitle(prefs.getString(StreamService.PREFKEY_TRACK, ""));
         track.setArtist(prefs.getString(StreamService.PREFKEY_ARTIST, ""));
         track.setAlbum(prefs.getString(StreamService.PREFKEY_ALBUM, ""));
         return track;
     }
 
     /**
      * Moves the stream status bar into view to show messages regarding stream status. Use indefinite time constraints
      * @param message String to display in the status bar.
      * @param isIndefinite true if the message should be displayed until explicitly cancelled by a broadcast message.
      */
     public void showStreamStatusBar(String message, boolean isIndefinite) {
         final LinearLayout statusContainer = (LinearLayout)getActivity().findViewById(R.id.stream_status_container_linearlayout);
         // Apply label text, then bring the status bar into view
         TextView statusLabel = (TextView)getActivity().findViewById(R.id.stream_status_label_textview);
         statusLabel.setText(message);
         statusContainer.setVisibility(View.VISIBLE);
 
         // Construct translation animations from xml.
         final Animation animIn = AnimationUtils.loadAnimation(getActivity(), R.anim.translate_up);
         final Animation animOut = AnimationUtils.loadAnimation(getActivity(), R.anim.translate_down);
         statusContainer.startAnimation(animIn);
 
         if (!isIndefinite) {
 
             // When fade in is completed, trigger a fade out animation.
             animIn.setAnimationListener(new Animation.AnimationListener() {
                 @Override
                 public void onAnimationStart(Animation animation) {
                 }
 
                 @Override
                 public void onAnimationEnd(Animation animation) {
                     animOut.setStartOffset(1200);
                     statusContainer.startAnimation(animOut);
                 }
 
                 @Override
                 public void onAnimationRepeat(Animation animation) {
                 }
             });
 
             // When the animation has completely faded out, hide its parent container
             animOut.setAnimationListener(new Animation.AnimationListener() {
                 @Override
                 public void onAnimationStart(Animation animation) {
                 }
 
                 @Override
                 public void onAnimationEnd(Animation animation) {
                     statusContainer.setVisibility(View.INVISIBLE);
                 }
 
                 @Override
                 public void onAnimationRepeat(Animation animation) {
                 }
             });
         }
     }
 
     /**
      * Manually hides the stream status bar from view.
      */
     public void hideStatusBar() {
         Log.v(TAG, "Manually hiding status bar.");
         final LinearLayout statusContainer = (LinearLayout)getActivity().findViewById(R.id.stream_status_container_linearlayout);
         final Animation animOut = AnimationUtils.loadAnimation(getActivity(), R.anim.translate_down);
         animOut.setAnimationListener(new Animation.AnimationListener() {
             @Override
             public void onAnimationStart(Animation animation) {
             }
 
             @Override
             public void onAnimationEnd(Animation animation) {
                 // Hide the container from view when it has finished animating
                 statusContainer.setVisibility(View.INVISIBLE);
             }
 
             @Override
             public void onAnimationRepeat(Animation animation) {
             }
         });
         statusContainer.startAnimation(animOut);
 
     }
 
     /**
      * Restores favorites state when fragment is resumed. If a track is playing and was favorited by the user before
      * leaving the fragment, we need to restore this state to the UI to avoid misbehavior.
      */
     private void restoreFavoriteState() {
         Log.v(TAG, "Trying to restore favorites state...");
 
         // Check if the current track was is in the favorites list
         Track currentTrack = getCurrentTrack();
         Log.v(TAG, "Checking if this track is the same as the last favorited track...");
         boolean isFavorite = favTrackManager.compareToLastFavoritedTrack(currentTrack);
 
         ImageView favoriteButton = (ImageView)getActivity().findViewById(R.id.stream_favorite_imageview);
         Log.v(TAG, "Trying to restore favorites state...");
         if (isFavorite) {
             Log.v(TAG, "Current track is identical to the last favorited track! Changing boolean and updating state.");
             trackIsFavorite = true;
             favoriteButton.setImageResource(R.drawable.star_filled_white);
         } else {
             Log.v(TAG, "Current track is NOT identical to the last favorited track! Refreshing UI.");
             trackIsFavorite = false;
             favoriteButton.setImageResource(R.drawable.star_unfilled_white);
         }
     }
 
 }
