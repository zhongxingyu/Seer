 package org.example;
 
import java.util.Map;

 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 
import com.google.common.collect.ImmutableMap;
 import com.googlecode.htmleasy.ViewWith;
 
 @Path("/")
 public class MainController {
 	
 	@GET 
 	@Path("/")
	@ViewWith("/soy/main.index")
	public Map<String, ?> showIndex() {
		return ImmutableMap.of();
 	}
 }
