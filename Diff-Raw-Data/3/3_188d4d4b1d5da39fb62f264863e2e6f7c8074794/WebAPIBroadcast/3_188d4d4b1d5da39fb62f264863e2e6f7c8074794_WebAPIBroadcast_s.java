 package com.avalchev.ide.webapp;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 
 import org.atmosphere.annotation.Broadcast;
 import org.atmosphere.cpr.Broadcaster;
 import org.atmosphere.jersey.SuspendResponse;
 
 @Path("/bus/{broadcasterId}")
 @Produces("application/json")
 public class WebAPIBroadcast {
 
 	private
     @PathParam("broadcasterId")
     Broadcaster topic;
 	
 	@GET
     public SuspendResponse<String> subscribe(@Context HttpServletRequest request) {
         return new SuspendResponse.SuspendResponseBuilder<String>()
                 .broadcaster(topic)
                 .outputComments(true)
                 .build();
     }
 	
 	@Broadcast(writeEntity = false)
 	@POST
     @Produces("application/json")
 	public String broadcast(String t) {
 		System.out.println("Broadcast " + t);
 		return t;
 	}
 }
