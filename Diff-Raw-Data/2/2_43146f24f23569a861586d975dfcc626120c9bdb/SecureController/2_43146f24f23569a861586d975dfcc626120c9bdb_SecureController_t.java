 package controllers;
 
 import models.User;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.With;
 
 /**
  * Controller que requiere autenticación y setea usuario loggeado.
  * 
  * 
  * @author Juan Edi
  * @since Jun 13, 2012
  */
 @With(Secure.class)
 public abstract class SecureController extends Controller {
 
     @Before
     static void setConnectedUser() {
         if (Security.isConnected()) {
             User user = connectedUser();
            renderArgs.put("loggedUser", user);
         }
     }
     
     static User connectedUser() {
         return User.find("byUsername", Security.connected()).first();
     }
     
 }
