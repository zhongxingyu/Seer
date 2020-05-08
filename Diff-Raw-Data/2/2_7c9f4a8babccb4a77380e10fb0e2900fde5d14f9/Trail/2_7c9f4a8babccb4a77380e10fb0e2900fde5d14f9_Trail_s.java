 package com.brousalis;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import android.graphics.Paint;
 
 import com.google.android.maps.Overlay;
 
 public class Trail {
 	
 	private Paint _linePaint;
 	private String _name;
 	private HashSet<TrailPoint> _trailPoints;
 	
 	public Trail(String name) {
 		_linePaint = new Paint();
 		_linePaint.setAntiAlias(true);
 		_linePaint.setARGB(255, 0, 255, 0);
 		_name = name;
 		_trailPoints = new HashSet<TrailPoint>();
 	}
 	
 	public void setName(String name) {
 		_name = name;
 	}
 	public String getName() {
 		return _name;
 	}
 
 	/**
 	 * Adds a List of TrailPoints to this trail.
 	 * It should be noted that the first point of 
 	 * this trail is NOT linked to any other point 
 	 * on this trail
 	 * @param trailPoints
 	 */
 	public void addLinkedPoints(LinkedList<TrailPoint> trailPoints) {
 		while(!trailPoints.isEmpty()) {
 			TrailPoint p = trailPoints.poll();
 			_trailPoints.add(p);
 			p.addConnection(trailPoints.peek());
 		}
 	}
 	
 	/**
 	 * Adds a List of TrailPoints to this trail.
 	 * It should be noted that the first point of 
 	 * this trail is NOT linked to any other point 
 	 * on this trail
 	 * @param trailPoints
 	 */
 	public void addLinkedPoints(LinkedList<TrailPoint> trailPoints, TrailPoint pOld) {
 		while(!trailPoints.isEmpty()) {
 			TrailPoint p = trailPoints.poll();
 			_trailPoints.add(p);
 			p.addConnection(trailPoints.peek());
 		}
 	}
 	
 	/**
 	 * Returns the number of points in this trail
 	 * @return The number of points in this trail
 	 */
 	public int getNumberOfTrailPoints() {
 		return _trailPoints.size();
 	}
 
 	/**
 	 * Adds a new TrailPoint to this Trail and
 	 * Adds a new connection from pOld to pNew
 	 * @param pNew The new TrailPoint to add
 	 * @param pOld The existing TrailPoint to form the connection from
 	 */
 	public boolean addPoint(TrailPoint pNew, TrailPoint pOld) {
 		if(hasPoint(pNew))
 			return false;
 		_trailPoints.add(pNew);
 		if(hasPoint(pOld))
 			pOld.addConnection(pNew);
 		return true;
 	}
 
 	/**
 	 * Adds a new TrailPoint to this Trail
 	 * @param point The new TrailPoint to add
 	 */
 	public void addPoint(TrailPoint point) {
 		_trailPoints.add(point);
 	}
 
 	/**
 	 * Removes a TrailPoint if it is in the current trail
 	 * @param point The TrailPoint to remove
 	 */
 	public void removePoint(TrailPoint point) {
 		_trailPoints.remove(point);
 	}
 	
 	/**
 	 * Gets a specific TrailPoint based on an ID.
 	 * After Testing, this method will be made private
 	 * @param id The ID of the TrailPoint
 	 * @return A TrailPoint if it exists, or null
 	 */
 	public TrailPoint getTrailPoint(int id) {
 		Iterator<TrailPoint> iter = _trailPoints.iterator();
 		while(iter.hasNext()) {
 			TrailPoint current = iter.next();
 			if (current.getID() == id) {
 				return current;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the Set of all TrailPoints
 	 * @return The Set of all TrailPoints
 	 */
 	public Collection<? extends Overlay> getTrailPoints() {
 		return _trailPoints;
 	}
 
 	/**
 	 * Determines if this Trail does or does not contain a specific point
 	 * @param point The TrailPoint to check the list against
 	 * @return True if the Point is contained within the list
 	 */
 	public boolean hasPoint(TrailPoint point) {
 		return _trailPoints.contains(getTrailPoint(point.getID()));
 	}
 }
