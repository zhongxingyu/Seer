 package gov.usgs.cida.coastalhazards.rest.data;
 
 import gov.usgs.cida.coastalhazards.jpa.ActivityManager;
 import gov.usgs.cida.coastalhazards.model.Activity;
 import gov.usgs.cida.coastalhazards.model.Activity.ActivityType;
 import java.util.Date;
import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 @Path("/activity")
 public class ActivityResource {
     
     private static ActivityManager activityManager;
     static {
         activityManager = new ActivityManager();
     }
 
    @PUT
     @Path("/{type}/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     public Response triggerItemHit(@PathParam("type") String type, @PathParam("id") String id) {
         Activity activity = new Activity();
         activity.setItemId(id);
         ActivityType actType = ActivityType.valueOf(type.toUpperCase());
         activity.setType(actType);
         activity.setActivityTimestamp(new Date());
         String response = activityManager.hit(activity);
         return Response.ok(response, MediaType.APPLICATION_JSON_TYPE).build();
     }
 }
