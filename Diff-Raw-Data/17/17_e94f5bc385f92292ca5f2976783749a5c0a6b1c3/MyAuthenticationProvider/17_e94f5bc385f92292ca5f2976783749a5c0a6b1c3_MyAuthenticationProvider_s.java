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
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.security.authentication.AuthenticationProvider;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.GrantedAuthorityImpl;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 
 /**
  * Sample implementation of {@link AuthenticationProvider} that authenticates users 
  * versus an in-memory map of username/password pairs,
  * and assign the same access control attribute to everybody.
  */
 public class MyAuthenticationProvider implements AuthenticationProvider {
 	
 	private final Log LOG = LogFactory.getLog(this.getClass());
 	private static final String[] ROLES = new String[] { "ROLE_USER", "ROLE_GUEST"};
 	
 	/**
 	 * Map of valid (username, password) combinations.
 	 */
 	private Map<String, List<String>> users = new HashMap<String, List<String>>();
 	
 	private UserDetailsService userDetailsService;
 
 	public Authentication authenticate(Authentication request)throws AuthenticationException {
 				
 		Authentication authentication = null;
 
 		// retrieve username, password from authentication request
 		final String username = request.getPrincipal().toString();
 		final String password = request.getCredentials().toString();
 		if (LOG.isDebugEnabled()) LOG.debug("Authenticating user="+username);
 
 		// validate username/password combination
 		if (isValid(username, password)) {
 			
			final GrantedAuthority[] authorities = new GrantedAuthority[] { 
					new GrantedAuthorityImpl(ROLES[0]), new GrantedAuthorityImpl(ROLES[1]) 
			};
 			authentication = new UsernamePasswordAuthenticationToken(username, password, authorities);
 			final UserDetails details = userDetailsService.loadUserByUsername(username);
 			((UsernamePasswordAuthenticationToken)authentication).setDetails(details);
 			if (LOG.isDebugEnabled()) {
 				LOG.debug("Authentication succesfull.");
 				for (final GrantedAuthority ga : authorities) {
 					LOG.debug("Granted Authority: "+ga.toString());
 				}
 			}
 			
 		}
 		
 		return authentication;
 
 	}
 
 	public boolean supports(final Class clazz) {
 		return (clazz.getName().equals("org.springframework.security.providers.UsernamePasswordAuthenticationToken") ? true : false);
 	}
 	
 	private boolean isValid(final String username, final String password) {
 		if (users.containsKey(username) && users.get(username).equals(password)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public void setUserDetailsService(UserDetailsService userDetailsService) {
 		this.userDetailsService = userDetailsService;
 	}
 
 	public void setUsers(Map<String, List<String>> users) {
 		this.users = users;
 	}
 
 }
