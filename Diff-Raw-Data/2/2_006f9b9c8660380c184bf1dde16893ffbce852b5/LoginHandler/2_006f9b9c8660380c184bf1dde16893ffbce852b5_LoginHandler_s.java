 package de.ayr.laserdb.main.controller;
 
 import javax.naming.NamingException;
 
 import com.google.inject.Inject;
 
 import de.ayr.laserdb.application.LaserWeb;
 import de.ayr.laserdb.common.services.login.control.Authentication;
 import de.ayr.laserdb.common.services.login.entity.User;
 
 public class LoginHandler {
 
 //    private AuthenticationImpl auth = new AuthenticationImpl();
     
     private static final long serialVersionUID = 2126197548761564883L;
     
             
     @Inject
     private Authentication auth;
     
     private String loginField;
     private String pwdField;
     private User user;
 
     public LoginHandler() {
                 
     }
 
    public void doLogin(String loginField, String pwdField) throws NamingException {
         
                
         this.loginField = loginField;
         this.pwdField = pwdField;
 
         user = auth.Authenticate(loginField, pwdField);
 //        System.out.println(auth2);
 
         if (user != null) {
 
             // Sets the application user and sends out an event to inform that
             // the user has logged in. UiHandler will receive this event.
             LaserWeb.getProject().setUser((Object) user.getVorname() + " " + user.getNachname());
                         
         } else {
             
             // Da getApplication nur aus einer Vaadin KOmponent heraus funktioniert wird dies durch die statische
             // ThreadLocal VAriable LaserWeb umgangen. Bei getApplication müsste diese Klasse von einem Vaadin
             // Element wie Panel oder CustomComponent extenden
             LaserWeb.getProject().getMainWindow().showNotification("Login Failed, try using demo / demo");
             //getApplication().getMainWindow().showNotification("Login Failed, try using demo / demo");
 
         }
 
     }
 
 }
