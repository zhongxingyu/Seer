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
 import javax.servlet.http.HttpServletResponse;
 import org.jamwiki.model.Role;
 import org.jamwiki.utils.WikiLogger;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.security.Authentication;
 import org.springframework.security.context.SecurityContextHolder;
 import org.springframework.security.providers.anonymous.AnonymousAuthenticationToken;
 
 /**
  * This class allows anonymous users to be provided default roles from the
  * JAMWiki database.
  */
 public class JAMWikiAnonymousProcessingFilter implements Filter, InitializingBean {
 
 	/** Standard logger. */
 	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiAnonymousProcessingFilter.class.getName());
 	private String key;
 
 	/**
 	 *
 	 */
 	public void afterPropertiesSet() {
     }
 
 	/**
 	 *
 	 */
 	public void destroy() {
 	}
 
 
 	/**
 	 * Override the parent method to ensure that default roles for anonymous
 	 * users have been retrieved.
 	 */
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
 		if (!(request instanceof HttpServletRequest)) {
 			throw new ServletException("HttpServletRequest required");
 		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth instanceof AnonymousAuthenticationToken) {
			AnonymousAuthenticationToken jamwikiAuth = new AnonymousAuthenticationToken(this.getKey(), auth.getPrincipal(), WikiUserAuth.getAnonymousGroupRoles());
			jamwikiAuth.setDetails(auth.getDetails());
			jamwikiAuth.setAuthenticated(auth.isAuthenticated());
			SecurityContextHolder.getContext().setAuthentication(jamwikiAuth);
 		}
 		chain.doFilter(request, response);
 	}
 
 	/**
 	 *
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
 	 *
 	 */
 	public void init(FilterConfig filterConfig) throws ServletException {
 	}
 }
