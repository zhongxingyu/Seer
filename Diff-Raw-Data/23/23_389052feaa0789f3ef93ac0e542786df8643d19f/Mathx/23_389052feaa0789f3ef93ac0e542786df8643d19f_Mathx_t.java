 package utils;
 
import model.MainModel;
 import model.core.Coordinate;
 import model.elements.GameElement;
 
 public class Mathx {
 
 	/**
 	 * @param milliseconds Time in milliseconds
 	 * @return The given time as a String formatted nicely in minutes and seconds
 	 */
 	static public String prettyTime(long milliseconds) {
 		double totalSeconds = milliseconds / 1000;
 		
 		int seconds = (int) (totalSeconds % 60);
 		int minutes = (int) ((totalSeconds - seconds) / 60);
 
 		String secondString = seconds < 10 ? "0" + seconds : String.valueOf(seconds);
 		String minuteString = minutes < 10 ? "0" + minutes : String.valueOf(minutes);
 		
 		return String.format("%s:%s", minuteString, secondString);
 	}
 	/**
 	 * @param n
 	 *            The number to check
 	 * @param min
 	 *            Minimum value for validation
 	 * @param max
 	 *            Maximum value for validation
 	 * @return True if n is inbetween (inclusive) min and max
 	 */
 	static public boolean between(double n, double min, double max) {
 		return n > min && n < max;
 	}
 
 	/**
 	 * @param timeDelta
 	 *            Time passed since last frame
 	 * @param speed
 	 *            How fast is the object moving
 	 * @return A distance dependant on time, thereby avoiding glitches that make
 	 *         movement unstable
 	 */
	static public double distance(long timeDelta, double speed) {
		return timeDelta / MainModel.FRAMERATE * speed / 2.0;
 	}
 
 	/**
 	 * Determine whether two game elements are intersecting, depending on their
 	 * size and position.
 	 * 
 	 * @return True if a and b intersects
 	 */
 	static public boolean intersects(GameElement a, GameElement b) {
 		Coordinate ap = a.getPosition();
 		Coordinate bp = b.getPosition();
 		double ax1 = ap.x, ax2 = ap.x + a.getWidth();
 		double ay1 = ap.y, ay2 = ap.y + a.getHeight();
 		double bx1 = bp.x, bx2 = bp.x + b.getWidth();
 		double by1 = bp.y, by2 = bp.y + b.getHeight();
 
 		return ax1 < bx2 && ax2 > bx1 && ay1 < by2 && ay2 > by1;
 	}
 	
 	static public boolean circleRectangleIntersects(GameElement rect, Coordinate circ, double radius){
 		double rectCenterX = rect.getPosition().x + ((double) rect.getWidth()/2);
 		double rectCenterY = rect.getPosition().y + ((double) rect.getHeight()/2);
 
 		double circleDistanceX = Math.abs(rectCenterX-circ.x);
 		double circleDistanceY = Math.abs(rectCenterY-circ.y);
 		
 		if (circleDistanceX > (rect.getWidth()/2 + radius)) { return false; }
 	    if (circleDistanceY > (rect.getHeight()/2 + radius)) { return false; }
 	    
 	    if (circleDistanceX <= (rect.getWidth()/2)) { return true; } 
 	    if (circleDistanceY <= (rect.getHeight()/2)) { return true; }
 	    
 	    double cornerDistance_squared = Math.pow((circleDistanceX - rect.getWidth()/2),2) +
                 Math.pow((circleDistanceY - rect.getHeight()/2), 2);
 
 		return (cornerDistance_squared <= Math.pow(radius,2));
 	}
 
 	public static Coordinate angle(Coordinate a, Coordinate b) {
 		return new Coordinate(a.x - b.x, a.y - b.y);
 	}
 	
 	//TODO perhaps make an intersects-method for circles like the one for rectangles
 }
