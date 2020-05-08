 package com.indivisible.tortidy.test;
 import android.app.*;
 import com.indivisible.tortidy.database.*;
 import com.indivisible.tortidy.*;
 import android.os.*;
 import java.util.*;
 import android.widget.*;
 import android.view.*;
 
 public class TestDb extends ListActivity
 {
 	private LabelsDataSource labels;
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
 
 		labels = new LabelsDataSource(this.getApplicationContext());
 		labels.openWriteable();
 
 		List<Label> values = labels.getAllLabels();
 		ArrayAdapter<Label> adapter = new ArrayAdapter<Label>(
 				this.getApplicationContext(),
 				android.R.layout.simple_list_item_1,
 				values);
 		setListAdapter(adapter);
 	}
 	
 	public void onClick(View view) {
 		@SuppressWarnings("unchecked")
 			ArrayAdapter<Label> adapter = (ArrayAdapter<Label>) getListAdapter();
 		Label label = null;
 		switch (view.getId()) {
 			
 			case R.id.bAdd:
 				String[] labelTitles = new String[] { "Cool", "Very nice", "Hate it" };
 				int nextInt = new Random().nextInt(3);
 				// Save the new comment to the database
 				label = labels.createLabel(labelTitles[nextInt], true);
 				adapter.add(label);
 				break;
 				
 			case R.id.bDelete:
 				if (getListAdapter().getCount() > 0) {
 					label = (Label) getListAdapter().getItem(0);
 					labels.deleteLabel(label);
 					adapter.remove(label);
 				}
 				break;
 		}
 		adapter.notifyDataSetChanged();
 	}
 	
 	@Override
 	protected void onResume() {
 		labels.openWriteable();
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		labels.close();
 		super.onPause();
 	}
 }
