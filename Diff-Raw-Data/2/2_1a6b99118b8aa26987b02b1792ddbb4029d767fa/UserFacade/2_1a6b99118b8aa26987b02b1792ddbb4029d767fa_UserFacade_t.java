 package ch.boxi.togetherLess.backend;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.logging.Logger;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import ch.boxi.togetherLess.businessLogic.dto.SimpleUserID;
 import ch.boxi.togetherLess.businessLogic.dto.User;
 import ch.boxi.togetherLess.dataAccess.UserDAO;
 
 @Path("/user")
 public class UserFacade {
 	private static final Logger logger = Logger.getLogger(UserFacade.class.getName());
 	
	@GET
 	@Path("register")
 	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 	public User register(
 			@QueryParam("userName") 	String userName, 
 			@QueryParam("password") 	String password, 
 			@QueryParam("firstName") 	String firstName, 
 			@QueryParam("lastName") 	String lastName, 
 			@QueryParam("email") 		String email, 
 			@QueryParam("targetWeight") int targetWeight, 
 			@QueryParam("targetDate")	String targetDate) throws ParseException{
 		logger.info("called register");
 		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
 		Date date = sdf.parse(targetDate);
 		UserDAO userDAO = new UserDAO();
 		User user = userDAO.register(userName, password, firstName, lastName, email, targetWeight, date);
 		return user;
 	}
 	
 	@GET
 	@Path("{id}")
 	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 	public User getUser(@PathParam("id") String id){
 		logger.info("called getUser");
 		UserDAO userDAO = new UserDAO();
 		SimpleUserID userID = new SimpleUserID(id);
 		User user = userDAO.getUser(userID);
 		return user;
 	}
 }
