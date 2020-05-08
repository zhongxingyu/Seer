 package uw.cse403.nonogramfun;
 
 /**
  * CSE 403 AA
  * Project Nonogram: Frontend
  * @author  Xiaoxia Jian, Huiqi Wang, Renhao Xie, Alan Loh
  * @version v1.0, University of Washington 
  * @since   Spring 2013 
  */
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 
 public class MainActivity extends Activity {
 
     public Button b;
     private MediaPlayer mPlayer;
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
        startMusicPlayer();
     }
 
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 	
	private void startMusicPlayer(){
		mPlayer = MediaPlayer.create(this, R.raw.humoresque);
        mPlayer.setLooping(true);
    	mPlayer.setVolume(100,100);
        mPlayer.start();
	}
 	
 	public void howToPlayScreen(View view) {
 		Intent i = new Intent(this, HowToPlay.class);
 		startActivity(i);
 	}
 	
 	
 	public void createGameScreen(View view) {
 		Intent i = new Intent(this, CreateGameMenu.class);
 		startActivity(i);
 	}
     
 	
 	public void playGameScreen(View view) {
 		Intent i = new Intent(this, PlayGameMenu.class);
 		startActivity(i);
 	}
 	
 	public void scoreBoardScreen(View view) {
 		Intent i = new Intent(this, ScoreBoard.class);
 		startActivity(i);
 	}
 	
 	@Override
 	public void onDestroy(){
 		super.onDestroy();
		mPlayer.stop();
 		mPlayer.release();
		mPlayer = null;
 	}
 }
 
 
