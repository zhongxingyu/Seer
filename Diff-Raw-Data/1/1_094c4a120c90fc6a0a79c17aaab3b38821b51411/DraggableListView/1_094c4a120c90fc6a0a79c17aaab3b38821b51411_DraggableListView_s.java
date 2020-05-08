 package interdroid.util.view;
 
 import interdroid.util.R;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.PixelFormat;
 import android.util.AttributeSet;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.HeaderViewListAdapter;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 
 public class DraggableListView extends ListView {
 	private static final Logger logger = LoggerFactory
 	.getLogger(DraggableListView.class);
 
 	private boolean mDragMode;
 	private boolean mRemoving;
 
 	private boolean mAllowLeftRightMovement = false;
 	private boolean mAllowAdd = true;
 	private int mAddResource = R.layout.draggable_add;
 
 	int mStartPosition;
 	int mEndPosition;
 	int mDragOffset;
 	int mRemoveTop;
 	int mRemoveBottom;
 
 	ImageView mDragView;
 
 	private AddListener mAddListener;
 
 	// List views do not properly measure their height. This hack gets around the problem.
 	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
 		// Let our parent figure it out most measurements for us
 		super.onMeasure( widthMeasureSpec, heightMeasureSpec );
 		logger.debug("onMeasure "+this+
 				": width: "+decodeMeasureSpec( widthMeasureSpec )+
 				"; height: "+decodeMeasureSpec( heightMeasureSpec )+
 				"; measuredHeight: "+getMeasuredHeight()+
 				"; measuredWidth: "+getMeasuredWidth() );
 
 		int height = 0; // getMeasuredHeight();
 		// logger.debug("Header height is: {}", height);
 		ListAdapter adapter = getAdapter();
 		int count = adapter.getCount();
 		for (int i = 0; i < count; i++) {
 			View child =  adapter.getView(i, null, null);
 			child.measure(widthMeasureSpec, heightMeasureSpec);
 			height += child.getMeasuredHeight();
 		}
 
 		logger.debug("Setting measured dimension to: {}x{}", getMeasuredWidth(), height);
 
 		setMeasuredDimension( getMeasuredWidth(), height );
 	}
 
 	private String decodeMeasureSpec( int measureSpec ) {
 		int mode = View.MeasureSpec.getMode( measureSpec );
 		String modeString = "<> ";
 		switch( mode ) {
 		case View.MeasureSpec.UNSPECIFIED:
 			modeString = "UNSPECIFIED ";
 			break;
 
 		case View.MeasureSpec.EXACTLY:
 			modeString = "EXACTLY ";
 			break;
 
 		case View.MeasureSpec.AT_MOST:
 			modeString = "AT_MOST ";
 			break;
 		}
 		return modeString+Integer.toString( View.MeasureSpec.getSize( measureSpec ) );
 	}
 
 	private DropListener mInnerDropListener =
 		new DropListener() {
 		public void onDrop(int from, int to) {
 			ListAdapter adapter = getAdapter();
 			if (mAllowAdd) {
 				if (from > 0) from -= 1;
 				if (to > 0) to -= 1;
 				adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
 			}
 
 			logger.debug("Adapter: {}", adapter);
 			if (adapter instanceof DraggableAdapter) {
 				logger.debug("Firing onDrop: {} {}", from, to);
 				((DraggableAdapter)adapter).onDrop(from, to);
 				invalidateViews();
 			} else {
 				logger.debug("Not a draggable adapter.");
 			}
 		}
 	};
 
 	private RemoveListener mInnerRemoveListener =
 		new RemoveListener() {
 		public void onRemove(int which) {
 			ListAdapter adapter = getAdapter();
 			if (mAllowAdd) {
 				if (which > 0) which -= 1;
 				adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
 			}
 
 			if (adapter instanceof DraggableAdapter) {
 				logger.debug("Firing onRemove: {}", which);
 				((DraggableAdapter)adapter).onRemove(which);
 				invalidateViews();
 			} else {
 				logger.debug("Not a removable adapter.");
 			}
 		}
 	};
 
 	private DragListener mInnerDragListener =
 		new DragListener() {
 
 		// TODO: This should come from style or something.
 		int backgroundColor = 0xe0103010;
 		int defaultBackgroundColor;
 
 		public void onDragStart(View itemView) {
 			itemView.setVisibility(View.INVISIBLE);
 			defaultBackgroundColor = itemView.getDrawingCacheBackgroundColor();
 			itemView.setBackgroundColor(backgroundColor);
 			ImageView iv = (ImageView)itemView.findViewById(R.id.drag_handle);
 			if (iv != null) iv.setVisibility(View.INVISIBLE);
 		}
 
 		public void onDragStop(View itemView) {
 			itemView.setVisibility(View.VISIBLE);
 			itemView.setBackgroundColor(defaultBackgroundColor);
 			ImageView iv = (ImageView)itemView.findViewById(R.id.drag_handle);
 			if (iv != null) iv.setVisibility(View.VISIBLE);
 		}
 
 	};
 
 	public DraggableListView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	public DraggableListView(Context context) {
 		super(context);
 	}
 
 	public void setAllowLeftRightMovement(boolean b) {
 		mAllowLeftRightMovement = b;
 	}
 
 	/** Resource must include a Button with id interdroid.util.R.add_button **/
 	public void setAddResource(int resc) {
 		mAddResource = resc;
 	}
 
 	public void setAllowAdd(boolean b) {
 		if(getAdapter() != null) {
 			throw new IllegalStateException("You must set allow before setting the adapter.");
 		}
 		mAllowAdd = b;
 	}
 
 	public void setAddListener(AddListener l) {
 		mAllowAdd = true;
 		mAddListener = l;
 	}
 
 	@Override
 	public void setAdapter(ListAdapter adapter) {
 		if (!(adapter instanceof DraggableAdapter)) {
 			throw new IllegalArgumentException("Adapter for a DraggableListView must be a DraggableAdapter");
 		}
 		if (mAllowAdd) {
 			View header = inflate(getContext(), mAddResource, null);
 			ImageButton addButton = (ImageButton) header.findViewById(R.id.add_button);
 			addButton.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					logger.debug("Add button clicked.");
 					if (mAddListener != null) {
 						logger.debug("Firing add event.");
 						mAddListener.onAddItem();
 					}
 				}
 
 			});
 			addHeaderView(header);
 		}
 		super.setAdapter(adapter);
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent ev) {
 		final int action = ev.getAction();
 		final int x = (int) ev.getX();
 		final int y = (int) ev.getY();
 
 		// How wide is our drag target?
 		int touched = pointToPosition(x, y);
 		int minX = 0;
 		int maxX = 0;
 
 
 		// Break out if they touched the add view
 		if (!mDragMode && mAllowAdd && touched == 0) {
 			return false;
 		}
 
 		if (touched != INVALID_POSITION && touched != 0) {
 			View tView = getChildAt(touched);
 			if (tView != null) {
 				tView = tView.findViewById(R.id.drag_handle);
 				minX = tView.getLeft();
 				maxX = tView.getRight();
 			}
 		}
 
 		if (!mRemoving && action == MotionEvent.ACTION_DOWN && x >= minX && x <= maxX) {
 			mDragMode = true;
 		}
 
 		if (!mDragMode) {
 			// Check if we are pressing the remove button
 			if (touched != INVALID_POSITION) {
 				ImageView button = (ImageView) getChildAt(touched).findViewById(R.id.remove_button);
 				switch (action) {
 				case MotionEvent.ACTION_UP:
 					logger.debug("Releasing: {} {}", x, y);
 					logger.debug("{} {}", button.getLeft(), button.getRight());
 					logger.debug("{} {}", mRemoveTop, mRemoveBottom);
 					if (mRemoving && x >= button.getLeft() && x <= button.getRight() && y >= mRemoveTop && y <= mRemoveBottom) {
 						logger.debug("Remove button pressed.");
 						if (mInnerRemoveListener != null) {
 							mInnerRemoveListener.onRemove(touched);
 						}
 					}
 					button.setImageResource(R.drawable.remove_button);
 					button.postInvalidate();
 					mRemoving = false;
 					break;
 				case MotionEvent.ACTION_MOVE:
 					if (mRemoving) {
 						logger.debug("Remove button moved: {} {}", x, y);
 						logger.debug("{} {}", button.getLeft(), button.getRight());
 						logger.debug("{} {}", mRemoveTop, mRemoveBottom);
 						if (x >= button.getLeft() && x <= button.getRight() && y >= mRemoveTop && y <= mRemoveBottom) {
 							logger.debug("Showing as pressed.");
 							button.setImageResource(R.drawable.remove_button_pressed);
 							button.postInvalidate();
 						} else {
 							button.setImageResource(R.drawable.remove_button);
 							button.postInvalidate();
 						}
 					}
 				case MotionEvent.ACTION_DOWN:
 					if (!mRemoving && x >= button.getLeft() && x <= button.getRight()) {
 						mRemoving = true;
 						mRemoveTop = getChildAt(touched).getTop();
 						mRemoveBottom = getChildAt(touched).getBottom();
 						logger.debug("Remove button pressed: {} {}", mRemoveTop, mRemoveBottom);
 						button.setImageResource(R.drawable.remove_button_pressed);
 						button.postInvalidate();
 					}
 					break;
 				}
 			}
 		} else {
 			switch (action) {
 			case MotionEvent.ACTION_DOWN: {
 				mStartPosition = touched;
 				int mItemPosition = mStartPosition - getFirstVisiblePosition();
 				logger.debug("Drag: {}", mItemPosition);
 				if (mStartPosition != INVALID_POSITION) {
 					mDragOffset = y - getChildAt(mItemPosition).getTop();
 					mDragOffset -= ((int)ev.getRawY()) - y;
 					startDrag(mItemPosition,y);
 					logger.debug("Drag Start: {} {} :" + y, getTop(), getBottom());
 					drag(mAllowLeftRightMovement ? x : 0,y);
 
 					// Now we need to try to turn off interception
 					requestDisallowInterceptRecursive(getRootView(), true);
 				}
 			}
 			break;
 			case MotionEvent.ACTION_MOVE: {
 				logger.debug("Drag: {} {} :", y, getBottom() - getTop());
 				if ( y >= 0 && y <= getBottom() - getTop())
 					drag(mAllowLeftRightMovement ? x : 0, y);
 			}
 			break;
 			case MotionEvent.ACTION_CANCEL:
 			case MotionEvent.ACTION_UP:
 			default: {
 				mDragMode = false;
 				mEndPosition = touched;
 				logger.debug("Checking end: {} {}", mEndPosition, getCount() - 1);
 				if (mEndPosition == getCount() - 1) {
 					View child = getChildAt(mEndPosition);
 					int top = y - (mDragView.getHeight() / 2);
 					logger.debug("Checking top: {} {}", top, child.getTop());
 					if (top > child.getTop()) {
 						logger.debug("After end.");
 						mEndPosition += 1;
 					}
 				}
 				logger.debug("Dropped: {} {}", mStartPosition, mEndPosition);
 				stopDrag(mStartPosition - getFirstVisiblePosition());
 				if (mStartPosition != INVALID_POSITION && mEndPosition != INVALID_POSITION && mStartPosition != mEndPosition)
 					mInnerDropListener.onDrop(mStartPosition, mEndPosition);
 
 				// Now we need to try to turn on interception again
 				requestDisallowInterceptRecursive(getRootView(), false);
 			}
 			break;
 			}
 		}
 		return true;
 	}
 
 	// Hack because PhoneDecore doesn't pass the request to children properly.
 	private void requestDisallowInterceptRecursive(View root, boolean disallow) {
 		if (root instanceof ViewGroup) {
 			ViewGroup rootGroup = (ViewGroup)root;
 			rootGroup.requestDisallowInterceptTouchEvent(disallow);
 			for (int i = 0; i < rootGroup.getChildCount(); i++) {
 				requestDisallowInterceptRecursive(rootGroup.getChildAt(i), disallow);
 			}
 		}
 	}
 
 	private void drag(int x, int y) {
 		if (mDragView != null) {
 			WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mDragView.getLayoutParams();
 			layoutParams.x = x;
 			layoutParams.y = y - mDragOffset;
 			WindowManager mWindowManager = (WindowManager) getContext()
 			.getSystemService(Context.WINDOW_SERVICE);
 			mWindowManager.updateViewLayout(mDragView, layoutParams);
 		}
 	}
 
 	private void startDrag(int itemIndex, int y) {
 		stopDrag(itemIndex);
 
 		View item = getChildAt(itemIndex);
 		if (item == null) return;
 		item.setDrawingCacheEnabled(true);
 		mInnerDragListener.onDragStart(item);
 
 		// Create a copy of the drawing cache so that it does not get recycled
 		// by the framework when the list tries to clean up memory
 		Bitmap bitmap = Bitmap.createBitmap(item.getDrawingCache());
 
 		WindowManager.LayoutParams mWindowParams = new WindowManager.LayoutParams();
 		mWindowParams.gravity = Gravity.TOP;
 		mWindowParams.x = 0;
 		mWindowParams.y = y - mDragOffset;
 
 		mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
 		mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
 		mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
 		| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
 		| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
 		| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
 		| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
 		mWindowParams.format = PixelFormat.TRANSLUCENT;
 		mWindowParams.windowAnimations = 0;
 
 		Context context = getContext();
 		ImageView v = new ImageView(context);
 		v.setImageBitmap(bitmap);
 
 		WindowManager mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
 		mWindowManager.addView(v, mWindowParams);
 		mDragView = v;
 	}
 
 	private void stopDrag(int itemIndex) {
 		if (mDragView != null) {
 			mInnerDragListener.onDragStop(getChildAt(itemIndex));
 			mDragView.setVisibility(GONE);
 			WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
 			wm.removeView(mDragView);
 			mDragView.setImageDrawable(null);
 			mDragView = null;
 		}
 	}
 }
