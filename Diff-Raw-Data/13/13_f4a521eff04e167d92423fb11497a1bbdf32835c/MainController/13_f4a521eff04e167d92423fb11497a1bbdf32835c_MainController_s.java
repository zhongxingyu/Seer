 package org.example;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 
import com.googlecode.htmleasy.View;
 import com.googlecode.htmleasy.ViewWith;
 
 @Path("/")
 public class MainController {
 	
 	@GET 
 	@Path("/")
	public View showIndex() {
		return new View("/soy/main.index");
 	}
	
 }
