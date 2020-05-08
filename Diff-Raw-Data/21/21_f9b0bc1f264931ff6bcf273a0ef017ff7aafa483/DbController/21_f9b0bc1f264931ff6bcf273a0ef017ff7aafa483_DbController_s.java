 package ca.ualberta.cmput301w13t11.FoodBook.controller;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.util.Log;
 import ca.ualberta.cmput301w13t11.FoodBook.model.DbManager;
 import ca.ualberta.cmput301w13t11.FoodBook.model.FView;
 import ca.ualberta.cmput301w13t11.FoodBook.model.Ingredient;
 import ca.ualberta.cmput301w13t11.FoodBook.model.IngredientsDbManager;
 import ca.ualberta.cmput301w13t11.FoodBook.model.Photo;
 import ca.ualberta.cmput301w13t11.FoodBook.model.Recipe;
 import ca.ualberta.cmput301w13t11.FoodBook.model.RecipesDbManager;
 import ca.ualberta.cmput301w13t11.FoodBook.model.ResultsDbManager;
 
 /**
  * Controller for the DbManager.
  * @author Mark Tupala, Marko Babic
  *
  */
 public class DbController {
         
     // name of tables
     protected String UserRecipes = "UserRecipes";
     protected String ResultsRecipes = "ResultsRecipes";
     protected String UserIngredients = "UserIngredients";
     
     // SQL queries
     private String getUserRecipesSQL = "SELECT * FROM " + UserRecipes;
     
     private String getResultRecipesSQL = "SELECT * FROM " + ResultsRecipes;
     private String getUserIngredientsSQL = "SELECT * FROM " + UserIngredients;
     
     private static DbManager db;
     private static RecipesDbManager recipesManager;
     private static ResultsDbManager resultsManager;
     private static IngredientsDbManager ingredsManager;
      
         
     // singleton pattern implementation
     private static DbController instance = null;
         
     /* 
      * private constructor because we're using the singleton pattern.
      */
     private DbController(Context context) {
     	db = DbManager.getInstance(context);
     	recipesManager = RecipesDbManager.getInstance(context);
     	ingredsManager = IngredientsDbManager.getInstance(context);
     	resultsManager = ResultsDbManager.getInstance(context);
     }
         
     /**
      * Get instance of the singleton DbController.
      * @return The instance of the class.
      */
     public static DbController getInstance(Context context, FView<DbManager> view) {
         if (instance == null) {
                 // db instance doesn't exist, create new one
                 instance = new DbController(context);
         }
         db.addView(view);
         return instance;
     }
 
     /**
      * delete view from model's array of views
      * @return void
      */
     public void deleteView(FView<DbManager> view) {
         db.deleteView(view);
     }
     
     /* *****************************************************************
     USE THESE METHODS FOR ADDING, EDITING, RETRIEVING, AND DELETING
     THE USER'S PERSONAL RECIPES
    ********************************************************************* */
     
     /**
      * @return Returns an ArrayList containing all the Recipes the user has stored on their device.
      */
     public ArrayList<Recipe> getUserRecipes() {
     	return recipesManager.getRecipes();
     }
     
     /**
      * @return Returns a Recipe the user has stored on their device, given recipe uri
      */
     public Recipe getUserRecipe(long uri) {
     	return recipesManager.getRecipe(uri);
     }
 
     /**
      * Adds the given Recipe to the database.
      * @param recipe The recipe to add.
      */
     public void addRecipe(Recipe recipe) {
     	recipesManager.insertRecipe(recipe);
     	db.notifyViews();
     }
 
     /**
      * Deletes the given Recipe from the database.
      * @param recipe The recipe to delete.
      */
     public boolean deleteRecipe(Recipe recipe)
     {	
     	Log.d("are we here as well", "are we here as well");
     	boolean success = recipesManager.removeRecipe(recipe);
     	db.notifyViews();
     	return success;
     }
     
     /**
      * Updates the Recipe with the new information passed by the Recipe parameter.
      * CAUTION: does no error checking, may cause error in Recipe not already in Db, call with caution.
      * @param recipe The Recipe to be updated.
      */
     public void updateRecipe(Recipe recipe)
     {
     	recipesManager.updateRecipe(recipe);
     	db.notifyViews();
     }   
     
     /* *****************************************************************
     USE THESE METHODS FOR RETRIEVING/SAVING/DELETING PHOTOS TO USER'S RECIPES
    ********************************************************************* */
     /**
      * Associates the Photo argument correctly with the given Recipe in the database.
      * @param photo The photo to add to the given Recipe.
      * @param recipe The Recipe to which the photo is to be added.
      */
     public boolean addPhotoToRecipe(Photo photo, Bitmap bitmap,long uri)
     {
     	boolean status = recipesManager.insertRecipePhotos(photo, bitmap, uri);
     	db.notifyViews();
     	return status;
     }
     /**
      * @return Returns an ArrayList of Photos for a given recipe
      */
     public ArrayList<Photo> getRecipePhotos(long uri) {
     	return recipesManager.getRecipePhotos(uri);
     }
     
     public boolean deleteRecipePhoto(Photo photo)
     {
     	boolean success = recipesManager.removeRecipePhoto(photo);
     	db.notifyViews();
     	return success;
     }
 
     /* *****************************************************************
     USE THESE METHODS FOR RETRIEVING/SAVING/DELETING INGREDIENTS TO USER'S RECIPES
    ********************************************************************* */
     /**
      * Associates the Ingredient argument correctly with the given Recipe in the database.
      * @param ingredient The ingredient to add to the given Recipe.
      * @param recipe The Recipe to which the ingredient is to be added.
      */
     public boolean addIngredientToRecipe(Ingredient ingred, long uri)
     {
         boolean status = recipesManager.insertRecipeIngredients(ingred, uri);
         db.notifyViews();
         return status;
     }
     
     /**
      * @return Returns an ArrayList of Ingredients for a given recipe
      */
     public ArrayList<Ingredient> getRecipeIngredients(long uri) {
         return recipesManager.getRecipeIngredients(uri);
     }
     
     public boolean deleteRecipeIngredient(Ingredient ingred, Recipe recipe)
     {
         boolean success = recipesManager.removeRecipeIngredient(ingred.getName(), recipe.getUri());
         db.notifyViews();
         return success;
     }
     
     /* *****************************************************************
    USE THESE METHODS FOR RETRIEVING/SAVING RECIPES STORED FROM SEARCH
    ********************************************************************* */
     
     /**
      * @return Returns an ArrayList containing all the Recipes stored from search
      */
     public ArrayList<Recipe> getStoredRecipes() {
     	return resultsManager.getRecipes();
     }
     
     /* *****************************************************************
          USE THESE METHODS FOR ADDING, EDITING, RETRIEVING, AND DELETING
          THE USER'S PERSONAL INGREDIENTS
      ********************************************************************* */
     /**
      * Adds the given Ingredient to the database.
      * @param ingredient The Ingredient to add.
      */
     public void addIngredient(Ingredient ingredient) {
     	ingredsManager.insert(ingredient);
     	db.notifyViews();
     }
 
     /**
      * @return Returns an ArrayList containing all the Ingredients the user has stored on their device.
      */
     public ArrayList<Ingredient> getUserIngredients() {
     	return ingredsManager.get();
     }
 
     /**
      * Deletes the given Ingredient from the database.
      * @param ingredient The Ingredient to delete.
      */
     public void deleteIngredient(Ingredient ingred) {
     	ingredsManager.delete(ingred);
     	db.notifyViews();
     }
 }
