 package com.chess.genesis;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 
 class TabText extends RobotoText implements OnTouchListener
 {
 	private final static int highlightColor = 0xff00b7eb;
 	private final static int touchColor = 0x8000b7eb;
 
 	private final Paint paint;
 	private boolean active;
 
 	public TabText(final Context context)
 	{
 		this(context, null);
 	}
 
 	public TabText(final Context context, final AttributeSet attrs)
 	{
 		super(context, attrs);
 
 		active = false;
 		paint = new Paint();
 		paint.setColor(highlightColor);
 
 		setLines(1);
 		setOnTouchListener(this);
 	}
 
 	@Override
 	public void onDraw(final Canvas canvas)
 	{
 		super.onDraw(canvas);
 
 		final int barSize = active? 6 : 2;
 		canvas.drawRect(0, getHeight() - barSize, getWidth(), getHeight(), paint);
 	}
 
 	public boolean onTouch(final View v, final MotionEvent event)
 	{
 		if (event.getAction() == MotionEvent.ACTION_DOWN)
 			setBackgroundColor(touchColor);
 		else if (event.getAction() == MotionEvent.ACTION_UP)
 			setBackgroundColor(0x00ffffff);
		return true;
 	}
 
 	public void setActive(final boolean Active)
 	{
 		active = Active;
 		setTextColor(active? 0xffffffff : 0xff808080);
 	}
 
 	public void setTabTextColor(final int color)
 	{
 		setTextColor(color);
 	}
 }
