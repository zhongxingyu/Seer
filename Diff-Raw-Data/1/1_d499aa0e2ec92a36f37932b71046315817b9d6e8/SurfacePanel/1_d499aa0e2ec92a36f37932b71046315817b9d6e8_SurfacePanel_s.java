 package com.nomath4u.breakbrick;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.media.SoundPool.OnLoadCompleteListener;
 import android.os.Handler;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 
 public class SurfacePanel extends SurfaceView implements SurfaceHolder.Callback {
 	public boolean _run;
 	private Bitmap mBitmap;
 	public boolean isDrawing = true;
 	protected DrawThread thread;
 	public Paddle mainPaddle;
 	public Ball mainBall;
 	public MainActivity parent;
 	public boolean over = false;
 	public List<Brick> bricks;
 	public boolean paused;
 	public static final int rows = 4;
 	public boolean playing = false;
 	public ExtraLife eLife = null;
     private LayoutInflater inflater;
     public View pauseView;
     private ViewGroup pauseViewGroup;
     private boolean added = false;
     public boolean tickable;
     public View scoreView;
 	
 	
 	
 	public SurfacePanel(Context context){
 		super(context);
 		getHolder().addCallback(this);
 
 		thread = new DrawThread(getHolder());
 		mainPaddle = new Paddle(context);
 		mainBall = new Ball(context);
         //this.getHolder().setFixedSize(mainPaddle.screenwidth,mainPaddle.screenheight);
 		parent = (MainActivity)context;
 		bricks = new ArrayList<Brick>();
         this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 
 		this.pauseViewGroup = (ViewGroup) findViewById(android.R.id.content);
         this.pauseView = inflater.inflate(R.layout.pause_screen,pauseViewGroup);
         this.scoreView = inflater.inflate(R.layout.game_overlay,pauseViewGroup);
         parent.addContentView(scoreView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
         //pauseView.setLayoutParams(new ViewGroup.LayoutParams(mainPaddle.screenwidth, mainPaddle.screenheight));
         tickable = false;
 
         /*Add the pause View on top of the surfaceview and make it invisible*/
         //parent.addContentView(pauseView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
 		//this.pauseView.setVisibility(pauseView.VISIBLE);
 	
 		
 		
 		/*Create the bricks*/
 		createBricks();
 
 
 		
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event){
 		/*We just care that there was one*/
 
         /*Depricated with pause menu*/
 		/*if(paused){
 			unpause();
 		}*/
 		/*else*/ if(playing){
 			pause();
 		}
 		if(!playing){
 			playing = true;
 		}
 			
 			
 		
 		return super.onTouchEvent(event);
 	}
 	
 	private void pause(){
 		paused = true;
 
         /*Show the pause view*/
         //this.pauseView.setVisibility(pauseView.VISIBLE);
 
         if(!added){
             parent.addContentView(pauseView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
             added = true;
         }
 
         else{
              pauseView.setVisibility(VISIBLE);
         }
 
 		
 	}
 	
 	public void unpause(){
 
         /*Loop through Views to find and remove the pauseview if this is faster
         for(int i = 0; i < this.pauseViewGroup.getChildCount(); i++){
             if(this.pauseViewGroup.getChildAt(i) == this.pauseView){
                 this.pauseViewGroup.removeViewAt(i);
                 break;
             }
         }*/
 
         //this.pauseView.setVisibility(pauseView.INVISIBLE);
         //((LinearLayout)pauseView.getParent()).removeView(this.pauseView);
 
 
         /*Start running the game again*/
         //parent.backToGame();
         pauseView.setVisibility(INVISIBLE);
         paused = false;
 	}
 	
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,  int height) {
         // Create a Bitmap with the dimensions of the View
         mBitmap =  Bitmap.createBitmap (width, height, Bitmap.Config.ARGB_8888);;
     }
  
     public void surfaceCreated(SurfaceHolder holder) {
         // Starts thread execution
         if(thread.getState() == Thread.State.NEW){
             thread.setRunning(true);
             thread.start();
         }
     }
  
     public void surfaceDestroyed(SurfaceHolder holder) {
         // Finish thread execution
         boolean retry = true;
         thread.setRunning(false);
         while (retry) {
             try {
                 thread.join();
                 retry = false;
             } catch (InterruptedException e) {
                 // we will try it again and again...
             }
         }
     }
     
     public void reset(){
         pauseView.setVisibility(INVISIBLE);
     	mainBall.setBallCharacteristics(parent.level);
     	mainBall.spawn();
     	parent.lives = 5;
     	parent.score = 0;
     	parent.level = 1;
     	over = false;
     	/*Remove and then re-add bricks*/
     	bricks.clear();
     	createBricks();
         playing = false;
         paused = false;
 
     }
     
     private void createBricks(){
 		for(int i = 0; i < (rows * 6); i++ ){
 			Brick tmpBrick = new Brick(i,mainPaddle.screenwidth, mainPaddle.screenheight/*, bricks*/);
 			bricks.add(tmpBrick);
 		}
     }
     
     private void nextLevel(){
     	parent.level = parent.level+1;
     	mainBall.setBallCharacteristics(parent.level);
     	playing = false;
         mainBall.spawned = false;
     	createBricks();
     }
 	
 	 class DrawThread extends  Thread {
 	        private SurfaceHolder mSurfaceHolder;
 	 
 	        public DrawThread (SurfaceHolder surfaceHolder){
 	            mSurfaceHolder = surfaceHolder;
 	        }
 	 
 	        public void setRunning(boolean run2) {
 	            _run = run2;
 	        }
 	 
 	        @Override
 	        public void run() {
 	            Canvas canvas = null;
 
                 float textSize = parent.getResources().getDimension(R.dimen.text_size);
 	 
 	            Paint paint = new Paint();
 	            paint.setColor(Color.RED);
 	            paint.setStrokeWidth(1);
 	            paint.setTextSize(textSize);
                 paint.setTextAlign(Paint.Align.CENTER);
 
                 Paint scorePaint = new Paint();
                 scorePaint.setColor(Color.RED);
                 scorePaint.setStrokeWidth(1);
                 scorePaint.setTextSize(textSize);
                 scorePaint.setTextAlign(Paint.Align.RIGHT);
 	            
 	            Paint brickPaint = new Paint();
 	            brickPaint.setColor(Color.GREEN);
 	            brickPaint.setStrokeWidth(1);
 	            
 	            Paint lifePaint = new Paint();
 	            lifePaint.setColor(Color.MAGENTA);
 	            lifePaint.setStrokeWidth(1);
 	            
 	            
 	            
 	 
 	            while (_run){
 	                if(isDrawing == true){
 	                    try{
 	                        canvas = mSurfaceHolder.lockCanvas(null);
 	                        if(mBitmap == null){
 	                            mBitmap =  Bitmap.createBitmap (1, 1, Bitmap.Config.ARGB_8888);
 	                        }
 	                        final Canvas c = new Canvas (mBitmap);
 	 
 	                     
 	                        c.drawColor(Color.BLACK);
 	                        
 	                        if(parent.lives > 0){
 	                        if(playing){
 	                        	if(!paused){
                                     if(!mainBall.spawned){
                                         mainBall.spawn();
                                     }
 	                        		c.drawRect(mainPaddle.selfimage, mainPaddle.selfstyle);
 	                        		c.drawRect(mainBall.image, mainBall.paint);
 	                        		if(eLife !=null){
 	                        			c.drawRect(eLife.image,lifePaint);
 	                        			eLife.tick(); //Move and check for extra life pickup
 	                        		}
 	                        		/*Draw the Bricks*/
 	                        		if(bricks.size() != 0){
 	                        		
 	                        	
 	                        			for(Brick tmpBrick : bricks){
 	                        					c.drawRect(tmpBrick.image, brickPaint);
 	                        			}
 	                        		}
 	                        		else{
 
 	                        			nextLevel();
 	                        		}
 	                        		c.drawText("Score:"+parent.score + "  Lives: " + parent.lives + " Level : " + parent.level, mainPaddle.screenwidth, 45, scorePaint);
 	                        		mainBall.tick(); //Tell the ball it needs to move again
                                     tickable = true;
                                     if(mainBall.timing){
                                         c.drawText(String.valueOf(mainBall.counter),mainPaddle.screenwidth/2,mainPaddle.screenheight/2,paint);
                                     }
 	                        		
 
                             }
 	                        	if(paused){
 	                        		//c.drawText("Game Paused (tap to unpause)", (mainPaddle.screenwidth/2) - 30, (mainPaddle.screenheight/2), paint);
                                     tickable = false;
 	                        	}
 	                        }
 	                        else{
 	                        	c.drawText("Tap to begin playing Level " + parent.level, mainPaddle.screenwidth/2, mainPaddle.screenheight/2 ,paint);
 	                        }
 	                        }
 	                        else{
 	                        	c.drawText("Game Over",mainPaddle.screenwidth/2 , mainPaddle.screenheight/2, paint);
 	                        	if(!over){
 	                        		parent.gameOver(parent);
 	                        		over = true;
                                     tickable = false;
 	                        	
 	                        	}
 	                        }
 	                        
 	                        
 	 
 	                        canvas.drawBitmap (mBitmap, 0,  0,null);
 	                    } finally {
 	                        mSurfaceHolder.unlockCanvasAndPost(canvas);
 	                    }
 	                }
 	            }
 	        }
 	    }
 	
 }
