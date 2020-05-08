 package org.leifolson.withinreach;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 
 public class ServerComMgr extends AsyncTask<String, Void, String> 
 {
 
 
 	protected String doInBackground(String... params) 
 	{
 		
 		//params[0] should be the url of the json file. I used:
 		// "http://withinreach.herokuapp.com/arrival/6309" as url for this test
 		
 		//to call this function in main thread:
		// new ServerCommunication().execute(url);
 		
 		
 		HttpClient client = new DefaultHttpClient();
 		HttpGet httpGet = new HttpGet(params[0]);
 		HttpResponse response;
 		StringBuilder stringBuilder = new StringBuilder();
 		String result;
 		
 		try //getting the http response and building a string out of it
 		{
 			response = client.execute(httpGet);
 			StatusLine statusLine = response.getStatusLine();
 			HttpEntity entity = response.getEntity();
 		    InputStream content = entity.getContent();
 		    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
 		    String line;
 		
 		    while ((line = reader.readLine()) != null)
 		    {
 		        stringBuilder.append(line + "\n");
 		    }
 			
 			
 		} 
 		catch (ClientProtocolException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		catch (IOException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		result = stringBuilder.toString();
 		
 		try //building the JSON object and grabbing the ETA of a bus
 		{
 			JSONObject jsonObject = new JSONObject(result);
 		
 			JSONArray jsonArray = jsonObject.getJSONObject("resultSet").getJSONArray("arrival");
 			JSONObject jsonObj = jsonArray.getJSONObject(0);
 			String output = jsonObj.getString("estimated");
 			System.out.println("ETA: " + output);
 		}
 		catch (JSONException e)
 		{
 			System.out.println(e.getMessage());
 			
 		}
 		
 		
 
 		
 		return null;
 	}
 
 }
