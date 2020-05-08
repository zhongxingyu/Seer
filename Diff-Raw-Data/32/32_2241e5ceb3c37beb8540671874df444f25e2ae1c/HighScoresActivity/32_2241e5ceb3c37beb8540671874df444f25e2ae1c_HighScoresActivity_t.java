 package edu.asu.tankgame;
 
 //import android.app.Activity;
 import java.util.List;
 import java.util.Random;
 
 import android.app.ListActivity;
 //import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 
 public class HighScoresActivity extends ListActivity {
 
 	private CommentsDataSource datasource;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		//setContentView(R.layout.activity_high_scores);
 		// Show the Up button in the action bar.
 		//getActionBar().setDisplayHomeAsUpEnabled(true);
 		
 		//Intent intent = getIntent();
 		//String message = intent.getStringExtra(MainMenuActivity.EXTRA_MESSAGE);	// get message by "key" name
 		
 		setContentView(R.layout.activity_high_scores);
 		
 		datasource = new CommentsDataSource(this);
 		datasource.open();
 		
 		List<Comment> values = datasource.getAllComments();
 		
 		// Use the SimpleCursorAdapter to show the elements in a ListView
		ArrayAdapter<Comment> adapter = new ArrayAdapter<Comment>(this, android.R.layout.simple_list_item_1, values); // android.R.layout.simple_list_item_1 is a common default
 		setListAdapter(adapter);
 		
 	}
 	
 	// Called via onClick attribute for the buttons in activity_high_scores.xml
 	public void onClick(View view)
 	{
 		@SuppressWarnings("unchecked")
 		ArrayAdapter<Comment> adapter = (ArrayAdapter<Comment>) getListAdapter();
 		Comment comment = null;
		/*
 		switch (view.getId())
 		{
 			case R.id.add:
 				String[] comments = new String[] { "Cool", "Very nice", "Hate it" };
 				int nextInt = new Random().nextInt(3);
 				// save the new comment to the database
 				comment = datasource.createComment(comments[nextInt]);
 				adapter.add(comment);
 				break;
 			case R.id.delete:
 				if (getListAdapter().getCount() > 0)
 				{
 					comment = (Comment) getListAdapter().getItem(0);
 					datasource.deleteComment(comment);
 					adapter.remove(comment);
 				}
 				break;
 		}
		*/
		if (view.getId() == R.id.add) {
			String[] comments = new String[] { "Cool", "Very nice", "Hate it" };
			int nextInt = new Random().nextInt(3);
			// save the new comment to the database
			comment = datasource.createComment(comments[nextInt]);
			adapter.add(comment);
		} else if (view.getId() == R.id.delete) {
			if (getListAdapter().getCount() > 0)
			{
				comment = (Comment) getListAdapter().getItem(0);
				datasource.deleteComment(comment);
				adapter.remove(comment);
			}
		}
 		adapter.notifyDataSetChanged();
 	}
 	
 	@Override
 	protected void onResume()
 	{
 		datasource.open();
 		super.onResume();
 	}
 	
 	@Override
 	protected void onPause()
 	{
 		datasource.close();
 		super.onPause();
 	}
 
 	/*@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_high_scores, menu);
 		return true;
 	}*/
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 }
