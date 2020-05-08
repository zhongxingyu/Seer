 package com.Circuit.test;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.ScaleGestureDetector;
 import android.widget.RelativeLayout;
 
 public class MainLayout extends RelativeLayout {
 	
 	private Path xSymbol;
 	private Path ySymbol;
     private Paint mPaint;     
 
     private static int NONE = 0;
 	private static int DRAG = 1;
 	private static int ZOOM = 2;
 	private int mode;
 	
 	private ScaleGestureDetector detector;
     private float mScaleFactor = 1.f;
 
 	public MainLayout(Context context) {
 		super(context);	
 		
 		detector = new ScaleGestureDetector(getContext(), new ScaleListener());
 		
 		init();
 	}
 	
 	public MainLayout(Context context, AttributeSet attrs) {
         super(context, attrs);
         
         detector = new ScaleGestureDetector(getContext(), new ScaleListener());
         init();
     }
 	
 	private void init() {
 		
         xSymbol = new Path();
         ySymbol = new Path();
         mPaint = new Paint();
         
         mPaint.setAntiAlias(true);		
 		mPaint.setStrokeWidth(2);
 		mPaint.setColor(Color.WHITE);	
 		
 		mPaint.setStyle(Paint.Style.STROKE);		
         
 		mPaint.setAlpha(20);
 		
         //...Your code here to set up the path,
         //...allocate objects here, never in the drawing code.
         
 		for (int x = 0; x <= 1000; x += 20){
 			
 			
 			xSymbol.moveTo(x, 0);
 			xSymbol.lineTo(x, 1000);
 			
 		}
 		
 		for (int y = 0; y <= 1000; y += 20){
 			
 			ySymbol.moveTo(0, y);
 			ySymbol.lineTo(1000, y);
 		}        	    
 	    
 		//Log.d("Main init", "Inside init");
 	    
     }
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		// TODO Auto-generated method stub
 		super.onDraw(canvas);
 		
 		//RelativeLayout.LayoutParams tempLayoutParams = (RelativeLayout.LayoutParams) this.getLayoutParams();
 		//tempLayoutParams.width = (int) (Global.mScaleFactor * 1000);
 		//tempLayoutParams.height = (int) (Global.mScaleFactor * 1000);
 		//this.setLayoutParams(tempLayoutParams);
 		
 		//Scale the canvas.
 		//canvas.scale(Global.mScaleFactor, Global.mScaleFactor);
 		
 		if (Global.setAlphaWhenMoving){
 			
 			mPaint.setAlpha(30);
 		}else {
 			
 			mPaint.setAlpha(20);
 		}		
 	
 		
 		canvas.drawPath(xSymbol, mPaint);
 		canvas.drawPath(ySymbol, mPaint);		
 		
 		//Log.d("Main onDraw", "Width " + tempLayoutParams.width);
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		// TODO Auto-generated method stub
 		
 		switch (event.getAction() & MotionEvent.ACTION_MASK) {
         case MotionEvent.ACTION_DOWN:
         	mode = DRAG;
         	Global.setSelectedResistor(false); 
         	
             break;
         case MotionEvent.ACTION_UP:
         	mode = NONE;
             break;
         case MotionEvent.ACTION_POINTER_DOWN:
         	mode = ZOOM;
             break;
         case MotionEvent.ACTION_POINTER_UP:
         	mode = NONE;
             break;
         case MotionEvent.ACTION_MOVE:
         	
             break;
     }
 		
 		detector.onTouchEvent(event);
 		
 		if (mode == ZOOM) {			
         	
			//invalidate();
 		}
 			
 		return true;
 	}
 	
 	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
 
 		@Override
 		public boolean onScale(ScaleGestureDetector detector) {
 			Global.mScaleFactor *= detector.getScaleFactor();
 	        
 	        // Don't let the object get too small or too large.
 	        Global.mScaleFactor = Math.max(0.1f, Math.min(Global.mScaleFactor, 5.0f));
 	        
 	        return true;
 
 			}
 	   
 	    }
 	
 	
 
 	/*@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		// TODO Auto-generated method stub
 		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 		setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
 	}*/
 
 }
