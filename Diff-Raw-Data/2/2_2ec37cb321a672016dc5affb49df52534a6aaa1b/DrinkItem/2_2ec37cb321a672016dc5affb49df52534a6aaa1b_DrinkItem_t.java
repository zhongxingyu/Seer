 package uk.ac.aber.dcs.cs12420.aberpizza.data;
 
 public class DrinkItem extends AbstractItem {
 	
 	
 	
 	public DrinkItem() {
 		setDescription("Default drink");
 	}
 	
 	public String toString() {
		String s = "Drink, " + getDescription() + ", " + getSize() + "L , " +
 				getPrice();
 		return s;
 	}
 	
 }
