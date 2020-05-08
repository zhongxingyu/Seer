 package com.rt.core;
 
 import java.util.ArrayList;
 
 public class MapDataManager {
 	//	Need to find suitible distance threshold
 	private static final double DIST_THRESHOLD = 0.5f;
 
 	private ArrayList<Waypoint> waypoints;
 	private ArrayList<Leg> legs;
 	public Position currentPos;
 
 	public MapDataManager() {
 		waypoints = new ArrayList<Waypoint>();
 		legs = new ArrayList<Leg>();
 		updatePosition();
 	}
 
 	public ArrayList<Waypoint> getWaypoints() {
 		return waypoints;
 	}
 
 	public ArrayList<Leg> getLegs() {
 		return legs;
 	}
 
 	public void addWaypoint(Waypoint w) {
 		waypoints.add(w);
 
 		if(w.legA != null) {
 			if(!legs.contains(w.legA)) {
 				legs.add(w.legA);
 			}
 		}
 
 		if(w.legB != null) {
 			if(!legs.contains(w.legB)) {
 				legs.add(w.legB);
 			}
 		}
 	}
 
 	public boolean removeElement(MapElement e) {
 		if(e instanceof Waypoint) {
 			return removeWaypoint((Waypoint) e);
 		} else {
 			return removeLeg((Leg) e);
 		}
 	}
 
 	public boolean removeLeg(Leg l) {
 		if(l.wayA != null) {
 			if(l.wayA.legA == l) {
 				l.wayA.legA = null;
 			} else {
 				l.wayA.legB = null;
 			}
 		}
 
 		if(l.wayB != null) {
 			if(l.wayB.legA == l) {
 				l.wayB.legA = null;
 			} else {
 				l.wayB.legB = null;
 			}
 		}
 
 		return legs.remove(l);
 	}
 
 	public boolean removeWaypoint(Waypoint w) {
 		if(w.legA != null) {
 			if(w.legA.wayA == w) {
 				w.legA.wayA = null;
 			} else {
 				w.legA.wayB = null;
 			}
 		}
 
 		if(w.legB != null) {
 			if(w.legB.wayA == w) {
 				w.legB.wayA = null;
 			} else {
 				w.legB.wayB = null;
 			}
 		}
 
 		return waypoints.remove(w);
 	}
 
 	public MapElement getElement(Position p) {
 		Waypoint closestWaypoint = getWaypoint(p);
 		Leg closestLeg  = getLeg(p);
 
 		double wayDist = p.distance(closestWaypoint.centerPoint);
 		double legDist = p.distance(closestLeg.centerPoint);
 
 		MapElement r = (wayDist > legDist)  ? closestLeg : closestWaypoint;
 
 		if(r.centerPoint.distance(p) < DIST_THRESHOLD) return r;
 		else return null;
 	}
 
 	public Waypoint getWaypoint(Position p)  {
 		Waypoint r = null;
 		double lastDist = 0.0f;
 
 		for(int i = 0; i < waypoints.size(); i++) {
 			if(r == null) {
 				r = waypoints.get(i);
 				lastDist = p.distance(r.centerPoint);
 
 				continue;
 			}
 			double newDist = p.distance(waypoints.get(i).centerPoint);
 
 			if(newDist < lastDist) {
 				lastDist = newDist;
 				r = waypoints.get(i);
 			}
 		}
 
 		return r;
 	}
 
 	public Leg getLeg(Position p) {
 		Leg r = null;
 		double lastDist = 0.0f;
 
 		for(int i = 0; i < legs.size(); i++) {
 			//	If r is null, find the closest point in the first leg
 			if(r == null) {
 				r = legs.get(i);
 				lastDist = -1.0f;
 
 				for(int j = 0; j < r.points.size(); j++) {
 					if(lastDist == -1.0f) {
 						lastDist = p.distance(r.points.get(j));
 						continue;
 					}
 
 					double t = p.distance(r.points.get(j));
 
 					if(lastDist > t) {
 						lastDist = t;
 					}
 				}
 
 				continue;
 			}
 
 			double newDist = -1.0f;
 
 			//	Find closest point in this leg
 			for(int j  = 0; j < legs.get(i).points.size(); j++) {
 				if(newDist == -1.0f) {
 					newDist = p.distance(legs.get(i).points.get(j)); 
 					continue;
 				}
 
 				double t = p.distance(legs.get(i).points.get(j));
 			
 				if(t < newDist) {
 					newDist = t;
 				}
 			}
 
 			if(newDist < lastDist && newDist != -1) {
 				lastDist = newDist;
 				r = legs.get(i);
 			}
 		}
 
 		return r;
 	}
 
 	public void updatePosition() {
 		//	Need to do this from android
 	}
 
 	public double getTotalDistance() {
 		double totalDistance = 0.0f;
 
 		for(Leg l : legs) {
 			if(l.points.size() <= 0) continue;
 
 			Position lastPos = l.points.at(0);
 			for(int i = 1; i < l.points.size(); i++) {
 				double thisDist = last.distance(l.points.get(i));
 				totalDistance += thisDist;
				lastPos = l.points.get(i);
 			}
 		}
 
 		return totalDistance;
 	}
 
 	public void printMapData() {
 		for(int i = 0; i < waypoints.size(); i++) {
 			Waypoint ww = waypoints.get(i);
 			System.out.printf("\tWaypoint at (%f, %f) with Legs: \n\t\t", ww.centerPoint.xCoord, ww.centerPoint.yCoord);
 		
 
 			for(int j = 0; ww.legA != null && j < ww.legA.points.size(); j++) {
 				System.out.printf("(%f, %f) ", ww.legA.points.get(j).xCoord, ww.legA.points.get(j).yCoord);
 			}
 
 			System.out.printf("\n\t\t");
 
 			for(int j = 0; ww.legB != null && j < ww.legB.points.size(); j++) {
 				System.out.printf("(%f, %f) ", ww.legB.points.get(j).xCoord, ww.legB.points.get(j).yCoord);
 			}
 
 			System.out.printf("\n");
 		}
 	}
 }
