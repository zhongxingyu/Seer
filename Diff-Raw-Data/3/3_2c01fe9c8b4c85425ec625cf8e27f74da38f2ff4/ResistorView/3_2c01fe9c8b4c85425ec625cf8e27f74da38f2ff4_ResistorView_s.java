 package com.Circuit.test;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Point;
 import android.graphics.PointF;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.RelativeLayout;
 
 public class ResistorView extends View implements OnTouchListener{
 
 	private Path mSymbol, mSymbolRotated;
     private Paint mPaint;
     private Paint mPaintNode;
     
     public int resistorOrientation;
     public Point node1;
     public Point node2;
     
     private int snapCase;
     
     private CircuitSolverActivity mainActivity;
     
     RelativeLayout root; 
     private int _xDelta;
     private int _yDelta;     
 	
     private int gridCellSize = 10;
     
     private static int NONE = 0;
 	private static int DRAG = 1;
 	private static int ZOOM = 2;
 	private int mode;
 
     
     private OnTouchListener detector;
     private float mScaleFactor = 1.f;
 
 	//These two variables keep track of the X and Y coordinate of the finger when it first
 	//touches the screen
 	private float startX = 0f;
 	private float startY = 0f;
 	
 	//These two variables keep track of the amount we need to translate the canvas along the X
 	//and the Y coordinate
 	private float translateX = 0f;
 	private float translateY = 0f;
 	
 	//These two variables keep track of the amount we translated the X and Y coordinates, the last time we
 	//panned.
 	private float previousTranslateX = 0f;
 	private float previousTranslateY = 0f;
 
     float mX, mY;
 
     //...Override Constructors...    
     public ResistorView(Context context, AttributeSet attrs) {
         super(context, attrs); 
         // Create our ScaleGestureDetector
         //detector = new ScaleGestureDetector(getContext(), new ScaleListener());
         this.setOnTouchListener(this);
         mainActivity = new CircuitSolverActivity();
         init();        
     }
     
     public ResistorView(Context context){
     	super(context); 
     	// Create our ScaleGestureDetector
     	//detector = new ScaleGestureDetector(getContext(), new ScaleListener()); 
     	this.setOnTouchListener(this);
     	mainActivity = new CircuitSolverActivity();
     	init();
     }
     
 	/*@Override
 	public boolean onTouchEvent(MotionEvent event) {		
 		
 		
 		final int X = (int) event.getRawX();
 	    final int Y = (int) event.getRawY();
 	    
 		switch (event.getAction() & MotionEvent.ACTION_MASK) {
         case MotionEvent.ACTION_DOWN:
         	mode = DRAG;
         	RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) this.getLayoutParams();
             _xDelta = X - lParams.leftMargin;
             _yDelta = Y - lParams.topMargin;
             mPaint.setColor(Color.YELLOW);
             mPaintNode.setColor(Color.YELLOW);
             selected = true;
             break;
         case MotionEvent.ACTION_UP:	        	
         	Global.setAlphaWhenMoving(false);
         	mode = NONE;
         	break;
 		case MotionEvent.ACTION_POINTER_DOWN:
         	mode = ZOOM;
             break;
         case MotionEvent.ACTION_POINTER_UP:
         	mode = NONE;
             break;
         case MotionEvent.ACTION_MOVE:
         	RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.getLayoutParams();
             layoutParams.leftMargin = X - _xDelta ;	            
             //Log.d("Right", "distance from right is" + this.getRight());
             layoutParams.topMargin = Y - _yDelta;	            
             layoutParams.rightMargin = -250;
             layoutParams.bottomMargin = -250;	            
             Global.setAlphaWhenMoving(true);	            
             this.setLayoutParams(layoutParams);
             break;
 		}	
 	    
 	    //This will set the value of scaleFactor
 	  	//detector.onTouchEvent(event);        
 	    //root.invalidate();  	    
 	    
 		return detector.onTouchEvent(event);
 		
 		// Let the ScaleGestureDetector inspect all events.
 	    /*mScaleDetector.onTouchEvent(event);
 	    
 	    final int action = event.getAction();
 	    switch (action & MotionEvent.ACTION_MASK) {
 	    
 	    	case MotionEvent.ACTION_DOWN: {
 	    		final float x = event.getX();
 	    		final float y = event.getY();
 	        
 	    		mLastTouchX = x;
 	    		mLastTouchY = y;
 	    		mActivePointerId = event.getPointerId(0);
 	    		break;
 	    	}
 	    
 	    	case MotionEvent.ACTION_MOVE: {
 	    		final int pointerIndex = event.findPointerIndex(mActivePointerId);
 	    		final float x = event.getX(pointerIndex);
 	    		final float y = event.getY(pointerIndex);
 
 	    		// Only move if the ScaleGestureDetector isn't processing a gesture.
 	    		if (!mScaleDetector.isInProgress()) {
 	    			final float dx = x - mLastTouchX;
 	    			final float dy = y - mLastTouchY;
 
 	    			mPosX += dx;
 	    			mPosY += dy;
 
 	    			invalidate();
 	    		}
 
 	    		mLastTouchX = x;
 	    		mLastTouchY = y;
 
 	    		break;
 	    	}
 	        
 	    	case MotionEvent.ACTION_UP: {
 	    		mActivePointerId = INVALID_POINTER_ID;
 	    		break;
 	    	}
 	        
 	    	case MotionEvent.ACTION_CANCEL: {
 	    		mActivePointerId = INVALID_POINTER_ID;
 	    		break;
 	    	}
 	    
 	    	case MotionEvent.ACTION_POINTER_UP: {
 	    		final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) 
 	                >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
 	                final int pointerId = event.getPointerId(pointerIndex);
 	                if (pointerId == mActivePointerId) {
 	                	// This was our active pointer going up. Choose a new
 	                	// active pointer and adjust accordingly.
 	                	final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
 	                	mLastTouchX = event.getX(newPointerIndex);
 	                	mLastTouchY = event.getY(newPointerIndex);
 	                	mActivePointerId = event.getPointerId(newPointerIndex);
 	                }	
 	                break;
 	    	}
 	    }
 	    
 	    return true;
 	}*/
 
 	private void init() {
 		
         mSymbol = new Path();
         mSymbolRotated = new Path();
         mPaint = new Paint();
         mPaintNode = new Paint();
         
         //Initialize Nodes.
         node1 = new Point(0,0);
 		node2 = new Point(0,0);
 		
 		//Initialize resistorOrientation.
         resistorOrientation = 0;
         
         //Set up paint for the resistor.
         mPaint.setAntiAlias(true);		
 		mPaint.setStrokeWidth(2);
 		mPaint.setColor(-7829368);		
 		mPaint.setStyle(Paint.Style.STROKE);	
          
 		//Set up paint for the node.
 		mPaintNode.setAntiAlias(true);
 		mPaintNode.setStrokeWidth(2);
 		mPaintNode.setColor(-7829368);
 		mPaintNode.setStyle(Paint.Style.FILL);
 		
         //Path to draw the symbol. Normally.
         
         mSymbol.moveTo(0.0F, 0.0F);
 	    mSymbol.lineTo(0.0F, 50.0F);
 	    mSymbol.lineTo(16.666666F, 58.333332F);
 	    mSymbol.lineTo(-16.666666F, 75.0F);
 	    mSymbol.lineTo(16.666666F, 91.666664F);
 	    mSymbol.lineTo(-16.666666F, 108.33333F);
 	    mSymbol.lineTo(16.666666F, 124.99999F);
 	    mSymbol.lineTo(-16.666666F, 141.66666F);
 	    mSymbol.lineTo(0.0F, 150.0F);
 	    mSymbol.lineTo(0.0F, 200.0F);
 	    mSymbol.offset(20, 10);	
 	    
 	    //Path to draw the symbol. Rotated.
 	    mSymbolRotated.moveTo(0.0F, 0.0F);
 	    mSymbolRotated.lineTo(0.0F, 50.0F);
 	    mSymbolRotated.lineTo(16.666666F, 58.333332F);
 	    mSymbolRotated.lineTo(-16.666666F, 75.0F);
 	    mSymbolRotated.lineTo(16.666666F, 91.666664F);
 	    mSymbolRotated.lineTo(-16.666666F, 108.33333F);
 	    mSymbolRotated.lineTo(16.666666F, 124.99999F);
 	    mSymbolRotated.lineTo(-16.666666F, 141.66666F);
 	    mSymbolRotated.lineTo(0.0F, 150.0F);
 	    mSymbolRotated.lineTo(0.0F, 200.0F);
 	    mSymbolRotated.offset(10, 20);
 	    
 	    
 	  
 	    
     }
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		
 		super.onDraw(canvas);
 		//canvas.save();	
 		
 		//Scale the canvas.
 		canvas.scale(Global.mScaleFactor, Global.mScaleFactor);	
 		
 		
 		RelativeLayout.LayoutParams tempLayoutParams = (RelativeLayout.LayoutParams) this.getLayoutParams();
 		
 		/*
 		tempLayoutParams.width = (int) (Global.mScaleFactor * 40);
 		tempLayoutParams.height = (int) (Global.mScaleFactor * 220);
 		
 		this.setLayoutParams(tempLayoutParams);
 		*/
 		
 		//Rotate the resistor.
 		if (resistorOrientation == 1){			
 			canvas.save();
 			tempLayoutParams.width = (int) (Global.mScaleFactor * 220);
 			tempLayoutParams.height = (int) (Global.mScaleFactor * 40);
 			this.setLayoutParams(tempLayoutParams);	
 			canvas.rotate(-90, 10, 20);
 			//Draw the resistor.			
 			canvas.drawPath(mSymbolRotated, mPaint);
 			canvas.restore();
 			//Draw the nodes.
 			canvas.drawCircle(10, 20, 10, mPaintNode);
 			canvas.drawCircle(210, 20, 10, mPaintNode);
 			
 			
 		}else {			
 			
 			canvas.drawPath(mSymbol, mPaint);
 			tempLayoutParams.width = (int) (Global.mScaleFactor * 40);
 			tempLayoutParams.height = (int) (Global.mScaleFactor * 220);
 			this.setLayoutParams(tempLayoutParams);
 			canvas.drawPath(mSymbol, mPaint);
 			//Draw the nodes.
 			canvas.drawCircle(20, 10, 10, mPaintNode);
 			canvas.drawCircle(20, 210, 10, mPaintNode);
 			
 		}		
 		
 		//Log.d("ResistorView", "is focused? " + this.isSelected());
 		if(this.isSelected()){	        		
     		mPaint.setColor(Color.YELLOW);
             mPaintNode.setColor(Color.YELLOW);
     	}else {	        		
     		mPaint.setColor(-7829368);
         	mPaintNode.setColor(-7829368);
     	}
 		
 		
 		//Draw the nodes.
 		//canvas.drawCircle(20, 10, 10, mPaintNode);
 		//canvas.drawCircle(20, 205, 10, mPaintNode);
 		
 		//Draw the resistor.
 		//canvas.drawPath(mSymbol, mPaint);
 		//canvas.restore();
 		
 		//Log.d("ResistorView", "Width " + tempLayoutParams.width + " Height " + tempLayoutParams.height);
 	}
 	
 	public void setOrientation(){
 		
 		//Log.d("setOrientation", "Orientation " + resistorOrientation + " selected " + this.isSelected());
                 
         
 		
 		if((resistorOrientation == 0)){
 			
 			resistorOrientation = 1;
 			/*this.startAnimation(ccw);
 	
 			
 			if (ccw.hasEnded()){
 				ccw.cancel();
 				ccw.reset();
 			}*/
 			
 		}else if ((resistorOrientation == 1)){
 			
 			resistorOrientation = 0; 
 			/*this.startAnimation(cw);
 			if (cw.hasEnded()){
 				cw.cancel();
 				cw.reset();
 			}*/
 		}
         
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		// TODO Auto-generated method stub
 		
 		//Log.d("onTouch", "ID: " + v.getId() + " Is focused " + v.isFocused());
 		
 		//float draggedInitialY = 0;
 		//float draggedInitialX = 0;
 		
 		final int X = (int) event.getRawX();
 	    final int Y = (int) event.getRawY();
 	    
 		switch (event.getAction() & MotionEvent.ACTION_MASK) {
         case MotionEvent.ACTION_DOWN:
         	mode = DRAG;
         	//draggedInitialY = event.getRawY();
         	//draggedInitialX = event.getRawX();
         	//touchDown = new PointF(event.getRawX(), event.getRawY());
         	
         	RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) this.getLayoutParams();
             _xDelta = X - lParams.leftMargin;
             _yDelta = Y - lParams.topMargin;
             
             //Log.d("onTouch", "X " + X + " Y " + Y);
             //Log.d("onTouch", "leftMargin " + lParams.leftMargin);
             mPaint.setColor(Color.YELLOW);
             mPaintNode.setColor(Color.YELLOW);             
             this.setSelected(true);
             break;
         case MotionEvent.ACTION_UP:	        	
         	Global.setAlphaWhenMoving(false);
         	mode = NONE;
         	
         	//Trying some snaping
         	RelativeLayout.LayoutParams snapParams = (RelativeLayout.LayoutParams) this.getLayoutParams();
         	
         	snapCase = mainActivity.checkSnap(this);
         	//Log.d("onTouch", "Snap Case " + snapCase);
         	
         	if (snapCase == 0){
         		
         		if(resistorOrientation == 0){
             		
             		snapParams.leftMargin = ((int) (Math.round((snapParams.leftMargin / 20.0)))) * 20;
                 	snapParams.topMargin = ((int) (Math.round((snapParams.topMargin / 20.0)))) * 20;
                 	//snapParams.bottomMargin = (int) (Math.round((Y / 20)) * 20);
             	} else {
             		
             		snapParams.leftMargin = ((int) (Math.round((snapParams.leftMargin / 10.0)))) * 10;
                 	snapParams.topMargin = ((int) (Math.round((snapParams.topMargin / 10.0)))) * 10;
                 	//snapParams.topMargin = 50;
             	}
         		
         	}       	
         	
         	
         	
         	//Log.d("onTouch", "leftMargin " + snapParams.leftMargin);
         	//Log.d("onTouch", "topMargin " + snapParams.topMargin);
         	//Log.d("onTouch", "X - 40 is " + (X - 40) + " round X " + Math.round((X - 40) / 20.0));
         	//Log.d("onTouch", "Y - 220 is " + (Y - 220) + " round Y " + Math.round((Y - 220) / 20.0));
         	//Log.d("onTouch", "------------------------------------------------------------------");
         	//Log.d("onTouch", "Node 1 " + node1);
         	//Log.d("onTouch", "Node 2 " + node2);
 
         	this.setLayoutParams(snapParams);
         	break;
 		case MotionEvent.ACTION_POINTER_DOWN:
         	mode = ZOOM;        	
         	this.setSelected(false);
             break;
         case MotionEvent.ACTION_POINTER_UP:
         	mode = NONE;
             break;
         case MotionEvent.ACTION_MOVE:
         	
         	//Update Resistor position
         	RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.getLayoutParams();
             layoutParams.leftMargin = X - _xDelta ;	            
             //Log.d("Right", "distance from right is" + this.getRight());
             layoutParams.topMargin = Y - _yDelta;	            
             layoutParams.rightMargin = -250;
             layoutParams.bottomMargin = -250;
             this.setLayoutParams(layoutParams);
             
     		//Update Nodes positions. 
     		if (resistorOrientation == 0){
     			
     			node1.x = layoutParams.leftMargin + 20;
     			node1.y = layoutParams.topMargin + 10;
     			node2.x = layoutParams.leftMargin + 20;
     			node2.y = layoutParams.topMargin + 210;
     		} else {
     			
     			node1.x = layoutParams.leftMargin + 10;
     			node1.y = layoutParams.topMargin + 20;
     			node2.x = layoutParams.leftMargin + 210;
     			node2.y = layoutParams.topMargin + 20;
     		}
             
             Global.setAlphaWhenMoving(true);	            
             
             break;
 		}
 		
 		/*if(root == null) {
 		    root = (RelativeLayout)getParent();
 		}
 		root.invalidate();
 		*/
 		
 		return true;
 	}
 
 
 
 
 
 
 
 	
 	/*private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
 
 		@Override
 		public boolean onScale(ScaleGestureDetector detector) {
 			mScaleFactor *= detector.getScaleFactor();
 	        
 	        // Don't let the object get too small or too large.
 	        mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));
 	        
 	        return true;
 
 			}
 	   
 	    }*/
 
 	
 	//...Override onMeasure()...
     /*@Override
     public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         //Use this method to tell Android how big your view is
     	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
         //int parentWidth = MeasureSpec.getSize(widthMeasureSpec);     
         //int parentHeight = MeasureSpec.getSize(heightMeasureSpec);      
         //int newH = (int) (parentHeight / 3.0f);
         //int newW = (int) (parentWidth / 3.0f);
     	//final int desiredHSpec = MeasureSpec.makeMeasureSpec(heightMeasureSpec, MeasureSpec.UNSPECIFIED);
     	//final int desiredWSpec = MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.UNSPECIFIED);
     	setMeasuredDimension(40, 200);
     	
     }*/
 	
 
 }
