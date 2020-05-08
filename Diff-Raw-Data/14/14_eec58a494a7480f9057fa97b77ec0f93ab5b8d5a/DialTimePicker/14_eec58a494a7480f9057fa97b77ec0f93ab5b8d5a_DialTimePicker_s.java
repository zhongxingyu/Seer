 /*
  * Waker project 2013
  * 
  * folks studio
  * 
  * by caifangmao8@gmail.com
  */
 package com.cfm.waker.widget;
 
 import com.cfm.waker.R;
 import com.cfm.waker.log.WLog;
 import com.cfm.waker.util.DensityUtil;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.RectF;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class DialTimePicker extends View{
 	
 	private static final String TAG = "DialTimePicker";
 	
 	private int mediumCircleRange;
 	
 	private int thumbPressRange;
 	
 	private int outerPressRange;
 	private int innerPressRange;
 	
 	private int circleWidth;
 	
 	private Point centerPoint;
 	
 	private boolean isDrawPressPoint;
 	private boolean isCirclePressed;
 	private Point drawPoint;
 	
 	private Drawable backgroundDrawable;
 	private int backgroundRange;
 	private int arcDrawOffset;
 	private int drawDegree;
 	
 	private Integer increment;
 	
 	private Paint paint;
 	
 	private RectF arcBound;
 	
 	private OnTimePickListener mOnTimePickListener;
 	
 	private boolean convert;
 	
 	public interface OnTimePickListener{
 		
 		public void onStartPick();
 		public void onPick(int value, int increment);
 		public void onStopPick();
 	}
 
 	public DialTimePicker(Context context){
 		this(context, null);
 	}
 	
 	public DialTimePicker(Context context, AttributeSet attrs){
 		this(context, attrs, 0);
 	}
 	
 	public DialTimePicker(Context context, AttributeSet attrs, int defStyle){
 		super(context, attrs, defStyle);
 		
 		circleWidth = (int) context.getResources().getDimension(R.dimen.dialtimepicker_default_circlewidth);
 		
 		thumbPressRange = (int) context.getResources().getDimension(R.dimen.dialtimepicker_default_thumbpressrange);
 		
 		isDrawPressPoint = false;
 		isCirclePressed = false;
 		
 		centerPoint = new Point();
 		drawPoint = new Point();
 		
 		backgroundDrawable = context.getResources().getDrawable(R.drawable.background_dialtimepicker);
 		arcDrawOffset = (int) context.getResources().getDimension(R.dimen.dialtimepicker_default_arcdrawoffset);
 		
 		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		
 		arcBound = new RectF();
 		
 		convert = false;
 	}
 	
 	public void setOnTimePickListener(OnTimePickListener mOnTimePickListener){
 		this.mOnTimePickListener = mOnTimePickListener;
 	}
 	
 	public OnTimePickListener getOnTimePickListener(){
 		return mOnTimePickListener;
 	}
 	
 	@Override
 	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
 		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 		
 		centerPoint.x = getMeasuredWidth() / 2;
 		centerPoint.y = getMeasuredHeight() / 2;
 		
 		backgroundRange = getMeasuredWidth() > getMeasuredHeight() ? getMeasuredHeight() : getMeasuredWidth();
 		
 		int arcWidth = (int) (backgroundRange * 0.7F) / 2;
 		int arcHeight = (int) (backgroundRange * 0.7F) / 2;
 		
 		mediumCircleRange = (int) (backgroundRange * 0.675F) / 2;
 		
 		outerPressRange = mediumCircleRange + thumbPressRange / 2;
 		innerPressRange = mediumCircleRange - thumbPressRange / 2;
 		
 		arcBound.top = centerPoint.y - arcHeight;
 		arcBound.left = centerPoint.x - arcWidth;
 		arcBound.right = centerPoint.x + arcWidth;
 		arcBound.bottom = centerPoint.y + arcHeight;
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event){
 		final int action = event.getAction();
 		
 		switch(action){
 		case MotionEvent.ACTION_DOWN:
 			float x = event.getX() - centerPoint.x;
 			float y = event.getY() - centerPoint.y;
 			int pressedRange = (int) Math.sqrt(x * x + y * y);
 			//calculate the pressedRange to make sure it's in the clickable circle
 			if(outerPressRange > pressedRange && pressedRange > innerPressRange){
 				isDrawPressPoint = true;
 				isCirclePressed = true;
 				increment = null;
 				if(null != mOnTimePickListener) mOnTimePickListener.onStartPick();
 			}
 			break;
 		case MotionEvent.ACTION_MOVE:
 			if(isDrawPressPoint){
 				getParent().requestDisallowInterceptTouchEvent(true);
 				double dx = event.getX() - centerPoint.x;
 				double dy = event.getY() - centerPoint.y;
 				double degree = Math.atan2(dy, dx);
 				drawPoint = centerRadiusPoint(centerPoint, degree, mediumCircleRange);
 				performDial(get360Angel(degree));
 				
 				int tDegree = (int) Math.toDegrees(degree);
 				tDegree = tDegree < 0 ? 180 + (180 + tDegree) : tDegree;
 				
 				int incrementValue = 0;
 				if(null == increment){
 					increment = tDegree;
 				}else{
 					incrementValue = tDegree - increment;
 					int positiveIncrement = Math.abs(incrementValue);
 					if(positiveIncrement > 180)
 						incrementValue = -(incrementValue / positiveIncrement) * (360 - positiveIncrement);
 				}
 				if(null != mOnTimePickListener) mOnTimePickListener.onPick(tDegree, incrementValue);
 				increment = tDegree;
 				invalidate();
 			}
 			break;
 		case MotionEvent.ACTION_UP:
 		case MotionEvent.ACTION_CANCEL:
 			isDrawPressPoint = false;
 			isCirclePressed = false;
 			if(null != mOnTimePickListener) mOnTimePickListener.onStopPick();
 			invalidate();
 			break;
 		}
 		
 		return isCirclePressed;
 	}
 	
 	//perform a dial action ever there's no touch event input
 	public void performDial(int angel){
 		
		int angelOffset = 5;
 		
 		if(drawDegree > 360 - angelOffset && angel < angelOffset || angel > 360 - angelOffset && drawDegree < angelOffset){
 			convert = !convert;
 		}
 		
 		drawDegree = angel;
 		
 		double realAngle = angel - 90;
 		if(realAngle >= 180 && realAngle < 270){
 			realAngle = angel - (360 + 90);
 		}
 		realAngle = Math.toRadians(realAngle);
 		isDrawPressPoint = true;
 		drawPoint = centerRadiusPoint(centerPoint, realAngle, mediumCircleRange);
 		invalidate();
 	}
 	
 	public void endPerformDial(){
 		isDrawPressPoint = false;
 		invalidate();
 	}
 	
 	@Override
 	public void onDraw(Canvas canvas){
 		super.onDraw(canvas);
 		
 		if(isDrawPressPoint){
 			paint.setColor(0xFF5CA4E5);
 			if(convert){
 				canvas.drawArc(arcBound, drawDegree - 90, 360 - drawDegree, true, paint);
 			}else{
 				canvas.drawArc(arcBound, -90, drawDegree, true, paint);
 			}
 			
 		}
 		
 		backgroundDrawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
 		backgroundDrawable.draw(canvas);
 		
 		if(isDrawPressPoint){
 			paint.setColor(0x44FFFFFF);
 			canvas.drawCircle(drawPoint.x, drawPoint.y, circleWidth * 3, paint);
 			paint.setColor(0x99FFFFFF);
 			canvas.drawCircle(drawPoint.x, drawPoint.y, circleWidth * 2, paint);
 		}
 	}
 	
 	private Point centerRadiusPoint(Point center, double angle, double radius){
 		Point p = new Point();
 		p.x = (int) (radius * Math.cos(angle)) + center.x;
 		p.y = (int) (radius * Math.sin(angle)) + center.y;
 		
 		return p;
 	}
 	
 	private int get360Angel(double angel){
 		angel = Math.toDegrees(angel);
 		return (int) (angel <= -90 && angel >= -180 ? 450 + angel : angel + 90);
 	}
 }
