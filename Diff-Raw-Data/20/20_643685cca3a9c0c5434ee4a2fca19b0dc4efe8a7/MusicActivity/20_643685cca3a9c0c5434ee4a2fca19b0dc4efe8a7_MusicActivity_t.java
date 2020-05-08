 package org.robobrain.test;
 
 import org.robobrain.test.R;
 import org.robobrain.sdk.GameActivity;
 import org.robobrain.sdk.audio.MusicManager;
 
 import android.os.Bundle;
 import android.view.View;
 
 public class MusicActivity extends GameActivity {
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.music);
         
         MusicManager.loadMusic("music/xltt.mp3");
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		MusicManager.stop();
 	}
 	
 	public void onPlayBtnClick(View view) {
		if (MusicManager.isStoped()){
			MusicManager.release();
			MusicManager.loadMusic("music/xltt.mp3");
		}
			
 		MusicManager.play();
 	}
 	
 	public void onPauseBtnClick(View view) {
 		MusicManager.pause();
 	}
 	
 	public void onStopBtnClick(View view) {
 		MusicManager.stop();
 	}
 }
