 package dkwestbr.spring.autowired.example;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
 
 @Path("/foo")
@Component
 public class WebEndpoint {
 
 	@Autowired
 	private IStringGetter getTheThing;
 	
 	@GET
 	@Path("/bar")
 	@Produces(MediaType.TEXT_HTML)
 	public String getStuff() {
 		System.out.println(getTheThing.getItGood());
 		return String.format("<html><body>Hello - %s</body></html>", getTheThing.getItGood());
 	}
 }
