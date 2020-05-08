 package com.vorsk.crossfitr;
 
 import com.vorsk.crossfitr.models.WorkoutModel;
 import com.vorsk.crossfitr.models.WorkoutRow;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.content.Intent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class HeroesActivity extends ListActivity 
 {
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		
 		WorkoutModel model = new WorkoutModel(this);
 
 		//Access the database and retrieve all heroes workouts
 		model.open();	
 		WorkoutRow[] results = model.getAllByType(WorkoutModel.TYPE_HERO);
 		model.close();
 		
		ArrayAdapter<WorkoutRow> adapter = new ArrayAdapter<WorkoutRow>(this,
 				android.R.layout.simple_list_item_1, android.R.id.text1, results);
 
 		setListAdapter(adapter);
 	}
 
 	public void onListItemClick(ListView list, View view, int position, long id) 
 	{
 		String item = (String) getListAdapter().getItem(position);
 		Toast.makeText(this, item + " selected", Toast.LENGTH_SHORT).show();
 	}
 }
