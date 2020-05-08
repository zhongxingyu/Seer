 package com.chaman.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.restfb.DefaultFacebookClient;
 import com.restfb.Facebook;
 import com.restfb.FacebookClient;
 import com.restfb.exception.FacebookException;
 import com.restfb.types.FacebookType;
 
 public class Attending extends Model {
 
 	@Facebook
 	long uid;
 	@Facebook
 	String first_name;
 	@Facebook
 	String last_name;
 	@Facebook("pic")
 	String picture;
 	@Facebook
 	String sex;
 	@Facebook
 	String rsvp_status;
 	
 	Long eid;
 	//String picture;
 	
 	public Attending() {
 		super();
 	}
 	
 	public static ArrayList<Model> GetInvitedFriendsList(String accessToken, String userID, String eid) throws FacebookException {
 		
 		ArrayList<Model> result 	= new ArrayList<Model>();
 		
 		FacebookClient client 		= new DefaultFacebookClient(accessToken);
 		
 		Long eid_long = Long.parseLong(eid);
 
 		//initiate multiquery for FB (one call for multiple queries = optimization)
 		Map<String, String> queries = new HashMap<String, String>();
 		queries.put("friends", "SELECT uid2 FROM friend WHERE uid1 = me()");
 		queries.put("invited_rsvp", "SELECT uid, rsvp_status FROM event_member WHERE eid = " + eid + " AND uid in (select uid2 from #friends)");
 		queries.put("invited_info", "SELECT uid, first_name, last_name, pic FROM user WHERE uid in (select uid from #invited_rsvp)");
 
 		MultiqueryResults multiqueryResult = client.executeMultiquery(queries, MultiqueryResults.class);
 		
 		for (int i=0; i<multiqueryResult.invited_info.size(); i++) {
 		
 			Attending a = multiqueryResult.invited_info.get(i);
 			
 			a.eid = eid_long;
 			
 			a.rsvp_status = multiqueryResult.invited_rsvp.get(i).rsvp_status;
 			
 			if (a.rsvp_status. equals("not_replied")) {
 				
 				a.rsvp_status = "not replied";
 			} else if (a.rsvp_status.equals("declined")) {	
 				
 				a.rsvp_status = "not attending";
 			} else if (a.rsvp_status.equals("unsure")){
 				
 				a.rsvp_status = "maybe attending";
 			}
 			
 			result.add(a);
 		}
  	
 		return result;
 	}
 	
 	/* 
 	 * - Get the list (picture) of users ATTENDING (not all the invited people) to the event
 	 */
 	public static ArrayList<Model> GetAttendingAllList(String accessToken, String eid) throws FacebookException {
 		
 		ArrayList<Model> result 	= new ArrayList<Model>();
 		
 		FacebookClient client 		= new DefaultFacebookClient(accessToken);
 		
 		Long eid_long = Long.parseLong(eid);
 
 		//initiate multiquery for FB (one call for multiple queries = optimization)
 		Map<String, String> queries = new HashMap<String, String>();
 		queries.put("invited_rsvp", "SELECT uid FROM event_member WHERE eid = " + eid + " AND rsvp_status = 'attending'");
 		queries.put("invited_info", "SELECT uid, first_name, last_name, pic, sex FROM user WHERE uid in (select uid from #invited_rsvp)");
 
 		MultiqueryResults multiqueryResult = client.executeMultiquery(queries, MultiqueryResults.class);
 		
 		for (int i=0; i<multiqueryResult.invited_info.size(); i++) {
 		
 			Attending a = multiqueryResult.invited_info.get(i);
 			
 			a.eid = eid_long;
 			
 			result.add(a);
 		}
  	
 		return result;		
 	}
 	
 	/* 
 	 * - Get the list of users ATTENDING (not all the invited people) to the event (pic name rsvp status)
 	 * - Fill the number of user invited (this.nb_invited)
 	 */
 	public static Event GetNb_attending_and_gender_ratio(String accessToken, String eid) throws FacebookException {
 
 		double male 			= 0;
 		double female			= 0;
 		
 		Event event 			= new Event();
 		
 		double nb_attending 	= 0;
 		
 		FacebookClient client 	= new DefaultFacebookClient(accessToken);
 
 		Long eid_long = Long.parseLong(eid);
 		
 		//initiate multiquery for FB (one call for multiple queries = optimization)
 		Map<String, String> queries = new HashMap<String, String>();
 		queries.put("invited_rsvp", "SELECT uid FROM event_member WHERE eid = " + eid + " AND rsvp_status = 'attending'");
 		queries.put("invited_info", "SELECT sex FROM user WHERE uid in (select uid from #invited_rsvp)");
 
 		MultiqueryResults multiqueryResult = client.executeMultiquery(queries, MultiqueryResults.class);
 		
 		for (int i=0; i<multiqueryResult.invited_info.size(); i++) {
 		
 			Attending a = multiqueryResult.invited_info.get(i);
 			
 			event.eid = eid_long;
 			
 			if (a.sex.equals("male")) {
 				male++;
 			} else if (a.sex.equals("female")) {
 				female++;
 			}
 	
 			nb_attending++;
 		}
 		
 		event.nb_attending = nb_attending;
 		
 		if (female + male != 0){
 			
 			event.female_ratio = female / (female + male);
 		}
 	
 		return event;		
 	}
 	
 	public static void SetFacebookRsvp (String accessToken, String eid, String rsvp) {
 		
 		FacebookClient client	= new DefaultFacebookClient(accessToken);
 		
 		if (rsvp.equals("yes")) {
 			client.publish(eid + "/attending", FacebookType.class);
 		} else if (rsvp.equals("no")) {
 			client.publish(eid + "/declined", FacebookType.class);
 		} else {
 			client.publish(eid + "/maybe", FacebookType.class);
 		}
 	}
 	
 	public static ArrayList<Model> GetFacebookRsvp (String accessToken, String userid, String eid) {
 		
 		ArrayList<Model> result 	= new ArrayList<Model>();
 		FacebookClient client		= new DefaultFacebookClient(accessToken);
 		String query 				= "SELECT rsvp_status, uid FROM event_member WHERE eid = " + eid + "AND uid = " + userid;
 		List<Attending> user_rsvp 	= client.executeQuery(query, Attending.class);
 		
 		if (!user_rsvp.isEmpty()) {
 			
 			Attending a = user_rsvp.get(0);
 			a.eid = Long.parseLong(eid);
 			if (a.rsvp_status. equals("not_replied")) {
 				
 				a.rsvp_status = "not replied";
 			} else if (a.rsvp_status.equals("declined")) {	
 				
 				a.rsvp_status = "not attending";
 			} else if (a.rsvp_status.equals("unsure")){
 				
 				a.rsvp_status = "maybe attending";
 			}
 			
 			result.add(a);
 		}
 
 		return result;
 	}
 	
 	public long getUid() {
 		
 		return this.uid;
 	}
 	
 	public String getFirst_name() {
 		
 		return this.first_name;
 	}
 	
 	public String getLast_name() {
 		
 		return this.last_name;
 	}
 	
 	public String getPicture() {
 		
 		return this.picture;
 	}
 	
 	public String getRsvp_status() {
 		
 		return this.rsvp_status;
 	}
 	
 	public long getEid() {
 		
 		return this.eid;
 	}
 
 	public String getSex() {
 		return sex;
 	}
 
 	public void setSex(String sex) {
 		this.sex = sex;
 	}
 }
