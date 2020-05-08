 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package org.lafayette.server.resources;
 
 import java.util.List;
 import javax.ws.rs.core.HttpHeaders;
 import org.lafayette.server.http.ForbiddenException;
 import org.lafayette.server.http.UnauthorizedException;
 import org.lafayette.server.http.digest.ResponseParameters;
 
 /**
  * XXX Consider package private.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public abstract class AuthenticatedResouce extends BaseResource {
 
     /**
      * Call this method to intercept authentication.
      */
     protected final void authenticate() {
         log.debug("Start authentication");
 
         if (requestHasAuthenticationHeader()) {
             log.debug("Verify authentication.");
             verifyAuthentiaction();
 
             if (!isAuthenticated()) {
                 log.debug("Not authorized! Send forbidden response.");
                 sendForbiddenResponse(); // 403
             }
         } else {
             log.debug("Request doesn't have authentication header! Send authentication response.");
             sendAuthenticationResponse(); // 401
         }
     }
 
     private String getAuthorizationHeader() {
       final List<String> authHeader = headers().getRequestHeader(HttpHeaders.AUTHORIZATION);
 
       if (authHeader == null || authHeader.isEmpty()) {
           log.debug("No authorization header sent by client.");
           return "";
       }
 
       if (authHeader.size() > 1) {
           log.warn("More than one authorization header sent by client! Using first one and ignore others.");
       }
 
       return authHeader.get(0);
     }
 
     private boolean requestHasAuthenticationHeader() {
         return !getAuthorizationHeader().isEmpty();
     }
 
     private void verifyAuthentiaction() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     private boolean isAuthenticated() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     private void sendForbiddenResponse() {
         throw new ForbiddenException();
     }
 
     private void sendAuthenticationResponse() {
         final ResponseParameters params = new ResponseParameters();
         throw new UnauthorizedException(params);
     }
 }
