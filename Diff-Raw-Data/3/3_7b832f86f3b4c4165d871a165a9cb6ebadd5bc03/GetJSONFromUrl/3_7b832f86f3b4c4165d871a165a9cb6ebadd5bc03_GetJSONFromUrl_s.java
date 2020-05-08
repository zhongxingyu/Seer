 package com.example.repbase;
 
 // TODO: spyke: pass Exception not via JSONObject but via exception mechanism  
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 ///import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class GetJSONFromUrl extends AsyncTask<String, Integer, JSONObject> {
 	private final String magicExcStartStr = "The exception message is '";
 	private final String magicExcEndStr = "'. See server";
 
 	protected JSONObject doInBackground(String... Urls) {
 		try {
 			HttpGet request = new HttpGet(Urls[0]);
 			request.setHeader("Accept", "application/json");
 			request.setHeader("Content-type", "application/json");
 
 			DefaultHttpClient httpClient = new DefaultHttpClient();
 			HttpResponse response = httpClient.execute(request);
 			HttpEntity resp = response.getEntity();
 			
 			InputStream stream = resp.getContent();
 			InputStreamReader reader = new InputStreamReader(stream, "utf-8");
 			
 			// BufferedReader is used for big blocks of data
 			BufferedReader br=new BufferedReader(reader);
 			
 			// we have to read respond per line
 			// because reader.read(buffer) sometimes places corrupted data into the buffer
 			// I didn't find the nature of such issue
 			// and used code from here: http://habrahabr.ru/qa/11449/
 			String strBuffer=null;
 			StringBuilder sb = new StringBuilder();
 			while ((strBuffer = br.readLine()) != null) {
 				sb.append(strBuffer);
 			}
 			strBuffer = sb.toString();
 			
 			stream.close();
 			reader.close();
 			br.close();
 
 			JSONObject result;
 
 			Log.d("Auth", "server's respond (the value of strBuffer): "
 					+ strBuffer);
 			try {
 				result = new JSONObject(strBuffer);
 			} catch (JSONException ex) {
 				Log.d("changeexc", "exception during creating JSONObject occurred.");
 				int startIndex = strBuffer.indexOf(magicExcStartStr);
 				int endIndex = strBuffer.indexOf(magicExcEndStr);
 				if (startIndex != -1 && endIndex != -1) {
 					Log.d("changeexc", "CAN parse");
 					startIndex += magicExcStartStr.length();
 					endIndex -= 1;
 					
 					String substr=strBuffer.substring(startIndex, endIndex);
 					Log.d("changeexc", "startIndex: " + String.valueOf(startIndex)
 					 + "; endIndex: " + String.valueOf(endIndex)+" " + substr+"; "+substr.replace("\"","\\\""));
 					result = new JSONObject("{\"Exception\":\""
 							+ strBuffer.substring(startIndex, endIndex).replace("\"","\\\"")
 							+ "\"}");
 					Log.d("changeexc", result.toString());
 				} else {
 					Log.d("changeexc", "can't parse");
 					result = new JSONObject(
 							"{\"Exception\":\"Error occurred. Can't parse error's text from server's respond\"}");
 				}
 			}
 			Log.d("changeexc","result from GetJSONFromUrl: "+result.toString());
 			return result;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 }
