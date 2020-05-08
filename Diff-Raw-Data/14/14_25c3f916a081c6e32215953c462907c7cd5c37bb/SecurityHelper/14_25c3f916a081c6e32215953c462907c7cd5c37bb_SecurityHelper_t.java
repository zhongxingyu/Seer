 /*
  * Copyright luntsys (c) 2004-2005,
  * Date: 2004-10-21
  * Time: 16:03:58
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met: 1.
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. 2. Redistributions in
  * binary form must reproduce the above copyright notice, this list of
  * conditions and the following disclaimer in the documentation and/or other
  * materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  */
 
 package com.luntsys.luntbuild.security;
 
 import com.luntsys.luntbuild.utility.Luntbuild;
 import com.luntsys.luntbuild.db.Role;
 import org.acegisecurity.*;
 import org.acegisecurity.context.SecurityContext;
 import org.acegisecurity.context.SecurityContextHolder;
 import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
 import org.acegisecurity.userdetails.UserDetails;
 import org.acegisecurity.userdetails.UserDetailsService;
 import org.acegisecurity.providers.dao.cache.EhCacheBasedUserCache;
 import net.sf.ehcache.Cache;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.util.*;
 
 /**
  * The <code>SecurityHelper</code> is used to validate autheticated pricipals
  * at runtime against a given set of required roles.
  * 
  * @author johannes plachy
  */
 public class SecurityHelper
 {
     final transient private static Log logger = LogFactory.getLog(SecurityHelper.class);
 
     final private static int NON_PRJ_ID = -1;
 
     /**
      * Checks currently logged-in principal for assigned roles.
      * 
      * @param rolesToBeChecked the needed roles
      * @return <code>true</code> if needed roles are assigned
      */
     public static boolean isUserInRole(String rolesToBeChecked)
     {
         return isUserInRole(NON_PRJ_ID, rolesToBeChecked);
     }
 
     /**
      * Checks currently logged-in principal for assigned roles or matching username.
      *
      * @param username to be checked against principal
      * @param rolesToBeChecked the needed roles
      * @return <code>true</code> if needed roles are assigned or username matches principal
      */
     public static boolean isUserInRoleOrPrincipal(String username, String rolesToBeChecked)
     {
         return (getPrincipal().toString().equals(username) || isUserInRole(NON_PRJ_ID, rolesToBeChecked));
     }
 
     /**
      * Checks curently logged-in principal for assigned roles which have to be project specific.
      * 
      * @param prjId the project identifier
      * @param rolesToBeChecked the needed roles
      * @return <code>true</code> if needed roles are assigned
      */
     public static boolean isUserInRole(long prjId, String rolesToBeChecked)
     {
         boolean accessGraned = false;
 
         Collection granted = getPrincipalAuthorities();
 
         Set grantedCopy = retainAll(granted, parseAuthoritiesString(prjId, rolesToBeChecked));
 
         accessGraned = !grantedCopy.isEmpty();
 
         return accessGraned;
     }
 
     /**
      * Gets the principal of the currently logged-in user.
      * 
      * @return the principal, or <code>null</code> if no current user
      */
     public static Object getPrincipal()
     {
         Object principal = null;
 
         Authentication currentUser = getCurrentUser();
 
         if ( currentUser != null)
         {
             principal = currentUser.getPrincipal();
         }
 
         return principal;
     }
 
     /**
      * Gets the principal of the currently logged-in user.
      * 
      * @return the principal, or <code>null</code> if no current user
      */
     public static String getPrincipalAsString() {
         final Authentication authentication = getCurrentUser();
         if (authentication != null) {
             if (authentication.getPrincipal() instanceof UserDetails) {
                 return ((UserDetails) authentication.getPrincipal()).getUsername();
             }
 
             return authentication.getPrincipal().toString();
         }
 
         return null;
     }
 
     /**
      * Gets the currently logged-in user.
      * 
      * @return the currently logged-in user, or <code>null</code> if no current user
      */
     private static Authentication getCurrentUser()
     {
         Authentication currentUser = null;
 
         SecurityContext context = SecurityContextHolder.getContext();
 
         if (null != context)
         {
             currentUser = context.getAuthentication();
         }
 
         return currentUser;
     }
 
     private static Collection getPrincipalAuthorities()
     {
         Authentication currentUser = getCurrentUser();
 
         if (null == currentUser) { return Collections.EMPTY_LIST; }
 
         Collection granted = Arrays.asList(currentUser.getAuthorities());
 
         return granted;
     }
 
     /**
      * Gets the required authorities for the curently logged-in principal.
      *
      * @param prjid the project identifier (use it to construct project specific roles)
      * @param rolesToBeChecked the needed roles
      * @return the set of authorities for the needed roles
      * @see GrantedAuthorityImpl
      */
     private static Set parseAuthoritiesString(long prjid, String rolesToBeChecked)
     {
         final Set requiredAuthorities = new HashSet();
         final StringTokenizer tokenizer;
 
         tokenizer = new StringTokenizer(rolesToBeChecked, ",", false);
 
         while (tokenizer.hasMoreTokens())
         {
             String role = tokenizer.nextToken();
            String newProjRole = null;
            
             // construct prj specific roles
             if ( prjid > NON_PRJ_ID)
             {
                 // if base_roles are specific
                 if ( role.indexOf("PRJ") > 0 )
                 {
                	String origRole = role;
                     role = role +"_" + prjid;
                    // Initially we get proj id 0 (when no project is created), so lets include it
                    if (prjid >= 1) {
                    	newProjRole = origRole +"_" + 0;
                    }
                 }
             }
 
             requiredAuthorities.add(new GrantedAuthorityImpl(role.trim()));
            if (newProjRole != null) {
            	requiredAuthorities.add(new GrantedAuthorityImpl(newProjRole.trim()));
            }
         }
 
         return requiredAuthorities;
     }
 
     private static Set retainAll(final Collection granted, final Set requiredAuthorities)
     {
         Set grantedCopy = new HashSet(granted);
         grantedCopy.retainAll(requiredAuthorities);
 
         return grantedCopy;
     }
 
 	/**
 	 * Checks if the currently logged-in principal can view the specified project.
 	 * 
 	 * @param prjId the project identifier
 	 * @return <code>true</code> if the user has read access
 	 */
 	public static boolean isPrjReadable(long prjId) {
 		return isUserInRole(prjId, "ROLE_SITE_ADMIN, ROLE_AUTHENTICATED, ROLE_ANONYMOUS, " +
                 "LUNTBUILD_PRJ_ADMIN, LUNTBUILD_PRJ_BUILDER, LUNTBUILD_PRJ_VIEWER");
 	}
 
 	/**
 	 * Checks if the currently logged-in principal can build the specified project.
 	 * 
 	 * @param prjId the project identifier
 	 * @return <code>true</code> if the user has execute access
 	 */
 	public static boolean isPrjBuildable(long prjId) {
 		return isUserInRole(prjId, "ROLE_SITE_ADMIN, LUNTBUILD_PRJ_ADMIN, LUNTBUILD_PRJ_BUILDER");
 	}
 
 	/**
 	 * Checks if the currently logged-in principal can change the specified project.
 	 * 
 	 * @param prjId the project identifier
 	 * @return <code>true</code> if the user has write access
 	 */
 	public static boolean isPrjAdministrable(long prjId) {
 		return isUserInRole(prjId, "ROLE_SITE_ADMIN, LUNTBUILD_PRJ_ADMIN");
 	}
 
 	/**
 	 * Checks if the currently logged-in principal is a site administrator.
 	 * 
 	 * @return <code>true</code> if the user has site administration access
 	 */
 	public static boolean isSiteAdmin() {
 		return isUserInRole("ROLE_SITE_ADMIN");
 	}
 
 	/**
 	 * Allows the currently logged-in principal temporarily run as the site administrator.
 	 * 
 	 * @throws RuntimeException if the <code>AuthenticationDao</code> could not be found in the application context
 	 * @throws RuntimeException if the built in user does not have site administrator status or failed to authenticate
 	 */
 	public static void runAsSiteAdmin() {
         UserDetailsService authDao = (UserDetailsService) Luntbuild.appContext.getBean("inMemoryAuthenticationDAO");
 		if (authDao == null) {
 			logger.error("Can not find bean named by \"inMemoryAuthenticationDao\" " +
 					"in the application context!");
 			throw new RuntimeException("Can not find bean named by \"inMemoryAuthenticationDao\" " +
 					"in the application context!");
 		}
 		UserDetails userDetails = authDao.loadUserByUsername("luntbuild");
 		GrantedAuthority[] authorities = userDetails.getAuthorities();
 		// check if this user has site admin priviledge
 		boolean isSiteAdmin = false;
 		for (int i = 0; i < authorities.length; i++) {
 			GrantedAuthority authority = authorities[i];
 			if (authority.getAuthority().trim().equals(Role.ROLE_SITE_ADMIN)) {
 				isSiteAdmin = true;
 				break;
 			}
 		}
 		if (!isSiteAdmin) {
 			logger.error("Built-in user \"luntbuild\" should have \"ROLE_SITE_ADMIN\" priviledge!");
 			throw new RuntimeException("Built-in user \"luntbuild\" should have \"ROLE_SITE_ADMIN\" priviledge!");
 		}
 		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken("luntbuild",
 				userDetails.getPassword());
 		AuthenticationManager authManager = Luntbuild.getAuthenticationManager();
 		Authentication authResult;
 		try {
 			authResult = authManager.authenticate(authRequest);
 		} catch (AuthenticationException failed) {
 			logger.error("Failed to authenticate build-in administrator: ", failed);
 			throw new RuntimeException(failed);
 		}
         SecurityContext context = SecurityContextHolder.getContext();
 		if (context == null) {
             logger.error("Failed to create SecurityContext");
             throw new RuntimeException();
 		}
 		context.setAuthentication(authResult);
 	}
 
 	/**
 	 * Refreshes the user cache.
 	 * 
 	 * @throws RuntimeException if refresh fails
 	 */
 	public static void refreshUserCache() {
 		EhCacheBasedUserCache userCache = (EhCacheBasedUserCache) Luntbuild.appContext.getBean("userCache");
 		Cache cache = userCache.getCache();
 		cache.removeAll();
 	}
 }
