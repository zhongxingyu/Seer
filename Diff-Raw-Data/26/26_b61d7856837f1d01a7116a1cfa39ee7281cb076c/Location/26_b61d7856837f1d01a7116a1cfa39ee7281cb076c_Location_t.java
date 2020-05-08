 package com.me4502.MAPL;
 
public class Location implements Cloneable {
 
 	double[] coordinates;
 
	public double[] getCoordinates() {
		return coordinates;
	}

 	public Location(double... coordinate) {
		coordinates = coordinate.clone();
 	}
 
 	public Location(Location location) {
		coordinates = location.coordinates.clone();
 	}
 
 	public Location add(Vector vector) {
 		try {
 			for(int dim = 0; dim < getDimensions(); dim++) {
 				this.setPoint(dim, getPoint(dim) + vector.getPoint(dim));
 			}
 
 			return this;
 		}
 		catch(Exception e) {
 			throw new InvalidDimensionException("Location does not contain this dimension!");
 		}
 	}
 
 	public Location multiply(Vector vector) {
 		try {
 			for(int dim = 0; dim < getDimensions(); dim++) {
 				this.setPoint(dim, getPoint(dim) * vector.getPoint(dim));
 			}
 
 			return this;
 		}
 		catch(Exception e) {
 			throw new InvalidDimensionException("Location does not contain this dimension!");
 		}
 	}
 
 	public Location divide(Vector vector) {
 		try {
 			for(int dim = 0; dim < getDimensions(); dim++) {
 				this.setPoint(dim, getPoint(dim) / vector.getPoint(dim));
 			}
 
 			return this;
 		}
 		catch(Exception e) {
 			throw new InvalidDimensionException("Location does not contain this dimension!");
 		}
 	}
 
 	public Location subtract(Vector vector) {
 		try {
 			for(int dim = 0; dim < getDimensions(); dim++) {
 				this.setPoint(dim, getPoint(dim) - vector.getPoint(dim));
 			}
 
 			return this;
 		}
 		catch(Exception e) {
 			throw new InvalidDimensionException("Location does not contain this dimension!");
 		}
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
 
 	public static Location fromString(String string) {
 		double[] bits;
 		string = string.substring(1, string.length() - 1);
 		bits = new double[string.split(",").length];
 		int i = 0;
 		for(String bit : string.split(",")) {
 			bits[i] = Double.parseDouble(bit);
 			i++;
 		}
 
 		return new Location(bits);
 	}
 
 	public int getDimensions() {
 		return coordinates.length;
 	}
 
 	public double getPoint(int dimension) throws InvalidDimensionException {
 		try {
 			return coordinates[dimension];
 		}
 		catch(Exception e) {
 			throw new InvalidDimensionException("Location does not contain this dimension!");
 		}
 	}
 
 	public int getRoundPoint(int dimension) throws InvalidDimensionException {
 		try {
 			return (int)coordinates[dimension];
 		}
 		catch(Exception e) {
 			throw new InvalidDimensionException("Location does not contain this dimension!");
 		}
 	}
 
 	public void setPoint(int dimension, double value) throws InvalidDimensionException {
 		try {
 			coordinates[dimension] = value;
 		}
 		catch(Exception e) {
 			throw new InvalidDimensionException("Location does not contain this dimension!");
 		}
 	}
 
 	public double getDistanceBetweenSquared(Location location) throws InvalidDimensionException {
 		if(location.getDimensions() == getDimensions()) {
 			double result = 0;
 			for(int i = 0; i < getDimensions(); i++) {
 				result += Math.pow(getPoint(i) + location.getPoint(i), 2);
 			}
 
 			return result;
 		}
 		else
 			throw new InvalidDimensionException("Dimensions of locations do not match!");
 	}
 
 	public double getDistanceBetween(Location location) throws InvalidDimensionException {
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
 	public Location clone() {
 		return new Location(this);
 	}
 
 	public Vector toVector() {
 		return new Vector(coordinates);
 	}
 }
