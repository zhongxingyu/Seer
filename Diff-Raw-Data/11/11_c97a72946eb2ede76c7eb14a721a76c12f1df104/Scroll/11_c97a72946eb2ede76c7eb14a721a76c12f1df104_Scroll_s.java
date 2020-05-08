 package com.thoughtworks.thoughtferret.view;
 
 import com.thoughtworks.thoughtferret.MathUtils;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.util.AttributeSet;
 import android.view.Display;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.GestureDetector.OnGestureListener;
 import android.widget.Scroller;
 
 public class Scroll extends View implements OnGestureListener {
 
 	private Rect fullSize = new Rect(); 
 	private Point currentScroll = new Point();
 	
 	private Scroller mScroller;
 	private GestureDetector mGestureDetector;
 	
 	private float flingSpeed = 0.75f;
 	
     protected Display display;
     
     private Paint fpsPaint;
     private Paint borderPaint;
 	
 	public Scroll(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		mScroller = new Scroller(context);
 		
 		mGestureDetector = new GestureDetector(this);
 		mGestureDetector.setIsLongpressEnabled(false);
 	
 		display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
 		
         fpsPaint = new Paint() {{
 			setStyle(Paint.Style.STROKE);
 			setAntiAlias(true);
 			setStrokeWidth(1.0f);
 			setStrokeCap(Cap.BUTT);
 			setColor(0xFFFFFFFF);
 		}};
 		
         borderPaint = new Paint() {{
 			setStyle(Paint.Style.STROKE);
 			setAntiAlias(true);
 			setStrokeWidth(3.0f);
 			setStrokeCap(Cap.SQUARE);
 			setColor(0xFF0000);
 		}};
 	}
 	
 	public void setFullSize(Rect fullSize) {
 		this.fullSize = fullSize;
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 		canvas.save();
 		
 		if (mScroller.computeScrollOffset()) {
 			currentScroll.x = mScroller.getCurrX();
 			currentScroll.y = mScroller.getCurrY();
 			invalidate();
 		}
 		
 		float dx = currentScroll.x - getWidth() * (currentScroll.x / fullSize.width());
 		float dy = currentScroll.y - getHeight() * (currentScroll.y / fullSize.height());
 		canvas.translate(dx, dy);
 
 		Rect visibleRect = MathUtils.getRect(currentScroll, getWidth(), getHeight());
 		drawFullCanvas(canvas, visibleRect);
 		
 		canvas.restore();
 	}
 	
 	protected void drawFullCanvas(Canvas canvas, Rect visibleRect) {
 		canvas.drawRect(fullSize, borderPaint);
     	String fps = String.format("%d fps", (int)display.getRefreshRate());
     	canvas.drawText(fps.toString(), visibleRect.left + 20, visibleRect.top + 20, fpsPaint);
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		return mGestureDetector.onTouchEvent(event);
 	}
 
 	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
 		currentScroll.x -= distanceX;
 		currentScroll.y -= distanceY;
		currentScroll.x = Math.max(-fullSize.width(), Math.min(0, currentScroll.x));
		currentScroll.y = Math.max(-fullSize.height(), Math.min(0, currentScroll.y));
 		invalidate();
 		return true;
 	}
 	
 	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
 		velocityX *= flingSpeed;
 		velocityY *= flingSpeed;
 		
 		mScroller.fling(currentScroll.x, currentScroll.y, (int) velocityX, (int) velocityY, -fullSize.width(), 0, -fullSize.height(), 0);
 		invalidate();
 		return true;
 	}
 
 	public void onLongPress(MotionEvent e) {
 	}
 
 	public void onShowPress(MotionEvent e) {
 	}
 	
 	public boolean onDown(MotionEvent e) {
 		return true;
 	}
 
 	public boolean onSingleTapUp(MotionEvent e) {
 		mScroller.forceFinished(true);
 		return true;
 	}
 
 }
