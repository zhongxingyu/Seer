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
 package org.exoplatform.acceptance.frontend.security;
 
 import org.springframework.security.authentication.AuthenticationProvider;
 import org.springframework.security.authentication.BadCredentialsException;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 
 /**
  * A basic implementation of {@link AuthenticationProvider} which is using our {@link CrowdAuthenticationProviderMock} to manage users.
  */
 public class CrowdAuthenticationProviderMock implements AuthenticationProvider {
 
   private final CrowdUserDetailsServiceMock crowdUserDetailsServiceMock;
 
   public CrowdAuthenticationProviderMock(CrowdUserDetailsServiceMock userDetailsService) {
     this.crowdUserDetailsServiceMock = userDetailsService;
   }
 
   /**
    * Performs authentication with the same contract as {@link
    * org.springframework.security.authentication.AuthenticationManager#authenticate(Authentication)}.
    *
    * @param authentication the authentication request object.
    * @return a fully authenticated object including credentials. May return <code>null</code> if the
    *         <code>AuthenticationProvider</code> is unable to support authentication of the passed
    *         <code>Authentication</code> object. In such a case, the next <code>AuthenticationProvider</code> that
    *         supports the presented <code>Authentication</code> class will be tried.
    * @throws AuthenticationException if authentication fails.
    */
   @Override
   public Authentication authenticate(Authentication authentication) throws AuthenticationException {
     String name = authentication.getName();
     String password = authentication.getCredentials().toString();
     try {
       UserDetails user = crowdUserDetailsServiceMock.loadUserByUsername(name);
       if (user.getPassword().equals(password)) {
         return new UsernamePasswordAuthenticationToken(
             user.getUsername(),
             user.getPassword(),
             user.getAuthorities());
       } else {
         throw new BadCredentialsException("Invalid username or password");
       }
     } catch (UsernameNotFoundException unnfe) {
      throw new BadCredentialsException("Invalid username or password",unnfe);
     }
   }
 
   /**
    * Returns <code>true</code> if this <Code>AuthenticationProvider</code> supports the indicated
    * <Code>Authentication</code> object.
    * <p>
    * Returning <code>true</code> does not guarantee an <code>AuthenticationProvider</code> will be able to
    * authenticate the presented instance of the <code>Authentication</code> class. It simply indicates it can support
    * closer evaluation of it. An <code>AuthenticationProvider</code> can still return <code>null</code> from the
    * {@link #authenticate(Authentication)} method to indicate another <code>AuthenticationProvider</code> should be
    * tried.
    * </p>
    * <p>Selection of an <code>AuthenticationProvider</code> capable of performing authentication is
    * conducted at runtime the <code>ProviderManager</code>.</p>
    *
    * @param authentication
    * @return <code>true</code> if the implementation can more closely evaluate the <code>Authentication</code> class
    *         presented
    */
   @Override
   public boolean supports(Class<?> authentication) {
     return authentication.equals(UsernamePasswordAuthenticationToken.class);
   }
 }
