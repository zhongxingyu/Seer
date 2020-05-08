 package org.MAG;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.View.OnTouchListener;
 import android.widget.VideoView;
 
 /**
  * 
  * @author UnderGear
  *
  */
 public class TravelScene extends Activity implements OnTouchListener, OnCompletionListener {
 
 	private String TAG = "TravelScene";
 	
 	private VideoView travel;
 	
 	/**
 	 * Called on Activity creation. set the background and video, media player and completion listener
 	 * 
 	 * @param savedInstanceState
 	 */
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		
         //TODO: redo the travel video and include audio in it as well.
 		
		setContentView(R.layout.travel);
		
         travel = (VideoView)findViewById(R.id.travel_background);
         
        Log.d(TAG, "travel null? " + (travel == null));
         travel.setOnTouchListener(this);
         Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.travel);
         
         travel.setOnCompletionListener(this);
         
         travel.setVideoURI(video);
         
         travel.start();
         
     }
 	
 	/**
 	 * We are exiting the app. release the media player and its assets.
 	 */
 	public void onDestroy() {
 		
 		travel.setOnCompletionListener(null);
 		travel.setOnTouchListener(null);
 		
 		super.onDestroy();
 	}
 	
 	
 	/**
 	 * Called when the screen is touched. Loads the next level.
 	 * 
 	 * @param v view that received the touch
 	 * @param event the touch event received by v
 	 */
 	public boolean onTouch(View v, MotionEvent event) {
 		loadNextLevel();
 		return true;
 	}
 	
 	/**
 	 * Release media player, assets, listeners, etc. Load up the Caster activity
 	 */
 	private void loadNextLevel() {
 		Log.e(TAG, "finished. Loading Caster");
 		try {
 			travel.stopPlayback();
         	Intent ourIntent = new Intent(TravelScene.this, Class.forName("org.MAG.Caster"));
         	travel.setOnTouchListener(null);
         	travel.setOnCompletionListener(null);
         	ourIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(ourIntent);
 			finish();
 		} catch (ClassNotFoundException ex) {
 			Log.e(TAG, "Failed to jump to another activity");
 		}
 	}
 
 	public void onCompletion(MediaPlayer mp) {
 		loadNextLevel();
 	}
 }
