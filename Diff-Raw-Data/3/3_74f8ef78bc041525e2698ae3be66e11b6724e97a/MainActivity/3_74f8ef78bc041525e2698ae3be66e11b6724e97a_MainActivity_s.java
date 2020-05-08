 package com.rarebird.photonpack;
 
 import java.io.IOException;
 
 import com.dancingsasquatch.photonsacks.R;
 
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.media.SoundPool;
 import android.media.SoundPool.OnLoadCompleteListener;
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.res.AssetFileDescriptor;
 import android.content.res.AssetManager;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.VideoView;
 
 public class MainActivity extends Activity implements OnPreparedListener{
 	VideoView vv;
 	ImageView ii;
 	Uri uri;
 	SoundPool soundPool;
 	int shooting_sound;
 	AssetFileDescriptor packshooting_descriptor;
 	boolean loaded = false;
 	int streamid;
 	
 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.pack_on);
 		vv = (VideoView) findViewById(R.id.videoView1);
 		ii = (ImageView) findViewById(R.id.PackOffOverlay);
 		vv.setVideoURI(uri);
 		ii.setVisibility(View.VISIBLE);
 		vv.seekTo(001);
 		setVolumeControlStream(AudioManager.STREAM_MUSIC);
 		soundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
         soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
             @Override
             public void onLoadComplete(SoundPool soundPool, int sampleId,
                     int status) {
                 loaded = true;
             }
         });
 	    vv.setOnPreparedListener (new OnPreparedListener() {                    
 	        @Override
 	        public void onPrepared(MediaPlayer mp) {
 	            mp.setLooping(true);
 	        }
 	    });
 	    
 	    try {
 	    	AssetManager assetManager = getAssets();
 	    	packshooting_descriptor = assetManager.openFd("spackshootmono.ogg");
 	    	shooting_sound = soundPool.load(packshooting_descriptor, 1);
 	    } catch (IOException e) {
 	    	Log.d("MEDIA ERROR", "Couldn't Open ogg");
 	    }    	
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event)
 	{
 		int maskedAction = event.getActionMasked();
 		switch (maskedAction) {
 		case MotionEvent.ACTION_DOWN:
 			vv.setVisibility(View.VISIBLE);
 			ii.setVisibility(View.GONE);
 			if(!vv.isPlaying()) {
 				vv.start();
 			}
             if (loaded) {
                 streamid = soundPool.play(shooting_sound, 1, 1, 1, -1, 1);
                Log.e("Test", "Played sound");
                loaded = false;
             }
 			break;
 		case MotionEvent.ACTION_UP:
 			ii.setVisibility(View.VISIBLE);
 			soundPool.stop(streamid);
 		    vv.pause();
 		    vv.seekTo(0);
 			break;
 		}
 		return true;
 	}
 	@Override 
 	protected void onResume(){
 		super.onResume();
 	    vv.requestFocus();
 	}
 	@Override
 	protected void onPause(){
 		super.onPause();
 		soundPool.release();
 	}
 	@Override
 	public void onPrepared(MediaPlayer mp) {
 		ii.setVisibility(View.GONE);
 		
 	}
 
 }
