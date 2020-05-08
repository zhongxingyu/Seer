 /**
  * 
  */
 package edu.rit.se.sse.rapdevx.clientmodels;
 
 import java.util.List;
 
 import edu.rit.se.sse.rapdevx.api.dataclasses.ShipClass;
 import edu.rit.se.sse.rapdevx.api.dataclasses.ShipPlacement;
 import edu.rit.se.sse.rapdevx.api.dataclasses.Unit;
 
 /**
  * @author Cody Krieger
  * 
  */
 public class Ship extends Unit implements Cloneable {
 	private int	X;
 	private int	Y;
 
 	public Ship() {
 	}
 
 	/**
 	 * @return the x
 	 */
 	public int getX() {
 		return X;
 	}
 
 	/**
 	 * @param x
 	 *            the x to set
 	 */
 	public void setX(int x) {
 		X = x;
 	}
 
 	/**
 	 * @return the y
 	 */
 	public int getY() {
 		return Y;
 	}
 
 	/**
 	 * @param y
 	 *            the y to set
 	 */
 	public void setY(int y) {
 		Y = y;
 	}
 
 	/**
 	 * Convenience method for getting a Unit's ShipClass.
 	 * 
 	 * @return A ShipClass instance.
 	 */
 	public ShipClass getShipClass() {
 		List<ShipClass> classes = AssetLibrary.getShipClasses();
 		for (ShipClass c : classes) {
 			if (c.getGid().equals(this.getClassid()))
 				return c;
 		}
 
 		return null;
 	}
 
 	/**
 	 * Convenience method for getting a ship's class name.
 	 * 
 	 * @return The class name of the ship.
 	 */
 	public String getClassName() {
 		ShipClass sc = getShipClass();
 		if (sc != null)
 			return "Awesome Ship!";
 
 		return "Unknown Class";
 	}
 
 	/**
 	 * Generate a ShipPlacement based on this Ship's information.
 	 * 
 	 * @return A newly generated ShipPlacement.
 	 */
 	public ShipPlacement getShipPlacement() {
 		ShipPlacement placement = new ShipPlacement();
 
 		placement.setClassid(this.getClassid());
 		placement.setX(this.X);
 		placement.setY(this.Y);
 
 		return placement;
 	}
 
 	/**
 	 * Convenience method for getting the max HP of a Ship's ShipClass.
 	 * 
 	 * @return The max HP.
 	 */
 	public int getMaxHp() {
 		ShipClass sc = getShipClass();
 		if (sc != null)
 			return sc.getMaxhp();
 
 		return 100;
 	}
 	
 	//TODO implement completely
 	public Ship clone() {
 		Ship ship = new Ship();
 		ship.setX(X);
 		ship.setY(Y);
 		ship.setHp(getHp());
 		
 		return ship;
 	}
 }
