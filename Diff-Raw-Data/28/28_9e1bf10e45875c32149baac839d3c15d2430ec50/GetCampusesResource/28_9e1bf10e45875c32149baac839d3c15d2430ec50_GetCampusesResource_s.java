 package com.supinfo.notetonsta.android.resource;
 
 import java.net.URI;
 import java.util.ArrayList;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.conn.ConnectTimeoutException;
 import org.apache.http.conn.HttpHostConnectException;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.widget.ArrayAdapter;
 
 import com.supinfo.notetonsta.android.entity.Campus;
 import com.supinfo.notetonsta.android.handler.BaseHandler;
 
 public class GetCampusesResource implements Runnable {
 	private String jsonString;
 	private ArrayList<Campus> parsedJSON = new ArrayList<Campus>();
 	
 	private Handler handler;
	private ArrayAdapter<Campus> adapter;
 	
	public GetCampusesResource(Handler h, ArrayAdapter<Campus> a) {
 		handler = h;
 		adapter = a;
 	}
 
 	public void run() {
 		Message message = handler.obtainMessage();
 		message.arg1 = BaseHandler.STATUS_STARTED;
 		handler.sendMessage(message);
 		message = handler.obtainMessage();
 		try {
 			getCampusesRaw();
 			parseJSON();
 			regenerateAdapter();
 			message.arg1 = BaseHandler.STATUS_FINISHED_OK;
 		} catch(ConnectTimeoutException e) { 
 			Log.w("Warning",e.getMessage(),e);
 			message.arg1 = BaseHandler.STATUS_ERROR_TIMEDOUT;
 		} catch(HttpHostConnectException e) {
 			Log.w("Warning",e.getMessage(),e);
 			message.arg1 = BaseHandler.STATUS_ERROR_CONNREFUSED;
 		}  catch (JSONException e) {
 			// This will be caught if JSON library can't parse server return
 			Log.w("Warning",e.getMessage(),e);
 			message.arg1 = BaseHandler.STATUS_ERROR_JSON;
 		} catch(Exception e) {
 			// We've to catch any unknown exception
 			Log.e("Error",e.getMessage(),e);
 			message.arg1 = BaseHandler.STATUS_ERROR_UNKNOWN;
 		} finally {
 			handler.sendMessage(message);
 		}
 	}
 
 	private void getCampusesRaw() throws ConnectTimeoutException, HttpHostConnectException, Exception {
 		jsonString = null;
 		BasicHttpParams basicHttpParams = new BasicHttpParams();
 		// Connection must time out after 10 second to prevent infinite loop
 		HttpConnectionParams.setConnectionTimeout(basicHttpParams, 10000);
 		HttpClient httpClient = new DefaultHttpClient(basicHttpParams);
 		
 		HttpGet httpGet = new HttpGet();
 		httpGet.setHeader("Accept", "application/json");
 		URI uri = new URI("http://192.168.0.13:8080/Note_ton_STA/resource/campus/");
 		httpGet.setURI(uri);
 		
 		HttpResponse response = httpClient.execute(httpGet);
 		jsonString = EntityUtils.toString(response.getEntity()); 
 	}
 	
 	private void parseJSON() throws JSONException {
 		JSONObject jsonObj = new JSONObject(jsonString);
 		JSONArray json = jsonObj.getJSONArray("campus");
 		int nb = json.length();
 		
 		JSONObject current;
 		Campus c;
 		for(int i = 0;i<nb;i++) {
 			current = json.getJSONObject(i);
 			c = new Campus();
 			c.setId(current.getLong("id"));
 			c.setName(current.getString("name"));
 			
 			parsedJSON.add(c);
 		}
 	}
 	
 	private void regenerateAdapter() {
 		adapter.clear();
 		for(Campus c : parsedJSON) {
 			adapter.add(c);
 		}
 	}
 }
