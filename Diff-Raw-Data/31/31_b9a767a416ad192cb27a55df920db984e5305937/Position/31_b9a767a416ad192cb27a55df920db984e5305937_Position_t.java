 package back;
 
 import java.util.Date;
 
 /**
  * Representation of position on the route
  * @author fafin
  *
  */
 public class Position {
 	private Date time;
 	private double lat, lon, altitude;
 	
 	public Date getTime() {
 		return time;
 	}
 
 	public void setTime(Date time) {
 		this.time = time;
 	}
 
 	public double getLat() {
 		return lat;
 	}
 
 	public void setLat(double lat) {
 		this.lat = lat;
 	}
 
 	public double getLon() {
 		return lon;
 	}
 
 	public void setLon(double lon) {
 		this.lon = lon;
 	}
 
 	public double getAltitude() {
 		return altitude;
 	}
 
 	public void setAltitude(double altitude) {
 		this.altitude = altitude;
 	}
 	public Position(Date time, double lat, double lon, double altitude){
	
 		this.time = time;
 		this.lat = lat;
 		this.lon = lon;
 		this.altitude = altitude;
 	}
 	
 
 }
