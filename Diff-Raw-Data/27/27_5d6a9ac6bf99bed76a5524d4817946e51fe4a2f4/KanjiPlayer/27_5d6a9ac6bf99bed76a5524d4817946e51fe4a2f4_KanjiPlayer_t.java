 package ch.shibastudio.kanjinotepad.drawing;
 
 import java.util.ArrayList;
 
 import ch.shibastudio.kanjinotepad.kanjivg.KanjiVGElement;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class KanjiPlayer extends SurfaceView implements SurfaceHolder.Callback{
 
 	private SurfaceHolder mHolder;
 	private DrawingThread mDrawingThread;
 	
 	public KanjiPlayer(Context context) {
 		super(context);
 		init();
 	}
 	
 	public KanjiPlayer(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 
 	private void init(){
 		mHolder = getHolder();
 		mHolder.addCallback(this);
 	}
 	
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		mDrawingThread = new DrawingThread(holder);
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		boolean retry = true;
 		mDrawingThread.setRunning(false);
 	    while (retry) {
 	        try {
 	        	mDrawingThread.join();
 	            retry = false;
 	        } catch (InterruptedException e) {
 	        }
 	    }
 	}
 
 	public void flushKanjiPath(){
 		// TODO: remove this method if it's not used
 	}
 	
 	public void setCurrentPaths(ArrayList<KanjiVGElement> paths){
		if(null != mDrawingThread){
			// Update the paths
			if(Thread.State.NEW != mDrawingThread.getState()){
				mDrawingThread.setRunning(false);
				mDrawingThread = new DrawingThread(mHolder);
				// NOTE: Memory Leak? How is the previous thread released from the memory?!
			}
			
			mDrawingThread.initPainting();
			mDrawingThread.generatePaths(paths);
			
			// Start the drawing thread
			mDrawingThread.setRunning(true);
			mDrawingThread.start();
 		}
 	}
 }
