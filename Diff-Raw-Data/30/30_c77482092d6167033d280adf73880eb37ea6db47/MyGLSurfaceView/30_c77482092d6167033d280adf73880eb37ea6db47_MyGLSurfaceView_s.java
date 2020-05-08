 package com.example.glscrollsample;
 
 import android.content.Context;
 import android.opengl.GLSurfaceView;
 import android.view.MotionEvent;
 
 public class MyGLSurfaceView extends GLSurfaceView {
 	private MyRenderer mRenderer;	
 	private float first;
 	private float y;
 	public boolean drawByEvent = true;
 	
 	public MyGLSurfaceView(Context context) {
 		super(context);
 		mRenderer = new MyRenderer(context);
 		setRenderer(mRenderer);
 		this.requestRender();
 	}
 
 	public boolean onTouchEvent(final MotionEvent event) {
 
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
