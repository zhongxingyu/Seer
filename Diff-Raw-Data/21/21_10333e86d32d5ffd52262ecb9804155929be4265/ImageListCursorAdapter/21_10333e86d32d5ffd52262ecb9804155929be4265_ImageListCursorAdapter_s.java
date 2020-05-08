 package de.hsbremen.android.dribl.adapter;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
import java.net.MalformedURLException;
 import java.net.URL;
 
 import android.app.ActivityManager;
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
 import android.os.AsyncTask;
 import android.support.v4.util.LruCache;
 import android.support.v4.widget.CursorAdapter;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.webimageloader.ImageLoader;
 import com.webimageloader.ext.ImageHelper;
 
 import de.hsbremen.android.dribl.R;
 import de.hsbremen.android.dribl.provider.DribbbleContract;
 
 public class ImageListCursorAdapter extends CursorAdapter {
 	
 	private LayoutInflater inflater;
 	LruCache<String, Bitmap> memoryCache;
	private ConnectivityManager connMgr;
 	
 	ImageLoader imageLoader;
 	
 	class ViewHolder {
 	    ImageView image;
 	    TextView title;
 //	    ProgressBar progressIndicator;
 	}
 	
 	public ImageListCursorAdapter(Context context, Cursor cursor) {
 		super(context, cursor, 0);
 		inflater = LayoutInflater.from(context);
 		
 		// Get memory class of this device, exceeding this amount will throw an
 		// OutOfMemory exception.
 		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
 		int memClass = am.getMemoryClass();
 
 		// Use part of the available memory for memory cache.
 		final int memoryCacheSize = 1024 * 1024 * memClass / 8;
 
 		File cacheDir = new File(context.getExternalCacheDir(), "images");
 		imageLoader = new ImageLoader.Builder(context)
 		        .enableDiskCache(cacheDir, 10 * 1024 * 1024)
 		        .enableMemoryCache(memoryCacheSize).build();
 		
 		
 //		connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		
 //	    // Get max available VM memory, exceeding this amount will throw an
 //	    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
 //	    // int in its constructor.
 //	    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
 //
 //	    // Use 1/8th of the available memory for this memory cache.
 //	    final int cacheSize = maxMemory / 8;
 //
 //	    memoryCache = new LruCache<String, Bitmap>(cacheSize) {
 //	        @Override
 //	        protected int sizeOf(String key, Bitmap bitmap) {
 //	            // The cache size will be measured in kilobytes rather than
 //	            // number of items.
 //	            return bitmap.getByteCount() / 1024;
 //	        }
 //	    };
 	}
 
 	@Override
 	public void bindView(View view, Context context, Cursor cursor) {
 		// Use a viewholder so we don't have to do findViewById lookups everytime a view gets recycled
 		ViewHolder listItem = (ViewHolder) view.getTag();
 		
 		// Get image URL
 		String imageUrlString = cursor.getString(cursor.getColumnIndex(DribbbleContract.Image.IMAGE_URL));
 //		URL imageUrl = null;
 //		try {
 //			imageUrl = new URL(imageUrlString);
 //		} catch (MalformedURLException e) {
 //			Log.e("Dribl", e.getStackTrace().toString());
 //			listItem.image.setImageBitmap(null);
 //			return;
 //		}
 //		Log.d("Dribl", "Scrolling into view");
 		
 		// Clear old image, because this view could be a recycled one
 //		listItem.image.setImageBitmap(null);
 		
 		// This will show a nice fade in when the image has loaded
 		new ImageHelper(context, imageLoader)
 			.setLoadingResource(R.drawable.placeholder)
 			.setFadeIn(true)
 			.load(listItem.image, imageUrlString);
 		
 //		final Bitmap cachedBitmap = getBitmapFromMemCache(imageUrl.toString());
 //		if (cachedBitmap != null) {
 ////			listItem.progressIndicator.setVisibility(View.GONE);
 ////    		listItem.image.setVisibility(View.VISIBLE);
 //    		listItem.image.setImageBitmap(cachedBitmap);
 //		} else {
 //			// Check network state
 //		    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 //		    if (networkInfo != null && networkInfo.isConnected()) {		        
 //				// Activate progress indicator
 ////				listItem.image.setVisibility(View.GONE);
 ////				listItem.progressIndicator.setVisibility(View.VISIBLE);
 //				
 //				
 //				
 //				// Set the image URL as a tag so we can check if the view got recycled by the time the image is done downloading
 //				listItem.image.setTag(imageUrl);
 //				
 //				// Load new image
 //		    	new LoadImageTask().execute(listItem);
 //		    } else {
 //		        // TODO: Display network connectivity error
 //		    }			
 //		}
 		
 
 
 		listItem.title.setText(imageUrlString);
 	}
  
 	@Override
 	public View newView(Context context, Cursor cursor, ViewGroup parent) {
 		// Inflate the view, but don't attach it to the parent yet
 		View view = inflater.inflate(R.layout.listitem_image, parent, false);
 		
 		// Make a ViewHolder
 		ViewHolder holder = new ViewHolder();
 		holder.image = (ImageView) view.findViewById(R.id.image);
         holder.title = (TextView) view.findViewById(R.id.title);
 //        holder.progressIndicator = (ProgressBar) view.findViewById(R.id.progressBar);
         // Store the ViewHolder as a tag
         view.setTag(holder);
 		
 		return view;
 	}
 	
     public Bitmap getBitmapFromMemCache(String key) {
         return memoryCache.get(key);
     }
 	
 	 /**
 	  * Given a URL, establishes an HttpUrlConnection and retrieves
 	  * the web page content as a InputStream, which it returns as
 	  * a string.
 	  * @param imageURL
 	  * @return Bitmap on success, null on failure
 	  * @throws IOException
 	  */
 	 static private Bitmap downloadImage(URL imageUrl) {
 		// Open a connection to the URL
 		HttpURLConnection urlConnection;
 		try {
 			urlConnection = (HttpURLConnection) imageUrl.openConnection();
 		} catch (IOException e) {
 			return null;
 		}
 		
 		// Perform the request
 		try {
 			urlConnection.setReadTimeout(10000); // 10 seconds
 			urlConnection.setConnectTimeout(15000); // 15 seconds
 			urlConnection.setRequestMethod("GET");
 			urlConnection.setDoInput(true);
 			// Execute the request
 			urlConnection.connect();
 			 
 			// Read the response code
 			if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
 				InputStream in = new BufferedInputStream(urlConnection.getInputStream());
 				// Convert to bitmap and return it
 				return BitmapFactory.decodeStream(in);
 			} else {
 				return null;
 			}
 		} catch (IOException e) {
 			return null;
 		} finally {
 			// Make sure that the Connection is closed after we finish using it.
 			// It may be held open longer due to connection pooling by the underlying system, though.
 			urlConnection.disconnect();
 		}
 	 }
 	
 	/**
 	 * A Task that downloads and caches images from the web asynchronously
 	 * @author jannes
 	 */
 	class LoadImageTask extends AsyncTask<Object, Void, Bitmap> {
 
         private ViewHolder listItem;
         private URL imageUrl;
         
         @Override
         protected Bitmap doInBackground(Object... params) {
             listItem = (ViewHolder) params[0];
             // Save the image url for later
 			imageUrl = (URL) listItem.image.getTag();
 			
 			// Download the image
 			Log.d("Dribl", "Downloading an image");
 			return downloadImage(imageUrl);
         }
         
         public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
         	// We are working with multiple threads here, so we need to synchronize
         	// when using multiple operations in a row 
         	synchronized (memoryCache) {
 	            if (getBitmapFromMemCache(key) == null) {
 	            	Log.d("Dribl", "Adding an image to the memory cache");
 	                memoryCache.put(key, bitmap);
 	            }
         	}
         }
         
         @Override
         protected void onPostExecute(Bitmap result) {
         	if (result != null) {
             	// Submit the bitmap to the memory cache
             	addBitmapToMemoryCache(imageUrl.toString(),	result);
             	
             	// Only update the view if it didn't get recycled in the meantime
             	if (imageUrl.equals((URL) listItem.image.getTag())) {
 //            		listItem.progressIndicator.setVisibility(View.GONE);
 //            		listItem.image.setVisibility(View.VISIBLE);
             		listItem.image.setImageBitmap(result);
             	}
         	}
         	
 //        	targetImageView.setImageAlpha(alpha)
 //            if(result != null && imv != null){
 //                imv.setVisibility(View.VISIBLE);
 //                imv.setImageBitmap(result);
 //            }else{
 //                imv.setVisibility(View.GONE);
 //            }
         }
     }
 
 }
