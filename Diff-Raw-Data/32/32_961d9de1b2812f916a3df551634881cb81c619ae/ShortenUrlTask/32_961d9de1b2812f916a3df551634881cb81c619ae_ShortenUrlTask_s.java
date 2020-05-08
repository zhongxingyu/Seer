 package com.snot.whereareyou;
 
 import android.os.AsyncTask;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 
 // http://login2win.blogspot.dk/2012/07/android-shorten-url-implementation.html
 // https://developers.google.com/url-shortener/v1/getting_started
 
 // TODO: if urlshortener api is disabled null is returned...
 //       make it such that the long url is returned
 
 public class ShortenUrlTask extends AsyncTask<String, Void, String> {
 
 	@Override
 	protected String doInBackground(String... url) {
 		String mLongUrl = url[0];
		String API_KEY = "AIzaSyBrux8v37VrTuSalEm-BJ2nYoJoLxSljq0";
 		String API_URL = "https://www.googleapis.com/urlshortener/v1/url?key=" + API_KEY;
 
 		try {
 // Set connection timeout to 5 secs and socket timeout to 10 secs
 //			HttpParams httpParameters = new BasicHttpParams();
 //			int timeoutConnection = 5000;
 //			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
 //			int timeoutSocket = 10000;
 //			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
 
 //			HttpClient hc = new DefaultHttpClient(httpParameters);
 			HttpClient hc = new DefaultHttpClient();
 			HttpPost request = new HttpPost(API_URL);
 			request.setHeader("Content-type", "application/json");
 			request.setHeader("Accept", "application/json");
 
 			JSONObject obj = new JSONObject();
 			obj.put("longUrl", mLongUrl);
 			request.setEntity(new StringEntity(obj.toString(), "UTF-8"));
 
 			HttpResponse response = hc.execute(request);
 
 			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
 			{
 				ByteArrayOutputStream out = new ByteArrayOutputStream();
 				response.getEntity().writeTo(out);
 				out.close();
 				final JSONObject json = new JSONObject(out.toString());
 				return json.getString("id");
 			}
 			else
 			{
 				return null;
 			}
 		}
 		catch(JSONException e)
 		{
 			e.printStackTrace();
 		}
 		catch(UnsupportedEncodingException e)
 		{
 			e.printStackTrace();
 		}
 		catch(IOException e)
 		{
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
 
