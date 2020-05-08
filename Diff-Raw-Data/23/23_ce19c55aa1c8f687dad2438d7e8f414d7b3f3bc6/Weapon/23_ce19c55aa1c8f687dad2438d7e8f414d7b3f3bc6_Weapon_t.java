 package rsmg.model.object.item;
 
 
 public class Weapon extends Item {
 	
 	/**
 	 * @param x horizontal coordinate for where the Item location
 	 * @param y vertical coordinate for where the Item location
 	 * @param width width of the item
 	 * @param height height of the item
 	 */
 	public Weapon(double x, double y, double width, double height, String weponType) {
 		super(x, y+32-height, width, height, weponType);
 	}
 }
