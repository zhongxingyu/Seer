 package edu.cmusv.lions.petmobile.util;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.os.AsyncTask;
import android.util.Log;
 
 /**
  * Used for making an HTTP request. 
  * New versions of android do not allow HTTP requests to be made from the main UI thread.
  * 
  * @author mhennessy
  */
 public class HttpRequestAsyncTask extends AsyncTask<String, String, String> {
 
 	@Override
 	protected String doInBackground(String... uri) {
		
		Log.i("PET LIONS making http request to ",uri[0] );
		
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpResponse response;
 		String responseString = null;
 		try {
 			response = httpclient.execute(new HttpGet(uri[0]));
 			StatusLine statusLine = response.getStatusLine();
 			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
 				ByteArrayOutputStream out = new ByteArrayOutputStream();
 				response.getEntity().writeTo(out);
 				out.close();
 				responseString = out.toString();
 			} else {
 				// Closes the connection.
 				response.getEntity().getContent().close();
 				throw new IOException(statusLine.getReasonPhrase());
 			}
 		} catch (ClientProtocolException ex) {
 			ex.printStackTrace();
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return responseString;
 	}
 	
 	@Override
 	protected void onPostExecute(String result) {
 		super.onPostExecute(result);
 	}
 }
