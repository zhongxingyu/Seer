 package org.omships.omships;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 
 /**
  * Methods for providing/caching files.
  * @author jimtahu
  *
  */
 public class FileBank {
 	static Context context;
 	
 	/**
 	 * Sets the context we are working in
 	 * (needed for access to cacheDir)
 	 * @param con
 	 */
	public static void setContext(Context con){
 		context = con;
 	}
 		
 	/**
 	 * Removes a item from the cache.
 	 * @param url
 	 * This removes a item from the local cache.
 	 * This should not have any negative side effects on existing
 	 *  usages of the image/feed, but forces the item to be redownloaded
 	 *  the next time it is called for.
 	 */
	public static void invalidate(String url){
 		Log.i("FILEBANK", "Invalidating "+hashURL(url));
 		new File(context.getCacheDir(), hashURL(url)).delete();
 	}//end invalidate
 
 	/**
 	 * Removes files older than a given date.
 	 * @param old
 	 */
	public static void cleanOldFiles(Date old){
 		for(File item:context.getCacheDir().listFiles()){
 			Date last = new Date(item.lastModified());
 			if(last.before(old))
 				item.delete();
 		}
 	}//end cleanOldFiles
 
 	/**
 	 * Removes files older than the default of 4 hours.
 	 */
	public static void cleanOldFiles() {
 		GregorianCalendar yesterday = new GregorianCalendar();
 		yesterday.roll(GregorianCalendar.HOUR_OF_DAY,-4);
 		cleanOldFiles(yesterday.getTime());
 	}
 	
 	/**
 	 * Processes the url into something which will not have magic char.
 	 * @param url a url 
 	 * @return a string corosponding to the url
 	 */
 	static String hashURL(String url){
 		return url.replace('/', 's').replace(':','c');
 	}
 	
 	/**
 	 * Saves a file to disk from the url.
 	 * @param url
 	 */
	static void saveFile(String url){
 		int count=1,size=0;
 		byte buffer[] = new byte[0x100];
 		try {
 			FileOutputStream fos =  new FileOutputStream(new File(context.getCacheDir(), hashURL(url)));
 			InputStream input = new BufferedInputStream(new URL(url).openStream());
 			Log.i("FILEBANK","Saveing "+hashURL(url));
 			while(true){
 				count=input.read(buffer);
 				if(count<0)break;
 				fos.write(buffer,0,count);
 				size+=count;
 			}
 			input.close();
 			fos.close();
 			Log.i("FILEBANK","Saved "+hashURL(url)+" "+size+" bytes.");
 		} catch (FileNotFoundException ex) {
			Log.e("FILEBANK", "Error saving image to cache", ex);
 		} catch (MalformedURLException ex) {
 			Log.e("FILEBANK","Invalid url: "+url, ex);
 		} catch (IOException ex) {
 			Log.e("FILEBANK","Error reading image", ex);
 		} catch (IndexOutOfBoundsException ex) {
 			Log.e("FILEBANK", "(count, size) = "+count+","+size, ex);
 		}
 	}//end saveImage
 	
 	/**
 	 * Opens a stream for the given url,
 	 * saving to disk if needed.
 	 * @param url
 	 * @return
 	 */
 	public static InputStream openStream(String url){
 		try {
 			File f = new File(context.getCacheDir(), hashURL(url));
 			InputStream stream= new BufferedInputStream(new FileInputStream(f));
 			Log.i("FILEBANK","Cache hit on "+hashURL(url));
 			return stream;
 		} catch (FileNotFoundException ex){
 			saveFile(url);
 			return openStream(url);
 		}
 	}
 	
 	/**
 	 * Fetches the image given as a url string.
 	 * @param url url of the image.
 	 * @return An image as a Bitmap.
 	 */
 	public static Bitmap getImage(String url){
 		Bitmap image = BitmapFactory.decodeStream(
 				openStream(url));
 		Log.i("IMAGE","Cache hit on "+hashURL(url));
 		return image;
 	}//end getImage
 
 }//end ImageBank
