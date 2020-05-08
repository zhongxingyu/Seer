 package labs.brian.brianstetris;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.TranslateAnimation;
 import android.widget.TextView;
 
 public class HomeScreen extends Activity implements OnClickListener {
 
 	private static final int HomeScreen = 1;
 	private TextView newgameTextview,highscoreTextview,settingsTextview,aboutTextview;
 	private TetrisMusic music;
 	private TranslateAnimation tranAnimation0,tranAnimation1,tranAnimation2,tranAnimation3;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_screen);
 		
 		newgameTextview = (TextView)findViewById(R.id.newgame);
 		highscoreTextview = (TextView)findViewById(R.id.highscores);
 		settingsTextview = (TextView)findViewById(R.id.settings);
 		aboutTextview = (TextView)findViewById(R.id.about);
 		
 		music = TetrisMusic.getInstance(this);
 		
 		tranAnimation0 = new TranslateAnimation(-400,0,0,0);
 		tranAnimation0.setDuration(1000);
 		tranAnimation0.setStartOffset(0);
 		
 		tranAnimation1 = new TranslateAnimation(-400,0,0,0);
 		tranAnimation1.setDuration(1000);
 		tranAnimation1.setStartOffset(500);
 		
 		tranAnimation2 = new TranslateAnimation(-400,0,0,0);
 		tranAnimation2.setDuration(1000);
 		tranAnimation2.setStartOffset(1000);
 		
 		tranAnimation3 = new TranslateAnimation(-400,0,0,0);
 		tranAnimation3.setDuration(1000);
 		tranAnimation3.setStartOffset(1500);
 	}
 	
 	@Override
 	public void onResume(){
 		super.onResume();
 		
 		newgameTextview.startAnimation(tranAnimation0);
 		highscoreTextview.startAnimation(tranAnimation1);
 		settingsTextview.startAnimation(tranAnimation2);
 		aboutTextview.startAnimation(tranAnimation3);
 		
 		music.playMusic();
 	}
 	
 	@Override
 	public void onPause(){
 		super.onPause();
 		
 		music.pauseMusic();
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()){
 			case R.id.newgame:
 				startActivityForResult(new Intent(this,MainActivity.class),HomeScreen);
 				break;
 			case R.id.highscores:
				startActivity(new Intent(this,HighScoresActivity.class));
 				break;
 			case R.id.settings:
 				
 				break;
 			case R.id.about:
 				
 				break;
 		}
 	}
 }
