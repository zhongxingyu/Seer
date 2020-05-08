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
 import java.util.Collection;
 
 import com.atlassian.crowd.integration.springsecurity.user.CrowdUserDetails;
 import com.google.common.base.Strings;
 import javax.inject.Inject;
 import javax.inject.Named;
 import org.exoplatform.acceptance.security.CrowdUser;
 import org.exoplatform.acceptance.security.CrowdUserImpl;
 import org.exoplatform.acceptance.security.CrowdUserMock;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 
 @Named("user")
 public class CurrentUser implements CrowdUser {
   private static final Logger LOGGER = LoggerFactory.getLogger(CurrentUser.class);
 
   @Inject
   @Named("crowdUserDetailsService")
   private UserDetailsService userDetailsService;
 
   private CrowdUser currentUser;
 
   /**
    * Returns the user's first name
    */
   @Override
   public String getFirstName() {
     return getCurrentUser().getFirstName();
   }
 
   /**
    * Returns the user's last name
    */
   @Override
   public String getLastName() {
     return getCurrentUser().getLastName();
   }
 
   /**
    * Returns the user's fullname
    */
   @Override
   public String getFullName() {
     return getCurrentUser().getFullName();
   }
 
   /**
    * Returns the user's email
    */
   @Override
   public String getEmail() {
     return getCurrentUser().getEmail();
   }
 
   /**
    * Is the user authenticated ?
    *
    * @return true if the user is authenticated
    */
   @Override
   public boolean isAuthenticated() {
     if (SecurityContextHolder.getContext().getAuthentication() != null) {
       return SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
     } else {
       return false;
     }
   }
 
   /**
    * Returns the authorities granted to the user. Cannot return <code>null</code>.
    *
    * @return the authorities, sorted by natural key (never <code>null</code>)
    */
   @Override
   public Collection<? extends GrantedAuthority> getAuthorities() {
     return getCurrentUser().getAuthorities();
   }
 
   /**
    * Returns the password used to authenticate the user.
    *
    * @return the password
    */
   @Override
   public String getPassword() {
     return getCurrentUser().getPassword();
   }
 
   /**
    * Returns the username used to authenticate the user. Cannot return <code>null</code>.
    *
    * @return the username (never <code>null</code>)
    */
   @Override
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
    * Indicates whether the user's account has expired. An expired account cannot be authenticated.
    *
    * @return <code>true</code> if the user's account is valid (ie non-expired), <code>false</code> if no longer valid
    *         (ie expired)
    */
   @Override
   public boolean isAccountNonExpired() {
     return getCurrentUser().isAccountNonExpired();
   }
 
   /**
    * Indicates whether the user is locked or unlocked. A locked user cannot be authenticated.
    *
    * @return <code>true</code> if the user is not locked, <code>false</code> otherwise
    */
   @Override
   public boolean isAccountNonLocked() {
     return getCurrentUser().isAccountNonLocked();
   }
 
   /**
    * Indicates whether the user's credentials (password) has expired. Expired credentials prevent
    * authentication.
    *
    * @return <code>true</code> if the user's credentials are valid (ie non-expired), <code>false</code> if no longer
    *         valid (ie expired)
    */
   @Override
   public boolean isCredentialsNonExpired() {
     return getCurrentUser().isCredentialsNonExpired();
   }
 
   /**
    * Indicates whether the user is enabled or disabled. A disabled user cannot be authenticated.
    *
    * @return <code>true</code> if the user is enabled, <code>false</code> otherwise
    */
   @Override
   public boolean isEnabled() {
     return getCurrentUser().isEnabled();
   }
 
   /**
    * Retrieves the current crowd user.
    */
   private CrowdUser getCurrentUser() throws UsernameNotFoundException {
     if (currentUser == null && isAuthenticated()) {
       UserDetails userDetails = userDetailsService.loadUserByUsername(getUsername());
       if (userDetails instanceof CrowdUserDetails) {
         currentUser = new CrowdUserImpl((CrowdUserDetails) userDetails);
       } else if (userDetails instanceof CrowdUserMock) {
         currentUser = (CrowdUserMock) userDetails;
       } else {
         LOGGER.error("Unknown UserDetails implementation : {}", userDetails.getClass().getName());
       }
     }
     return currentUser;
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
       return "https://secure.gravatar.com/avatar/" + hash + "?s=" + size + "&d=404";
     } else {
       return "http://www.gravatar.com/avatar/" + hash + "?s=" + size + "&d=404";
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
    for (GrantedAuthority authority : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
      if (authority.getAuthority().equals(role)) {
        return true;
       }
     }
     return false;
   }
 
   public boolean isAnonymous() {
     return hasRole("ROLE_ANONYMOUS");
   }
 }
