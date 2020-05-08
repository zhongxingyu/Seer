 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2012, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
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
 
 package cz.muni.fi.pv243.et.exception;
 
 import cz.muni.fi.pv243.et.message.WebMessage;
 import cz.muni.fi.pv243.et.security.EventLog;
 import org.apache.deltaspike.core.api.exception.control.annotation.ExceptionHandler;
 import org.apache.deltaspike.core.api.exception.control.annotation.Handles;
 import org.apache.deltaspike.core.api.exception.control.event.ExceptionEvent;
 import org.apache.deltaspike.security.api.authorization.AccessDeniedException;
 import org.apache.deltaspike.security.api.authorization.SecurityViolation;
 import org.picketlink.authentication.AuthenticationException;
 
 import javax.ejb.EJBException;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 
 @ExceptionHandler
 public class DefaultExceptionHandler {
 
     @Inject
     private FacesContext facesContext;
 
     @Inject
     private EventLog log;
 
     @Inject
     private WebMessage webMessage;
 
     private static final String ERROR_MESSAGE = "errorMessage";
 
     public void onAccessDenied(@Handles ExceptionEvent<AccessDeniedException> event) {
         log.logAccessDenied(event.getException());
 
         this.facesContext.getExternalContext().getSessionMap()
                 .put(ERROR_MESSAGE, webMessage.accessDenied(getReasons(event.getException())));
         event.handled();
     }
 
     public void onAuthenticationException(@Handles ExceptionEvent<AuthenticationException> event) {
         log.logAuthenticationFailed(event.getException());
 
         this.facesContext.getExternalContext().getSessionMap()
                 .put(ERROR_MESSAGE, webMessage.authenticationFailed());
         event.handled();
     }
 
     public void onEJBException(@Handles ExceptionEvent<EJBException> event) {
         log.logGeneralError(event.getException());
 
         this.facesContext.getExternalContext().getSessionMap()
                 .put(ERROR_MESSAGE, webMessage.generalError());
 
         event.handled();
     }
 
     private String getReasons(AccessDeniedException ex) {
         StringBuilder sb = new StringBuilder("<br/>");
 
         for (SecurityViolation sv : ex.getViolations()) {
             sb.append(sv.getReason()).append("<br/>");
         }
         return sb.toString();
     }
 }
