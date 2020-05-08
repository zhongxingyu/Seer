 package com.jamsesh;
 
 import java.util.ArrayList;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class JamList extends ListActivity { 
 	private String[] jamArray;
 
 	@Override 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState); 
 		jamArray = new String[3];
 		jamArray[0] = "Jam One";
 		jamArray[1] = "Jam Two";
 		jamArray[2] = "Jam Three";
 		
 		setListAdapter(new ArrayAdapter<String>(this, 
 				android.R.layout.simple_list_item_1,
 				new ArrayList())); 
 		
 		new AddStringTask().execute();
 	}
 	
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		Intent message = new Intent(JamList.this, JamInfo.class);
 		message.putExtra("com.jamsesh.title", jamArray[position]);
 		message.putExtra("com.jamsesh.parent", 1);
         startActivity(message);
 	}
 
 	class AddStringTask extends AsyncTask<Void, String, Void> { 
 		@Override 
 		protected Void doInBackground(Void... unused) {
 			for (String s:jamArray) {
				publishProgress(s);				
 			}
 			return(null);
 		}
 		
 		@Override 
 		protected void onProgressUpdate(String... item) {
 			((ArrayAdapter)getListAdapter()).add(item[0]);
 		}
 	}
 }	
