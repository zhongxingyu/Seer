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
 	String pic_big;
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
 	@Facebook
 	String privacy;
 	@Facebook
 	String update_time;
 	
 	String venue_id;
 	double score;
 	double nb_attending;
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
 	String offer_link; // for the future, could be a barcode etc.
 	String offer_title;
 	String offer_description;
 	String ticket_link; //link to a website provided by promoter (later a link to our own ticket system)
 	String guest_list; // Open, close, full or no guest list
 	double female_ratio; // female:%
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
 		String properties 		= "eid, name, pic, pic_big, start_time, end_time, venue, location, host, privacy, update_time";
 		String query 			= "SELECT " + properties + " FROM event WHERE eid IN (SELECT eid FROM event_member WHERE uid = " + userID + ") AND end_time > " + TAS + " ORDER BY start_time"; /*need to check privacy CLOSED AND SECRET */
 		List<Event> fbevents 	= client.executeQuery(query, Event.class);
 		
 		Dao dao = new Dao();
 		
 		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
 		Event e_cache; 
 		
 		int timeZoneInMinutes = Integer.parseInt(timeZone);
 		
 		for (Event e : fbevents) {
 				
 			e_cache = (Event) syncCache.get(e.eid); // read from Event cache
     	    if (e_cache == null) {
 			 	    	
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
 			        	
     	    			EventLocationCapable elc = new EventLocationCapable(e);
     	    			dao.ofy().put(elc);
     	    		}
     	    		
     	    	} else {
 								
     	    		e.distance = "N/A";
     	    	}
     	    	
     	    	e.GetNb_attending_and_gender_ratio(accessToken, String.valueOf(e.eid));
     	    	e.Score(v_graph);
     	    }
 	
 	    	e.Format(timeZoneInMinutes);
     	    
 	    	if (e.latitude != null && e.latitude != "" && e.longitude != null && e.longitude != "") {
 
     	    	float distance = Geo.Fence(userLatitude, userLongitude, e.latitude, e.longitude);
         	    e.distance = String.format("%.2f", distance);
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
 		
 		//Prepare a timestamp to filter on current dateTime
 		DateTimeZone PST = DateTimeZone.forID("America/Los_Angeles");
 		DateTime now = new DateTime(PST);
 		now.plusMinutes(PST.getOffset(now));
 		long actual_time = now.getMillis() / 1000;
 		
 		LocationCapableRepositorySearch<EventLocationCapable> ofySearch = new OfyEntityLocationCapableRepositorySearchImpl(dao.ofy(), timeZone, searchTimeFrame);
 		List<EventLocationCapable> l = GeocellManager.proximityFetch(new Point(Double.parseDouble(userLatitude), Double.parseDouble(userLongitude)), searchLimit, searchRadius * 1000, ofySearch);
 		
 		FacebookClient client 	= new DefaultFacebookClient(accessToken);
 		String properties 		= "eid, name, pic, pic_big, start_time, end_time, venue, location, host, privacy";
 		
 		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
 		
 		int timeZoneInMinutes = Integer.parseInt(timeZone);
 		Event event;
 		
         for (EventLocationCapable e : l) {
         	
         	if (actual_time < e.getTimeStampEnd()) { //if event not in the past
         		
             	event = (Event) syncCache.get(e.getEid());
             	if (event == null) { // if not in the cache
        	
             		String query 			= "SELECT " + properties + " FROM event WHERE eid = " + e.getEid();
             		List<Event> fbevents 	= client.executeQuery(query, Event.class);
             	
             		if (fbevents != null && fbevents.size() > 0) {
         	
             			event = fbevents.get(0);
             			event.venue_id = JSON.GetValueFor("id", event.venue);
         				Venue v_graph = new Venue(accessToken, event.venue_id);
         				event.venue_category = v_graph.category;
         				event.Score(v_graph);
         				event.GetNb_attending_and_gender_ratio(accessToken, String.valueOf(event.eid));
             		}
             	}
             	
             	if (event != null) {
             		
         			event.Format(timeZoneInMinutes);    		
         			
         			event.latitude 	= Double.toString(e.getLatitude());
         			event.longitude = Double.toString(e.getLongitude());
         			
         			float distance = Geo.Fence(userLatitude, userLongitude, event.latitude, event.longitude);
         			event.distance = String.format("%.2f", distance);
 
         			syncCache.put(event.eid, event); //add event to cache
             		
         			result.add(event);
             	} 
         	} else { // event in the past
 					
         		dao.ofy().delete(e); //clean the datastore by removing old events TODO: call a task doesn't have to be deleted right away
         	}
         }	
         return result;    
 	}
 
 	
 	public static void GetCron() throws FacebookException {
 		
 		Dao dao = new Dao();
 		
 		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
 		
 		//get users and Access tokens from DS		
 		Query<User> quser = dao.ofy().query(User.class);
 		for (User u: quser) {
 
 			try {
 				
 				//Get friend list 
 				List<Friend> uidList = Friend.GetCron(u.getAccess_token(), Long.toString(u.getUid()), syncCache);
 				
 		    	//Loop to get all events	
 				for (Friend l : uidList) {
 				
 					try {
 						
 						//Prepare a timestamp to filter the facebook DB on the upcoming events
 						DateTimeZone PST = DateTimeZone.forID("America/Los_Angeles");
 						DateTime now = new DateTime(PST);
 						now.plusMinutes(PST.getOffset(now));
 						String TAS = String.valueOf(now.getMillis() / 1000);
 						
 						FacebookClient client 	= new DefaultFacebookClient(u.getAccess_token());
 						String properties 		= "eid, name, pic, pic_big, start_time, end_time, venue, location, host, privacy, update_time";
 						String query 			= "SELECT " + properties + " FROM event WHERE eid IN (SELECT eid FROM event_member WHERE uid = " + l.getUid() + ") AND end_time > " + TAS; /*need to check privacy CLOSED AND SECRET */
 						List<Event> fbevents 	= client.executeQuery(query, Event.class);
 						
 						
 						Event e_cache; 
 						
 						for (Event e : fbevents) {
 							
 							e_cache = (Event) syncCache.get(e.eid); // read from Event cache
 				    	    if (e_cache == null) {
 							 	    	
 				    	    	e.venue_id = JSON.GetValueFor("id", e.venue);    	
 				    	    	Venue v_graph = new Venue(u.getAccess_token(), e.venue_id);
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
 							        	
 				    	    			EventLocationCapable elc = new EventLocationCapable(e);
 				    	    			dao.ofy().put(elc);
 				    	    		}
 				    	    		
 				    	    	} else {
 												
 				    	    		e.distance = "N/A";
 				    	    	}
 				    	    	
 				    	    	e.GetNb_attending_and_gender_ratio(u.getAccess_token(), String.valueOf(e.eid));
 				    	    }
 				    	    syncCache.put(e.eid, e); // Add Event to cache
 						}
 					} catch (Exception ex ) {}
 				}		
 			} catch (Exception ex) {}
 		}
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
         	
     	String query 			= "SELECT eid, name, pic, pic_big, start_time, end_time, venue, location, host, privacy FROM event WHERE eid = " + eid;
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
 		DateTimeZone T = DateTimeZone.forID("America/Los_Angeles");
 		
 		// so need to add time zone offset to DateTime
 		this.dtStart = new DateTime(timeStampStart, T);
 		this.dtEnd = new DateTime(timeStampEnd, T);
 		
 		this.time_start = dtStart.toString("KK:mm a");
 		this.time_end = dtEnd.toString("KK:mm a");
 		
 		this.date_start = dtStart.toString("MMM d, Y");
 		this.date_end = dtEnd.toString("MMM d, Y");
 		
 		DateTimeZone GMT = DateTimeZone.forID("GMT");		
 		DateTime now = DateTime.now(GMT).plusMinutes(timeZoneInMinutes);
 		
 		int end_minus_start = dtEnd.getDayOfYear() - dtStart.getDayOfYear();
 		
 		if (dtStart.getDayOfYear() <= now.getDayOfYear() && dtEnd.dayOfYear().get() >= now.getDayOfYear()) {
 			
 			if (end_minus_start >= 7) { // to filter bogus "Fridays", "Tuesdays" events
 				
 				this.Filter_bogus_events(now);
 			} else {
 				
 				this.group = "a";
 				this.groupTitle = "Today";
 			}
 		} else {
 			
 			if (dtStart.getDayOfYear() <= now.plusDays(1).getDayOfYear()) {
 				
 				this.group = "b";
 				this.groupTitle = "Tomorrow";
 			} else {
 				
 				if (dtStart.getWeekyear() == now.getWeekyear()) {
 					
 					this.group = "c";
 					this.groupTitle = "This week";
 				} else {
 					
 					if (dtStart.getMonthOfYear() == now.getMonthOfYear()) {
 						
 						this.group = "d";
 						this.groupTitle = "This month";
 					} else {
 						
 						this.group = "e";
 						this.groupTitle = "Later";
 					}
 				}
 			}
 		}
 	}
 	
 	public void Filter_bogus_events(DateTime now_userTZ) {
 		
 		String name = this.name.toLowerCase();
 		
 		int dayofweek = now_userTZ.getDayOfWeek();		
 		
 		int dayindex = name.indexOf("day");
 		
 		String day;
 		int dayoffweek_name = 0;
 		
 		if (dayindex != -1 && dayindex > 3) {
 			
 			day = name.substring(dayindex - 3, dayindex);
 			
 			if (day.contains(" ")){
 				
 				this.group = "a";
 				this.groupTitle = "Today";
 			} else {
 				
 				if (day.equals("mon")) {dayoffweek_name = 1;} else
 					if (day.equals("ues")) {dayoffweek_name = 2;} else
 						if (day.equals("nes")) {dayoffweek_name = 3;} else
 							if (day.equals("urs")) {dayoffweek_name = 4;} else
 								if (day.equals("fri")) {dayoffweek_name = 5;} else
 									if (day.equals("sat")) {dayoffweek_name = 6;} else
 										if (day.equals("sun")) {dayoffweek_name = 7;} 
 											
 				
 				if (dayoffweek_name - dayofweek < 0) {
 					
 					this.group = "d";
 					this.groupTitle = "This month";
 				} else if (dayoffweek_name - dayofweek > 0) {
 					
 					this.group = "c";
 					this.groupTitle = "This week";
 				} else {
 					
 					this.group = "a";
 					this.groupTitle = "Today";
 				}
 			}
 		} else {
 			
 			this.group = "a";
 			this.groupTitle = "Today";
 		}
 	}
 	
 	/* 
 	 * - Get the number of users invited
 	 */
 	public void GetNb_attending_and_gender_ratio(String accessToken, String eid) throws FacebookException {
 		
 		Attending a = new Attending();
 		
 		Event event = a.GetNb_attending_and_gender_ratio(accessToken, eid);
 		
 		this.nb_attending = event.nb_attending;
 		this.female_ratio = event.female_ratio;
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
 	
 	public double getNb_attending() {
 		
 		return this.nb_attending;
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
 	
 	public String getVenue_category() {
 		return this.venue_category;
 	}
 	
 	public void setVenue_category(String category) {
 		this.venue_category = category;
 	}
 
 	public String getOffer_link() {
 		return offer_link;
 	}
 
 	public void setOffer_link(String offer_link) {
 		this.offer_link = offer_link;
 	}
 
 	public String getOffer_title() {
 		return offer_title;
 	}
 
 	public void setOffer_title(String offer_title) {
 		this.offer_title = offer_title;
 	}
 
 	public String getOffer_description() {
 		return offer_description;
 	}
 
 	public void setOffer_description(String offer_description) {
 		this.offer_description = offer_description;
 	}
 
 	public String getTicket_link() {
 		return ticket_link;
 	}
 
 	public void setTicket_link(String ticket_link) {
 		this.ticket_link = ticket_link;
 	}
 
 	public String getGuest_list() {
 		return guest_list;
 	}
 
 	public void setGuest_list(String guest_list) {
 		this.guest_list = guest_list;
 	}
 
 	public double getFemale_ratio() {
 		return female_ratio;
 	}
 
 	public void setFemale_ratio(double female_ratio) {
 		this.female_ratio = female_ratio;
 	}
 
 	public String getPic_big() {
 		return pic_big;
 	}
 
 	public void setPic_big(String pic_big) {
 		this.pic_big = pic_big;
 	}
 }
