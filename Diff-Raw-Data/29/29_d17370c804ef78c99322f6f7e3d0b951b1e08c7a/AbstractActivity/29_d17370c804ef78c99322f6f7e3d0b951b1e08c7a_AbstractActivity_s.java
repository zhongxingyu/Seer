 package com.engine9;
 
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Vector;
 
 import com.engine9.R;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class AbstractActivity extends Activity {
 	
 	private JsonArray jData;
 	private ListView stopsList;
 	private AbstractAdapter adapter;
 	private Vector<AbstractInfo> stops = new Vector<AbstractInfo>();
 	private BroadcastReceiver br;
 	private Boolean registered = false;
 	private AbstractRequest ar;
 	
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_abstract);
 		
 		stopsList = (ListView) findViewById(R.id.abstract_list);
 		
 		Intent i = getIntent();
		String sURL = i.getStringExtra("stops");
 		ar = new AbstractRequest();
 		ar.execute(sURL);
 		}
 	
 	protected void onStop(){
 		super.onStop();
 		if(br != null && registered){
 			registered = false;
 			unregisterReceiver(br);
 		}
 		if(ar != null && ar.getStatus() == AsyncTask.Status.FINISHED){
 			ar.cancel(true);
 		}
 	}
 	
 	protected void onPause(){
 		super.onPause();
 		if(br != null && registered){
 			registered = false;
 			unregisterReceiver(br);
 		}
 		if(ar != null && ar.getStatus() == AsyncTask.Status.FINISHED){
 			ar.cancel(true);
 		}
 	}
 	
 	protected void onResume(){
 		super.onResume();
 		if(br != null && !registered){
 			registered = true;
 			registerReceiver(br, new IntentFilter(Intent.ACTION_TIME_TICK));
 		}
 	}
 	
 	
 	public class AbstractRequest extends Request{
 		@Override
 		public void onPostExecute(String result)
 		{
 			try{
 				jData = (JsonArray) JParser2.main(result);
 				loadStops();
 				
 				br =  new BroadcastReceiver(){
 
 					@Override
 					public void onReceive(Context context, Intent intent) {
 						 if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
 							 adapter.highlightSet = false;
 							 adapter.notifyDataSetChanged();
 						 }
 						
 					}
 					
 				};
 				
 				registerReceiver(br, new IntentFilter(Intent.ACTION_TIME_TICK));
 				registered = true;
 			}
 			catch(Exception e){
 				Toast toast = Toast.makeText(getApplicationContext(), "Error receiving request", Toast.LENGTH_SHORT);
 				toast.show();
 			}
 			
 		}
 	}
 	
 	private void loadStops(){
 		for (JsonElement j : jData){
 			JsonObject jo = j.getAsJsonObject();
 			
 			LatLng pos = new LatLng(jo.get("Lat").getAsDouble(), jo.get("Lng").getAsDouble());
 			Long t = Long.parseLong(jo.get("time").getAsString().substring(6, 19));
 			String des = jo.get("des").getAsString();
 			
 			AbstractInfo ai = new AbstractInfo(pos, t, des);
 			
 			stops.add(ai);
 		}
 		
 		Collections.reverse(stops);
 		
 		adapter = new AbstractAdapter(getApplicationContext(), stops);
 		stopsList.setAdapter(adapter);
 	}
 
 }
