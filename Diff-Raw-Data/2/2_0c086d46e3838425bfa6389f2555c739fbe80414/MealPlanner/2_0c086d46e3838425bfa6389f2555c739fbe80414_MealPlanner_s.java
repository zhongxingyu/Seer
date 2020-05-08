 package com.customfit.ctg.controller;
 
 import com.customfit.ctg.model.*;
 import com.customfit.ctg.view.*;
 import com.customfit.ctg.view.meal.*;
 import java.util.*;
 import javax.swing.*;
 
 
 /**
  * The composite model of our data.
  * 
  * @author Drew, David
  */
 public class MealPlanner { // implements Observable
 
     //needs restructuring
     //particularly, the model for Meal needs to be developed first
 
     /**
      * Activates Browse Recipes application feature, which displays
      * a Browse Recipes JPanel in the main JFrame from the list
      * of all known recipes in the Controller's default database.
      */
     public static void browseMenu()
     {
         //recipes not provided, so go get them
         List<Recipe> recipes = Application.getDataDriver().selectAllRecipes();
         List<Meal> meals = UserManagement.getCurrentUser().getAllMeals();
 
         //now pass them to the overloaded method
         browseMenu(recipes, meals);
     }
 
     /**
      * Activates Browse Recipes application feature, which displays
      * a Browse Recipes JPanel in the main JFrame with the recipes
      * you have provided.
      *
      * @param recipes The list of recipes to browse through.
      */
     public static void browseMenu(List<Recipe> recipes, List<Meal> meals)
     {
         //create panel
        MealMenuPanel mealMenuPanel = new MealMenuPanel(MealMenuPanel.ListMode.LIST_BROWSE);
         //tell panel about our recipes
         mealMenuPanel.setRecipeList(recipes);
         mealMenuPanel.setMenuList(meals);
         //display panel in main frame
         Application.getMainFrame().setPanel(mealMenuPanel);
     }
 
     /**
      * Add the recipe to the meal.
      *
      * @param recipe
      * @param meal
      */
 //    public static void addRecipeToMeal(Recipe recipe, String mealName) {
 //        User cUser = UserManagement.getCurrentUser();
 //        Meal meal = cUser.getMealByName(mealName);
 //        if (meal != null)
 //            meal.getRecipes().add(recipe);
 //    }
 
     /**
      * Add the recipe to the meal.
      *
      * @param recipe
      * @param meal
      */
     public static void addRecipeToMeal(Recipe recipe, int mealIndex) {
         User cUser = UserManagement.getCurrentUser();
         if (mealIndex >=0 && mealIndex < cUser.getAllMeals().size()) {
             Meal meal = cUser.getAllMeals().get(mealIndex);
             if (meal != null)
                 meal.getRecipes().add(recipe);
         }
     }
 
     /**
      * Remove the recipe from the meal.
      *
      * @param recipe
      * @param meal
      */
 //    public static void removeRecipeFromMeal(String recipeName, String mealName) {
 //        User cUser = UserManagement.getCurrentUser();
 //        Meal meal = cUser.getMealByName(mealName);
 //        if (meal != null) {
 //            int rindex = getRecipeIndexInMeal(meal, recipeName);
 //            if (rindex > 0)
 //                meal.getRecipes().remove(rindex);
 //        }
 //    }
 
     /**
      * Remove the recipe from the meal.
      *
      * @param mealIndex
      * @param recipeIndex
      */
     public static void removeRecipeFromMeal(int mealIndex, int recipeIndex) {
         User cUser = UserManagement.getCurrentUser();
         if (mealIndex >= 0 && recipeIndex >= 0) {
             if (mealIndex < cUser.getAllMeals().size()) {
                 Meal meal =  cUser.getAllMeals().get(mealIndex);
                 if (recipeIndex < meal.getRecipes().size()) {
                     Recipe r = meal.getRecipes().get(recipeIndex);
                     meal.getRecipes().remove(r);
                 }
             }
         }
     }
 
     /**
      * Get the index for the recipe in a given meal.
      *
      * @param meal The meal to get the index from.
      * @param mealName The meal's name to search for
      * @return Returns the index.  -1 if no match is found.
      */
     public static int getRecipeIndexInMeal(Meal meal, String recipeName) {
         int index = 0;
         for (Recipe r : meal.getRecipes()) {
             if (r.getName().equals(recipeName))
                 return index;
             index++;
         }
         return -1;
     }
 
     /**
      * Gets the list of meals from the current user.
      */
     public static List<Meal> getAllMeals()
     {
         return UserManagement.getCurrentUser().getAllMeals();
     }
 
     public static void createDefaultMeals() {
         User cUser = UserManagement.getCurrentUser();
         cUser.getAllMeals().add(new Meal("Monday"));
         cUser.getAllMeals().add(new Meal("Tuesday"));
         cUser.getAllMeals().add(new Meal("Wednesday"));
         cUser.getAllMeals().add(new Meal("Thursday"));
         cUser.getAllMeals().add(new Meal("Friday"));
         cUser.getAllMeals().add(new Meal("Saturday"));
         cUser.getAllMeals().add(new Meal("Sunday"));
 
     }
     
     /**
      * You insert a Date and it loads the Insert Meal Plan panel.
      * 
      * @param date The initial date to set the meal to.
      */
     public static void insertMealPlan(Date date)
     {
         //create panel
         EditMealPanel newMealPanel = new EditMealPanel(CreateEditMode.CREATE);
         //begin building a new meal
         Meal meal = new Meal("", date);
         //tell panel about new meal
         newMealPanel.setMeal(meal);
         //display panel in main frame
         Application.getMainFrame().setPanel(newMealPanel);
     }
     
     /**
      * The Insert Meal Plan panel calls this back when the meal has been prepared.
      * It inserts the meal into your user's meal plans, saves the user to
      * disk, and then loads the last panel, presumably the weekly meal view.
      * 
      * @param meal The meal to insert into the database.
      * 
      * @return Boolean indicating the success of the operation. 
      */
     public static boolean insertMealPlan(Meal meal)
     {
         //update the user object by adding the new meal
         UserManagement.getCurrentUser().getAllMeals().add(meal);
         //send the user over to the database
         boolean status = Application.getDataDriver().updateUserByName(UserManagement.getCurrentUser().getName(), UserManagement.getCurrentUser());
         //check for errors
         if (!status)
             //if failed, tell user about the failure
             JOptionPane.showMessageDialog(Application.getMainFrame(), "There was a problem creating your meal plan.", "Error", JOptionPane.ERROR_MESSAGE);
         else
         {
             //otherwise, assume success and go back
             Application.getMainFrame().goBack();
             //refresh data on previous panel
             Application.getMainFrame().getPanel().refresh();
         }
         //return status
         return status;
 
     }
     
     /**
      * Loads the Edit Meal Plan panel with the meal you specified.
      * 
      * @param meal The initial meal to display.
      */
     public static void editMealPlan(Meal meal)
     {
         //create panel
         EditMealPanel newMealPanel = new EditMealPanel(CreateEditMode.EDIT);
         //tell panel about new meal
         newMealPanel.setMeal(meal);
         //display panel in main frame
         Application.getMainFrame().setPanel(newMealPanel);
     }
     
     /**
      * The Edit Meal Plan panel calls this back when the meal has been prepared.
      * It inserts the meal into your user's meal plans, saves the user to
      * disk, and then loads the last panel, presumably the weekly meal view.
      * 
      * @param 
      * @param meal The meal to insert into the database.
      * 
      * @return Boolean indicating the success of the operation. 
      */
     public static boolean editMealPlan(Meal initialMeal, Meal newMeal)
     {
         //find and replace initial meal with new meal
         for (int m = 0; m < UserManagement.getCurrentUser().getAllMeals().size(); m++)
             if (UserManagement.getCurrentUser().getAllMeals().equals(initialMeal))
                 UserManagement.getCurrentUser().getAllMeals().set(m, newMeal);
         //send the user over to the database
         boolean status = Application.getDataDriver().updateUserByName(UserManagement.getCurrentUser().getName(), UserManagement.getCurrentUser());
         //check for errors
         if (!status)
             //if failed, tell user about the failure
             JOptionPane.showMessageDialog(Application.getMainFrame(), "There was a problem editing your meal plan.", "Error", JOptionPane.ERROR_MESSAGE);
         else
         {
             //otherwise, assume success and go back
             Application.getMainFrame().goBack();
             //refresh data on previous panel
             Application.getMainFrame().getPanel().refresh();
         }
         //return status
         return status;
 
     }
 }
