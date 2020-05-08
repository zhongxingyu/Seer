 package ru.rutube.RutubeFeed.helpers;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.PorterDuff;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.util.Log;
 import android.widget.ImageView;
 
 /**
  * Created by tumbler on 06.07.13.
  */
 public class TopRoundCornerBitmapProcessor implements BitmapProcessor {
     private static final String LOG_TAG = TopRoundCornerBitmapProcessor.class.getName();
     private final double mRoundPercent;
     private final double mCropAspect;
 
     public TopRoundCornerBitmapProcessor(double roundPercent, double cropAspect) {
         this.mRoundPercent = roundPercent;
         this.mCropAspect = cropAspect;
     }
 
     @Override
     public Bitmap process(Bitmap bitmap, ImageView imageView) {
         int width = imageView.getWidth();
         Bitmap cropped = cropAspect(bitmap, mCropAspect);
         Bitmap round = roundCorners(cropped, imageView, (int)(width * mRoundPercent));
        if (cropped != null)
            cropped.recycle();
         return round;
     }
 
     private static Bitmap cropAspect(Bitmap bitmap, double cropAspect) {
         if (bitmap == null)
             return null;
         if (bitmap.getWidth() == 0)
             return bitmap;
         double srcAspect = (double)bitmap.getHeight() / (double)bitmap.getWidth();
         int width, height;
         Bitmap cropped;
         if (srcAspect < cropAspect) {
             height = bitmap.getHeight();
             width = (int)(height / cropAspect);
             cropped = Bitmap.createBitmap(bitmap, (bitmap.getWidth() - width) / 2, 0, width, height);
         } else {
             width = bitmap.getWidth();
             height = (int)(width * cropAspect);
             cropped = Bitmap.createBitmap(bitmap, 0, (bitmap.getHeight() - height) / 2, width, height);
         }
         return cropped;
     }
 
     private static Bitmap roundCorners(Bitmap bitmap, ImageView imageView, int roundPixels) {
         Bitmap roundBitmap;
         if (bitmap == null) {
             return bitmap;
         }
 
         int bw = bitmap.getWidth();
         int bh = bitmap.getHeight();
         int vw = imageView.getWidth();
         int vh = imageView.getHeight();
         if (vw <= 0) vw = bw;
         if (vh <= 0) vh = bh;
 
         int width, height;
         Rect srcRect;
         Rect destRect;
         switch (imageView.getScaleType()) {
             case CENTER_INSIDE:
                 float vRation = (float) vw / vh;
                 float bRation = (float) bw / bh;
                 int destWidth;
                 int destHeight;
                 if (vRation > bRation) {
                     destHeight = Math.min(vh, bh);
                     destWidth = (int) (bw / ((float) bh / destHeight));
                 } else {
                     destWidth = Math.min(vw, bw);
                     destHeight = (int) (bh / ((float) bw / destWidth));
                 }
                 int x = (vw - destWidth) / 2;
                 int y = (vh - destHeight) / 2;
                 srcRect = new Rect(0, 0, bw, bh);
                 destRect = new Rect(x, y, x + destWidth, y + destHeight);
                 width = vw;
                 height = vh;
                 break;
             case FIT_CENTER:
             case FIT_START:
             case FIT_END:
             default:
                 vRation = (float) vw / vh;
                 bRation = (float) bw / bh;
                 if (vRation > bRation) {
                     width = (int) (bw / ((float) bh / vh));
                     height = vh;
                 } else {
                     width = vw;
                     height = (int) (bh / ((float) bw / vw));
                 }
                 srcRect = new Rect(0, 0, bw, bh);
                 destRect = new Rect(0, 0, width, height);
                 break;
             case CENTER_CROP:
                 vRation = (float) vw / vh;
                 bRation = (float) bw / bh;
                 int srcWidth;
                 int srcHeight;
                 if (vRation > bRation) {
                     srcWidth = bw;
                     srcHeight = (int) (vh * ((float) bw / vw));
                     x = 0;
                     y = (bh - srcHeight) / 2;
                 } else {
                     srcWidth = (int) (vw * ((float) bh / vh));
                     srcHeight = bh;
                     x = (bw - srcWidth) / 2;
                     y = 0;
                 }
                 width = Math.min(vw, bw);
                 height = Math.min(vh, bh);
                 srcRect = new Rect(x, y, x + srcWidth, y + srcHeight);
                 destRect = new Rect(0, 0, width, height);
                 break;
             case FIT_XY:
                 width = vw;
                 height = vh;
                 srcRect = new Rect(0, 0, bw, bh);
                 destRect = new Rect(0, 0, width, height);
                 break;
             case CENTER:
             case MATRIX:
                 width = Math.min(vw, bw);
                 height = Math.min(vh, bh);
                 x = (bw - width) / 2;
                 y = (bh - height) / 2;
                 srcRect = new Rect(x, y, x + width, y + height);
                 destRect = new Rect(0, 0, width, height);
                 break;
         }
 
         try {
             roundBitmap = getRoundedCornerBitmap(bitmap, roundPixels, srcRect, destRect, width, height);
         } catch (OutOfMemoryError e) {
             Log.e(LOG_TAG, "Can't create bitmap with rounded corners. Not enough memory.");
             roundBitmap = bitmap;
         }
 
         return roundBitmap;
     }
 
     private static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int roundPixels, Rect srcRect, Rect destRect, int width, int height) {
         Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
         Canvas canvas = new Canvas(output);
 
         final Paint paint = new Paint();
         final RectF destRectF = new RectF(destRect.left, destRect.top, destRect.right, destRect.bottom + roundPixels);
 
         paint.setAntiAlias(true);
         canvas.drawARGB(0, 0, 0, 0);
         paint.setColor(0xFF000000);
         canvas.drawRoundRect(destRectF, roundPixels, roundPixels, paint);
 
         paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
         canvas.drawBitmap(bitmap, srcRect, destRectF, paint);
 
         return output;
     }
 }
