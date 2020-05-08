 package ca.ualberta.cmput301w13t11.FoodBook.controller;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import ca.ualberta.cmput301w13t11.FoodBook.model.DbManager;
 import ca.ualberta.cmput301w13t11.FoodBook.model.FView;
 import ca.ualberta.cmput301w13t11.FoodBook.model.Ingredient;
 import ca.ualberta.cmput301w13t11.FoodBook.model.Recipe;
 import ca.ualberta.cmput301w13t11.FoodBook.model.RecipesDbManager;
 
 /**
  * Controller for the DbManager.
  * @author Mark Tupala, Marko Babic
  *
  */
 public class DbController {
         
         private static DbManager db;
         private static RecipesDbManager recipesDB;
         
         // singleton pattern implementation
     private static DbController instance = null;
         
         /* 
          * private constructor because we're using the singleton pattern.
          */
         private DbController(Context context) {
                 db = DbManager.getInstance(context);
                 recipesDB = RecipesDbManager.getInstance(context);
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
     
     /**
      * @return Returns an ArrayList containing all the Recipes the user has stored on their device.
      */
         public ArrayList<Recipe> getUserRecipes() {
                return recipesDB.getRecipes("UserRecipes");
         }
 
     /**
      * @return Returns an ArrayList containing all the Recipes stored from search
      */
         public ArrayList<Recipe> getStoredRecipes() {
                return recipesDB.getRecipes("ResultsRecipes");
         }
         
         /**
          * Adds the given Recipe to the database.
          * @param recipe The recipe to add.
          */
         public void addRecipe(Recipe recipe) {
                 recipesDB.insertRecipe(recipe, "UserRecipes");
                 db.notifyViews();
         }
         
         /**
          * Deletes the given Recipe from the database.
          * @param recipe The recipe to save.
          */
         public void deleteRecipe(Recipe recipe)
         {
                 recipesDB.deleteRecipe(recipe);
                 db.notifyViews();
         }
         
         /**
          * Associates the Ingredient argument correctly with the given Recipe in the database.
          * @param ingredient The ingredient to add to the given Recipe.
          * @param recipe The Recipe to which the ingredient is to be added.
          */
         public void addIngredientToRecipe(Ingredient ingredient, Recipe recipe)
         {
                 db.insert(ingredient, recipe.getUri());
                 db.notifyViews();
         }
 }
