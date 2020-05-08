package ut.veeb.lambiprojekt123;

 import javax.ws.rs.GET;
import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 @Path("/hr/")
public class Lambiprojekt123RestServlet {
 	  
 	 @GET
 	 @Produces("text/plain")  //("application/json")
 	 @Path("/canditate")
 	 public String getCanditate() {
 	        return "Hello World!";
     }
 }
