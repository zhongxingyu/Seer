 package com.teamluper.luper;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.Color;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaRecorder;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.HorizontalScrollView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.androidlearner.widget.DragThingPlayhead;
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.UiThread;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.BitmapFactory;
 import android.graphics.Shader;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 @EActivity
 public class LuperProjectEditorActivity extends SherlockActivity {
   public SQLiteDataSource dataSource;
   Sequence sequence = null;
   private static String mFileName = null;
   private MediaRecorder mRecorder = null;
   private static final String LOG_TAG = "TrackView";
   private TextView fileSelected;
   private RecordButton mRecordButton = null;
   private MediaPlayer   mPlayer = null;
   private Track playBackTest = new Track ();
   private AudioManager audioManager;
   private ScrollView vert;
   private HorizontalScrollView horz;
   //private LinearLayout base1;
   private DragThingPlayhead base;
 
   public Clip veryLastClip = null;
 
   public boolean isPaused = false;
 
   //this object is gonna move de move.
   //ClipThing deClip;
   //int [] paramz;
 
   // TODO these will be moved to within Sequence, and accessed with
   // sequence.getClips() and sequence.getTracks(), etc.
   // ArrayList<Clip> clips = new ArrayList<Clip>();
   // ArrayList<Track> tracks = new ArrayList<Track>();
 
   @Override
   public void onCreate(Bundle icicle) {
     super.onCreate(icicle);
     vert = new ScrollView(this);
     horz = new HorizontalScrollView(this);
     base = new DragThingPlayhead(this);
 
 
     //LinearLayout top = new LinearLayout(this);
     TextView a = new TextView(this);
     a.setText("Timeline");
     a.setBackgroundColor(Color.parseColor("#f5f5f5"));
     base.addView(a);
     //this.addView(top);
 
     base.setId(1337);
 
     setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 
     // to figure out which sequence data to load, we fetch the Sequences _id from the Intent.
     // this is put into the intent when you press the project in the Projects list in LuperMainActivity.
     long ID = getIntent().getLongExtra("selectedProjectId", -1);
     if(ID == -1) {
       DialogFactory.alert(this,"ERROR","No project ID found!  Aborting.",
           new Lambda.VoidCallback() {
             public void go() {
               finish();
             }
           });
       return;
     }
 
     // set up a connection to the phone's SQLite database.
     dataSource = new SQLiteDataSource(this);
     dataSource.open();
 
     // fetch the sequence object we're editing from SQLite.
     sequence = dataSource.getSequenceById(ID);
 
     // fetch all the Track objects associated with this sequence from SQLite.
     sequence.tracks = dataSource.getTracksBySequenceId(sequence.getId());
 
     // for each track in the sequence, fetch all the associated Clip and AudioFile objects.
     for(Track track : sequence.tracks) {
       long trackId = track.getId();
       ArrayList<Clip> clips = dataSource.getClipsByTrackId(trackId);
       track.clips = clips;
       for(Clip clip : track.clips) {
         clip.audioFile = dataSource.getAudioFileById(clip.getAudioFileID());
         clip.parentTrack = track;
       }
      if(track.clips.size() > 0) track.nextClip = track.clips.get(0);
     }
     // now that all the Sequence, Track, Clip, and AudioFile objects are in memory, this sequence is ready for editing!
     sequence.setReady(true);
 
     final ActionBar bar = getSupportActionBar();
     bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS); // Gives us Tabs!
 
     //base.addView(theplayhead);
     LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
         ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
     base.setLayoutParams(params);
     Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.line);
     BitmapDrawable bitmapDrawable = new BitmapDrawable(bmp);
     bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
     base.setBackgroundDrawable(bitmapDrawable);
     base.setOrientation(LinearLayout.VERTICAL);
     base.setPadding(0, 0, 50, 0);
 
 //    ll2.addView(fileSelected,
 //        new LinearLayout.LayoutParams(
 //            ViewGroup.LayoutParams.FILL_PARENT,
 //            ViewGroup.LayoutParams.FILL_PARENT,
 //            0));
 
     horz.addView(base);
     vert.addView(horz);
     setContentView(vert);
 
     render();
   }
 
   @UiThread
   public void render() {
 //    LinearLayout base = new LinearLayout(this);
 //    base.setId(1337);
 //    base.setBackgroundColor(Color.parseColor("#e2dfd8"));
 //
 //    base.setOrientation(LinearLayout.VERTICAL);
 
     // RENDERING ROUTINE STARTS HERE
     if(sequence.isReady() && base != null) {
       if(base.getChildCount() != 0) base.removeAllViews();
       // draw stuff in it
       for(Track track : sequence.tracks) {
         RelativeLayout tracklayout = new RelativeLayout(this);
         TrackView tv = new TrackView(this, track, dataSource);
         track.addAssociatedView(tv);
         tracklayout.addView(tv);
         base.addView(tracklayout,
             new RelativeLayout.LayoutParams(
                 ViewGroup.LayoutParams.WRAP_CONTENT,
                 ViewGroup.LayoutParams.WRAP_CONTENT));
 
         for(Clip clip : track.clips) {
           // render the clip
           ColorChipButton chip = new ColorChipButton(this, clip);
           clip.addAssociatedView(chip);
         	tv.addView(chip);
         }
       }
     }
     base.invalidate();
     //setContentView(vert);
   }
 
   @Override
   protected void onStop() {
     if(dataSource.isOpen()) dataSource.close();
     super.onStop();
   }
 
   @Override
   protected void onResume() {
     if(!dataSource.isOpen()) dataSource.open();
     super.onResume();
     //playhead.setXPosition();
     //if(paramz != null) deClip.layout(paramz[0] , 0, paramz[2], 0);
   }
 
 
     /*@Override
     protected void onPause() {
         super.onPause();
         //gets the current layout
         paramz = deClip.getCurrent();
     }*/
 
   // #Creates the Actionbar
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     MenuInflater inf = getSupportMenuInflater();
     inf.inflate(R.menu.editor_bar, menu);
     return super.onCreateOptionsMenu(menu);
   }
 
   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
     MenuItem playButton = menu.findItem(R.id.editor_play);
     MenuItem pauseButton = menu.findItem(R.id.editor_pause);
     MenuItem stopButton = menu.findItem(R.id.editor_stop);
     if(base.isPlaying()) {
       playButton.setVisible(false);
       pauseButton.setVisible(true);
       stopButton.setVisible(true);
     } else {
       playButton.setVisible(true);
       pauseButton.setVisible(false);
       stopButton.setVisible(false);
     }
     return super.onPrepareOptionsMenu(menu);
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     boolean incomplete = false;
     if(item.getItemId() == R.id.editor_play) {
       if(!isPaused) {
         base.startPlayback(0);
       } else {
         base.startPlayback();
         resumeAllMediaPlayers();
       }
 
       /*
       for(Track track  : sequence.tracks) {
         for(View v : track.getAssociatedViews()) {
           if(TrackView.class.isInstance(v)) {
             ((TrackView) v).startPlayingTrack(); // doesn't actually run them in parallel - see trackview
           }
         }
       }
       */
     }
     if(item.getItemId() == R.id.editor_pause) {
       isPaused = true;
       base.stopPlayback();
       pauseAllMediaPlayers();
     }
     if(item.getItemId() == R.id.editor_stop) {
       isPaused = false;
       base.stopPlayback(0);
       stopAllMediaPlayers();
       base.postInvalidate();
     }
     if(item.getItemId() == R.id.editor_add_track) {
     	Track addTrack = dataSource.createTrack(sequence);
       sequence.tracks.add(addTrack);
     	TrackView addTrackView = new TrackView(this, addTrack, dataSource);
       addTrack.addAssociatedView(addTrackView);
     	base.addView(addTrackView);
     }
     if(item.getItemId() == R.id.editor_volume) {
       // TODO
       //incomplete = true;
       LinearLayout ll3 = new LinearLayout(this);
       SeekBar volBar = new SeekBar(this);
       ll3.addView(volBar,
           new LinearLayout.LayoutParams(
               ViewGroup.LayoutParams.MATCH_PARENT,
               ViewGroup.LayoutParams.WRAP_CONTENT,
               0));//what we need to get the volume bar to work
       audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
       int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
       int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
       volBar.setMax(maxVolume);
       volBar.setProgress(curVolume);
       volBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
         public void onStopTrackingTouch(SeekBar seekBar) {}
         public void onStartTrackingTouch(SeekBar seekBar) {}
         public void onProgressChanged(SeekBar seekBar, int progress,
                                       boolean fromUser) {
           audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
         }
       });
       new AlertDialog.Builder(this)
           .setView(ll3)
           .show();
     }
     if(item.getItemId() == R.id.editor_help) {
       // TODO
       //incomplete = true;
       Intent intent = new Intent(this, LuperHelp_.class);
       startActivity(intent);
     }
     if(item.getItemId() == R.id.invalidateTrackViews) {
       invalidateTrackViews();
     }
     if(item.getItemId() == R.id.invalidateClipViews) {
       invalidateClipViews();
     }
     if(item.getItemId() == R.id.callRender) {
       render();
     }
     if(incomplete) DialogFactory.alert(this,"Incomplete Feature",
         "That button hasn't been hooked up to anything.");
     return super.onOptionsItemSelected(item);
   }
 
   @UiThread
   public void invalidateTrackViews() {
     for(Track t : sequence.tracks) {
       t.invalidateAssociatedViews();
     }
   }
 
   @UiThread
   public void invalidateClipViews() {
     for(Track t : sequence.tracks) {
       for(Clip c: t.clips) {
         c.invalidateAssociatedViews();
       }
     }
   }
 
   public void playNextClipIfExistsAtTime(int timeMS) {
     for(Track t : sequence.tracks) {
       if(t.nextClip != null) {
         int nextStartTime = t.nextClip.getStartTime();
         if(nextStartTime < timeMS) {
           t.nextClip = null;
           // todo factor all the playback stuff into the Track instead of the TrackView.
           t.trackView.playPreparedClip();
         }
       }
     }
   }
 
   public boolean findVeryLastClip() {
     int latestFinishTime = 0;
     Clip lastClip = null;
     for(Track t : sequence.tracks) {
       for(Clip c : t.clips) {
         int finishTime = c.getStartTime() + c.getDurationMS();
         if(finishTime > latestFinishTime) {
           latestFinishTime = finishTime;
           lastClip = c;
         }
       }
     }
     if(lastClip != null) {
       this.veryLastClip = lastClip;
       return true;
     }
     return false;
   }
 
   public void stopAllMediaPlayers() {
     for(Track t : sequence.tracks) {
       t.trackView.stopPlaying();
     }
   }
 
   public void pauseAllMediaPlayers() {
     for(Track t : sequence.tracks) {
       MediaPlayer mp = t.trackView.getMediaPlayer();
       if(mp.isPlaying()) mp.pause();
     }
   }
 
   public void resumeAllMediaPlayers() {
     for(Track t : sequence.tracks) {
       MediaPlayer mp = t.trackView.getMediaPlayer();
      if(mp != null && !mp.isPlaying()) mp.start();
     }
   }
 
   class RecordButton extends Button {
     boolean mStartRecording = true;
 
     OnClickListener clicker = new OnClickListener() {
       public void onClick(View v) {
         onRecord(mStartRecording);
         if (mStartRecording) {
           setText("Stop recording");
         } else {
           setText("Start recording");
         }
         mStartRecording = !mStartRecording;
       }
     };
 
     public RecordButton(Context ctx) {
       super(ctx);
       setText("Start recording");
       setOnClickListener(clicker);
     }
   }
 
   private void onRecord(boolean start) {
 
     if (start) {
       startRecording();
     } else {
       stopRecording();
     }
   }
 
   private void startRecording() {
     //Sets the name of the file when you start recording as opposed to when you click "Audio Record Test" from the main screen
     mFileName = Environment.getExternalStorageDirectory()+"/LuperApp/Clips";
     mFileName += "/clip_" + System.currentTimeMillis() +".3gp";
 
     mRecorder = new MediaRecorder();
     mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
     mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
     mRecorder.setOutputFile(mFileName);
 
     mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
 
     try {
       mRecorder.prepare();
     } catch (IOException e) {
       System.out.println(e.toString());
       Log.e(LOG_TAG, "prepare() failed2");
     }
 
     mRecorder.start();
   }
 
   private void stopRecording() {
     mRecorder.stop();
     mRecorder.release();
     fileSelected.setText(mFileName);
     mRecorder = null;
   }
 
   @UiThread
   public void alert(String message) {
     DialogFactory.alert(this, message);
   }
 }
