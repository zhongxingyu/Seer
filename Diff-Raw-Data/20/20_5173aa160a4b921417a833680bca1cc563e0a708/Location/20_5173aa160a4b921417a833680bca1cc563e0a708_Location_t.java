 package order;
 
 /**
  * Data class met informatie over product locaties
  * @author timpotze
  *
  */
 public class Location {
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + x;
 		result = prime * result + y;
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Location other = (Location) obj;
 		if (x != other.x)
 			return false;
 		if (y != other.y)
 			return false;
 		return true;
 	}
 
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
	
	@Override
	public String toString() {
		return x + ", " + y;
	}
 }
