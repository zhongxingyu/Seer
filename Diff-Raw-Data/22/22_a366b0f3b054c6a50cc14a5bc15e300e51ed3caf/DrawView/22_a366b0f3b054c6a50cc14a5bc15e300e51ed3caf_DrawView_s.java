 package usask.hci.fastdraw;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class DrawView extends View {
 	private Bitmap mBitmap;
     private Canvas mCanvas;
     private Paint mBitmapPaint;
 	private Tool mTool;
 	private final int mCols = 5;
 	private final int mRows = 6;
 	private float mColWidth;
 	private float mRowHeight;
 	private boolean showCM;
 	private Paint mCMPaint;
 	private int mSelected;
 
     public DrawView(Context c) {
         super(c);
 
 		mTool = new PenTool(Color.RED, 16);
         mBitmapPaint = new Paint(Paint.DITHER_FLAG);
         mCMPaint = new Paint();
         mCMPaint.setTextSize(26);
         mCMPaint.setTextAlign(Align.CENTER);
         mSelected = -1;
     }
 	
 	@Override
 	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 		super.onSizeChanged(w, h, oldw, oldh);
         mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
         mCanvas = new Canvas(mBitmap);
         mCanvas.drawRGB(0xFF, 0xFF, 0xFF);
         mColWidth = (float)w / mCols;
         mRowHeight = (float)h / mRows;
 	}
 
     @Override
     protected void onDraw(Canvas canvas) {
         canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
         
     	mCMPaint.setColor(0x88FFFF00);
     	canvas.drawRect(0, mRowHeight * (mRows - 1), mColWidth, mRowHeight * mRows, mCMPaint);
     	mCMPaint.setColor(0x88888888);
     	
         if (showCM) {
         	for (int i = 0; i < mRows; i++) {
         		float top = i * mRowHeight;    		
         		canvas.drawLine(0, top, mColWidth * mCols, top, mCMPaint);
         	}
         	for (int i = 0; i < mCols; i++) {
         		float left = i * mColWidth;    		
         		canvas.drawLine(left, 0, left, mRowHeight * mRows, mCMPaint);
         	}
         } else {
         	float top = mRowHeight * (mRows - 1);
         	float right = mColWidth;
         	canvas.drawLine(0, top, right, top, mCMPaint);
         	canvas.drawLine(right, top, right, mRowHeight * mRows, mCMPaint);
         }
 
     	float top = mRowHeight * (mRows - 1);
         canvas.drawText(mTool.getName(), mColWidth / 2, top + mRowHeight / 2, mCMPaint);
     }
     
     @Override
     public boolean onTouchEvent(MotionEvent event) {
     	int index = event.getActionIndex();
         float x = event.getX(index);
         float y = event.getY(index);
         int id = event.getPointerId(index);
 
         switch (event.getActionMasked()) {
             case MotionEvent.ACTION_DOWN:
             case MotionEvent.ACTION_POINTER_DOWN:
             	if (event.getPointerCount() == 1 && x < mColWidth && y > mRowHeight * (mRows - 1)) {
             		showCM = true;
             	} else if (showCM && event.getPointerCount() == 2) {
             		int col = (int) (x / mColWidth);
             		int row = (int) (y / mRowHeight);
             		mSelected = row * mCols + col;
             	}
             	
             	if (showCM)
             		break;
             	
             	mTool.touchStart(id, x, y, mCanvas);
                 break;
                 
             case MotionEvent.ACTION_MOVE:
             	if (showCM)
             		break;
             	
             	int count = event.getPointerCount();
             	
             	for (int i = 0; i < count; i++) {
                     float x2 = event.getX(i);
                     float y2 = event.getY(i);
                 	mTool.touchMove(event.getPointerId(i), x2, y2, mCanvas);
             	}
             	
                 break;
                 
             case MotionEvent.ACTION_UP:
             case MotionEvent.ACTION_POINTER_UP:
             	if (showCM) {
             		if (event.getPointerCount() == 1) {
             			showCM = false;
            			handleSelection(mSelected);
             		}
             		
             		break;
             	}
             	
             	mTool.touchStop(id, x, y, mCanvas);
                 break;
         }
 
         invalidate();
         return true;
     }
     
    private void handleSelection(int selected) {
     	switch (selected) {
    		case 0:
    			mTool = new PencilTool(Color.RED);
    			break;
    		case 1:
    			mTool = new PenTool(Color.RED, 16);
    			break;
    		case 2:
    			mTool = new EraserTool();
    			break;
    		default:
    			break;
     	}
     }
 }
