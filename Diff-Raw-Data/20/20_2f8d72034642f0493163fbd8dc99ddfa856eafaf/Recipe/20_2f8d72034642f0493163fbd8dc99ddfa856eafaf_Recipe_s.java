 package com.customfit.ctg.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * The model of a recipe.
  * 
  * @author Drew
  */
 public class Recipe {
 
 	private String name = "New Recipe";
 	private String description = "Your recipe description goes here.";
 	private String instructions = "Insert your recipe here.";
 
	private MeasurableUnit servingSize = new MeasurableUnit(1.0, "specify unit");
 	
 	private double servings = 0.0;
 	
 	private double rating = 0.0;
 
 	private List<RecipeIngredient> ingredients = new ArrayList<RecipeIngredient>();
 	
 	private NutritionFacts nutritionInformation;
 
 	/**
 	 * Constructs an empty Recipe object.
 	 * Fields must be set with setters.
 	 */
 	public Recipe() {
             super();
 	}
 	
 	/**
 	 * Constructs a recipe object with all parameters.
 	 * 
 	 * @param name
 	 * @param description
 	 * @param instructions
 	 * @param servingSize
 	 * @param servings
 	 * @param rating
 	 * @param ingredients
 	 * @param nutrition
 	 */
 	public Recipe(String name,
 				  String description,
 				  String instructions,
 				  MeasurableUnit servingSize,
 				  double servings,
 				  double rating,
 				  List<RecipeIngredient> ingredients,
 				  NutritionFacts nutritionInformation
 				  ){
             super();
             this.setName(name);
             this.setDescription(description);
             this.setInstructions(instructions);
             this.setServingSize(servingSize);
             this.setServings(servings);
             this.setRating(rating);
             this.setIngredients(ingredients);
             this.setNutritionInformation(nutritionInformation);
 	}
 
 	/**
 	 * @return the servingSize
 	 */
 	public MeasurableUnit getServingSize() {
 		return servingSize;
 	}
 
 	/**
 	 * @param servingSize the servingSize to set
 	 */
 	public void setServingSize(MeasurableUnit servingSize) {
 		this.servingSize = servingSize;
 	}
 
 	/**
 	 * @return the servings
 	 */
 	public double getServings() {
 		return servings;
 	}
 
 	/**
 	 * @param servings the servings to set
 	 */
 	public void setServings(double servings) {
 		this.servings = servings;
 	}
 
 	/**
 	 * @param name the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param description the description to set
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @param instructions the instructions to set
 	 */
 	public void setInstructions(String instructions) {
 		this.instructions = instructions;
 	}
 
 	/**
 	 * @return the instructions
 	 */
 	public String getInstructions() {
 		return instructions;
 	}
 
 	/**
 	 * @param rating the rating to set
 	 */
 	public void setRating(double rating) {
 		this.rating = rating;
 	}
 
 	/**
 	 * @return the rating
 	 */
 	public double getRating() {
 		return rating;
 	}
 	
 	/**
 	 * @param ingredients the ingredients to set
 	 */
 	protected void setIngredients(List<RecipeIngredient> ingredients) {
 		this.ingredients = ingredients;
 	}
 	
 	/**
 	 * @return the ingredients
 	 */
 	public List<RecipeIngredient> getIngredients() {
 		return ingredients;
 	}
 	
 	/**
 	 * @param nutrition the nutrition information to set
 	 */
 	public void setNutritionInformation(NutritionFacts nutritionInformation) {
 		this.nutritionInformation = nutritionInformation;
 	}
 
 	/**
 	 * @return the nutrition information
 	 */
 	public NutritionFacts getNutritionInformation() {
 		return nutritionInformation;
 	}
 	
 	@Override
 	public String toString()
 	{
 		return this.getName();
 	}        
 }
