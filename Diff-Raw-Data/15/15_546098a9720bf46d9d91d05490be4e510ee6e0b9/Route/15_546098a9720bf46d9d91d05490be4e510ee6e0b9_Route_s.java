 package gps.dao;
 
 /**
  * Stores a list of GpsPoints and a name and ID
  * @author nathan
  */
 
 public class Route {
	private GpsPoint[] points;
 	private String name;
 	private int id;
 
 	/**
 	 * Sets all points at the same time
 	 * @param points
 	 */
	public void setPoints(GpsPoint[] points) {
 		this.points = points;
 	}
 	
 	/**
 	 * adds a single point to the route
 	 * @param point
 	 */
 	public void addPoint(GpsPoint point){
		//this.points[this.points.length] = point;
		
 	}
 
	public GpsPoint[] getPoints() {
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
