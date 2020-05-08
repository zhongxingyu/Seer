 package com.google.corrigan.owen.wordformed;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.util.Log;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.EditText;
 import android.media.SoundPool;
 import android.os.CountDownTimer;
 
 
 public class SinglePlayerGameView extends SurfaceView implements SurfaceHolder.Callback
 {
	private Context context;
 	private HiScoreDataSource datasource;
 	
 	public void draw(Canvas canvas)
 	{
 		Paint paint = new Paint();
 		paint.setColor(Color.rgb(42, 63, 82));
 		canvas.drawRect(holdBox, paint);
 		paint.setColor(Color.rgb(42, 73, 82));
 		canvas.drawRect(scoreBox, paint);
 	}
 	
 	
 	private final String TAG = "SINGLEPLAYERGAMEVIEW";
 	
 	final EditText input;
 	
 	private static List<Word> wordsByScore;
 	private SoundPool sound;
 	private int soundID;
 	private boolean loaded = false;
 	public boolean finished = false;
 	private RectF holdBox;
 	private RectF scoreBox;
 	private CreateBox create = new CreateBox(10, 200, 460, 70);
 	private Dropbox drop = new Dropbox(10, 350, 460, 70);
 	private Dropbox answer = new Dropbox(10, 500, 460, 70);
 	private Display display;
 	private Button submit = new Button(answer, 150, 650, 200, 100);
 	static private int score = 0;
 	SurfaceView sv = this;
 	
 	static int getScore()
 	{
 		return score;
 	}
 	
 	static List<Word> getWords()
 	{
 		return wordsByScore;
 	}
 	
 	//private Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
 	private String timeLeft = "3:00";
 	
 	private CountDownTimer anim = new CountDownTimer(180000, 10)
 	{
 		public void onTick(long millisUntilFinished)
 		{
 			for(DraggableBox d: db)
 				d.anim();
 		}
 
 		@Override
 		public void onFinish()
 		{
 			
 		}
 	};
 	
 	private CountDownTimer clock = new CountDownTimer(10000, 1000)
 	{
 		public void onTick(long millisUntilFinished)
 		{
 			timeLeft = "Time: "+ String.format("%d:%02d", 
 						TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
 						TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - 
 						TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
 		}
 		
 		public void onFinish()
 		{
 			
 			DraggableBox.playSound(DraggableBox.finishID);
 			wordsByScore = new ArrayList<Word>(SinglePlayerGame.wordList);
 			Collections.sort(wordsByScore, new Comparator<Word>() {
 				public int compare(Word o1, Word o2)
 				{
 					return o2.score - o1.score;
 				}
 			});
 			for(Word p: wordsByScore)
 			{
 				Log.d("Words Used", p.word + ": "+p.score);
 			}
 			if(score > 0)
 			{
 				new AlertDialog.Builder(context)
 			      .setMessage("Congratulations, you acheived a score of " + score
 			    		  + "\n Please enter your tag:")
 			      .setTitle("Game Over")
 			      .setView(input)
 			      .setCancelable(false)
 			      .setNeutralButton("Ok",
 			         new DialogInterface.OnClickListener() {
 			         public void onClick(DialogInterface dialog, int whichButton){
 			        	 //Close this window
 			        	 String name = input.getText().toString();
 			        	 if(name.length() > 0)
 			        		 datasource.createHiScore(name, score);
 			        	 Log.d("HISCORES", "In SinglePlayerGame, score = " + score);
 			        	 datasource.close();
 			        	 sv.setVisibility(INVISIBLE);
			        	 context.startActivity(new Intent(context, GameOverActivity.class));

 			         }
 			         })
 			      .show();
 			}
 			else
 			{
 				new AlertDialog.Builder(context)
			      .setMessage("You didn't make a single word. Your failures offend me.")
 			      .setTitle("Game Over")
 			      .setCancelable(false)
 			      .setNeutralButton("Ok",
 			         new DialogInterface.OnClickListener() {
 			         public void onClick(DialogInterface dialog, int whichButton){
 			        	 sv.setVisibility(INVISIBLE);
 			         }
 			         }) 
 			      .show();
 			}
 			timeLeft = "Done!";
 			finished = true;
 		}
 	};
 	
 	LinkedList<DraggableBox> db = new LinkedList<DraggableBox>();
 	
 	private GameThread thread;
 	
 	public SinglePlayerGameView(Context context)
 	{
 		super(context);
 		display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
 		scoreBox = new RectF(30, 580, display.getWidth() - 30, 640);
 		holdBox = new RectF(0, 0, display.getWidth(), 180);
 		Log.d(TAG, "Width = " + display.getWidth() + ", Height = " + display.getHeight());
 		thread = new GameThread(getHolder(), this);
 		input = new EditText(this.getContext());
 		datasource = new HiScoreDataSource(this.getContext());
 		datasource.open();
		this.context = context;
 		clock.start();
 		anim.start();
 		setFocusable(true);
 		getHolder().addCallback(this);
 		create.setContext(context);
 		create.setRef(db);
 		
 		score = 0;
			
 		for(int i = 0; i < 7; i++)
 		{
 			DraggableBox d = new DraggableBox(context, i * 65 + 20, 60);
 			db.add(d);
 			create.add(d);
 		}
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas)
 	{
 		
 		//Draw the background...
 		Paint background = new Paint();
 		background.setColor(getResources().getColor(
 				R.color.background));
 		canvas.drawRect(0, 0, getWidth(), getHeight(), background);
 		
 		clock.start();
 		//Draw background for dragboxes
 		drop.draw(canvas);
 		answer.draw(canvas);
 		create.draw(canvas);
 		this.draw(canvas); //Draw Top Rectangle
 		
 		submit.draw(canvas);
 		
 		for(DraggableBox d: db)
 		{
 			d.draw(canvas);
 		}
 	}
 
 	@Override
 	public synchronized boolean onTouchEvent(MotionEvent event)
 	{
 		if(event.getAction() == MotionEvent.ACTION_DOWN)
 		{
 			
 			if(loaded)
 			{
 				sound.play(soundID, 1, 1, 1, 0, 1f);
 				Log.d(TAG, "Played Sound");
 			}
 		}
 		int selected = 0;
 		for(DraggableBox d: db)
 		{
 			if(d.isSelected()) selected = db.indexOf(d);
 		}
 		Collections.swap(db, db.size() - 1, selected);
 		
 		score += submit.onTouchEvent(event);
 		
 		try
 		{
 			for(DraggableBox d: db)
 			{
 				d.onTouchEvent(event, drop, answer, create);
 			}
 			invalidate();
 		}
 		catch(Exception e)
 		{
 		//TODO	
 		}
 		
 		return true;
 	}
 
 	
 	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
 	{
 	}
 
 	
 	public void surfaceCreated(SurfaceHolder arg0)
 	{
 		// TODO Auto-generated method stub
 		thread.setRunning(true);
 		thread.start();
 	}
 
 	public void surfaceDestroyed(SurfaceHolder arg0)
 	{
 		boolean retry = true;
 		while(retry)
 		{
 			try {
 				thread.setRunning(false);
 				((Activity)getContext()).finish();
 				thread.join();
 				retry = false;
 			}
 			catch (InterruptedException e){
 				thread.stop();
 			}
 		}		
 	}
 
 	public void update()
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	public synchronized void render(Canvas canvas)
 	{	
 		Paint background = new Paint();
 		try{
 		//Draw the background...
 		background.setColor(getResources().getColor(
 				R.color.background));
 		canvas.drawRect(0, 0, getWidth(), getHeight(), background);
 		}
 		catch(NullPointerException e)
 		{
 			e.printStackTrace();
 		}
 		//Draw background for dragboxes
 		
 		background.setColor(Color.WHITE);
 		drop.draw(canvas);
 		answer.draw(canvas);
 		create.draw(canvas);
 		this.draw(canvas);
 		background.setTextSize(45);
 		
 		canvas.drawText(timeLeft, 130, 80, background);
 		canvas.drawText("Score: " + score, 150, 150, background);
 		
 		submit.draw(canvas);
 		synchronized (db) {
 			for(DraggableBox d: db)
 			{
 				d.draw(canvas);
 			}
 		}
 	}
 }
 
