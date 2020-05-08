 package ca.dreamteam.newrecipebook.Models;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import android.graphics.Bitmap;
 
 public class Recipe implements Serializable {
     
 	private static final long serialVersionUID = -3419759999296394802L;
 	public long id;
 	public String name;
 	public String author;
 	public ArrayList<Bitmap> photos;
 	public ArrayList<String> ingredients;
 	public String instructions;
 	public ArrayList<String> tags;
 	public ArrayList<String> comments;
 	public Boolean favorited;
 	public int favorite;
 
 	
 	public void uploadPhoto(Bitmap newPhoto)
 	{
 		//TODO
 	}
 
 	public void setName(String name){
 		this.name = name;
 	}
 
 	public String getName(){
 		return name;
 	}
 
 	public void setAuthor(String author){
 		this.author = author;
 	}
 
 	public String getAuthor(){
 		return author;
 	}
 
 	public ArrayList<String> getIngredients(){
 		return ingredients;
 	}
 
 	public void setIngredients (ArrayList<String> ingredients){
 		this.ingredients = ingredients;
 	}
 
 	public void addIngredient(Ingredient ingredient)
 	{
 		this.ingredients.add(ingredient.getName());
 	}
 
 	public void setId(long id){
 		this.id = id;
 	}
 
 	public long getId(){
 		return id;
 	}
 
 	@Override
 	public String toString()
 	{
		return "Recipe [id=" + id + ", author=" + author + ", name=" + name + ", ingredients="
				+ ingredients + ", instructions=" + instructions + "]";
 	}
 
 	public String toEmailString()
 	{
 		return "<p>" + this.name + "</p>" + "<p>Author: " + this.author
 		        + "</p>" + "<p>Ingredients: " + this.ingredients + "</p>"
 		        + "<p>Instructions: " + this.instructions + "</p>";
 	}
 
 	public void addIngredient(String ingredient) {
 		ingredients.add(ingredient); 	
 	}
 
 	public void addInstructions(String instructions) {
 		this.instructions = instructions;
 	}
 	
 	public String setInstructions(){
 	    return instructions;
 	}
 }
