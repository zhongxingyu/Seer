 /*
  * SafeOnline project.
  *
  * Copyright 2006-2007 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 
 package net.link.safeonline.sdk.auth.protocol;
 
 import com.google.common.base.Function;
 import java.io.IOException;
 import java.util.Map;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import net.link.safeonline.sdk.configuration.AuthenticationContext;
 import net.link.safeonline.sdk.configuration.LogoutContext;
 import net.link.util.error.ValidationFailedException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 
 /**
  * Manager class for the stateful protocol handlers.
 *
  * <p> The state is preserved using the HTTP session. </p>
  *
  * @author fcorneli
  */
 public abstract class ProtocolManager {
 
     private static final Log LOG = LogFactory.getLog( ProtocolManager.class );
 
     /**
      * Initiates the authentication.
      *
      * @see ProtocolHandler#sendAuthnRequest(HttpServletResponse, AuthenticationContext)
      */
     public static AuthnProtocolRequestContext initiateAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                                      AuthenticationContext context)
             throws IOException {
 
         ProtocolHandler protocolHandler = context.getProtocol().newHandler();
         AuthnProtocolRequestContext authnRequest = protocolHandler.sendAuthnRequest( response, context );
 
         ProtocolContext.addContext( request.getSession(), authnRequest );
         return authnRequest;
     }
 
     public static AuthnProtocolResponseContext findAndValidateAuthnResponse(HttpServletRequest request)
             throws ValidationFailedException {
 
         Map<String, ProtocolContext> contexts = ProtocolContext.getContexts( request.getSession() );
         for (ProtocolContext protocolContext : contexts.values()) {
             if (protocolContext instanceof AuthnProtocolRequestContext) {
                 AuthnProtocolRequestContext protocolRequestContext = (AuthnProtocolRequestContext) protocolContext;
                 ProtocolHandler protocolHandler = protocolRequestContext.getProtocolHandler();
 
                 AuthnProtocolResponseContext authnResponse = protocolHandler.findAndValidateAuthnResponse( request );
                 if (authnResponse != null)
                     return authnResponse;
             }
         }
 
         LOG.debug( "No authn response found in request matching known Ids." );
         LOG.debug( "Known Contexts:" );
         for (Map.Entry<String, ProtocolContext> protocolContextEntry : contexts.entrySet())
             LOG.debug( protocolContextEntry.getKey() + ": " + protocolContextEntry.getValue() );
 
         return null;
     }
 
     public static AuthnProtocolResponseContext findAndValidateAuthnAssertion(HttpServletRequest request,
                                                                              final Function<AuthnProtocolResponseContext, AuthenticationContext> responseContext)
             throws ValidationFailedException {
 
         for (Protocol protocol : Protocol.values()) {
             AuthnProtocolResponseContext authnResponse = protocol.newHandler().findAndValidateAuthnAssertion( request, responseContext );
             if (authnResponse != null)
                 return authnResponse;
         }
 
         LOG.debug( "No authn assertion found in request." );
         return null;
     }
 
     /**
      * Initiates a logout request.
     *
     * @throws IOException
      */
     public static LogoutProtocolRequestContext initiateLogout(HttpServletRequest request, HttpServletResponse response, String userId,
                                                               LogoutContext context)
             throws IOException {
 
         // Delegate the authentication initiation to the relevant protocol handler.
         ProtocolHandler protocolHandler = context.getProtocol().newHandler();
         LogoutProtocolRequestContext logoutRequest = protocolHandler.sendLogoutRequest( response, userId, context );
 
         ProtocolContext.addContext( request.getSession(), logoutRequest );
         return logoutRequest;
     }
 
     public static LogoutProtocolRequestContext findAndValidateLogoutRequest(HttpServletRequest request,
                                                                             Function<LogoutProtocolRequestContext, LogoutContext> requestToContext)
             throws ValidationFailedException {
 
         for (Protocol protocol : Protocol.values()) {
             ProtocolHandler protocolHandler = protocol.newHandler();
             LogoutProtocolRequestContext logoutRequest = protocolHandler.findAndValidateLogoutRequest( request, requestToContext );
             if (logoutRequest != null)
                 return logoutRequest;
         }
 
         return null;
     }
 
     public static LogoutProtocolResponseContext findAndValidateLogoutResponse(HttpServletRequest request)
             throws ValidationFailedException {
 
         Map<String, ProtocolContext> contexts = ProtocolContext.getContexts( request.getSession() );
         for (ProtocolContext protocolContext : contexts.values()) {
             if (protocolContext instanceof LogoutProtocolRequestContext) {
                 LogoutProtocolRequestContext protocolRequestContext = (LogoutProtocolRequestContext) protocolContext;
                 ProtocolHandler protocolHandler = protocolRequestContext.getProtocolHandler();
 
                 LogoutProtocolResponseContext logoutResponse = protocolHandler.findAndValidateLogoutResponse( request );
                 if (logoutResponse != null)
                     return logoutResponse;
             }
         }
 
         return null;
     }
 }
