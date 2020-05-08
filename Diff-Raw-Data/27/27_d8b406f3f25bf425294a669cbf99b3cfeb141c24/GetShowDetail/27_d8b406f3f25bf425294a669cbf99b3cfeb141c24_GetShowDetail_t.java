 package com.hackathon.tvnight.api;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 
import android.util.Log;

 import com.hackathon.tvnight.model.ShowEntityList;
 import com.hackathon.tvnight.model.TVShow;
 import com.hackathon.tvnight.util.JSONHelper;
 
 public class GetShowDetail {
 	
 	public TVShow getDetail(String id) {
 		TVShow show = null;
 		try {
 			// get show time
 			String query = ApiConstant.COMCAST_SERVER +
 					ApiConstant.QUERY_DETAILS + "?" +
 					"q=id%3A" + id +	// id:<id> 
 					"&" + ApiConstant.MASHERY_KEY;
 
 			URL url = new URL(query);
 			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
 			String response = sendRequest(conn);
			Log.d(this.getClass().getSimpleName(), "show detail ---> " + response);
 			ShowEntityList detailList = JSONHelper.fromJson(response, ShowEntityList.class);
 			if (detailList != null) {
 				ArrayList<TVShow> list = detailList.getEntities();
 				if (list != null && list.size() > 0) {
 					//there should be only 1
 					show = list.get(0);
 				}
 			}
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		return show;
 	}
 
 	private String sendRequest(HttpURLConnection conn) throws IOException {
 		StringBuilder builder = new StringBuilder();			
 		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 		String line = null;
 		while ((line = reader.readLine()) != null) {
 			builder.append(line);
 		}
 		
 		return builder.toString();		
 	}
 }
