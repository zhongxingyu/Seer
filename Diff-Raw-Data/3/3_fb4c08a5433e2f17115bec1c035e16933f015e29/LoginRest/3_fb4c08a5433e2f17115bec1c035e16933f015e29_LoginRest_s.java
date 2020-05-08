 /**
  * 
  */
 package fr.lalourche.rest;
 
 import javax.ejb.EJB;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Application;
 
 import fr.lalourche.Login;
 
 /**
  * @author Lalourche
  *
  */
 @Path("/login")
 public class LoginRest extends Application
 {
   @EJB
   private Login login;
   
   @GET
   @Path("{username}")
   @Produces("text/plain")
   public String login(@PathParam("username") String userName)
   {
     return login.enterName(userName);
   }
 }
