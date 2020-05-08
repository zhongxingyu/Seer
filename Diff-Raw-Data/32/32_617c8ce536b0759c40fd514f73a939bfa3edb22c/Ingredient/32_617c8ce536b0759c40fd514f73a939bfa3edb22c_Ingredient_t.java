 package ca.ualberta.cs.oneclick_cookbook;
 
 public class Ingredient {
 	private int quantity;
 	private String measurement;
 	private String name;
 	
 	Ingredient (String name, int quantity, String measurement) {
 		this.name = name;
 		this.quantity = quantity;
 		this.measurement = measurement;
 	}
 	
 	public void modifyQuantity (int quantity, String measurement) {
 		this.quantity = quantity;
 		this.measurement = measurement;
 	}
 	
 	public String getName() {
 		return name;
 	}
	
	public void setName(String newName){
		this.name = newName;
	}
 
 	public int getQuantity() {
 		return quantity;
 	}
 
 	public String getMeasurement() {
 		return measurement;
 	}
 	
 	// This should probably be modified to return a string
 	public void printIngredient() {
 		return;
 	}
 
 }
