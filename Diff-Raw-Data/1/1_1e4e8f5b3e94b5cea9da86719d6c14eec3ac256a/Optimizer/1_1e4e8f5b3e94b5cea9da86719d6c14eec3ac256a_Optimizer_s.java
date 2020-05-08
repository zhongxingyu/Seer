 package com.ezgliding.igc;
 
 public abstract class Optimizer {
 
 	protected Flight flight;
 
 	protected int numPoints;
 
 	public Optimizer(Flight flight, int numPoints) {
 		if (flight == null) throw new IllegalArgumentException("Flight cannot be null");
 		if (numPoints < 2) throw new IllegalArgumentException("Invalid number of points :: < 3");
 
 		this.flight = flight;
 		this.numPoints = numPoints;
 	}
 
 	public abstract Result optimize();
 
 	public class Result implements Comparable<Result> {
 		
 		private double distance = -1;
 		
 		public Fix[] points;
 
 		public Result() {
 			this(null);
 		}
 
 		public Result(Fix[] points) {
 			this.points = points;
 		}
 
 		public double distance() {
 			if (distance != -1 || points == null) return distance;
 			for (int i=0; i<points.length-1; i++)
 				distance += Util.distance(points[i], points[i+1]);
 			return distance;
 		}
 
 		public String toKml() {
 			String kml = "<LineString><coordinates>";
 			for (Fix point: points)
 				kml += point.lon() + "," + point.lat() + "," + point.pressureAlt + " ";
 			return kml + "</coordinates></LineString>";
 		}
 
 		@Override
 		public boolean equals(Object otherO) {
 			if (otherO == null) return false;
 			if (compareTo((Result)otherO) == 0) return true;
 			return false;
 		}
 
 		@Override
 		public int compareTo(Result other) {
 			double diff = distance() - other.distance();
 			if (diff == 0) return 0;
 			else if (diff > 0) return 1;
 			return -1;
 		}
 
 		@Override
 		public String toString() {
 			String str = "{" + String.format("%1$,.2f", distance) + ",";
 			for (Fix point: points)
 				str += point + ",";
 			return str + "}";
 		}
 	}
 }
