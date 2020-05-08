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
			boolean result = true;
			if (this.country!=null) {
				result = result && this.country.equals(o.getCountry());
			} else {
				result = result && o.getCountry()==null;
			}
			if(this.region!=null) {
				result = result && this.region.equals(o.getRegion());
			} else {
				result = result && o.getRegion()==null;
			}
			if (this.city != null) {
				result = result && this.city.equals(o.getCity());
			} else {
				result = result && o.getCity()==null;
			}
			if (this.locale != null) {
					result = result && this.locale.equals(o.getLocale());
			} else {
				result = result && o.getLocale()==null;
			}
			return result;
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
