 package com.pixeltron.maproulette.responses;
 
 import java.util.List;
 
 import com.google.common.collect.Lists;
 import fi.foyt.foursquare.api.entities.CompactVenue;
 
 public class WaypointResponse {
 	public boolean isOK;
 	public WaypointResponseData data;
 	public List<String> errors;
 	
 	public WaypointResponse() {
 		isOK = false;
 	}
 	
 	public void setData(List<CompactVenue> venues) {
 		data = new WaypointResponseData(venues);
 	}
 	
 	public void addError(String error) {
 		if (errors == null) {
 			errors = Lists.newArrayList();
 		}
 		errors.add(error);
 	}
 	
 	public void prepareForTransport() {
		if (errors != null && !errors.isEmpty()) {
 			isOK = false;
 		} else {
 			isOK = true;
 		}
 	}
 }
