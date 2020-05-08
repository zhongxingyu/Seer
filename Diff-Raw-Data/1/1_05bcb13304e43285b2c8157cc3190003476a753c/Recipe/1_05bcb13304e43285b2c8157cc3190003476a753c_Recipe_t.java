 package ca.ualberta.team2recipefinder;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class Recipe implements Serializable
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private String name;
 	private String procedure;
 	private String author;
 	private List<Ingredient> ingredients;
 	private boolean onServer;
 	private long id;
 	
 	public Recipe() {
 		name = new String();
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
 	}
 	
 	public String getProcedure(){
 		return procedure;
 	}
 	
 	public void setProcedure(String procedure){
 		this.procedure = procedure;
 	}
 	
 	public String getAuthor(){
 		return author;
 	}
 	
 	public void setAuthor(String author){
 		this.author = author;
 	}
 	
 	public List<Ingredient> getIngredients(){
 		return ingredients;
 	}
 	
 	public void setIngredients(List<Ingredient> ingredients){
 		this.ingredients = ingredients;
 	}
 	
 
 	
 	public boolean getOnServer(){
 		return onServer;
 	}
 	
 	public void setOnServer(boolean onServer){
 		this.onServer = onServer;
 	}
 	
 	
 	
 }
