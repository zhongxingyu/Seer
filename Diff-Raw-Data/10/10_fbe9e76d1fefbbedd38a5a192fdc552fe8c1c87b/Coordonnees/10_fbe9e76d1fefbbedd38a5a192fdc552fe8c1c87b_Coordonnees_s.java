 package iaws.NBMR.domaines;
 
 public class Coordonnees {
 
 	private double latitude;
 	private double longitude;
 	
 
 	public Coordonnees(double x, double y){
 		this.latitude = x;
 		this.longitude = y;
 	}
 	
 	public double getLatitude() {
 		return latitude;
 	}
 	public void setLatitude(double x) {
 		this.latitude = x;
 	}
 	public double getLongitude() {
 		return longitude;
 	}
 	public void setLongitude(double y) {
 		this.longitude = y;
 	}
 
 	@Override
 	public String toString() {
 		return "Coordonnees [lat=" + latitude + ", lon=" + longitude + "]";
 	}
 	
 }
