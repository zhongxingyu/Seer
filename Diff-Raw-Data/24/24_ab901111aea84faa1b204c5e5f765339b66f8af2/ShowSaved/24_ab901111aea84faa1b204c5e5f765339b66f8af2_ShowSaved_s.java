 package com.werebug.randomsequencegenerator;
 
 import java.util.ArrayList;
 import java.util.Map;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class ShowSaved extends Activity implements OnItemClickListener {
 	
 	// Layout
 	private ListView add_here;
 	
 	// String Array
 	private ArrayList<String> string = new ArrayList<String>();
 	
 	// SharedPreferences object and editor
 	private SharedPreferences sp;
 	
 	// Array adapter for ListView add_here
 	private ArrayAdapter<String> saved;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		this.setContentView(R.layout.activity_show_saved);
 		
 		this.add_here = (ListView)findViewById(R.id.saved_list);
 		
 		// Loading shared preferences and editor
 		this.sp = this.getSharedPreferences("saved_sequences", MODE_PRIVATE);
 		
 		// Retrieving all pairs in SharedPreferences sp
 		Map<String, ?> kv = sp.getAll();
 		
 		// Iterating through shared preferences
 		for(Map.Entry<String, ?> entry : kv.entrySet()) {		
			string.add(entry.getKey());
 		}
 		
		// Creating the Adapter with all strings in an array
		this.saved = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, string);
		
		// Setting Adapter to ListView
 		this.add_here.setAdapter(this.saved);
 		
 		// Setting Listener for ListView add_here
 		this.add_here.setOnItemClickListener(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_show_saved, menu);
 		return true;
 	}
 	
 	public void onItemClick (AdapterView<?> adapter, View view, int pos, long id) {
 		return;
 	}
 	
 	public boolean onOptionsItemSelected (MenuItem mi){
 		switch (mi.getItemId()){
 			case R.id.menu_delete_all:
 				// Erasing entries from SharedPreferences
 				Editor ed = (Editor)this.sp.edit();
 				ed.clear();
 				ed.commit();
 				
 				// Erasing entries from ListView
 				this.saved.clear();
				this.saved.notifyDataSetChanged();
 				return true;
 				
 			default:
 				return super.onOptionsItemSelected(mi);
 		}
 	}
 
 }
