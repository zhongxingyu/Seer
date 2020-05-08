 public class Exit {
	/* self-explanatory. this class only recognizes the following strings as valid
	   long directions. */
 	public static String acceptedDirections[] = {"north", "east", 
 		"west", "south", "up", "down", "in", "out", "northeast", 
 		"northwest", "southeast", "southwest"};
	/* self-explanatory. this class only recognizes the following strings as valid
	   short directions. */
 	public static String acceptedShortDirections[] = {"n", "e", "w", "s", "u", "d",
 		"i", "o", "ne", "nw", "se", "sw"};
 	private String direction;
 	private String shortDirection;
 	private Location exitLocation;
 	/* setting this to true results in blocking off the exit 
 	 * until you decide to set it to true */
 	private boolean blocked = false;
 	
 	public Exit() {
 		
 	}
 	
 	/* checks the direction argument to the ones defined in acceptedDirections.
 	 * if they match, the direction and its corresponding short direction
 	 * is stored in their member variables and the loop breaks. */
 	public Exit(String direction, Location exitLocation) {
 		this.exitLocation = exitLocation;
 		
 		for (int i = 0; i < acceptedDirections.length; i++) {
 			if (direction.equals(acceptedDirections[i])) {
 				this.direction = direction.toLowerCase();
 				shortDirection = acceptedShortDirections[i].toLowerCase();
 				break;
 			}
 		}
 	}
 	
 	// same as before, adding the blocked boolean as an argument.
 	public Exit(String direction, Location exitLocation, boolean blocked) {
 		this.exitLocation = exitLocation;
 		this.blocked = blocked;
 		
 		for (int i = 0; i < acceptedDirections.length; i++) {
 			if (direction.equals(acceptedDirections[i])) {
 				this.direction = direction.toLowerCase();
 				shortDirection = acceptedShortDirections[i].toLowerCase();
 			}
 		}
 	}
 	
 	public String getDirection() {
 		return direction;
 	}
 	
 	
 	public String getShortDirection() {
 		return shortDirection;
 	}
 	
 	public Location getExitLocation() {
 		return exitLocation;
 	}
 	
 	public boolean isBlocked() {
 		return blocked;
 	}
 	
 	public void setBlocked(boolean blocked) {
 		this.blocked = blocked;
 	}
 }
