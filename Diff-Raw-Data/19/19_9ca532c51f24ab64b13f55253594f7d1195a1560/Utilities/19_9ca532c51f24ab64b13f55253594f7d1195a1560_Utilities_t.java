 package edu.selu.android.classygames;
 
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.res.AssetManager;
 import android.content.res.Resources;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.graphics.Shader.TileMode;
 import android.graphics.Bitmap;
 import android.graphics.Typeface;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.Build;
import android.support.v4.util.LruCache;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.facebook.android.Facebook;
 import com.koushikdutta.urlimageviewhelper.DiskLruCache;
 import com.koushikdutta.urlimageviewhelper.DiskLruCache.Snapshot;
 
 
 public class Utilities
 {
 
 	public final static String DISPLAY_MESSAGE_ACTION = "edu.selu.android.classygames.CONTEXT_BROADCAST";
 	public final static String LOG_TAG = "ClassyGames";
 	
 	
 	public final static CompressFormat COMPRESS_FORMAT = CompressFormat.PNG;
     public final static int COMPRESS_QUALITY = 70;
 	public final static int APP_VERSION = 1;
 	public final static int VALUE_COUNT = 1;
 	public final static int IO_BUFFER_SIZE = 8 * 1024;
 	public final static int DISK_CACHE_SIZE = 1024 * 1024 * 10;
 	public final static String DISK_CACHE_SUBDIR = "thumbnails";
 	
 
 	public static SharedPreferences sharedPreferences;
 
 
 	// typeface data below
 	public final static int TYPEFACE_BLUE_HIGHWAY_D = 0;
 	public final static int TYPEFACE_BLUE_HIGHWAY_RG = 1;
 	public final static int TYPEFACE_SNELL_ROUNDHAND_BDSCR = 10;
 	public final static int TYPEFACE_SNELL_ROUNDHAND_BLKSCR = 11;
 	private final static String TYPEFACE_BLUE_HIGHWAY_D_PATH = "fonts/blue_highway_d.ttf";
 	private final static String TYPEFACE_BLUE_HIGHWAY_RD_PATH = "fonts/blue_highway_rg.ttf";
 	private final static String TYPEFACE_SNELL_ROUNDHAND_BDSCR_PATH = "fonts/snell_roundhand_bdscr.otf";
 	private final static String TYPEFACE_SNELL_ROUNDHAND_BLKSCR_PATH = "fonts/snell_roundhand_blkscr.otf";
 	private static Typeface typefaceBlueHighwayD;
 	private static Typeface typefaceBlueHighwayRG;
 	private static Typeface typefaceSnellRoundHandBDSCR;
 	private static Typeface typefaceSnellRoundHandBLKSCR;
 	// end typeface data
 
 
 	// facebook data below
 	private static Facebook facebook;
 
 	public final static String FACEBOOK_EXPIRES = "expires_in";
 	public final static String FACEBOOK_TOKEN = "access_token";
 
 	public final static String FACEBOOK_GRAPH_API_URL = "https://graph.facebook.com/";
 	public final static String FACEBOOK_GRAPH_API_URL_PICTURE = "/picture";
 	public final static String FACEBOOK_GRAPH_API_URL_PICTURE_SSL = "return_ssl_resources=1";
 	public final static String FACEBOOK_GRAPH_API_URL_PICTURE_TYPE = "?type=";
 	public final static String FACEBOOK_GRAPH_API_URL_PICTURE_TYPE_LARGE = FACEBOOK_GRAPH_API_URL_PICTURE + FACEBOOK_GRAPH_API_URL_PICTURE_TYPE + "large";
 	public final static String FACEBOOK_GRAPH_API_URL_PICTURE_TYPE_LARGE_SSL = FACEBOOK_GRAPH_API_URL_PICTURE_TYPE_LARGE + "&" + FACEBOOK_GRAPH_API_URL_PICTURE_SSL;
 	public final static String FACEBOOK_GRAPH_API_URL_PICTURE_TYPE_NORMAL = FACEBOOK_GRAPH_API_URL_PICTURE + FACEBOOK_GRAPH_API_URL_PICTURE_TYPE + "normal";
 	public final static String FACEBOOK_GRAPH_API_URL_PICTURE_TYPE_NORMAL_SSL = FACEBOOK_GRAPH_API_URL_PICTURE_TYPE_NORMAL + "&" + FACEBOOK_GRAPH_API_URL_PICTURE_SSL;
 	public final static String FACEBOOK_GRAPH_API_URL_PICTURE_TYPE_SMALL = FACEBOOK_GRAPH_API_URL_PICTURE + FACEBOOK_GRAPH_API_URL_PICTURE_TYPE + "small";
 	public final static String FACEBOOK_GRAPH_API_URL_PICTURE_TYPE_SMALL_SSL = FACEBOOK_GRAPH_API_URL_PICTURE_TYPE_SMALL + "&" + FACEBOOK_GRAPH_API_URL_PICTURE_SSL;
 	public final static String FACEBOOK_GRAPH_API_URL_PICTURE_TYPE_SQUARE = FACEBOOK_GRAPH_API_URL_PICTURE + FACEBOOK_GRAPH_API_URL_PICTURE_TYPE + "square";
 	public final static String FACEBOOK_GRAPH_API_URL_PICTURE_TYPE_SQUARE_SSL = FACEBOOK_GRAPH_API_URL_PICTURE_TYPE_SQUARE + "&" + FACEBOOK_GRAPH_API_URL_PICTURE_SSL;
 	// end facebook data
 
 
 	/**
 	 * <p>Prints a Toast message to the screen.</p>
 	 * 
 	 * <p><strong>Examples</strong><br />
 	 * Utilities.easyToast(MainActivity.this, "Hello!");<br />
 	 * Utilities.easyToast(getApplicationContext(), "Another message huh?");</p>
 	 * 
 	 * @param context
 	 * Just put the name of your class.this, or you can use getApplicationContext().
 	 * 
 	 * @param message
 	 * The String that you want to be shown as a toast message.
 	 * 
 	 */
 	public static void easyToast(final Context context, final String message)
 	{
 		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
 	}
 
 
 	/**
 	 * <p>Prints a Toast message to the screen and prints that same message to the Log.d
 	 * console.</p>
 	 * 
 	 * <p><strong>Examples</strong><br />
 	 * Utilities.easyToastAndLog(MainActivity.this, "Hello!");<br />
 	 * Utilities.easyToastAndLog(getApplicationContext(), "Another message huh?");</p>
 	 * 
 	 * @param context
 	 * Just put the name of your class.this, or you can use getApplicationContext().
 	 * 
 	 * @param message
 	 * The String that you want to be shown as a toast message. This exact String will also
 	 * be printed to the Log.d console.
 	 */
 	public static void easyToastAndLog(final Context context, final String message)
 	{
 		easyToast(context, message);
 		Log.d(LOG_TAG, message);
 	}
 
 
 	/**
 	 * <p>Prints a Toast message to the screen and prints that same message to each and
 	 * every log console.</p>
 	 * 
 	 * <p><strong>Examples</strong><br />
 	 * Utilities.easyToastAndLogAll(MainActivity.this, "Hello!");<br />
 	 * Utilities.easyToastAndLogAll(getApplicationContext(), "Another message huh?");</p>
 	 * 
 	 * @param context
 	 * Just put the name of your class.this, or you can use getApplicationContext().
 	 * 
 	 * @param message
 	 * The String that you want to be shown as a toast message. This exact String will also
 	 * be printed to the Log.d console.
 	 */
 	public static void easyToastAndLogAll(final Context context, final String message)
 	{
 		easyToast(context, message);
 		Log.d(LOG_TAG, message);
 		Log.e(LOG_TAG, message);
 		Log.i(LOG_TAG, message);
 		Log.v(LOG_TAG, message);
 		Log.wtf(LOG_TAG, message);
 	}
 
 
 	/**
 	 * <p>Prints a Toast message to the screen and prints that same message to the Log.e
 	 * console.</p>
 	 * 
 	 * <p><strong>Examples</strong><br />
 	 * Utilities.easyToastAndLogError(MainActivity.this, "Hello!");<br />
 	 * Utilities.easyToastAndLogError(getApplicationContext(), "Another message huh?");</p>
 	 * 
 	 * @param context
 	 * Just put the name of your class.this, or you can use getApplicationContext().
 	 * 
 	 * @param message
 	 * The String that you want to be shown as a toast message. This exact String will also
 	 * be printed to the Log.e console.
 	 */
 	public static void easyToastAndLogError(final Context context, final String message)
 	{
 		easyToast(context, message);
 		Log.e(LOG_TAG, message);
 	}
 
 
 	/**
 	 * Use this method to retrieve the global Facebook variable that our app uses. This
 	 * method is good to use because it ensures that our Facebook variable isn't null.
 	 */
 	public static Facebook getFacebook()
 	{
 		if (facebook == null)
 		{
 			facebook = new Facebook(SecretConstants.FACEBOOK_APP_ID);
 		}
 
 		return facebook;
 	}
 
 
 	/**
 	 * <p>Returns to you a Typeface that you request.</p>
 	 * 
 	 * <p><strong>Examples</strong><br />
 	 * Utilities.getTypeface(getAssets(), Utilities.TYPEFACE_BLUE_HIGHWAY_D);<br />
 	 * Utilities.getTypeface(getAssets(), Utilities.TYPEFACE_SNELL_ROUND_HAND_BDSCR);</p>
 	 * <p><strong>An invalid example</strong><br />
 	 * Utilities.getTypeface(getAssets(), -1);</p>
 	 * 
 	 * @param assetManager
 	 * The getAssets() method. Just pass that sucker in here.
 	 * 
 	 * @param typeface
 	 * The int ID of the Typeface object that you want. This must be a valid int ID, as if
 	 * it's not, this method will return null.
 	 * 
 	 * @return
 	 * Returns the Typeface object that you requested. If an invalid int ID was passed into
 	 * this method, this method will then return null.
 	 */
 	public static Typeface getTypeface(final AssetManager assetManager, final int typeface)
 	{
 		switch (typeface)
 		{
 			case TYPEFACE_BLUE_HIGHWAY_D:
 				if (typefaceBlueHighwayD == null)
 				{
 					typefaceBlueHighwayD = Typeface.createFromAsset(assetManager, TYPEFACE_BLUE_HIGHWAY_D_PATH);
 				}
 
 				return typefaceBlueHighwayD;
 
 			case TYPEFACE_BLUE_HIGHWAY_RG:
 				if (typefaceBlueHighwayRG == null)
 				{
 					typefaceBlueHighwayRG = Typeface.createFromAsset(assetManager, TYPEFACE_BLUE_HIGHWAY_RD_PATH);
 				}
 
 				return typefaceBlueHighwayRG;
 
 			case TYPEFACE_SNELL_ROUNDHAND_BDSCR:
 				if (typefaceSnellRoundHandBDSCR == null)
 				{
 					typefaceSnellRoundHandBDSCR = Typeface.createFromAsset(assetManager, TYPEFACE_SNELL_ROUNDHAND_BDSCR_PATH);
 				}
 
 				return typefaceSnellRoundHandBDSCR;
 
 			case TYPEFACE_SNELL_ROUNDHAND_BLKSCR:
 				if (typefaceSnellRoundHandBLKSCR == null)
 				{
 					typefaceSnellRoundHandBLKSCR = Typeface.createFromAsset(assetManager, TYPEFACE_SNELL_ROUNDHAND_BLKSCR_PATH);
 				}
 
 				return typefaceSnellRoundHandBLKSCR;
 
 			default:
 				return null;
 		}
 	}
 
 
 	/**
 	* <p>Simple class that handles pulling an image from a URL.</p>
 	* 
 	* <p><strong>Examples</strong><br />
 	* Utilities.LoadImageFRomWebOperations(myURL);</p>
 	* 
 	* @param url
 	* The URL to the image that you want to download.
 	*/
 	public static Drawable loadImageFromWebOperations(final String url)
 	{
 		try
 		{
 			InputStream is = (InputStream) new URL(url).getContent();
 			return Drawable.createFromStream(is, "src");
 		}
 		catch (Exception e)
 		{
 			Log.e(LOG_TAG, "Image Load Failed" + e);
 			return null;
 		}
 	}
 
 
 	/**
 	 * <p>This is a workaround for http://b.android.com/15340 from http://stackoverflow.com/a/5852198/132047.
 	 * This ensures that pre ice cream sandwich devices properly render our customized actionbar.
 	 * This method should always be run immediately after the setContentView() method is run.</p>
 	 * 
 	 * <p><strong>Examples</strong><br />
 	 * Utilities.styleActionBar(getResources(), getSupportActionBar(), false);</p>
 	 * 
 	 * @param resources
 	 * getResources()
 	 * 
 	 * @param actionBar
 	 * getSupportActionBar()
 	 * 
 	 * @param backArrow
 	 * Whether or not you want to have a back arrow drawn next to the app icon in the actionbar.
 	 */
 	public static void styleActionBar(final Resources resources, final ActionBar actionBar, final boolean backArrow)
 	{
 		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
 		{
 			final BitmapDrawable bg = (BitmapDrawable) resources.getDrawable(R.drawable.bg_actionbar);
 			bg.setDither(true);
 			bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
 			actionBar.setBackgroundDrawable(bg);
 		}
 
 		if (backArrow)
 		{
 			actionBar.setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 
 	/**
 	 * <p>This is a workaround for http://b.android.com/15340 from http://stackoverflow.com/a/5852198/132047.
 	 * This ensures that pre ice cream sandwich devices properly render our customized actionbar.
 	 * This method should always be run immediately after the setContentView() method is run.</p>
 	 * 
 	 * <p><strong>Examples</strong><br />
 	 * Utiltiles.styleActionBar(getResources(), getSupportActionBar());</p>
 	 * 
 	 * @param resources
 	 * getResources()
 	 * 
 	 * @param actionBar
 	 * getSupportActionBar()
 	 */
 	public static void styleActionBar(final Resources resources, final ActionBar actionBar)
 	{
 		styleActionBar(resources, actionBar, true);
 	}
 	
 	
 	public static void addBitmapToCache(Long key, Bitmap bitmap, LruCache<Long, Bitmap> memoryCache, DiskLruCache diskCache)
 	{	
 		//Add to memory cache
 		if(getBitmapFromMemCache(key, memoryCache) == null)
 		{
 			memoryCache.put(key, bitmap);
 		}
 		
 		//Add to disk cache as well
 		if(!containsKey(key.toString(), diskCache))
 		{
 			put(key.toString(), bitmap, diskCache);
 		}
 		
 	}
 	
 	
 	public static Bitmap getBitmapFromMemCache(Long key, LruCache<Long, Bitmap> memoryCache)
 	{
 		return (Bitmap) memoryCache.get(key);
 	}
 	
 	
 	public static Bitmap getBitmapFromDiskCache(Long key, DiskLruCache diskCache) 
 	{
 		final InputStream in;
 		final BufferedInputStream bufferIn;
 		Snapshot snapshot = null;
 		Bitmap bitmap = null;
 		
 		try
 		{
 			snapshot = diskCache.get(key.toString());
 			
 			if(snapshot == null)
 			{
 				return null;
 			}
 			
 			in = snapshot.getInputStream(0);
 			
 			if(in != null)
 			{
 				bufferIn = new BufferedInputStream(in, IO_BUFFER_SIZE);
 				bitmap = BitmapFactory.decodeStream(bufferIn);
 			}
 		}
 		catch(IOException e)
 		{
 			Log.e(LOG_TAG, "DiskCache get failed: " + e);
 			return null;
 		}
 		
 		snapshot.close();
 		
 	    return bitmap;
 	}
 
 	
 	public static boolean containsKey(String key, DiskLruCache diskCache) 
 	{
         boolean contained = false;
         Snapshot snapshot = null;
         
         try 
         {
             snapshot = diskCache.get( key );
             
             if(snapshot != null)
             {
             	contained = true;
                	snapshot.close();
             }
         } 
         catch (IOException e) 
         {
             e.printStackTrace();
         }
                   
 
         return contained;
     }
 	
 	
 	public static void put(String key,Bitmap data, DiskLruCache diskCache) {
 
         DiskLruCache.Editor editor = null;
         
         try 
         {
             editor = diskCache.edit( key );
             if ( editor == null ) 
             {
                 return;
             }
 
             if(writeBitmapToFile(data, editor)) 
             {               
                 diskCache.flush();
                 editor.commit();
                 
                 if ( BuildConfig.DEBUG ) 
                 {
                    Log.d( "cache_test_DISK_", "image put on disk cache " + key );
                 }
             } 
             else 
             {
                 editor.abort();
                 
                 if ( BuildConfig.DEBUG ) 
                 {
                     Log.d( "cache_test_DISK_", "ERROR on: ibbbbbmage put on disk cache " + key );
                 }
             }   
         } 
         catch (IOException e) 
         {
             if ( BuildConfig.DEBUG ) 
             {
                 Log.d( "cache_test_DISK_", "ERROR on: imaaaaage put on disk cache " + key );
             }
             
             try 
             {
                 if ( editor != null ) 
                 {
                     editor.abort();
                 }
             } 
             catch (IOException ignored) 
             {		
             	//Do nothing
             }           
         }
 
     }
 	
 	
 	public static boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor) throws IOException 
 	{
 		OutputStream out = null;
 	  
 		try
 		{
 			out = new BufferedOutputStream( editor.newOutputStream( 0 ), IO_BUFFER_SIZE );
 			return bitmap.compress( COMPRESS_FORMAT, COMPRESS_QUALITY, out );
 		}
 		finally
 		{
 			if(out != null)
 			{
 				out.close();
 			}
 	    }
 	}
 	
 	
 }
