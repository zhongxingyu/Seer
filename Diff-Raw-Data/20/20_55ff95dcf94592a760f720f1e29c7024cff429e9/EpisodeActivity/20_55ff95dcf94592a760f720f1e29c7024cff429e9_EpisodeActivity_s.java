 package com.podcast.securitynow;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import sncatalog.shared.MobileEpisode;
 
 import com.podcast.securitynow.R;
 
 import android.app.Activity;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.text.Html;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 
 public final class EpisodeActivity  extends Activity{
 	
 	static final String classIdentifier = "EpisodeActivity";
 	static final String playButtonPause = "Pause";
 	static final String playButtonPlay = "Play";
 	
 	MediaPlayer mMp = new MediaPlayer();
 	EpisodeFetcher mFetcher;
 	Episode mEpisode = null;
 	
 	private Button mPlayButton = null;
 	private TextView mTitle = null;
 	private TextView mDescription = null;
 	
 	private TextView mButton1 = null;
 	private TextView mButton2 = null;
 	private TextView mButton3 = null;
 	private TextView mButton4 = null;
 	
 	private boolean isPlaying;
 	private StreamingMediaPlayer audioStreamer;
 	private TextView textStreamed;
 	private Button streamButton;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState)
     {
        Log.v(classIdentifier, "Activity State: onCreate()");
         super.onCreate(savedInstanceState);
         setContentView(R.layout.episode);
         // this.getExternalFilesDir(null)
         
         mTitle = (TextView) findViewById(R.id.title);
         mDescription = (TextView) findViewById(R.id.textarea);
         mPlayButton = (Button) findViewById(R.id.play);
         
         mButton1 = (TextView) findViewById(R.id.button1);
         mButton2 = (TextView) findViewById(R.id.button2);
         mButton3 = (TextView) findViewById(R.id.button3);
         mButton4 = (TextView) findViewById(R.id.button4);
         
         Bundle bun = getIntent().getExtras();
         mTitle.setText(bun.getString("title"));
        mDescription.setText(Html.fromHtml(bun.getString("description")) );
         
         final Button button = (Button) findViewById(R.id.play);
         button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	if (audioStreamer.getMediaPlayer().isPlaying()) {
             		audioStreamer.getMediaPlayer().pause();
             		mPlayButton.setText(playButtonPlay);
             	} else {
             		audioStreamer.getMediaPlayer().start();
             		audioStreamer.startPlayProgressUpdater();
             		mPlayButton.setText(playButtonPause);
             	}
             	isPlaying = !isPlaying;
             }
         });
     }
 	
	public void onPostCreate(Bundle savedInstanceState) {
 		File folder = new File(Environment.getExternalStorageDirectory(), "/sn");
         mFetcher = new EpisodeFetcher(folder);
 		
 		Bundle bun = getIntent().getExtras();
 		mEpisode = new Episode(mFetcher.getEpisode(bun.getInt("episode")));
 		startStreamingAudio(mEpisode.getLink());
 	}
 	
 	public void button1ClickHandler(View v) {
 		mDescription.setText(mEpisode.getDescription());
 	}
 	
 	public void button2ClickHandler(View v) {
 		mDescription.setText(mEpisode.getTransscript());
 	}
 	
 	public void button3ClickHandler(View v) {
 		TextView tv = (TextView) v;
 		tv.setText("Test");
 	}
 	
 	public void button4ClickHandler(View v) {
 		TextView tv = (TextView) v;
 		tv.setText("Test");
 	}
 
 	private void startStreamingAudio(String url) {
     	try { 
     		final ProgressBar progressBar = (ProgressBar) findViewById(R.id.SeekBar01);
     		if ( audioStreamer != null) {
     			audioStreamer.interrupt();
     		}
     		audioStreamer = new StreamingMediaPlayer(this, mPlayButton, streamButton,progressBar);
     		audioStreamer.startStreaming(url);
     		//audioStreamer.startStreaming("http://www.pocketjourney.com/downloads/pj/tutorials/audio.mp3",1677, 214);
     		//streamButton.setEnabled(false);
     	} catch (IOException e) {
 	    	Log.e(getClass().getName(), "Error starting to stream audio.", e);            		
     	}
     	    	
     }
 }
