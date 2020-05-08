 package base;
 
 public class Seat implements Structure {
 	Person occupant;
 	int x,y;
 	int angle;
 	
 	public Seat(int x, int y, Person person) {
 		this.x = x;
 		this.y = y;
 		occupant = person;
 	}
 	public Seat(int x, int y) {
 		this(x,y,null);
 	}
 
 	public void setX(int x) {
 		this.x = x;
 	}
 
 	public void setY(int y) {
 		this.y = y;
 	}
 
 	public void setAngle(int angle) {
 		this.angle = angle;
 	}
 	
 	public void setOccupant(Person occupant) {
 		this.occupant = occupant;
 	}
 	public void removeOccupant() {
 		occupant = null;
 	}
 
 	public int getX() {
 		return x;
 	}
 	public int getY() {
 		return y;
 	}
 	public int getAngle() {
 		return angle;
 	}
 
 	public Person getOccupant() {
 		Person result = null;
 		if(occupant != null) {
 			result = occupant;
 		}
 		return result;
 	}
 	public boolean isOccupied() {
 		return occupant != null;
 	}
 	public String toString() {
 		String occString = "";
 		if(isOccupied()) {occString = "{"+occupant+"}";}
 		return "("+x+","+y+")["+angle+"o]" + occString;
 	}
 }
