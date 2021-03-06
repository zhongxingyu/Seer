 
 package com.android.gallery3d.filtershow.imageshow;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.Region;
 import android.util.AttributeSet;
 
 public class ImageSmallBorder extends ImageSmallFilter {
 
     // TODO: move this to xml.
     protected final int mSelectedBackgroundColor = Color.WHITE;
     protected final int mInnerBorderColor = Color.BLACK;
     protected final int mInnerBorderWidth = 2;
    protected final float mImageScaleFactor = 3.5f;
 
     public ImageSmallBorder(Context context) {
         super(context);
     }
 
     public ImageSmallBorder(Context context, AttributeSet attrs) {
         super(context, attrs);
     }
 
     @Override
     public void onDraw(Canvas canvas) {
         getFilteredImage();
         if (mIsSelected) {
             canvas.drawColor(mSelectedBackgroundColor);
         } else {
             canvas.drawColor(mBackgroundColor);
         }
         // TODO: simplify & make faster...
         mPaint.setColor(mInnerBorderColor);
         RectF border = new RectF(mMargin, mMargin, getWidth() - mMargin - 1, getHeight() - mMargin);
         canvas.drawLine(0, 0, getWidth(), 0, mPaint);
         mPaint.setStrokeWidth(mInnerBorderWidth);
         Path path = new Path();
         path.addRect(border, Path.Direction.CCW);
         mPaint.setStyle(Paint.Style.STROKE);
         canvas.drawPath(path, mPaint);
         mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
         canvas.save();
         canvas.clipRect(mMargin + 1, mMargin, getWidth() - mMargin - 2, getHeight() - mMargin - 1,
                 Region.Op.INTERSECT);
         canvas.translate(mMargin, mMargin + 1);
         canvas.scale(mImageScaleFactor, mImageScaleFactor);
         Rect d = new Rect(0, 0, getWidth(), getWidth());
         drawImage(canvas, mFilteredImage, d);
         canvas.restore();
     }
 
     @Override
     public void drawImage(Canvas canvas, Bitmap image, Rect d) {
         if (image != null) {
             int iw = image.getWidth();
             int ih = image.getHeight();
             Rect s = new Rect(0, 0, iw, iw);
             canvas.drawBitmap(image, s, d, mPaint);
         }
     }
 }
