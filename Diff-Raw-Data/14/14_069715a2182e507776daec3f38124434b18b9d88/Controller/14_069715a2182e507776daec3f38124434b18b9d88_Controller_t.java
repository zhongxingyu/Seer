 package ca.ualberta.team2recipefinder;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Controller {
 	
 	RecipeModel model;
 	IngredientList myKitchen;
 	
 	public Controller(RecipeModel model, IngredientList myKitchen) {
 		this.model = model;
 		this.myKitchen = myKitchen;
 	}
 	
 	public List<Recipe> search(String[] keywords, boolean searchLocally, boolean searchFromWeb) {
 		// for now, it only searches locally
 		return model.searchRecipe(keywords);
 	}
 	
 	public List<Recipe> searchWithIngredients(String[] keywords, boolean searchLocally, 
 									  boolean searchFromWeb) {
 		// for now, it only searches locally
 		//return model.searchWithIngredients(keywords, searchLocally, searchFromWeb);
 		
 		// stil working in  the search with ingredients
 		return model.searchRecipe(keywords);
 	}
 	
 	public Recipe getRecipe(long id) {
 		return model.getRecipeById(id);
 	}
 	
 	public List<Recipe> getRecipes() {
 		return model.getAllRecipes();
 	}
 	
 	public void addRecipe(Recipe r) {
 		model.add(r);
 	}
 	
 	public void replaceRecipe(Recipe r, long id) {
 		model.replaceRecipe(r, id);
 		// should replace recipe with given id with r
 	}
 	
 	public List<Ingredient> getIngredients() {
 		return myKitchen.load();
 	}
 	
 	public void deleteIngredient(Ingredient ingredient) {
 		myKitchen.remove(ingredient);
 	}
 	
 	public List<Ingredient> searchIngredient(String keyword) {
 		return myKitchen.searchIngredient(keyword);
 	}
 	
 	public void addIngredient(Ingredient ingredient) {
 		myKitchen.add(ingredient);
 	}
	
	public void deleteRecipe(Recipe r){
		model.remove(r);
	}
 }
