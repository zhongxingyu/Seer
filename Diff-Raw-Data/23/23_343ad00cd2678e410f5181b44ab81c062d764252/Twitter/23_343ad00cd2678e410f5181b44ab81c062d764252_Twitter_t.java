 package com.example.iceberg;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ListFragment;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.SimpleAdapter;
 
 public class Twitter extends ListFragment {
 	
 	//stores player twitter handles	
    String[] twitterHandle = new String[11];
     //stores actual tweets
    String[] tweets = new String[11];
     	
 	public Twitter() {
 	}
     
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
 		//Call twitter api to get tweets
 		//For some reason having rpp=12 means we get 15 results.
 		String result = null;
 		try {
			result = new TwitGet().execute("http://search.twitter.com/search.json?q=lincolnstars&rpp=10&result_type=mixed").get();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		//Log.i("afl",result);
 		JSONArray jArray = null;
 		try {
 			JSONObject jObject = new JSONObject(result);
 			jArray = jObject.getJSONArray("results");
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		//Log.i("af",""+jArray.length());
		twitterHandle[0] = "#LincolnStars";
		tweets[0] = "Use the LincolnStars hash tag to join the conversation!";
		for(int i=0; i < 11; i++){
 			JSONObject oneObject;
 			try {
 				oneObject = jArray.getJSONObject(i);
				twitterHandle[i+1] = oneObject.getString("from_user");
				tweets[i+1] = oneObject.getString("text");
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}	    
 		}
 		
 		
 		// Each row in the list stores twitter handle and tweet
         List<HashMap<String,String>> dataList = new ArrayList<HashMap<String,String>>();
  
         for(int i=0;i< twitterHandle.length;i++){
             HashMap<String, String> dataMap = new HashMap<String,String>();
             dataMap.put("twitterHandle", twitterHandle[i]);
             dataMap.put("tweets", tweets[i]);
             dataList.add(dataMap);
         }
         
 		// Inflate the layout for this fragment  
         View view =  inflater.inflate(R.layout.twitter_list_fragment, container, false);   
         
         // Keys used in Hashmap
         String[] from = {"twitterHandle","tweets" };
  
         // Ids of views in listview_layout
         int[] to = {R.id.twitterHandle,R.id.tweets};
         SimpleAdapter adapter = new SimpleAdapter(getActivity(), dataList, R.layout.twitter_listview_layout, from, to);
         setListAdapter(adapter);  
 
         return view;
 	}
 }
