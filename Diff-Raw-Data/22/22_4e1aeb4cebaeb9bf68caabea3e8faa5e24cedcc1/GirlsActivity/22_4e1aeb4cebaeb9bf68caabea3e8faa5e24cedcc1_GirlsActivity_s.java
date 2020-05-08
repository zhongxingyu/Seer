 package com.vorsk.crossfitr;
 
 import com.vorsk.crossfitr.models.*;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Toast;
 
 public class GirlsActivity extends ListActivity
 {
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		
 		WorkoutModel model = new WorkoutModel(this);
 
 		//Access the database and retrieve all girl workouts
 		model.open();	
 		WorkoutRow[] results = model.getAllByType(WorkoutModel.TYPE_GIRL);
 		model.close();
 		
 		String[] results2 = new String[] {"GIRLS"};
 		
 		/*
 		ArrayAdapter<Row> adapter = new ArrayAdapter<Row>(this,
 				android.R.layout.simple_list_item_1, android.R.id.text1, results);
 		*/
 		
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_1, android.R.id.text1, results2);
 		
 		setListAdapter(adapter);
 	}
 	
 	public void onListItemClick(ListView list, View view, int position, long id) 
 	{
 		String item = (String) getListAdapter().getItem(position);
 		Toast.makeText(this, item + " selected", Toast.LENGTH_SHORT).show();
 	}
 	
 }
