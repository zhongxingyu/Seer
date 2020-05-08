 package com.projectmaxwell.service.resource.filter;
 
 import java.security.Principal;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.SecurityContext;
 
 import com.projectmaxwell.exception.InvalidTokenException;
 
 
 @Produces("application/json")
 public class PhiAuthSecurityContext implements SecurityContext {
 
 	private static final Logger LOGGER = Logger.getLogger(PhiAuthSecurityContext.class.getName());
 	
 	private final TokenWrapper token;
 	private final Session session;
 
 	public PhiAuthSecurityContext(Session session, TokenWrapper token) {
 		this.session = session;
 		this.token = token;
 	}
 
 	@Override
 	public String getAuthenticationScheme() {
 		return SecurityContext.DIGEST_AUTH;
 	}
 
 	@Override
 	public Principal getUserPrincipal() {
 		return token;
 	}
 
 	@Override
 	public boolean isSecure() {
 		return (null != session) ? session.isSecure() : false;
 	}
 
 	@Override
 	public boolean isUserInRole(String scope) {
 
 		if (null == session || !session.isActive()) {
 			//TODO: Use session stuff or delete it
 			System.out.println("Warning: Authorization was passed insecurely");
			// Forbidden
			/*Response denied = Response.status(Response.Status.FORBIDDEN).entity("Permission Deniedasdf").build();
			throw new WebApplicationException(denied);*/
 		}
 		
 		if(token == null){
 			LOGGER.log(Level.SEVERE,"No token provided.");
 			throw new InvalidTokenException(String.valueOf(Math.random()),"No authorization token was passed with request.");
		}else{
			System.out.println("Token provided.");
			LOGGER.log(Level.SEVERE,"Token provided.");
 		}
 
 		try {
 			// this user has this scope?
 			return token.getScopesAsSet().contains(scope);
 		} catch (Exception e) {
			e.printStackTrace();
 			LOGGER.log(Level.SEVERE,e.getMessage());
 			throw new InvalidTokenException(String.valueOf(Math.random()),"Could not compare returned scopes with allowed scopes due to exception.");
 		}
 	}
 }
