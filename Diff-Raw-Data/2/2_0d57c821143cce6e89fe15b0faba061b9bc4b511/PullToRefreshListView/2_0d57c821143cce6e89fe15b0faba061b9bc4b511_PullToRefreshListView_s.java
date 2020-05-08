 package pl.qbasso.pulltorefresh;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Context;
 import android.util.AttributeSet;
 import android.util.DisplayMetrics;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewTreeObserver.OnGlobalLayoutListener;
 import android.view.WindowManager;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.TranslateAnimation;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class PullToRefreshListView extends ListView {
 
 	private static final int ANIMATION_DURATION = 700;
 	private int mState = 0;
 	private float mPrevY = 0f;
 	private float mPrevX = 0f;
 	private static int sScreenWidth = 0;
 	private int draggingPosition = -1;
 	private int mCurrentMargin = 0;
 	private int mRowCurrentMargin = 0;
 	private View mHeaderView;
 	private int headerViewHeight;
 	private LinearLayout mHeaderContent;
 	private PullToRefreshListner mListener;
 	private static final float SCROLL_RESISTANCE = 1.25f;
 
 	private static final int IDLE = 0;
 	private static final int PULLING = 1;
 	private static final int REFRESHING = 2;
 	private static final int RELEASE_TO_REFRESH = 3;
 	private static final int ITEM_DRAGGING = 4;
 	private static final int DRAGGING = 5;
 
 	public interface PullToRefreshListner {
 		public void onRefreshTriggered();
 
 		public void onItemRemoved(int pos);
 	}
 
 	private OnGlobalLayoutListener mGlobalLayoutChangeListener = new OnGlobalLayoutListener() {
 
 		@Override
 		public void onGlobalLayout() {
 			headerViewHeight = mHeaderView.getHeight();
 			if (headerViewHeight > 0) {
 				setHeaderMargin(-headerViewHeight);
 			}
 			mHeaderView.getViewTreeObserver()
 					.removeGlobalOnLayoutListener(this);
 		}
 	};
 	private TextView mRefreshState;
 	private View mDraggableView;
 
 	public PullToRefreshListView(Context context, AttributeSet attrs,
 			int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 
 	public PullToRefreshListView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 
 	private void init() {
 		DisplayMetrics dm = new DisplayMetrics();
 		mHeaderView = inflate(getContext(), R.layout.refresh_item, null);
 		mHeaderContent = (LinearLayout) mHeaderView
 				.findViewById(R.id.header_content);
 		mRefreshState = (TextView) mHeaderView.findViewById(R.id.refresh_state);
 		addHeaderView(mHeaderView);
 		mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(
 				mGlobalLayoutChangeListener);
 		((WindowManager) getContext().getSystemService(Activity.WINDOW_SERVICE))
 				.getDefaultDisplay().getMetrics(dm);
 		sScreenWidth = dm.widthPixels;
 	}
 
 	private void setHeaderMargin(int margin) {
 		mCurrentMargin = margin;
 		LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) mHeaderContent
 				.getLayoutParams();
 		params.setMargins(0, margin, 0, 0);
 		mHeaderContent.setLayoutParams(params);
 	}
 
 	private void setRowMargin(int margin, View v) {
 		mRowCurrentMargin = margin;
 		MarginLayoutParams params = (MarginLayoutParams) v.getLayoutParams();
 		params.setMargins(margin, 0, -margin, 0);
 		v.setLayoutParams(params);
 	}
 
 	private void animateRow(int margin, final View v) {
 		TranslateAnimation ta;
 		ta = new TranslateAnimation(0, margin, 0, 0);
 		ta.setDuration(ANIMATION_DURATION);
 		ta.setAnimationListener(new AnimationListener() {
 
 			@Override
 			public void onAnimationStart(Animation animation) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				// TODO Auto-generated method stub
 				setRowMargin(0, v);
 			}
 		});
 		v.startAnimation(ta);
 
 		// v.postDelayed(new Runnable() {
 		// @Override
 		// public void run() {
 		// setRowMargin(0, v);
 		// }
 		// }, ANIMATION_DURATION);
 		// if (margin > 0) {
 		// for (int i = 1; i <= margin; i++) {
 		// setRowMargin(mRowCurrentMargin + 1, v);
 		//
 		// }
 		// } else {
 		// for (int i = -1; i >= margin; i--) {
 		// setRowMargin(mRowCurrentMargin - 1, v);
 		// }
 		// }
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent ev) {
 		int action = ev.getAction();
 		float diffX, diffY;
 		switch (action) {
 		case MotionEvent.ACTION_DOWN:
 			if (getFirstVisiblePosition() == 0 && mState == IDLE) {
 				mState = PULLING;
 			} else {
 				mState = DRAGGING;
 			}
 
 			mPrevY = ev.getY();
 			mPrevX = ev.getX();
 			draggingPosition = pointToPosition((int) mPrevX, (int) mPrevY);
 			mDraggableView = getChildAt(
 					draggingPosition - getFirstVisiblePosition()).findViewById(
 					R.id.item_content);
 			break;
 		case MotionEvent.ACTION_MOVE:
 			diffY = ev.getY() - mPrevY;
 			mPrevY = ev.getY();
 			diffX = ev.getX() - mPrevX;
 			mPrevX = ev.getX();
 			if ((mState == PULLING || mState == RELEASE_TO_REFRESH)) {
 				int newMargin = Math.max(mCurrentMargin
 						+ (int) (diffY / SCROLL_RESISTANCE), -headerViewHeight);
 				if (newMargin != mCurrentMargin) {
 					setHeaderMargin(newMargin);
 					if (mState == PULLING && newMargin > headerViewHeight) {
 						mState = RELEASE_TO_REFRESH;
 					} else if (mState == RELEASE_TO_REFRESH
 							&& newMargin < headerViewHeight) {
 						mState = PULLING;
 					}
 					return true;
 				} else if (mCurrentMargin == -headerViewHeight
 						&& Math.abs(diffX) > 15f) {
 					setRowMargin(mRowCurrentMargin + (int) diffX,
 							mDraggableView);
 					mState = ITEM_DRAGGING;
 					return true;
 				}
 			} else if (mState == DRAGGING) {
 				if (mCurrentMargin == -headerViewHeight
 						&& Math.abs(diffX) > 20f) {
 					setRowMargin(mRowCurrentMargin + (int) diffX,
 							mDraggableView);
 					mState = ITEM_DRAGGING;
 					return true;
 				}
 			} else if (mState == ITEM_DRAGGING) {
 				setRowMargin(mRowCurrentMargin + (int) diffX, mDraggableView);
 				return true;
 			}
 			break;
 		case MotionEvent.ACTION_UP:
 			if (getFirstVisiblePosition() == 0) {
 				if (mState == PULLING) {
 					mState = IDLE;
 					hideHeader(headerViewHeight, mCurrentMargin);
 					return true;
 				} else if (mState == RELEASE_TO_REFRESH) {
 					mRefreshState.setText("Refreshing...");
 					mState = REFRESHING;
 					hideHeader(0, mCurrentMargin);
 					if (mListener != null) {
 						mListener.onRefreshTriggered();
 					}
 				}
 			}
 
 			if (mState == ITEM_DRAGGING) {
 				if (Math.abs(mRowCurrentMargin) < sScreenWidth / 2) {
 					animateRow(-mRowCurrentMargin, mDraggableView);
 				} else {
 					if (mRowCurrentMargin > 0) {
 						animateRow(sScreenWidth - mRowCurrentMargin,
 								mDraggableView);
 					} else {
 						animateRow(-(sScreenWidth + mRowCurrentMargin),
 								mDraggableView);
 					}
 					mDraggableView.postDelayed(new Runnable() {
 						@Override
 						public void run() {
 							mListener.onItemRemoved(draggingPosition - 1);
 						}
 					}, ANIMATION_DURATION);
 				}
 				mRowCurrentMargin = 0;
 				mDraggableView = null;
 				mState = IDLE;
 			}
 
 			break;
 		default:
 			break;
 		}
 		return super.onTouchEvent(ev);
 	}
 
 	private void hideHeader(final int headerHeight, final int margin) {
 		TranslateAnimation ta = new TranslateAnimation(0, 0, 0,
 				-(headerHeight + margin));
 		ta.setDuration(ANIMATION_DURATION);
 		mHeaderView.startAnimation(ta);
 		mHeaderView.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				setHeaderMargin(-(headerHeight));
 				scrollTo(0, 0);
 			}
 		}, ANIMATION_DURATION);
 
 	}
 
 	public void setPullToRefreshListener(PullToRefreshListner mListener) {
 		this.mListener = mListener;
 	}
 
 	public void refreshDone() {
 		mState = IDLE;
 		mRefreshState.setText(String.format(Locale.getDefault(),
 				"Last refreshed: %s", (new SimpleDateFormat("dd/MM/yyyy",
 						Locale.getDefault())).format(new Date(System
 						.currentTimeMillis()))));
 		hideHeader(headerViewHeight, mCurrentMargin);
 	}
 
 }
