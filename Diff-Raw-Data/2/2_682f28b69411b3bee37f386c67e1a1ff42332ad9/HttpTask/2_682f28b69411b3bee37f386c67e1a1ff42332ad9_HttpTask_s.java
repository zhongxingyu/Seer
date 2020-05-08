package com.fixedd.AndroidTrimet.util;
 
 import java.io.IOException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 /**
  * A class for making asynchronous HTTP requests.
  * @author Jeremy Logan
  *
  */
 public class HttpTask extends AsyncTask<HttpUriRequest, Integer, HttpResponse> {
 	private HttpTaskCaller mCaller;
 	
 	/**
 	 * Class constructor
 	 * @param caller the calling Activity. Must implement HttpTaskCaller.
 	 */
 	public HttpTask(HttpTaskCaller caller) {
 		this.mCaller = caller;
 	}
 	
 	
 	/**
 	 * Make the HTTP request on a background thread.
 	 * @param request a pre-built request object that is to be run on the background thread. 
 	 */
 	public HttpResponse doInBackground(HttpUriRequest... request) {
 		HttpResponse resp = null;
 		
 		Log.i(getClass().getSimpleName(), "Starting HTTP Request");
     	try {
     		HttpClient httpClient = new DefaultHttpClient();
     		resp = httpClient.execute(request[0]);
 		} catch (ClientProtocolException e) {
 			Log.e(getClass().getSimpleName(), "HTTP protocol error", e);
 		} catch (IOException e) {
 			Log.e(getClass().getSimpleName(), "Communication error", e);
 		}
 		Log.i(getClass().getSimpleName(), "Ending HTTP Request");
 				
     	return resp;
 	}
 	
     protected void onProgressUpdate(Integer... progress) {
     	this.mCaller.handlePercent(progress[0]);
     }
 
     protected void onPostExecute(HttpResponse response) {
     	this.mCaller.handleResponse(response);
     }
     
     public interface HttpTaskCaller {
     	void handleResponse(HttpResponse response);
     	void handlePercent(Integer progress);
     	void handleError(Exception exception);
     }
 }
