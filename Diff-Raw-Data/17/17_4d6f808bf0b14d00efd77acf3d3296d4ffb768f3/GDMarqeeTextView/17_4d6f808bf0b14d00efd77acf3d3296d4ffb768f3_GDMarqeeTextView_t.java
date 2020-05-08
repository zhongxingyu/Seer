 package com.dbstar.widget;
 
 import java.lang.ref.WeakReference;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.dbstar.R;
 import com.dbstar.R.styleable;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.os.Handler;
 import android.os.Message;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 
 public class GDMarqeeTextView extends View {
 
 	private static final String TAG = "GDMarqeeTextView";
 
 	public static final int MarqeeForever = -1;
 	private Context mContext = null;
 
 //	private String mText = null;
 
 	private int mTextColor = 0x0000ff;
 	private float mTextSize = 22;
 
 	private Paint mTextPaint = null;
 
 	private Marquee mMarquee;
 
 	List<String> mTexts = null;
 	List<Float> mTextLengths = null;
 	private int mHeadTextIndex = 0;
 	private int mTailTextIndex = 0;
 
 	List<String> mDisplayTexts = new LinkedList<String>();
 	List<Float> mDisplayTextLengths = new LinkedList<Float>();
 	private int mDisplayTextTotalLength = 0, mDisplayTextLength = 0;
 	private float mDisplayTextIntervalLength = 0;
 	private int mDisplayViewWidth = 0;
 	
 	private boolean mPendingToStart = false;
 	private int mRepeatLimit = MarqeeForever;
 	
	private int mCountPerCycle = -1;
 	private int mCycleInterval = 10000; //10s
 	private int mShowCounts = 0;
 	private boolean mReachACycle = false;
 	/*
 	 * Kick-start the font cache for the zygote process (to pay the cost of
 	 * initializing freetype for our default font only once).
 	 */
 	static {
 		Paint p = new Paint();
 		p.setAntiAlias(true);
 		// We don't care about the result, just the side-effect of measuring.
 		p.measureText("H");
 	}
 
 	public GDMarqeeTextView(Context context) {
 		super(context);
 		mContext = context;
 
 		initializeView();
 	}
 
 	public GDMarqeeTextView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 
 		mContext = context;
 
 		if (attrs != null) {
 			TypedArray a = context.obtainStyledAttributes(attrs,
 					R.styleable.GDMarqeeTextView);
 
 //			mText = a.getString(R.styleable.GDMarqeeTextView_text);
 
 			mTextSize = a.getDimension(R.styleable.GDMarqeeTextView_textSize,
 					mTextSize);
 			setTextSize(mTextSize);
 
 			mTextColor = a.getColor(R.styleable.GDMarqeeTextView_textColor,
 					mTextColor);
 			setTextColor(mTextColor);
 		}
 
 		initializeView();
 	}
 
 	private void initializeView() {
 
 		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		mTextPaint.setColor(mTextColor);
 		mTextPaint.setTextSize(mTextSize);
 
 		mMarquee = new Marquee(this);
 
 		float density = getContext().getResources().getDisplayMetrics().density;
 		mDisplayTextIntervalLength = density * 100.f;
 		
 		if (mTextHeight == 0) {
 			Rect textBounds = new Rect();
 			mTextPaint.getTextBounds("A", 0, 1, textBounds);
 			mTextHeight = textBounds.height();
 		}
 	}
 
 //	public void setText(int resId) {
 //		mText = mContext.getResources().getString(resId);
 //	}
 //
 //	public void setText(String text) {
 //		mText = text;
 //		Log.d(TAG, "text " + text);
 //	}
 
 	public void setTextColor(int color) {
 		mTextColor = color;
 		if (mTextPaint != null) {
 			mTextPaint.setColor(mTextColor);
 		}
 	}
 
 	public void setTextSize(float size) {
 		mTextSize = size;
 		Log.d(TAG, "text size " + size);
 		if (mTextPaint != null) {
 			mTextPaint.setTextSize(mTextSize);
 		}
 	}
 
 	// Show number of times per cycle
 	public void setCountPerCycle(int count) {
 		mCountPerCycle = count;
 	}
 	
 	public void setCycleInterval(int milliseconds) {
 		mCycleInterval = milliseconds;
 	}
 	
 	public void addText(String text) {
 		if (mTexts == null) {
 			mTexts = new LinkedList<String>();
 			mTextLengths = new LinkedList<Float>();
 		}
 
 		mTexts.add(text);
 		mTextLengths.add(mTextPaint.measureText(text));
 		
 		Log.d(TAG, "addText = " + text);
 		
 		if (mPendingToStart) {
 			mPendingToStart = false;
 			startMarqee(mRepeatLimit);
 		}
 	}
 	
 	public boolean isRunning() {
 		return mMarquee !=null && mMarquee.isRunning();
 	}
 	
 	public boolean isStopped() {
 		return mMarquee != null && mMarquee.isStopped();
 	}
 
 	private int mViewWidth = 0, mViewHeight = 0;
 	private int mTextHeight = 0;
 	private float mTextOriginX = 0, mTextOriginY = 0;
 
 	public void onLayout(boolean changed, int left, int top, int right,
 			int bottom) {
 		super.onLayout(changed, left, top, right, bottom);
 
 		Log.d(TAG, "onLayout");
 
 		if (changed) {
 			int paddingLeft = getPaddingLeft();
 			int paddingRight = getPaddingRight();
 //			int paddingTop = getPaddingTop();
 //			int paddingBottom = getPaddingBottom();
 	
 			mViewWidth = super.getMeasuredWidth();
 			mViewHeight = super.getMeasuredHeight();
 	
 			mDisplayViewWidth = mViewWidth - paddingLeft - paddingRight;
 			
 			mTextOriginX = paddingLeft;
 			mTextOriginY = mTextHeight + (mViewHeight - mTextHeight) / 2.0f;
 		}
 
 		if (mPendingToStart) {
 			mPendingToStart = false;
 			startMarqee(mRepeatLimit);
 		}
 	}
 
 	public void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 
 		if (mDisplayTexts != null && mDisplayTexts.size() > 0) {
 			//Log.d(TAG, "onDraw " + mDisplayTexts.size());
 
 			updateDisplayText();
 
 			int saveCount = canvas.getSaveCount();
 			canvas.save();
 			canvas.translate(-mMarquee.mScroll, 0.0f);
 
 			float originX = mTextOriginX;
 			for (int i = 0; i < mDisplayTexts.size(); i++) {
 				canvas.drawText(mDisplayTexts.get(i), originX, mTextOriginY, mTextPaint);
 				originX += mDisplayTextLengths.get(i) + mDisplayTextIntervalLength;
 				
 //				if ((originX - mTextOriginX) > mDisplayViewWidth)
 //					break;
 			}
 
 			canvas.restoreToCount(saveCount);
 		}
 	}
 
 	private float getDisplayTextWidth () {
 		float width = 0.0f;
 		
 		for (int i=0; i<mDisplayTextLengths.size() ; i++) {
 			width += mDisplayTextLengths.get(i);
 			width += mDisplayTextIntervalLength;
 		}
 		
 		return width;
 	}
 
 	private boolean initMarqee() {
 		if (mTexts == null || mTexts.size() == 0 || mDisplayViewWidth == 0) {
 			return false;
 		}
 
 		mHeadTextIndex = 0;
 		mTailTextIndex = 0;
 		mDisplayTexts.clear();
 
 		do {
 			mDisplayTexts.add(mTexts.get(mTailTextIndex));
 			mDisplayTextLengths.add(mTextLengths.get(mTailTextIndex));
 			
 			mDisplayTextTotalLength += mTextLengths.get(mTailTextIndex) + mDisplayTextIntervalLength;
 
 			if (mDisplayTextTotalLength > mDisplayViewWidth)
 				break;
 
 			mTailTextIndex++;
 			if (mTailTextIndex == mTexts.size())
 				mTailTextIndex = 0;
 
 		} while (true);
 		
 		return true;
 	}
 
 	private boolean mRestartMarqee = false;
 
 	private void updateDisplayText() {
 
 		if (mDisplayTexts.size() == 0)
 			return;
 		
 		//float totalLength = getDisplayTextWidth();
 
 		//mDisplayTextTotalLength = (int)totalLength;
 		mDisplayTextLength = (int) (mDisplayTextTotalLength - mMarquee.mScroll);
 		//Log.d(TAG, "mScroll "+ mMarquee.mScroll + " mDisplayTextLength=" + mDisplayTextLength + " mDisplayTextTotalLength=" + mDisplayTextTotalLength);
 
 		// check the head text
 		float headTextLength = mDisplayTextLengths.get(0) + mDisplayTextIntervalLength;
 		if (headTextLength <= mMarquee.mScroll) {
 			// head has gone out of the screen
 			// not need to show
 
 			mMarquee.mScroll -= headTextLength;
 			mDisplayTextTotalLength -= headTextLength;
 			
 			mDisplayTexts.remove(0);
 			mDisplayTextLengths.remove(0);
 			
 			if (mReachACycle) {
 				if (mDisplayTexts.size() == 0) {
 					mReachACycle = false;
 					mRestartMarqee = true;
 //					Log.d(TAG, "reach end ----- restart = " + mRestartMarqee);
 					return;
 				}
 			}
 			
 			int index = mHeadTextIndex + 1;
 			if (index < mTexts.size()) {
 				mHeadTextIndex = index;
 			}
 			
 			Log.d(TAG, "remove head index " + mHeadTextIndex + " count = " + mDisplayTexts.size() + " mScroll " + mMarquee.mScroll);
 
 		}
 
 		// check tail text
 		if ((mDisplayTextLength - (int)mDisplayTextIntervalLength) < mDisplayViewWidth) {
 			int index = mTailTextIndex + 1;
 			if (index == mTexts.size()) {
 				
 				if (!mReachACycle) {
					if (mCountPerCycle > 0) {
						mShowCounts++;
						if (mShowCounts == mCountPerCycle) {
							mShowCounts = 0;
							mReachACycle = true;
	//						Log.d(TAG, "reach count -----1");
							return;
						}
 					}
 				} else {
 //					Log.d(TAG, "reach count -----2");
 					return;
 				}
 				
 				mTailTextIndex = 0;
 				
 			} else {
 				mTailTextIndex = index;
 			}
 
 			do {
 				mDisplayTexts.add(mTexts.get(mTailTextIndex));
 				mDisplayTextLengths.add(mTextLengths.get(mTailTextIndex));
 				
 				mDisplayTextTotalLength += mTextLengths.get(mTailTextIndex) + mDisplayTextIntervalLength;
 				//Log.d(TAG, "add tail index " + mTailTextIndex + " count = " + mDisplayTexts.size() + " mDisplayTextTotalLength " + mDisplayTextTotalLength);
 
 				if (mDisplayTextTotalLength > mDisplayViewWidth)
 					break;
 
 				mTailTextIndex++;
 				if (mTailTextIndex == mTexts.size())
 					mTailTextIndex = 0;
 
 			} while (true);
 
 		}
 	}
 
 	public void startMarqee(int repeatLimit) {
 //		if (mTexts == null || mTexts.size() == 0)
 //			return;
 
 		if (!initMarqee()) {
 			mPendingToStart = true;
 			mRepeatLimit = repeatLimit;
 			return;
 		}
 		
 		mReachACycle = false;
 		mRestartMarqee = false;
 		mMarquee.start(repeatLimit);
 	}
 
 	public void stopMarqee() {
 		mMarquee.stop();
 	}
 
 	public void startMarqeeIfNeeded(int repeatLimit) {
 		if (mTexts == null || mTexts.size() == 0)
 			return;
 
 		if (mMarquee != null && !mMarquee.isRunning()) {
 			mMarquee.start(repeatLimit);
 		}
 	}
 
 	private class Marquee extends Handler {
 		// TODO: Add an option to configure this
 		private static final float MARQUEE_DELTA_MAX = 0.07f;
 		private static final int MARQUEE_DELAY = 1200;
 		private static final int MARQUEE_RESTART_DELAY = 1200;
 		private static final int MARQUEE_PIXELS_PER_SECOND = 32;
 		private static final int MARQUEE_FRMAE_RATE = 16;
 		private static final int MARQUEE_RESOLUTION = 1000 / MARQUEE_FRMAE_RATE;
 
 
 		private static final byte MARQUEE_STOPPED = 0x0;
 		private static final byte MARQUEE_STARTING = 0x1;
 		private static final byte MARQUEE_RUNNING = 0x2;
 
 		private static final int MESSAGE_START = 0x1;
 		private static final int MESSAGE_TICK = 0x2;
 		private static final int MESSAGE_RESTART = 0x3;
 
 		private final WeakReference<GDMarqeeTextView> mView;
 
 		private byte mStatus = MARQUEE_STOPPED;
 		private final float mScrollUnit;
 		private int mRepeatLimit;
 
 		float mScroll = 0.0f;
 
 		Marquee(GDMarqeeTextView v) {
 			final float density = v.getContext().getResources()
 					.getDisplayMetrics().density;
 			mScrollUnit = (MARQUEE_PIXELS_PER_SECOND * density)
 					/ MARQUEE_FRMAE_RATE;
 
 			mView = new WeakReference<GDMarqeeTextView>(v);
 		}
 
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MESSAGE_START:
 				mStatus = MARQUEE_RUNNING;
 				tick();
 				break;
 			case MESSAGE_TICK:
 				tick();
 				break;
 			case MESSAGE_RESTART:
 				if (mStatus == MARQUEE_RUNNING) {
 					if (mRepeatLimit >= 0) {
                         mRepeatLimit--;
                     }
 					
 //					Log.d(TAG, "restart marqee ......");
 					startMarqee(mRepeatLimit);
                     start(mRepeatLimit);
 				}
 			default:
 				break;
 			}
 		}
 
 		void tick() {
 			if (mStatus != MARQUEE_RUNNING) {
 				return;
 			}
 
 			// Log.d(TAG, "tick  mScrollUnit " + mScrollUnit);
 
 			removeMessages(MESSAGE_TICK);
 			
 			if (mRestartMarqee) {
 				mRestartMarqee = false;
 				sendEmptyMessageDelayed(MESSAGE_RESTART, mCycleInterval);
 //				Log.d(TAG, "need to restart marqee....");
 				return;
 			}
 
 			final GDMarqeeTextView textView = mView.get();
 			if (textView != null) {
 				mScroll += mScrollUnit;
 				// if (mScroll > mMaxScroll) {
 				// mScroll = mMaxScroll;
 				// sendEmptyMessageDelayed(MESSAGE_RESTART,
 				// MARQUEE_RESTART_DELAY);
 				// } else {
 				// sendEmptyMessageDelayed(MESSAGE_TICK, MARQUEE_RESOLUTION);
 				// }
 
 				sendEmptyMessageDelayed(MESSAGE_TICK, MARQUEE_RESOLUTION);
 				//Log.d(TAG, "tick  mScroll " + mScroll);
 
 				textView.invalidate();
 			}
 		}
 
 		void stop() {
 			mStatus = MARQUEE_STOPPED;
 			removeMessages(MESSAGE_START);
 			removeMessages(MESSAGE_RESTART);
 			removeMessages(MESSAGE_TICK);
 			resetScroll();
 		}
 
 		private void resetScroll() {
 			mScroll = 0.0f;
 			final GDMarqeeTextView textView = mView.get();
 			if (textView != null)
 				textView.invalidate();
 		}
 
 		void start(int repeatLimit) {
 			if (repeatLimit == 0) {
 				stop();
 				return;
 			}
 
 			Log.d(TAG, "start");
 			mRepeatLimit = repeatLimit;
 			final GDMarqeeTextView textView = mView.get();
 			if (textView != null) {
 				mStatus = MARQUEE_STARTING;
 				mScroll = 0.0f;
 
 				textView.invalidate();
 				sendEmptyMessageDelayed(MESSAGE_START, MARQUEE_DELAY);
 			}
 		}
 
 		boolean isRunning() {
 			return mStatus == MARQUEE_RUNNING;
 		}
 
 		boolean isStopped() {
 			return mStatus == MARQUEE_STOPPED;
 		}
 	}
 
 }
