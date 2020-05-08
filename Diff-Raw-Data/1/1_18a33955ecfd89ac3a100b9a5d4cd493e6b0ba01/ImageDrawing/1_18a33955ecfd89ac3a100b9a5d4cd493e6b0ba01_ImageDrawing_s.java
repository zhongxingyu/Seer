 /* ========================================================================= *
  * Boarder                                                                   *
  * http://boarder.mikuz.org/                                                 *
  * ========================================================================= *
  * Copyright (C) 2013 Boarder                                                *
  *                                                                           *
  * Licensed under the Apache License, Version 2.0 (the "License");           *
  * you may not use this file except in compliance with the License.          *
  * You may obtain a copy of the License at                                   *
  *                                                                           *
  *     http://www.apache.org/licenses/LICENSE-2.0                            *
  *                                                                           *
  * Unless required by applicable law or agreed to in writing, software       *
  * distributed under the License is distributed on an "AS IS" BASIS,         *
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
  * See the License for the specific language governing permissions and       *
  * limitations under the License.                                            *
  * ========================================================================= */
 
 package fi.mikuz.boarder.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 
 import com.bugsense.trace.BugSenseHandler;
 
 import fi.mikuz.boarder.component.soundboard.GraphicalSound;
 
 /**
  * 
  * @author Jan Mikael Lindlf
  */
 public class ImageDrawing {
 	private static final String TAG = ImageDrawing.class.getSimpleName();
 	
 	static final int IMAGE_MAX_SIZE = 4000;
 	
 	static private Map<Context, ImageCache> imageCaches = new HashMap<Context, ImageCache>();
 	static private Object registrationLock = new Object();
 	
 	public static void registerCache(Context context) {
 		synchronized (registrationLock) {
 			try {
 				imageCaches.put(context, new ImageCache());
 				Log.v(TAG, "Registered new image cache " + context);
 			} catch (IOException e) {
 				Log.e(TAG, "Unable to register image cache " + context, e);
 			}
 		}
 	}
 	
 	public static void unregisterCache(Context context) {
 		synchronized (registrationLock) {
 			Object cache = imageCaches.remove(context);
 			if (cache != null) {
 				Log.v(TAG, "Unregistered image cache " + context);
 			} else {
 				Log.v(TAG, "Could not unregister cache " + context);
 			}
 		}
 	}
 	
 	public static Bitmap decodeSoundImage(Context context, GraphicalSound sound) {
 		return decodeFile(context, sound.getImagePath(), sound.getImageWidth(), sound.getImageHeight());
 	}
 	
 	public static Bitmap decodeSoundActiveImage(Context context, GraphicalSound sound) {
 		return decodeFile(context, sound.getActiveImagePath(), sound.getActiveImageWidth(), sound.getActiveImageHeight());
 	}
 	
 	public static Bitmap decodeFile(Context context, File f) {
 		return decodeFile(context, f, decodeFileWidth(f), decodeFileHeight(f));
 	}
 	
 	public static Bitmap decodeFile(Context context, File f, float width, float height) {
 		return decodeFile(context, f, (int) Math.ceil(width), (int) Math.ceil(height));
 	}
 
 	/**
 	 * Custom bitmap decoder to avoid and improve memory errors with enormous images and large amount of huge images.
 	 * 
 	 * @param image file
 	 * @return image bitmap
 	 */
 	public static Bitmap decodeFile(Context context, File f, int width, int height) {
 	    Bitmap b = null;
 	    
 	    ImageCache imageCache = null;
 	    if (context != null ) {
 	    	imageCache = imageCaches.get(context);
 	    }
 	    
 	    if (imageCache == null) {
 			Log.e(TAG, "No cache registered for " + context);
 	    }
 	    
 	    String cacheKey = null;
 	    if (imageCache != null) {
 	    	int fileChecksum = f.getAbsolutePath().hashCode();
 	    	cacheKey = fileChecksum + "-" + width + "-" + height;
 	    	b = imageCache.get(cacheKey);
 	    	if (b != null) {
 	    		Log.v(TAG, "Found image " + f + " from cache as " + cacheKey);
 	    		if (b.isRecycled()) {
 	    			Log.v(TAG, "Image " + f + " is recycled. Decoding.");
 	    		} else {
 	    			return b;
 	    		}
 	    	}
 	    }
 	    
 	    // To avoid unexpected stuff bitmaps won't be decoded if memory is running very low.
 	    if (underFivePercentOfMemoryLeft()) {
 	    	String errorMessage = "Not enough memory, won't decode image " + f.getAbsolutePath();
 	    	Log.e(TAG, errorMessage);
 	    	if (context != null) ContextUtils.toast(context, "Not enough memory");
 	    	return null;
 	    }
 	    
 	    try {
 	    	try {
 	    		BitmapFactory.Options options = new BitmapFactory.Options();
 	    		options.inJustDecodeBounds = true;
 	    		BitmapFactory.decodeFile(f.getAbsolutePath(), options);
 	    		
 	    		options.inSampleSize = calculateInSampleSize(options, width, height);
 	    		options.inJustDecodeBounds = false;
 	    		b = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
 		    } catch (OutOfMemoryError ome) {
 		    	Log.w(TAG, "Image " + f.getAbsolutePath() + " is enormous! It has to be decoded to smaller resolution.");
 		    	try {
 			        //Decode image size
 			        BitmapFactory.Options o = new BitmapFactory.Options();
 			        o.inJustDecodeBounds = true;
 
 			        FileInputStream fis = new FileInputStream(f);
 			        BitmapFactory.decodeStream(fis, null, o);
 			        fis.close();
 
 			        int scale = 1;
 			        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
 			            scale = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
 			        }
 
 			        //Decode with inSampleSize
 			        BitmapFactory.Options o2 = new BitmapFactory.Options();
 			        o2.inSampleSize = scale;
 			        fis = new FileInputStream(f);
 			        b = BitmapFactory.decodeStream(fis, null, o2);
 			        fis.close();
 			    } catch (IOException e) {
 			    	Log.e(TAG, "Unable to read image file");
 			    }
 		    }
 	    } catch (OutOfMemoryError ome2) {
 	    	String errorMessage = "Unable to decode image, out of memory";
 	    	Log.e(TAG, errorMessage, ome2);
 	    	if (context != null) ContextUtils.toast(context, "Out of memory");
 	    }
 	    
 	    if (b == null) {
 	    	Exception e = new IOException("Unable to decode image " + f.getAbsolutePath());
 	    	Log.e(TAG, e.getMessage(), e);
 	    	if (context != null) ContextUtils.toast(context, e.getMessage());
	    	BugSenseHandler.sendException(e);
 	    }
 	    
 	    if (imageCache != null && b != null && cacheKey != null) {
 	    	imageCache.add(cacheKey, b);
 	    	Log.v(TAG, "Cached image " + f + " as " + cacheKey);
 	    }
 	    
 	    return b;
 	    
 	}
 
 	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
 		// Raw height and width of image
 		final int height = options.outHeight;
 		final int width = options.outWidth;
 		int inSampleSize = 1;
 
 		if (height > reqHeight || width > reqWidth) {
 
 			// Calculate ratios of height and width to requested height and width
 			final int heightRatio = Math.round((float) height / (float) reqHeight);
 			final int widthRatio = Math.round((float) width / (float) reqWidth);
 
 			// Choose the smallest ratio as inSampleSize value, this will guarantee
 			// a final image with both dimensions larger than or equal to the
 			// requested height and width.
 			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
 		}
 
 		return inSampleSize;
 	}
 	
 	private static boolean underFivePercentOfMemoryLeft() {
 	    Runtime runtime = Runtime.getRuntime();
 	    long usedMemory = (runtime.totalMemory() - runtime.freeMemory());
 	    long maxMemory = runtime.maxMemory();
 	    
 	    return (usedMemory > (maxMemory - maxMemory*0.05));
 	}
 	
 	public static int decodeFileWidth(File f) {
 		BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inJustDecodeBounds = true;
 		BitmapFactory.decodeFile(f.getAbsolutePath(), options);
 		return options.outWidth;
 	}
 	
 	public static int decodeFileHeight(File f) {
 		BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inJustDecodeBounds = true;
 		BitmapFactory.decodeFile(f.getAbsolutePath(), options);
 		return options.outHeight;
 	}
 
 }
