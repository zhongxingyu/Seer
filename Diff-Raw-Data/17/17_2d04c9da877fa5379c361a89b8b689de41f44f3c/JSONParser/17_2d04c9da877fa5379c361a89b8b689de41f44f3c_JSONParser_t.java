 package com.gpslistener.helpers;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpEntity;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.gpslistener.models.GeoCodingAPI_Response;
 
 public class JSONParser 
 {
 	
 	public static JSONObject getJSONFromEntity(HttpEntity entity)
 	{
 		JSONObject jsonObject = null;
 		StringBuilder builder = new StringBuilder();
 		String line = null;
 		try
 		{
 		InputStream in = entity.getContent();
 		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 		while((line=reader.readLine())!=null)
 			builder.append(line+"\n");
 		in.close();
 		jsonObject = new JSONObject(builder.toString());
 		}
 		
 		catch(Exception e) 
 		{
 			
 		}
 		return jsonObject;
 	}
 	
 	public static GeoCodingAPI_Response parseJSON(JSONObject jsonObject)
 	{
 		GeoCodingAPI_Response response = new GeoCodingAPI_Response();
 		try 
 		{
 			JSONArray results = jsonObject.getJSONArray(GeoCodingAPI_Response.TAG_RESULTS);
 			JSONObject object = results.getJSONObject(0);
 			response.setName(object.getString(GeoCodingAPI_Response.TAG_ADDRESS));
 			response.setLatitude(object.getJSONObject(GeoCodingAPI_Response.TAG_GEOMETRY).getJSONObject(GeoCodingAPI_Response.TAG_LOCATION).getString(GeoCodingAPI_Response.TAG_LATITUDE));
 			response.setLongitude(object.getJSONObject(GeoCodingAPI_Response.TAG_GEOMETRY).getJSONObject(GeoCodingAPI_Response.TAG_LOCATION).getString(GeoCodingAPI_Response.TAG_LONGITUDE));
 		} 
 		catch (JSONException e) 
 		{
			response.setName("Request denied by Google API");
 			e.printStackTrace();
 		}
 		return response;
 	}
 
 }
