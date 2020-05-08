 package com.android.opengl.utils;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.util.Log;
 
 public class BitmapUtils {
     private static int bitmapW;
     private static int bitmapH;
     private static int bitmapW2N;
     private static int bitmapH2N;
 
     public static Bitmap create2NBitmapFromResource(Context context,
             int resourceId) {
         Bitmap bitmap = createFromResource(context, resourceId);
         if (bitmap != null) {
             bitmap = expandBitmapTo2N(bitmap);//
         }
         return bitmap;
     }
 
     public static Bitmap expandBitmapTo2N(Bitmap pBitmap) {
         Bitmap bitmap = null;
         bitmapW = pBitmap.getWidth();
         bitmapH = pBitmap.getHeight();
         bitmapW2N = OpenGLUtils.getNext2N(bitmapW);
         bitmapH2N = OpenGLUtils.getNext2N(bitmapH);
         Log.d("bitmapW", String.valueOf(bitmapW));
         Log.d("bitmapH", String.valueOf(bitmapH));
         Log.d("bitmapW2N", String.valueOf(bitmapW2N));
         Log.d("bitmapH2N", String.valueOf(bitmapH2N));
 
         bitmap = Bitmap.createBitmap(bitmapW2N, bitmapH2N, Config.ARGB_8888);
        Canvas canvas = new Canvas();
         canvas.setBitmap(bitmap);
         // paint.setARGB(255, 255, 0, 0);
         // canvas.drawRect(0, 0, bitmapW2N, bitmapH2N, paint);
         canvas.drawBitmap(pBitmap, 0, bitmapH2N - bitmapH, null);
         return bitmap;
     }
 
     public static Bitmap[] splitBitmap(Bitmap pBitmap, int nInRow, int length) {
         Bitmap[] bitmaps = new Bitmap[length];
         bitmapW = pBitmap.getWidth();
         bitmapH = pBitmap.getHeight();
         Log.d("BitmapUtils.splitBitmap", "width=" + String.valueOf(bitmapW)
                 + ",height=" + String.valueOf(bitmapH));
         int subW = bitmapW / nInRow;
         Log.d("BitmapUtils.splitBitmap", "subW=" + String.valueOf(subW));
         for (int i = 0; i < length; i++) {
             Log.d("BitmapUtils.splitBitmap", "i=" + String.valueOf(i)
                     + ",subX=" + String.valueOf((i % nInRow) * subW) + ",subY="
                     + String.valueOf(subW * (i / nInRow)));
             bitmaps[i] = Bitmap.createBitmap(pBitmap, (i % nInRow) * subW, subW
                     * (i / nInRow), subW, subW);
             bitmaps[i] = expandBitmapTo2N(bitmaps[i]);
         }
         return bitmaps;
     }
 
     public static Bitmap createFromResource(Context context, int resourceId) {
         Bitmap bitmap = null;
         InputStream is = null;
         is = context.getResources().openRawResource(resourceId);
 
         try {
             bitmap = BitmapFactory.decodeStream(is);
         } finally {
             try {
                 is.close();
             } catch (IOException e) {
                 // Ignore.
             }
         }
 
         return bitmap;
     }
 }
