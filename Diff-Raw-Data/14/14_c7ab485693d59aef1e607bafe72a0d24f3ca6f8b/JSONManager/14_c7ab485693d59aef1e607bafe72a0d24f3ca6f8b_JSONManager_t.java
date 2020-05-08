 package com.iCompute.tour.backend;
 
 import android.util.Log;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.HttpClient;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpEntity;
 import org.apache.http.NameValuePair;
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import org.json.JSONObject;
 import java.util.Date;
import java.util.TimeZone;
 import java.text.SimpleDateFormat;
 
 /*
  * "Authorization:
 JMoEudX2ESn5ZNiUcMbFd25ynBErffCF7l4ezRWRe959PENv6XVYNckiImF7P34Q:OGJkYTM5YzA1MDIxYzA0YWZmZjNkNjlkYmJkNGU4ZDQzODEwNzMxYg==:bGlmZWlzbXVzaWM0MzRAZ21haWwuY29t:cGFzc3dvcmQ="   
  */
 
 public class JSONManager{
 	
 	private final String BASE_URL = "https://eebsy.com/api/tours:1";
 
 	private JSONObject mObject;
 	private ArrayList<NameValuePair> mHeaders;
 	
 	private final String AUTH_STRING = "JMoEudX2ESn5ZNiUcMbFd25ynBErffCF7l4ezRWRe959PENv6XVYNckiImF7P34Q:MDM1YjcwZDRmZTU1Y2FiNjk0MzJiNDllZjFiOTVkNGExYzQ3YTllYg==:bGlmZWlzbXVzaWM0MzRAZ21haWwuY29t:cGFzc3dvcmQ=";
 	private String mDateString;
 	
 	public JSONManager(){
 		mHeaders = new ArrayList<NameValuePair>();
 		Date now = new Date();
 		SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss Z");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
 		mDateString  = new StringBuilder(format.format(now)).toString();
		Log.i("Date: " ,mDateString);
 	}
 	
 	public void getToursJSON(){
 		HttpClient client = new DefaultHttpClient();
 		HttpGet get = new HttpGet(BASE_URL);
 		
		mHeaders.add(new BasicNameValuePair("Authorization:", AUTH_STRING));
 		mHeaders.add(new BasicNameValuePair("Date:", mDateString));
 		
		int i;
		for (i=0;i<mHeaders.size();i++){
			get.addHeader(mHeaders.get(i).getName(), mHeaders.get(i).getValue());
		}
		
 		//add headers to request
 		
 		
 		HttpResponse response;
 	    try {
 	        response = client.execute(get);
 	        // Examine the response status
 	        Log.i("Response: ",response.getStatusLine().toString());
 
 	        // Get hold of the response entity
 	        HttpEntity entity = response.getEntity();
 	        // If the response does not enclose an entity, there is no need
 	        // to worry about connection release
 
 	        if (entity != null) {
 
 	            // A Simple JSON Response Read
 	            InputStream instream = entity.getContent();
 	            String result= convertStreamToString(instream);
 	            Log.i("Result: ",result);
 	            // now you have the string representation of the HTML request
 	            instream.close();
 	        }
 
 
 	    }
 	    catch (Exception e) {}
 	}
 	    
     private static String convertStreamToString(InputStream is) {
 	    /*
 	     * To convert the InputStream to String we use the BufferedReader.readLine()
 	     * method. We iterate until the BufferedReader return null which means
 	     * there's no more data to read. Each line will appended to a StringBuilder
 	     * and returned as String.
 	     */
 	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 	    StringBuilder sb = new StringBuilder();
 
 	    String line = null;
 	    try {
 	        while ((line = reader.readLine()) != null) {
 	            sb.append(line + "\n");
 	        }
 	    } catch (IOException e) {
 	        e.printStackTrace();
 	    } finally {
 	        try {
 	            is.close();
 	        } catch (IOException e) {
 	            e.printStackTrace();
 	        }
 	    }
 	    return sb.toString();
 	}
 
 
 
 }
