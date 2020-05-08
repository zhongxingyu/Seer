 package com.mailgun.api;
 
 import com.google.gson.Gson;
 import com.mailgun.api.domain.Bounce;
 import com.mailgun.api.resources.lists.Bounces;
 import com.sun.jersey.core.util.MultivaluedMapImpl;
 
 import javax.ws.rs.core.MultivaluedMap;
 
 
 public class MailGunExample {
	
 	public static void main (String args[]){
 
        // test mailgun api key
	    MailGunClient client = new MailGunClient("key-3ax6xnjp29jd6fds4gc373sgvjxteol0","samples.mailgun.org");
 
	    Gson gson = new Gson();
	    gson.getAdapter(Bounces.class);
        MultivaluedMap map = new MultivaluedMapImpl();
        map.add("limit","2");
        Bounces list = client.getBounces(map);
 		
	    for (Bounce bounce : list){
		    System.out.println(bounce.getAddress() + " " + bounce.getCreatedAt());
		    System.out.println(gson.toJson(bounce));
	    }
    }
 }
 
