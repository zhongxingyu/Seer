 package uk.frequencymobile.flow.server.service;
 
 import java.util.List;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.core.Response;
 
 import uk.frequencymobile.flow.server.data.EventDAO;
 import uk.frequencymobile.flow.server.data.UserDAO;
 import uk.frequencymobile.flow.server.model.Event;
 
 
 @Path("/event")
 public class EventService extends GenericService<Event>{
 
 	EventDAO eventDao;
 	UserDAO userDao;
 
 	public EventService() {
 		super(new EventDAO());
 		eventDao = (EventDAO)dao;
 		userDao = new UserDAO();
 	}
 	
 	@GET
	@Path("/user-{id}")
 	public Response findByAuthor(@PathParam("id") long userId) {
 		List<Event> list = eventDao.findByAuthor(userId);
 		String output = list.toString();
 		return Response.status(200).entity(output).build();
 	}
 	
 }
