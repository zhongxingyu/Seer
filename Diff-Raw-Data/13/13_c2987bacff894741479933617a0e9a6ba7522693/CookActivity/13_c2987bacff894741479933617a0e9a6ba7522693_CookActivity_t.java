 package com.csci5115.group2.planmymeal;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.HorizontalScrollView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ScrollView;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 public class CookActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_cook);
 		
 		Intent intent = getIntent();
		String mealName = intent.getStringExtra(HomeActivity.EXTRA_MEAL);
 		
 		//TextView mealNameTextView = (TextView) findViewById(R.id.recipeName);
 		//mealNameTextView.setText(mealme);
 		
 		
 		LinkedList<Meal> meals = GlobalData.userMeals;
 		//meals.getFirst();
 		Meal meal = GlobalData.findUserMealByName(mealName);
 		//String mealName = meal.getName();
 		List<Recipe> recipes = meal.getRecipes();
 		Recipe recipe = recipes.get(0);
 		Recipe recipessss = recipes.get(1);
 		String recipeName = recipe.getName();
 		LinkedList<Ingredient> ingredients = new LinkedList<Ingredient>();
 		
 		Ingredient egg = new Ingredient("Egg", 10.00, "test");
 		Ingredient bread = new Ingredient("Bread", 10.00, "test");
 		Ingredient bacon = new Ingredient("Bacon", 10.00, "test");
 		Ingredient potato = new Ingredient("Potato", 10.00, "test");
 		Ingredient chicken = new Ingredient("Chicken", 10.00, "test");
 		Ingredient beef = new Ingredient("Beef", 10.00, "test");
 		Ingredient tomato = new Ingredient("Tomato", 10.00, "test");
 		Ingredient pickles = new Ingredient("Pickle", 10.00, "test");
 		ingredients.add(egg);
 		ingredients.add(bread);
 		ingredients.add(bacon);
 		ingredients.add(potato);
 		ingredients.add(chicken);
 		ingredients.add(beef);
 		ingredients.add(tomato);
 		ingredients.add(pickles);
 		
 		RecipeStep one = new RecipeStep();
 		one.setInstructions("Testererer");
 		RecipeStep two = new RecipeStep();
 		two.setInstructions("Test");
 		RecipeStep three = new RecipeStep();
 		three.setInstructions("Test");
 		RecipeStep four = new RecipeStep();
 		four.setInstructions("Test");
 		RecipeStep five = new RecipeStep();
 		five.setInstructions("Test");
 		RecipeStep six = new RecipeStep();
 		six.setInstructions("Test");
 		RecipeStep seven = new RecipeStep();
 		seven.setInstructions("Test");
 		RecipeStep eight = new RecipeStep();
 		eight.setInstructions("Testing");
 		LinkedList<RecipeStep> recipeSteps = new LinkedList<RecipeStep>();
 		recipeSteps.add(one);
 		recipeSteps.add(two);
 		recipeSteps.add(three);
 		recipeSteps.add(four);
 		recipeSteps.add(five);
 		recipeSteps.add(six);
 		recipeSteps.add(seven);
 		recipeSteps.add(eight);
 		recipe.setSteps(recipeSteps);
 		recipe.setIngredients(ingredients);
 		LinkedList<Recipe> recipeList = new LinkedList<Recipe>();
 		recipeList.add(recipe);
 		//recipeList.add(recipe);
 		
 		//ListView recipeContainer = (ListView) findViewById(R.id.myListView);
 		//CookMealRecipeArrayAdapter cMRArrayAdapter = new CookMealRecipeArrayAdapter(this, recipeList);
 		 //recipeContainer.setAdapter(cMRArrayAdapter);
 		
 		//
 		//recipeContainer.setAdapter(cMRArrayAdapter);
 		//(LinkedList)recipe.getIngredients();
 		//List<RecipeStep> steps = recipe.getSteps();
 		//String[] stepsss = {"test", "More"};
 		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_meal);
 		
 		//Intent intent = getIntent();
 		//String mealName = intent.getStringExtra(HomeActivity.EXTRA_MEAL);
 
 		//LinearLayout myLayout = (LinearLayout)View.inflate(getApplicationContext(), R.id.myLinearLayout, null); //root)findViewById(R.id.myLinearLayout);
 		//LinearLayout myContainer = (LinearLayout)findViewById(R.id.cookMealRecipeHolder);
 		
 		TextView myText = (TextView)findViewById(R.id.recipeCookName);
 		myText.setText(recipeName);
 		//TextView mySecondText = (TextView)findViewById(R.id.recipeName);
 		//myText.setText("Recipe 1");
 		//mySecondText.setText("Recipe 2");
 		
 		IngredientArrayAdapter ingredientsAdapter = new IngredientArrayAdapter(this, ingredients);
 		GridView recipeIngredients = (GridView) findViewById(R.id.recipeCookIngredients);
 		recipeIngredients.setAdapter(ingredientsAdapter);
 		
 		RecipeStepsArrayAdapter recipeStepAdapter = new RecipeStepsArrayAdapter(this, recipeSteps);
 		ListView recipeStepsListView = (ListView) findViewById(R.id.recipeCookSteps);
 		recipeStepsListView.setAdapter(recipeStepAdapter);
 		
 		IngredientArrayAdapter secondAdapter = new IngredientArrayAdapter(this, ingredients);
 		GridView secondRecipe = (GridView) findViewById(R.id.recipeCookIngredients);
 		secondRecipe.setAdapter(secondAdapter);
 		
 		
 		//HorizontalScrollView hSV = (HorizontalScrollView) findViewById(R.id.myListView);
 		//hSV.addView(myText, R.layout.row_cook_recipe_container);

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
