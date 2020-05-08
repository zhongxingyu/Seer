 package com.pingme.service;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 import com.pingme.model.POIData;
 
 public class ServerRequest {
 
	private static final String SERVER_URL = "http://pingme.cloudfoundry.com/poi/%f+%f+%f+%s";
 	
 	public static POIData fetchPOI( double lat, double lng, double radius, String[] categories ) throws ClientProtocolException, IOException, JSONException {
		final int timeoutConnection = 45000;
 
 		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters,timeoutConnection);
 
 		// Set the default socket timeout (SO_TIMEOUT)
 		// in milliseconds which is the timeout for waiting for data.
		final int timeoutSocket = 45000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
 
 		// Initialize the HTTP client with params
 		HttpClient httpclient = new DefaultHttpClient(httpParameters);
 
         
 		// Prepare a request object
 		StringBuilder sb = new StringBuilder();
 		for( String categorie : categories ){
 			if( sb.length() > 0 ) sb.append(",");
 			sb.append( categorie );
 		}
 		
		String serverURL = String.format( SERVER_URL, lat, lng, radius, sb.toString() );
 		Log.v( "ServerRequest", "URL: " + serverURL );
 		HttpGet httpget = new HttpGet(serverURL);
 		HttpResponse httpResponse = httpclient.execute(httpget);
 
 		// Get hold of the response entity
 		HttpEntity entity = httpResponse.getEntity();
 		
 		if (entity != null) 
 		{
 			InputStream instream = entity.getContent();
 			String stringResult = streamToString(instream);
 			
 			return parseJSON( stringResult );
 					
 		}
 
 		return null;
 
 	}
 	public static POIData parseJSON(String jsonString) throws JSONException {
 		Log.v( "ServerRequest parsing", jsonString );
 		
 		POIData data = new POIData();
 		JSONArray jsonArray = new JSONObject(jsonString).getJSONArray("poiDatas");
 		JSONObject jsonObject = jsonArray.getJSONObject(0); 
 		//data.setId( jsonObject.getString("id") );
 		data.setTitle( jsonObject.getString("title") );
 		data.setDescription( jsonObject.getString("description") );
 		data.setAddresse( jsonObject.getString("addresse") );
 		data.setLat( jsonObject.getDouble("latitude") );
 		data.setLng( jsonObject.getDouble("longitude") );
 		data.setCategory( jsonObject.getString("category") );
 		//data.setUrl_image( jsonObject.getString(""));
 		
 		
 		return data;		
 	}
 	
 	private final static int bufferSize = 8192;
 
 	public static String streamToString(InputStream is)
 			throws UnsupportedEncodingException {
 
 		BufferedReader reader = new BufferedReader( new InputStreamReader(is, "UTF-8"), bufferSize );
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
 				reader.close();
 			} catch (IOException e) {
 				Log.e("ServerRequest", e.getMessage(), e );
 			}
 		}
 		return sb.toString();
 	}
 
 }
