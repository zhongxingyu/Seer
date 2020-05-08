 package com.example.glscrollsample;
 
 import android.content.Context;
 import android.opengl.GLSurfaceView;
import android.util.Log;
 import android.view.MotionEvent;
 
 public class MyGLSurfaceView extends GLSurfaceView {
 	private MyRenderer mRenderer;	
 	private float first;
 	private float y;
 	public boolean drawByEvent = true;
	private int eventCounter;
	private long last;
 	
 	public MyGLSurfaceView(Context context) {
 		super(context);
 		mRenderer = new MyRenderer(context);
 		setRenderer(mRenderer);
 		this.requestRender();
 	}
 
 	public boolean onTouchEvent(final MotionEvent event) {
 
		long current = System.currentTimeMillis();
		if (current > last + 1000) {
			Log.i("*****************", String.format("%f", eventCounter * 1000.0f / (current - last)));
			eventCounter = 0;
			last = current;
		}
		
		eventCounter++;
		
 		if (event.getAction() == MotionEvent.ACTION_DOWN)
 			first = (int)event.getY();
 
 		if (event.getAction() == MotionEvent.ACTION_MOVE) {
 			mRenderer.ypos = y + first - (int)event.getY();
 			if (drawByEvent)
 				this.requestRender();		
 		}
 		
 		if (event.getAction() == MotionEvent.ACTION_UP)
 			y = mRenderer.ypos;
 		    	
 		return true;
 	}
 }
