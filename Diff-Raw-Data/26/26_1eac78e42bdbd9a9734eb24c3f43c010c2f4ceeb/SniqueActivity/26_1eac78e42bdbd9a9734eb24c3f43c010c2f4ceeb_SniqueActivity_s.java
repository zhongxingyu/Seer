 package com.nomzit.snique;
 
 import static com.nomzit.snique.Utilities.extractBytesFromHexInString;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.security.InvalidKeyException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.InflaterInputStream;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.webkit.CacheManager;
 import android.webkit.WebView;
 
 public class SniqueActivity extends Activity {
 	private WebView wv;
 
 	private static final byte keyRaw[] = { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xaa, (byte) 0xbb, (byte) 0xcc,
 			(byte) 0xdd, (byte) 0xee, (byte) 0xff };
 	
 	private SniqueMessageDecoder decoder;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		this.setTitle(R.string.app_name);
 		try
 		{
 			decoder = new SniqueMessageDecoder(keyRaw);
 		}
 		catch (WillNeverWorkException e)
 		{
 			Log.e("SniqueActivity", "Could not create decoder",e);
 			System.exit(4);
 		}
 
 		wv = (WebView) findViewById(R.id.webView1);
 		wv.getSettings().setJavaScriptEnabled(true);
 		SniqueWebViewClient wvc = new SniqueWebViewClient(this);
 		wv.setWebViewClient(wvc);
 
 		String url = "http://blog.nomzit.com/snique/";
 
 		// Get HTML and decode outside of UI thread to avoid blocking UI.
 		new NetworkTask().execute(url);
 	}
 
 	protected void pageLoading(SniqueWebViewClient wvc, Bitmap favicon) {
 		try {
 			Method getActionBarMethod = this.getClass().getMethod("getActionBar", (Class<?>) null);
 			Object actionBar = getActionBarMethod.invoke(this, (Object) null);
 			Method setIconMethod = actionBar.getClass().getMethod("setIcon", Drawable.class);
 			setIconMethod.invoke(actionBar, new BitmapDrawable(favicon));
 		} catch (SecurityException e) {
 			Log.e("SniqueActivity", "Security exception getting getActionBar() method", e);
 		} catch (NoSuchMethodException e) {
 		} catch (IllegalArgumentException e) {
 			Log.e("SniqueActivity", "Illegal argument exception calling getActionBar() method", e);
 		} catch (IllegalAccessException e) {
 			Log.e("SniqueActivity", "Illegal access exception calling getActionBar() method", e);
 		} catch (InvocationTargetException e) {
 			Log.e("SniqueActivity", "Invocation target exception calling getActionBar() method", e);
 		}
 	}
 
 	private void displayData(SniqueMessage message)
 	{
 		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		Notification notification = new Notification();
 		notification.when = System.currentTimeMillis();
		notification.defaults = Notification.DEFAULT_ALL;
 		notification.flags = Notification.FLAG_AUTO_CANCEL;
 		notification.icon = R.drawable.statusbar;
		notification.setLatestEventInfo(getApplicationContext(), "snique", message.getMessage(), null);
 		notificationManager.notify(message.getId(), notification);
 	}
 
 	public void resetTitle() {
 		this.setTitle(R.string.app_name);
 	}
 
 	public SniqueMessage decodeData(String html) throws InvalidKeyException, NoMessageException, WillNeverWorkException
 	{
 		Log.d("SniqueActivity", "decodeData");
 		Pattern findsrc = Pattern.compile("src\\s*=\\s*['\"]([^']*)['\"]");
 
 		Matcher srcMatcher = findsrc.matcher(html);
 		boolean hassrc = srcMatcher.find();
 		if (!hassrc) {
 			Log.d("SniqueActivity", "No src tags");
 			return null;
 		}
 
 		List<String> urls = new ArrayList<String>();
 		do
 		{
 			String srcUrl = srcMatcher.group(1);
 			urls.add(srcUrl);
 		}
 		while (srcMatcher.find());
 
 		List<String> etags = new ArrayList<String>();
 		for (String imgUrl : urls)
 		{
 			Log.d("SniqueActivity", imgUrl);
 			// CacheManager seems to work well enough to be used for retrieving image data successfully.
 			CacheManager.CacheResult imgCr = CacheManager.getCacheFile(imgUrl, null);
 			String ETag = null;
 			if (imgCr != null)
 			{
 				ETag = imgCr.getETag();
 			}
 			else
 			{
 				// However, if we get here we couldn't find the image in the cache.
 				// At least we only need to get the headers, not the entire image.
 				Log.d("SniqueActivity", "Image " + imgUrl + " not in cache, downloading headers");
 				try
 				{
 					HttpURLConnection con = (HttpURLConnection) new URL(imgUrl).openConnection();
 					con.connect();
 					ETag = con.getHeaderField("ETag");
 				}
 				catch (MalformedURLException e)
 				{
 					Log.e("SniqueActivity", "MalformedURLException", e);
 				} 
 				catch (IOException e) 
 				{
 					Log.e("SniqueActivity", "IOException", e);
 				}
 			}
 
 			if (ETag == null)
 				continue;
 			Log.d("SniqueActivity", ETag);
 			if ((ETag.charAt(0) == 'W') || (ETag.charAt(0) == 'w'))
 				ETag = ETag.substring(2, ETag.length() - 1);
 			else
 				ETag = ETag.substring(1, ETag.length() - 1);
 			etags.add(ETag);
 		}
 		urls = null;
 		Log.d("SniqueActivity", "etags are" + etags);
 		byte message[][] = new byte[etags.size()][];
 		int messageIndex = 0;
 		for (String etag:etags)
 		{
 			byte newFragment[] = extractBytesFromHexInString(etag);
 			if (newFragment.length > 0)
 				message[messageIndex++] = newFragment;
 		}
 		return decoder.decodeMessage(new CodedMessage(message));
 	}
 
 	protected class NetworkTask extends AsyncTask<String, Void, SniqueMessage>
 	{
 		@Override
 		protected SniqueMessage doInBackground(String... params)
 		{
 			// Retrieve HTML source from URL being loaded
 			String url = params[0];
 			String html = null;
 			String mimeType = "text/html";
 			String charSet = "US-ASCII";
 			try
 			{
 				HttpClient client = new DefaultHttpClient();
 				HttpGet request = new HttpGet(url);
 				Header acceptEncoding = new BasicHeader("Accept-Encoding","gzip;q=1.0, identity;q=0.5, deflate;q=0.1, *;q=0");
 				request.setHeader(acceptEncoding);
 				HttpResponse response = client.execute(request);
 				Header headers[] = response.getHeaders("content-type");
 				for (Header contentType: headers)
 				{
 					String value = contentType.getValue();
 					
 					Pattern findMime = Pattern.compile("\\s*([^;\\s]*)");
 					Matcher mimeMatcher = findMime.matcher(value);
 					boolean hasMime = mimeMatcher.find();
 					if (hasMime)
 					{
 						mimeType = mimeMatcher.group(1);
 					}
 					else
 					{
 						Log.i("SniqueActivity", "No MIME type in response for url "+ url);
 					}
 
 					Pattern findCharset = Pattern.compile(";\\s*charset\\s*=\\s*([^;\\s]*)");
 					Matcher charsetMatcher = findCharset.matcher(value);
 					boolean hasCharset = charsetMatcher.find();
 					if (hasCharset)
 					{
 						charSet = charsetMatcher.group(1);
 					}
 					else
 					{
 						Log.i("SniqueActivity", "No character set in response for url "+ url);
 					}
 				}
 				
 				boolean hasGzip = false;
 				boolean hasDeflate = false;
 				headers = response.getHeaders("content-encoding");
 				for (Header contentEncoding: headers)
 				{
 					String value = contentEncoding.getValue();
 					
 					Pattern findGzip = Pattern.compile("\\s*(gzip)",Pattern.CASE_INSENSITIVE);
 					Matcher gzipMatcher = findGzip.matcher(value);
 					hasGzip = gzipMatcher.find();
 					Log.i("SniqueActivity", "gzip content-encoding? "+ hasGzip);
 
 					Pattern findDeflate = Pattern.compile("\\s*(deflate)",Pattern.CASE_INSENSITIVE);
 					Matcher deflateMatcher = findDeflate.matcher(value);
 					hasDeflate = deflateMatcher.find();
 					Log.i("SniqueActivity", "deflate content-encoding? "+ hasDeflate);
 					
 					if (!(hasGzip || hasDeflate))
 						Log.e("SniqueActivity", "Cannot process content-encoding: "+ value);
 				}
 
 				InputStream in = response.getEntity().getContent();
 				if (hasGzip)
 					in = new GZIPInputStream(in);
 				else if (hasDeflate)
 					in = new InflaterInputStream(in);
 				BufferedReader reader = new BufferedReader(new InputStreamReader(in,Charset.forName(charSet)));
 				StringBuilder str = new StringBuilder();
 				String line = null;
 				while ((line = reader.readLine()) != null) {
 					str.append(line);
 				}
 				in.close();
 				html = str.toString();
 			}
 			catch (IOException ioe)
 			{
 				ioe.printStackTrace();
 			}
 
 			if (html == null)
 			{
 				return null;
 			}
 
 			// Display page in WebView
 			wv.loadDataWithBaseURL(url, html, mimeType, charSet, null);
 
 			// Check for data to decode
 			SniqueMessage data = null;
 			try
 			{
 				data = decodeData(html);
 			}
 			catch (InvalidKeyException e)
 			{
 				Log.e("SniqueActivity - Network Task", "Invalid Key", e);
 			}
 			catch (NoMessageException e)
 			{
 				Log.d("SniqueActivity - Network Task", "No Message");
 			}
 			catch (WillNeverWorkException e)
 			{
 				Log.e("SniqueActivity - Network Task", "Decoding will never work", e);
 			}
 			return data;
 		}
 
 		@Override
 		protected void onPostExecute(SniqueMessage message) {
 			if (message != null) {
 				// We have a decoded message, return to UI thread
 				displayData(message);
 			} else {
 				// No message detected, so reset the title of the activity
 				resetTitle();
 			}
 		}
 	}
 }
