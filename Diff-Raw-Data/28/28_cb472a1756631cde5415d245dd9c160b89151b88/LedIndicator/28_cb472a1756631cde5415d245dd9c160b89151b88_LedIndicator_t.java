 package com.softanalle.scma;
 
 /**
  *  @author Tommi Rintala <tommi.rintala@iki.fi>
  *  @version 1.0
  */
 
 import android.view.View;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Rect;
 import android.graphics.drawable.ShapeDrawable;
 import android.graphics.drawable.shapes.RectShape;
 import android.util.AttributeSet;
 import android.util.Log;
 // import android.widget.TextView;
 
 public class LedIndicator extends View {
 	private boolean isInitialized = false;
 	private static String TAG = "LedIndicator";
 	private int[] mRectColors = {
 			Color.GREEN,
 			Color.BLUE,
 			Color.RED,
 			
 			Color.WHITE,			
 			Color.YELLOW,
			Color.LTGRAY,
 			
 			Color.MAGENTA,			
 			Color.CYAN
 	};
 	
 	public static final int LED_GREEN = 0;
 	public static final int LED_BLUE = 1;
 	public static final int LED_RED = 2;
 	public static final int LED_WHITE = 3;
 	
 	public static final int LED_YELLOW = 4;
 	public static final int LED_NIR = 5;
 	
 	public static final int LED_MAGENTA = 6;
 	public static final int LED_CYAN = 7;
 	
 	protected static final int LED_FOCUS = 6;
 	protected static final int LED_CALIBRATE = 7;
 	
 	protected static final int LED_POWER = 64;
 
	private static final int mOffColor = Color.BLACK;
 	private static final int POWER_COLOR = Color.GREEN;
 	
 	private final static int mLedCount = 8;
 
 	/*
 	 * Constructors for the component
 	 */
 	public LedIndicator(Context context) {
 		super(context);
 		if (!isInitialized) initializeFirstTime();
 	}
 	public LedIndicator(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		if (!isInitialized) initializeFirstTime();
 	}
 
 	public LedIndicator(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		if (!isInitialized) initializeFirstTime();
 	}
 
 
 	private Rect[] mLedArea;
 	private Rect mPowerArea;
 	private ShapeDrawable[] mLeds;
 	private ShapeDrawable mPowerLed;
 	private boolean[] mActivity;
 	private int mLeft = 0;
 	private int mTop = 0;
 	private int mRight = 0;
 	private int mHeight = 20;
 	private int mWidth = 24;
 	private boolean isReady;
 
 	/**
 	 * Initialize the component, this is needed to run only one time,
 	 * but can be called several times.
 	 */
 	private void initializeFirstTime() {
 		//Log.d(TAG, "initializeFirstTime()");
 		if (isReady) {
 			Log.d(TAG, "(isReady)");
 			return; // no new initialization
 		}
 		isInitialized = true;
 		try {
 
 
 			mActivity = new boolean[mLedCount];
 			mLeds = new ShapeDrawable[mLedCount];
 			mLedArea = new Rect[mLedCount];
 			for (int i=0; i<mLedCount; i++) {
 				mActivity[i] = false;
 				mLeds[i] = new ShapeDrawable(new RectShape());
 				//mLeds[i] = new ShapeDrawable(new OvalShape());
 				// mLeds[i].getPaint().setColor(mRectColors[i]);
 				mLeds[i].getPaint().setColor(mOffColor);
 				int x = mLeft + 2 + i * mWidth;
 				int y = mTop + 2 ;
 				
 				mLedArea[i] = new Rect( mRight - mWidth, y, mRight, y + mHeight );
 				mLeds[i].setBounds(mLedArea[i]);
 				// mLeds[i].setBounds(x, y, x + mWidth, y + mHeight);
 			}
 			mPowerLed = new ShapeDrawable(new RectShape());
 			mPowerLed.getPaint().setColor(mOffColor);
 			mPowerArea = new Rect( mRight - mWidth, mTop + 2, mWidth, mTop + 2 + mHeight );
 			
 			mPowerLed.setBounds(mPowerArea);
 		} catch (Exception ex) {
 			Log.e(TAG, "Error while building leds;" + ex.toString());
 		} finally {
 			isReady = true;
 		}
 		//Log.d(TAG, "initializeFirstTime(): success");
 	}
 
 	/*
 	 * Respond on layout change
 	 * @see android.view.View#onLayout(boolean, int, int, int, int)
 	 */
 	@Override
 	public void onLayout(boolean changed, int left, int top, int right, int bottom) {
 		//Log.d(TAG, "onLayout(" + changed + "," + left + "," + top + "," + right + "," + bottom + ")");
 		if (!isInitialized) initializeFirstTime();
 
 		if ( changed ) {
 			mHeight = (int) (bottom - top) - 4;
 			mWidth = mHeight;
 			mRight = right;
 			for (int i=0;i<mLedCount;i++) {
 				int x = mLeft + i * 2 + i * mWidth;
 				int y = mTop + 2;
 				// Log.d(TAG, "setPos:" + x + ", " + y + ", " + mWidth + ", " + mHeight);
 				mLedArea[i].left = x;
 				mLedArea[i].top = y;
 				mLedArea[i].bottom = y + mHeight;
 				mLedArea[i].right = x + mWidth;
 				if (mLeds[i] != null)
 					mLeds[i].setBounds(mLedArea[i]);
 			}
 			mPowerArea.left = mRight - mWidth;
 			mPowerArea.right = mRight;
 			mPowerArea.top = mTop + 2;
 			mPowerArea.bottom = mTop + 2 + mHeight;
 			mPowerLed.setBounds(mPowerArea);
 		}
 	}
 
 	/**
 	 * Draw the canvas
 	 * @see android.view.View#onDraw(android.graphics.Canvas)
 	 */
 	@Override
 	public void onDraw(Canvas canvas) {
 		if (isReady && canvas != null) {
 			for (int i=0;i<mLedCount;i++) {
 				if(mLeds[i] != null )
 					mLeds[i].draw(canvas);
 			}
 			if ( mPowerLed != null) {
 				mPowerLed.draw(canvas);
 			}
 		}
 		invalidate();
 	}
 
 	/**
 	 * Set the status of power-led (the one in the right)
 	 * @param on power status
 	 */
 	public void setPowerState(boolean on) {		
 		if ( on ) {
 			mPowerLed.getPaint().setColor(POWER_COLOR);
 		} else {
 			mPowerLed.getPaint().setColor(mOffColor);
 		}
 		invalidate(mPowerArea);
 	}
 	
 	/**
 	 * @method turnOn index
 	 * turn on led indicator location index
 	 */
 	
 	/**
 	 * @param index
 	 */
 	public void turnOn(int index) {
 		if (index >= 0 && index < mLedCount) {
 			if ( mActivity[index] == false ) {
 				Log.d(TAG, "turnOn(" + index + ")");
 				mActivity[index] = true;
 				mLeds[index].getPaint().setColor(mRectColors[index]);
 
 				// invalidate led area
 				invalidate(mLedArea[index]);
 			}
 		}
 	}
 
 	/**
 	 * @param index
 	 */
 	public void turnOff(int index) {
 		if ( index >= 0 && index < mLedCount) {
 			if ( mActivity[index] ) {
 				Log.d(TAG, "turnOff(" + index + ")");
 				mActivity[index] = false;
 				mLeds[index].getPaint().setColor(mOffColor);
 
 				// invalidate led area
 				invalidate(mLedArea[index]);
 			}
 		}
 	}
     
     
 	/**
 	 * Set the state of led
 	 * @param index the number of led
 	 * @param onoff the led status
 	 */
     public void setLedState(int index, boolean onoff) {
     	if ( index >= 0 && index < mLedCount) {
     		if ( onoff != mActivity[index] ) {
     			// Log.d(TAG, "ledState(" + index + " => " + onoff + ")");
     			mActivity[index] = onoff;
     			if ( onoff ) {
     				mLeds[index].getPaint().setColor(mRectColors[index]);
     			} else {
     				mLeds[index].getPaint().setColor(mOffColor);
     			}
 
     			// invalidate(mLedArea[index]);
     			this.invalidate();
     		}
     	}
     }
 
     /**
      * Get the number of leds
      * @return int The number of leds
      */
     public int getLedCount() {
     	return mLedCount;
     }
 }
 
 
 
