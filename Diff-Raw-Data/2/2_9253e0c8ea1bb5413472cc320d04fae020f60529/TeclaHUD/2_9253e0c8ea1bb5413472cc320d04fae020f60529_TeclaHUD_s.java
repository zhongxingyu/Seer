 package ca.idrc.tecla;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.os.Handler;
 import android.os.Message;
 import android.util.AttributeSet;
 import android.view.Display;
 import android.view.View;
 import android.view.WindowManager;
 
 public class TeclaHUD extends View {
 
 	protected ArrayList<TeclaHUDAsset> mHUDAssets = new ArrayList<TeclaHUDAsset>();
 	private TeclaHUDAsset mHUDScanAsset_arrow_highlight;
 	private TeclaHUDAsset mHUDScanAsset_dpad_highlight_background;
 	private TeclaHUDAsset mHUDScanAsset_dpad_highlight_border;
 	private TeclaHUDAsset mHUDScanAsset_dpad_center_highlight_border;
 	
 	protected final static long SCAN_PERIOD = 1500;
 	private byte mState;
 	protected final static byte TOTAL_STATES = 5;
 	protected final static byte STATE_UP = 0;
 	protected final static byte STATE_RIGHT = 1;
 	protected final static byte STATE_DOWN = 2;
 	protected final static byte STATE_LEFT = 3;
 	protected final static byte STATE_OK = 4;
 	//protected final static byte STATE_BACK = 5;
 	//protected final static byte STATE_HOME = 6;
 	
 	private int[] mCenterLocation = new int[2];
 	
 	private Matrix matrix = new Matrix();
 	private Paint paint = new Paint();
 	private Point size = new Point();
 
     public TeclaHUD(Context context, AttributeSet attrs) {
         super(context, attrs);
         
         mState = STATE_UP;
         WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
         Display display = wm.getDefaultDisplay();
         Point size = new Point();
         display.getSize(size);
         mCenterLocation[0] = size.x/2;
         mCenterLocation[1] = size.y/2;
     
         Bitmap bmp;
         bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.dpad_background);
         mHUDAssets.add(new TeclaHUDAsset("DPad Background", bmp, 0, 0, 0));  
         bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.dpad_center);
         mHUDAssets.add(new TeclaHUDAsset("DPad Center", bmp, 0, 0, 0)); 
         bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.side_button_ok_symbol);
         mHUDAssets.add(new TeclaHUDAsset("OK Symbol", bmp, 0, 0, 0)); 
         bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.side_button_background);
         mHUDAssets.add(new TeclaHUDAsset("Left Side Button Background", bmp, -180, 320, 0)); 
         bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.right_side_button_background);
         mHUDAssets.add(new TeclaHUDAsset("Right Side Button Background", bmp, 180, 320, 0)); 
         bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.side_button_back_symbol);
         mHUDAssets.add(new TeclaHUDAsset("Back Symbol", bmp, -180, 340, 0)); 
        bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.side_button_home_symbol);
         mHUDAssets.add(new TeclaHUDAsset("Home Symbol", bmp, 180, 340, 0)); 
         bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.dpad_arrow);
         mHUDAssets.add(new TeclaHUDAsset("Up Arrow", bmp, 0, -250, 0)); 
         bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.dpad_arrow);
         mHUDAssets.add(new TeclaHUDAsset("Down Arrow", bmp, 0, 250, 180)); 
         bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.dpad_arrow);
         mHUDAssets.add(new TeclaHUDAsset("Left Arrow", bmp, -250, 0, -90)); 
         bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.dpad_arrow);
         mHUDAssets.add(new TeclaHUDAsset("Right Arrow", bmp, 250, 0, 90));
         
         bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.dpad_arrow_highlighted);
         mHUDScanAsset_arrow_highlight = new TeclaHUDAsset("Arrow Highlight", bmp, 0, 0, 0); 
         bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.dpad_highlight_background);
         mHUDScanAsset_dpad_highlight_background = new TeclaHUDAsset("DPad Highlight Background", bmp, 0, 0, 0);  
         bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.dpad_highlight_border);
         mHUDScanAsset_dpad_highlight_border = new TeclaHUDAsset("DPad Highlight Border", bmp, 0, 0, 0); 
         bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.dpad_center_highlight_border);
         mHUDScanAsset_dpad_center_highlight_border = new TeclaHUDAsset("DPad Center Highlight Border", bmp, 0, 0, 0);  
                 
         mAutoScanHandler.sleep(1000);
     }
 
     @Override
     public void onLayout(boolean changed, int left, int top, int right, int bottom) {
         super.onLayout(changed, left, top, right, bottom);
     }
 
     @Override
     public void onDraw(Canvas c) {
     	for (TeclaHUDAsset t: mHUDAssets) {
     		matrix.setRotate(t.mAngleDegree, t.mBmp.getWidth()/2, t.mBmp.getHeight()/2);
     		matrix.postTranslate(mCenterLocation[0]+t.mScreenLocationOffset[0]-t.mBmp.getWidth()/2, 
     				mCenterLocation[1]+t.mScreenLocationOffset[1]-t.mBmp.getHeight()/2);
     		paint.setAlpha(t.mAlpha);
     		c.drawBitmap(t.mBmp, matrix, paint);
     	}
 
         switch (mState) {
         case (STATE_UP):
         	matrix.setTranslate(mCenterLocation[0] + 0 - mHUDScanAsset_dpad_highlight_background.mBmp.getWidth()/2, 
     				mCenterLocation[1] - 180 - mHUDScanAsset_dpad_highlight_background.mBmp.getHeight()/2);
     		paint.setAlpha(mHUDScanAsset_dpad_highlight_background.mAlpha);
     		c.drawBitmap(mHUDScanAsset_dpad_highlight_background.mBmp, matrix, paint);
     			
     		matrix.setTranslate(mCenterLocation[0] + 0 - mHUDScanAsset_dpad_highlight_border.mBmp.getWidth()/2, 
     			mCenterLocation[1] - 180 - mHUDScanAsset_dpad_highlight_border.mBmp.getHeight()/2);
     		paint.setAlpha(mHUDScanAsset_dpad_highlight_border.mAlpha);
     		c.drawBitmap(mHUDScanAsset_dpad_highlight_border.mBmp, matrix, paint);
     			
     		matrix.setTranslate(mCenterLocation[0] + 0 - mHUDScanAsset_arrow_highlight.mBmp.getWidth()/2, 
     			mCenterLocation[1] - 250 - mHUDScanAsset_arrow_highlight.mBmp.getHeight()/2);
     		paint.setAlpha(mHUDScanAsset_arrow_highlight.mAlpha);
     		c.drawBitmap(mHUDScanAsset_arrow_highlight.mBmp, matrix, paint);
         	break; 
         case (STATE_RIGHT):
         	matrix.setRotate(90, 
         			mHUDScanAsset_dpad_highlight_background.mBmp.getWidth()/2, 
         			mHUDScanAsset_dpad_highlight_background.mBmp.getHeight()/2);
 			matrix.postTranslate(mCenterLocation[0] + 180 - mHUDScanAsset_dpad_highlight_background.mBmp.getWidth()/2, 
 				mCenterLocation[1] + 0 - mHUDScanAsset_dpad_highlight_background.mBmp.getHeight()/2);
 			paint.setAlpha(mHUDScanAsset_dpad_highlight_background.mAlpha);
 			c.drawBitmap(mHUDScanAsset_dpad_highlight_background.mBmp, matrix, paint);
 			
 			matrix.setRotate(90, 
 					mHUDScanAsset_dpad_highlight_border.mBmp.getWidth()/2, 
 					mHUDScanAsset_dpad_highlight_border.mBmp.getHeight()/2);
 			matrix.postTranslate(mCenterLocation[0] + 180 - mHUDScanAsset_dpad_highlight_border.mBmp.getWidth()/2, 
 				mCenterLocation[1] + 0 - mHUDScanAsset_dpad_highlight_border.mBmp.getHeight()/2);
 			paint.setAlpha(mHUDScanAsset_dpad_highlight_border.mAlpha);
 			c.drawBitmap(mHUDScanAsset_dpad_highlight_border.mBmp, matrix, paint);
 			
 			matrix.setRotate(90, 
 					mHUDScanAsset_arrow_highlight.mBmp.getWidth()/2, 
 					mHUDScanAsset_arrow_highlight.mBmp.getHeight()/2);
 			matrix.postTranslate(mCenterLocation[0] + 250 - mHUDScanAsset_arrow_highlight.mBmp.getWidth()/2, 
 				mCenterLocation[1] + 0 - mHUDScanAsset_arrow_highlight.mBmp.getHeight()/2);
 			paint.setAlpha(mHUDScanAsset_arrow_highlight.mAlpha);
 			c.drawBitmap(mHUDScanAsset_arrow_highlight.mBmp, matrix, paint);
         	break; 
         case (STATE_DOWN):
         	matrix.setRotate(180, 
         			mHUDScanAsset_dpad_highlight_background.mBmp.getWidth()/2, 
         			mHUDScanAsset_dpad_highlight_background.mBmp.getHeight()/2);
 			matrix.postTranslate(mCenterLocation[0] + 0 - mHUDScanAsset_dpad_highlight_background.mBmp.getWidth()/2, 
 				mCenterLocation[1] + 180 - mHUDScanAsset_dpad_highlight_background.mBmp.getHeight()/2);
 			paint.setAlpha(mHUDScanAsset_dpad_highlight_background.mAlpha);
 			c.drawBitmap(mHUDScanAsset_dpad_highlight_background.mBmp, matrix, paint);
 			
 			matrix.setRotate(180, 
 					mHUDScanAsset_dpad_highlight_border.mBmp.getWidth()/2, 
 					mHUDScanAsset_dpad_highlight_border.mBmp.getHeight()/2);
 			matrix.postTranslate(mCenterLocation[0] + 0 - mHUDScanAsset_dpad_highlight_border.mBmp.getWidth()/2, 
 				mCenterLocation[1] + 180 - mHUDScanAsset_dpad_highlight_border.mBmp.getHeight()/2);
 			paint.setAlpha(mHUDScanAsset_dpad_highlight_border.mAlpha);
 			c.drawBitmap(mHUDScanAsset_dpad_highlight_border.mBmp, matrix, paint);
 			
 			matrix.setRotate(180, 
 					mHUDScanAsset_arrow_highlight.mBmp.getWidth()/2, 
 					mHUDScanAsset_arrow_highlight.mBmp.getHeight()/2);
 			matrix.postTranslate(mCenterLocation[0] + 0 - mHUDScanAsset_arrow_highlight.mBmp.getWidth()/2, 
 				mCenterLocation[1] + 250 - mHUDScanAsset_arrow_highlight.mBmp.getHeight()/2);
 			paint.setAlpha(mHUDScanAsset_arrow_highlight.mAlpha);
 			c.drawBitmap(mHUDScanAsset_arrow_highlight.mBmp, matrix, paint);
         	break; 
         case (STATE_LEFT):
         	matrix.setRotate(-90, 
         			mHUDScanAsset_dpad_highlight_background.mBmp.getWidth()/2, 
         			mHUDScanAsset_dpad_highlight_background.mBmp.getHeight()/2);
 			matrix.postTranslate(mCenterLocation[0] - 180 - mHUDScanAsset_dpad_highlight_background.mBmp.getWidth()/2, 
 				mCenterLocation[1] + 0 - mHUDScanAsset_dpad_highlight_background.mBmp.getHeight()/2);
 			paint.setAlpha(mHUDScanAsset_dpad_highlight_background.mAlpha);
 			c.drawBitmap(mHUDScanAsset_dpad_highlight_background.mBmp, matrix, paint);
 			
 			matrix.setRotate(-90, 
 					mHUDScanAsset_dpad_highlight_border.mBmp.getWidth()/2, 
 					mHUDScanAsset_dpad_highlight_border.mBmp.getHeight()/2);
 			matrix.postTranslate(mCenterLocation[0] - 180 - mHUDScanAsset_dpad_highlight_border.mBmp.getWidth()/2, 
 				mCenterLocation[1] + 0 - mHUDScanAsset_dpad_highlight_border.mBmp.getHeight()/2);
 			paint.setAlpha(mHUDScanAsset_dpad_highlight_border.mAlpha);
 			c.drawBitmap(mHUDScanAsset_dpad_highlight_border.mBmp, matrix, paint);
 			
 			matrix.setRotate(-90, 
 					mHUDScanAsset_arrow_highlight.mBmp.getWidth()/2, 
 					mHUDScanAsset_arrow_highlight.mBmp.getHeight()/2);
 			matrix.postTranslate(mCenterLocation[0] - 250 - mHUDScanAsset_arrow_highlight.mBmp.getWidth()/2, 
 				mCenterLocation[1] + 0 - mHUDScanAsset_arrow_highlight.mBmp.getHeight()/2);
 			paint.setAlpha(mHUDScanAsset_arrow_highlight.mAlpha);
 			c.drawBitmap(mHUDScanAsset_arrow_highlight.mBmp, matrix, paint);
         	break; 
         case (STATE_OK):
         	matrix.setTranslate(mCenterLocation[0] + 0 - mHUDScanAsset_dpad_center_highlight_border.mBmp.getWidth()/2, 
     				mCenterLocation[1] + 0 - mHUDScanAsset_dpad_center_highlight_border.mBmp.getHeight()/2);
     		paint.setAlpha(mHUDScanAsset_dpad_center_highlight_border.mAlpha);
     		c.drawBitmap(mHUDScanAsset_dpad_center_highlight_border.mBmp, matrix, paint);
         	break; 
         default: 
         	break; 
         }
     }
 
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
         Display display = wm.getDefaultDisplay();
         display.getSize(size);
         mCenterLocation[0] = size.x/2;
         mCenterLocation[1] = size.y/2;
     
 		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 	}
 	
 	protected void scanForward() {
 		++mState;
 		mState %= TOTAL_STATES;
 		this.invalidate();
 		mAutoScanHandler.sleep(SCAN_PERIOD);
 	}
 
 	protected void scanTrigger() {
 		switch (mState) {
 		case(STATE_UP):
 			TeclaAccessibilityService.selectNode(TeclaAccessibilityService.DIRECTION_UP);
 			break; 
 		case(STATE_RIGHT):
 			TeclaAccessibilityService.selectNode(TeclaAccessibilityService.DIRECTION_RIGHT);
 			break; 
 		case(STATE_DOWN):
 			TeclaAccessibilityService.selectNode(TeclaAccessibilityService.DIRECTION_DOWN);
 			break; 
 		case(STATE_LEFT):
 			TeclaAccessibilityService.selectNode(TeclaAccessibilityService.DIRECTION_LEFT);
 			break; 
 		case(STATE_OK):
 			TeclaAccessibilityService.clickActiveNode();
 			break; 
 		default: 
 			break; 
 		}
 		mAutoScanHandler.sleep(SCAN_PERIOD);
 	}
 	
 	class AutoScanHandler extends Handler {
 		public AutoScanHandler() {
 			
 		}
 
         @Override
         public void handleMessage(Message msg) {
         	TeclaHUD.this.scanForward();
         }
 
         public void sleep(long delayMillis) {
                 removeMessages(0);
                 sendMessageDelayed(obtainMessage(0), delayMillis);
         }
 	}
 	AutoScanHandler mAutoScanHandler = new AutoScanHandler();
 	 
 }
