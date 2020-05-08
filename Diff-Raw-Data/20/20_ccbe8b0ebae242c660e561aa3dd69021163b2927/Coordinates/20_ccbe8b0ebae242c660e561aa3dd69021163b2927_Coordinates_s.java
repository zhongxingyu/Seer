 package internal.space;
 
 import java.io.Serializable;
 
 /**
  * The Coordinates class is used to store coordinates that are used in Space
  * @author guru
  *
  */
 public class Coordinates implements Serializable {
 	private static final long serialVersionUID = 2109376945752939066L;
 	
 	protected boolean cartesian;
 	public int x, y;
 	
 	public Coordinates(boolean cartesian, int x, int y) {
 		this.cartesian	= cartesian;
 		this.x			= x;
 		this.y			= y;
 	}
 	
 	public Coordinates(Coordinates coordinates) {
 		this.x 			= coordinates.x;
 		this.y			= coordinates.y;
 		this.cartesian	= coordinates.cartesian;
 	}
 	
 	/**
 	 * Identify whether this Coordinates object is in Cartesian format
 	 * @return {@code true} if this Coordinates object is in Cartesian format<br>
 	 * {@code false} otherwise
 	 */
 	public boolean cartesian() {
 		return cartesian;
 	}
 	
 	/**
 	 * Obtain the x-coordinate from this {@code Coordinates} object<br>
 	 * This method does not convert from array to Cartesian form or vice-versa
 	 * @return {@code Integer} representing the x-coordinate
 	 */
 	public int x() {
 		return x;
 	}
 	
 	
 	/**
 	 * Obtain the y-coordinate from this {@code Coordinates} object<br>
 	 * This method does not convert from array to Cartesian form or vice-versa
 	 * @return {@code Integer} representing the y-coordinate
 	 */
 	public int y() {
 		return y;
 	}
 	
 	@Override
 	public String toString() {
 		if(cartesian)
 			return "C(" + x + "," + y + ")";
 		else
 			return "A(" + y + "," + x + ")";
 	}
 	
 	public static Coordinates stringToArray(String string) {
 		string = string.replace("(", "");
 		String[] strings = string.split(",");
 		int x = Integer.parseInt(strings[0]);
 		int y = Integer.parseInt(strings[1]);
 		Coordinates coords = new Coordinates(false, x, y);
 		return coords;
 	}
 }
