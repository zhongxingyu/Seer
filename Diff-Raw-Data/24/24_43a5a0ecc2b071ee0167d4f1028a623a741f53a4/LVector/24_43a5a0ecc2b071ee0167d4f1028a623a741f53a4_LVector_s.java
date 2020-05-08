 package org.libraryofthings.math;
 
 import org.apache.commons.math3.linear.ArrayRealVector;
 import org.apache.commons.math3.linear.RealVector;
 
 public final class LVector {
	private RealVector v = new ArrayRealVector(new double[3]);

 	public LVector(double x, double y, double z) {
 		v.setEntry(0, x);
 		v.setEntry(1, y);
 		v.setEntry(2, x);
 	}
 
 	public LVector() {
 		// zeros
 	}
 
 	public void set(LVector location) {
 		v.setEntry(0, location.getX());
 		v.setEntry(1, location.getY());
 		v.setEntry(2, location.getZ());
 	}
 
 	private double getY() {
 		return v.getEntry(1);
 	}
 
 	private double getX() {
 		return v.getEntry(0);
 	}
 
 	private double getZ() {
 		return v.getEntry(2);
 	}
 }
