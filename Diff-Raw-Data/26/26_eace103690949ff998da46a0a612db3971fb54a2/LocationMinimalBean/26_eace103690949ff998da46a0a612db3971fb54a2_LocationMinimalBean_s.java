 package pojos;
 
 import models.Location;
 
 public class LocationMinimalBean {
 	private String country;
 	private String city;
 	private String region;
 	private String locale;
 	
 	public LocationMinimalBean(String country, String city, String region, String locale) {
 		this.country = country;
 		this.region = region;
 		this.city = city;
 		this.locale = locale;
 	}
 	
 	public LocationMinimalBean(Location loc) {
 		this.country = loc.getCountry();
 		this.region = loc.getRegion();
 		this.city = loc.getCity();
 		this.locale = loc.getLocale();
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof LocationMinimalBean) {
 			LocationMinimalBean o = (LocationMinimalBean) obj;
			return this.country.equals(o.getCountry()) 
					&& this.region.equals(o.getRegion()) 
					&& this.city.equals(o.getCity())
					&& this.locale.equals(o.getLocale());
 		} else {
 			return obj.equals(this);
 		}
 		
 	};
 	
 	public String getCountry() {
 		return country;
 	}
 	public void setCountry(String country) {
 		this.country = country;
 	}
 	public String getCity() {
 		return city;
 	}
 	public void setCity(String city) {
 		this.city = city;
 	}
 	public String getRegion() {
 		return region;
 	}
 	public void setRegion(String region) {
 		this.region = region;
 	}
 	public String getLocale() {
 		return locale;
 	}
 	public void setLocale(String locale) {
 		this.locale = locale;
 	}
 }
