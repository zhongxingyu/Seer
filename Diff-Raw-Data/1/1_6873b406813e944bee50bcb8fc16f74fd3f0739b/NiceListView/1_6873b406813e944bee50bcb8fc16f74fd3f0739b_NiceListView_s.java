 package jp.gr.java_conf.neko_daisuki.android.widget;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.util.AttributeSet;
 import android.util.SparseArray;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.AbsListView;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 
 import jp.gr.java_conf.neko_daisuki.anaudioplayer.R;
 
 public class NiceListView extends ListView {
 
     private enum Direction {
         UP,
         DOWN;
 
         public int toInteger() {
             return this == UP ? 1 : -1;
         }
     }
 
     private abstract class MoveAction implements Runnable {
 
         public void run() {
             if (!mAutoScrolling) {
                 return;
             }
             work();
         }
 
         protected abstract void work();
     }
 
     private class MoveUpAction extends MoveAction {
 
         protected void work() {
             int offset = computeVerticalScrollOffset();
             int range = computeVerticalScrollRange();
             int extent = computeVerticalScrollExtent();
             if (range <= offset + extent) {
                 return;
             }
             autoScroll(Direction.UP, this);
         }
     }
 
     private class MoveDownAction extends MoveAction {
 
         protected void work() {
             if (computeVerticalScrollOffset() <= 0) {
                 return;
             }
             autoScroll(Direction.DOWN, this);
         }
     }
 
     private class SelfListener implements OnScrollListener {
 
         private class FadeOut implements Runnable {
 
             private class Invisible implements Runnable {
 
                 public void run() {
                     mView.setVisibility(INVISIBLE);
                 }
             }
 
             private ImageView mView;
             private int mCount = 0;
 
             public FadeOut(ImageView view) {
                 mView = view;
             }
 
             public void run() {
                 float alpha = mView.getAlpha();
                 mView.setAlpha(alpha - 0.2f);
                 invalidate();
                 postDelayed(mCount < 4 ? this : new Invisible(), 10);
                 mCount++;
             }
         }
 
         private class InitialOnScrollRunner implements Runnable {
 
             public void run() {
                 mOnScrollRunner = new OnScrollRunner();
             }
         }
 
         private class OnScrollRunner implements Runnable {
 
             public void run() {
                 int offset = computeVerticalScrollOffset();
                 if (offset != mLastOffset) {
                     int extent = computeVerticalScrollExtent();
                     if (offset == 0) {
                         showImage(mTopNoEntryImage);
                     }
                     else if (offset + extent == computeVerticalScrollRange()) {
                         showImage(mBottomNoEntryImage);
                     }
                 }
                 mLastOffset = offset;
             }
         }
 
         private int mLastOffset;
         private Runnable mOnScrollRunner;
 
         public SelfListener() {
             reset();
         }
 
         public void reset() {
             /*
              * ListView invokes onScroll() in layouting after setAdapter(). Such
              * onScroll() invoking can show the no entry icon. So the first
              * invoking must be ignored.
              */
             mOnScrollRunner = new InitialOnScrollRunner();
         }
 
         public void onScroll(AbsListView view, int firstVisibleItem,
                              int visibleItemCount, int totalItemCount) {
             mOnScrollRunner.run();
         }
 
         public void onScrollStateChanged(AbsListView view, int scrollState) {
         }
 
         private void showImage(ImageView view) {
             view.setVisibility(VISIBLE);
             view.setAlpha(1.0f);
             postDelayed(new FadeOut(view), 400);
         }
     }
 
     private interface MotionHandler {
 
         public boolean run(MotionEvent event);
     }
 
     private class DefaultMotionHandler implements MotionHandler {
 
         public boolean run(MotionEvent event) {
             return onMotionDefault(event);
         }
     }
 
     private class MotionUpHandler implements MotionHandler {
 
         public boolean run(MotionEvent event) {
             return onMotionUp(event);
         }
     }
 
     private class MotionDownHandler implements MotionHandler {
 
         public boolean run(MotionEvent event) {
             return onMotionDown(event);
         }
     }
 
     private class MotionMoveHandler implements MotionHandler {
 
         public boolean run(MotionEvent event) {
             return onMotionMove(event);
         }
     }
 
     // internal views
     private ImageView mTopNoEntryImage;
     private ImageView mBottomNoEntryImage;
     private ImageView mArrowUpImage;
     private ImageView mArrowDownImage;
 
     // helpers
     private float mLastPointerY;
     private boolean mAutoScrolling = false;
     private SparseArray<MotionHandler> mMotionHandlers;
     private MotionHandler mDefaultMotionHandler;
     private SelfListener mSelfListener = new SelfListener();
     // cache
     private View[] mViews;
     private MoveAction mMoveUpAction = new MoveUpAction();
     private MoveAction mMoveDownAction = new MoveDownAction();
 
     public NiceListView(Context context) {
         super(context);
         initialize(context);
     }
 
     public NiceListView(Context context, AttributeSet attrs) {
         super(context, attrs);
         initialize(context);
     }
 
     public NiceListView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         initialize(context);
     }
 
     public boolean onTouchEvent(MotionEvent event) {
         MotionHandler entry = mMotionHandlers.get(event.getActionMasked());
         return (entry != null ? entry : mDefaultMotionHandler).run(event);
     }
 
     public void setAdapter(ListAdapter adapter) {
         mSelfListener.reset();
         super.setAdapter(adapter);
     }
 
     protected void dispatchDraw(Canvas canvas) {
         super.dispatchDraw(canvas);
 
         for (View view: mViews) {
             drawImage(canvas, view);
         }
     }
 
     protected void onLayout(boolean changed, int left, int top, int right,
                             int bottom) {
         super.onLayout(changed, left, top, right, bottom);
 
         layoutNoEntryImages(left, top, right, bottom);
         layoutArrows(left, top, right, bottom);
     }
 
     protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 
         int width = MeasureSpec.getSize(widthMeasureSpec);
         int height = MeasureSpec.getSize(heightMeasureSpec);
         int mode = MeasureSpec.UNSPECIFIED;
         int widthSpec = MeasureSpec.makeMeasureSpec(width, mode);
         int heightSpec = MeasureSpec.makeMeasureSpec(height, mode);
         for (View view: mViews) {
             view.measure(widthSpec, heightSpec);
         }
     }
 
     private void initialize(Context context) {
         mTopNoEntryImage = makeNoEntryImageView(context);
         mBottomNoEntryImage = makeNoEntryImageView(context);
         mArrowUpImage = makeArrowImageView(context, R.drawable.ic_arrow_up);
         mArrowDownImage = makeArrowImageView(context, R.drawable.ic_arrow_down);
         mViews = new View[] {
             mArrowUpImage,
             mArrowDownImage,
             mTopNoEntryImage,
             mBottomNoEntryImage
         };
 
         setOverScrollMode(OVER_SCROLL_NEVER);
         setOnScrollListener(mSelfListener);
 
         mMotionHandlers = new SparseArray<MotionHandler>();
         mMotionHandlers.put(MotionEvent.ACTION_DOWN, new MotionDownHandler());
         mMotionHandlers.put(MotionEvent.ACTION_MOVE, new MotionMoveHandler());
         mMotionHandlers.put(MotionEvent.ACTION_UP, new MotionUpHandler());
         mDefaultMotionHandler = new DefaultMotionHandler();
     }
 
     private ImageView makeImageView(Context context, int resId) {
         ImageView view = new ImageView(context);
         view.setImageResource(resId);
         view.setVisibility(INVISIBLE);
         return view;
     }
 
     private ImageView makeArrowImageView(Context context, int resId) {
         ImageView view = makeImageView(context, resId);
         view.setBackgroundColor(Color.argb(64, 0, 0, 0));
         return view;
     }
 
     private ImageView makeNoEntryImageView(Context context) {
         return makeImageView(context, R.drawable.ic_no_entry);
     }
 
     private void drawImage(Canvas canvas, View view) {
         if (view.getVisibility() == VISIBLE) {
             drawChild(canvas, view, 0);
         }
     }
 
     private void layoutNoEntryImages(int left, int top, int right, int bottom) {
         int width = mTopNoEntryImage.getMeasuredWidth();
         int height = mTopNoEntryImage.getMeasuredHeight();
         int l = (right - left - width) / 2;
         int r = l + width;
         mTopNoEntryImage.layout(l, 0, r, height);
 
         int b = bottom - top;
         mBottomNoEntryImage.layout(l, b - height, r, b);
     }
 
     private void layoutArrows(int left, int top, int right, int bottom) {
         int width = right - left;
         int height = mArrowUpImage.getMeasuredHeight();
         mArrowUpImage.layout(0, 0, width, height);
 
         int b = bottom - top;
         mArrowDownImage.layout(0, b - height, width, b);
     }
 
     private boolean onMotionUp(MotionEvent event) {
         mAutoScrolling = false;
         mArrowUpImage.setVisibility(INVISIBLE);
         mArrowDownImage.setVisibility(INVISIBLE);
         return super.onTouchEvent(event);
     }
 
     private boolean onMotionDefault(MotionEvent event) {
         return super.onTouchEvent(event);
     }
 
     private boolean onMotionDown(MotionEvent event) {
         mLastPointerY = event.getY();
         return super.onTouchEvent(event);
     }
 
     private boolean onMotionMove(MotionEvent event) {
         float y = event.getY();
         // The following code suppresses chattering.
         if (Math.abs(mLastPointerY - y) < 8f) {
             return super.onTouchEvent(event);
         }
 
         boolean isDown = mLastPointerY < y;
         mLastPointerY = y;
         View visibleView = isDown ? mArrowDownImage : mArrowUpImage;
         View goneView = isDown ? mArrowUpImage : mArrowDownImage;
         visibleView.setVisibility(VISIBLE);
         goneView.setVisibility(INVISIBLE);
         if ((y < visibleView.getTop()) || (visibleView.getBottom() < y)) {
             if (mAutoScrolling) {
                 /*
                  * smoothScrollByOffset() stops handling of touching. So the
                  * following code restarts it explicitly.
                  */
                 MotionEvent me = MotionEvent.obtain(event);
                 me.setAction(MotionEvent.ACTION_DOWN);
                 super.onTouchEvent(me);
             }
             mAutoScrolling = false;
             return super.onTouchEvent(event);
         }
         if (!mAutoScrolling) {
             post(isDown ? mMoveDownAction : mMoveUpAction);
         }
         mAutoScrolling = true;
         return true;
     }
 
     private void autoScroll(Direction direction, MoveAction action) {
         int direc = direction.toInteger();
         smoothScrollByOffset(direc * computeVerticalScrollRange() / 100);
         postDelayed(action, 50);
     }
 }
 
 // vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
