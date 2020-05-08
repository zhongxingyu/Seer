 package com.csci5115.group2.planmymeal;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.view.WindowManager;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.csci5115.group2.planmymeal.database.DataSourceManager;
 
 public class NewMealActivity extends Activity implements TextWatcher, OnFocusChangeListener{
 	
 	public final static String EXTRA_MEAL = "com.csci5115.group2.planmymeal.MEAL";
 	public final static String EXTRA_RECIPE = "com.csci5115.group2.planmymeal.RECIPE";
 	public final static String BUNDLE_SHOWMEALS = "com.csci5115.group2.planmymeal.BUNDLE_SHOWMEALS";
 	public final static String BUNDLE_SHOWRECIPES = "com.csci5115.group2.planmymeal.BUNDLE_SHOWRECIPES";
 	
 	public Meal meal;
 	public View view;
 	public Context context;
 	public LinearLayout tagContainer;
 	private DataSourceManager datasource;
 	private RecipeArrayAdapter recipeAdapter; 
 	private Typeface fontAwesome;
 	private List<Tag> addedTags = new ArrayList<Tag>();
 	private List<Recipe> addedRecipes = new ArrayList<Recipe>();
 	private	Meal newMeal =null;
 
 	@Override
 	protected void onCreate(final Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 		setContentView(R.layout.activity_new_meal);
         setTitle("New Meal");
 		context = this;
 		
 		// Database Creation
 		datasource = new DataSourceManager(this);
 		datasource.open();
 		
 		this.fontAwesome = Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf" );
 				
 		final EditText newMealNameView = (EditText) findViewById(R.id.new_meal_mealName);
 		newMealNameView.setHint("New Meal Name");
 		
 		final EditText newMealTimeView = (EditText) findViewById(R.id.new_meal_mealTime);
 		newMealTimeView.setHint("Meal Time (in minutes)");
 		
 		final EditText newMealDescriptionView = (EditText) findViewById(R.id.new_meal_mealDescription);
 		newMealDescriptionView.setHint("Meal Decription");
 		        
 		// Set up recipes in meal list
 		//TODO:Populate all 						
 		RecipeDetailArrayAdapter mealRecipesAdapter = new RecipeDetailArrayAdapter(this, addedRecipes);
 		ListView mealListView = (ListView) findViewById(R.id.new_meal_recipes_in_meal);
 		mealListView.setAdapter(mealRecipesAdapter);
 
 		// Set up all user recipes
 		//TODO:Populate the recipe list
 		List<Recipe> allUserRecipesList = datasource.getAllRecipes();
 				
 		recipeAdapter = new RecipeArrayAdapter(this, allUserRecipesList, mealRecipesAdapter);
 		ListView allUserListView = (ListView) findViewById(R.id.new_meal_all_recipes);
 		allUserListView.setAdapter(recipeAdapter);
 				
 		// Register text listener
         List<Tag> allTags = datasource.getAllTags();
         List<Recipe> allRecipes = datasource.getAllUserRecipes();
         
         String[] autocompleteStrings = new String[allTags.size() + allRecipes.size()];
         int i = 0;
         int j = 0;
         for (j = 0; j < allRecipes.size(); i++, j++) {
         	autocompleteStrings[i] = allRecipes.get(j).getName();
         }
         for (j = 0; j < allTags.size(); i++, j++) {
         	autocompleteStrings[i] = allTags.get(j).getName();
         }
         
 		EditText search = (EditText) findViewById(R.id.new_meal_search);
 		search.addTextChangedListener(this);
 		search.setOnFocusChangeListener(this);
 				
 		// Set up tags
 		tagContainer = (LinearLayout)findViewById(R.id.new_meal_tag_container);
 		
 		//Get tags for id
 	    
 		for(final Tag tag : addedTags)
 		{			    
 			final View tagHolder = getLayoutInflater().inflate(R.layout.tag, null);
 			
 		    TextView tagName = (TextView) tagHolder.findViewById(R.id.tag_name);
 			tagName.setText(tag.getName());
 	    
 			Button tagDelete = (Button) tagHolder.findViewById(R.id.tag_button_delete);
 			tagDelete.setTypeface(fontAwesome);
 			
 			tagDelete.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					tagHolder.setVisibility(View.GONE);
 					addedTags.remove(tag);
 				}
 			});
 
 			tagContainer.addView(tagHolder);
 		}
 		
 		Button addTagButton = (Button) findViewById(R.id.addTagButton);
 		addTagButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v)
 			{
 				final View tagHolder = getLayoutInflater().inflate(R.layout.tag, null);
 				
 				EditText newTagText = (EditText) findViewById(R.id.new_meal_newTagName);
 				String tagText = newTagText.getText().toString();
 				newTagText.setText("");
 				newTagText.setHint("New Tag Name");
 				
 				//Add tag to meal
 				final Tag tag = datasource.createTag(tagText);
 				addedTags.add(tag);
 				
 			    TextView tagName = (TextView) tagHolder.findViewById(R.id.tag_name);
 				tagName.setText(tag.getName());
 		    
 				Button tagDelete = (Button) tagHolder.findViewById(R.id.tag_button_delete);
 				tagDelete.setTypeface(fontAwesome);
 				
 				tagDelete.setOnClickListener(new View.OnClickListener() {
 					
 					@Override
 					public void onClick(View v) {
 						tagHolder.setVisibility(View.GONE);
 						addedTags.remove(tag);
 					}
 				});
 
 				tagContainer.addView(tagHolder);
 			}
 		}
 		);
 		//TODO: Create New Meal With all stuff
 		Button saveButton = (Button) findViewById(R.id.new_meal_save_button);
 		saveButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v)
 			{
 				//TODO: save new name of meal
 				String mealNameText = newMealNameView.getText().toString();
 				String mealTimeText = newMealTimeView.getText().toString();
 				String mealDescriptionText = newMealDescriptionView.getText().toString();
				Double mealTimeDouble = Double.parseDouble(mealTimeText);
 					
 				if(newMeal == null)
 				{
 					newMeal = datasource.createNewUserMeal(mealNameText, mealTimeDouble, mealDescriptionText);
 					//notifyDataSetChanged:not sure how to do this for prev page
 				}
 				else{
 					newMeal = datasource.updateMeal(newMeal.getId(), mealNameText, mealTimeDouble, mealDescriptionText);
 				}
 				
 				//TODO: save added Tags
 				for(Tag tag : addedTags)
 				{
 					if(!datasource.getMealTags(newMeal.getId()).contains(tag))
 					{
 						newMeal.tags.add(new Tag(tag.getName()));
 						datasource.addTagToMeal(tag.getName(), newMeal.getId());
 					}
 				}
 				//TODO: save added Recipes
 				for(Recipe recipe : addedRecipes)
 				{
 					if(!datasource.getMealRecipes(newMeal.getId()).contains(recipe))
 					{
 						newMeal.recipes.add(recipe);
 						datasource.addMealRecipe(newMeal.getId(), recipe.getId());
 					}
 				}
 				AlertDialog.Builder builder = new AlertDialog.Builder(context);
 				builder.setTitle("Meal Saved");
 				builder.setMessage("Meal: " + newMeal.getName() +" Saved");
 				builder.setNeutralButton("Okay",  new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {   
 						//add meal
 					}
 				});
 				AlertDialog dialog = builder.create();
 				dialog.show();
 			}
 		}
 		);
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.\
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
 
 	@Override
 	public void afterTextChanged(Editable arg0) {
 	}
 
 	@Override
 	public void beforeTextChanged(CharSequence s, int start, int count,
 			int after) {
 	}
 
 	@Override
 	public void onTextChanged(CharSequence s, int start, int before, int count) {
 		recipeAdapter.getFilter().filter(s);
 	}
 
 	@Override
 	public void onFocusChange(View v, boolean hasFocus) {
 	}
 	
 }
