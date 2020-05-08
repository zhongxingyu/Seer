 package steve4448.livetextbackground.widget;
 
 import steve4448.livetextbackground.R;
 import steve4448.livetextbackground.widget.listener.OnMinMaxBarChangeListener;
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class MinMaxBar extends View {
 	public static final int MAX_CIRCLE_RADIUS = 24;
 	public static final int BAR_COLOR = Color.argb(100, 60, 60, 60);
 	public static final int THUMB_COLOR = Color.argb(100, 0, 166, 255);
 	public static final int THUMB_COLOR_PRESSED = Color.argb(200, 0, 166, 255);
 	public static final int THUMB_OUTER_RING_COLOR = Color.argb(200, 0, 80, 255);
 	public static final int THUMB_INNER_COLOR = Color.argb(200, 255, 255, 255);
 	public static final int THUMB_INNER_COLOR_PRESSED = Color.argb(255, 200, 200, 200);
 	public static final int THUMB_INNER_RING_COLOR = Color.argb(200, 55, 55, 55);
 	
 	private Paint paint = new Paint();
 	
 	private OnMinMaxBarChangeListener onMinMaxBarChangeListener;
 	
 	private float locationMinXThumb = 0;
 	private float locationMaxXThumb = 24;
 	
 	private float absoluteMinimum = 0, actualMinimum = 0;
 	private float absoluteMaximum = 100, actualMaximum = 0;
 	
 	private boolean draggingMinXThumb = false;
 	private boolean draggingMaxXThumb = false;
 	
 	public MinMaxBar(Context context) {
 		super(context);
 		init(context, null, 0);
 	}
 	
 	public MinMaxBar(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init(context, attrs, 0);
 	}
 	
 	public MinMaxBar(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init(context, attrs, defStyle);
 	}
 	
 	public void init(Context context, AttributeSet attrs, int defStyle) {
 		paint.setAntiAlias(true);
 		if(attrs != null) {
 			TypedArray extraAttrs = context.obtainStyledAttributes(attrs, R.styleable.MinMaxBar);
 			actualMinimum = extraAttrs.getFloat(R.styleable.MinMaxBar_minDefault, 0);
 			actualMaximum = extraAttrs.getFloat(R.styleable.MinMaxBar_maxDefault, 100);
 			absoluteMinimum = extraAttrs.getFloat(R.styleable.MinMaxBar_min, actualMinimum);
 			absoluteMaximum = extraAttrs.getFloat(R.styleable.MinMaxBar_max, actualMaximum);
 			extraAttrs.recycle();
 		}
 	}
 	
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 		this.setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), 60);
 	}
 	
 	@Override
 	public void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
		locationMinXThumb = actualMinimum == 0 ? 0 : (((actualMinimum - absoluteMinimum) / (absoluteMaximum - absoluteMinimum)) * (getWidth() - MAX_CIRCLE_RADIUS * 2));
		locationMaxXThumb = actualMaximum == 0 ? 0 : (((actualMaximum - absoluteMinimum) / (absoluteMaximum - absoluteMinimum)) * (getWidth() - MAX_CIRCLE_RADIUS * 2));
 		canvas.translate(MAX_CIRCLE_RADIUS, 0);
 		paint.setColor(BAR_COLOR);
 		canvas.drawRect(0, getHeight() / 2 - getHeight() / 6, getWidth() - (MAX_CIRCLE_RADIUS * 2), getHeight() / 2 + getHeight() / 6, paint);
 		if(isEnabled()) {
 			paint.setColor(draggingMinXThumb ? THUMB_COLOR_PRESSED : THUMB_COLOR);
 			canvas.drawCircle(locationMinXThumb, getHeight() / 2, MAX_CIRCLE_RADIUS, paint);
 			paint.setColor(draggingMaxXThumb ? THUMB_COLOR_PRESSED : THUMB_COLOR);
 			canvas.drawCircle(locationMaxXThumb, getHeight() / 2, MAX_CIRCLE_RADIUS, paint);
 		}
 		paint.setColor(draggingMinXThumb ? THUMB_INNER_COLOR_PRESSED : isEnabled() ? THUMB_INNER_COLOR : THUMB_INNER_COLOR_PRESSED);
 		canvas.drawCircle(locationMinXThumb, getHeight() / 2, MAX_CIRCLE_RADIUS / 2, paint);
 		paint.setColor(draggingMaxXThumb ? THUMB_INNER_COLOR_PRESSED : isEnabled() ? THUMB_INNER_COLOR : THUMB_INNER_COLOR_PRESSED);
 		canvas.drawCircle(locationMaxXThumb, getHeight() / 2, MAX_CIRCLE_RADIUS / 2, paint);
 		if(isEnabled()) {
 			paint.setStyle(Paint.Style.STROKE);
 			paint.setColor(THUMB_OUTER_RING_COLOR);
 			canvas.drawCircle(locationMinXThumb, getHeight() / 2, MAX_CIRCLE_RADIUS, paint);
 			canvas.drawCircle(locationMaxXThumb, getHeight() / 2, MAX_CIRCLE_RADIUS, paint);
 		}
 		paint.setColor(THUMB_INNER_RING_COLOR);
 		canvas.drawCircle(locationMinXThumb, getHeight() / 2, MAX_CIRCLE_RADIUS / 2, paint);
 		canvas.drawCircle(locationMaxXThumb, getHeight() / 2, MAX_CIRCLE_RADIUS / 2, paint);
 		paint.setStyle(Paint.Style.FILL);
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if(!isEnabled())
 			return false;
 		switch(event.getAction()) {
 			case MotionEvent.ACTION_DOWN:
 				setPressed(true);
 				attemptMove(event);
 			break;
 			
 			case MotionEvent.ACTION_MOVE:
 				getParent().requestDisallowInterceptTouchEvent(true);
 				attemptMove(event);
 			break;
 			
 			case MotionEvent.ACTION_UP:
 				setPressed(false);
 			break;
 			
 			case MotionEvent.ACTION_CANCEL:
 				setPressed(false);
 			break;
 		}
 		return true;
 	}
 	
 	public void attemptMove(MotionEvent event) {
 		RectF pointRect = null, thumbMinXRect = null, thumbMaxXRect = null;
 		if(!draggingMinXThumb && !draggingMaxXThumb) {
 			pointRect = new RectF(event.getX() - MAX_CIRCLE_RADIUS, event.getY() - MAX_CIRCLE_RADIUS, event.getX() + MAX_CIRCLE_RADIUS, event.getY() + MAX_CIRCLE_RADIUS);
 			thumbMinXRect = new RectF(locationMinXThumb, getHeight() / 2 - MAX_CIRCLE_RADIUS, locationMinXThumb + MAX_CIRCLE_RADIUS * 2, getHeight() / 2 + MAX_CIRCLE_RADIUS);
 			thumbMaxXRect = new RectF(locationMaxXThumb, getHeight() / 2 - MAX_CIRCLE_RADIUS, locationMaxXThumb + MAX_CIRCLE_RADIUS * 2, getHeight() / 2 + MAX_CIRCLE_RADIUS);
 		}
 		if(draggingMinXThumb || (pointRect != null && RectF.intersects(pointRect, thumbMinXRect))) {
 			draggingMinXThumb = true;
 			draggingMaxXThumb = false;
 			float newLocationMinXThumb = event.getX() - MAX_CIRCLE_RADIUS;
 			
 			if(newLocationMinXThumb < 0)
 				newLocationMinXThumb = 0;
 			else if(newLocationMinXThumb > getWidth() - (MAX_CIRCLE_RADIUS * 2))
 				newLocationMinXThumb = getWidth() - (MAX_CIRCLE_RADIUS * 2);
 			
			if(newLocationMinXThumb > locationMaxXThumb) {
 				newLocationMinXThumb = locationMaxXThumb;
 				draggingMinXThumb = false;
 				draggingMaxXThumb = true;
 			}
 			
 			actualMinimum = absoluteMinimum + (newLocationMinXThumb / (getWidth() - (MAX_CIRCLE_RADIUS * 2))) * (absoluteMaximum - absoluteMinimum);
 			if(onMinMaxBarChangeListener != null)
 				onMinMaxBarChangeListener.onMinValueChanged((int)actualMinimum, true);
 		} else if(draggingMaxXThumb || (pointRect != null && RectF.intersects(pointRect, thumbMaxXRect))) {
 			draggingMinXThumb = false;
 			draggingMaxXThumb = true;
 			float newLocationMaxXThumb = event.getX() - MAX_CIRCLE_RADIUS;
 			
 			if(newLocationMaxXThumb < 0)
 				newLocationMaxXThumb = 0;
 			else if(newLocationMaxXThumb > getWidth() - (MAX_CIRCLE_RADIUS * 2))
 				newLocationMaxXThumb = getWidth() - (MAX_CIRCLE_RADIUS * 2);
 			
			if(newLocationMaxXThumb < locationMinXThumb) {
 				newLocationMaxXThumb = locationMinXThumb;
 				draggingMinXThumb = true;
 				draggingMaxXThumb = false;
 			}
 			actualMaximum = absoluteMinimum + (newLocationMaxXThumb / (getWidth() - (MAX_CIRCLE_RADIUS * 2))) * (absoluteMaximum - absoluteMinimum);
 			if(onMinMaxBarChangeListener != null)
 				onMinMaxBarChangeListener.onMaxValueChanged((int)actualMaximum, true);
 		}
 		invalidate();
 	}
 	
 	@Override
 	public void setPressed(boolean pressed) {
 		if(!pressed) {
 			draggingMinXThumb = false;
 			draggingMaxXThumb = false;
 		}
 		super.setPressed(pressed);
 	}
 	
 	public void setMinMaxAbsolutes(int min, int max) {
 		absoluteMinimum = min;
 		absoluteMaximum = max;
 		if(getMinimum() != min)
 			setMinimum(min);
 		if(getMaximum() != max)
 			setMaximum(max);
 	}
 	
 	public void setMinimum(int minimum) {
 		actualMinimum = minimum;
 		if(actualMinimum < absoluteMinimum)
 			actualMinimum = absoluteMinimum;
 		else if(actualMinimum > absoluteMaximum)
 			actualMinimum = absoluteMaximum;
 		if(onMinMaxBarChangeListener != null)
 			onMinMaxBarChangeListener.onMinValueChanged((int)actualMinimum, false);
 		invalidate();
 	}
 	
 	public float getMinimum() {
 		return actualMinimum;
 	}
 	
 	public void setMaximum(int maximum) {
 		actualMaximum = maximum;
 		if(actualMaximum < absoluteMinimum)
 			actualMaximum = absoluteMinimum;
 		else if(actualMaximum > absoluteMaximum)
 			actualMaximum = absoluteMaximum;
 		if(onMinMaxBarChangeListener != null)
 			onMinMaxBarChangeListener.onMaxValueChanged((int)actualMaximum, false);
 		invalidate();
 	}
 	
 	public float getMaximum() {
 		return actualMaximum;
 	}
 	
 	public void setOnMinMaxBarChangeListener(OnMinMaxBarChangeListener onMinMaxBarChangeListener) {
 		this.onMinMaxBarChangeListener = onMinMaxBarChangeListener;
 	}
 }
