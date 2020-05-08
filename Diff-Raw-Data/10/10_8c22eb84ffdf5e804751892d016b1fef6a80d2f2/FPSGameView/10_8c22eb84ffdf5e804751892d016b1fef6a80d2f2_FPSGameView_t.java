 package com.example.wiiphone;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.PointF;
 import android.graphics.drawable.ShapeDrawable;
 import android.graphics.drawable.shapes.OvalShape;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class FPSGameView extends View
 {
 	private static final int AXIS_X = 0;
 	private static final int AXIS_Y = 1;
 	private ShapeDrawable mDrawableCircleInner = null;
 	private ShapeDrawable mDrawableCircleOuter = null;
 	private int mSwingMultiplier = 10;
 	private int mXPos = 50;
 	private int mYPos = 250;
 	private int mCircleWidth = 175;
 	private TCPClient mTcpClient = null;
 	private float mMaxLength = 150.0f;
 	
 	public FPSGameView(Context context, AttributeSet attrs) 
 	{
 		super(context, attrs);
 		
 		mXPos = (int) 300;
 		mYPos = (int) 310;
 		mCircleWidth = 200;
 		
 		mDrawableCircleInner = new ShapeDrawable(new OvalShape());
     	mDrawableCircleInner.getPaint().setARGB(255,255,0,0);
     	mDrawableCircleInner.setBounds(mXPos - (mCircleWidth / 2), mYPos - (mCircleWidth / 2),
     			mXPos + (mCircleWidth) - (mCircleWidth / 2), mYPos + (mCircleWidth) - (mCircleWidth / 2));
     	
     	mDrawableCircleOuter = new ShapeDrawable(new OvalShape());
     	mDrawableCircleOuter.getPaint().setARGB(255,0,255,0);
     	mDrawableCircleOuter.getPaint().setStyle(Paint.Style.STROKE);
     	mDrawableCircleOuter.getPaint().setStrokeWidth(10);
     	mDrawableCircleOuter.setBounds(mXPos - 5 - (mCircleWidth / 2), mYPos - 5 - (mCircleWidth / 2),
     			mXPos + mCircleWidth + 5 - (mCircleWidth / 2), mYPos + mCircleWidth + 5 - (mCircleWidth / 2));
 		
 		/*mDrawableInner = new ShapeDrawable(new RectShape());
 		mDrawableInner.getPaint().setARGB(255,255,0,0);
 		mDrawableInner.setBounds(mXPos, mYPos, mXPos + mWidth, mYPos + mHeight);
 		
 		mDrawableOuter = new ShapeDrawable(new RectShape());
 		mDrawableOuter.setBounds(mXPos, mYPos, mXPos + mWidth, mYPos + mHeight);
         mDrawableOuter.getPaint().setARGB(255,255,255,255);
         mDrawableOuter.getPaint().setStyle(Paint.Style.STROKE);
 		mDrawableOuter.getPaint().setStrokeWidth(10);
 		
 		mDrawCircle = new ShapeDrawable(new OvalShape());
 		mDrawCircle.getPaint().setARGB(255,0,0,255);
 		mDrawCircle.setBounds(mSteeringCircle, mYPos , mSteeringCircle + mCircleWidth, mYPos + mCircleWidth);*/
 	}
 	
 	public void setTcpClient( TCPClient tcp )
     {
     	mTcpClient = tcp;
     }
 	
 	public void InvalidateView(float x, float y, float z)
     {
     	/*int mMiddleX = super.getWidth() / 2;
     	int mMiddleY = super.getHeight() / 4;
     	postInvalidate();
     	int width = 100;
     	int height = 100;
     	
     	int lY = (int)x * mSwingMultiplier + mMiddleY - (height / 2);
     	int lX = (int)y * mSwingMultiplier + mMiddleX - (width / 2);*/
     	
     	
     	
         postInvalidate();
     }
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) 
 	{
 		PointF joyVector = new PointF(0.0f ,0.0f);
 		PointF joySMid = new PointF(0.0f ,0.0f);
 		PointF pos = new PointF(0.0f ,0.0f);
		if(event.getAxisValue(AXIS_X) > super.getWidth() / 2)
		{
			joyVector = new PointF(mXPos, mYPos);
		}
		else if(event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN)  
 		{
 			joySMid = new PointF(mXPos, mYPos);
 			pos = new PointF(event.getAxisValue(AXIS_X),
 	    			event.getAxisValue(AXIS_Y));
 	    	joyVector = new PointF(pos.x - joySMid.x , pos.y - joySMid.y);
 	    	
 	    	if(joyVector.length() > mMaxLength)
 	    	{
 	    		float length = joyVector.length();
 	    		joyVector.x /= length;
 	    		joyVector.y /= length;
 	    		
 	    		joyVector.x *= mMaxLength;
 	    		joyVector.y *= mMaxLength;
 	    	}
 	    	joyVector.x += mXPos;
 	    	joyVector.y += mYPos;
 		}
		else if(event.getAction() == MotionEvent.ACTION_UP)
 		{
 			joyVector = new PointF(mXPos, mYPos);
 		}
 		if(mTcpClient != null)
 		{
 			String message = "AIM " + (joyVector.x - mXPos) + " " + (joyVector.y - mYPos) + " ";
 			mTcpClient.sendMessage(message);
 		}
 		mDrawableCircleInner.setBounds((int) (joyVector.x) - (mCircleWidth / 2), (int) (joyVector.y) - (mCircleWidth / 2) , 
 				(int) (joyVector.x + mCircleWidth) - (mCircleWidth / 2), (int) (joyVector.y + mCircleWidth) - (mCircleWidth / 2));
 		postInvalidate();
 		
 		return true;
 	} 
 	@Override
 	protected void onDraw(Canvas canvas) 
 	{
 		super.onDraw(canvas);
 		
 		/*if(mDrawableInner != null)
 		{
 			mDrawableInner.draw(canvas);
 		}
 		if(mDrawableOuter != null)
 		{
 			mDrawableOuter.draw(canvas);
 		}
 		if(mDrawCircle != null)
 		{
 			mDrawCircle.draw(canvas);
 		}*/
 		if(mDrawableCircleInner != null)
 		{
 			mDrawableCircleInner.draw(canvas);
 		}
 		if(mDrawableCircleOuter != null)
 		{
 			mDrawableCircleOuter.draw(canvas);
 		}
 	}
 }
