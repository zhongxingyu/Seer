 package org.klab.geocoding.service.impl;
 
 public enum GoogleStatusCode {
	G_GEO_SUCCESS ("No errors occurred; the address was successfully parsed and its geocode was returned."), 
 	G_GEO_SERVER_ERROR ("A geocoding or directions request could not be successfully processed, yet the exact reason for the failure is unknown."),  
 	G_GEO_MISSING_QUERY ("An empty address was specified in the HTTP q parameter."),  
 	G_GEO_UNKNOWN_ADDRESS ("No corresponding geographic location could be found for the specified address, possibly because the address is relatively new, or because it may be incorrect."),  
 	G_GEO_UNAVAILABLE_ADDRESS ("The geocode for the given address or the route for the given directions query cannot be returned due to legal or contractual reasons."), 
 	G_GEO_BAD_KEY ("The given key is either invalid or does not match the domain for which it was given."), 
 	G_GEO_TOO_MANY_QUERIES ("The given key has gone over the requests limit in the 24 hour period or has submitted too many requests in too short a period of time. If you're sending multiple requests in parallel or in a tight loop, use a timer or pause in your code to make sure you don't send the requests too quickly.");
 	
 	private String description;
 	
 	GoogleStatusCode(String description) {
 		this.description = description;
 	}
 	
 	public static GoogleStatusCode fromCode(int code) {
 		switch (code) {
 			case 200: return G_GEO_SUCCESS;
 			case 500: return G_GEO_SERVER_ERROR;
 			case 601: return G_GEO_MISSING_QUERY;
 			case 602: return G_GEO_UNKNOWN_ADDRESS;
 			case 603: return G_GEO_UNAVAILABLE_ADDRESS;
 			case 610: return G_GEO_BAD_KEY;
 			case 620: return G_GEO_TOO_MANY_QUERIES;
 			default:
 				throw new IllegalArgumentException(code + " is not a valide google status code");
 		}
 	}
 	
 	public String getDescription() {
 		return description;
 	}
 }
