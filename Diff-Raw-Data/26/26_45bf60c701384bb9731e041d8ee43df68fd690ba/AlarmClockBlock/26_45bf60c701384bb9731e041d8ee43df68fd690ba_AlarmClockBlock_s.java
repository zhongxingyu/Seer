 package com.cfm.waker.widget;
 
 import java.lang.Runnable;
 import java.util.Calendar;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.Typeface;
 import android.os.Handler;
 import android.text.TextPaint;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 
 import com.cfm.waker.R;
 import com.cfm.waker.entity.Alarm;
 
 public class AlarmClockBlock extends View {
 	
 	private static final String TAG = "AlarmClockBlock";
 	
 	private Context context;
 	
 	private static final int TOUCHMODE_IDLE = 0;
 	private static final int TOUCHMODE_DOWN = 1;
 	private static final int TOUCHMODE_DRAGGING = 2;
 	
 	private int touchMode;
 	
 	private Alarm alarm;
 	
 	private int width, height;
 	private int centerX, centerY;
 	private int radius;
 	private float dx, dy;
 	private float moveX, moveY;
 	
 	private boolean enabled;
 	
 	private Paint paint;
 	private TextPaint textPaint;
 	
 	private int color;
 	
 	private Rect bound;
 	private Rect bound2;
 	
 	private Handler handler;
 	private MyRunnable runnable;
 	
 	private OnStateChangeListener onStateChangeListener;
 	
 	public interface OnStateChangeListener{
 		public void onStateChanged(long id, boolean isEnabled);
 	}
 	
 	public AlarmClockBlock(Context context){
 		this(context, null);
 	}
 	
 	public AlarmClockBlock(Context context, AttributeSet attrs){
 		this(context, attrs, 0);
 	}
 	
 	public AlarmClockBlock(Context context, AttributeSet attrs, int defStyle){
 		super(context, attrs, defStyle);
 		
 		this.context = context;
 		
 		enabled = false;
 		
 		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
 		
 		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AlarmClockBlock);
 		if(null != ta.getString(R.styleable.AlarmClockBlock_typeface)){
 			Typeface typeface = Typeface.createFromAsset(context.getAssets(), ta.getString(R.styleable.AlarmClockBlock_typeface));
 			textPaint.setTypeface(typeface);
 		}
 		
 		color = ta.getColor(R.styleable.AlarmClockBlock_color, 0xFFFFFFFF);
 		
 		ta.recycle();
 		
 		textPaint.setColor(0xFFFFFFFF);
 		
 		bound = new Rect();
 		bound2 = new Rect();
 		
 	}
 	
 	public void setAlarm(Calendar calendar, boolean is24Format){
 		alarm = new Alarm(calendar, is24Format);
 		enabled = alarm.isEnabled();
 	}
 	
 	public void setAlarm(Alarm alarm){
 		this.alarm = alarm;
 		enabled = alarm.isEnabled();
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event){
 		switch(event.getAction() & MotionEvent.ACTION_MASK){
 		case MotionEvent.ACTION_DOWN:
 			Log.d(TAG,"MOTIONEVENT_DOWN");
 			dx = centerX - event.getX();
 			dy = centerY - event.getY();
 			
 		    if(event.getY() < height){
 		    	touchMode = TOUCHMODE_DOWN;
 			    return true;
 		    }
 			
 		case MotionEvent.ACTION_MOVE:
 			Log.d(TAG,"MOTIONEVENT_MOVE" + moveY);
 			switch(touchMode){
 			case TOUCHMODE_IDLE:
 				break;
 			case TOUCHMODE_DOWN:
 				if(Math.abs(centerX - dx - event.getX()) < Math.abs(centerY - dy - event.getY())){
 					touchMode = TOUCHMODE_DRAGGING;
 					getParent().requestDisallowInterceptTouchEvent(true);
 				}
 				break;
 			case TOUCHMODE_DRAGGING:
 				moveX = centerX;
 				moveY = dy + event.getY();
 				if(moveY > centerY && moveY <= centerY + radius * 2){
 					invalidate();
 				}else if(moveY > centerY + radius * 2){
 					touchMode = TOUCHMODE_IDLE;
 					enabled = !enabled;
 					alarm.setEnabled(enabled);
					onStateChangeListener.onStateChanged(alarm.getId(), enabled);
 					returnToOriginalSpot();
 				}
 				
 			}
 			break;
 		case MotionEvent.ACTION_UP:
 		case MotionEvent.ACTION_CANCEL:
 			Log.d(TAG,"MOTIONEVENT_UP & MOTIONEVENT_CANCEL");
 			touchMode = TOUCHMODE_IDLE;
 			returnToOriginalSpot();
 			break;
 		}
 		
 		return true;
 	}
 	
 	private void returnToOriginalSpot(){
 		handler = new Handler();
 		runnable = new MyRunnable();
 		
 		handler.post(runnable);
 		
 	}
 	
 	private class MyRunnable implements Runnable{
 		@Override
 		public void run(){
 			if(touchMode == TOUCHMODE_IDLE && moveY != centerY){
 				moveY += (centerY - moveY) * 0.6F;
 				invalidate();
 				
 				handler.postDelayed(runnable, 20);
 			}
 		}
 	}
 	
 	@Override
 	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
 		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 		
 		width = getMeasuredWidth();
 		height = getMeasuredHeight();
 		
 		centerX = width / 2;
 		centerY = height / 2;
 		
 		radius = (int) (height * 0.4F);
 	    
 	    super.setMeasuredDimension(width, height * 2);
 	}
 	
 	@Override
 	public void onDraw(Canvas canvas){
 		super.onDraw(canvas);
 		
 		moveX = moveX == 0 ? centerX : moveX;
 		moveY = moveY == 0 ? centerY : moveY;
 		
 		/*
 		//for background
 		paint.setColor(color);
 		paint.setStyle(Paint.Style.FILL);
 		bound.top = 0;
 		bound.left = 0;
 		bound.right = width;
 		bound.bottom = height;
 		canvas.drawRect(bound, paint);
 		 */
 		
 		paint.setColor(enabled ? color : 0xFF999999);
 		paint.setStyle(Paint.Style.STROKE);
 		paint.setStrokeWidth(2);
 		canvas.drawCircle(moveX, moveY, radius, paint);
 		String text = alarm.getFormatedTime();
 		if(null != text){
 			textPaint.setTextSize(width * 0.25F);
 			textPaint.getTextBounds(text, 0, text.length(), bound);
 			
 			canvas.drawText(text, moveX - bound.width() / 2, moveY + bound.height() / 2, textPaint);
 			if(!alarm.is24Format()){
 				text = context.getString(alarm.getAmpmRes());
 				textPaint.setTextSize(textPaint.getTextSize() / 2);
 				textPaint.getTextBounds(text, 0, text.length(), bound2);
 				
 				canvas.drawText(text, moveX - bound2.width() / 2, moveY + bound.height() / 2 + bound2.height() * 2, textPaint);
 			}
 		}
 	}
 }
