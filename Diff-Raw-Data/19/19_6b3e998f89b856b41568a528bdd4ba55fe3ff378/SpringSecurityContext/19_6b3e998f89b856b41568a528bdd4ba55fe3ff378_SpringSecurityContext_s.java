 /*
  * Copyright (c) 2009 TouK
  * All rights reserved
  */
 package pl.touk.security.context;
 
 import org.springframework.security.userdetails.UserDetails;
 import org.springframework.security.context.SecurityContextHolder;
 
 /**
  * @author <a href="mailto:jnb@touk.pl">Jakub Nabrdalik</a>.
  */
 public class SpringSecurityContext implements SecurityContextInterface {
     public UserDetails getLoggedInUser() {
         Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 
         if(user == null) {
             throw new SecurityException("No user logged in");    
         }
 
         if (!(user instanceof UserDetails)) {
             throw new SecurityException("Wrong user object instance. Expected UserDetails."); 
         }
         
         return (UserDetails)user;
     }
 
     public String getLoggedUserName() {
        return this.getLoggedInUser().getUsername();
     }
 }
