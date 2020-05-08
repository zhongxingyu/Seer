 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  * only (the "License"). You may not use this file except in compliance with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
  * the specific language governing permissions and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
  * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
  * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  *
  * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
  * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
  * terms.
  */
 
 package de.escidoc.core.common.util.security;
 
 import de.escidoc.core.aa.service.interfaces.EscidocUserDetailsServiceInterface;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.security.authentication.AnonymousAuthenticationToken;
 import org.springframework.security.authentication.AuthenticationProvider;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.GrantedAuthorityImpl;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.stereotype.Service;
 
 /**
  * @author Torsten Tetteroo
  */
 @Service("eSciDoc.core.common.security.EscidocAuthenticationProvider")
 public class EscidocAuthenticationProvider implements AuthenticationProvider {
 
     @Autowired
     @Qualifier("eSciDoc.core.common.security.EscidocUserDetailsService")
     private EscidocUserDetailsServiceInterface escidocUserDetailsService;
 
     /**
      * Protected constructor to prevent instantiation outside of the Spring-context.
      */
     protected EscidocAuthenticationProvider() {
     }
 
     /**
      * See Interface for functional description.
      *
      */
     @Override
     public Authentication authenticate(final Authentication authentication) {
         final String credentials = (String) authentication.getCredentials();
        if (credentials == null || credentials.isEmpty()) {
             final GrantedAuthority grantedAuthority = new GrantedAuthorityImpl("");
             final GrantedAuthority[] grantedAuthorities = { grantedAuthority };
             return new AnonymousAuthenticationToken("key", "Anonymous", grantedAuthorities);
         }
         final UserDetails userDetails = escidocUserDetailsService.loadUserByUsername(credentials);
         return new UsernamePasswordAuthenticationToken(userDetails, credentials);
     }
 
     /**
      * See Interface for functional description.
      *
      */
     @Override
     public boolean supports(final Class cls) {
 
         return UsernamePasswordAuthenticationToken.class.isAssignableFrom(cls);
     }
 
 }
