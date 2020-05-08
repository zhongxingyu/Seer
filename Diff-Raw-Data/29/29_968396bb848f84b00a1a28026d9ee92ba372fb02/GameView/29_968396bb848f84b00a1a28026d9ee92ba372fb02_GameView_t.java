 package com.kongentertainment.android.cardtactics.view;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.os.Handler;
 import android.os.Message;
 import android.util.AttributeSet;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.widget.RelativeLayout;
 
 import com.kongentertainment.android.cardtactics.R;
 import com.kongentertainment.android.cardtactics.model.entities.CreatureCard;
 
 /**
  * Class GameView
  */ 
 
 public class GameView extends SurfaceView implements SurfaceHolder.Callback {
 	public class GameViewThread extends Thread {
 		
 		 /** The state of the game. One of MYTURN, HISTURN, PAUSE, LOSE, or WIN */
         private int mMode;
 		
 		  /** Indicate whether the surface has been created & is ready to draw */
         private boolean mRun = false;
 		
 		/**
          * Current height of the surface/canvas.
          * 
          * @see #setSurfaceSize
          */
         private int mCanvasHeight = 1;
         
         /**
          * Current width of the surface/canvas.
          * 
          * @see #setSurfaceSize
          */
         private int mCanvasWidth = 1;
 		
 		  /** Handle to the surface manager object we interact with */
         private SurfaceHolder mSurfaceHolder;
         
         /** Handle to the application context, used to e.g. fetch Drawables. */
         private Context mContext;
         
         /** Message handler used by thread to interact with TextView */
         private Handler mHandler;
 		
         /** Background bitmap, drawn under everything else. */
 		private Bitmap mBackgroundImage;
 		
 		 /** Scratch rect object. */
 		private RectF mScratchRect;
 		
 		/** Relative layout holding buttons, etc */
 		//private RelativeLayout mRelativeLayout;
 
         /** Our copy of the view on the player's creature yard */
         private CreatureYardView mCreatureYardView;
         /** Holds all the bitmaps for all the cards */
         private CardViewManager mCardViewManager;
 		
 		private Bitmap mTestCard;
 		private Bitmap mLittleCard;
 		private Bitmap mCommandButton;
 		private Bitmap mResourceCounter;
 		private Bitmap mResourceSymbol;
 		
         public GameViewThread(SurfaceHolder surfaceHolder, Context context,
 	                Handler handler) {
 	            // get handles to some important objects
 	            mSurfaceHolder = surfaceHolder;
 	            mHandler = handler;
 	            mContext = context;
 
 	            Resources res = context.getResources();
 
                 //DEBUG CODE, must replace
                 mCardViewManager  = new CardViewManager(res);
                mCreatureYardView = new CreatureYardView("Debug", mCardViewManager);
 
 				//mRelativeLayout = (RelativeLayout) findViewById(R.id.gameRelativeLayout);
 
 				//CreatureCard newCard = new CreatureCard(1);
 				//CreatureCardView creatureCardView = new CreatureCardView(context, newCard);
 				//mRelativeLayout.addView(creatureCardView);
 
 	            // load background image as a Bitmap instead of a Drawable b/c
 	            // we don't need to transform it and it's faster to draw this way
 	            mBackgroundImage = BitmapFactory.decodeResource(res,
 	                    R.drawable.background);
 				mTestCard = BitmapFactory.decodeResource(res,
                     R.drawable.bigcard);
 				mLittleCard = BitmapFactory.decodeResource(res,
                     R.drawable.card);
 				mCommandButton = BitmapFactory.decodeResource(res,
                     R.drawable.commandbutton);
 				mResourceSymbol = BitmapFactory.decodeResource(res,
                     R.drawable.resourcesymbol);
 				mResourceCounter = BitmapFactory.decodeResource(res,
                     R.drawable.resourcecounter);
 	            /*
 	            // Initialize paints for speedometer
 	            mLinePaint = new Paint();
 	            mLinePaint.setAntiAlias(true);
 	            mLinePaint.setARGB(255, 0, 255, 0);
 
 	            mLinePaintBad = new Paint();
 	            mLinePaintBad.setAntiAlias(true);
 	            mLinePaintBad.setARGB(255, 120, 180, 0);
 	            */
         }
 		
 		@Override
         public void run() {
             while (mRun) {
                	//if (mMode == STATE_RUNNING) updatePhysics();
             	//Take input
             	//Make move
                 Canvas c = null;
                 try {
                     c = mSurfaceHolder.lockCanvas(null);
                     synchronized (mSurfaceHolder) {                 
                     	//Draw to screen
                         doDraw(c);
                     }
                 } finally {
                     // do this in a finally so that if an exception is thrown
                     // during the above, we don't leave the Surface in an
                     // inconsistent state
                     if (c != null) {
                         mSurfaceHolder.unlockCanvasAndPost(c);
                     }
                 }
                 //Play sounds
             }
         }		
 		
         /**
          * Draws the ship, fuel/speed bars, and background to the provided
          * Canvas.
          */
         private void doDraw(Canvas canvas) {
             // Draw the background image. Operations on the Canvas accumulate
             // so this is like clearing the screen.
             canvas.drawBitmap(mBackgroundImage, 0, 0, null);
 
             //draw anything we haven't implemented programatically yet
             testDraw(canvas);
             
             //Player's creature yard
             mCreatureYardView.draw(canvas);
 
             //Draw effects
         }
 
         private void testDraw(Canvas canvas) {
 			//Center line
 			Paint blackPaint = new Paint();
 			blackPaint.setColor(0xff000000);
 			blackPaint.setStrokeWidth((float) 1.0);
 			blackPaint.setStyle(Paint.Style.STROKE);
 			canvas.drawLine(0, 160, 480, 160, blackPaint);
 
             //Card Preview
             canvas.drawBitmap(mTestCard, 7, 75, null);            
 
 			//Player's back row
 			//canvas.drawBitmap(mLittleCard, 120, 242, null);            
 			//canvas.drawBitmap(mLittleCard, 192, 242, null);            
 			//canvas.drawBitmap(mLittleCard, 264, 242, null);            
 			
 			//Player's front row
 			//canvas.drawBitmap(mLittleCard, 192, 164, null);            
 			//Command Button
             //canvas.drawBitmap(mCommandButton, 352, 140, null);
 			//Resource items
 			canvas.drawBitmap(mResourceSymbol, 379, 214, null);
 			canvas.drawBitmap(mResourceCounter, 363, 183, null);
 			//OPPONENT
 			
 			//Resource items
 			canvas.drawBitmap(mResourceSymbol, 379, 10, null); 
 			canvas.drawBitmap(mResourceCounter, 363, 102, null);            
 			//Opponent's back row
 			canvas.drawBitmap(mLittleCard, 120, 1, null);            
 			canvas.drawBitmap(mLittleCard, 192, 1, null);            
 			canvas.drawBitmap(mLittleCard, 264, 1, null);            
 			
 			//Opponent's front row
 			//canvas.drawBitmap(mLittleCard, 264, 79, null);            
         }
         
         /* Callback invoked when the surface dimensions change. */
         public void setSurfaceSize(int width, int height) {
             // synchronized to make sure these all change atomically
             synchronized (mSurfaceHolder) {
                 mCanvasWidth = width;
                 mCanvasHeight = height;
 
                 // don't forget to resize the background image
                 mBackgroundImage = Bitmap.createScaledBitmap(
                         mBackgroundImage, width, height, true);
             }
         }
         
         /**
          * Sets the game mode. That is, whether we are running, paused, in the
          * failure state, in the victory state, etc.
          * 
          * @see #setState(int, CharSequence)
          * @param mode one of the STATE_* constants
          */
         public void setState(int mode) {
             synchronized (mSurfaceHolder) {
                 setState(mode, null);
             }
         }
 
         /**
          * Sets the game mode. That is, whether we are running, paused, in the
          * failure state, in the victory state, etc.
          * 
          * @param mode one of the STATE_* constants
          * @param message string to add to screen or null
          */
         public void setState(int mode, CharSequence message) {
             /*
              * This method optionally can cause a text message to be displayed
              * to the user when the mode changes. Since the View that actually
              * renders that text is part of the main View hierarchy and not
              * owned by this thread, we can't touch the state of that View.
              * Instead we use a Message + Handler to relay commands to the main
              * thread, which updates the user-text View.
              */
             synchronized (mSurfaceHolder) {
                 mMode = mode;
 
                 /*
                 if (mMode == STATE_RUNNING) {
                     Message msg = mHandler.obtainMessage();
                     Bundle b = new Bundle();
                     b.putString("text", "");
                     b.putInt("viz", View.INVISIBLE);
                     msg.setData(b);
                     mHandler.sendMessage(msg);
                 } else {
                     mRotating = 0;
                     mEngineFiring = false;
                     Resources res = mContext.getResources();
                     CharSequence str = "";
                     if (mMode == STATE_READY)
                         str = res.getText(R.string.mode_ready);
                     else if (mMode == STATE_PAUSE)
                         str = res.getText(R.string.mode_pause);
                     else if (mMode == STATE_LOSE)
                         str = res.getText(R.string.mode_lose);
                     else if (mMode == STATE_WIN)
                         str = res.getString(R.string.mode_win_prefix)
                                 + mWinsInARow + " "
                                 + res.getString(R.string.mode_win_suffix);
 
                     if (message != null) {
                         str = message + "\n" + str;
                     }
 
                     if (mMode == STATE_LOSE) mWinsInARow = 0;
 
                     Message msg = mHandler.obtainMessage();
                     Bundle b = new Bundle();
                     b.putString("text", str.toString());
                     b.putInt("viz", View.VISIBLE);
                     msg.setData(b);
                     mHandler.sendMessage(msg);
                 */
             }
         }
         
         /**
          * Used to signal the thread whether it should be running or not.
          * Passing true allows the thread to run; passing false will shut it
          * down if it's already running. Calling start() after this was most
          * recently called with false will result in an immediate shutdown.
          * 
          * @param b true to run, false to shut down
          */
         public void setRunning(boolean b) {
             mRun = b;
         }
         
         public CardViewManager getCardViewManager() {
             return mCardViewManager;
         }
     }
 	
 	 /** The thread that actually draws the animation */
     private GameViewThread thread;
 	
 	public GameView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		
 		 // register our interest in hearing about changes to our surface
         SurfaceHolder holder = getHolder();
         holder.addCallback(this);
 
         // create thread only; it's started in surfaceCreated()
         thread = new GameViewThread(holder, context, new Handler() {
             @Override
             public void handleMessage(Message m) {
                 //mStatusText.setVisibility(m.getData().getInt("viz"));
                 //mStatusText.setText(m.getData().getString("text"));
             }
         });
 
         setFocusable(true); // make sure we get key events
 	}
 
 	  /* Callback invoked when the surface dimensions change. */
     public void surfaceChanged(SurfaceHolder holder, int format, int width,
             int height) {
         thread.setSurfaceSize(width, height);
     }
 
 	  /*
      * Callback invoked when the Surface has been created and is ready to be
      * used.
      */
     public void surfaceCreated(SurfaceHolder holder) {
         // start the thread here so that we don't busy-wait in run()
         // waiting for the surface to be created
         thread.setRunning(true);
         thread.start();
     }
 
     /*
      * Callback invoked when the Surface has been destroyed and must no longer
      * be touched. WARNING: after this method returns, the Surface/Canvas must
      * never be touched again!
      */
     public void surfaceDestroyed(SurfaceHolder holder) {
         // we have to tell thread to shut down & wait for it to finish, or else
         // it might touch the Surface after we return and explode
         boolean retry = true;
         thread.setRunning(false);
         while (retry) {
             try {
                 thread.join();
                 retry = false;
             } catch (InterruptedException e) {
             }
         }
     }
     //******************************
     //ACCESSORS
     //******************************
     /**
      * Fetches the animation thread corresponding to this LunarView.
      * 
      * @return the animation thread
      */
     public GameViewThread getThread() {
         return thread;
     }
 
 }
