 package com.Cory.week_3_final_project;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.ContextWrapper;
 import android.os.Bundle;
 import android.os.Looper;
 import android.util.Log;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 
 public class PulledInfo extends Activity {
 	
 	// global variables
 	SaveData m_file;
 	String fileName = "returned_json.txt";
 	
 	ListView listView;
 	
 	Context _context;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.pulledinfo);
 		
 		_context = this;
 		
 		// targetting my listView
         listView = (ListView)this.findViewById(R.id.list);
 		View listHeader = this.getLayoutInflater().inflate(R.layout.list_header, null);
         listView.addHeaderView(listHeader);
 		
 		displayData();
 		
 		
 	}
 	
 	public void displayData(){
 		
 		// having an issue with the context of this s
		//String JSONString = m_file.readStringFile(_context, fileName);
 		
 		
     	/*
     	ArrayList<HashMap<String, String>>mylist = new ArrayList<HashMap<String,String>>();
     	JSONObject job = null;
     	JSONArray results = null;
     	
     	
     	try{
     		
     		// getting the array from the field "results"
     		job = new JSONObject(JSONString);
     		results = job.getJSONArray("results");
     		
     		// gathers the specific fields
     		String artistName = results.getJSONObject(0).getString("artistName").toString();
     		String artistGenre = results.getJSONObject(0).getString("primaryGenreName").toString();
     		String artistURL = results.getJSONObject(0).getString("artistLinkUrl").toString();
     		
     		//text.setText("artistName: " + artistName + "artistGenre: " + artistGenre + "artistURL: " + artistURL);
     		HashMap<String, String> displayMap = new HashMap<String, String>();
     		displayMap.put("artist", artistName);
     		displayMap.put("genre", artistGenre);
     		displayMap.put("url", artistURL);
     		
     		//displayMap.put("artist", cursor.getString(1));
     		
     		mylist.add(displayMap);
     		
     		// this is complicated but it basically assigns the rows for each element
     		SimpleAdapter adapter = new SimpleAdapter(this, mylist, R.layout.list_row, 
     				new String[] {"artist", "genre", "url"}, 
     				new int[] {R.id.artist, R.id.genre, R.id.url});
     		
     		listView.setAdapter(adapter);
     		
     	} catch(Exception e){
     		
     	}*/
 	}
 	
 }
