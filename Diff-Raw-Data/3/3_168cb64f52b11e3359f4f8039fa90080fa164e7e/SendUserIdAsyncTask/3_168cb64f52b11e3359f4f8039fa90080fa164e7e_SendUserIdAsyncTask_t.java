 package cz.android.monet.restfulclient;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URISyntaxException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import cz.android.monet.restfulclient.interfaces.OnServerResultReturned;
 
 import android.R.bool;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.webkit.URLUtil;
 
 public class SendUserIdAsyncTask extends AsyncTask<Object, Void, String> {
 
 	private static final String TAG = "SendUserIdAsyncTask";
 
 	OnServerResultReturned mResultCallback;
 
 	@Override
 	protected String doInBackground(Object... params) {
 
 		mResultCallback = (OnServerResultReturned) params[2];
 		return sendData(params[0].toString(), params[1].toString());
 	}
 
 	protected void onPostExecute(String result) {
 		mResultCallback.onResultReturned(result);
 	}
 
 /*	private boolean validateHost(String string) {
 		Pattern p = Pattern.compile("^\\s*(.*?)");
 		Matcher m = p.matcher(string);
 
 		return m.matches();
 	}*/
 
 	protected String sendData(String targetDomain, String userId) {
 
 		try {
 			if (!targetDomain.matches("^\\s*(.*?)")) {
 				Log.e(TAG, "Invalid host string | " + targetDomain);
 				return null;
 			}
 
 /*			String urlToSendRequest = "http://" + targetDomain + ":" + "2323"
 					+ "/restfulexample/app/user/" + Integer.parseInt(userId);*/
 			
 			Uri uri = new Uri.Builder()
 		    .scheme("http")
 		    .authority(targetDomain + ":2323")
 		    .path("/restfulexample/app/user/")
 		    .appendQueryParameter("param1", userId)
 		    .build();
 			if(!URLUtil.isValidUrl(uri.toString()))
 			{
 				Log.e(TAG, "Invalid uri |" + uri.toString());
 				return null;
 			}
 
 
 			DefaultHttpClient httpClient = new DefaultHttpClient();
 			
 			HttpHost targetHost = new HttpHost(targetDomain, 2323, "http");
 			// Using GET here
 			//HttpGet httpGet = new HttpGet(urlToSendRequest);
 			HttpGet httpGet = new HttpGet(new java.net.URI(uri.toString()));
 
 			// Make sure the server knows what kind of a response we will accept
 			httpGet.addHeader("Accept", "application/xml");
 
 			// Also be sure to tell the server what kind of content we are
 			// sending
 			httpGet.addHeader("Content-Type", "application/xml");
 
 			// execute is a blocking call, it's best to call this code in a
 			// thread separate from the ui's
 			HttpResponse response = httpClient.execute(targetHost, httpGet);
 
 			// Have your way with the response
 			final OutputStream outstrem = new ByteArrayOutputStream();
 
 			response.getEntity().writeTo(outstrem);
 			return outstrem.toString();
 
 		} catch (ClientProtocolException ex) {
 			ex.printStackTrace();
 			Log.e(TAG, ex.getMessage());
 		} catch (URISyntaxException ex) {
 			ex.printStackTrace();
 			Log.e(TAG, ex.getMessage());
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
			Log.e(TAG, ex.getMessage());
 		} catch (IOException ex) {
 			ex.printStackTrace();
 			Log.e(TAG, ex.getMessage());
 		}
 
 		return null;
 	}
 }
