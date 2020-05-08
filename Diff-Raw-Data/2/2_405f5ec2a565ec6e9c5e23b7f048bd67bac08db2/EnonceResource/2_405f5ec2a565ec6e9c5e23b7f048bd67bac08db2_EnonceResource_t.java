 package org.diveintojee.codestory2013;
 
 import org.springframework.stereotype.Component;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.Response;
 
 /**
  * @author louis.gueye@gmail.com
  */
 @Component
 @Path("/enonce")
 public class EnonceResource {
 
     @Path("/{id}")
     @POST
     public Response readEnonce(@PathParam("id") Long id, String body) {
        System.out.println(body);
         final Response.ResponseBuilder ok = Response.ok();
         return ok.entity(body).build();
     }
 }
