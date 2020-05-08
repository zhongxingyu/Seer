 package nz.ac.victoria.ecs.kpsmart.controller;
 
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.HandlesEvent;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.UrlBinding;
 import nz.ac.victoria.ecs.kpsmart.resolutions.FormValidationResolution;
 import nz.ac.victoria.ecs.kpsmart.state.entities.log.EntityUpdateEvent.LocationUpdateEvent;
 import nz.ac.victoria.ecs.kpsmart.state.entities.state.Location;
 
 @UrlBinding("/event/location?{$event}") // TODO: not in /event?
 public class LocationActionBean extends AbstractActionBean {
 	private String name;
 	private double latitude;
 	private double longitude;
 	private boolean isInternational;
 	
 	@DefaultHandler
 	@HandlesEvent("list")
 	public Resolution jsonLocationList() {
 		return new ForwardResolution("/views/event/locationListJSON.jsp"); // TODO: not in /event?
 	}
 	
 	@HandlesEvent("new")
 	public Resolution addNewLocation() {
 		Location location = new Location();
 		location.setName(name);
 		location.setLongitude(longitude);
 		location.setLatitude(latitude);
 		location.setInternational(isInternational);
 		LocationUpdateEvent event = new LocationUpdateEvent();
 		event.setEntity(location);
 		getEntityManager().performEvent(event);
 		return new FormValidationResolution(true, null, null);
 	}
 
 	/**
 	 * @return the longitude
 	 */
 	public double getLongitude() {
 		return longitude;
 	}
 
 	/**
 	 * @param longitude the longitude to set
 	 */
 	public void setLongitude(double longitude) {
 		this.longitude = longitude;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param name the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @return the latitude
 	 */
 	public double getLatitude() {
 		return latitude;
 	}
 
 	/**
 	 * @param latitude the latitude to set
 	 */
 	public void setLatitude(double latitude) {
 		this.latitude = latitude;
 	}
 
 	/**
 	 * @return the isInternational
 	 */
 	public boolean isInternational() {
 		return isInternational;
 	}
 
 	/**
 	 * @param isInternational the isInternational to set
 	 */
	public void setIsInternational(boolean isInternational) {
 		this.isInternational = isInternational;
 	}
 }
