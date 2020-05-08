 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.user.integration;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.apache.commons.lang.Validate;
 
 import org.acegisecurity.userdetails.UserDetailsService;
 import org.acegisecurity.userdetails.UserDetails;
 import org.acegisecurity.userdetails.UsernameNotFoundException;
 
 import com.globant.katari.user.domain.User;
 import com.globant.katari.user.domain.UserRepository;
 
 /** A user details service needed by acegi, that that obtains the user
  * information from the application domain.
  */
 public class DomainUserDetailsService implements UserDetailsService {
 
   /** The class logger.
    */
   private static Log log = LogFactory.getLog(DomainUserDetailsService.class);
 
  /** The user service used to get the domain user.
    *
    * It is never null.
    */
   private UserRepository userRepository;
 
   /** Builds an instance of this service.
    *
    * @param theUserRepository The user repository to get the domain user from.
    * It cannot be null.
    */
   DomainUserDetailsService(final UserRepository theUserRepository) {
     Validate.notNull(theUserRepository, "The user repository cannot be null");
     userRepository = theUserRepository;
   }
 
   /** Obtains the user details from a user name.
    *
    * @param username The user name to search. This cannot be null.
    *
    * @return Returns the user details needed by acegi. Specifically, an
    * instance of DomainUserDetails.
    */
   public UserDetails loadUserByUsername(final String username) {
 
     log.trace("Entering loadUserByUsername");
 
     Validate.notNull(username, "The username cannot be null");
 
     User user = userRepository.findUserByName(username);
 
     if (user == null) {
       throw new UsernameNotFoundException("User not found: " + username);
     }
 
     UserDetails userDetails = new DomainUserDetails(user);
 
     log.trace("Leaving loadUserByUsername");
     return userDetails;
   }
 }
 
