 /**
  * 
  */
 package ltg.phenomena;
 
 import java.util.Observable;
 import java.util.Observer;
 
 import ltg.phenomena.helioroom.Helioroom;
 import ltg.phenomena.helioroom.Planet;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.graphics.drawable.shapes.ArcShape;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.widget.TextView;
 
 /**
  * @author tebemis
  *
  */
 public class SimulationView extends SurfaceView implements Observer, SurfaceHolder.Callback {
 	
 	/*
      * State-tracking constants
      */
 	public static final int STATE_SETUP = 1;
     public static final int STATE_PAUSE = 2;
     public static final int STATE_STOP = 3;
     public static final int STATE_RUNNING = 4;
     
     
     public static final float SPACE_BETWEEN_PLANETS = 10;
     
     
     /** Handle to the application context, used to e.g. fetch Drawables. */
     private Context mContext;
 
     /** Pointer to the text view to display "Paused.." etc. */
     private TextView mStatusText;
 
     /** The thread that actually draws the animation */
     private CanvasThread thread;
     
     /** Handle to the data to be rendered */
 	private Helioroom mData;
 	
 	private float planetRadius = -1;
 	
 	
 	
 	/**
 	 * Rendering thread
 	 * @author tebemis
 	 */
 	class CanvasThread extends Thread {
 	
         private int mCanvasHeight = 1;
         private int mCanvasWidth = 1;
         /** The state of the game. One of SETUP, RUNNING, PAUSE or STOP*/
         private int mState = STATE_SETUP;
         /** Indicate whether the surface has been created & is ready to draw */
         private boolean mRun = false;
         /** Scratch rectangle object. */
         private RectF mScratchRect;
         /** Handle to the surface manager object we interact with */
         private SurfaceHolder mSurfaceHolder;
         /** Time of the last frame */
         private long timeDelta = -1;
 
         
         public CanvasThread(SurfaceHolder surfaceHolder, Context context) {
             mSurfaceHolder = surfaceHolder;
             mContext = context;
             mScratchRect = new RectF(0, 0, 0, 0);
         }
 		
         
 		@Override
 		public void run() {
 			while (mRun) {
                 Canvas c = null;
                 try {
                     c = mSurfaceHolder.lockCanvas(null);
                     synchronized (mSurfaceHolder) {
                     	if (mState == STATE_RUNNING)
                         	doDraw(c);
                         // Waits for the data to be ready
                         if (mState==STATE_SETUP)
                         	if (mData != null && mData.getInstanceId()!= null)
                         		setState(STATE_RUNNING);
                     }
                 } finally {
                     if (c != null) {
                         mSurfaceHolder.unlockCanvasAndPost(c);
                     }
                 }
             }
 			doStop();
 		}
 		
 		
 		private void doStop() {
 			Log.e("", "Stopped thread!");
 		}
 
 
 		public void pause() {
 			synchronized (mSurfaceHolder) {
                 if (mState == STATE_RUNNING) 
                 	setState(STATE_PAUSE);
             }
 		}
 		
 		
         public void unpause() {
             synchronized (mSurfaceHolder) {
                if(mState==STATE_PAUSE)
             	   setState(STATE_RUNNING);
             }
         }
 		
 		
 		
         public void setRunning(boolean b) {
         	synchronized (mSurfaceHolder) {
         		mRun = b;
         	}
         }
         
         
         public void setState(int mode) {
             synchronized (mSurfaceHolder) {
                 mState = mode;
             }
         }
 
 
         
         public void setSurfaceSize(int width, int height) {
             synchronized (mSurfaceHolder) {
                 mCanvasWidth = width;
                 mCanvasHeight = height;
             }
         }
         
         
         private void doDraw(Canvas canvas) {
         	// Computes the time deltas
         	timeDelta = System.currentTimeMillis()/1000 - mData.getStartTime();
         	// Clean the background
         	canvas.drawColor(Color.BLACK);
         	// Draw planets
         	Paint pa = new Paint();
        	//Draw a white sun in the middle
        	pa.setColor(Color.WHITE);
        	canvas.drawCircle(mCanvasWidth/2, mCanvasHeight/2, planetRadius, pa);
         	int i = 1;
         	for (Planet p: mData.getPlanets()) {
         		p.computePosition((float)timeDelta);
         		pa.setColor(Color.parseColor(p.getColor().replaceAll("0x", "#")));
         		float scale = i*(planetRadius+SPACE_BETWEEN_PLANETS);
         		canvas.drawCircle(mCanvasWidth/2 + p.getX()*scale, mCanvasHeight/2 + p.getY()*scale, planetRadius, pa);
         		i++;
         	}
         }
 		
 	}
 
 	
 	//---------------------------//
 	// END OF THREAD DECLARATION //
 	//---------------------------//
 	
 	
 	
 	
 	
 	public SimulationView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		// register our interest in hearing about changes to our surface
         SurfaceHolder holder = getHolder();
         holder.addCallback(this);
         // create thread only; it's started in surfaceCreated()
         thread = new CanvasThread(holder, context);
         setFocusable(true); // make sure we get key events
 	}
 	
 	
 	public CanvasThread getThread() {
 		return thread;
 	}
 	
 	
 	public void setTextView(TextView textView) {
 		mStatusText = textView;
 	}
 	
 	
 	@Override
 	public void update(Observable observable, Object data) {
 		mData = ((Helioroom) data);
 		float n = (float) mData.getPlanets().size();
 		planetRadius = (thread.mCanvasWidth - 2*(n+1)*SPACE_BETWEEN_PLANETS) / (2 * (2*n+1));
 	}
 	
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 		thread.setSurfaceSize(width, height); 
 	}
 
 	
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		thread.setRunning(true);
         thread.start();
 	}
 
 	
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
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
 }
