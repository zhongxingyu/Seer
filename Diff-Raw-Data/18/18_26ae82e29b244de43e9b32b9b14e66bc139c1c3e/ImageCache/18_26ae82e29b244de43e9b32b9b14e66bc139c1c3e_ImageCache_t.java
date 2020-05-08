 package tst.drd;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map.Entry;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Environment;
 import android.util.Log;
 
 public class ImageCache {
 
 	private static final int MAX_IMAGE_SIZE = 1000000;
 	public static final int IN_MEM_CACHE_SIZE = 3;
 	private File reddimgDir;
 	private LinkedHashMap<String, Bitmap> inMemCache;
 
 	private ImageResizer imgResizer;
 
 	public ImageCache(ImageResizer imgResizer) {
 		this.imgResizer = imgResizer;
 		
 		File sdCard = Environment.getExternalStorageDirectory();
 		reddimgDir = new File(sdCard.getAbsolutePath() + "/Reddimg");
 		if(reddimgDir.exists() == false) {
 			reddimgDir.mkdir();
 		}
 		inMemCache = new LinkedHashMap<String, Bitmap>();
 		
 	}
 
 	public Bitmap getFromMem(String url) {
 		synchronized (inMemCache) {
 			if (inMemCache.containsKey(url)) {
 				Bitmap bitmap = inMemCache.get(url);
 				// bump the image in the cache
 //				inMemCache.remove(url);
 //				inMemCache.put(url, bitmap);
 				return bitmap;
 			} else {
 				return null;
 			}
 		}
 	}
 	
 	public boolean prepareImage(String url) {
 		Log.d(TstActivity.APP_NAME, "Preparing " + url);
 		Bitmap result = getFromDisk(url);
 		if (result != null) {
 			Log.d(TstActivity.APP_NAME, url + " found on disk cache");
 			return true;
 		}
 		
 		result = getFromWeb(url);
 		if(result != null) {
 			Log.d(TstActivity.APP_NAME, url + " dl from web");
 			return true;
 		}
 		
 		Log.w(TstActivity.APP_NAME, url + " could not be dl from web");
 		return false;
 	}
 
 	// TODO: add max disk cache size
 	private Bitmap getFromDisk(String url) {
 		Bitmap result = null;
 		File img = new File(reddimgDir, urlToFilename(url));
 		if (img.exists()) {
 			try {
 				FileInputStream is = new FileInputStream(img);
 				
 				// TODO: add downsampling for large imgs
 				try {
 					BitmapFactory.Options options = new BitmapFactory.Options();
 					options.inSampleSize = 1;
 					result = BitmapFactory.decodeStream(is, null, options);
 				} catch(OutOfMemoryError err) {
					Log.w(TstActivity.APP_NAME, "ERROR WHILE DECODING " + url + " " + img.length() + " : " + err.toString());
 				}
 				
 				is.close();
 			} catch (FileNotFoundException e) {
 				Log.e(TstActivity.APP_NAME, e.toString());
 			} catch (IOException e) {
 				Log.e(TstActivity.APP_NAME, e.toString());				
 			}
 		}
 		if (result != null) {
 			storeInMem(url, result);
 			result.recycle();
 		}
 		return getFromMem(url);
 	}
 
 	private Bitmap getFromWeb(String url) {
 		try {
 			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
 			connection.setConnectTimeout(5000);
 			if (connection.getContentLength() < MAX_IMAGE_SIZE) {
 				File img = new File(reddimgDir, urlToFilename(url));
 				InputStream is = connection.getInputStream();
 				OutputStream out = new FileOutputStream(img);
 				byte buf[] = new byte[1024];
 				int len;
 				while ((len = is.read(buf)) > 0) {
 					out.write(buf, 0, len);
 				}
 				out.close();
 				is.close();
 				return getFromDisk(url);
 			}
 		} catch (MalformedURLException e) {
 			Log.e(TstActivity.APP_NAME, e.toString());
 		} catch (IOException e) {
 			Log.e(TstActivity.APP_NAME, e.toString());
 		}
 		
 		return null;
 	}
 	
 	private void storeInMem(String url, Bitmap bitmap) {
 		synchronized (inMemCache) {
 			if (inMemCache.size() >= IN_MEM_CACHE_SIZE) {
 				Iterator<Entry<String, Bitmap>> iterator = inMemCache.entrySet().iterator();
 				Entry<String, Bitmap> entry = iterator.next();
 				entry.getValue().recycle();
 				iterator.remove();
 				Log.d(TstActivity.APP_NAME, entry.getKey() + " removed from mem cache");
 			}
 			Bitmap resizedImg = imgResizer.resize(bitmap);
 			inMemCache.put(url, resizedImg);
 		}
 	}
 	
 	private static String urlToFilename(String url) {
 		return url.replaceAll("[\\W&&[^\\.]]+", "_");
 	}
 
 }
