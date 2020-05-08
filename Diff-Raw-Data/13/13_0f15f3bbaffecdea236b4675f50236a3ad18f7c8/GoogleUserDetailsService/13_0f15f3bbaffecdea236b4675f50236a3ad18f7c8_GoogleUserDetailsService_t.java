 package com.media2359.euphoria.service.security;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.springframework.dao.DataAccessException;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
 import org.springframework.security.core.userdetails.User;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.security.openid.OpenIDAttribute;
 import org.springframework.security.openid.OpenIDAuthenticationToken;
 
 /**
  * User Details Service to fetch the details of the logged in Google User
  * 
  * @author alfreds
  *
  */
public class GoogleUserDetailsService implements AuthenticationUserDetailsService<OpenIDAuthenticationToken> {
 	private Logger log = Logger.getLogger(GoogleUserDetailsService.class);
 
	public UserDetails loadUserDetails(OpenIDAuthenticationToken token)
 			throws UsernameNotFoundException, DataAccessException {
		log.info("Fetching user details ...");
 		UserDetails userDetail = null;
 		if(token != null) {
 			List<OpenIDAttribute> attributes = token.getAttributes();
 			if(attributes != null) {
 				for(OpenIDAttribute attribute:attributes) {
 					if("email".equals(attribute.getName())) {
 						if(attribute.getValues() != null) {
 							String email = attribute.getValues().get(0);
 							log.info("Retrieved Email of the user: "+email);
 							
 							//TODO: Verify this user in database and fetch role details
 							//Currently we hardcode these values and return
 							List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
 							grantedAuthorities.add(new GrantedAuthorityImpl("ROLE_USER"));
 							userDetail = new User(email, "", true, true, true, true, grantedAuthorities);
 							return userDetail;
 						} else {
 							log.error("Received empty email attribute");
 						}
 					}
 				}
 			} else {
 				log.error("Did not receive any attributes");
 			}
 		} else {
 			log.error("Received empty token");
 		}
 		throw new UsernameNotFoundException("Invalid user details");
 	}
 }
