 package fr.ravenfeld.livewallpaper.library.objects;
 
 import android.app.WallpaperManager;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.net.Uri;
 import android.util.Log;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 
 public class Utils {
     private static int SIZE_MAX = 2048;
 
     public static Bitmap compositeDrawableWithMask(
             Bitmap rgbBitmap, Bitmap alphaBitmap) {
         return Utils.compositeDrawableWithMask(rgbBitmap, alphaBitmap, true);
     }
 
     public static Bitmap compositeDrawableWithMask(
             Bitmap rgbBitmap, Bitmap alphaBitmap, boolean recycle) {
 
         int width = rgbBitmap.getWidth();
         int height = rgbBitmap.getHeight();
         if (width != alphaBitmap.getWidth() || height != alphaBitmap.getHeight()) {
             throw new IllegalStateException("image size mismatch!");
         }
         Bitmap destBitmap = Bitmap.createBitmap(width, height,
                 Bitmap.Config.ARGB_8888);
 
         int[] pixels = new int[width];
         int[] alpha = new int[width];
         for (int y = 0; y < height; y++) {
             rgbBitmap.getPixels(pixels, 0, width, 0, y, width, 1);
             alphaBitmap.getPixels(alpha, 0, width, 0, y, width, 1);
 
             for (int x = 0; x < width; x++) {
                 // Replace the alpha channel with the r value from the bitmap.
                 pixels[x] = (pixels[x] & 0x00FFFFFF) | ((alpha[x] << 8) & 0xFF000000);
             }
             destBitmap.setPixels(pixels, 0, width, 0, y, width, 1);
         }
         if (recycle) {
             rgbBitmap.recycle();
             alphaBitmap.recycle();
         }
         return destBitmap;
     }
 
     public static Bitmap rotateBitmap(Bitmap bitmapOriginal, int grados,
                                       boolean recycle) {
         if (grados != 0) {
             int width = bitmapOriginal.getWidth();
             int height = bitmapOriginal.getHeight();
             Matrix matrix = new Matrix();
             matrix.postRotate(grados);
             Bitmap result = Bitmap.createBitmap(bitmapOriginal, 0, 0, width,
                     height, matrix, true);
             if (recycle) {
                 bitmapOriginal.recycle();
             }
 
             bitmapOriginal = result;
         }
 
         return bitmapOriginal;
     }
 
     public static Bitmap compressBitmap(Bitmap bitmapOriginal) {
 
         ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
         bitmapOriginal.compress(Bitmap.CompressFormat.JPEG, 90, arrayOutputStream);
         byte[] data = arrayOutputStream.toByteArray();
         Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
         return bitmap;
     }
 
     public static Bitmap decodeUri(Context context, Uri selectedImage)
             throws FileNotFoundException {
         WallpaperManager wallpaperManager = WallpaperManager
                 .getInstance(context);
         final int REQUIRED_SIZE_WIDTH = wallpaperManager
                 .getDesiredMinimumWidth();
         final int REQUIRED_SIZE_HEIGHT = wallpaperManager
                 .getDesiredMinimumHeight();
         return Utils.decodeUri(context, selectedImage, REQUIRED_SIZE_WIDTH, REQUIRED_SIZE_HEIGHT);
     }
 
     public static Bitmap decodeUri(Context context, Uri selectedImage,
                                    int widthMinimum, int heightMinimum)
             throws FileNotFoundException {
 
         Bitmap destBitmap = BitmapFactory.decodeStream(context.getContentResolver()
                 .openInputStream(selectedImage), null, null);
         int width_tmp = destBitmap.getWidth();
         int height_tmp = destBitmap.getHeight();
         float ratioSize;
         if (heightMinimum < widthMinimum) {
             ratioSize = (float) heightMinimum / (float) height_tmp;
         } else {
             ratioSize = (float) widthMinimum / (float) width_tmp;
         }
         return Bitmap.createScaledBitmap(destBitmap, nextPowerOfTwo((int) (width_tmp * ratioSize)), nextPowerOfTwo((int) (height_tmp * ratioSize)), true);
     }
 
 
     public static Bitmap decodeResource(Context context, int selectedImage) {
         WallpaperManager wallpaperManager = WallpaperManager
                 .getInstance(context);
         final int REQUIRED_SIZE_WIDTH = wallpaperManager
                 .getDesiredMinimumWidth();
         final int REQUIRED_SIZE_HEIGHT = wallpaperManager
                 .getDesiredMinimumHeight();
         return Utils.decodeResource(context, selectedImage, REQUIRED_SIZE_WIDTH, REQUIRED_SIZE_HEIGHT);
     }
 
     public static Bitmap decodeResource(Context context, int selectedImage,
                                         int widthMinimum, int heightMinimum) {
 
         Bitmap destBitmap = BitmapFactory.decodeResource(context.getResources(), selectedImage);
         int width_tmp = destBitmap.getWidth();
         int height_tmp = destBitmap.getHeight();
         float ratioSize;
         if (heightMinimum < widthMinimum) {
             ratioSize = (float) heightMinimum / (float) height_tmp;
         } else {
             ratioSize = (float) widthMinimum / (float) width_tmp;
         }
         if(ratioSize>1){
             ratioSize=1;
         }
         return Bitmap.createScaledBitmap(destBitmap, nextPowerOfTwo((int) (width_tmp * ratioSize)), nextPowerOfTwo((int) (height_tmp * ratioSize)), true);
     }
 
     public static Bitmap decodeResource(Context context, int selectedImage,
                                         float scale) {
         Bitmap destBitmap = BitmapFactory.decodeResource(context.getResources(), selectedImage);
         int width_tmp = destBitmap.getWidth();
         int height_tmp = destBitmap.getHeight();
         if(scale>1){
             scale=1;
         }
         return Bitmap.createScaledBitmap(destBitmap, nextPowerOfTwo((int) (width_tmp * scale)), nextPowerOfTwo((int) (height_tmp * scale)), true);
     }
 
 
     public static float ratioSize(Context context, int selectedBackground) {
         BitmapFactory.Options options = new BitmapFactory.Options();
         options.inJustDecodeBounds = true;
         BitmapFactory.decodeResource(context.getResources(), selectedBackground, options);
         int width_tmp = options.outWidth;
         int height_tmp = options.outHeight;
 
         WallpaperManager wallpaperManager = WallpaperManager
                 .getInstance(context);
         final int REQUIRED_SIZE_WIDTH = wallpaperManager
                 .getDesiredMinimumWidth();
         final int REQUIRED_SIZE_HEIGHT = wallpaperManager
                 .getDesiredMinimumHeight();
 
         if (REQUIRED_SIZE_HEIGHT < REQUIRED_SIZE_WIDTH) {
             return (float) REQUIRED_SIZE_HEIGHT / (float) height_tmp;
         } else {
             return (float) REQUIRED_SIZE_WIDTH / (float) width_tmp;
         }
     }
 
    public static int nextPowerOfTwo(long x) {
         int i = 2;
         double pow = 1;
 
         boolean powerOfTwo = false;
         while (i < SIZE_MAX && !powerOfTwo) {
             if (i >= x) {
                 powerOfTwo = true;
             } else {
 
                 i = (int) Math.pow(2, pow);
                 pow++;
             }
         }
         //Log.e("TEST","SIZE FINAL "+i+" SIZE INIT "+x);
         return i;
     }
 
     public static boolean isPowerOfTwo(long x) {
         return (x != 0) && ((x & (x - 1)) == 0);
     }
 }
