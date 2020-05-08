 package tst.drd;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.Window;
 import android.view.WindowManager;
 
 enum ScrollingState { NO_SCROLL, SCROLL_LEFT, SCROLL_RIGHT };
 
 // TODO: NEED TO CLEANUP BITMAPS AND THREADS ON STATE CHANGE!!!
 public class TstActivity extends Activity {
 	
 	public static final String APP_NAME = "REDDIMG";
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		MyView myView = new MyView(getApplicationContext());
 		setContentView(myView);
 	}
 
 	public class MyView extends View implements OnTouchListener {
 		private static final double SCROLL_MARGIN = 5.;
 
 		private RedditLinkQueue linksQueue;
 		private ImageCache imageCache;
 		private int screenW;
 		private int screenH;
 
 		private int currentLinkIndex = 0;
 		private float startY;
 		private float currentY;
 		private float yPos;		
 		private ScrollingState scrollingState;
 		private Bitmap viewBitmap;
 
 		private LinkRenderer linkRenderer;
 
 		private ImagePrefetcher imagePrefetcher;
 		
 		//private Bitmap brokenImg;
 
 
 		public MyView(Context context) {
 			super(context);
 			linksQueue = new RedditLinkQueue();
 			
 			DisplayMetrics displaymetrics = new DisplayMetrics();
 			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
 			wm.getDefaultDisplay().getMetrics(displaymetrics);
 			screenW = displaymetrics.widthPixels;
 			screenH = displaymetrics.heightPixels;
 			ImageResizer imgResizer = new ImageResizer(screenW, screenH);
 			imageCache = new ImageCache(imgResizer);
 			
 			yPos = 0;
 			scrollingState = ScrollingState.NO_SCROLL;
 			
 			linkRenderer = new LinkRenderer(screenW, screenH);
 			
 			imagePrefetcher = new ImagePrefetcher(imageCache, linksQueue);
 			imagePrefetcher.start();
 
 			loadImage();			
 
 			setOnTouchListener(this);
 			
 			//brokenImg = Bitmap.createBitmap( 100, 100, Bitmap.Config.ARGB_8888);
 		}
 
 		public boolean onTouch(View v, MotionEvent event) {
 			if (event.getAction() == MotionEvent.ACTION_DOWN) {
 				if (event.getX() < screenW * (1. / SCROLL_MARGIN)) {
 					scrollingState = ScrollingState.SCROLL_LEFT;
 				} else if (event.getX() > screenW * (1 - 1. / SCROLL_MARGIN)) {
 					scrollingState = ScrollingState.SCROLL_RIGHT;
 				} else {
 					startY = event.getY();
 				}
 			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
 				if(scrollingState == ScrollingState.NO_SCROLL) {
 					currentY = event.getY();
 					postInvalidate();
 				}
 			} else if (event.getAction() == MotionEvent.ACTION_UP) {
 				if (scrollingState == ScrollingState.SCROLL_LEFT && currentLinkIndex > 0) {
 					currentLinkIndex--;
 					loadImage();
 				} else if (scrollingState == ScrollingState.SCROLL_RIGHT) {
 					currentLinkIndex++;
 					loadImage();
 				} else {
 					yPos = yPos + currentY - startY;
 				}
 				currentY = startY = 0;
 				scrollingState = ScrollingState.NO_SCROLL;
 				postInvalidate();
 			}
 			return true;
 		}
 
 		private void loadImage() {
 			RedditLink currentLink = null;
			synchronized (linksQueue) {
				currentLink = linksQueue.get(currentLinkIndex);
			}

			Bitmap image = imageCache.getFromMem(currentLink.getUrl());
 			while (image == null) {
 				try {
 					Thread.sleep(250);
 				} catch (InterruptedException e) {
 
 				}
 				image = imageCache.getFromMem(currentLink.getUrl());
 			}
 
 			Log.d(TstActivity.APP_NAME, currentLink.getUrl() + " found in mem cache");
 
 			if (viewBitmap != null) {
 				viewBitmap.recycle();
 			}
 			viewBitmap = linkRenderer.render(currentLink, image);
 			yPos = 0;
 		}
 
 		@Override
 		protected void onDraw(Canvas canvas) {
 			super.onDraw(canvas);
 			float actualY = yPos + currentY - startY;
 			if(actualY < -viewBitmap.getHeight() + screenH) {
 				actualY = -viewBitmap.getHeight() + screenH;
 			} 
 			if(actualY > 0) {
 				actualY = 0;
 			}
 			canvas.drawBitmap(viewBitmap, 0, actualY, null);
 		}
 
 	}
 }
