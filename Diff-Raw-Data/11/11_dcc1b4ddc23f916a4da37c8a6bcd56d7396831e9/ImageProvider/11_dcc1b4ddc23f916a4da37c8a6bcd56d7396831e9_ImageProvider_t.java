 package org.ivan.simple;
 
 import android.content.Context;
 import android.content.res.AssetManager;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.support.v4.util.LruCache;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 
 public class ImageProvider {
     public static final int CACHE_SIZE = 2048;
     private static String resSet = "large/";
     private static final String base = "sprites/";
 
     private AssetManager asssetsMananger;
     private HashMap<String, Bitmap> images = new HashMap<String, Bitmap>();
 	private int gridStep = 128;
 	private double baseStep = 230d;
 	private int cacheSize = 0;
     private LruCache<String, Bitmap> lruCache = new LruCache<String, Bitmap>(CACHE_SIZE) {
         @Override
         protected int sizeOf(String key, Bitmap value) {
             return value.getWidth() * value.getHeight() * 4 / 1024;
         }
     };
 
 	public ImageProvider(Context context, int displayWidth, int displayHeight) {
         init(context);
         setScaleParameters(displayWidth, displayHeight);
 	}
 	
 	public void init(Context context) {
 //		int cacheSize = (int) (Runtime.getRuntime().maxMemory() / 8192);
 //		images = new LruCache<String, Bitmap>(cacheSize);
 		asssetsMananger = context.getAssets();
 	}
 	
 	private int setScaleParameters(int width, int height) {
 		if(height < 432) {
 			gridStep = 48;
 			baseStep = 144;
 			resSet = "small/";
 		} else if(height < 528) {
 			gridStep = 72;
 			baseStep = 144;
 			resSet = "small/";
 		} else if(height < 672) {
             gridStep = 88;
             baseStep = 144;
             resSet = "small/";
        } else if(height < 1080) {
            gridStep = 112;
            baseStep = 230;
            resSet = "large/";
		} else {
             gridStep = 144;
             baseStep = 144;
             resSet = "small/";
 		}
 		return gridStep;
 	}
 	
 	public Bitmap getBitmapNoCache(String path, int rows, int cols) {
 		System.out.println("Cache size: " + cacheSize);
 		try {
 			Bitmap ret;
 			BitmapFactory.Options opts = new BitmapFactory.Options();
             if(rows == 1 && cols == 1) {
 //                opts.inPreferredConfig = Bitmap.Config.RGB_565;
                 opts.inSampleSize = (int) (baseStep / gridStep);
 //					System.out.println("Sample:" + opts.inSampleSize);
 				ret = loadBitmap(path, opts);
 			} else {
 				double scale = gridStep / baseStep;
                 Bitmap original;
                 try {
 				    original = loadBitmap(path, opts);
                 } catch (OutOfMemoryError error) {
                     reportOutOfMemory(path);
                     throw error;
                 }
 				int width = (int) Math.ceil(opts.outWidth * scale);
 				width -= width % cols;
 				int height = (int) Math.ceil(opts.outHeight * scale);
 				height -= height % rows;
                 if(original.getWidth() == width && original.getHeight() == height) {
                     ret = original;
                 } else {
                     // TODO learn about filter flag
                     ret = Bitmap.createScaledBitmap(original, width, height, true);
                     original.recycle();
                 }
 			}
 			return ret;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 	}
 
     private void reportOutOfMemory(String path) {
         System.err.println("Error loading path " + path);
         System.err.println("Max heap: " + Runtime.getRuntime().maxMemory());
         System.err.println("Total heap: " + Runtime.getRuntime().totalMemory());
         System.err.println("Free heap: " + Runtime.getRuntime().freeMemory());
     }
 
     public Bitmap getBitmapLruCache(String path, int rows, int cols) {
         Bitmap bmp = lruCache.get(path);
         if(bmp == null) {
             bmp = getBitmapNoCache(path, rows, cols);
             lruCache.put(path, bmp);
         }
         return bmp;
     }
 
     public Bitmap getBitmapStrictCache(String path, int rows, int cols) {
 		Bitmap  bmp = images.get(path);
 		if(bmp == null) {
 			bmp = getBitmapNoCache(path, rows, cols);
 			cacheSize += bmp.getWidth() * bmp.getHeight() / 256;// * 4 / 1024
 			images.put(path, bmp);
 		}
 		return bmp;
 	}
 	
 	public Bitmap getBitmapStrictCache(String path) {
 		return getBitmapStrictCache(path, 1, 1);
 	}
 	
 	public Bitmap getBitmapNoCache(String path) {
 		return getBitmapNoCache(path, 1, 1);
 	}
 	
 	public BitmapFactory.Options loadBitmapSize(String path) {
 		BitmapFactory.Options opts = new BitmapFactory.Options();
 		opts.inJustDecodeBounds = true;
 		try {
 			loadBitmap(path, opts);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return opts;
 	}
 
     private Bitmap loadBitmap(String path, BitmapFactory.Options opts) throws IOException {
         InputStream input = null;
         try {
             input = asssetsMananger.open(base + resSet + path);
             return BitmapFactory.decodeStream(input, null, opts);
         } finally {
             if(input != null) input.close();
         }
     }
 
     public void removeFromCatch(String path) {
 		Bitmap bmp = images.get(path);
 		if(bmp == null) return;
 		cacheSize -= bmp.getWidth() * bmp.getHeight() / 256;// * 4 / 1024
 		bmp.recycle();
 		images.remove(path);
 //		System.out.println("Bitmap removed");
 	}
 
 	public int getGridStep() {
 		return gridStep;
 	}
 
     public String[] list(String path) throws IOException {
         return asssetsMananger.list(base + resSet + path);
     }
 
     public Bitmap getBackground(String path, int width, int height) {
         BitmapFactory.Options opts = new BitmapFactory.Options();
         opts.inPreferredConfig = Bitmap.Config.RGB_565;
         return getScaledBitmapNoCache(path, width, height, opts);
     }
 
     public Bitmap getScaledBitmapNoCache(String path, int width, int height, BitmapFactory.Options opts) {
         try {
             Bitmap orig = loadBitmap(path, opts);
             if(orig.getWidth() == width && orig.getHeight() == height) {
                 return orig;
             } else {
                 Bitmap scaled = Bitmap.createScaledBitmap(orig, width, height, true);
                 orig.recycle();
                 return scaled;
             }
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }
     }
 }
