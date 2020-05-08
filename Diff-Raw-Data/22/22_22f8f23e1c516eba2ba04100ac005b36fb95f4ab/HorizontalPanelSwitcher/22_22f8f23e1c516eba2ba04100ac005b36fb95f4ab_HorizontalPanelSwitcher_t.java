 package com.selagroup.schedu;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.VelocityTracker;
 import android.view.View;
 import android.widget.RelativeLayout;
 
 public class HorizontalPanelSwitcher extends RelativeLayout {
 	public interface OnDragListener {
 		public void dragStart();
 
 		public void dragEnd();
 	}
 
 	public interface OnPanelSwitchListener {
 		public void switchRight();
 
 		public void switchLeft();
 	}
 
 	private OnDragListener mDragListener = null;
 	private OnPanelSwitchListener mSwitchListener = null;
 
 	private static final int VELOCITY_SENSITIVITY = 2000;
 
 	private static final int PANEL_LEFT = 0;
 	private static final int PANEL_CENTER = 1;
 	private static final int PANEL_RIGHT = 2;
 
 	private int mTargetPanel = PANEL_CENTER;
 
 	private float mDownX; // x-coordinate of the last ACTION_DOWN motion event
 	private boolean mIsDragging = false; // True if dragging this panel
 	private boolean mDragDirectionDecided = false;
 	private VelocityTracker mVelocityTracker; // Tracks velocity
 
 	// Store the switcher's width and height
 	private int mWidth;
 	private int mHeight;
 
 	// Panels
 	private RelativeLayout mLeftPanel;
 	private RelativeLayout mCenterPanel;
 	private RelativeLayout mRightPanel;
 
 	// Animation variables
 	private volatile int mDelta = 0; // Offset for laying out the views
 	private volatile int mShift = 0; // Amount shifted by previous drags
 
 	private volatile int mTarget = 0; // Target offset to animate towards
 	private volatile boolean mIsAnimating = false; // True if animating the view
 	private volatile int mAnimationStep = 8; // Number of pixels that the view moves each update while animating
 
 	// Refreshes the layout
 	private final Runnable mRefreshLayout = new Runnable() {
 		public void run() {
 			requestLayout();
 		}
 	};
 
 	// Animation thread
 	private Thread mAnimationThread = new Thread(new Runnable() {
 		public void run() {
 			while (HorizontalPanelSwitcher.this != null) {
 				if (mIsAnimating) {
 					if (Math.abs(mTarget - mDelta) <= mAnimationStep) {
 						mDelta = mTarget;
 						mShift = mDelta;
 					}
 
 					if (mDelta > mTarget) {
 						mDelta -= mAnimationStep;
 						mShift = mDelta;
 					} else if (mDelta < mTarget) {
 						mDelta += mAnimationStep;
 						mShift = mDelta;
 					} else {
 						// Reached target: animation done
 						mIsAnimating = false;
 
 						// If there's a switch listener set, get new panels
 						if (mSwitchListener != null) {
 							post(new Runnable() {
 								public void run() {
 									if (mTargetPanel == PANEL_RIGHT) {
 										mSwitchListener.switchRight();
 									} else if (mTargetPanel == PANEL_LEFT) {
 										mSwitchListener.switchLeft();
 									}
 									mDelta = 0;
 									mShift = mDelta;
 								}
 							});
 						}
 					}
 					post(mRefreshLayout);
 				}
 				try {
 					Thread.sleep(5);
 				} catch (InterruptedException e) {
 				}
 			}
 		}
 	});
 
 	// Constructors, inherited
 	public HorizontalPanelSwitcher(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 
 	public HorizontalPanelSwitcher(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 
 	public HorizontalPanelSwitcher(Context context) {
 		super(context);
 		init();
 	}
 
 	// Set listener for the panel switch
 	public void setOnPanelSwitchListener(OnPanelSwitchListener iSwitchListener) {
 		mSwitchListener = iSwitchListener;
 	}
 
 	public void setOnDragListener(OnDragListener iDragListener) {
 		mDragListener = iDragListener;
 	}
 
 	// Setters for panels
 	public void setLeftLayout(RelativeLayout iLeftPanel) {
 		removeView(mLeftPanel);
 		removeView(iLeftPanel);
 		mLeftPanel = iLeftPanel;
 		addView(mLeftPanel);
 	}
 
 	public void setCenterLayout(RelativeLayout iCenterPanel) {
 		removeView(mCenterPanel);
 		removeView(iCenterPanel);
 		mCenterPanel = iCenterPanel;
 		addView(mCenterPanel);
 	}
 
 	public void setRightLayout(RelativeLayout iRightPanel) {
 		removeView(mRightPanel);
 		removeView(iRightPanel);
 		mRightPanel = iRightPanel;
 		addView(mRightPanel);
 	}
 
 	// Initialize panels, listeners, animations
 	public void init() {
 		setGravity(Gravity.TOP);
 		setClickable(true);
 		mAnimationThread.start();
 
 		mLeftPanel = new RelativeLayout(getContext());
 		mCenterPanel = new RelativeLayout(getContext());
 		mRightPanel = new RelativeLayout(getContext());
 
 		addView(mLeftPanel);
 		addView(mCenterPanel);
 		addView(mRightPanel);
 
 		setOnTouchListener(new OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 				return movePanels(event);
 			}
 		});
 	}
 
 	@Override
 	public boolean onInterceptTouchEvent(MotionEvent ev) {
 		return movePanels(ev);
 	}
 
 	@Override
 	protected void onLayout(boolean changed, int l, int t, int r, int b) {
 		super.onLayout(changed, l, t, r, b);
 		mWidth = getWidth();
 		mHeight = getHeight();
 
 		mLeftPanel.layout(-mWidth + mDelta, 0, mDelta, mHeight);
 		mCenterPanel.layout(mDelta, 0, mWidth + mDelta, mHeight);
 		mRightPanel.layout(mWidth + mDelta, 0, 2 * mWidth + mDelta, mHeight);
 	}
 
 	public boolean movePanels(MotionEvent event) {
 		switch (event.getAction()) {
 		case MotionEvent.ACTION_DOWN:
			if (!mIsAnimating) {
				mDownX = event.getX();
				mVelocityTracker = VelocityTracker.obtain();
				mVelocityTracker.addMovement(event);
				mDragDirectionDecided = false;
			}
 			break;
 
 		case MotionEvent.ACTION_MOVE:
 
 			// Only start dragging if the x velocity > the y velocity
 			if (!mDragDirectionDecided) {
 				mVelocityTracker.addMovement(event);
 				mVelocityTracker.computeCurrentVelocity(VELOCITY_SENSITIVITY);
 				if (Math.abs(mVelocityTracker.getXVelocity()) > Math.abs(mVelocityTracker.getYVelocity())) {
 					mIsDragging = true;
 					mDragListener.dragStart();
 				}
 				mDragDirectionDecided = true;
 			}
 
 			// If dragging, move the panels
 			if (mIsDragging) {
 				float xDiff = event.getX() - mDownX;
 				mDelta = mShift + (int) xDiff;
 				mDelta = Math.max(Math.min(mDelta, 3 * mWidth / 2), -3 * mWidth / 2);
 				requestLayout();
 			}
 
 			break;
 
 		case MotionEvent.ACTION_UP:
 			if (mIsDragging) {
 				mDragListener.dragEnd();
 
 				mVelocityTracker.computeCurrentVelocity(VELOCITY_SENSITIVITY);
 				float xVelocity = mVelocityTracker.getXVelocity();
 
 				// Check which panel should become the center
 				if (mDelta + xVelocity < -mWidth / 2) {
 					mTarget = -mWidth;
 					mTargetPanel = PANEL_RIGHT;
 				} else if (mDelta + xVelocity > mWidth / 2) {
 					mTarget = mWidth;
 					mTargetPanel = PANEL_LEFT;
 				} else {
 					mTarget = 0;
 					mTargetPanel = PANEL_CENTER;
 				}
 				mIsAnimating = true;
 
 				// Prevents up events from going through when the switcher has been dragged
 				boolean tmp = mIsDragging;
 				mIsDragging = false;
 				return tmp;
 			}
 		}
 
 		return false;
 	}
 }
