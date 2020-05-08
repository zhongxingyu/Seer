 /*******************************************************************************
  * Copyright (c) 2011 Earth System Grid Federation
  * ALL RIGHTS RESERVED. 
  * U.S. Government sponsorship acknowledged.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  * 
  * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
  * 
  * Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
  * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  ******************************************************************************/
 package org.esgf.security;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.dao.DataAccessException;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.GrantedAuthorityImpl;
 import org.springframework.security.core.userdetails.User;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.stereotype.Component;
 
 import esg.common.util.ESGFProperties;
 import esg.node.security.UserInfo;
 import esg.node.security.UserInfoCredentialedDAO;
 
 /**
  * Implementation of Spring Security {@link UserDetailsService} that returns a user object populated with the user permissions read from the database.
  * Each user, after authentication, is assigned at least the role "ROLE_USER".
  * Additionally, the user permissions are read from the database and result in granted authorities of the form:
  * "group_<group>_role_<role>".
  * The special authority "group_wheel_role_super" is duplicated as "ROLE_ADMIN".
  */
 @Component("userDetailsService")
 public class UserDetailsServiceImpl implements UserDetailsService {
 	
     // group, role used by UI
 	private final static String ROLE_USER = "ROLE_USER";
 	private final static String ROLE_ADMIN = "ROLE_ADMIN";
 	
 	// group, role stored in database
 	private final static String ADMIN_GROUP = "wheel";
 	private final static String ADMIN_ROLE = "super";
 	
 	private final Log LOG = LogFactory.getLog(this.getClass());
 		
 	private UserInfoCredentialedDAO userInfoDAO = null;
 	
 	// constructor initializes the DAO
 	public UserDetailsServiceImpl() {
 	    
        try {
            ESGFProperties myESGFProperties = new ESGFProperties();
            String passwd = myESGFProperties.getAdminPassword(); 
            this.userInfoDAO = new UserInfoCredentialedDAO("rootAdmin", passwd, myESGFProperties);
         } catch(Exception e) {
             LOG.warn(e.getMessage());
         }
 	        
 	}
 	
 	public UserDetails loadUserByUsername(final String userName) throws UsernameNotFoundException, DataAccessException {
 		
 		if (LOG.isDebugEnabled()) LOG.debug("Retrieving details for user="+userName);
 		
 		final String password = "*****";
 		final boolean enabled = true;
 		final boolean accountNonExpired = true;
 		final boolean credentialsNonExpired = true;
 		final boolean accountNonLocked = true;
 		final List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
 		authorities.add( new GrantedAuthorityImpl(ROLE_USER) );
 		
 		// loop over database permissions (lookup by openid)
 		final UserInfo user = userInfoDAO.getUserById(userName);
		final Map<String, Set<String>> permissions = user.getPermissions();
		for (final String group : permissions.keySet()) {
		    for (final String role : permissions.get(group)) {
		        String authority = "group_"+group+"_role_"+role;
		        authorities.add( new GrantedAuthorityImpl(authority) );
		        
		        // translate ('wheel','super') -> 'ROLE_ADMIN'
		        if (group.equals(ADMIN_GROUP) && role.equals(ADMIN_ROLE)) {
		            authorities.add( new GrantedAuthorityImpl(ROLE_ADMIN) );
		        }
		        
		    }
 		}
 		
 		return new User(userName, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
 		
 	}
 	
 }
