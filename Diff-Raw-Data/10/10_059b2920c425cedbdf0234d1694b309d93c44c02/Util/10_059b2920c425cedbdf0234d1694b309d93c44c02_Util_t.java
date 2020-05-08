 /**
  * 
  */
 package com.steelthorn.android.watch.smartsquare;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.URL;
import java.security.acl.LastOwnerException;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.ColorMatrix;
 import android.graphics.ColorMatrixColorFilter;
 import android.graphics.Paint;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 
 /**
  * @author Jeff Mixon
  * 
  */
 class Util
 {
 	private static final String TAG = "Util";
 
 	public static int ConvertMetersToFeet(double meters)
 	{
 		double feet = meters * 3.28083989501312;
 		return (int) feet;
 	}
 
 	public static Bitmap downloadImageWithCaching(Context ctx, String uri) throws IOException
 	{
 		String filename = uri.substring(uri.lastIndexOf('/') + 1);
 
		if (filename.startsWith("default"))
		{
			String cutoff = uri.substring(0,uri.lastIndexOf('/')-1);
			String cat = cutoff.substring(cutoff.lastIndexOf('/')+1);
			
			filename = cat + filename;
		}
		
 		Log.d(TAG, "Checking if file is cached: " + filename);
 
 		if (!doesCacheFileExist(ctx, filename))
 		//if (true)
 		{
 			Log.d(TAG, "No cache exists. Downloading.");
 			URL url = new URL(uri);
 
 			HttpURLConnection con = (HttpURLConnection) url.openConnection();
 
 			Bitmap b = BitmapFactory.decodeStream(con.getInputStream());
 
 			b = postProcessBitmap(b);
 
 			cacheBitmap(ctx, b, filename);
 
 			return b;
 		}
 		else
 		{
 			Log.d(TAG, "Found cached file.");
 			return getCachedBitmap(ctx, filename);
 		}
 	}
 
 	private static Bitmap postProcessBitmap(Bitmap stock)
 	{
 		stock = Util.centerCrop(stock, 200, 200);
 		stock = Util.invertBitmap(stock);
 		stock = Util.makeBrightnessBitmap(stock, -80);
 		return stock;
 	}
 
 	private static Bitmap getCachedBitmap(Context ctx, String filename) throws FileNotFoundException
 	{
 		File cacheDir = ctx.getCacheDir();
 
 		File cacheFile = new File(cacheDir, filename);
 
 		FileInputStream fis = new FileInputStream(cacheFile);
 
 		return BitmapFactory.decodeStream(fis);
 	}
 
 	private static Boolean doesCacheFileExist(Context ctx, String filename)
 	{
 		File cacheDir = ctx.getCacheDir();
 
 		File cacheFile = new File(cacheDir, filename);
 
 		return cacheFile.exists();
 	}
 
 	private static void cacheBitmap(Context ctx, Bitmap b, String filename) throws FileNotFoundException
 	{
 		File cacheDir = ctx.getCacheDir();
 
 		File cacheFile = new File(cacheDir, filename);
 
 		FileOutputStream fos = new FileOutputStream(cacheFile);
 
 		b.compress(CompressFormat.PNG, 90, fos);
 	}
 
 	public static Bitmap invertBitmap(Bitmap b)
 	{
 		Bitmap d = Bitmap.createBitmap(b.getWidth(), b.getHeight(), Bitmap.Config.ARGB_8888);
 
 		Canvas c = new Canvas(d);
 		Paint p = new Paint();
 		float mx[] = { -1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
 
 		0.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f };
 		ColorMatrix cm = new ColorMatrix(mx);
 		p.setColorFilter(new ColorMatrixColorFilter(cm));
 		c.drawBitmap(b, 0, 0, p);
 
 		b.recycle();
 
 		return d;
 	}
 
 	public static Bitmap makeBrightnessBitmap(Bitmap original, int brightness)
 	{
 		Bitmap newBitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());
 		int[] argb = new int[original.getWidth() * original.getHeight()];
 		original.getPixels(argb, 0, original.getWidth(), 0, 0, original.getWidth(), original.getHeight());
 		for (int i = argb.length - 1; i >= 0; --i)
 		{
 			int alpha = argb[i] >> 24;
 			int red = (argb[i] >> 16) & 0xFF;
 			int green = (argb[i] >> 8) & 0xFF;
 			int blue = argb[i] & 0xFF;
 			int red2 = red + brightness;
 			if (red2 > 0xFF)
 				red2 = 0xFF;
 			if (red2 < 0)
 				red2 = 0;
 			int green2 = green + brightness;
 			if (green2 > 0xFF)
 				green2 = 0xFF;
 			if (green2 < 0)
 				green2 = 0;
 			int blue2 = blue + brightness;
 			if (blue2 > 0xFF)
 				blue2 = 0xFF;
 			if (blue2 < 0)
 				blue2 = 0;
 			int composite = (alpha << 24) | (red2 << 16) | (green2 << 8) | blue2;
 			argb[i] = composite;
 		}
 		newBitmap.setPixels(argb, 0, original.getWidth(), 0, 0, original.getWidth(), original.getHeight());
 		original.recycle();
 		return newBitmap;
 	}
 
 	public static Bitmap centerCrop(Bitmap b, int newWidth, int newHeight)
 	{
 		//Bitmap q = Bitmap.createBitmap(b, 28, 28, 200, 200);
 
 		Bitmap cropped = Bitmap.createBitmap(b, (b.getWidth() - newWidth) / 2, (b.getHeight() - newHeight) / 2, newWidth, newHeight);
 		b.recycle();
 		return cropped;
 	}
 }
