 package org.diveintojee.codestory2013;
 
 import org.springframework.stereotype.Component;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 /**
  * @author louis.gueye@gmail.com
  */
 @Component
 @Path("/")
 public class Q1Resource {
 
     @Consumes(MediaType.TEXT_PLAIN)
     @Produces(MediaType.TEXT_PLAIN)
     @GET
     public Response readQuestion(@QueryParam("q") String q) {
         final Response.ResponseBuilder ok = Response.ok();
         if ("Quelle est ton adresse email".equals(q)) {
             return ok.entity("louis.gueye@gmail.com").build();
         } else if ("Es tu heureux de participer(OUI/NON)".equals(q)) {
             return ok.entity("OUI").build();
         } else if ("Es tu abonne a la mailing list(OUI/NON)".equals(q)) {
            return ok.entity("NON").build();
         } else {
             return ok.build();
         }
     }
 }
