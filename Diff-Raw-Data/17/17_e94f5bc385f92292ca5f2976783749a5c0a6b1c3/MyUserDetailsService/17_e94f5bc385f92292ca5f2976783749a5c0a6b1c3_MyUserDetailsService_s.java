 /*******************************************************************************
  * Copyright (c) 2010 Earth System Grid Federation
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
 package esg.datanode.security.orp;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.dao.DataAccessException;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.GrantedAuthorityImpl;
 import org.springframework.security.core.userdetails.User;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 
 /**
  * Sample implementation of {@link UserDetailsService} that returns a user object populated with standard values.
  * Optionally, each user object can be assigned a set of granted authorities.
  */
 public class MyUserDetailsService implements UserDetailsService {
 	
 	private final Log LOG = LogFactory.getLog(this.getClass());
 	
 	private Map<String, List<String>> users = new HashMap<String, List<String>>();
 
 	public UserDetails loadUserByUsername(final String userName) throws UsernameNotFoundException, DataAccessException {
 		
 		if (LOG.isDebugEnabled()) LOG.debug("Retrieving details for user="+userName);
 		
 		final String password = "*****";
 		final boolean enabled = true;
 		final boolean accountNonExpired = true;
 		final boolean credentialsNonExpired = true;
 		final boolean accountNonLocked = true;
 		final List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
 		if (users.containsKey(userName)) {
 			for (final String authority : users.get(userName)) {
 				authorities.add( new GrantedAuthorityImpl(authority) );
 			}
 		}
		return new User(userName, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities.toArray(new GrantedAuthority[]{}));
 		
 	}
 
 	public void setUsers(Map<String, List<String>> users) {
 		this.users = users;
 	}
 	
 }
