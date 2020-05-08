 package com.aerodynelabs.map;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 
 public class MapPath {
 	
 	private String name;
 	private double boundNorth = 0.0;
 	private double boundSouth = 0.0;
 	private double boundEast = 0.0;
 	private double boundWest = 0.0;
 	private double maxAlt = 0.0;
 	private long startTime = 0;
 	private long endTime = 0;
 	
 	private LinkedList<MapPoint> path;
 	private ArrayList<MapPoint> markers;
 	
 	public MapPath() {
 		path = new LinkedList<MapPoint>();
 		markers = new ArrayList<MapPoint>();
 	}
 	
 	public MapPath(String name) {
 		this();
 		this.name = name;
 	}
 	
 	public MapPath(List<MapPoint> path) {
 		this();
 		this.path.addAll(path);
 	}
 	
 	public void addMarker(MapPoint mark) {
 		markers.add(mark);
 	}
 	
 	public void addMarker(String name, double lat, double lon) {
 		markers.add(new MapPoint(lat, lon, 0, 0, name));
 	}
 	
 	public MapPoint getFirst() {
 		return path.getFirst();
 	}
 	
 	public MapPoint getLast() {
 		return path.getLast();
 	}
 	
 	public double getDistance() {
 		double dLat = Math.toRadians(path.getLast().getLatitude() - path.getFirst().getLatitude());
 		double dLon = Math.toRadians(path.getLast().getLongitude() - path.getFirst().getLongitude());
 		double sLat = Math.toRadians(path.getFirst().getLatitude());
 		double fLat = Math.toRadians(path.getLast().getLatitude());
 		double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(sLat) * Math.cos(fLat);
 		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
 		return c * 6371000;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public double getMaxAlt() {
 		return maxAlt;
 	}
 	
 	public long getStartTime() {
 		return startTime;
 	}
 	
 	public long getEndTime() {
 		return endTime;
 	}
 	
 	public long getElapsedTime() {
 		return endTime - startTime;
 	}
 	
 	private void updateBounds(double lat, double lon) {
 		if(lat > boundNorth) {
 			boundNorth = lat;
 		} else if(lat < boundSouth) {
 			boundSouth = lat;
 		}
 		if(lon > boundEast) {
 			boundEast = lon;
 		} else if(lon < boundWest) {
 			boundWest = lon;
 		}
 	}
 	
 	public void add(MapPoint point) {
 		updateBounds(point.getLatitude(), point.getLongitude());
 		path.add(point);
 	}
 	
 	public void add(double lat, double lon) {
 		updateBounds(lat, lon);
 		path.add(new MapPoint(lat, lon));
 	}
 	
 	public void add(double lat, double lon, double alt) {
 		updateBounds(lat, lon);
 		if(alt > maxAlt) maxAlt = alt;
 		path.add(new MapPoint(lat, lon, alt));
 	}
 	
 	public void add(double lat, double lon, double alt, long time) {
 		updateBounds(lat, lon);
 		if(alt > maxAlt) maxAlt = alt;
 		if(time < startTime || startTime == 0) startTime = time;
 		if(time > endTime) endTime = time;
 		path.add(new MapPoint(lat, lon, alt, time));
 	}
 	
 	public void addAll(List<MapPoint> path) {
 		this.path.addAll(path);
 	}
 	
 	public double getNorthBound() {
 		return boundNorth;
 	}
 	
 	public double getSouthBound() {
 		return boundSouth;
 	}
 	
 	public double getEastBound() {
 		return boundEast;
 	}
 	
 	public double getWestBound() {
 		return boundWest;
 	}
 	
 	public LinkedList<MapPoint> getPath() {
 		return path;
 	}
 	
 	public List<MapPoint> getMarkers() {
 		return markers;
 	}
 	
 	public boolean inBounds(double north, double east, double south, double west) {
 		if(south > boundNorth) return false;	// Region is above of bounds
 		if(north < boundSouth) return false;	// Region is below of bounds
 		if(east < boundWest) return false;		// Region is left of bounds
 		if(west > boundEast) return false;		// Region is right of bounds
 		
 		return true;
 	}
 	
 	public ListIterator<MapPoint> iterator() {
 		return path.listIterator();
 	}
 	
 	public boolean export(File file) {
 		PrintWriter out;
 		try {
 			out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		
 		ListIterator<MapPoint> itr = path.listIterator();
 		while(itr.hasNext()) {
 			out.println(itr.next());
 		}
		out.flush();
 		
 		return true;
 	}
 
 }
