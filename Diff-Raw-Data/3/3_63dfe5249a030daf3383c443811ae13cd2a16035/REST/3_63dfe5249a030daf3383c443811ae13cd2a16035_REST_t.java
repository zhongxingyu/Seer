 package org.jboss.pressgangccms.restserver;
 
 import java.util.ArrayList;
 
import javax.annotation.security.PermitAll;
 import javax.annotation.security.RolesAllowed;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import org.jboss.pressgangccms.restserver.structures.RESTInfo;
 import org.jboss.pressgangccms.restserver.structures.RESTVersionInfo;
 
 /**
  * This class provides a way to get information about the different versions of the REST interface that are available. It also 
  * provide the HTTP 200 response that HA proxies expect to see when requesting the root resources.
  * @author Matthew Casperson
  *
  */
 @Path("/")
 public class REST
 {
 	@GET
 	@Path("/")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
	@PermitAll
 	public RESTInfo getRESTInfo()
 	{
 		final RESTInfo restInfo = new RESTInfo();
 		restInfo.latest = 1.0f;
 		
 		final RESTVersionInfo restVersionInformation = new RESTVersionInfo();
 		restVersionInformation.version = 1.0f;
 		restVersionInformation.description = "Version 1.0 of the REST interface.";
 		
 		restInfo.versions = new ArrayList<RESTVersionInfo>();
 		restInfo.versions.add(restVersionInformation);
 		
 		return restInfo;
 	}
 }
