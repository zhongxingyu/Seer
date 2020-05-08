 package com.cmput301w13t09.cmput301project;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 
 /**
  * @author Kyle,Marcus,Landre Class: RecipeListModel RecipeList is a class that
  *         stores a list of recipes. These recipes are from RecipeModel and are
  *         stored in ArrayList class. The constructor takes in a single recipe
  *         (RecipeModel)and appends it to a blank recipe list. RecipeLists
  *         methods are getLength, getRecipeListName, getRecipeListDesc
  *         getRecipeingredientList, and getRecipePhotoList.
  */
 
 @SuppressLint("DefaultLocale")
 public class RecipeController {
 	private RecipeListModel recipe_list;
 	private Context ctx;
 
 	/**
 	 * 
 	 * @param tctx
 	 *            Context of the activity running the controller
 	 */
 	public RecipeController(Context tctx) {
 		recipe_list = new RecipeListModel();
 		ctx = tctx;
 		this.loadFromFile();
 
 	}
 
 	/**
 	 * 
 	 * @param recipe
 	 *            : The Recipe to be appended to the list
 	 * @return this so that chain adding can happen.
 	 */
 	public RecipeController addRecipe(RecipeModel recipe) {
 		recipe_list.add(recipe);
 		return this;
 	}
 
 	public void replaceRecipe(int i, RecipeModel recipe) {
 		recipe_list.set(i, recipe);
 	}
 
 	/**
 	 * Returns length of recipe list (RecipeListModel)
 	 * 
 	 * @return size : The size of the list of recipes
 	 */
 	public int getLength() {
 		return recipe_list.size();
 	}
 
 	/**
 	 * Returns name of recipe (RecipeListModel) based on its position in list
 	 * 
 	 * @param i
 	 *            : Position of recipe in the list
 	 * @return Name of recipe
 	 */
 	public String getRecipeListName(int i) {
 		return recipe_list.get(i).getRecipeName();
 	}
 
 	/**
 	 * Returns description of recipe (RecipeListModel) based on its position in
 	 * list
 	 * 
 	 * @param i
 	 *            : Position of recipe in the list
 	 * @return Description of recipe
 	 */
 	public String getRecipeListDesc(int i) {
 		return recipe_list.get(i).getRecipeDesc();
 	}
 
 	/**
 	 * Returns list of ingredients of recipe (RecipeListModel) based on its
 	 * position in recipe list
 	 * 
 	 * @param i
 	 *            : Position of recipe in the list
 	 * @return List of ingredient that go into this recipe
 	 */
 	public IngredientListModel getRecipeingredientList(int i) {
 		return recipe_list.get(i).getIngredList();
 	}
 
 	/**
 	 * Returns list of photos of recipe (RecipeListModel) based on its position
 	 * in recipe list
 	 * 
 	 * @param i
 	 *            : Position of recipe in the list
 	 * @return List of photos attached to a recipe item
 	 */
 	public PhotoListModel getRecipePhotoList(int i) {
 		return recipe_list.get(i).getPhotoList();
 	}
 
 	/**
 	 * 
 	 * @return the List of recipes
 	 */
 	public RecipeListModel getRecipeList() {
 		return this.recipe_list;
 	}
 
 	/**
 	 * 
 	 * @param i
 	 *            The index of the recipe to be removed
 	 */
 	public void remove(int i) {
 		this.recipe_list.remove(i);
 	}
 
 	public void loadFromFile() {
 		try {
 			FileInputStream fileIn = ctx.openFileInput("Recipe.data");
 			ObjectInputStream objectInStream = new ObjectInputStream(fileIn);
 			recipe_list = (RecipeListModel) objectInStream.readObject();
 			objectInStream.close();
 			return;
 		} catch (FileNotFoundException FNE) {
 			try {
 				FileOutputStream temp = ctx.openFileOutput("Recipe.data",
 						Context.MODE_PRIVATE);
 				ObjectOutputStream objectOutStream = new ObjectOutputStream(
 						temp);
 				objectOutStream.writeObject(null);
 				objectOutStream.close();
 
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} catch (NullPointerException NPE) {
 			try {
 				FileOutputStream temp = ctx.openFileOutput("Recipe.data",
 						Context.MODE_PRIVATE);
 				ObjectOutputStream objectOutStream = new ObjectOutputStream(
 						temp);
 				objectOutStream.writeObject(null);
 				objectOutStream.close();
 
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} catch (ClassNotFoundException cnfe) {
 			cnfe.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Saves RecipeListModel ingred_list to file Recipe.data
 	 */
 	public void saveToFile() {
 		try {
 			new File("Recipe.data").delete();
 			FileOutputStream fileOut = ctx.openFileOutput("Recipe.data",
 					Context.MODE_PRIVATE);
 			ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOut);
 			objectOutStream.writeObject(recipe_list);
 			objectOutStream.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public RecipeModel getRecipe(int position) {
 		return recipe_list.get(position);
 	}
 
 	/**
 	 * Searches for fname in RecipeList and returns the position in list.
 	 * 
 	 * @param fname
 	 * @return Returns position of name found in RecipeList if not found returns
 	 *         -1
 	 */
 	@SuppressLint("DefaultLocale")
 	public int findRecipe(String fname) {
 		int position = -1;
 		for (int i = 0; i < this.recipe_list.size(); i++) {
 			if (fname
 					.trim()
 					.toLowerCase()
 					.equals(this.recipe_list.get(i).getRecipeName().trim()
 							.toLowerCase())) {
 				position = i;
 			}
 		}
 		return position;
 	}
 
 	/**
 	 * Returns position of name found in RecipeList if ingredients are in
 	 * MyPantry otherwise return -1
 	 * 
 	 * @param position
 	 * @param ingredController
 	 * @return returns position of name found in RecipeList if ingredients are
 	 *         in MyPantry otherwise return -1
 	 */
 	public int checkRecipeHasIngredients(int position,
 			IngredientController ingredController) {
 		// If no recipe is found
 		if (position == -1) {
 			return position;
 		}
 		int count = 0;
 		for (int i = 0; i < this.recipe_list.get(position).getIngredList()
 				.size(); i++) {
 			for (int j = 0; j < ingredController.getIngredientList().size(); j++) {
 				if (recipe_list
 						.get(position)
 						.getIngredList()
 						.get(i)
 						.getIngredientName()
 						.trim()
 						.toLowerCase()
 						.equals(ingredController.getIngredient(j)
								.getIngredientName().trim().toLowerCase())) {
 					count++;
 				}
 			}
 		}
 		if (count == this.recipe_list.get(position).getIngredList().size()) {
 			return position;
 		} else {
 			return -1;
 		}
 	}
 
 	/**
 	 * Returns a RecipeList that is all the recipes that have all ingredients in
 	 * IngredientController
 	 * 
 	 * @param ingredController
 	 * @return
 	 */
 	public RecipeListModel getQueryRecipeList(
 			IngredientController ingredController) {
 		RecipeListModel temp = new RecipeListModel();
 		for (int i = 0; i < this.recipe_list.size(); i++) {
 			if (this.checkRecipeHasIngredients(i, ingredController) != -1) {
 				temp.add(recipe_list.get(i));
 			}
 		}
 		return temp;
 	}
 }
