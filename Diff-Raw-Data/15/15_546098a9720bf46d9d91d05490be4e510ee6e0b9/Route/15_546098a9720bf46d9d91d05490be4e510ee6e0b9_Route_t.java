 package gps.dao;
 
import java.util.Vector;

 /**
  * Stores a list of GpsPoints and a name and ID
  * @author nathan
  */
 
 public class Route {
	private Vector points = new Vector();
 	private String name;
 	private int id;
 
 	/**
 	 * Sets all points at the same time
 	 * @param points
 	 */
	public void setPoints(Vector points) {
 		this.points = points;
 	}
 	
 	/**
 	 * adds a single point to the route
 	 * @param point
 	 */
 	public void addPoint(GpsPoint point){
		points.addElement( point );
 	}
 
	public Vector getPoints() {
 		return points;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public int getId() {
 		return id;
 	}
 	
 }
