 package com.yugy.qianban.sdk;
 
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.JsonHttpResponseHandler;
 import com.loopj.android.http.PersistentCookieStore;
 
 public class Douban {
 	
 	private AsyncHttpClient client;
 	
 	public Douban(Context context){
 		client = new AsyncHttpClient();
 		client.setCookieStore(new PersistentCookieStore(context));
 	}
 	
 	public void getCatalog(final JsonHttpResponseHandler responseHandler){
 		final JSONObject result = new JSONObject();
 		client.get("http://douban.fm/j/explore/hot_channels?start=0&limit=12", new JsonHttpResponseHandler(){
 			@Override
 			public void onSuccess(int statusCode, JSONObject response) {
 				// TODO Auto-generated method stub
 				try {
 					result.put("hot_channels", response.getJSONObject("data").getJSONArray("channels"));
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				super.onSuccess(statusCode, response);
 			}
 		});
 		client.get("http://douban.fm/j/explore/up_trending_channels?start=0&limit=12", new JsonHttpResponseHandler(){
 			@Override
 			public void onSuccess(int statusCode, JSONObject response) {
 				// TODO Auto-generated method stub
 				try {
 					result.put("fast_channels", response.getJSONObject("data").getJSONArray("channels"));
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				super.onSuccess(statusCode, response);
 			}
 		});
 		try {
 			result.put("com_channels", new JSONArray());
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				while(true){
 					if(result.has("hot_channels") && result.has("fast_channels") && result.has("com_channels")){
 						responseHandler.onSuccess(result);
 						break;
 					}
 				}
 			}
 		}).start();
 	}
 	
 	public void getSongs(String id, final JsonHttpResponseHandler responseHandler){
		client.get("http://douban.fm/j/mine/playlist?type=n&channel=" + id + "&pb=64&from=mainsite", new JsonHttpResponseHandler(){
 			@Override
 			public void onSuccess(JSONObject response) {
 				// TODO Auto-generated method stub
 				try {
 					responseHandler.onSuccess(response.getJSONArray("song"));
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				super.onSuccess(response);
 			}
 			
 			@Override
 			public void onFailure(Throwable error, String content) {
 				// TODO Auto-generated method stub
 				super.onFailure(error, content);
 			}
 		});
 	}
 	
 }
