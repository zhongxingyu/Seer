 package com.example.homeautomation.utility;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 import junit.framework.Test;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.protocol.HttpContext;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.Toast;
 public class GetRequestTask extends AsyncTask<String, Void, JSONObject>{
 	private String ip;
 	private String tag;
 	private HttpContext localContext;
 	private HttpClient client;
 	public GetRequestTask(String tag, String ip, HttpClient client, HttpContext localContext)
 	{
 		this.tag = tag;
 		this.ip = ip;
 		this.client = client;
 		this.localContext = localContext;
 	}
 	protected JSONObject doInBackground(String...urls){
 		String url = ip;
         Log.i(tag,"url set");
         HttpGet getRequest = new HttpGet(url);
         JSONObject jsonObject = null;
         try{
         HttpResponse response = client.execute(getRequest,localContext);
         
         Log.i(tag,"GET Request sent.");
         Log.i(tag,"Response Code: "+response.getStatusLine().getStatusCode());
         
         BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
         StringBuffer result = new StringBuffer();
         String line = "";
         while((line = reader.readLine())!=null)
         	result.append(line);
        String test = line;
        if (test.equals("a"))
        	line = "";
         jsonObject = new JSONObject(result.toString());
         }catch(Exception e){
         	Log.e(tag,e.toString());
         }
         
         return jsonObject;            
 	}
 }
