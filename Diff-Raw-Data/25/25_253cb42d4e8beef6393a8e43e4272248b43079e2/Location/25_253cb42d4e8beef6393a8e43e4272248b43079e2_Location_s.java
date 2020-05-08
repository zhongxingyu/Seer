 package order;
 
 /**
  * Data class met informatie over product locaties
  * @author timpotze
  *
  */
 public class Location {
 	
 	/**
 	 * X coordinaat van location
 	 */
 	public int x;
 	/**
 	 * Y coordinaat van location
 	 */
 	public int y;
 
 	/**
 	 * 
 	 * @param x coordinaat
 	 * @param y coordinaat
 	 */
 	public Location(int x, int y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	/**
 	 * Vergelijk deze Location met toLocation
 	 * @param toLocation Location om mee te vergelijken
 	 * @return afstand
 	 */
 	public float getDistanceTo(Location toLocation) {
 		
 		//Pitaguras ofzo
 		return (float) Math.sqrt(Math.pow(Math.abs(toLocation.x - x), 2)
 				+ Math.pow(Math.abs(toLocation.y - y), 2));
 	}
 }
