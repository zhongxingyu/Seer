 package com.teamluper.luper;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
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
 import android.view.View.OnClickListener;
 import android.widget.AutoCompleteTextView;
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
 import com.googlecode.androidannotations.annotations.Background;
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.UiThread;
 import com.teamluper.luper.TrackView.RecordButton;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 
 @EActivity
 public class LuperProjectEditorActivity extends SherlockActivity {
   SQLiteDataSource dataSource;
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
   private LinearLayout base;
 
   // TODO these will be moved to within Sequence, and accessed with
   // sequence.getClips() and sequence.getTracks(), etc.
   // ArrayList<Clip> clips = new ArrayList<Clip>();
   // ArrayList<Track> tracks = new ArrayList<Track>();
 
   @Override
   public void onCreate(Bundle icicle) {
     super.onCreate(icicle);
     vert = new ScrollView(this);
     horz = new HorizontalScrollView(this);
     base = new LinearLayout(this);
     base.setId(1337);
     base.setBackgroundColor(Color.parseColor("#e2dfd8"));
 
     base.setOrientation(LinearLayout.VERTICAL);
     setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 
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
 
     dataSource = new SQLiteDataSource(this);
     dataSource.open();
 
     sequence = dataSource.getSequenceById(ID);
     // TODO use the data within the sequence object to render the clips.
     DialogFactory.alert(this, "Successfully Loaded Project ID #"+ID+
         "!  The title is '"+sequence.getTitle()+"'");
 
     // TODO: we only want to load stuff if we don't already have it loaded...
     // onCreate gets called a bunch of times, so be careful only to load stuff once per open project
     loadDataInForeground();
 
     final ActionBar bar = getSupportActionBar();
     bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS); // Gives us Tabs!
 
     horz.addView(base);
     vert.addView(horz);
 
     render();
   }
 
   //playhead class view to be implemented --Eric
   public class Playhead extends View{
     private final float STARTY = 0;
     private final float ENDY = 5000;
     private float X;
 
     public Playhead(Context context){
       super(context);
       init();
     }
     public void init(){
 //      this.setX(this.getStartTime());
 //      this.setY(5000);
 //      this.setWidth(this.getStartTime() + this.getLength());
     }
 
 //    public boolean onTouchEvent(MotionEvent event) {
 //      int action = event.getAction();
 //      switch (action) {
 //        case MotionEvent.ACTION_DOWN:
 //          x = event.getX();
 //          break;
 //        case MotionEvent.ACTION_MOVE:
 //        case MotionEvent.ACTION_UP:
 //        case MotionEvent.ACTION_CANCEL:
 //          x = initialX + event.getX() - offsetX;
 //          y = initialY + event.getY() - offsetY;
 //          break;
 //      }
 //      return (true);
 //    }
 
 //    public void draw(Canvas canvas) {
 //      int width = canvas.getWidth();
 //      int height = canvas.getHeight();
 //      drawLine(X, STARTY, X, float ENDY);
    //   invalidate();
   //  }
   }
 
   @UiThread
   public void render() {
 //    LinearLayout base = new LinearLayout(this);
 //    base.setId(1337);
 //    base.setBackgroundColor(Color.parseColor("#e2dfd8"));
 //
 //    base.setOrientation(LinearLayout.VERTICAL);
 
     int tracksTraversed = 0;
     int clipsTraversed = 0;
     // RENDERING ROUTINE STARTS HERE
     DialogFactory.alert(this, "RENDER","Render is Happening, and sequence.isReady = "+sequence.isReady());
 //    if(sequence.isReady()) {
 //      // draw stuff in it
       for(Track track : sequence.tracks) {
         RelativeLayout tracklayout = new RelativeLayout(this);
         TrackView tv = new TrackView(this, track, dataSource);
         tracklayout.addView(tv);
         base.addView(tracklayout,
             new RelativeLayout.LayoutParams(
                 ViewGroup.LayoutParams.WRAP_CONTENT,
                 ViewGroup.LayoutParams.WRAP_CONTENT));
 
         tracksTraversed++;

         for(Clip clip : track.clips) {
           // render the clip
           clipsTraversed++;
         }
       }
       DialogFactory.alert(this,"Done with render sequence","Traversed "+tracksTraversed+" tracks and "+clipsTraversed+" clips.");
     //}
 
     setContentView(vert);
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
   }
 
   // #Creates the Actionbar
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     MenuInflater inf = getSupportMenuInflater();
     inf.inflate(R.menu.editor_bar, menu);
     return super.onCreateOptionsMenu(menu);
   }
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     boolean incomplete = false;
     if(item.getItemId() == R.id.editor_play) {
       // TODO
       //incomplete = true;
 
     	int i=0;
     	while(playBackTest!=null && i!=playBackTest.size())
     	{
 	        mPlayer = new MediaPlayer();
 	        mFileName=playBackTest.clips.get(i).name;
 	        try
 	        {
 	            mPlayer.setDataSource(mFileName);
 	            mPlayer.prepare();
 	            Thread.sleep(playBackTest.clips.get(i).getDuration());
 	            mPlayer.start();
 	            i++;
 	        } catch (Exception e) {
 	        	//handle interrupted exceptions in a different way
 	            Log.e(LOG_TAG, "prepare() failed1");
 	        }
     	}
     }
     if(item.getItemId() == R.id.editor_add_track) {
     	Track addTrack = dataSource.createTrack(sequence);
     	TrackView addTrackView = new TrackView(this, addTrack, dataSource);
     	base.addView(addTrackView);
     }
     if(item.getItemId() == R.id.editor_add_clip) {
       // TODO
       //incomplete = true;
     	LinearLayout custom = new LinearLayout(this);
 		custom.setOrientation(LinearLayout.VERTICAL);
 
     	LinearLayout ll = new LinearLayout(this);
     	mRecordButton = new RecordButton(this);
         ll.addView(mRecordButton,
                 new LinearLayout.LayoutParams(
                     ViewGroup.LayoutParams.WRAP_CONTENT,
                     ViewGroup.LayoutParams.WRAP_CONTENT,
                     0));
 
 
         LinearLayout ll2 = new LinearLayout(this);
         fileSelected = new AutoCompleteTextView(this);
         fileSelected.setHint("Select a File");
         ll2.addView(fileSelected,
                 new LinearLayout.LayoutParams(
                         ViewGroup.LayoutParams.FILL_PARENT,
                         ViewGroup.LayoutParams.FILL_PARENT,
                         0));
 
         custom.addView(ll,
                 new LinearLayout.LayoutParams(
                         ViewGroup.LayoutParams.WRAP_CONTENT,
                         ViewGroup.LayoutParams.WRAP_CONTENT,
                         0));
         custom.addView(ll2,
                 new LinearLayout.LayoutParams(
                         ViewGroup.LayoutParams.MATCH_PARENT,
                         ViewGroup.LayoutParams.WRAP_CONTENT,
                         0));
 
         new AlertDialog.Builder(this)
 		.setTitle("Record or Browse?")
 		.setView(custom)
 	    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 	        public void onClick(DialogInterface dialog, int whichButton) {
 	        	//want it to pass a new clip back to the editor panel and add it to the screen
 	        }
 	    })
 	    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 	        public void onClick(DialogInterface dialog, int whichButton) {
 	            // Do nothing.
 	        }
 	    })
 		.show();
     }
     if(item.getItemId() == R.id.editor_delete_clip) {
       // TODO
       incomplete = true;
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
       incomplete = true;
     }
     if(incomplete) DialogFactory.alert(this,"Incomplete Feature",
         "That button hasn't been hooked up to anything.");
     return super.onOptionsItemSelected(item);
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
       //System.out.println("here");
       mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
       //System.out.println("and here");
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
 
       Clip newClip = new Clip(mFileName);
 
       try {
 			newClip.getDuration();
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
       //playBackTest.putClip(newClip);
       //alertDialog("Clip Created! The clip's length is: " + newClip.duration + "(ms). The tracks size is " + playBackTest.size() + " and it's name in the track is ..." + playBackTest.clips.get(0).name);
 
      // alertDialog("Clip Created! The clip's length is: " + newClip.duration + "(ms) the clip's name is: " + newClip.name);
 
       fileSelected.setText(mFileName);
       mRecorder = null;
   }
 
   @Background
   public void loadDataInBackground() {
     if(sequence == null || sequence.isReady()) return;
     sequence.loadAllTrackData();
     render();
   }
 
   @UiThread
   public void loadDataInForeground() {
     if(sequence == null || sequence.isReady()) return;
     sequence.loadAllTrackData();
   }
 
   @Background
   public void loadAudioInBackground() {
     if(sequence == null) return;
     sequence.loadAllTrackAudio();
   }
 
   @UiThread
   public void alert(String message) {
     DialogFactory.alert(this, message);
   }
 }
