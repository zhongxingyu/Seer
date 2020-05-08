 package com.chaman.model;
 
 import com.restfb.DefaultFacebookClient;
 import com.restfb.Facebook;
 import com.restfb.FacebookClient;
 import com.restfb.exception.FacebookException;
 
 public class Venue  extends Model {
 
 	@Facebook
 	String id;
 	@Facebook
 	String name;
 	@Facebook
 	String category;
 	@Facebook
 	String checkins;
 	@Facebook
 	String talking_about_count;
 	@Facebook
 	String type;
 	@Facebook
 	String likes;
 	@Facebook
 	String picture;
 	@Facebook
 	String location;
 	
 	public Venue() {
 		
 		super();
 	}
 	
 	public Venue(String accessToken, String venueID) throws FacebookException {
 		
		if (venueID != null && venueID.charAt(0) != '{'){
 			Venue result = new Venue();
 			
 			FacebookClient client 	= new DefaultFacebookClient(accessToken);
 			result = client.fetchObject(venueID, Venue.class);
 
 			this.id = result.id;
 			this.name = result.name;
 			this.category = result.category;
 			this.checkins = result.checkins;
 			this.talking_about_count = result.talking_about_count;
 			this.type = result.type;
 			this.likes = result.likes;		
 			this.picture = result.picture;	
 			this.location = result.location;
 		}
 	}
 }
