 package com.petrifiednightmares.singularityChess.utilities;
 
 import java.lang.ref.WeakReference;
 
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.util.SparseArray;
 
 public class SingularBitmapFactory
 {
 	private static SparseArray<WeakReference<Bitmap>> cache = new SparseArray<WeakReference<Bitmap>>();
 
 	public static Bitmap buildSingularBitmap(Resources r, int id)
 	{
 		WeakReference<Bitmap> b = cache.get(id, null);
 		Bitmap image = null;
 		if (b != null)
 		{
 			image = b.get();
 		}
 		if (image == null)
 		{
 			image = BitmapFactory.decodeResource(r, id);
 			b = new WeakReference<Bitmap>(image);
 			cache.put(id, b);
 			return image;
 		} else
 		{
 			return image;
 		}
 	}
 
 	public static Bitmap buildSingularScaledBitmap(Resources r, int id, int newWidth, int newHeight)
 	{
 		WeakReference<Bitmap> b = cache.get(id, null);
 		Bitmap image = null;
 		if (b != null)
 		{
 			image = b.get();
 		}
 		if (image == null)
 		{
 			image = BitmapFactory.decodeResource(r, id);
 
 			image = scaleImage(image, newWidth, newHeight);
 
 			b = new WeakReference<Bitmap>(image);
 			cache.put(id, b);
 			return image;
 		} else
 		{
 			if (image.getWidth() == newWidth && image.getHeight() == newHeight)
 			{
 				return image;
 			} else
 			{
 				image = scaleImage(image, newWidth, newHeight);
 				b = new WeakReference<Bitmap>(image);
 				cache.put(id, b);

 				return image;
 			}
 		}
 	}
 
 	private static Bitmap scaleImage(Bitmap original, int newWidth, int newHeight)
 	{
 		float scaleWidth = ((float) newWidth) / original.getWidth();
 		float scaleHeight = ((float) newHeight) / original.getHeight();
 		Matrix matrix = new Matrix();
 		matrix.postScale(scaleWidth, scaleHeight);
 
 		return Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(),
 				matrix, false);
 	}
 
 	public static Bitmap buildBitmap(Resources r, int id)
 	{
 		return BitmapFactory.decodeResource(r, id);
 	}
 
 	public static Bitmap buildScaledBitmap(Resources r, int id, int newWidth, int newHeight)
 	{
 		Bitmap image = BitmapFactory.decodeResource(r, id);
 
 		return scaleImage(image, newWidth, newHeight);
 	}
 
 	public static Bitmap buildScaledBitmap(Resources r, String f, int newWidth, int newHeight)
 	{
 		Bitmap image = BitmapFactory.decodeFile(f);
 
		if (image != null)
			return scaleImage(image, newWidth, newHeight);
		else
			return null;
 	}
 
 }
