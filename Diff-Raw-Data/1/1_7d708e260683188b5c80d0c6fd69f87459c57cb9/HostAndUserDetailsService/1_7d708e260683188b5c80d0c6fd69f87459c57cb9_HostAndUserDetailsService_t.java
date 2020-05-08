 package de.zib.gndms.gndms.security;
 /*
  * Copyright 2008-2011 Zuse Institute Berlin (ZIB)
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 import de.zib.gndms.stuff.misc.X509DnConverter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.security.authentication.BadCredentialsException;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.context.SecurityContext;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.security.web.authentication.WebAuthenticationDetails;
 import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Collections;
 
 /**
  * @author Maik Jorra
  * @email jorra@zib.de
  * @date 20.03.12  17:39
  * @brief
  */
 public class HostAndUserDetailsService implements  AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {
 
     private GridMapUserDetailsService userDetailsService;
     private String allowedHostsFileName;
     private boolean reverseDNSTest = true;
     protected Logger logger = LoggerFactory.getLogger( this.getClass() );
 
 
     @Override
     public UserDetails loadUserDetails(
             final PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken )
             throws UsernameNotFoundException
     {
 
         String dn = ( String ) preAuthenticatedAuthenticationToken.getPrincipal();
 
         try {
             if( GridMapUserDetailsService.searchInGridMapfile( allowedHostsFileName, dn ) ) {
                 if ( reverseDNSTest )
                     try {
                         if( ! reverseDNSLookup( X509DnConverter.openSslDnExtractCn( dn ),
                                 preAuthenticatedAuthenticationToken.getDetails() ) ) {
                             logger.info( "Host-CN revers DNS lookup failed for: " + dn );
                             throw new BadCredentialsException( "Host-CN reverse DNS lookup failed."
                             );
                         }
                     } catch ( UnknownHostException e ) {
                         throw new BadCredentialsException( "", e );
                     }
                 GNDMSUserDetails userDetails = new GNDMSUserDetails();
                 userDetails.setAuthorities( Collections.<GrantedAuthority>emptyList() );
                 userDetails.setDn( dn );
                 userDetails.setIsUser( false );
                 return userDetails;
             } else {
                 final SecurityContext context = SecurityContextHolder.getContext();
                 if( context != null && context.getAuthentication() != null ) {
                     final Object principal = context.getAuthentication().getPrincipal();
                     if( principal instanceof GNDMSUserDetails ) {
                         // now this must be the Request header authentication
                         final GNDMSUserDetails gndmsUserDetails = ( GNDMSUserDetails ) principal;
                         if( gndmsUserDetails.isUser() )
                             // the x509 cert from the previous filter must have been a user cert
                             // check if the dn's match
                             if (! dn.equals( gndmsUserDetails.getUsername() ) )
                                 throw new UsernameNotFoundException( "Certificate vs HttpHeader: dn " +
                                                                  "mismatch");
                     }
                 }
                 return userDetailsService.loadUserByUsername( dn );
             }
         } catch ( IOException e ) {
             throw new RuntimeException( e );
         }
     }
 
     
     private boolean reverseDNSLookup( final String hostName, final Object details ) throws UnknownHostException {
 
         String requestSourceIp = null;
 
         if( details instanceof WebAuthenticationDetails ) {
             requestSourceIp = ((WebAuthenticationDetails) details).getRemoteAddress();
             InetAddress addr = InetAddress.getByName( hostName );
             if( ! addr.getHostAddress().equals( requestSourceIp ) ) {
                 logger.info( "Revers dns lookup fail for host: \"" +hostName + "\" got: " +
                         requestSourceIp );
                 return false;
             }
             return true;
         }
 
         return false;
     }
         
 
 
 
     public GridMapUserDetailsService getUserDetailsService() {
 
         return userDetailsService;
     }
 
 
     public void setUserDetailsService( final GridMapUserDetailsService userDetailsService ) {
 
         this.userDetailsService = userDetailsService;
     }
 
 
     public void setReverseDNSTest( final boolean reverseDNSTest ) {
 
         this.reverseDNSTest = reverseDNSTest;
     }
 
 
     public void setAllowedHostsFileName( final String allowedHostsFileName ) {
 
         this.allowedHostsFileName = allowedHostsFileName;
     }
 
 
     public String getAllowedHostsFileName() {
 
         return allowedHostsFileName;
     }
 
 
     public boolean isReverseDNSTest() {
 
         return reverseDNSTest;
     }
 
 }
