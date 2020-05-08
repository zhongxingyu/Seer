 package android.com.touchanimtn;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.Window;
 
 public class TouchanimtnActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(new Panel (this));
     }
     class Panel extends SurfaceView implements SurfaceHolder.Callback{
     	private Bitmap mBitmap;
     	private TutorialThread mthread;
     	
     	private int mX;
         private int mY;
     	
 		public Panel(Context context) {
 			super(context);
 			// TODO Auto-generated constructor stub
 			mBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher);
 			getHolder().addCallback(this);
 			mthread = new TutorialThread(this);
//			mthread = new Tutorialhread(this);
 		}
 		
 		
         public void doDraw(Canvas canvas) {
             canvas.drawColor(Color.BLACK);
             canvas.drawBitmap(mBitmap, mX, mY, null);
         }
     	
 		 @Override
 	        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 	            // TODO Auto-generated method stub
 	 
 	        }
 	 
 	        @Override
 	        public void surfaceCreated(SurfaceHolder holder) {
 	            if(!mthread.isAlive()){
 	        	  	mthread.setRunning(true);
 	        	  	mthread.start();
 	            }
 	        }
 	 
 	        @Override
 	        public void surfaceDestroyed(SurfaceHolder holder) {
 	            if(mthread.isAlive()){
 	            	mthread.setRunning(false);
 	            }
 	        }
 	        
 	        @Override
 	        public boolean onTouchEvent(MotionEvent event) {
 	            mX = (int) event.getX() - mBitmap.getWidth() / 2;
 	            mY = (int) event.getY() - mBitmap.getHeight() / 2;
 	            return super.onTouchEvent(event);
 	        }
 	    }
     	
     class TutorialThread extends Thread {
         private SurfaceHolder mHolder;
         private Panel mpanel;
         private boolean mrun = false;
  
         public TutorialThread( Panel panel) {
             mpanel = panel;
             mHolder = mpanel.getHolder();
         }
  
         public void setRunning(boolean run) {
             mrun = run;
         }
  
         @Override
         public void run() {
             Canvas c = null;
             while (mrun) {
                 c = mHolder.lockCanvas();
                 if (c != null) {
                 	mpanel.doDraw(c);
                 	mHolder.unlockCanvasAndPost(c);
                 }
                 
             }
         }
     }
 }
