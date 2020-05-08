 package net.winterroot.hzsv;
 
 import java.io.File;
 
import com.amci.nissan360.Nissan360;
 import com.amci.nissan360.R;
import com.amci.nissan360.Utilities;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.graphics.Point;
 import android.graphics.drawable.Drawable;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.widget.CursorAdapter;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.Display;
 import android.view.MotionEvent;
 import android.view.WindowManager;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.HorizontalScrollView;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 
 
 
 public class HorizontalZoomingCarousel extends HorizontalScrollView  {
 
 	HorizontalZoomingCarouselListener listener = null;
 	Context c;
 
 	int edgeBufferWidth = 150;
 	public int matrixMultiplier = 1;
 
 	int itemWidth = 0;
 	int itemHeight = 0;
 	int viewWidth = 656; // Hard coded for target device for now
 
 	ImageView[] imageViews;
 
 	LinearLayout mScrollableArea = null;
 
 	CarouselAdapter mAdapter;
 
 	int currentSelectedIndex = 0;
 
 	boolean zoomEnabled = true;
	
	public int flingVelocity = 100;
 
 	public HorizontalZoomingCarousel(Context context, int setItemWidth, int setItemHeight) {
 		super(context);
 		c = context;
 		itemWidth = setItemWidth;
 		itemHeight = setItemHeight;
 
 	}
 
 	public void layoutImageViews(){
 		/*WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
 		Display display = wm.getDefaultDisplay();
 		Point size = new Point();
 		display.getSize(size);
 		 */
 
 		//int screenWidth = size.x;
 		int thisViewWidth = getWidth();
 
 		edgeBufferWidth = (viewWidth - itemWidth * matrixMultiplier) / 2;
 
 		mScrollableArea	 = new LinearLayout(c);	
 		mScrollableArea.setOrientation(LinearLayout.HORIZONTAL);
 		android.view.ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, itemHeight * matrixMultiplier);
 		mScrollableArea.setLayoutParams(params);
 
 		LinearLayout.LayoutParams bufferLayoutParams =  new LinearLayout.LayoutParams(edgeBufferWidth, LayoutParams.MATCH_PARENT);
 		LinearLayout edgeBuffer = new LinearLayout(c);
 		edgeBuffer.setLayoutParams(bufferLayoutParams);
 		mScrollableArea.addView(edgeBuffer);
 
 		for(ImageView iv : imageViews){
 			//android.view.ViewGroup.LayoutParams lp = new android.view.ViewGroup.LayoutParams(216, LayoutParams.WRAP_CONTENT);
 			//iv.setLayoutParams(lp);
 			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(itemWidth * matrixMultiplier, itemHeight * matrixMultiplier);
 			iv.setLayoutParams(layoutParams);
 			mScrollableArea.addView(iv);
 		}
 
 		LinearLayout edgeBufferRight = new LinearLayout(c);
 		edgeBufferRight.setLayoutParams(bufferLayoutParams);
 		mScrollableArea.addView(edgeBufferRight);
 
 		addView(mScrollableArea);
 
 		adjustImageSizes();
 
 	}
 
 	public void setListener(HorizontalZoomingCarouselListener theListener){
 		listener = theListener;
 	}
 
 	/*  Not going to use these ones
 	public HorizonalZoomingCarousel(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		c= context;
 	}
 
 	public HorizonalZoomingCarousel(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		c= context;
 	}
 	 */
 
 	@Override
	public void fling (int velocity)
 	{
		if(velocity > flingVelocity){
			velocity = flingVelocity;
		}
		super.fling(velocity);

 	}
 
 
 
 	@Override
 	protected void onLayout (boolean changed, int left, int top, int right, int bottom){
 		super.onLayout(changed, left, top, right, bottom);
 		adjustImageSizes();
 	}
 
 
 
 
 	private void adjustImageSizes(){
 
 		if(!zoomEnabled){
 			return;
 		}
 
 		WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
 		Display display = wm.getDefaultDisplay();
 		Point size = new Point();
 		display.getSize(size);
 		int screenWidth = size.x;
 
 		int width = getWidth();
 		width = viewWidth; // To do this correctly we need to know the calculated width of the scrollview
 		// which is tricky because it's a calculated value
 		int paddingLeft = (screenWidth - width) / 2;
 		if(imageViews != null){
 			for(ImageView iv : imageViews){
 				int[] location = new int[2];
 				iv.getLocationOnScreen(location);
 
 				int imageViewCenter = (location[0] + (itemWidth * matrixMultiplier) / 2) - paddingLeft; // This can be calculated for each imageView that is on screen
 
 				if(imageViewCenter < 0){
 					imageViewCenter = 0;
 				}
 				if(imageViewCenter > width){
 					imageViewCenter = width;
 				}
 
 				int scrollerCenter =  getWidth() / 2; //(getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
 				float offset = Math.abs(scrollerCenter - imageViewCenter);
 
 				float ratio = ( offset / ( width / 2)  );
 
 				// Scale the size.
 				float scale = .5f + (1.0f - ratio) / 2;
 				if (scale <= 0) {
 					scale = 0.1f;
 				}
 				scale = scale * matrixMultiplier;
 
 				// Compute the matrix
 				Matrix m = new Matrix();
 				m.reset();
 
 				// Middle of the image should be the scale pivot
 				m.postScale(scale, scale);
 
 				//center in frame.
 				int offsetInFrame = (int) ((itemWidth * matrixMultiplier) - (itemWidth * matrixMultiplier) * scale) / 2;
 				m.postTranslate(offsetInFrame, 0);
 
 				iv.setScaleType(ScaleType.MATRIX);
 				iv.setImageMatrix(m);
 			}
 		}
 	}
 
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		boolean result = super.onTouchEvent(event);
 
 		switch(event.getAction()) {
 		case(MotionEvent.ACTION_DOWN):
 			if(listener != null){
 				listener.onStartedMove();
 			}
 		break;
 		case(MotionEvent.ACTION_UP):
 
 			int itemWidth = this.itemWidth * matrixMultiplier;
 
 		int scrollX = getScrollX();
 		int page = (scrollX + (itemWidth * matrixMultiplier) / 2) / (itemWidth * matrixMultiplier);
 		if(page >= imageViews.length){
 			page = imageViews.length-1;
 		}
 		// Sometimes imageVies is 0 here, leading to a -1
 
 		this.scrollToPage(page);
 
 		break;
 		}
 
 		return result;
 	}
 
 	public void setAdapter(CarouselAdapter carouselAdapter) {
 		mAdapter = carouselAdapter;
 	}
 
 	public void cursorUpdate(){
 		// As a hack, we'll just read from the adapter here and do the layout
 		// Ideally this should be doing some kind of observing
 		if(mScrollableArea != null){
 			removeView(mScrollableArea);
 		}
 		mScrollableArea = null;
 		imageViews = null;
 		System.gc(); // They say to garbage collect here..
 		// http://stackoverflow.com/questions/3117429/garbage-collector-in-android
 
 		imageViews = new ImageView[mAdapter.getCount()];
 		// This logic should move to the adapter, to take advantage of view reuse
 		for(int i=0; i< mAdapter.getCount(); i++){
 			ImageView iv = new ImageView(c);  // Have to wonder about this and the memory leaks
 			Cursor c = mAdapter.getCursor();
 			c.moveToPosition(i);
 			String imagePath = getContext().getFilesDir().getPath() + "/" + c.getString(c.getColumnIndex(CarouselAdapter.IMAGE_COLUMN));
 
			Utilities.setImageBitmapNissan360(this, iv, imagePath)
 			iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
 			imageViews[i] = iv;
 		}
 
 		layoutImageViews();
 
 
 
 	}
 
 	public void scrollToPage(int page) {
 		int offset = page * itemWidth * matrixMultiplier;
 		this.smoothScrollTo(offset, 0);
 
 		currentSelectedIndex = page;
 
 	}    
 
 	private static final int WHAT = 1;
 
 	class MyHandler extends Handler {
 		@Override
 		public void handleMessage(Message msg) {
 			super.handleMessage(msg);
 			if(!hasMessages(WHAT)) {
 				Log.i("TestScroll", "Last msg!. Position from " 
 						+ msg.arg1 + " to " + msg.arg2);   
 				if(listener!=null){
 					listener.onItemSelected(currentSelectedIndex);
 				}
 			}
 		}
 	}
 
 	private Handler mHandler = new MyHandler();
 
 
 	@Override
 	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
 		super.onScrollChanged(l, t, oldl, oldt);

 		adjustImageSizes();

 		final Message msg = Message.obtain();
 		msg.arg1 = oldt;
 		msg.arg2 = t;
 		msg.what = WHAT;
 		mHandler.sendMessageDelayed(msg, 200);
 	}
 
 	/*
 	@Override
 	public void onScrollStateChanged(AbsListView view, int scrollState) {
 		// TODO Auto-generated method stub
 		if(scrollState == 0) {
 			Log.i("a", "scrolling stopped...");
 			listener.onItemSelected(currentSelectedIndex);
 		}
 	}
 	 */
 
 	public int getCurrentSelectedIndex(){
 		return currentSelectedIndex;
 	}
 
 	public void setZoomEnabled(boolean b) {
 		zoomEnabled = false;
 	}
 
 
 
 
 }
