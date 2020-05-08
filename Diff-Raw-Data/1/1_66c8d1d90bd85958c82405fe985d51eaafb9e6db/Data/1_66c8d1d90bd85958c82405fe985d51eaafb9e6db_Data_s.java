 package com.example.gpstracker;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 public class Data {
 	public static String login = "sygi";
 	public static String password = "?";
 	private static int lastSent = -1;
 	public static ArrayList<Pin> pos = new ArrayList<Pin>();
 	public static void sendTo(String host){
 		JSONObject json = new JSONObject();
 		try {
 			json.put("login", login);
 			json.put("passwd", password);
 			//TODO check if there's anything to send
 			JSONArray p = new JSONArray();
 			for(int j = lastSent + 1; j < pos.size(); j++){
 				p.put(pos.get(j).toJson());
 			}
 			json.put("positions", p);
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		StringEntity se = null;
 		try {
 			se = new StringEntity(json.toString());
 			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		//obiekt json i StringEntity utworzone
 		
 		HttpPost post = new HttpPost(host);
 		post.setEntity(se);
 		HttpClient client = new DefaultHttpClient();
 		try {
 			Log.d("sygi", "sending");
 			client.execute(post);
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
