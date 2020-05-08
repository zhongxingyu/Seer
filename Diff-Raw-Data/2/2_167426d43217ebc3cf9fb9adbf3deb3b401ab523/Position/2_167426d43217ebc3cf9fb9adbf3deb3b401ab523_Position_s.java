 package fr.ickik.formulamath.entity;
 
 /**
  * The position is a couple of coordinates, an horizontal and a vertical. The
  * position indicates the situation on the map.
  * @author Ickik
  * @version 0.1.000
  */
 public class Position implements Cloneable {
 
 	private int x;
 	private int y;
 
 	/**
 	 * Default constructor, it defines the position at (0, 0).
 	 */
 	public Position() {
 		this(0, 0);
 	}
 
 	/**
 	 * Constructor to initialize the coordinates with the arguments.
 	 * @param x the horizontal coordinate.
 	 * @param y the vertical coordinate.
 	 */
 	public Position(int x, int y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	/**
 	 * Set the horizontal coordinate.
 	 * @param y the new horizontal coordinate.
 	 */
 	public void setX(int x) {
 		this.x = x;
 	}
 
 	/**
 	 * Return the horizontal coordinate.
 	 * @return the horizontal coordinate.
 	 */
 	public int getX() {
 		return x;
 	}
 
 	/**
 	 * Set the vertical coordinate.
 	 * @param y the new vertical coordinate.
 	 */
 	public void setY(int y) {
 		this.y = y;
 	}
 
 	/**
 	 * Return the vertical coordinate.
 	 * @return the vertical coordinate.
 	 */
 	public int getY() {
 		return y;
 	}
 
 	@Override
 	public Position clone() {
 		return new Position(x, y);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof Position) {
 			Position p = (Position) obj;
 			if (p.getX() == x && p.getY() == y) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public int hashCode() {
 		int hash = 23;
		hash *= x * y;
 		return hash;
 	}
 
 	@Override
 	public String toString() {
 		return "( " + Integer.toString(x) + " , " + Integer.toString(y) + " )";
 	}
 }
