 package com.csci5115.group2.planmymeal;
 
 import java.util.List;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 
 import com.csci5115.group2.planmymeal.database.DataSourceManager;
 
 // Samantha Oyen: This used to implement OnClickListener... had to take out.
 public class HomeActivity extends FragmentActivity implements CookableListFragment.Callbacks, TextWatcher, OnFocusChangeListener {
 	
 	public final static String EXTRA_MEAL = "com.csci5115.group2.planmymeal.MEAL";
 	public final static String BUNDLE_SHOWMEALS = "com.csci5115.group2.planmymeal.BUNDLE_SHOWMEALS";
 	public final static String BUNDLE_SHOWRECIPES = "com.csci5115.group2.planmymeal.BUNDLE_SHOWRECIPES";
 	
 	public final static String TAG = "HomeActivity";
 	
 	// Databases
 	private DataSourceManager datasource;
 	
 	private Boolean showMeals;
 	private Boolean showRecipes;
 	private ArrayAdapter<Cookable> adapter;
 	
 	private static LinearLayout homeColumn0;
 	private static LinearLayout homeColumn1;
 	private static LinearLayout homeColumn2;
 	
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_home);
        setTitle("My meals and recipes");
         
         // Database Creation
         datasource = new DataSourceManager(this);
         datasource.open();
         
         showMeals = ((CheckBox) findViewById(R.id.home_checkBoxMeals)).isChecked();
         showRecipes = ((CheckBox) findViewById(R.id.home_checkBoxRecipes)).isChecked();
         
         // List items should be given the 'activated' state when touched.
         CookableListFragment fragment = (CookableListFragment) getSupportFragmentManager().findFragmentById(
  				R.id.home_cookable_list);
         fragment.setActivateOnItemClick(true);
         adapter = (ArrayAdapter<Cookable>) fragment.getListAdapter();
         
         // Register text listener
         List<Tag> tags = datasource.getAllTags();
         List<Meal> meals = datasource.getAllMeals();
         List<Recipe> recipes = datasource.getAllRecipes();
         
         String[] autocompleteStrings = new String[tags.size() + meals.size() + recipes.size()];
         int i = 0;
         int j = 0;
         for (j = 0; j < meals.size(); i++, j++) {
         	autocompleteStrings[i] = meals.get(j).getName();
         }
         for (j = 0; j < recipes.size(); i++, j++) {
         	autocompleteStrings[i] = recipes.get(j).getName();
         }
         for (j = 0; j < tags.size(); i++, j++) {
         	autocompleteStrings[i] = tags.get(j).getName();
         }
         
 		EditText search = (EditText) findViewById(R.id.home_search);
 		search.addTextChangedListener(this);
 		search.setOnFocusChangeListener(this);
 		
 		homeColumn0 = (LinearLayout) findViewById(R.id.home_column_0);
 		homeColumn1 = (LinearLayout) findViewById(R.id.home_column_1);
 		homeColumn2 = (LinearLayout) findViewById(R.id.home_column_2);
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		
 		datasource.close();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.home, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 		Intent intent;
 	    switch (item.getItemId()) {
 	        case R.id.action_community_cookbook:
 	    		intent = new Intent(this, CommunityCookbookActivity.class);
 	    		startActivity(intent);
 	            return true;
 	        case R.id.action_new_meal:
 	    		intent = new Intent(this, EditMealActivity.class);
 	    		startActivity(intent);
 	            return true;
 	        case R.id.action_new_recipe:
 	    		intent = new Intent(this, EditMealActivity.class);
 	    		startActivity(intent);
 	            return true;
 	        case R.id.action_settings:
 	    		intent = new Intent(this, SettingsActivity.class);
 	    		startActivity(intent);
 	            return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	@Override
 	public void onItemSelected(String type, long id) {
 		
 		// No matter which type of item is selected, we want to split the view
 		// evenly between col0 and col1.
 		showColumns(2);
 		
 		if (type == "Meal") {
 			Log.v(HomeActivity.TAG, "Meal selected");
 			Bundle arguments = new Bundle();
 			arguments.putLong(MealDetailFragment.ARG_ITEM_ID, id);
 			MealDetailFragment fragment = new MealDetailFragment();
 			fragment.setArguments(arguments);
 			getSupportFragmentManager().beginTransaction()
 					.replace(R.id.home_col1_container, fragment).commit();
 		} else if (type == "Recipe") {
 			Log.v(HomeActivity.TAG, "Recipe selected");
 			Bundle arguments = new Bundle();
 			arguments.putLong(RecipeDetailFragment.ARG_ITEM_ID, id);
 			RecipeDetailFragment fragment = new RecipeDetailFragment();
 			fragment.setArguments(arguments);
 			getSupportFragmentManager().beginTransaction()
 					.replace(R.id.home_col1_container, fragment).commit();
 		}
 	}
 	
 	 @Override
 	  protected void onResume() {
 	    datasource.open();
 	    super.onResume();
 	  }
 
 	  @Override
 	  protected void onPause() {
 		datasource.close();
 	    super.onPause();
 	  }
 	  
 	  public static void showColumns(int numberOfColumns) {
 		  
 		  if (homeColumn0 != null && homeColumn1 != null && homeColumn2 != null) {
 			  LinearLayout.LayoutParams params0;
 			  LinearLayout.LayoutParams params1;
 			  LinearLayout.LayoutParams params2;
 			  
 			  switch (numberOfColumns) {
 		        case 1:
 		        	params0 = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 6);
 		        	params1 = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0);
 		        	params2 = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0);
 		  		  	homeColumn0.setLayoutParams(params0);
 		  		  	homeColumn1.setLayoutParams(params1);
		  		  	homeColumn1.setVisibility(View.GONE);
 		  		  	homeColumn2.setLayoutParams(params2);
		  		  	homeColumn2.setVisibility(View.GONE);
 		            break;
 		        case 2:
 		        	params0 = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 3);
 		        	params1 = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 3);
 		        	params2 = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0);
 		  		  	homeColumn0.setLayoutParams(params0);
 		  		  	homeColumn1.setLayoutParams(params1);
		  		  	homeColumn1.setVisibility(View.VISIBLE);
 		  		  	homeColumn2.setLayoutParams(params2);
		  		  	homeColumn2.setVisibility(View.GONE);
 		        	break;
 		        case 3:
 		        	params0 = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2);
 		        	params1 = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2);
 		        	params2 = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2);
 		  		  	homeColumn0.setLayoutParams(params0);
 		  		  	homeColumn1.setLayoutParams(params1);
		  		  	homeColumn1.setVisibility(View.VISIBLE);
 		  		  	homeColumn2.setLayoutParams(params2);
		  		  	homeColumn2.setVisibility(View.VISIBLE);
 		        	break;
 		        default:
 		            break;
 			  }
 		  }
 	  }
 
 	@Override
 	public void afterTextChanged(Editable arg0) {
 	}
 
 	@Override
 	public void beforeTextChanged(CharSequence s, int start, int count,
 			int after) {
 	}
 
 	@Override
 	public void onTextChanged(CharSequence s, int start, int before, int count) {
 		adapter.getFilter().filter(s);
 	}
 
 	@Override
 	public void onFocusChange(View v, boolean hasFocus) {
 		switch(v.getId()) {
 		case R.id.home_search:
 			if (hasFocus) {
 				showColumns(1);
 			}
 			break;
 		}
 	}
 }
