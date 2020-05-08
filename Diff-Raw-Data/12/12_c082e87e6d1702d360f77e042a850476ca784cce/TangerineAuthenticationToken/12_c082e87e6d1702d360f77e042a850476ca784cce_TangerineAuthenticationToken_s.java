 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.orangeleap.tangerine.security;
 
 import org.apache.commons.logging.Log;
 import org.jasig.cas.client.validation.AssertionImpl;
 import org.springframework.security.GrantedAuthority;
 import org.springframework.security.providers.cas.CasAuthenticationToken;
 import org.springframework.security.userdetails.User;
 
 import com.orangeleap.tangerine.util.OLLogger;
 
 public class TangerineAuthenticationToken extends CasAuthenticationToken {
 
     /**
      * Logger for this class and subclasses
      */
     protected final Log logger = OLLogger.getLog(getClass());
 
     private static final long serialVersionUID = 1L;
 
 
     public TangerineAuthenticationToken(Object principal, Object credentials, String site) {
         this(principal, credentials, site, new GrantedAuthority[0]);
     }
 
     public TangerineAuthenticationToken(Object principal, Object credentials, String site, GrantedAuthority[] authorities) {
         super("tangerine-client-key", principal, credentials, authorities, new User(principal.toString(),credentials.toString(),true,true,true,true,authorities), new AssertionImpl(principal.toString()));
         TangerineAuthenticationDetails details = new TangerineAuthenticationDetails();
         this.setDetails(details);
         details.setSite(site);
     }
     
 }
