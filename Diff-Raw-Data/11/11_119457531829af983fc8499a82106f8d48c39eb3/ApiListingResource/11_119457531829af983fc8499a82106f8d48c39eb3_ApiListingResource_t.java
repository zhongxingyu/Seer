 /**
  * 
  */
 package uk.co.o2.customer;
 
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 
 import com.wordnik.swagger.jaxrs.JavaApiListing;
 
 @Path("/resources.json")
//@Api("/resources")
 @Produces({"application/json"})
 public class ApiListingResource extends JavaApiListing {}
