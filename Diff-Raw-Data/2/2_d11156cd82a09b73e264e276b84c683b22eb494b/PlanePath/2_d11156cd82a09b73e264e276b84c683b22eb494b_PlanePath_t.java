 package airplane.g2.waypoint;
 
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 
 import airplane.sim.Plane;
 
 public class PlanePath {
 	
 	double INTERSECTION_DISTANCE = 10.1;
 	private Integer segmentForLastBearingRequest = -1;
 	
 	ArrayList<Point2D.Double> waypoints;
 	private Plane plane;
 	int startTimestep;
 	
 	public PlanePath(Plane p) {
 		startTimestep = p.getDepartureTime();
 		
 		waypoints = new ArrayList<Point2D.Double>();
 		waypoints.add(p.getLocation());
 		waypoints.add(p.getDestination());
 		
 		setPlane(p);		
 	}
 	
 	public PlanePath(PlanePath oldPath) {
 		plane = oldPath.getPlane();
 		startTimestep = oldPath.getStartTimestep();
 		waypoints = new ArrayList<Point2D.Double>(oldPath.waypoints);
 	}
 	
 	public void delay(int amount) {
 		startTimestep += amount;
 	}
 
 	public String toString() {
 		String output = String.format("\nPlane %d path:\n", getPlaneId());
 		for (Point2D.Double point : waypoints) {
 			output += point + "\n";
 		}
 		output += "\n";
 		return output;
 	}
 	
 	public void appendWaypoint(Point2D.Double point) {
 		addWaypoint(waypoints.size() - 1, point);
 	}
 	
 	public void addWaypoint(int index, Point2D.Double point) {
 		waypoints.add(index, point);
 	}
 	
 	public int getStartTimestep() {
 		return startTimestep;
 	}
 	
 	public PlaneCollision getPlaneCollision(PlanePath otherPath) {
 		PlaneCollision collision = new PlaneCollision();
 		collision.setPath1(this);
 		collision.setPath2(otherPath);
 		for (int i = Math.max(startTimestep, otherPath.getStartTimestep()); i <= Math.min(getArrivalStep(), otherPath.getArrivalStep()); i++) {
 			PlanePathPosition thisPathPos = getPathPosition(i);
 			PlanePathPosition otherPathPos = otherPath.getPathPosition(i);
 			Point2D.Double thisPos = thisPathPos.getPosition();
 			Point2D.Double otherPos = otherPathPos.getPosition();
 			
 			if (thisPos.distance(otherPos) < INTERSECTION_DISTANCE) {
 				collision.setRound(i);
 				collision.setCollisionPoint(thisPos);
 				collision.setPlane1segment(thisPathPos.getSegment());
 				collision.setPlane2segment(otherPathPos.getSegment());
 				
 				return collision;
 			}
 		}
 		return null;
 	}
 	
 	public Point2D.Double getCollisionPoint(PlanePath otherPath) {
 		return getPlaneCollision(otherPath).getCollisionPoint();
 	}
 	
 	public Double getArrivalStepRaw() {
 	int totaltime = startTimestep;
 		int segment = 0;
 		
 		double distance = waypoints.get(segment).distance(waypoints.get(segment + 1));
 		while(segment < waypoints.size() - 2) {
 			totaltime += distance;
 			segment++;
 			distance = waypoints.get(segment).distance(waypoints.get(segment + 1));
 		}
 		
 		return totaltime + distance;
 	}
 	
 	public int getArrivalStep() {
 		return getArrivalStepRaw().intValue() + 1;
 	}
 	
 	
 	//TODO Get this working.
 	private void smoothCurves() {
 		int currentIndex = 1;
 		while (currentIndex < waypoints.size() - 1) {
 			
 			double bearingIn = calculateBearing(waypoints.get(currentIndex - 1), waypoints.get(currentIndex));
 			double bearingOut = calculateBearing(waypoints.get(currentIndex), waypoints.get(currentIndex + 1));
 			
 			double deltaBearing = Math.abs(bearingIn - bearingOut);
 			
 			if (Plane.MAX_BEARING_CHANGE >  deltaBearing) {
 				double halfDeltaBearing = deltaBearing/2 + 5;
 				
 				double bearingInTemp = halfDeltaBearing;
 				Point2D.Double endPoint = waypoints.get(currentIndex);
 				Point2D.Double startPoint = waypoints.get(currentIndex);
 				
 				while(bearingInTemp > Plane.MAX_BEARING_CHANGE) {
 					double dx = endPoint.x - startPoint.x;
 					double dy = endPoint.y -startPoint.y;
 					
 					
 					
 					double norm = Math.sqrt(dx*dx + dy*dy);
 					
 					dx = dx/norm;
 					dy = dy/norm;
 					
 					//addWaypoint(currentIndex, Point2D.Double())
 				}
 				
 				//TODO
 			}
 			
 			
 		}
 	}
 	
 	public double getBearing(int timestep) {
 		return getBearing(getPlane(), timestep);
 	}
 	
 	public double getBearing(Plane aPlane, int timestep) {
 		setSegmentForLastBearingRequest(-1);
 		
 		if (timestep < startTimestep) return -1;
 		if (timestep > getArrivalStep()) return calculateBearing(aPlane.getLocation(), waypoints.get(waypoints.size() - 1));
 		int savedTime = startTimestep;
 		int segment = 0;
 		
 		double distance = waypoints.get(segment).distance(waypoints.get(segment + 1));
		while(savedTime + distance + 1.5 < timestep && segment < waypoints.size() - 2) {
 			savedTime += distance;
 			segment++;
 			distance = waypoints.get(segment).distance(waypoints.get(segment + 1));
 		}
 		
 		setSegmentForLastBearingRequest(segment);
 		
 		return calculateBearing(aPlane.getLocation(), waypoints.get(segment + 1));
 		
 	}
 	
 	public PlanePathPosition getPathPosition(int timestep) {
 		if (timestep < startTimestep) return null;
 		if (timestep > getArrivalStep() + 1) return null;
 		int savedTime = startTimestep;
 		int segment = 0;
 		
 		double distance = waypoints.get(segment).distance(waypoints.get(segment + 1));
 		while(savedTime + distance + 0.5 < timestep && segment < waypoints.size() - 2) {
 			savedTime += distance;
 			segment++;
 			distance = waypoints.get(segment).distance(waypoints.get(segment + 1));
 		}
 		
 		double dx = waypoints.get(segment + 1).x - waypoints.get(segment).x;
 		double dy = waypoints.get(segment + 1).y - waypoints.get(segment).y;
 		
 		double norm = Math.sqrt(dx*dx + dy*dy);
 		
 		dx = dx/norm;
 		dy = dy/norm;
 		
 		PlanePathPosition pos = new PlanePathPosition();
 		pos.setPosition(new Point2D.Double(waypoints.get(segment).x + dx * (timestep - savedTime), waypoints.get(segment).y + dy * (timestep - savedTime)));
 		pos.setSegment(segment);
 		return pos;
 	}
 	
 	public Point2D.Double getPosition(int timestep) {
 		PlanePathPosition pos = getPathPosition(timestep);
 		return pos == null ? null : pos.getPosition();
 	}
 
 	/*
 	 * This is a helper method that will calculate the bearing between two points
 	 */
     private static double calculateBearing(Point2D.Double p1, Point2D.Double p2) {
 
 		double dx = p1.x - p2.x;
 		double dy = p1.y - p2.y;
 
 		double bearing = Math.atan2(dy, dx) * 180 / Math.PI - 90;
 
 		if (bearing < 0) bearing += 360;
 
 		return bearing;
     }
 
 	public Plane getPlane() {
 		return plane;
 	}
 
 	public void setPlane(Plane plane) {
 		this.plane = plane;
 	}
 
 	public Integer getSegmentForLastBearingRequest() {
 		return segmentForLastBearingRequest;
 	}
 
 	public void setSegmentForLastBearingRequest(
 			Integer segmentForLastBearingRequest) {
 		this.segmentForLastBearingRequest = segmentForLastBearingRequest;
 	}
 	
 	public Point2D.Double waypointAt(int index) {
 		return waypoints.get(index);
 	}
 	
 	public Line2D.Double segmentAt(int index) {
 		return new Line2D.Double(waypointAt(index), waypointAt(index + 1));
 	}
 	
 	public Integer getPlaneId() {
 		return getPlane().id;
 	}
 }
