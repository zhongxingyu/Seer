 package com.thu9group.snake;
 
 
 import java.io.FileNotFoundException;
 
 import com.thu9group.snake.R;
 
 
 import android.net.NetworkInfo.State;
 import android.os.*;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.graphics.drawable.Drawable;
 import android.support.v4.view.GestureDetectorCompat;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class Grid extends Activity implements Runnable {
 
 	
 	public final static String HIGH_SCORE = "com.thu9group.snake.SCORE";	
 	private GridView gridView;
 	private GameState gameState;
 	private GestureDetectorCompat mDetector;
 	public ScoreView score;
 	public Button pause;
 	private Button menu;
 	private Thread gameThread;
     private long lastUpdate;
     private GameOverHandler mHandler;
     private DifficultyView difficultyInfo;
     private boolean destroyed = false;
 
     
     @Override
     public void onDestroy()
     {
         destroyed = true;
         super.onDestroy();
       //  Toast.makeText(getApplicationContext(),"16. onDestroy()", Toast.LENGTH_SHORT).show();
     }
     
     
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		
 		
 		gameState = new GameState(this);	
 		gridView = new GridView(this);
 		mHandler = new GameOverHandler(Looper.getMainLooper(), this);
 		
 		
 		
 		LinearLayout gameLayout = new LinearLayout(this);
 		gameLayout.setOrientation(LinearLayout.VERTICAL);
 		
 		LinearLayout topLayout = new LinearLayout(this);
 		topLayout.setOrientation(LinearLayout.HORIZONTAL);
 		
 		LayoutParams glp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
 		LayoutParams tlp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		
 		
 
 		setContentView(gameLayout, glp);
 
 		pause = new Button(this);
 		menu = new Button(this);
 		Typeface typeHead = Typeface.createFromAsset(getAssets(),"fonts/woodbadge.ttf"); 
 		menu.setTypeface(typeHead);
 		pause.setTypeface(typeHead);
 		menu.setTextColor(Color.parseColor("#663300"));
 		pause.setTextColor(Color.parseColor("#663300"));
 		
 		menu.setText("MENU");
 		pause.setText("PAUSE");
 		topLayout.addView(pause, tlp);
 		topLayout.addView(menu, tlp);
 		menu.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent newIntent = new Intent(Grid.this,GameOver.class);
 				startActivity(newIntent);
 				
 			}
 		});
 		pause.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
                 if (gameState.state == gameState.RUNNING) {
                 	gameState.state = gameState.PAUSED;
                 	pause.setText("RESUME");
                 } else {
                 	gameState.state = gameState.RUNNING;
                 	pause.setText("PAUSE");
                 }
             }
         });
 		
 		score = new ScoreView(this);
 		Typeface typeText = Typeface.createFromAsset(getAssets(),"fonts/acmesab.ttf"); 
 		score.setTypeface(typeText);
 		
 		score.setText("SCORE: ");
 		
 		
 		score.setLayoutParams(tlp);
 		topLayout.addView(score);
 		
 	
 		difficultyInfo = new DifficultyView(this);
 		difficultyInfo.setTypeface(typeText);
 		difficultyInfo.setText("    DIFFICULTY: ");
 		
 		if(getBaseContext().getFileStreamPath("level.txt").exists()){
 			try {
 				difficultyInfo.setDifficultyViewInfo(openFileInput("level.txt"));
 				difficultyInfo.setLayoutParams(tlp);
 				topLayout.addView(difficultyInfo);
 				
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		
 		gameLayout.addView(topLayout, tlp);
 		gameLayout.addView(gridView);
 		
 		
 		((GridView)gridView).gameState = gameState;
 		mDetector = new GestureDetectorCompat(this, new MyGestureListener());
 		gameThread = new Thread(this);
 		gameThread.start();
 		
 
 		
 	}
 	
 	//this is the main game loop
 	public void run() {
 		while(gameState.isGameOver() == false) {
 			long now = System.currentTimeMillis();
 			if(now - lastUpdate > gameState.delay) {
 				lastUpdate = now;
 				gameState.cycle();
 				score.setscore(gameState.score);
 				score.postInvalidate();
 			    gridView.postInvalidate();  // Force a re-draw
 			} 
 		}
 		
 		//game over
 		mHandler.sendMessage(mHandler.obtainMessage());
 	}
 
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.grid, menu);
 		return true;
 	}
 	
 	@Override 
     public boolean onTouchEvent(MotionEvent event){ 
         this.mDetector.onTouchEvent(event);
         return super.onTouchEvent(event);
     }
 
 	private class GameOverHandler extends Handler {
 		private Activity activity;
 		
 		public GameOverHandler(Looper looper, Activity activity) {
 			super(looper);
 			this.activity = activity;
 		}
 
         @Override
         public void handleMessage(Message inputMessage) {
             // Gets the message from the Game Thread
         	// In this case, the message will always be a game over message.
         	// That means we must close the activity and start the GameOver activity
         	if (destroyed == false) {
         		Intent intent = new Intent(activity, GameOver.class);
 				activity.startActivity(intent);
         	}
         }		
 	}
 	
 	class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
 		private static final String DEBUG_TAG = "Gestures";
 		
 		
 		@Override
         public boolean onDown(MotionEvent event) { 
             
             return true;
         }
 
         @Override
         public boolean onFling(MotionEvent event1, MotionEvent event2, 
                 float velocityX, float velocityY) {
         	String direction;
         	int d;
         	
         	float deltax = event1.getX() - event2.getX();
         	float deltay = event1.getY() - event2.getY();
         	if(Math.abs(deltax) > Math.abs(deltay)) {
         		if(deltax > 0) {
         			direction = "LEFT";
         			d = GameState.LEFT;
         		} else {
         			direction = "RIGHT";
         			d = GameState.RIGHT;
         		}
         	} else {
         		if(deltay > 0) {
         			direction = "UP";
         			d = GameState.UP;
         		} else {
         			direction = "DOWN";
         			d = GameState.DOWN;
         		}
         	}
         	
         	gameState.updateDirection(d);
             return true;
         }
 	}
 }
