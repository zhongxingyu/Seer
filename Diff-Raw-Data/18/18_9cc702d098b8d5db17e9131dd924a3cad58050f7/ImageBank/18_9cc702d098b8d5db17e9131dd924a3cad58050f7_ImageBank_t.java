 package org.omships.omships;
 
 import java.io.BufferedInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 
 /**
  * Methods for providing/caching images.
  * @author jimtahu
  *
  */
 public class ImageBank {
 	static Context context;
 	
 	static void setContext(Context con){
 		context = con;
 	}
 	
 	/**
 	 * Removes an image from the cache.
 	 * @param url
 	 * This removes an image from the local cache.
 	 * This should not have any negative side effects on existing
 	 *  usages of the image, but forces the image to be redownloaded
 	 *  the next time it is called for.
 	 */
 	public static void invalidate(String url){
		Log.i("IMAGE", "Invalidating "+hashURL(url));
 		context.deleteFile(hashURL(url));
 	}//end invalidate
 	
 	/**
 	 * Processes the image url into something which will not have magic char.
 	 * @param url a url 
 	 * @return a string corosponding to the url
 	 */
 	static String hashURL(String url){
 		return url.replace('/', 's').replace(':','c');
 	}
 	
 	/**
 	 * Saves an image to disk from the url.
 	 * @param url
 	 */
 	static void saveImage(String url){
 		int count=1,size=0;
 		byte buffer[] = new byte[0x100];
 		try {
 			FileOutputStream fos =  context.openFileOutput(hashURL(url), Context.MODE_PRIVATE);
 			InputStream input = new BufferedInputStream(new URL(url).openStream());
 			Log.i("IMAGE","Saveing "+hashURL(url));
 			while(true){
 				count=input.read(buffer);
 				if(count<0)break;
 				fos.write(buffer,0,count);
 				size+=count;
 			}
 			input.close();
 			fos.close();
 			Log.i("IMAGE","Saved "+hashURL(url)+" "+size+" bytes.");
 		} catch (FileNotFoundException ex) {
 			Log.e("IMAGE", "Error saving image to cache", ex);
 		} catch (MalformedURLException ex) {
 			Log.e("IMAGE","Invalid url: "+url, ex);
 		} catch (IOException ex) {
 			Log.e("IMAGE","Error reading image", ex);
 		} catch (IndexOutOfBoundsException ex) {
 			Log.e("IMAGE", "(count, size) = "+count+","+size, ex);
 		}
 	}//end saveImage
 	
 	/**
 	 * Fetches the image given as a url string.
 	 * @param url url of the image.
 	 * @return An image as a Bitmap.
 	 */
 	static Bitmap getImage(String url){
 		try {
 			Bitmap image = BitmapFactory.decodeStream(
 					new BufferedInputStream(
 							context.openFileInput(hashURL(url))
 					));
			Log.i("IMAGE","Cache hit on "+hashURL(url));
 			return image;
 		} catch (FileNotFoundException ex){
 			saveImage(url);
 			return getImage(url);
 		}	
 	}//end getImage
 }//end ImageBank
