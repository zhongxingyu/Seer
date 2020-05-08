 package fr.ravenfeld.livewallpaper.library.objects;
 
 import android.app.WallpaperManager;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.net.Uri;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 
 public class Utils {

     public static Bitmap compositeDrawableWithMask(
             Bitmap rgbBitmap, Bitmap alphaBitmap) {
 
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
        rgbBitmap.recycle();
        alphaBitmap.recycle();
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
         BitmapFactory.Options o = new BitmapFactory.Options();
         o.inJustDecodeBounds = true;
         BitmapFactory.decodeStream(context.getContentResolver()
                 .openInputStream(selectedImage), null, o);
 
         int width_tmp = o.outWidth;
         int height_tmp = o.outHeight;
 
         int scale = 1;
         while (width_tmp > widthMinimum || height_tmp > heightMinimum) {
 
             width_tmp /= 2;
             height_tmp /= 2;
             scale *= 2;
         }
         BitmapFactory.Options o2 = new BitmapFactory.Options();
         o2.inSampleSize = scale;
         return BitmapFactory.decodeStream(context.getContentResolver()
                 .openInputStream(selectedImage), null, o2);
     }
 }
