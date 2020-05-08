 package net.acuttone.reddimg.core;
 
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
 import java.util.Comparator;
 import java.util.TreeSet;
 
 import net.acuttone.reddimg.prefs.PrefsActivity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Environment;
 import android.os.StatFs;
 import android.util.Log;
 
 public class ImageCache {
 	private static final String NET_ACUTTONE_REDDIMG = "net.acuttone.reddimg";
 	
 	private static final long MIN_FREE_SPACE = 10 * ReddimgApp.MEGABYTE;
 	private static final String FILE_PREFIX = "__RDIMG_";
 	private static final int MAX_IMAGE_SIZE = 3 * ReddimgApp.MEGABYTE;
 	
 	private File reddimgDir;
 	private TreeSet<File> diskCacheFiles;
 	
 	public ImageCache(Context context) {
 		initDiskCache(context);
 	}
 
 	private void initDiskCache(Context context) {
 		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
 			reddimgDir = new File(Environment.getExternalStorageDirectory() + File.separator + NET_ACUTTONE_REDDIMG);
 			if(reddimgDir.exists() == false) {
 				reddimgDir.mkdir();
 			}
 		} else {
 			reddimgDir = context.getCacheDir();
 		} 
 		
 		diskCacheFiles = new TreeSet<File>(new Comparator<File>() {
 
 			@Override
 			public int compare(File f1, File f2) {
 				return (int) (f1.lastModified() - f2.lastModified());
 			}
 		});
 		File[] listFiles = reddimgDir.listFiles();
 		for (File f : listFiles) {
 			if(f.isFile() && f.getName().startsWith(FILE_PREFIX)) {
 				diskCacheFiles.add(f);
 			}
 		}
 		Log.d(ReddimgApp.APP_NAME, "Cache dir : " + reddimgDir.getAbsolutePath());
 	}
 
 	public Bitmap getImage(String url) {
 		Log.d(ReddimgApp.APP_NAME, "Preparing " + url);
 		Bitmap result = getFromDisk(url);
 		if (result != null) {
 			Log.d(ReddimgApp.APP_NAME, url + " found on disk cache");
 			return result;
 		}
 		
 		result = getFromWeb(url);
 		if(result != null) {
 			Log.d(ReddimgApp.APP_NAME, url + " dl from web");
 			return result;
 		}
 		
 		Log.w(ReddimgApp.APP_NAME, url + " could not be dl from web");
 		return null;
 	}
 	
 	private Bitmap getFromDisk(String url) {
 		Bitmap result = null;
 		File img = new File(getImageDiskPath(url));
 		if (img.exists()) {
 			try {
 				// start scaling the image so that it fits 1024x1024
 				FileInputStream is = new FileInputStream(img);
 				BitmapFactory.Options options = new BitmapFactory.Options();
 				options.inJustDecodeBounds = true;
 			    BitmapFactory.decodeStream(is, null, options);
 			    is.close();
 			    int largerDim = Math.max(options.outWidth, options.outHeight);
 			    final float MAX_SIZE = 1024.0f;
 			    int scaleFactor = largerDim > MAX_SIZE ? (int) Math.ceil(largerDim / MAX_SIZE) : 1;
			    Log.d(ReddimgApp.APP_NAME, "" + scaleFactor);
 			    options = new BitmapFactory.Options();
 				// try scaling more if OutOfMemoryError
 			    while(result == null && scaleFactor <= 16) {
 			    	is = new FileInputStream(img);
 			    	try {
 			    		options.inSampleSize = scaleFactor;
 			    		result = BitmapFactory.decodeStream(is, null, options);
 			    	} catch(OutOfMemoryError err) {
 			    		Log.w(ReddimgApp.APP_NAME, "ERROR WHILE DECODING " + url + " WITH SCALE " + scaleFactor + ": " + err.toString());
 			    	}
 			    	is.close();
 			    	scaleFactor++;
 			    }
 			} catch (FileNotFoundException e) {
 				Log.e(ReddimgApp.APP_NAME, e.toString());
 			} catch (IOException e) {
 				Log.e(ReddimgApp.APP_NAME, e.toString());				
 			}
 		}
 		return result;
 	}
 
 	private Bitmap getFromWeb(String url) {
 		HttpURLConnection connection = null;
 		InputStream is = null;
 		OutputStream out = null;
 
 		try {
 			connection = (HttpURLConnection) new URL(url).openConnection();
 			connection.setConnectTimeout(5000);
 			int contentLength = connection.getContentLength();
 			if (contentLength > MAX_IMAGE_SIZE) {
 				Log.w(ReddimgApp.APP_NAME, url + " exceeds max image size");
 				return null;
 			}
 			boolean enoughSpace = false;
 			synchronized (diskCacheFiles) {
 				enoughSpace = checkDiskCacheSize(contentLength);
 			}
 			if (!enoughSpace) {
 				Log.w(ReddimgApp.APP_NAME, "Insufficient space on disk to store " + url);
 			} else {
 				File img = new File(getImageDiskPath(url));
 				is = connection.getInputStream();
 				out = new FileOutputStream(img);
 				byte buf[] = new byte[1024];
 				int len;
 				while ((len = is.read(buf)) > 0) {
 					out.write(buf, 0, len);
 				}
 				out.close();
 				is.close();
 				synchronized (diskCacheFiles) {
 					diskCacheFiles.add(img);
 				}
 				return getFromDisk(url);
 			}
 
 		} catch (MalformedURLException e) {
 			Log.e(ReddimgApp.APP_NAME, e.toString());
 		} catch (IOException e) {
 			Log.e(ReddimgApp.APP_NAME, e.toString());
 		} finally {
 			if (connection != null) {
 				connection.disconnect();
 			}
 			try {
 				if (is != null) {
 					is.close();
 				}
 			} catch (IOException e) {
 				Log.e(ReddimgApp.APP_NAME, e.toString());
 			}
 			try {
 				if (out != null) {
 					out.close();
 				}
 			} catch (IOException e) {
 				Log.e(ReddimgApp.APP_NAME, e.toString());
 			}
 		}
 		return null;
 	}
 	
 	private boolean checkDiskCacheSize(long contentLength) {
 		long totSize = contentLength;
 		long cacheSize = getMaxCacheSize();
 		synchronized (diskCacheFiles) {
 			for (File f : diskCacheFiles) {
 				totSize += f.length();
 			}
 
 			while (totSize > cacheSize && diskCacheFiles.size() > 0) {
 				File oldest = diskCacheFiles.first();
 				diskCacheFiles.remove(oldest);
 				totSize -= oldest.length();
 				if (oldest.exists()
 					&& oldest.getAbsolutePath().contains(NET_ACUTTONE_REDDIMG) 
 					&& oldest.isFile()
 					&& oldest.getName().startsWith(FILE_PREFIX)) {
 						Log.d(ReddimgApp.APP_NAME, "Deleting from disk " + oldest.getName());
 						oldest.delete();
 				}
 			}
 		}
 		StatFs stat = new StatFs(reddimgDir.getPath());
 		double freeSpace = (double) stat.getAvailableBlocks() * (double) stat.getBlockSize();
 
 		return totSize < cacheSize && freeSpace > MIN_FREE_SPACE;
 	}
 	
 	public double getCurrentCacheSize() {
 		double totSize = 0;
 		for(File f : diskCacheFiles) {
 			totSize += f.length();
 		}
 		return totSize / ReddimgApp.MEGABYTE;
 	}
 
 	private long getMaxCacheSize() {
 		SharedPreferences sp = ReddimgApp.instance().getPrefs();
 		int size = Integer.parseInt(sp.getString(PrefsActivity.CACHE_SIZE_KEY, PrefsActivity.DEFAULT_CACHE_SIZE));
 		return size * ReddimgApp.MEGABYTE;
 	}
 	
 	public void clearCache() {
 		// little innocent hack to make checkDiskCacheSize delete all the cached files
 		checkDiskCacheSize(Long.MAX_VALUE / 2);
 	}
 
 	public String getImageDiskPath(String url) {
 		url = FILE_PREFIX + url; 
 		if(url.length() > 256) {
 			url = url.substring(url.length() - 256);
 		}
 		url = url.replaceAll("\\W+", "_");
 		return reddimgDir.getAbsolutePath() + "/" + url;
 	}
 
 }
