 package webapp;
 
 
 import com.google.code.morphia.annotations.Embedded;
 
 /*
  * Holds data for a single Office
  */
 @Embedded
 public class Office {
 	
 	private String _zip, _city, _state, _country;
 	private double _latitude, _longitude;
 
 	public Office() {
 	}
 	
 	public String getZip() { return _zip; }
	protected void setZip(String zip) { _zip = zip.trim(); }
 	
 	public String getCity() { return _city; }
	protected void setCity(String city) { _city = city.trim(); }
 	
 	public String getState() { return _state; }
	protected void setState(String state) { _state = state.trim(); }
 	
 	public String getCountry() { return _country; }
	protected void setCountry(String country) { _country = country.trim(); }
 	
 	public double getLatitude() {return _latitude;}
 	protected void setLatitude(double latitude) { _latitude = latitude; }
 	
 	public double getLongitude() {return _longitude;}
 	protected void setLongitude(double longitude) { _longitude = longitude; }
 }
