 package summative2013.memory;
 
 /**
  * A memory that describes the knowledge of the existence of something
  * at some location, and the location's terrain
  * @author quincy
  */
 
 import java.awt.Point;
 import summative2013.Summative;
 import summative2013.lifeform.Lifeform;
 import summative2013.lifeform.Animal;
 
 public class LocationMemory extends Memory {
 	/**
 	 * Location remembered
 	 */
 	private Point location;
 	/**
 	 * Object at location. May be null.
 	 */
 	private Lifeform object;
 	/**  
 	 * Terrain at location
 	 */
	private Summative.Terrain terrain;
 	/**
 	 * Increases or decreases both coordinates by a random amount in 
 	 * [-CORRUPTION_DELTA, CORRUPTION_DELTA]
 	 */
 	public void corrupt() {
 		location.x  = corruptNumber(location.x);
 		location.y =corruptNumber(location.y);
 		
 		tryForget();
 	}
 	/**
 	 * Probably doesn't affect the animal directly.
 	 * @param a  The animal to affect, probably the owner of this memory
 	 */
 	public void affect(Animal a) {
 		
 	}
 	
 	/**
 	 * 
 	 * @return If the terrain of the location being remembered is null
 	 */
 	public boolean isForgotten() {
 		return terrain == null;
 	}
 	
 	protected void forget() {
 		terrain = null;
 	}
 }
