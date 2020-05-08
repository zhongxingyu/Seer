 package com.cloud.pushpin;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 
 public class Listfriends extends ListActivity implements Serializable{
 
 	 ArrayList<String> listItems=new ArrayList<String>();
 	    //DEFINING STRING ADAPTER WHICH WILL HANDLE DATA OF LISTVIEW
 	 ArrayAdapter<String> adapter;
 	 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.flist);
 		Intent i=getIntent();
 		//recieve access token
 		String access_token=(String)i.getSerializableExtra("access_token");
 		Httpclass http=new Httpclass();
 		JSONArray arr=http.getfriends(access_token);
 		//get friends
 		for(int x=0;x<arr.size();x++)
 		{
 			JSONObject json=(JSONObject)arr.get(x);
 			System.out.println(json);
 			System.out.println(json.get("username"));
 			listItems.add(json.get("username").toString());
 		}
 		//alert the user if they have no friends
 		if(listItems.isEmpty()==true)
			listItems.add("You have no friends the moment.");
 		
 		adapter=new ArrayAdapter<String>(this,
 			           android.R.layout.simple_list_item_1,
 			           listItems);
 	    setListAdapter(adapter);
 		
 	    
 		
 	}
 	
 	//returns to previous activity
 	public void finishact(View view)
 	{
 		//finishes the activity
 		finish();
 	}
 
 }
