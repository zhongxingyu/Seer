 package com.github.goscore;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
import android.graphics.Rect;
 import android.view.View;
 
 public class CameraOverlay extends View {
     Bitmap mBoardImage;
 	
 	CameraOverlay(Context context) {
 		super(context);
 	}
 
 	@Override
     protected void onDraw(Canvas canvas) {
     	super.onDraw(canvas);
     	int height = canvas.getHeight();
     	int width = canvas.getWidth();
     	int boxWidth = (width - height) / 2;
     	if (boxWidth <= 100) {
     		boxWidth = 100;
     	}
     	Paint paint = new Paint();
     	paint.setColor(Color.GRAY);
    	paint.setAlpha(192);
     	paint.setStyle(Paint.Style.FILL);
     	canvas.drawRect(0, 0, boxWidth, height, paint);
     	canvas.drawRect(width - boxWidth, 0, width, height, paint);
     	
     	float gridStep = ((float)height) / 40;
     	float gridStart = gridStep * 2;
     	float gridEnd = gridStep * 38;
     	if (mBoardImage != null) {
    		Rect imageTarget = new Rect(
    				(int)(boxWidth + gridStart), (int)gridStart,
    				(int)(boxWidth + gridEnd), (int)gridEnd);
    		Rect imageSource = new Rect(0, 0, mBoardImage.getWidth(), mBoardImage.getHeight());
    		canvas.drawBitmap(mBoardImage, imageSource, imageTarget, paint);
     	} else {
 	    	paint.setColor(Color.RED);
 	    	for (int i = 1; i < 20; i++) {
 	    		float offset = i * 2 * gridStep;
 	    		// vertical line
 	    		canvas.drawLine(boxWidth + offset, gridStart, 
 	    				boxWidth + offset, gridEnd, paint);
 	    		// horizontal line
 	    		canvas.drawLine(boxWidth + gridStart, offset,
 	    				boxWidth + gridEnd, offset, paint);
 	    	}
     	}
 	}
 	
 	public void setBoardImage(Bitmap bitmap) {
 		mBoardImage = bitmap;
 		invalidate();
 	}
 }
