 package au.csiro.notify.web.rest;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.core.Response;
 
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.jaxrs.interceptor.JAXRSInInterceptor;
import org.apache.cxf.transport.servlet.ServletController;

 import au.csiro.notify.web.rest.dto.NotificationDTO;
 
 /**
  * note about the resource class
  * This class is used to configure the JAXRS properties ONLY. 
  * methods will NOT be executed during the routing of messages to the endpoint, 
  * the route itself is responsible for ALL processing instead.
  * @author Simon
  */
@Path("/notificationService")
//@Path("/")
 public class NotificationService {
 
 	@POST
 	@Path("/")
 	public Response create(NotificationDTO notification)
 	{
	    ServletController s;
	    JAXRSInInterceptor i;
 		return null;
 	}
	
	@GET
    @Path("/")
    public Response get()
    {
        return null;
    }
 
 	@GET
 	@Path("/{id}")
 	public Response view(@PathParam("id") long id)
 	{
 		return null;
 	}
 }
