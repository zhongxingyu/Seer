 package edu.neumont.java3.factor.service;
 
 import javax.ws.rs.core.Response;
 
 import org.apache.cxf.configuration.security.AuthorizationPolicy;
 import org.apache.cxf.jaxrs.ext.RequestHandler;
 import org.apache.cxf.jaxrs.model.ClassResourceInfo;
 import org.apache.cxf.message.Message;
 
 public class AuthenticationHandler implements RequestHandler {
 
 	public Response handleRequest(Message m, ClassResourceInfo resourceClass) {
 		AuthorizationPolicy policy = (AuthorizationPolicy) m
 				.get(AuthorizationPolicy.class);
 		if (policy != null) {
 			String username = policy.getUserName();
 			String password = policy.getPassword();
 			if (isAuthenticated(username, password)) {
 				//on with the request
 				return null;
 			
 			} else {
 				//bad
 				return Response.status(401).header("WWW-Authenticate", "Basic")
 						.build();
 			}
 		}
 			//bad
 			return Response.status(401).header("WWW-Authenticate", "Basic")
 					.build();
 		
 	}
 
 	private boolean isAuthenticated(String username, String password) {
 		return (username.equals("abc123") && password.equals("password"));
 	}
 
 }
