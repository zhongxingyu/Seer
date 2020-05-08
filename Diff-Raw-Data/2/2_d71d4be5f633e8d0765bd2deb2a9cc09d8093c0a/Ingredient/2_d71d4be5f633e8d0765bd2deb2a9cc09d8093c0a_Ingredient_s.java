 package ca.ualberta.cmput301w13t11.FoodBook.model;
 
 import android.content.ContentValues;
 
 /**
  * Class which models an ingredient (as part of a recipe, or in the User's MyIngredients Db).
  * @author Marko Babic
  *
  */
 public class Ingredient {
 
 	private String name;
 	private String unit;
 	private float quantity;
 	
 	/**
 	 * Constructor -- creates an Ingredient object from the given parameters.
 	 * @param name The name of the Ingredient.
 	 * @param unit The unit type of the Ingredient.
 	 * @param quantity The quantity of this Ingredient.
 	 */
 	public Ingredient(String name, String unit, float quantity)
 	{
 		this.name = name;
 		this.unit = unit;
 		this.quantity = quantity;
 	}
 	
 	public String getName()
 	{
 		return this.name;
 	}
 	
 	public String getUnit()
 	{
 		return this.unit;
 	}
 	
 	public float getQuantity()
 	{
 		return this.quantity;
 	}
 	
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 	
 	public void setUnit(String unit)
 	{
 		this.unit = unit;
 	}
 	
 	public void setQuantity(float quantity)
 	{
 		this.quantity = quantity;
 	}
 
     /**
      * Converts an Ingredient object to a ContentValues object to be stored in the database.
      * @param ingred The ingredient to be transformed.
      * @return An appropriately transformed cop of the Ingredient for database storage.
      */
     public ContentValues toContentValues() {
         ContentValues values = new ContentValues();
         values.put("name", name);
         values.put("unit", unit);
         values.put("quantity", quantity);
         return values;
     }
 	
     /**
      * Returns a string representation of the object
      */
     public String toString(){
    	return unit + " " + quantity + " " + name ;
     }
 }
