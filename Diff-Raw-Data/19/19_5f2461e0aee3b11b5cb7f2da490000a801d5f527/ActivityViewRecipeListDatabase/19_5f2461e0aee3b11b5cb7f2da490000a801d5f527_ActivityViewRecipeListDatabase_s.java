 package com.github.cmput301w13t04.food.view;
 
 import java.util.ArrayList;
 
 import org.apache.http.entity.StringEntity;
 
 import com.github.cmput301w13t04.food.R;
 import com.github.cmput301w13t04.food.R.layout;
 import com.github.cmput301w13t04.food.R.menu;
 import com.github.cmput301w13t04.food.controller.Database;
 import com.github.cmput301w13t04.food.controller.RecipeAdapter;
 import com.github.cmput301w13t04.food.model.Ingredient;
 import com.github.cmput301w13t04.food.model.Recipe;
 import com.github.cmput301w13t04.food.model.query.QueryIngredients;
 import com.google.gson.Gson;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.support.v4.app.NavUtils;
 
 public class ActivityViewRecipeListDatabase extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view_recipe_list_database);
 		// Show the Up button in the action bar.
 		setupActionBar();
 
 		ArrayList<Recipe> recipes = null;
 
 		// Handle query by Username
 		String username = getIntent().getStringExtra("USER");
 		if (username != null) {
 			Database database = new Database();
 			recipes = database.searchRecipeByEmail(username);
 		}
 
 		ArrayList<Ingredient> ingredients = getIntent()
 				.getParcelableArrayListExtra("INGREDIENTS");
 		if(ingredients != null) {
 			Database database = new Database();
 			recipes = database.searchByIngredients(ingredients);
 		}
 
		QueryIngredients lolwut = new QueryIngredients(ingredients);
		String resultsmaybeidk = new Gson().toJson(lolwut);
		Log.d("Testing", resultsmaybeidk);

		Log.d("Ingredients Size",
				"Ingredients Size: " + String.valueOf(ingredients.size()));
		for (int i = 0; i < ingredients.size(); i++) {
			Log.d("Testing", "We found ingredient"
					+ ingredients.get(i).getName());
		}

		// TODO: Handle query by ingredient

 		// Populate Recipe List
 		ListView list = (ListView) findViewById(R.id.recipe_list);
 		list.setAdapter(new RecipeAdapter(list.getContext(),
 				R.layout.item_recipe, recipes));
 
 		list.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> arg0, View arg1,
 					int position, long id) {
 				Intent intent = new Intent(ActivityViewRecipeListDatabase.this,
 						ActivityViewRecipe.class);
 				intent.putExtra("RECIPE_ID", id);
 				startActivity(intent);
 			}
 		});
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 	}
 
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
