 package se.chalmers.kangaroo.model;
 
 import java.awt.Polygon;
 
 import se.chalmers.kangaroo.constants.Constants;
 
 /**
  * A class for representing a tile. Every tile has an ID representing the
  * collidable state.
  * 
  * @author alburgh
  * 
  */
 public class Tile {
 	private int id;
 	private boolean collidable;
 	private Polygon poly;
 
 	/**
 	 * Creates a tile with the given id.
 	 * 
 	 * @param a
 	 */
 	public Tile(int i, int x, int y) {
 		this.id = i;
		collidable = Constants.COLLIDE_IDS.contains(" " + id + " ");
 		int xRec[] = {x*32+1,x*32+32,x*32+32,x*32+1};
 		int yRec[] = {y*32+1,y*32+1,y*32+32,y*32+32};
 		poly = new Polygon( xRec , yRec , 4 );
 	}
 
 	/**
 	 * Returns whether the tile is collidable or not.
 	 * 
 	 * @return true if the tile is collidable
 	 */
 	public boolean isCollidable() {
 		return collidable;
 	}
 
 	/**
 	 * Returns the char which represents the id of the tile.
 	 * 
 	 * @return the id of the tile
 	 */
 	public int getId() {
 		return id;
 	}
 	/**
 	 * Returns the polygon representing the size.
 	 * @return the polygon
 	 */
 	public Polygon getPolygon(){
 		return poly;
 	}
 	/**
 	 * Make it possible for extensions to change ID.
 	 * @param i, the new ID
 	 */
 	protected void changeId(int i){
 		id = i;
 	}
 	/**
 	 * Make it possible for extensions to toggle collidable.
 	 */
 	protected void toggleCollidable(){
 		collidable = !collidable;
 	}
 	/**
 	 * @return an int representing the hashCode
 	 */
 	@Override
 	public int hashCode() {
 		return super.hashCode() * 7 * id * 37;
 	}
 	/**
 	 * @return true if obj is a Tile and the id is the same. 
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Tile other = (Tile) obj;
 		if (id != other.id)
 			return false;
 		return true;
 	}
 
 	/**
 	 * A string representing a tile.
 	 */
 	@Override
 	public String toString() {
 		return "Id: " + id + " Collidable: " + collidable;
 	}
 
 }
