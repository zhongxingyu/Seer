 package lab08;
 
 public class Wall {
 	Square northWest = new Square ();
 	Orientation orientation = null; 
 
 	public Wall(Square northWest, Orientation orientation) {
 		this.northWest = northWest;
 		this.orientation = orientation;
 	}
 
     public Wall(String s) {
    	// TODO Should probably have preconditions to check the string is valid though that may be the job of isValidMove
     	if (s.length() > 2) {
         	this.northWest = new Square (s.substring(0, 2));
         	this.orientation = s.charAt(2) == 'h' ? Orientation.HORIZONTAL : Orientation.VERTICAL;
         }
     }
 	
 	public Square getNorthWest() {
 		return northWest;
 	}
 
 	public Orientation getOrientation() {
 		return orientation;
 	}
 	
 }
