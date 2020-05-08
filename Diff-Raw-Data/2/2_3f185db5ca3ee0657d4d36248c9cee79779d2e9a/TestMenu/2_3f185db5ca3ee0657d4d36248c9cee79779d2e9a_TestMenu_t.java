 package com.indivisible.tortidy;
 
 import android.os.Bundle;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class TestMenu extends ListActivity {
 
 	// file names of the activities to offer (remember to add to AndroidManifest!)
 	String[] tests = { 
 		"MainActivity",
 		"test.TestTorHandler"
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setListAdapter(new ArrayAdapter<String>(
 					TestMenu.this, android.R.layout.simple_list_item_1, tests));
 		Log.i("onCreate()", "Starting app...");
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		
 		Class<?> ourClass = null;
 		try {
 			String nextClass = tests[position];
			ourClass = Class.forName("com.indivisible.tortidy." + nextClass);
 			Intent ourIntent = new Intent(TestMenu.this, ourClass);
 			startActivity(ourIntent);
 		} catch (ClassNotFoundException e) {
 			Toast toast = Toast.makeText(TestMenu.this, "Not a suitable class", Toast.LENGTH_SHORT);
 			toast.show();
 			e.printStackTrace();
 		}
 	}
 }
