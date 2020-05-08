 package com.me4502.MAPL;
 
 
public class Vector {
 
 	double[] coordinates;
 
 	public Vector(double... coordinate) {
		coordinates = coordinate;
 	}
 
 	public Vector(Vector vector) {
		coordinates = vector.coordinates;
 	}
 
 	@Override
 	public String toString() {
 		String locs = "";
 		for(double d : coordinates) {
 			locs = locs + d + ",";
 		}
 		locs = locs.substring(0, locs.length() - 1);
 		return "[" + locs + "]";
 	}
 
 	public static Vector fromString(String string) {
 		double[] bits;
 		string = string.substring(1, string.length() - 1);
 		bits = new double[string.split(",").length];
 		int i = 0;
 		for(String bit : string.split(",")) {
 			bits[i] = Double.parseDouble(bit);
 			i++;
 		}
 
 		return new Vector(bits);
 	}
 
 	public int getDimensions() {
 		return coordinates.length;
 	}
 
 	public double getPoint(int dimension) throws InvalidDimensionException {
 		try {
 			return coordinates[dimension];
 		}
 		catch(Exception e) {
 			throw new InvalidDimensionException("Vector does not contain this dimension!");
 		}
 	}
 
 	public int getRoundPoint(int dimension) throws InvalidDimensionException {
 		try {
 			return (int)coordinates[dimension];
 		}
 		catch(Exception e) {
 			throw new InvalidDimensionException("Vector does not contain this dimension!");
 		}
 	}
 
 	public void setPoint(int dimension, double value) throws InvalidDimensionException {
 		try {
 			coordinates[dimension] = value;
 		}
 		catch(Exception e) {
 			throw new InvalidDimensionException("Vector does not contain this dimension!");
 		}
 	}
 
 	public double getDistanceBetweenSquared(Vector location) throws InvalidDimensionException {
 		if(location.getDimensions() == getDimensions()) {
 			double result = 0;
 			for(int i = 0; i < getDimensions(); i++) {
 				result += Math.pow(getPoint(i) + location.getPoint(i), 2);
 			}
 
 			return result;
 		}
 		else
 			throw new InvalidDimensionException("Dimensions of Vectors do not match!");
 	}
 
 	public double getDistanceBetween(Vector location) throws InvalidDimensionException {
 		return Math.sqrt(getDistanceBetweenSquared(location));
 	}
 
 	public class InvalidDimensionException extends RuntimeException {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 4144631083085939420L;
 
 		public InvalidDimensionException(String message) {
 			super(message);
 		}
 	}
 
 	@Override
 	public Vector clone() {
 		return new Vector(this);
 	}
 
 	public Location toLocation() {
 		return new Location(coordinates);
 	}
 
 	public Vector relative(Location location) {
 		Vector vec = new Vector(this);
 		for(int i = 0; i < getDimensions(); i++) {
 			vec.setPoint(i, location.getPoint(i) - getPoint(i));
 		}
 
 		return vec;
 	}
 }
