 package edu.mines.csci498.snake;
 
 import android.app.Activity;
import android.graphics.Color;
 import android.os.Bundle;
 import android.widget.TextView;
 
 public class Snake extends Activity {
 	
 
 
 	@Override
 	public void onCreate(Bundle savedInstance) {
 		super.onCreate(savedInstance);
 		setContentView(R.layout.main);
 		
 		GameView game = (GameView) findViewById(R.id.snake);
 		TextView status = (TextView) findViewById(R.id.status);
 		game.setStatusTextView(status);
 		game.setOnTouchListener(game);
		game.setBackgroundColor(Color.BLACK);
 		game.setMode(GameView.MENU);
 		
 	}
 }
