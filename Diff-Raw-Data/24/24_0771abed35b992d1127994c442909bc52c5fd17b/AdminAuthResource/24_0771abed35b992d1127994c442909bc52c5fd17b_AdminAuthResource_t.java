 package co.speechat.rest;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.Path;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response.Status;
 
 /**
  * TODO: Document me
  * Author: Iurii Lytvynenko
  */
 @Path("/admin")
 public class AdminAuthResource {
     private static final String AUTH_TOKEN = "auth-token";
 
     @Path("/")
     public AdminResource auth(@HeaderParam("auth-token") String requestToken, @Context HttpServletRequest r) {
         ServletContext context = r.getSession().getServletContext();
         String authToken = context.getInitParameter(AUTH_TOKEN);
         if (!authToken.equals(requestToken))
             throw new WebApplicationException(Status.FORBIDDEN);
         return new AdminResource();
    }
 }
