 package com.chaman.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 
 import com.chaman.model.Venue;
 import com.beoui.geocell.GeocellManager;
 import com.beoui.geocell.LocationCapableRepositorySearch;
 import com.beoui.geocell.model.Point;
 import com.chaman.dao.Dao;
 import com.chaman.util.Geo;
 import com.chaman.util.JSON;
 import com.google.appengine.api.memcache.MemcacheService;
 import com.google.appengine.api.memcache.MemcacheServiceFactory;
 import com.googlecode.objectify.Query;
 import com.restfb.DefaultFacebookClient;
 import com.restfb.Facebook;
 import com.restfb.FacebookClient;
 import com.restfb.exception.FacebookException;
 
 /*
  * Event object from FB + formatting for our app
  */
 public class Event extends Model implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 379496115984033603L;
 	@Facebook
 	Long eid;
 	@Facebook
 	String name;
 	@Facebook
 	String pic;
 	@Facebook
 	String venue;
 	@Facebook
 	String location;
 	@Facebook
 	String start_time;
 	@Facebook
 	String end_time;
 	@Facebook
 	String description;
 	@Facebook
 	String host;
 	String creator; /*promoter ID*/
 	@Facebook
 	String owner;
 	@Facebook
 	String privacy;
 	@Facebook
 	String update_time;
 	
 	String venue_id;
 	double score;
 	long nb_invited;
 	String group;
 	String latitude;
 	String longitude;
 	String time_start;
 	String time_end;
 	String date_start;
 	String date_end;
 	String picture;
 	String invited_by;
 	String distance;
 	String groupTitle;
 	String venue_category; // (club, bar etc)
 	User user;
 	DateTime dtStart;
 	DateTime dtEnd;
 	
 	public Event() {
 		
 		super();
 	}
 	
 	/*
 	 * - Get list of events from facebook for a userID
 	 * - store in our DB events w/ latitude and longitude
 	 * - exclude past events
 	 */
 	public static ArrayList<Model> Get(String accessToken, String userID, String userLatitude, String userLongitude, String timeZone) throws FacebookException {
 		
 		ArrayList<Model> result = new ArrayList<Model>();
 		
 		//Prepare a timestamp to filter the facebook DB on the upcoming events
 		DateTimeZone PST = DateTimeZone.forID("America/Los_Angeles");
 		DateTime now = new DateTime(PST);
 		now.plusMinutes(PST.getOffset(now));
 		String TAS = String.valueOf(now.getMillis() / 1000);
 		
 		FacebookClient client 	= new DefaultFacebookClient(accessToken);
 		String properties 		= "eid, name, pic, start_time, end_time, venue, location, host, privacy, creator, update_time";
 		String query 			= "SELECT " + properties + " FROM event WHERE eid IN (SELECT eid FROM event_member WHERE uid = " + userID + ") AND end_time > " + TAS + " ORDER BY start_time"; /*need to check privacy CLOSED AND SECRET */
 		List<Event> fbevents 	= client.executeQuery(query, Event.class);
 		
 		Dao dao = new Dao();
 		
 		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
 		Event e_cache; 
 		
 		int timeZoneInMinutes = Integer.parseInt(timeZone);
 		
 		for (Event e : fbevents) {
 				
 			e_cache = (Event) syncCache.get(e.eid); // read from Event cache
     	    if (e_cache == null) {// || !e_cache.update_time.equals(e.update_time)) { // check the cache event version with the current one
 			
     	    	e.Format(timeZoneInMinutes);
     	    	
     	    	e.venue_id = JSON.GetValueFor("id", e.venue);    	
     	    	Venue v_graph = new Venue(accessToken, e.venue_id);
     	    	e.venue_category = v_graph.category;
 			
     	    	e.latitude 	= JSON.GetValueFor("latitude", e.venue);
     	    	e.longitude = JSON.GetValueFor("longitude", e.venue);
 			
     	    	if ((e.latitude == null || e.latitude == "" || e.longitude == null || e.longitude == "") && v_graph != null) {
 				
     	    		// take value from venue if event location is null
     	    		e.latitude = JSON.GetValueFor("latitude", v_graph.location);
     	    		e.longitude = JSON.GetValueFor("longitude", v_graph.location);
     	    	}	
 			
     	    	if (e.latitude != null && e.latitude != "" && e.longitude != null && e.longitude != "") {
 				
     	    		Query<EventLocationCapable> q = dao.ofy().query(EventLocationCapable.class);
     	    		q.filter("eid", e.eid); //can be optimized with a get (filter = 1 read + 1small op)
 					
     	    		if (q.count() == 0) {
 			        	
     	    			//e.getNb_invited(accessToken);
     	    			EventLocationCapable elc = new EventLocationCapable(e);
     	    			dao.ofy().put(elc);
     	    		} else {
 			        	
     	    			// update score in DB     	
     	    		}
 			        			        
     	    		float distance = Geo.Fence(userLatitude, userLongitude, e.latitude, e.longitude);
 					
     	    		e.distance = String.format("%.2f", distance);
     	    	} else {
 								
     	    		e.distance = "N/A";
     	    	}
     	    	
     	    	e.Score(v_graph);
     	    }else {
     	    	
     	    	e = e_cache;
    	    	e.Format(timeZoneInMinutes);
     	    	Venue v_graph = new Venue(accessToken, e.venue_id);
     	    	e.Score(v_graph);
     	    }
     	    result.add(e);
 	    	syncCache.put(e.eid, e); // Add Event to cache
 		}
 		
 		return result;
 	}
 
 	 /* - Get list of event for any user in search area
 	 * - exclude past event
 	 */
 	public static ArrayList<Model> Get(String accessToken, String userLatitude, String userLongitude, String timeZone, int searchTimeFrame, int searchRadius, int searchLimit) throws FacebookException {
 		
 		ArrayList<Model> result = new ArrayList<Model>();
 		
 		Dao dao = new Dao();
 		
 		LocationCapableRepositorySearch<EventLocationCapable> ofySearch = new OfyEntityLocationCapableRepositorySearchImpl(dao.ofy(), timeZone, searchTimeFrame);
 		List<EventLocationCapable> l = GeocellManager.proximityFetch(new Point(Double.parseDouble(userLatitude), Double.parseDouble(userLongitude)), searchLimit, searchRadius * 1000, ofySearch);
 		
 		FacebookClient client 	= new DefaultFacebookClient(accessToken);
 		String properties 		= "eid, name, pic, start_time, end_time, venue, location, host, privacy";
 		
 		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
 		
 		int timeZoneInMinutes = Integer.parseInt(timeZone);
 		
         for (EventLocationCapable e : l) {
         	
     		String query 			= "SELECT " + properties + " FROM event WHERE eid = " + e.getEid();
     		List<Event> fbevents 	= client.executeQuery(query, Event.class);
     		
     		if (fbevents != null && fbevents.size() > 0) {
     			
     			Event event = fbevents.get(0);
     			event.Format(timeZoneInMinutes);    		
     			
     			if (event.IsNotPast()) {
     				
     				// event.nb_invited = e.getNb_invited();
     				event.latitude 	= Double.toString(e.getLatitude());
     				event.longitude = Double.toString(e.getLongitude());
     				
     				float distance = Geo.Fence(userLatitude, userLongitude, event.latitude, event.longitude);
             		event.distance = String.format("%.2f", distance);
 
             		event.creator = String.valueOf(e.getCreator());
     		
         	    	event.venue_id = JSON.GetValueFor("id", event.venue);
         	    	Venue v_graph = new Venue(accessToken, event.venue_id);
         	    	event.venue_category = v_graph.category;
         	    	event.Score(v_graph);
             		syncCache.put(event.eid, event); //add event to cache
             		
                 	result.add(event);
     			} else {
     				
 	    			dao.ofy().delete(e); //clean the datastore by removing old events TODO: call a task doesn't have to be deleted right away
     				//TODO: delete vote DS
     			}
     		}
         }
 		
 		return result;
 	}
 	
 	public static ArrayList<Model> getMultiple(String accessToken, String[] eids, String timeZone, String userLatitude, String userLongitude) {
 		
 		ArrayList<Model> result	= new ArrayList<Model>();
 		
 		for (String eid : eids) {
 	    	
 			result.add(getSingle(accessToken, eid, timeZone, userLatitude, userLongitude));
 		}
 		
 		return result;
 	}
 	
 	public static Event getSingle(String accessToken, String eid, String timeZone, String userLatitude, String userLongitude) {
 		
 		FacebookClient client	= new DefaultFacebookClient(accessToken);
 		int timeZoneInMinutes	= Integer.parseInt(timeZone);
 		
 		Event e = new Event();
         	
     	String query 			= "SELECT eid, name, pic, start_time, end_time, venue, location, host, privacy FROM event WHERE eid = " + eid;
     	List<Event> fbevents 	= client.executeQuery(query, Event.class);
 		
 		e = fbevents.get(0);
 
 		e.Format(timeZoneInMinutes);
 			
 		e.venue_id = JSON.GetValueFor("id", e.venue);
 		Venue v_graph = new Venue(accessToken, e.venue_id);
 		e.venue_category = v_graph.category;
 		e.latitude 	= JSON.GetValueFor("latitude", e.venue);
 		e.longitude = JSON.GetValueFor("longitude", e.venue);
 		
 		if ((e.latitude == null || e.latitude == "" || e.longitude == null || e.longitude == "") && v_graph != null) {
 	    		
 			e.latitude = JSON.GetValueFor("latitude", v_graph.location);
 			e.longitude = JSON.GetValueFor("longitude", v_graph.location);
 		}
 		
 		if (e.latitude != null && e.latitude != "" && e.longitude != null && e.longitude != "") {
 				
 			float distance = Geo.Fence(userLatitude, userLongitude, e.latitude, e.longitude);
 			e.distance = String.format("%.2f", distance);
 		}  else {
 				
 			e.distance = "N/A";
 		}
 		
 		e.Score(v_graph);
 		return e;
 	}
 	
 	private void Format(int timeZoneInMinutes) {
 		
 		// format misc.
 		this.picture	= this.pic;
 		this.invited_by = this.host;
 			
 		long timeStampStart = Long.parseLong(this.start_time) * 1000;
 		long timeStampEnd = Long.parseLong(this.end_time) * 1000;
 		
 		// facebook events timestamp are in PST
		DateTimeZone GMT = DateTimeZone.forID("GMT");
 		
 		// so need to add time zone offset to DateTime
		this.dtStart = new DateTime(timeStampStart, GMT).plusMinutes(timeZoneInMinutes);
		this.dtEnd = new DateTime(timeStampEnd, GMT).plusMinutes(timeZoneInMinutes);	
 		
 		this.time_start = dtStart.toString("KK:mm a");
 		this.time_end = dtEnd.toString("KK:mm a");
 		
 		this.date_start = dtStart.toString("MMM d, Y");
 		this.date_end = dtEnd.toString("MMM d, Y");
 		
 		if (dtStart.getDayOfYear() <= DateTime.now().getDayOfYear() && dtEnd.dayOfYear().get() >= DateTime.now().getDayOfYear()) {
 			
 			this.group = "a";
 			this.groupTitle = "Today";
 		} else {
 			
 			if (dtStart.getDayOfYear() <= DateTime.now().plusDays(1).getDayOfYear()) {
 				
 				this.group = "b";
 				this.groupTitle = "Tomorrow";
 			} else {
 				
 				if (dtStart.getDayOfYear() <= DateTime.now().plusWeeks(1).getDayOfYear()) {
 					
 					this.group = "c";
 					this.groupTitle = "This week";
 				} else {
 					
 					if (dtStart.getDayOfYear() <= DateTime.now().plusMonths(1).getDayOfYear()) {
 						
 						this.group = "d";
 						this.groupTitle = "This month"; // TODO not really this month. This is more like "In the next 30 days"
 					} else {
 						
 						this.group = "e";
 						this.groupTitle = "Later";
 					}
 				}
 			}
 		}
 	}
 	
 	/* 
 	 * - Get the number of users invited
 	 */
 	public static ArrayList<Model> getNb_invited(String accessToken, String eid) throws FacebookException {
 		
 		ArrayList<Model> result 	= new ArrayList<Model>();
 		
 		FacebookClient client 		= new DefaultFacebookClient(accessToken);
 		
 		String query 				= "SELECT uid FROM event_member WHERE eid = " + eid;
 		List<Attending> Attendings 	= client.executeQuery(query, Attending.class);
 		
 		Event e = new Event();
 		
 		e.eid = Long.parseLong(eid);
 		e.nb_invited = Attendings.size();
  	
 		result.add(e);
 		
 		return result;
 	}
 	
 	private void Score(Venue v) {
 
 		// offcourse this is not the final scoring algo :)
 		if (v != null){
 			
 			int likes = v.likes != null ? Integer.valueOf(v.likes) : 0;
 			int checkins = v.checkins != null ? Integer.valueOf(v.checkins) : 0;
 			int talking_about_count = v.talking_about_count != null ? Integer.valueOf(v.talking_about_count) : 0;	
 			double res = 0;
 			double res_vote = 0;
 
 			String eid_string = String.valueOf(this.eid);
 			
 			Dao dao = new Dao();
 			
 			Vote dsvote = new Vote();
 			
 			MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
 			Vote v_cache; 		
 			v_cache = (Vote) syncCache.get(eid_string); // read from vote cache
 			
 			if (v_cache == null) {
 		    	
 		    	//get from DS
 		    	dsvote = dao.ofy().find(Vote.class, eid_string);
 		    	
 		    	if (dsvote != null) {
 		    		
 		    		res_vote = dsvote.getVote_avg();
 		    		syncCache.put(eid_string, dsvote); // Add vote to cache
 		    	} else {
 
 		    		syncCache.put(eid_string, new Vote(eid_string, 0L, 0D)); // Add vote to cache with 0 vote and 0 average
 		    	}
 			} else {
 				
 				res_vote = v_cache.vote_avg;
 			}
 			
 			//venue score
 			if (likes >= 1 && likes < 1000){
 				res = res + 0.5;
 			}
 			if (likes >= 1000 && likes < 2000){
 				res = res + 1;
 			}
 			if (likes >= 2000){
 				res = res + 1;
 			}
 			
 			if (checkins >= 1 && checkins < 100){
 				res = res + 1;
 			}
 			if (checkins >= 100 && checkins < 200){
 				res = res + 1.5;
 			}
 			if (checkins >= 200){
 				res = res + 1.25;
 			}
 			
 			if (talking_about_count >= 1 && talking_about_count < 25){
 				res = res + 1;
 			}
 			if (talking_about_count >= 25 && talking_about_count < 50){
 				res = res + 2;
 			}
 			if (talking_about_count >= 50){
 				res = res + 2;
 			}
 			
 			this.score = res_vote == 0 ? res : (res + res_vote) / 2;
 		}
 	}
 	
 	private Boolean IsNotPast() {
 		
 		return dtEnd.isAfterNow();
 	}
 	
 	public long getEid() {
 		
 		return this.eid;
 	}
 	
 	public String getName() {
 		
 		return this.name;
 	}
 	
 	public String getPicture() {
 		
 		return this.picture;
 	}
 	
 	public String getGroup() {
 		
 		return this.group;
 	}
 	
 	public String getDescription() {
 		
 		return this.description;
 	}
 	
 	public String getVenue() {
 		
 		return this.venue;
 	}
 	
 	public String getLocation() {
 		
 		return this.location;
 	}
 	
 	public double getScore() {
 		
 		return this.score;
 	}
 	
 	public String getDistance() {
 		
 		return this.distance;
 	}
 	
 	public String getTime_start() {
 		
 		return this.time_start;
 	}
 	
 	public String getTime_end() {
 		
 		return this.time_end;
 	}
 	
 	public String getDate_start() {
 		
 		return this.date_start;
 	}
 	
 	public String getDate_end() {
 		
 		return this.date_end;
 	}
 	
 	public String getInvited_by() {
 		
 		return this.invited_by;
 	}
 	
 	public String getLatitude() {
 		
 		return this.latitude;
 	}
 	
 	public long getNb_invited() {
 		
 		return this.nb_invited;
 	}
 	
 	public String getLongitude() {
 		
 		return this.longitude;
 	}
 	
 	public String getGroupTitle() {
 		return groupTitle;
 	}
 
 	public void setGroupTitle(String groupTitle) {
 		this.groupTitle = groupTitle;
 	}
 	
 	public String getCategory() {
 		return this.venue_category;
 	}
 	
 	public void setCategory(String category) {
 		this.venue_category = category;
 	}
 }
