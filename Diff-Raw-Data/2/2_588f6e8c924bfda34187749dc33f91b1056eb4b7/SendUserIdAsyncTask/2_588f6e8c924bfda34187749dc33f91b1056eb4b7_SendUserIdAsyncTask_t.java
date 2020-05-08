 package cz.android.monet.restexample;
 
 import java.io.ByteArrayOutputStream;
 import java.io.OutputStream;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import cz.android.monet.restexample.interfaces.OnServerResultReturned;
 
 import android.R.bool;
 import android.os.AsyncTask;
 import android.util.Log;
 
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
 	
 	private boolean validateHost(String string) {
		Pattern p = Pattern.compile("^\\s*(.*?)");
 		Matcher m = p.matcher(string);
 
 		return m.matches();
 
 	}
 
 	protected String sendData(String host, String userId) {
 		
 		if(!this.validateHost(host))
 		{
 			Log.e(TAG, "Invalid host string | " + host);
 			return null;
 		}
 		
 		String urlToSendRequest = "http://" + host + ":" + "2323"
 				+ "/restfulexample/app/user/" + userId;
 		String targetDomain = host;
 		// String xmlContentToSend = "hello this is a test";
 
 		DefaultHttpClient httpClient = new DefaultHttpClient();
 
 		HttpHost targetHost = new HttpHost(targetDomain, 2323, "http");
 		// Using GET here
 		HttpGet httpGet = new HttpGet(urlToSendRequest);
 
 		// Make sure the server knows what kind of a response we will accept
 		httpGet.addHeader("Accept", "application/xml");
 
 		// Also be sure to tell the server what kind of content we are
 		// sending
 		httpGet.addHeader("Content-Type", "application/xml");
 
 		try {
 			// execute is a blocking call, it's best to call this code in a
 			// thread separate from the ui's
 			HttpResponse response = httpClient.execute(targetHost, httpGet);
 
 			// Have your way with the response
 			final OutputStream outstrem = new ByteArrayOutputStream();
 
 			response.getEntity().writeTo(outstrem);
 			return outstrem.toString();
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			Log.e(TAG, ex.getMessage());
 		}
 		return null;
 	}
 }
