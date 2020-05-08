 package edu.tufts.cs.gv.util;
 
 public class Vector {
 
 	private double x, y;
 	
 	public Vector() {
 		this(0, 0);
 	}
 	
 	public Vector(double x, double y) {
 		this.x = x;
 		this.y = y;
 	}
 	
 	public void setX(double x) {
 		this.x = x;
 	}
 
 	public void setY(double y) {
 		this.y = y;
 	}
 
 	public double getX() {
 		return x;
 	}
 
 	public double getY() {
 		return y;
 	}
 
 	public double length() {
 		return Math.sqrt(length2());
 	}
 	
 	public double length2() {
 		return x * x + y * y;
 	}
 	
 	public void normalize() {
 		double length = length();
 		if (length != 0) {
 			x /= length;
 			y /= length;
		} else {
			x = Math.sqrt(2);
			y = Math.sqrt(2);
 		}
 	}
 	
 	public void scale(double scale) {
 		x *= scale;
 		y *= scale;
 	}
 }
