 package com.venus.controller.auth;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.Cookie;
 
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.providers.AuthenticationProvider;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
 import org.springframework.beans.factory.InitializingBean;
 
 import java.util.List;
 import java.util.ArrayList;
 import org.apache.log4j.Logger;
 
 public class authnProvider implements AuthenticationProvider, InitializingBean {
   public authnProvider() {
   }
 
   /**
    * Not sure about this handler
    * 
    * @param authn    The authn object
    * @return
    */
   public boolean supports(Class authn) {
     return true;
   }
 
   private static final Logger log = Logger.getLogger(authnProvider.class);
 
   /**
    * Authenticate the user
    * @param token    The authn object
    * @return The authentication object with more details
    * @throws AuthenticationException thrown if the user is not authenticated
    */
   public Authentication authenticate(Authentication token) throws AuthenticationException {
     UsernamePasswordAuthenticationToken upToken;
     String username = (String)token.getPrincipal();
     String password = (String)token.getCredentials();
 
     log.info("Got username: " + username + ", password: " + password);
 
     /* See if the username is passed or not */
     if ((username == null) || ("".equals(username)) || ("".equals(username.trim()))) {
       throw new BadCredentialsException("UserName is not supplied or is empty");
     }
 
     List<GrantedAuthority> gaList = new ArrayList<GrantedAuthority>(2);
 
 
     /*XXX: lets give users permission to everybody  */
     GrantedAuthority ga1 = new GrantedAuthorityImpl(Role.getRoleWithPrefix(Role.Member.toString()));
     gaList.add(ga1);
 
     /* Every user will be given access as Anonymous -- Why? I dont know for now */
     GrantedAuthority ga2 = new GrantedAuthorityImpl(Role.getRoleWithPrefix(Role.Anonymous.toString()));
     gaList.add(ga2);
 
     if ("ravi".equals(username)) {
       GrantedAuthority ga3 = new GrantedAuthorityImpl(Role.getRoleWithPrefix(Role.Admin.toString()));
       gaList.add(ga3);      
     }
 
     /* Add the roles to the token */
     upToken = new UsernamePasswordAuthenticationToken(username, token.getCredentials(), (GrantedAuthority[]) gaList.toArray(new GrantedAuthority[gaList.size()]));
     upToken.setDetails(token.getDetails());
 
     log.info("AuthnProvider: returning token for: " + username);
     
     return upToken;
   }
 
   
   /**
    * This API will be called after all of the properties are set
    * @throws Exception thrown when there is any error
    */
   public final void afterPropertiesSet() throws Exception {
     //do nothing
     log.info("---------after properties----------");
   }
 
 }
   
