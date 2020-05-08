 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.pv243.mymaps.jsf;
 
 import cz.muni.fi.pv243.mymaps.dto.User;
 import cz.muni.fi.pv243.mymaps.service.UserService;
 import cz.muni.fi.pv243.mymaps.util.Crypto;
 import java.io.UnsupportedEncodingException;
 import java.security.NoSuchAlgorithmException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.annotation.ManagedBean;
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 import org.jboss.seam.security.BaseAuthenticator;
 import org.jboss.seam.security.Credentials;
 import org.jboss.seam.security.Identity;
 import org.picketlink.idm.impl.api.PasswordCredential;
 import org.picketlink.idm.impl.api.model.SimpleUser;
 
 /**
  *
  * @author andrej
  */
 @ManagedBean
 @RequestScoped
 @Named(value = "login")
 public class LoginBean extends BaseAuthenticator {
 
     @Inject
     Identity identity;
     @Inject
     private Credentials credentials;
     @Inject
     UserService userService;
     @Inject
     private org.jboss.logging.Logger log;
 
     public String login() {
         identity.login();
         if (!identity.isLoggedIn()) {
             return "login.xhtml";
         }
         return "maps.xhtml";
     }
 
     @Override
     public void authenticate() {
         User user = userService.getUserByLogin(credentials.getUsername());
         if (user == null) {
             setStatus(AuthenticationStatus.FAILURE);
         } else {
             try {
                 PasswordCredential pc = (PasswordCredential) credentials.getCredential();
                 String pass = pc.getValue();
                 if (user.getPassword().equals(Crypto.encode(pass))) {
                     setStatus(AuthenticationStatus.SUCCESS);
                    org.picketlink.idm.api.User seamUser = new SimpleUser(user.getId().toString());
                     setUser(seamUser);
 
 
                 } else {
                     setStatus(AuthenticationStatus.FAILURE);
                 }
             } catch (NoSuchAlgorithmException ex) {
                 log.error(ex);
             } catch (UnsupportedEncodingException ex) {
                 log.error(ex);
             }
         }
 
     }
 
     public Credentials getCredentials() {
         return credentials;
     }
 
     public void setCredentials(Credentials credentials) {
         this.credentials = credentials;
     }
     
     
 }
