 package ca.thewhitakers.recipebook.domain.model;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
import org.hibernate.FetchMode;

 @Entity
 @Table(name = "recipe")
 public class Recipe extends DomainObject {
 	private String name;
 	private Collection<RecipeInfo> recipeInfos;
 	private Collection<Ingredient> ingredients;
 	
 	public Recipe() {
 		recipeInfos = new ArrayList<RecipeInfo>();
 		ingredients = new ArrayList<Ingredient>();
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="recipe")
 	public Collection<RecipeInfo> getRecipeInfos() {
 		return recipeInfos;
 	}
 	
 	public void setRecipeInfos(Collection<RecipeInfo> recipeInfos) {
 		this.recipeInfos = recipeInfos;
 	}
 	
 //	@OneToMany(cascade = CascadeType.ALL)
 //	public Collection<Ingredient> getIngredients() {
 //		return ingredients;
 //	}
 //	
 //	public void setIngredients(Collection<Ingredient> ingredients) {
 //		this.ingredients = ingredients;
 //	}
 	
 	public RecipeInfo addInfo(RecipeInfoType type, String value) {
 		RecipeInfo info = new RecipeInfo();
 		info.setRecipeInfoType(type);
 		info.setValue(value);
 		
 		recipeInfos.add(info);
 		
 		return info;
 	}
 	
 	public Ingredient addIngredient(float quantity, FoodItem foodItem) {
 		return addIngredient(quantity, null, foodItem);
 	}
 	
 	public Ingredient addIngredient(float quantity, Measurement measurement, FoodItem foodItem) {
 		Ingredient ingredient = new Ingredient();
 		ingredient.setQuantity(quantity);
 		ingredient.setMeasurement(measurement);
 		ingredient.setFoodItem(foodItem);
 		ingredient.setRecipe(this);
 		
 		ingredients.add(ingredient);
 		
 		return ingredient;
 	}
 }
