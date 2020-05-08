 package com.andrewdutcher.indexcards;
 
 //Written by Kyle Slattery (http://kyleslattery.com)
 //Free to use and edit, so long as above line stays intact
 
 public class Vector {
 	private double x,y,z;
 	private double magnitude;
 	private Vector unit;
 	
 	public Vector(boolean isUnit, double x, double y, double z) {
 		this.x = x;
 		this.y = y;
 		this.z = z;
 		
 		magnitude = Math.sqrt(x*x+y*y+z*z);
 		
 		if(isUnit) {
 			this.unit = this;
 		} else {
 			if(magnitude == 0) {
 				this.unit = new Vector(true, 0, 0, 0);
 			} else {
 				this.unit = new Vector(true, x/magnitude, y/magnitude, z/magnitude);
 			}
 		}
 	}
 	
 	public Vector(double x, double y, double z) {
 		this(false, x, y, z);
 	}
 	
	public Vector(double r, double theta) {
		this(false,r*Math.cos(Math.toRadians(theta)),r*Math.sin(Math.toRadians(theta)),0);
	}
	
 	// Accessor Methods
 	public double getX() {
 		return x;
 	}
 	
 	public double getY() {
 		return y;
 	}
 	
 	public double getZ() {
 		return z;
 	}
 	
 	public double getMagnitude() {
 		return magnitude;
 	}
 	
 	public Vector getUnitVector() {
 		return unit;
 	}
 	
 	public double get2DAngle() {
 		double out = Math.toDegrees(Math.atan(y/x));
 		if (x < 0)
 			out += 180;
 		return out;
 	}
 	
 	// Operations
 	public Vector scalarMult(double scalar) {
 		return new Vector(x*scalar, y*scalar, z*scalar);
 	}
 	
 	public Vector add(Vector vec) {
 		return new Vector(x+vec.getX(), y+vec.getY(), z+vec.getZ());
 	}
 	
 	public Vector add(Vector[] vec) {
 		double x2,y2,z2;
 		x2 = y2 = z2 = 0;
 		for(int i = 0; i < vec.length; i++) {
 			x2 += vec[i].getX();
 			y2 += vec[i].getY();
 			z2 += vec[i].getZ();
 		}
 		
 		return new Vector(x+x2, y+y2, z+z2);
 	}
 	
 	public double dot(Vector vec) {
 		return x*vec.getX() + y*vec.getY() + z*vec.getZ();
 	}
 	
 	public Vector cross(Vector vec) {
 		double xi = y*vec.getZ() - z*vec.getY();
 		double yi = z*vec.getX() - x*vec.getZ();
 		double zi = x*vec.getY() - y*vec.getX();
 		return new Vector(xi,yi,zi);
 	}
 	
 	public Vector minus(Vector vec) {
 		return new Vector(x-vec.getX(), y-vec.getY(), z-vec.getZ());
 	}
 	
 	public String toString() {
 		return "<" + x + ", " + y + ", " + z + ">";
 	}
 }
