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
        //call outside of operating security context
        //not exactly bad, but possible during hibernate bootstrap.
        //@see org.hibernate.engine.UnsavedValueFactory.getUnsavedIdentifierValue;
        if ( null == SecurityContextHolder.getContext() ) { 
            return null;
        }

        if ( null == SecurityContextHolder.getContext().getAuthentication() ) {
            return null;
        }

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
        UserDetails userDetails = this.getLoggedInUser();

        if(null == userDetails)
            return null;
        else
            return userDetails.getUsername();
     }
 }
