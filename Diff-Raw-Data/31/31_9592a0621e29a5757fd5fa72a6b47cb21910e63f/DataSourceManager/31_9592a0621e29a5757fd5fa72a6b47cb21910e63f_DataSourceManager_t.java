 package com.csci5115.group2.planmymeal.database;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.csci5115.group2.planmymeal.Ingredient;
 import com.csci5115.group2.planmymeal.Meal;
 import com.csci5115.group2.planmymeal.Recipe;
 import com.csci5115.group2.planmymeal.RecipeStep;
 import com.csci5115.group2.planmymeal.Tag;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 public class DataSourceManager
 {
 	//Database fields
 	  private SQLiteDatabase database;
 	  private DatabaseHelper dbHelper;
 	  
 	// MEAL DATABASE 
 	  public static final String TABLE_MEAL = "meal";
 	  public static final String COLUMN_ID = "_id";
 	  public static final String COLUMN_NAME = "name";
 	  public static final String COLUMN_TIME = "time";
 	  public static final String COLUMN_DESCRIPTION = "description";
 	  private static String[] allMealColumns = { COLUMN_ID, COLUMN_NAME, COLUMN_TIME, COLUMN_DESCRIPTION };
 	  private static final String MEAL_DATABASE_CREATE = "create table " 
 	      + TABLE_MEAL
 	      + "(" 
 	      + COLUMN_ID + " integer primary key autoincrement, " 
 	      + COLUMN_NAME + " text not null, " 
 	      + COLUMN_TIME + " real not null," 
 	      + COLUMN_DESCRIPTION
 	      + " text" 
 	      + ");";
 	  
 	  	//TAG DATABASE
 		  public static final String TABLE_TAG = "tag";
 		  private static String[] allTagColumns = { COLUMN_ID, COLUMN_NAME };
 		// Database creation
 		  private static final String TAG_DATABASE_CREATE = "create table " 
 		      + TABLE_TAG
 		      + "(" 
 		      + COLUMN_ID + " integer primary key autoincrement, " 
 		      + COLUMN_NAME + " text not null" 
 		      + ");";
 		  
 		// MEAL TAG REL DATABASE
 		  public static final String TABLE_MEAL_TAG_REL = "mealTagRel";
 		  public static final String COLUMN_MEAL_ID = "mealId";
 		  public static final String COLUMN_TAG_ID = "tagId";
 		  private static String[] allMealTagColumns = { COLUMN_ID, COLUMN_MEAL_ID, COLUMN_TAG_ID};
 		// Database creation 
 		  private static final String MEAL_TAG_DATABASE_CREATE = "create table " 
 		      + TABLE_MEAL_TAG_REL
 		      + "(" 
 		      + COLUMN_ID + " integer primary key autoincrement, " 
 		      + COLUMN_MEAL_ID + " integer not null, " 
 		      + COLUMN_TAG_ID + " integer not null"
 		      + ");";
 		  
 		  //RECIPE DATABASE
 		  public static final String TABLE_RECIPE = "recipe";
 		  public static final String COLUMN_RECIPE_NUM_SERVINGS = "numServings";
 		  private static String[] allRecipeColumns = { COLUMN_ID, COLUMN_NAME, COLUMN_TIME, COLUMN_DESCRIPTION };
 		// Database creation 
 		  private static final String RECIPE_DATABASE_CREATE = "create table " 
 		      + TABLE_RECIPE
 		      + "(" 
 		      + COLUMN_ID + " integer primary key autoincrement, " 
 		      + COLUMN_NAME + " text not null, " 
 		      + COLUMN_TIME + " real not null," 
 		      + COLUMN_DESCRIPTION
 		      + " text," +
 		      COLUMN_RECIPE_NUM_SERVINGS + " integer not null"
 		      + ");";
 		  
 		// MEAL TAG REL DATABASE
 		  public static final String TABLE_MEAL_RECIPE_REL = "mealRecipeRel";
 		  public static final String COLUMN_RECIPE_ID = "recipeId";
 		  private static String[] allMealRecipeColumns = { COLUMN_ID, COLUMN_MEAL_ID, COLUMN_RECIPE_ID};
 		// Database creation 
 		  private static final String MEAL_RECIPE_DATABASE_CREATE = "create table " 
 		      + TABLE_MEAL_RECIPE_REL
 		      + "(" 
 		      + COLUMN_ID + " integer primary key autoincrement, " 
 		      + COLUMN_MEAL_ID + " integer not null, " 
 		      + COLUMN_RECIPE_ID + " integer not null"
 		      + ");";
 
 	  public DataSourceManager(Context context)
 	  {
 		  dbHelper = new DatabaseHelper(context);
 	  }
 	  
 	  public void open() throws SQLException {
 		    database = dbHelper.getWritableDatabase();
 	  }
 
 	  	public void close() {
 	  		dbHelper.close();
 	  	}
 	  	
 	  	// MEAL DATA ACCESS METHODS
 	  	public Meal createMeal(String name, double time, String description) {
 	  	    ContentValues values = new ContentValues();
 	  	    values.put(COLUMN_NAME, name);
 	  	  values.put(COLUMN_TIME, time);
 	  	values.put(COLUMN_DESCRIPTION, description);
 	  	    long insertId = database.insert(TABLE_MEAL, null,
 	  	        values);
 	  	    Cursor cursor = database.query(TABLE_MEAL,
 	  	    		allMealColumns, COLUMN_ID + " = " + insertId, null,
 	  	        null, null, null);
 	  	    cursor.moveToFirst();
 	  	    Meal newMeal = cursorToMeal(cursor);
 	  	    cursor.close();
 	  	    return newMeal;
 	  	  }
 
 	  	  public void deleteMeal(Meal meal) {
 	  	    long id = meal.getId();
 	  	    System.out.println("Meal deleted with id: " + id);
 	  	    database.delete(TABLE_MEAL, COLUMN_ID
 	  	        + " = " + id, null);
 	  	  }
 
 	  	  public List<Meal> getAllMeals() {
 	  	    List<Meal> meals = new ArrayList<Meal>();
 
 	  	    Cursor cursor = database.query(TABLE_MEAL,
 	  	    		allMealColumns, null, null, null, null, null);
 
 	  	    cursor.moveToFirst();
 	  	    while (!cursor.isAfterLast()) {
 	  	      Meal meal = cursorToMeal(cursor);
 	  	      meals.add(meal);
 	  	      cursor.moveToNext();
 	  	    }
 	  	    // make sure to close the cursor
 	  	    cursor.close();
 	  	    return meals;
 	  	  }
 	  	  
 	  	  public Meal getMealById(long mealId)
 	  	  {
 	  		Meal meal = new Meal();
 
 	  		Cursor cursor = database.query(TABLE_MEAL,
 	  				allMealColumns, COLUMN_ID + " = " + mealId, null,
 		  	        null, null, null);
 
 	  	    cursor.moveToFirst();
 	  	    meal = cursorToMeal(cursor);
 	  	   
 	  	    // make sure to close the cursor
 	  	    cursor.close();
 	  	    return meal;
 	  	  }
 	  	  private Meal cursorToMeal(Cursor cursor) {
 	  	    Meal meal = new Meal();
 	  	    meal.setId(cursor.getLong(0));
 	  	    meal.setName(cursor.getString(1));
 	  	    meal.setTime(cursor.getLong(2));
 	  	    meal.setDescription(cursor.getString(3));
 	  	    meal.setType("Meal");
 	  	    meal.setRecipes(new LinkedList<Recipe>());
 	  	    meal.setTags(new LinkedList<Tag>());
 	  	    return meal;
 	  	  }
 	  	  
 	  	  // TAG DATA ACCESS METHODS
 	  	// Data Access Methods
 		  	public Tag createTag(String name) {
 		  	    ContentValues values = new ContentValues();
 		  	    values.put(COLUMN_NAME, name);
 		  	    long insertId = database.insert(TABLE_TAG, null,
 		  	        values);
 		  	    Cursor cursor = database.query(TABLE_TAG,
 		  	        allTagColumns, COLUMN_ID + " = " + insertId, null,
 		  	        null, null, null);
 		  	    cursor.moveToFirst();
 		  	    Tag newTag = cursorToTag(cursor);
 		  	    cursor.close();
 		  	    return newTag;
 		  	  }
 
 		  	  public void deleteTag(Tag tag) {
 		  	    long id = tag.getId();
 		  	    System.out.println("Tag deleted with id: " + id);
 		  	    database.delete(TABLE_TAG, COLUMN_ID
 		  	        + " = " + id, null);
 		  	  }
 
 		  	  public List<Tag> getAllTags() {
 		  	    List<Tag> tags = new ArrayList<Tag>();
 
 		  	    Cursor cursor = database.query(TABLE_TAG,
 		  	        allTagColumns, null, null, null, null, null);
 
 		  	    cursor.moveToFirst();
 		  	    while (!cursor.isAfterLast()) {
 		  	      Tag tag = cursorToTag(cursor);
 		  	      tags.add(tag);
 		  	      cursor.moveToNext();
 		  	    }
 		  	    // make sure to close the cursor
 		  	    cursor.close();
 		  	    return tags;
 		  	  }
 		  	  
 		  	  public List<Tag> getMealTags(long mealId)
 		  	  {
 		  		List<Long> tagIds = getTagIdsForMeal(mealId);
 		  		
 		  		List<Tag> tags = new LinkedList<Tag>();
 		  		for(long tagId : tagIds)
 		  		{
 		  			Cursor cursor = database.query(TABLE_TAG,
 				  	        allTagColumns, COLUMN_ID + " = " + tagId, null,
 				  	        null, null, null);
 
 			  	    cursor.moveToFirst();
 			  	    tags.add(cursorToTag(cursor));
 			  	    
 			  	    // make sure to close the cursor
 			  	    cursor.close();
 		  		}
 		  		
 		  		  return tags;
 		  	  }
 
 		  	  private Tag cursorToTag(Cursor cursor) {
 		  	    Tag tag = new Tag();
 		  	    tag.setId(cursor.getLong(0));
 		  	    tag.setName(cursor.getString(1));
 		  	    return tag;
 		  	  }
 
 		  	  //MEALTAG METHODS
 		  	// Data Access Methods
 			  	public List<Long> getTagIdsForMeal(long mealId)
 			  	{
 			  		List<Long> mealTags = new ArrayList<Long>();
 
 			  		Cursor cursor = database.query(TABLE_MEAL_TAG_REL,
 				  	        allMealTagColumns, COLUMN_ID + " = " + mealId, null,
 				  	        null, null, null);
 
 			  	    cursor.moveToFirst();
 			  	    while (!cursor.isAfterLast()) {
 			  	      long tagId = cursor.getLong(2);
 			  	      mealTags.add(tagId);
 			  	      cursor.moveToNext();
 			  	    }
 			  	    // make sure to close the cursor
 			  	    cursor.close();
 			  	    return mealTags;
 			  	}
 			  	  
 			  	// MEAL DATA ACCESS METHODS
 				  	public Recipe createRecipe(String name, double time, String description, Integer numServings) {
 				  	    ContentValues values = new ContentValues();
 				  	    values.put(COLUMN_NAME, name);
 				  	  values.put(COLUMN_TIME, time);
 				  	values.put(COLUMN_DESCRIPTION, description);
 				  	values.put(COLUMN_RECIPE_NUM_SERVINGS, numServings);
 				  	    long insertId = database.insert(TABLE_RECIPE, null,
 				  	        values);
 				  	    Cursor cursor = database.query(TABLE_RECIPE,
 				  	    		allRecipeColumns, COLUMN_ID + " = " + insertId, null,
 				  	        null, null, null);
 				  	    cursor.moveToFirst();
 				  	    Recipe newRecipe = cursorToRecipe(cursor);
 				  	    cursor.close();
 				  	    return newRecipe;
 				  	  }
 
 				  	  public void deleteRecipe(Recipe recipe) {
 				  	    long id = recipe.getId();
 				  	    System.out.println("Recipe deleted with id: " + id);
 				  	    database.delete(TABLE_RECIPE, COLUMN_ID
 				  	        + " = " + id, null);
 				  	  }
 
 				  	  public List<Recipe> getAllRecipes() {
 				  	    List<Recipe> recipes = new ArrayList<Recipe>();
 
 				  	    Cursor cursor = database.query(TABLE_RECIPE,
 				  	    		allRecipeColumns, null, null, null, null, null);
 
 				  	    cursor.moveToFirst();
 				  	    while (!cursor.isAfterLast()) {
 				  	      Recipe recipe = cursorToRecipe(cursor);
 				  	      recipes.add(recipe);
 				  	      cursor.moveToNext();
 				  	    }
 				  	    // make sure to close the cursor
 				  	    cursor.close();
 				  	    return recipes;
 				  	  }
 				  	  
 				  	public List<Recipe> getMealRecipes(long mealId)
 				  	  {
 				  		List<Long> recipeIds = getRecipeIdsForMeal(mealId);
 				  		
 				  		List<Recipe> recipes = new LinkedList<Recipe>();
 				  		for(long recipeId : recipeIds)
 				  		{
 				  			Cursor cursor = database.query(TABLE_RECIPE,
						  	        allRecipeColumns, COLUMN_ID + " = " + recipeId, null,
 						  	        null, null, null);
 
 					  	    cursor.moveToFirst();
 					  	    recipes.add(cursorToRecipe(cursor));
 					  	    
 					  	    // make sure to close the cursor
 					  	    cursor.close();
 				  		}
 				  		
 				  		  return recipes;
 				  	  }
 				  	  
 				  	  public Recipe getRecipeById(long recipeId)
 				  	  {
 				  		Recipe recipe = new Recipe();
 
 				  		Cursor cursor = database.query(TABLE_RECIPE,
 				  				allRecipeColumns, COLUMN_ID + " = " + recipeId, null,
 					  	        null, null, null);
 
 				  	    cursor.moveToFirst();
 				  	    recipe = cursorToRecipe(cursor);
 				  	   
 				  	    // make sure to close the cursor
 				  	    cursor.close();
 				  	    return recipe;
 				  	  }
 				  	  private Recipe cursorToRecipe(Cursor cursor) {
 				  	    Recipe recipe = new Recipe();
 				  	    recipe.setId(cursor.getLong(0));
 				  	    recipe.setName(cursor.getString(1));
 				  	    recipe.setTime(cursor.getLong(2));
 				  	    recipe.setDescription(cursor.getString(3));
 				  	    recipe.setType("Recipe");
 				  	    recipe.setSteps(new LinkedList<RecipeStep>());
 				  	    recipe.setIngredients(new LinkedList<Ingredient>());
 				  	    recipe.setTags(new LinkedList<Tag>());
 				  	    return recipe;
 				  	  }
 				  	  
 				  	 //MEALTAG METHODS
 					  	// Data Access Methods
 						  	public List<Long> getRecipeIdsForMeal(long mealId)
 						  	{
 						  		List<Long> mealRecipes = new ArrayList<Long>();
 
 						  		Cursor cursor = database.query(TABLE_MEAL_RECIPE_REL,
 							  	        allMealRecipeColumns, COLUMN_ID + " = " + mealId, null,
 							  	        null, null, null);
 
 						  	    cursor.moveToFirst();
 						  	    while (!cursor.isAfterLast()) {
 						  	      long tagId = cursor.getLong(2);
 						  	      mealRecipes.add(tagId);
 						  	      cursor.moveToNext();
 						  	    }
 						  	    // make sure to close the cursor
 						  	    cursor.close();
 						  	    return mealRecipes;
 						  	}
 			
 	  	  // GENERIC DATABASE METHODS
 	  public static void onCreate(SQLiteDatabase database) {
 	    database.execSQL(MEAL_DATABASE_CREATE);
 	    database.execSQL(TAG_DATABASE_CREATE);
 	    database.execSQL(MEAL_TAG_DATABASE_CREATE);
 	    database.execSQL(RECIPE_DATABASE_CREATE);
 	    database.execSQL(MEAL_RECIPE_DATABASE_CREATE);
 	    
 	    
 	    // add initial meals & tags
 	    //Meal 1
 	    ContentValues values = new ContentValues();
   	    values.put(COLUMN_NAME, "Sam Initial Meal");
   	    values.put(COLUMN_TIME, 5.5);
   	    values.put(COLUMN_DESCRIPTION, "Initial Description");
   	    long meal1Id = database.insert(TABLE_MEAL, null,
   	        values);
   	    
   	    //Spicy Tag
   	    values = new ContentValues();
 	    values.put(COLUMN_NAME, "Spicy");
 	    long tag1Id = database.insert(TABLE_TAG, null,
 	        values);
 	    
 	    //Yummy Tag
   	    values = new ContentValues();
 	    values.put(COLUMN_NAME, "Yummy");
 	    long tag2Id = database.insert(TABLE_TAG, null,
 	        values);
 	    
 	    //MealTag1
 	    values = new ContentValues();
   	    values.put(COLUMN_MEAL_ID, meal1Id);
   	  values.put(COLUMN_TAG_ID, tag1Id);
   	    long mealTag1Id = database.insert(TABLE_MEAL_TAG_REL, null,
   	        values);
   	    
   	    //MealTag2
 	    values = new ContentValues();
   	    values.put(COLUMN_MEAL_ID, meal1Id);
   	  values.put(COLUMN_TAG_ID, tag2Id);
   	    long mealTag2Id = database.insert(TABLE_MEAL_TAG_REL, null,
   	        values);
   	    
   	    //Meal 1
 	    values = new ContentValues();
   	    values.put(COLUMN_NAME, "Sam Initial Meal 2");
   	    values.put(COLUMN_TIME, 6.8);
   	    values.put(COLUMN_DESCRIPTION, "Initial Description 2");
   	    long meal2Id = database.insert(TABLE_MEAL, null,
   	        values);
   	    
   	    //Recipe 1
   	    values = new ContentValues();
 	    values.put(COLUMN_NAME, "Sam Initial Recipe 1");
 	    values.put(COLUMN_TIME, 2.2);
 	    values.put(COLUMN_DESCRIPTION, "Initial Recipe Description 1");
 	    values.put(COLUMN_RECIPE_NUM_SERVINGS, 3);
 	    long recipe1Id = database.insert(TABLE_RECIPE, null,
 	        values);
 	    
 	    //Recipe 1
 	  	  values = new ContentValues();
 		    values.put(COLUMN_NAME, "Sam Initial Recipe 2");
 		    values.put(COLUMN_TIME, 2);
 		    values.put(COLUMN_DESCRIPTION, "Initial Recipe Description 2");
 		    values.put(COLUMN_RECIPE_NUM_SERVINGS, 6);
 		    long recipe2Id = database.insert(TABLE_RECIPE, null,
 		        values);
 		    
 		    //MealRecipe1
 		    values = new ContentValues();
 	  	    values.put(COLUMN_MEAL_ID, meal1Id);
 	  	  values.put(COLUMN_RECIPE_ID, recipe1Id);
 	  	    long mealRecipe1Id = database.insert(TABLE_MEAL_RECIPE_REL, null,
 	  	        values);
 	  	    
 	  	  //MealRecipe2
 		    values = new ContentValues();
 	  	    values.put(COLUMN_MEAL_ID, meal1Id);
 	  	  values.put(COLUMN_RECIPE_ID, recipe2Id);
 	  	    long mealRecipe2Id = database.insert(TABLE_MEAL_RECIPE_REL, null,
 	  	        values);
 	  }
 
 	  public static void onUpgrade(SQLiteDatabase database, int oldVersion,
 	      int newVersion) {
 	    Log.w(DataSourceManager.class.getName(), "Upgrading database from version "
 	        + oldVersion + " to " + newVersion
 	        + ", which will destroy all old data");
 	    database.execSQL("DROP TABLE IF EXISTS " + TABLE_MEAL);
 	    database.execSQL("DROP TABLE IF EXISTS " + TABLE_TAG);
 	    database.execSQL("DROP TABLE IF EXISTS " + TABLE_MEAL_TAG_REL);
 	    database.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE);
 	    database.execSQL("DROP TABLE IF EXISTS " + TABLE_MEAL_RECIPE_REL);
 	    onCreate(database);
 	  }
 
 }
