 package edu.pugetsound.vichar;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONObject;
 
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.IBinder;
 
 
 /**
  * A bound service that provides a simple, non-continuous interface with the server.
  * Used for objects that only need to be sent or received once or infrequently. 
  * @author Kirah Taylor & Robert Shapiro
  * @version 10/24/12
  */
 public class HttpService extends Service {
 
 	
 private final IBinder binder = new LocalBinder();
 private Object jsonobj = new JSONObject();
 
 	/**
 	 * Takes JSON objects sent by activities and passes them to the server
	 * @param view The calling view
 	 */
 	public void send(Intent intent) {
 		final Intent theIntent = intent;
 				Bundle extras;
 				extras = theIntent.getExtras();
 				jsonobj = extras.get("WORDS");
 				new SendObject().execute();
 			}
 	
 /**
  * Describes the interface for the IBiner object passed to activities upon binding
  * @author Kirah Taylor
  * @version 10/24/12
  */
 public class LocalBinder extends Binder {
         HttpService getService() {
         return HttpService.this;
         }
 	}
 
 	/**
 	 * Returns the communication interface
 	 * @return Returns binder the client can use to interface with service
 	 */
 	@Override
 	public IBinder onBind(Intent intent) {
 		return binder;
 
 	}
 	
     /**
      * Sends the JSON object to the server in a separate thread
      * @author Kirah Taylor
      * @version 10/24/12
      */
 	private class SendObject extends AsyncTask<Boolean, Boolean, Boolean> 
     {    	
     	/**
     	 * Send the object to the server
     	 * @return True if successful, false if not
     	 */
     	@Override
         protected Boolean doInBackground(Boolean...booleans)
         {
     		HttpClient httpclient = new DefaultHttpClient();
 			HttpPost httppost = new HttpPost("http://puppetmaster.pugetsound.edu:1730");
             boolean result = false;
             try 
             {
             	StringEntity words = new StringEntity(jsonobj.toString());
 				words.setContentType("application/json;charset=UTF-8");
 				words.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
 				httppost.setEntity(words);
 				httpclient.execute(httppost);
             	result = true;
             }
     		catch (ClientProtocolException ex)
     		{
     			System.out.println(ex);
     		}
             catch (UnsupportedEncodingException ex) {
             	System.out.println(ex);
 			} 
 			catch (IOException ex) {
 				System.out.println(ex);
 			}
             return result;
         }
     	
         /**
          * Performs function of displaying result, but here
          * it won't be displayed.
          */
         @Override
         protected void onPostExecute(Boolean result) 
         {
         	return;
         }
     }
 
 }
 
 
