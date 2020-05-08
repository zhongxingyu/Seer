 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.authentication;
 
 import java.io.IOException;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.utils.WikiLogger;
 import org.springframework.security.Authentication;
 import org.springframework.security.GrantedAuthority;
 import org.springframework.security.context.SecurityContextHolder;
 import org.springframework.security.providers.anonymous.AnonymousAuthenticationToken;
 import org.springframework.security.userdetails.UserDetails;
 
 /**
  * Provide processing of a successfully authenticated user.  This filter will examine
  * the authentication credentails for systems using LDAP or other external
  * authentication systems, and verify that the authenticated user has valid records in
  * the jam_wiki_user and similar tables; if no such records exist they will be created to
  * allow tracking of edit history and user contributions.  Additionally, this filter
  * provides capabilities for adding anonymous group permissions to anonymous users.
  */
 public class JAMWikiPostAuthenticationFilter implements Filter {
 
 	/** Standard logger. */
 	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiPostAuthenticationFilter.class.getName());
 	private String key;
 	private boolean useJAMWikiAnonymousRoles;
 
 	/**
 	 *
 	 */
 	private GrantedAuthority[] combineAuthorities(GrantedAuthority[] list1, GrantedAuthority[] list2) {
 		if (list1 == null || list2 == null) {
 			return null;
 		}
 		// add these roles to a single array of authorities
 		int authoritySize = list1.length + list2.length;
 		GrantedAuthority[] combinedAuthorities = new GrantedAuthority[authoritySize];
 		for (int i = 0; i < list1.length; i++) {
 			combinedAuthorities[i] = list1[i];
 		}
 		for (int i = 0; i < list2.length; i++) {
 			combinedAuthorities[i + list1.length] = list2[i];
 		}
 		return combinedAuthorities;
 	}
 
 	/**
 	 *
 	 */
 	public void destroy() {
 	}
 
 	/**
 	 *
 	 */
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
 		if (!(request instanceof HttpServletRequest)) {
 			throw new ServletException("HttpServletRequest required");
 		}
 		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		if (auth instanceof AnonymousAuthenticationToken) {
 			// anonymous user
 			this.handleAnonymousUser(auth);
 		} else if (auth != null && auth.isAuthenticated()) {
 			// registered user
 			this.handleRegisteredUser(auth);
 		}
 		chain.doFilter(request, response);
 	}
 
 	/**
 	 *
 	 */
 	private void handleAnonymousUser(Authentication auth) {
 		if (!this.getUseJAMWikiAnonymousRoles()) {
 			// the configuration file indicates that JAMWiki anonymous roles should not be 
 			// used, so assume that an external system is providing this information.
 			return;
 		}
 		// get arrays of existing Spring Security roles and JAMWiki anonymous user roles
 		GrantedAuthority[] springSecurityAnonymousAuthorities = auth.getAuthorities();
 		GrantedAuthority[] jamwikiAnonymousAuthorities = JAMWikiAuthenticationConfiguration.getJamwikiAnonymousAuthorities();
 		GrantedAuthority[] anonymousAuthorities = combineAuthorities(springSecurityAnonymousAuthorities, jamwikiAnonymousAuthorities);
 		if (anonymousAuthorities == null) {
 			return;
 		}
 		// replace the existing anonymous authentication object with the new authentication array
 		AnonymousAuthenticationToken jamwikiAuth = new AnonymousAuthenticationToken(this.getKey(), auth.getPrincipal(), anonymousAuthorities);
 		jamwikiAuth.setDetails(auth.getDetails());
 		jamwikiAuth.setAuthenticated(auth.isAuthenticated());
 		SecurityContextHolder.getContext().setAuthentication(jamwikiAuth);
 	}
 
 	/**
 	 *
 	 */
 	private void handleRegisteredUser(Authentication auth) throws ServletException {
 		Object principal = auth.getPrincipal();
		if (!(principal instanceof UserDetails)) {
			logger.warning("Unknown principal type: " + principal);
			return;
		}
 		if (principal instanceof WikiUserDetails) {
 			// user has gone through the normal authentication path, no need to process further
 			return;
 		}
		String username = ((UserDetails)principal).getUsername();
 		if (StringUtils.isBlank(username)) {
 			logger.warning("Null or empty username found for authenticated principal");
 			return;
 		}
 		// for LDAP and other authentication methods, verify that JAMWiki database records exist
		try {
 			if (WikiBase.getDataHandler().lookupWikiUser(username) == null) {
 				// if there is a valid security credential & no JAMWiki record for the user, create one
 				WikiUser user = new WikiUser(username);
 				// default the password empty so that the user cannot login directly
 				String encryptedPassword = "";
 				WikiBase.getDataHandler().writeWikiUser(user, username, encryptedPassword);
 			}
 		} catch (Exception e) {
 			logger.severe("Failure while processing user credentials for " + username, e);
 			throw new ServletException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void init(FilterConfig filterConfig) throws ServletException {
 	}
 
 	/**
 	 * Standard get method to return the anonymous authentication token key configured
 	 * for anonymous users.  This value is set from the configuration file and MUST match
 	 * the value used when creating anonymous authentication credentials.
 	 */
 	public String getKey() {
 		return key;
 	}
 
 	/**
 	 *
 	 */
 	public void setKey(String key) {
 		this.key = key;
 	}
 
 	/**
 	 * Provide a flag to disable the addition of JAMWiki GROUP_ANONYMOUS permissions to
 	 * all anonymous users.
 	 */
 	public boolean getUseJAMWikiAnonymousRoles() {
 		return useJAMWikiAnonymousRoles;
 	}
 
 	/**
 	 * Provide a flag to disable the addition of JAMWiki GROUP_ANONYMOUS permissions to
 	 * all anonymous users.
 	 */
 	public void setUseJAMWikiAnonymousRoles(boolean useJAMWikiAnonymousRoles) {
 		this.useJAMWikiAnonymousRoles = useJAMWikiAnonymousRoles;
 	}
 }
