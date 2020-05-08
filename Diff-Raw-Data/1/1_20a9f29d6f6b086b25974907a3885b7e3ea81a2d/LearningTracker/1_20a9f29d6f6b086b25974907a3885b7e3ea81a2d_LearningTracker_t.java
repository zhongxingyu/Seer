 package com.magloire.learningTracker;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 //import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.CursorAdapter;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 
 public class LearningTracker extends ListActivity {
 
 	public final static String ROW_ID = "row_id";
 	private ListView goalsListView;
 	private CursorAdapter goalsAdapter;
 	
 	
 	
     
	@SuppressWarnings("deprecation")
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         goalsListView = getListView();
         goalsListView.setOnItemClickListener(viewGoalListener);
         
         //map each goal's name to a textView in ListView layout
         String[] from = new String[] {"name"};
         int[] to = new int[] {R.id.goalTextView};
         goalsAdapter = new SimpleCursorAdapter(
         		LearningTracker.this, R.layout.goal_list_item_view, null, from, to);
         setListAdapter(goalsAdapter);
     }
 
 	@Override
 	protected void onResume(){
 		super.onResume();
 		new GetGoalsTask().execute((Object[])null);
 	}
 	
 	@Override
 	protected void onStop(){
 		Cursor cursor = goalsAdapter.getCursor();
 		
 		if(cursor != null)
 			cursor.deactivate();
 		
 		goalsAdapter.changeCursor(null);
 		super.onStop();
 	}
 	
 	private class GetGoalsTask extends AsyncTask<Object, Object, Cursor>{
 		
 		DatabaseConnector dbConnector = new DatabaseConnector(LearningTracker.this);
 		
 		@Override
 		protected Cursor doInBackground(Object... params){
 			dbConnector.open();
 			return dbConnector.getAllGoals();
 		}
 		
 		// use the cursor returned
 		@Override
 		protected void onPostExecute(Cursor result){
 			goalsAdapter.changeCursor(result);
 			dbConnector.close();
 		}
 	}// end GetGoalsTask class
 	
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
     	getMenuInflater().inflate(R.menu.learning_tracker, menu);
         return true;
     }// end onCreateOptionsMenu
      
     //Action to run when a menu item is selected.
     //Adding a new goal is the only option.
     @Override
     public boolean onOptionsItemSelected(MenuItem item){
     	Intent addNewGoal =
     			new Intent(LearningTracker.this, AddEditGoal.class);
     	startActivity(addNewGoal);
     	return super.onOptionsItemSelected(item);
     }// end onOptionsIemSelected
 	
     
     //The callback to execute when an item on the list is clicked on
 	OnItemClickListener viewGoalListener = new OnItemClickListener() {
 		@Override
 		public  void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3){
 			
 			Intent viewGoal = new Intent(LearningTracker.this, ViewGoal.class);
 			viewGoal.putExtra(ROW_ID, arg3);
 			startActivity(viewGoal);
 		}
 	};
 	
 	
 
     
 }
