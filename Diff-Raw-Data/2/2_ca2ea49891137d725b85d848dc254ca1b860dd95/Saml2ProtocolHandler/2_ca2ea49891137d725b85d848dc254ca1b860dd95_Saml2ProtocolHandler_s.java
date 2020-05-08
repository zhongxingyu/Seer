 /*
  * SafeOnline project.
  *
  * Copyright 2006-2007 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 
 package net.link.safeonline.sdk.auth.protocol.saml2;
 
 import static net.link.safeonline.sdk.configuration.SDKConfigHolder.*;
 
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 import com.lyndir.lhunath.opal.system.logging.Logger;
 import java.io.IOException;
 import java.net.URI;
 import java.util.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import net.link.safeonline.sdk.api.attribute.AttributeSDK;
 import net.link.safeonline.sdk.auth.protocol.*;
 import net.link.safeonline.sdk.configuration.*;
 import net.link.util.common.CertificateChain;
 import net.link.util.exception.ValidationFailedException;
 import net.link.util.saml.Saml2Utils;
 import org.jetbrains.annotations.Nullable;
 import org.opensaml.saml2.core.*;
 
 
 /**
  * Implementation class for the SAML2 browser POST authentication protocol.
  *
  * @author fcorneli
  */
 public class Saml2ProtocolHandler implements ProtocolHandler {
 
     private static final Logger logger = Logger.get( Saml2ProtocolHandler.class );
 
     private AuthenticationContext authnContext;
     private LogoutContext         logoutContext;
 
     @Override
     public Protocol getProtocol() {
 
         return Protocol.SAML2;
     }
 
     @Override
     public AuthnProtocolRequestContext sendAuthnRequest(HttpServletResponse response, AuthenticationContext context)
             throws IOException {
 
         authnContext = context;
 
         String targetURL = authnContext.getTarget();
         if (targetURL == null || !URI.create( targetURL ).isAbsolute())
             targetURL = ConfigUtils.getApplicationURLForPath( targetURL );
         logger.dbg( "target url: %s", targetURL );
 
         String landingURL = null;
         if (config().web().landingPath() != null)
             landingURL = ConfigUtils.getApplicationConfidentialURLFromPath( config().web().landingPath() );
         logger.dbg( "landing url: %s", landingURL );
 
         if (landingURL == null) {
             // If no landing URL is configured, land on target.
             landingURL = targetURL;
             targetURL = null;
         }
 
         String authnService;
         if (authnContext.isMobileAuthentication())
             authnService = config().web().mobileAuthURL();
         else if (authnContext.isMobileAuthenticationMinimal())
             authnService = config().web().mobileAuthMinimalURL();
         else
             authnService = ConfigUtils.getLinkIDAuthURLFromPath( config().linkID().authPath() );
 
         String templateResourceName = config().proto().saml().postBindingTemplate();
 
         AuthnRequest samlRequest = AuthnRequestFactory.createAuthnRequest( authnContext.getApplicationName(), null,
                 authnContext.getApplicationFriendlyName(), landingURL, authnService, authnContext.getDevices(), authnContext.isForceAuthentication(),
                 authnContext.getSessionTrackingId(), authnContext.getDeviceContext(), authnContext.getSubjectAttributes() );
 
         CertificateChain certificateChain = null;
         if (null != authnContext.getApplicationCertificate()) {
             certificateChain = new CertificateChain( authnContext.getApplicationCertificate() );
         }
 
         RequestUtil.sendRequest( authnService, authnContext.getSAML().getBinding(), samlRequest, authnContext.getApplicationKeyPair(),
                 certificateChain, response, authnContext.getSAML().getRelayState(), templateResourceName, authnContext.getLanguage(),
                 authnContext.getThemeName(), authnContext.getLoginMode(), authnContext.getStartPage() );
 
         logger.dbg( "sending Authn Request for: %s issuer=%s", authnContext.getApplicationName(), samlRequest.getIssuer().getValue() );
         return new AuthnProtocolRequestContext( samlRequest.getID(), samlRequest.getIssuer().getValue(), this, targetURL,
                 authnContext.isMobileAuthentication(), authnContext.isMobileAuthenticationMinimal() );
     }
 
     @Nullable
     @Override
     public AuthnProtocolResponseContext findAndValidateAuthnResponse(HttpServletRequest request,
                                                                      final Function<AuthnProtocolResponseContext, AuthenticationContext> responseToContext)
             throws ValidationFailedException {
 
         if (authnContext == null)
             // This protocol handler has not sent an authentication request.
             return null;
 
         Map<String, ProtocolContext> contexts = ProtocolContext.getContexts( request.getSession() );
         Saml2ResponseContext saml2ResponseContext = ResponseUtil.findAndValidateAuthnResponse( request, contexts,
                 authnContext.getTrustedCertificates() );
         if (null == saml2ResponseContext)
             // The request does not contain an authentication response or it didn't match the request sent by this protocol handler.
             return null;
 
         Response samlResponse = (Response) saml2ResponseContext.getResponse();
 
         String userId = null, applicationName = null;
         List<String> authenticatedDevices = new LinkedList<String>();
         Map<String, List<AttributeSDK<?>>> attributes = new HashMap<String, List<AttributeSDK<?>>>();
         if (!samlResponse.getAssertions().isEmpty()) {
             Assertion assertion = samlResponse.getAssertions().get( 0 );
             userId = assertion.getSubject().getNameID().getValue();
             for (AuthnStatement authnStatement : assertion.getAuthnStatements()) {
                 authenticatedDevices.add( authnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef() );
                 logger.dbg( "authenticated device %s", authnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef() );
             }
             attributes.putAll( LinkIDSaml2Utils.getAttributeValues( assertion ) );
             applicationName = LinkIDSaml2Utils.findApplicationName( assertion );
         }
         logger.dbg( "userId: %s", userId );
 
         boolean success = samlResponse.getStatus().getStatusCode().getValue().equals( StatusCode.SUCCESS_URI );
         AuthnProtocolRequestContext authnRequest = ProtocolContext.findContext( request.getSession(), samlResponse.getInResponseTo() );
 
         AuthenticationContext authnContext = responseToContext.apply(
                 new AuthnProtocolResponseContext( authnRequest, null, userId, applicationName, authenticatedDevices, attributes, true, null ) );
         authnRequest = new AuthnProtocolRequestContext( samlResponse.getInResponseTo(), authnContext.getApplicationName(), this,
                null != authnContext.getTarget()? authnContext.getTarget(): authnRequest.getTarget(), authnRequest.isMobileAuthentication(),
                 authnRequest.isMobileAuthenticationMinimal() );
 
         return new AuthnProtocolResponseContext( authnRequest, samlResponse.getID(), userId, applicationName, authenticatedDevices, attributes,
                 success, saml2ResponseContext.getCertificateChain() );
     }
 
     @Override
     public AuthnProtocolResponseContext findAndValidateAuthnAssertion(final HttpServletRequest request,
                                                                       final Function<AuthnProtocolResponseContext, AuthenticationContext> responseToContext)
             throws ValidationFailedException {
 
         Assertion assertion = ResponseUtil.findAuthnAssertion( request );
         if (null == assertion)
             // The request does not contain an authentication response or it didn't match the request sent by this protocol handler.
             return null;
 
         Map<String, List<AttributeSDK<?>>> attributes = LinkIDSaml2Utils.getAttributeValues( assertion );
         String userId = assertion.getSubject().getNameID().getValue();
         List<String> authenticatedDevices = LinkIDSaml2Utils.getAuthenticatedDevices( assertion );
         String applicationName = LinkIDSaml2Utils.findApplicationName( assertion );
 
         AuthenticationContext authnContext = responseToContext.apply(
                 new AuthnProtocolResponseContext( null, null, userId, applicationName, authenticatedDevices, attributes, true, null ) );
         AuthnProtocolRequestContext authnRequest = new AuthnProtocolRequestContext( null, authnContext.getApplicationName(), this,
                 authnContext.getTarget(), false, false );
 
         CertificateChain certificateChain = Saml2Utils.validateSignature( assertion.getSignature(), request, authnContext.getTrustedCertificates() );
 
         return new AuthnProtocolResponseContext( authnRequest, null, userId, applicationName, authenticatedDevices, attributes, true,
                 certificateChain );
     }
 
     @Override
     public LogoutProtocolRequestContext sendLogoutRequest(HttpServletResponse response, String userId, LogoutContext context)
             throws IOException {
 
         logoutContext = context;
 
         String targetURL = logoutContext.getTarget();
         if (targetURL == null || !URI.create( targetURL ).isAbsolute())
             targetURL = ConfigUtils.getApplicationURLForPath( targetURL );
         logger.dbg( "target url: %s", targetURL );
 
         String logoutService = ConfigUtils.getLinkIDAuthURLFromPath( config().linkID().logoutPath() );
 
         String templateResourceName = config().proto().saml().postBindingTemplate();
 
         LogoutRequest samlRequest = LogoutRequestFactory.createLogoutRequest( userId, logoutContext.getApplicationName(), logoutService,
                 logoutContext.getSessionTrackingId() );
 
         CertificateChain certificateChain = null;
         if (null != context.getApplicationCertificate()) {
             certificateChain = new CertificateChain( context.getApplicationCertificate() );
         }
 
         RequestUtil.sendRequest( logoutService, logoutContext.getSAML().getBinding(), samlRequest, logoutContext.getApplicationKeyPair(),
                 certificateChain, response, logoutContext.getSAML().getRelayState(), templateResourceName, logoutContext.getLanguage(),
                 logoutContext.getThemeName(), logoutContext.getLoginMode(), null );
 
         return new LogoutProtocolRequestContext( samlRequest.getID(), samlRequest.getIssuer().getValue(), this, targetURL,
                 samlRequest.getNameID().getValue() );
     }
 
     @Nullable
     @Override
     public LogoutProtocolResponseContext findAndValidateLogoutResponse(HttpServletRequest request)
             throws ValidationFailedException {
 
         if (logoutContext == null)
             // This protocol handler has not sent an authentication request.
             return null;
 
         Map<String, ProtocolContext> contexts = ProtocolContext.getContexts( request.getSession() );
         Saml2ResponseContext saml2ResponseContext = ResponseUtil.findAndValidateLogoutResponse( request, contexts,
                 logoutContext.getTrustedCertificates() );
         if (null == saml2ResponseContext)
             // The request does not contain a logout response or it didn't match the request sent by this protocol handler.
             return null;
 
         LogoutResponse samlResponse = (LogoutResponse) saml2ResponseContext.getResponse();
 
         String status = samlResponse.getStatus().getStatusCode().getValue();
         boolean success = !(!status.equals( StatusCode.SUCCESS_URI ) && !status.equals( StatusCode.PARTIAL_LOGOUT_URI ));
         LogoutProtocolRequestContext logoutRequest = ProtocolContext.findContext( request.getSession(), samlResponse.getInResponseTo() );
         return new LogoutProtocolResponseContext( logoutRequest, samlResponse.getID(), success, saml2ResponseContext.getCertificateChain() );
     }
 
     @Override
     public LogoutProtocolRequestContext findAndValidateLogoutRequest(HttpServletRequest request,
                                                                      Function<LogoutProtocolRequestContext, LogoutContext> requestToContext)
             throws ValidationFailedException {
 
         LogoutRequest samlRequest = RequestUtil.findLogoutRequest( request );
         if (null == samlRequest)
             // The request does not contain a logout request of the protocol handled by this protocol handler.
             return null;
 
         LogoutProtocolRequestContext logoutRequest = new LogoutProtocolRequestContext( samlRequest.getID(), samlRequest.getIssuer().getValue(), this,
                 null, samlRequest.getNameID().getValue() );
         logoutContext = requestToContext.apply( logoutRequest );
 
         CertificateChain certificateChain = RequestUtil.validateRequest( request, samlRequest, logoutContext.getTrustedCertificates() );
 
         logoutRequest.setCertificateChain( certificateChain );
         return logoutRequest;
     }
 
     @Override
     public LogoutProtocolResponseContext sendLogoutResponse(HttpServletResponse response, LogoutProtocolRequestContext logoutRequestContext,
                                                             boolean partialLogout)
             throws IOException {
 
         Preconditions.checkNotNull( logoutContext, "This protocol handler has not received a logout request to respond to." );
 
         String templateResourceName = config().proto().saml().postBindingTemplate();
         String logoutExitService = ConfigUtils.getLinkIDAuthURLFromPath( config().linkID().logoutExitPath() );
 
         LogoutResponse samlLogoutResponse = LogoutResponseFactory.createLogoutResponse( partialLogout, logoutRequestContext,
                 logoutContext.getApplicationName(), logoutExitService );
 
         CertificateChain certificateChain = null;
         if (null != logoutContext.getApplicationCertificate()) {
             certificateChain = new CertificateChain( logoutContext.getApplicationCertificate() );
         }
 
         ResponseUtil.sendResponse( logoutExitService, logoutContext.getSAML().getBinding(), samlLogoutResponse, logoutContext.getApplicationKeyPair(),
                 certificateChain, response, logoutContext.getSAML().getRelayState(), templateResourceName, null, null );
 
         String status = samlLogoutResponse.getStatus().getStatusCode().getValue();
         return new LogoutProtocolResponseContext( logoutRequestContext, samlLogoutResponse.getID(), //
                 status.equals( StatusCode.SUCCESS_URI ) || status.equals( StatusCode.PARTIAL_LOGOUT_URI ), certificateChain );
     }
 
     @Override
     public String toString() {
 
         return String.format( "{proto: SAML2, auth=%s, logout=%s}", authnContext, logoutContext );
     }
 }
