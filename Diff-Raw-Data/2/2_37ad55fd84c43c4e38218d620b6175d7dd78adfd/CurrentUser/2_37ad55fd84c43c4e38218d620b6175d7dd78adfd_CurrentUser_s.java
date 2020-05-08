 /*
  * Copyright (C) 2011-2013 eXo Platform SAS.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 3 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.exoplatform.acceptance.frontend.model;
 
 import java.math.BigInteger;
 import java.nio.charset.Charset;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import com.google.common.base.Strings;
 import javax.inject.Inject;
 import javax.inject.Named;
 import lombok.Delegate;
 import org.exoplatform.acceptance.frontend.security.AppAuthority;
 import org.exoplatform.acceptance.frontend.security.ICrowdUserDetails;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 
 @Named("user")
 public class CurrentUser {
   @Inject
   @Named("userDetailsService")
   private UserDetailsService userDetailsService;
 
   private ICrowdUserDetails currentUser;
 
   /**
    * Is the user authenticated ?
    *
    * @return true if the user is authenticated
    */
   public boolean isAuthenticated() {
     return SecurityContextHolder.getContext().getAuthentication() != null && SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
   }
 
   /**
    * Returns the username used to authenticate the user. Cannot return <code>null</code>.
    *
    * @return the username (never <code>null</code>)
    */
   public String getUsername() {
     if (isAuthenticated()) {
       Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
       if (principal instanceof UserDetails) {
         return ((UserDetails) principal).getUsername();
       } else {
         return principal.toString();
       }
     } else {
       throw new UsernameNotFoundException("User not authenticated");
     }
   }
 
   /**
   * Simple searches for an exactly matching {@link org.springframework.security.core.GrantedAuthority.getAuthority()}.
    * Will always return false if the SecurityContextHolder contains an Authentication with nullprincipal and/or GrantedAuthority[] objects.
    *
    * @param role the GrantedAuthorityString representation to check for
    * @return true if an exact (case sensitive) matching granted authority is located, false otherwise
    */
   public boolean hasRole(String role) {
     if (isAuthenticated()) {
       for (GrantedAuthority authority : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
         if (authority.getAuthority().equals(role)) {
           return true;
         }
       }
     }
     return false;
   }
 
   /**
    * Checks if the current user is anonymous.
    *
    * @return true only if the user isn't authenticated or has the role ANONYMOUS
    */
   public boolean isAnonymous() {
     return !isAuthenticated() || hasRole(AppAuthority.ROLE_ANONYMOUS.getAuthority());
   }
 
   /**
    * Checks if the current user has the application USER role.
    *
    * @return true only if the user has the application USER role.
    */
   public boolean isUser() {
     return hasRole(AppAuthority.ROLE_USER.getAuthority());
   }
 
   /**
    * Checks if the current user has the application ADMIN role.
    *
    * @return true only if the user has the application ADMIN role.
    */
   public boolean isAdmin() {
     return hasRole(AppAuthority.ROLE_ADMIN.getAuthority());
   }
 
   /**
    * Computes the gravatar URL associated to the user email
    *
    * @param size  The size (width) of the image to generate
    * @param https If the URL must be in HTTPs or no
    * @return The URL of the gravatar
    * @throws NoSuchAlgorithmException If MD5 Algorithm isn't available
    */
   public String getGravatarUrl(int size, boolean https) throws NoSuchAlgorithmException {
     MessageDigest digest = MessageDigest.getInstance("MD5");
     digest.update(getEmail().trim().toLowerCase().getBytes(Charset.defaultCharset()));
     String hash = Strings.padStart(new BigInteger(1, digest.digest()).toString(16), 32, '0');
     if (https) {
       return "https://secure.gravatar.com/avatar/" + hash + "?s=" + size + "&d=mm";
     } else {
       return "http://www.gravatar.com/avatar/" + hash + "?s=" + size + "&d=mm";
     }
   }
 
   /**
    * Retrieves the current crowd user.
    */
   @Delegate(excludes = ExcludeICrowdUserDetailsDelegate.class)
   private ICrowdUserDetails getCurrentUser() throws UsernameNotFoundException {
     if (currentUser == null) {
       currentUser = (ICrowdUserDetails) userDetailsService.loadUserByUsername(getUsername());
     }
     return currentUser;
   }
 
   private interface ExcludeICrowdUserDetailsDelegate {
     String getUsername();
   }
 
 }
