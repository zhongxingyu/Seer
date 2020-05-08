 package fr.keemto.web.security;
 
 import fr.keemto.core.User;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.security.authentication.AuthenticationManager;
 import org.springframework.security.authentication.BadCredentialsException;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Service;
 
 @Service("springLoginService")
 public class SpringSecurityLoginService implements LoginService {
     private Log log = LogFactory.getLog(SpringSecurityLoginService.class);
 
     @Autowired(required = false)
     @Qualifier("authenticationManager")
     AuthenticationManager authenticationManager;
 
     public LoginStatus getStatus() {
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         if (auth != null && !auth.getName().equals("anonymousUser") && auth.isAuthenticated()) {
             return new LoginStatus(true, auth.getName());
         } else {
             return new LoginStatus(false, null);
         }
     }
 
     public LoginStatus login(String username, String password) {
         UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
         User details = new User(username);
         token.setDetails(details);
 
         try {
             Authentication auth = authenticationManager.authenticate(token);
            log.debug("Login succeeded for user: " + username);
             SecurityContextHolder.getContext().setAuthentication(auth);
             return new LoginStatus(auth.isAuthenticated(), auth.getName());
         } catch (BadCredentialsException e) {
            log.warn("Unable to logged in user :" + username, e);
            //TODO add reason into response
             return new LoginStatus(false, username);
         }
     }
 }
