 package uk.co.optimisticpanda.dropwizard.event;
 
 import java.util.Collections;
 import java.util.List;
 
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
 
 import uk.co.optimisticpanda.dropwizard.Environment;
 
 import com.yammer.metrics.annotation.Timed;
 
 @Produces({ MediaType.APPLICATION_ATOM_XML })
 @Path("/environment-event/")
 public class EventResource {
 
 	public static enum Categories{ CREATE, DELETE, UPDATE}
 	private final EventStore store;
 
	@Context
    UriInfo uriInfo;
	
 	public EventResource(EventStore store) {
 		this.store = store;
 	}
 
 	@GET
 	@Timed
 	@Path("")
 	public EventList<Environment,Categories> getEnvironmentEvents() {
 		return wrap(store.getEvents());
 	}
 
 	@GET
 	@Timed
 	@Path("{id}")
 	public EventList<Environment,Categories> getEnvironmentEvent(String uuid) {
 		return wrap(Collections.singletonList(store.getEvent(uuid)));
 	}
 
 	@POST
 	@Timed
 	@Path("")
 	//Requires more thought! - generic parse
 	public void addEvent(Event<Environment, Categories> event) {
 		store.addEvent(event);
 	}
 
 	@DELETE
 	@Timed
 	@Path("{id}")
 	public void deleteEvent(@PathParam("id") String id) {
 		store.deleteEvent(id);
 	}
 
 	private EventList<Environment,Categories> wrap(List<Event<Environment,Categories>> payload){
 		return new EventList<Environment,Categories>(
 				payload, //
 				Categories.class, 
				uriInfo.getAbsolutePath().toString(), //
				uriInfo.getBaseUri().toString() + "environment/" //
 				);
 	}
 }
