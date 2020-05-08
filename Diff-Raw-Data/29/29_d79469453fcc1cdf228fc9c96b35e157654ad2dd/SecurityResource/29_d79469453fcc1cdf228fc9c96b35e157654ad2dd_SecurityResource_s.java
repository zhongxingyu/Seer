 package it.dariofabbri.ivncr.service.rest.resource;
 
 
 import it.dariofabbri.ivncr.model.security.User;
 import it.dariofabbri.ivncr.service.rest.dto.ActionGrantDTO;
 import it.dariofabbri.ivncr.service.rest.dto.SecurityDTO;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.authc.UsernamePasswordToken;
 import org.apache.shiro.session.Session;
 import org.apache.shiro.session.SessionException;
 import org.apache.shiro.session.mgt.DefaultSessionKey;
 import org.apache.shiro.subject.Subject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Path("/security")
 @Produces("application/json")
 public class SecurityResource {
 
 	private static final Logger logger = LoggerFactory.getLogger(SecurityResource.class);
 	
 	@POST
 	@Path("/sessions")
 	@Consumes("application/json")
 	public Response createSession(SecurityDTO dto) {
 
 		if(dto == null) {
 			return Response
 				.status(Status.INTERNAL_SERVER_ERROR)
 				.entity("No credentials received.")
 				.build();
 		}
 		
 		Subject.Builder builder = new Subject.Builder();
 		Subject currentUser = builder.buildSubject();
 
 		UsernamePasswordToken upt = new UsernamePasswordToken(
 				dto.getUsername(),
 				dto.getPassword());
 		currentUser.login(upt);
 		String sessionId = currentUser.getSession(true).getId().toString();
 		logger.info("Session id: " + sessionId);
 
 		// Extract some useful data from current user.
 		//
 		for(Object principal : currentUser.getPrincipals()) {
 			if(principal instanceof User) {
 				
 				User user = (User)principal;
 				dto.setName(user.getFirstName());
 				dto.setSurname(user.getLastName());
 				break;
 			}
 		}
 		
 		dto.setPassword(null);
 		dto.setSecurityToken(sessionId);
 		dto.setLoggedOn(true);
 		dto.setLogonTs(new Date());
 
 		return Response
 			.ok()
 			.entity(dto)
 			.build();
 	}
 	
 	
 	@DELETE
 	@Path("/sessions/{token}")
 	public Response deleteSession(
 			@PathParam("token") String token) {
 
 		// SecurityResource token must be present.
 		//
 		if(token == null) {
 			return Response
 					.status(Status.INTERNAL_SERVER_ERROR)
 					.entity("No token received.")
 					.build();
 		}
 		
 		
 		try {
 			
 			// Check if the passed token is associated with a valid session.
 			//
 			Session session = SecurityUtils.getSecurityManager().getSession(new DefaultSessionKey(token));
 			if(session == null) {
 				return Response
 						.status(Status.NOT_FOUND)
 						.entity("Session not found using provided token.")
 						.build();
 			}
 			
 			// Terminate current session.
 			//
 			session.stop();
 			return Response
 					.ok()
 					.build();
 
 		} catch(SessionException se) {
 			return Response
 					.status(Status.NOT_FOUND)
 					.entity("Session not found using provided token.")
 					.build();		
 		}
 	}
 	
 	
 	@GET
	@Path("/permissions/{action}")
	public Response checkPermission(
 			@PathParam("action") String action) {
 
		logger.debug("checkPermission called!");
 
 		Subject currentUser = SecurityUtils.getSubject();
 		
 		boolean allowed = currentUser.isPermitted(action);
 
 		ActionGrantDTO dto = new ActionGrantDTO();
 		dto.setAction(action);
 		dto.setAllowed(allowed);
 		
 		return Response
 			.ok()
 			.entity(dto)
 			.build();
 	}
 	
 	
 	@GET
	@Path("/permissions")
	public Response checkPermissions(
 			@QueryParam("actions") String actions) {
 
		logger.debug("checkPermissions called!");
 
 		String[] permissions = actions.split(",");
 		
 		Subject currentUser = SecurityUtils.getSubject();
 
 		boolean[] result = currentUser.isPermitted(permissions);
 		if(result.length != permissions.length)
 			throw new RuntimeException("Unexpected number of results obtained while checking permissions.");
 		
 		List<ActionGrantDTO> list = new ArrayList<ActionGrantDTO>();
 		for(int i = 0; i < result.length; ++i) {
 			
 			ActionGrantDTO dto = new ActionGrantDTO();
 			dto.setAction(permissions[i]);
 			dto.setAllowed(result[i]);
 			
 			list.add(dto);
 		}
 
 		return Response
 			.ok()
 			.entity(list)
 			.build();
 	}	
 
 }
