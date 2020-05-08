 package com.finproj;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.PixelFormat;
 import android.graphics.drawable.Drawable;
 
 public class ImageUtil {
 	protected Bitmap frameBitmap;
 	
 	public Bitmap drawableToBitmap( final Drawable drawable ) {
 		Bitmap.Config c = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
 		Bitmap bitmap = Bitmap.createBitmap( drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),  c);
 		Canvas canvas = new Canvas(bitmap);
         drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
         drawable.draw(canvas);
         return bitmap;
     }
 	
     public Bitmap mergeBitmap( final Bitmap currentBitmap ) {   	
         Bitmap mBmOverlay = Bitmap.createBitmap( frameBitmap.getWidth(), frameBitmap.getHeight(), frameBitmap.getConfig() );
         Canvas canvas = new Canvas( mBmOverlay );
        canvas.drawBitmap( currentBitmap, 18, 18, null );
         canvas.drawBitmap( frameBitmap, new Matrix(), null );
         return mBmOverlay;
     }
 }
