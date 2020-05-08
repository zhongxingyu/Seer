 package com.csci5115.group2.planmymeal;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class CommunityCookbookActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_community_cookbook);
 		
 		// Set up list view
 		// TODO Use a database call to populate this list view
 		Meal[] mealArray = new Meal[10];
 		
 		for (int i = 0; i < 10; i++) {
 			if (i % 2 == 0) {
				mealArray[i] = new Meal("Beef Stew", "1:18:00");
 			} else {
				mealArray[i] = new Meal("Olivia's Favorite", "1:40:22");
 			}
 		}
 
 		CommunityCookbookArrayAdapter communityCookbookAdapter = new CommunityCookbookArrayAdapter(this, mealArray);
 		ListView listView = (ListView) findViewById(R.id.communityCookbook_mealListView);
 		listView.setAdapter(communityCookbookAdapter);
 		listView.setOnItemClickListener(new OnItemClickListener() {
 			  @Override
 			  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				  Meal meal = (Meal) parent.getItemAtPosition(position);
 			    Toast.makeText(getApplicationContext(),
 			      meal.getName(), Toast.LENGTH_LONG)
 			      .show();
 			  }
 			}); 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.community_cookbook, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 	        case R.id.action_settings:
 	    		Intent intent = new Intent(this, SettingsActivity.class);
 	    		startActivity(intent);
 	            return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
 
 }
