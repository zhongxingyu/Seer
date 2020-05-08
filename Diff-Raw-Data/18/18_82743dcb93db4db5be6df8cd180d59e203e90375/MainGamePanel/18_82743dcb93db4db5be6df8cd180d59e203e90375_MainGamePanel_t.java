 /**
  * 
  */
 package com.september.tableroids;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 import com.september.tableroids.consts.Constants;
 import com.september.tableroids.model.Sprite;
 import com.september.tableroids.model.elements.ResponseCointaner;
 import com.september.tableroids.model.elements.SquareResponse;
 import com.september.tableroids.statics.GameBuilder;
 import com.september.tableroids.statics.Scorer;
 import com.september.tableroids.utils.GraphicsUtils;
 import com.september.tableroids.utils.UIWidgtesManager;
 import com.september.tableroids.utils.Updater;
 
 /**
  * @author impaler
  * This is the main surface that handles the ontouch events and draws
  * the image to the screen.
  */
 public class MainGamePanel extends SurfaceView implements
 		SurfaceHolder.Callback {
 
 	private static final String TAG = MainGamePanel.class.getSimpleName();
 	
 	private MainThread thread;
 	private Context context;
 
 	// the fps to be displayed
 	private String avgFps;
 	public void setAvgFps(String avgFps) {
 		this.avgFps = avgFps;
 	}
 	
 	 @Override
 	    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 	        super.onSizeChanged(w, h, oldw, oldh);
 	        GraphicsUtils.setScreenSize(w,h);
 	        int translation = oldh - h;
 	        GameBuilder.getBackgroundSprite().setY(GameBuilder.getBackgroundSprite().getY() - translation);
 	    }
 
 	    @Override
 	    protected void onDraw(Canvas canvas) {
 	       super.onDraw(canvas);
 	       GraphicsUtils.setScreenSize(canvas.getWidth(),canvas.getHeight());
 	   }
 	    
 	   
 	    
 	public MainGamePanel(Context context, AttributeSet as) {
 		super(context,as);
 		init(context);
 	}
 
 	public MainGamePanel(Context context) {
 		super(context);
 		// adding the callback (this) to the surface holder to intercept events
 //		getHolder().addCallback(this);
 //
 //		// create Elaine and load bitmap
 ////		elaine = new ElaineAnimated(
 ////				BitmapFactory.decodeResource(getResources(), R.drawable.walk_elaine) 
 ////				, 10, 50	// initial position
 ////				, 30, 47	// width and height of sprite
 ////				, 5, 5);	// FPS and number of frames in the animation
 //		
 //		GameBuilder.build((Activity)context);
 //		
 //		// create the game loop thread
 //		thread = new MainThread(getHolder(), this);
 //		
 //		// make the GamePanel focusable so it can handle events
 //		setFocusable(true);
 		init(context);
 	}
 	
 	public void summary() {
 		Scorer.setMoltiplicando(null);
 		Scorer.setMoltiplicatore(null);
 		GameBuilder.setReady(false);
 		thread.setRunning(false);
 		Intent intent = new Intent(((Activity)context), SummaryActivity.class);
 		((Activity)context).startActivity(intent);
 	}
 	
 	private void init(Context context) {
 		this.context = context;
 		getHolder().addCallback(this);
 
 		// create Elaine and load bitmap
 //		elaine = new ElaineAnimated(
 //				BitmapFactory.decodeResource(getResources(), R.drawable.walk_elaine) 
 //				, 10, 50	// initial position
 //				, 30, 47	// width and height of sprite
 //				, 5, 5);	// FPS and number of frames in the animation
 //		((Activity)context).runOnUiThread(new Runnable() {
 //
 //			@Override
 //			public void run() {
 //				LinearLayout ll = (LinearLayout) ((Activity)context).findViewById(R.id.gamellayout);
 //				while (!GameBuilder.isReady() || ll == null) {
 //					try {
 //						Thread.sleep(200);
 //						if (ll == null) {
 //							ll = (LinearLayout) ((Activity)context).findViewById(R.id.gamellayout);
 //						}
 //					} catch (InterruptedException e) {
 //						// TODO Auto-generated catch block
 //						android.util.Log.e(TAG,e.getMessage());
 //					}
 //				}
 //				
 //				TextView tv = new TextView(context);
 //				tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 20));
 //				tv.setText("FAKE ADMOB");
 //				tv.setBackgroundColor(Color.MAGENTA);
 //				tv.setTextColor(Color.WHITE);
 //				
 //				ll.addView(tv);
 //			}
 //			
 //		});
 		
 		
 		GameBuilder.build((Activity)context);
 		
 		// create the game loop thread
 		thread = new MainThread(getHolder(), this);
 		
 		// make the GamePanel focusable so it can handle events
 		setFocusable(true);
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		// at this point the surface is created and
 		// we can safely start the game loop
		if(thread == null) {
			thread = new MainThread(getHolder(), this);
		}
 		thread.setRunning(true);
 		thread.start();
		
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		Log.d(TAG, "Surface is being destroyed");
 		// tell the thread to shut down and wait for it to finish
 		// this is a clean shutdown
 		boolean retry = true;
 		while (retry) {
 			try {
				Scorer.reset();
				GameBuilder.setReady(false);
				Scorer.setReadyToPlay(false);
 				thread.setRunning(false);
 				thread.join();
				thread = null;
 				retry = false;
 			} catch (Exception e) {
 				// try again shutting down the thread
 			}
 		}
 		Log.d(TAG, "Thread was shut down cleanly");
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if (event.getAction() == MotionEvent.ACTION_UP) {
 			Bitmap bmp = Bitmap.createBitmap(1,1,Bitmap.Config.RGB_565);
 			bmp.setPixel(0, 0, Color.BLACK);
 			
 			Sprite collider = new Sprite(bmp, (int) event.getX(), (int) event.getY(), 1, new int[]{1,1}) {
 				
 				@Override
 				public void onTouch(MotionEvent event) {
 					// TODO Auto-generated method stub
 					
 				}
 				
 				@Override
 				public void onCollide() {
 					// TODO Auto-generated method stub
 					
 				}
 				
 				@Override
 				protected void doUpdate() {
 					// TODO Auto-generated method stub
 					
 				}
 			};
 			
 			for(Sprite s:Updater.getInstance().getSprites()) {
 				if(s.isDrawn() && s.isTouchable() && s.collide(collider)) {
 					s.onTouch(event);
 				}
 				
 			}
 		}
 		return true;
 	}
 
 	public void render(Canvas canvas) {
 		canvas.drawColor(Color.parseColor("#B8DBFF"));
 		//elaine.draw(canvas);
 		for(Sprite s:Updater.getInstance().getSprites()) {
 			s.draw(canvas);
 		}
 		// display fps
 		//displayFps(canvas, avgFps);
 	}
 
 	/**
 	 * This is the game update method. It iterates through all the objects
 	 * and calls their update method if they have one or calls specific
 	 * engine's update method.
 	 */
 	public void update() {
 		//elaine.update(System.currentTimeMillis());
 		Updater.getInstance().clear();
 		Long gameTime = System.currentTimeMillis();
 		for(Sprite s:Updater.getInstance().getSprites()) {
 			s.update(gameTime);
 		}
 	}
 	
 
 
 	private void displayFps(Canvas canvas, String fps) {
 		if (canvas != null && fps != null) {
 			Paint paint = new Paint();
 			paint.setARGB(255, 255, 255, 255);
 			canvas.drawText(fps, this.getWidth() - 50, 20, paint);
 		}
 	}
 
 	public void showLoading(Canvas canvas) {
 		
 		//Bitmap.Config conf = Bitmap.Config.ARGB_8888;
 		
 		Sprite backGround = GameBuilder.getBackgroundSprite();
 		backGround.setY(GameBuilder.getOut().heightPixels-backGround.getResizedHeight());
 //		Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 //		Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		
 		canvas.drawColor(Color.parseColor("#B8DBFF"));
 		GameBuilder.getBackgroundSprite().draw(canvas);
 		
 		int x = 0;
 		int y = GameBuilder.getOut().heightPixels/3;
 		
 		int width = GameBuilder.getOut().widthPixels;
 		int height = GameBuilder.getOut().heightPixels/15;
 		
 		UIWidgtesManager.getInstance().paintStartButton(Constants.NEW_GAME_BUTTON_ID_10, Constants.QUESTION_NO_10, canvas, x, y, width, height, getResources().getString(R.string.easy));
 		
 		UIWidgtesManager.getInstance().paintStartButton(Constants.NEW_GAME_BUTTON_ID_20, Constants.QUESTION_NO_20, canvas, x, y+height*2, width, height,getResources().getString(R.string.medium));
 		
 		UIWidgtesManager.getInstance().paintStartButton(Constants.NEW_GAME_BUTTON_ID_30, Constants.QUESTION_NO_30, canvas, x, y+height*4, width, height, getResources().getString(R.string.hard));
 		
 //		Sprite button = Updater.getInstance().getById(Constants.NEW_GAME_BUTTON_ID);
 //		if(button == null) {		
 //		
 //
 //	    
 //	    Bitmap newGameBitmap = Bitmap.createBitmap(GameBuilder.getOut().widthPixels, GameBuilder.getOut().heightPixels/10, Bitmap.Config.ARGB_8888);
 //	    
 //	    for(int x = 0; x< newGameBitmap.getWidth(); x++) {
 //	    	for(int y = 0; y < newGameBitmap.getHeight(); y++) {
 //	    		if(x%2==0) {
 //	    			newGameBitmap.setPixel(x, y, Color.RED);
 //	    		}
 //	    	}
 //	    }
 //	    
 //	    button = new Sprite(newGameBitmap, 0, GameBuilder.getOut().heightPixels/3 - (GameBuilder.getOut().heightPixels/10), 1, new int[]{1,1}) {
 //			
 //			@Override
 //			public void onTouch(MotionEvent event) {
 //				Scorer.setReadyToPlay(true);
 //			}
 //			
 //			@Override
 //			public void onCollide() {
 //				// TODO Auto-generated method stub
 //				
 //			}
 //			
 //			@Override
 //			protected void doUpdate() {
 //				// TODO Auto-generated method stub
 //				
 //			}
 //		};
 //		
 //		button.setTouchable(true);
 //		button.setId(Constants.NEW_GAME_BUTTON_ID);
 //		Updater.getInstance().addSprite(button);
 //		}
 //		canvas.drawColor(Color.parseColor("#B8DBFF"));
 //		GameBuilder.getBackgroundSprite().draw(canvas);
 //		
 //		
 //	    strokePaint.setColor(Color.WHITE);
 //	    strokePaint.setTextAlign(Paint.Align.CENTER);
 //	    strokePaint.setTextSize(GameBuilder.getOut().heightPixels/10);
 //	    strokePaint.setTypeface(GameBuilder.getTypeFace());
 //	    strokePaint.setStyle(Paint.Style.STROKE);
 //	    strokePaint.setStrokeWidth(10);
 //
 //	   
 //	    textPaint.setColor(Color.BLUE);
 //	    textPaint.setTextAlign(Paint.Align.CENTER);
 //	    textPaint.setTextSize(GameBuilder.getOut().heightPixels/10);
 //	    textPaint.setTypeface(GameBuilder.getTypeFace());
 //	    
 //	    
 //
 //	    canvas.drawText("New Game",  GameBuilder.getOut().widthPixels/2, GameBuilder.getOut().heightPixels/3, strokePaint);
 //	    canvas.drawText("New Game",  GameBuilder.getOut().widthPixels/2, GameBuilder.getOut().heightPixels/3, textPaint);
 //		
 //		button.draw(canvas);
 		
 
 	}
 
 }
