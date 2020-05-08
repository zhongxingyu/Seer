 package kanbannow.resources;
 
 import com.yammer.metrics.annotation.Timed;
import kanbannow.BacklogItemService;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 @Path("/hello-world")
 @Produces(MediaType.APPLICATION_JSON)
 public class HelloWorldResource {
 
 
     @GET
     @Timed
     public String sayHello() {
         return "HelloWorld";
     }
 }
