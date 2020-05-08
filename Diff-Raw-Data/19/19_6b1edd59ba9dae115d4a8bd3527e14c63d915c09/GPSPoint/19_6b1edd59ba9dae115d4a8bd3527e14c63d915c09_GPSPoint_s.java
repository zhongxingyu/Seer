 package Common;
 
 import android.location.Location;
 
 public class GPSPoint  {
 
 	private double longitude;
 	private double latitude;
 
 	public double getLongitude() {
 		return longitude;
 	}
 	public double getLatitude() {
 		return latitude;
 	}
 	
 	public GPSPoint(double latitude, double longitude) {
 		this.longitude = roundDouble(longitude);
 		this.latitude = roundDouble(latitude);
 	}
 	
 	/**
 	 * method to return real distance between two GPS Points
 	 * @param p
 	 * @return
 	 */
 	public float distanceFrom(GPSPoint p) {
 		float[] results = new float[3]; 
 		// Using Android's Location.distanceBetween to ensure that calculation of distance is more accurate 
 		Location.distanceBetween(this.latitude, this.longitude, p.getLatitude(), p.getLongitude(), results);
 		return results[0];
 	}
 	
 	@Override
 	public String toString() {
 		return (latitude + "," + longitude);
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		long temp;
 		temp = Double.doubleToLongBits(latitude);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		temp = Double.doubleToLongBits(longitude);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		return result;
 	}
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		GPSPoint other = (GPSPoint) obj;
 		if (Double.doubleToLongBits(latitude) != Double
 				.doubleToLongBits(other.latitude))
 			return false;
 		if (Double.doubleToLongBits(longitude) != Double
 				.doubleToLongBits(other.longitude))
 			return false;
 		return true;
 	}
 	
 	private double roundDouble (double num)
 	{
 		return (Math.round(num * Math.pow(10, 10)) / Math.pow(10, 10));
 	}
 	
 }
