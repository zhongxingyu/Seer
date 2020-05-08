 package ca.ualberta.team2recipefinder;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class Recipe extends Model<View> implements Serializable
 {
 	/**
 	 * Constructor
 	 */
 	private static final long serialVersionUID = 1L;
 	private String name;
 	private String procedure;
 	private String author;
 	private List<Ingredient> ingredients;
 	private boolean onServer;
 	private long id;
 	
 	public Recipe() {
 		name = "NEW_RECIPE";
 		procedure = new String();
 		author = new String();
 		ingredients = new ArrayList<Ingredient>();
 		onServer = false;
 		id = System.currentTimeMillis();
 	}
 	
 	public Recipe(String name, String procedure, String author, List<Ingredient> ingredients, boolean onServer){
 		this.name = name;
 		this.procedure = procedure;
 		this.author = author;
 		this.ingredients = ingredients;
 		this.onServer = onServer;
 	 	this.id = System.currentTimeMillis();
 	}
 	
 	public long getRecipeID(){
 		return id;
 	}
 	
 	public String getName(){
 		return name;
 	}
 	
 	public String toString() {
 		return name;
 	}
 	
 	public void setName(String name){
 		this.name = name;
 		notifyViews();
 	}
 	
 	public String getProcedure(){
 		return procedure;
 	}
 	
 	public void setProcedure(String procedure){
 		this.procedure = procedure;
 		notifyViews();
 	}
 	
 	public String getAuthor(){
 		return author;
 	}
 	
 	public void setAuthor(String author){
 		this.author = author;
 		notifyViews();
 	}
 	
 	public List<Ingredient> getIngredients(){
 		return ingredients;
 	}
 		
 	public void removeIngredient(Ingredient ingredient) {
 		ingredients.remove(ingredient);
 		notifyViews();
 	}
 	
 	public void replaceIngredient(Ingredient oldIngredient, Ingredient newIngredient) {
 		ingredients.remove(oldIngredient);
 		ingredients.add(newIngredient);
 		notifyViews();
 	}
 	
 	public void addIngredient(Ingredient ingredient) {
		ingredients.add(ingredient);
		notifyViews();
 	}
 	
 	public Ingredient getIngredient(String type) {
 		for (Ingredient ingredient : ingredients) {
 			if (ingredient.getType().equals(type)) {
 				return ingredient;
 			}
 		}
 		
 		return null;
 	}
 	
 	public boolean getOnServer(){
 		return onServer;
 	}
 	
 	public void setOnServer(boolean onServer){
 		this.onServer = onServer;
 		notifyViews();
 	}
 	
 	
 	
 }
