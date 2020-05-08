 /*
  * SafeOnline project.
  *
  * Copyright 2006-2007 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 
 package net.link.safeonline.sdk.auth.protocol.saml2;
 
 import java.io.IOException;
 import java.security.KeyPair;
 import java.security.PrivateKey;
 import java.security.cert.X509Certificate;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import net.link.safeonline.sdk.auth.protocol.AuthnProtocolRequestContext;
 import net.link.safeonline.sdk.auth.protocol.ProtocolContext;
 import net.link.safeonline.sdk.logging.exception.ValidationFailedException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.joda.time.DateTime;
 import org.opensaml.saml2.core.*;
 
 
 /**
  * Utility class for SAML2 responses.
  *
  * @author lhunath
  */
 public abstract class ResponseUtil {
 
     private static final Log LOG = LogFactory.getLog( ResponseUtil.class );
 
     /**
      * Sends out a SAML response message to the specified consumer URL.
      *
      * @param consumerUrl          The URL of the SAML response message consumer.
      * @param certificateChain     optional certificate chain, if not specified KeyInfo in signature will be the PublicKey.
      * @param responseBinding      The SAML Binding to use for communicating the response to the consumer.
      * @param samlResponse         The SAML response token.
      * @param signingKeyPair       The {@link KeyPair} to use for generating the message signature.
      * @param relayState           The RelayState that was passed in the matching request.
      * @param response             The {@link HttpServletResponse} to write the response to.
      * @param postTemplateResource The resource that contains the template of the SAML HTTP POST Binding message.
      * @param language             A language hint to make the application retrieving the response use the same locale as the requesting
      *                             application.
      *
      * @throws IOException IO Exception
      */
     public static void sendResponse(String consumerUrl, SAMLBinding responseBinding, StatusResponseType samlResponse,
                                     KeyPair signingKeyPair, List<X509Certificate> certificateChain, HttpServletResponse response,
                                     String relayState, String postTemplateResource, Locale language)
             throws IOException {
 
         switch (responseBinding) {
             case HTTP_POST:
                 PostBindingUtil.sendResponse( samlResponse, signingKeyPair, certificateChain, relayState, postTemplateResource, consumerUrl,
                                               response, language );
                 break;
 
             case HTTP_REDIRECT:
                 RedirectBindingUtil.sendResponse( samlResponse, signingKeyPair, relayState, consumerUrl, response );
                 break;
         }
     }
 
     /**
      * Validates a SAML response in the specified HTTP request. Checks: <ul> <li>response ID</li> <li>response validated with STS WS
      * location</li> <li>at least 1 assertion present</li> <li>assertion subject</li> <li>assertion conditions notOnOrAfter and notBefore
      * </ul>
      *
      * @param request                 HTTP Servlet Request
      * @param contexts                map of {@link ProtocolContext}'s, one matching the original authentication request will be looked up
      * @param applicationCertificate  application certificate used in the STS WS Request
      * @param applicationPrivateKey   application private key used for signing the STS WS request
      * @param serviceCertificates     The linkID service certificates for validation of the HTTP-Redirect signature (else can be
      *                                <code>null</code> or empty)
      * @param serviceRootCertificates The linkID service root certificate, optionally used for trust validation of the cert.chain returned
      *                                in signed authentication responses.
      *
      * @return The SAML {@link Saml2ResponseContext} that is in the HTTP request<br> <code>null</code> if there is no SAML message in the
      *         HTTP request. Also contains (if present) the certificate chain embedded in the SAML {@link Response}'s signature.
      *
      * @throws ValidationFailedException validation failed for some reason
      */
     public static Saml2ResponseContext findAndValidateAuthnResponse(HttpServletRequest request, Map<String, ProtocolContext> contexts,
                                                                     X509Certificate applicationCertificate,
                                                                     PrivateKey applicationPrivateKey,
                                                                     List<X509Certificate> serviceCertificates,
                                                                     List<X509Certificate> serviceRootCertificates)
             throws ValidationFailedException {
 
         Response authnResponse = findAuthnResponse( request );
         if (authnResponse == null) {
             LOG.debug( "No Authn Response in request." );
             return null;
         }
 
         // Check whether the response is indeed a response to a previous request by comparing the InResponseTo fields
         AuthnProtocolRequestContext authnRequest = (AuthnProtocolRequestContext) contexts.get( authnResponse.getInResponseTo() );
         if (authnRequest == null || !authnResponse.getInResponseTo().equals( authnRequest.getId() ))
             throw new ValidationFailedException( "Request's SAML response ID does not match that of any active requests." );
         LOG.debug( "response matches request: " + authnRequest );
 
         // validate signature
         List<X509Certificate> certificateChain = Saml2Util.validateSignature( authnResponse.getSignature(), serviceCertificates, request );
 
         // validate cert.chain trust
         if (null != serviceRootCertificates && !serviceRootCertificates.isEmpty()) {
             LOG.debug( "LinkID certificate chain trust validation" );
             Saml2Util.validateCertificateChain( serviceRootCertificates, certificateChain );
         }
 
         // validate response
         validateResponse( authnResponse, authnRequest.getIssuer() );
 
         return new Saml2ResponseContext( authnResponse, certificateChain );
     }
 
     /**
      * @param request HTTP Servlet Request
      *
      * @return The SAML {@link Response} that is in the HTTP request<br> <code>null</code> if there is no SAML message in the HTTP request.
      */
     public static Response findAuthnResponse(HttpServletRequest request) {
 
         return BindingUtil.findSAMLObject( request, Response.class );
     }
 
     /**
      * Validate the SAML v2.0 Authentication Response.
      *
      * @param response the authentication response
      * @param audience the expected audience
      *
      * @throws ValidationFailedException validation failed for some reason
      */
     public static void validateResponse(Response response, String audience)
             throws ValidationFailedException {
 
         DateTime now = new DateTime();
 
         // check status
         String samlStatusCode = response.getStatus().getStatusCode().getValue();
         if (!samlStatusCode.equals( StatusCode.AUTHN_FAILED_URI ) && !samlStatusCode.equals( StatusCode.UNKNOWN_PRINCIPAL_URI )
             && !StatusCode.SUCCESS_URI.equals( samlStatusCode )) {
             throw new ValidationFailedException( "Invalid SAML status code: " + samlStatusCode );
         }
 
        List<Assertion> assertions = response.getAssertions();
        if (assertions.isEmpty()) {
            throw new ValidationFailedException( "missing Assertion in SAML2 Response" );
        }

        for (Assertion assertion : assertions) {
            validateAssertion( assertion, now, audience );
         }
     }
 
     /**
      * Validates the specified assertion.
      *
      * Validates : <ul> <li>The notBefore and notOnOrAfter conditions based on the specified time.</li> <li>If the audience in the audience
      * restriction matches the specified audience</li> <li>If a subject is present</li> </ul>
      *
      * @param assertion        SAML v2.0 assertion to validate
      * @param now              current time to validate assertion's conditions against
      * @param expectedAudience expected audience in the assertion
      *
      * @throws ValidationFailedException One of the validation checks failed.
      */
     public static void validateAssertion(Assertion assertion, DateTime now, String expectedAudience)
             throws ValidationFailedException {
 
         Conditions conditions = assertion.getConditions();
         DateTime notBefore = conditions.getNotBefore();
         DateTime notOnOrAfter = conditions.getNotOnOrAfter();
 
         LOG.debug( "now: " + now.toString() );
         LOG.debug( "notBefore: " + notBefore.toString() );
         LOG.debug( "notOnOrAfter : " + notOnOrAfter.toString() );
 
         if (now.isBefore( notBefore )) {
             // time skew
             if (now.plusMinutes( 5 ).isBefore( notBefore ) || now.minusMinutes( 5 ).isAfter( notOnOrAfter ))
                 throw new ValidationFailedException(
                         "SAML2 assertion validation audience=" + expectedAudience + " : invalid SAML message timeframe" );
         } else if (now.isBefore( notBefore ) || now.isAfter( notOnOrAfter ))
             throw new ValidationFailedException(
                     "SAML2 assertion validation audience=" + expectedAudience + " : invalid SAML message timeframe" );
 
         Subject subject = assertion.getSubject();
         if (null == subject)
             throw new ValidationFailedException(
                     "SAML2 assertion validation audience=" + expectedAudience + " : missing Assertion Subject" );
 
         if (assertion.getAuthnStatements().isEmpty())
             throw new ValidationFailedException( "SAML2 assertion validation audience=" + expectedAudience + " : missing AuthnStatement" );
 
         AuthnStatement authnStatement = assertion.getAuthnStatements().get( 0 );
         if (null == authnStatement.getAuthnContext())
             throw new ValidationFailedException( "SAML2 assertion validation audience=" + expectedAudience + " : missing AuthnContext" );
 
         if (null == authnStatement.getAuthnContext().getAuthnContextClassRef())
             throw new ValidationFailedException(
                     "SAML2 assertion validation audience=" + expectedAudience + " : missing AuthnContextClassRef" );
 
         if (expectedAudience != null)
             // Check whether the audience of the response corresponds to the original audience restriction
             validateAudienceRestriction( conditions, expectedAudience );
     }
 
     private static void validateAudienceRestriction(Conditions conditions, String expectedAudience)
             throws ValidationFailedException {
 
         List<AudienceRestriction> audienceRestrictions = conditions.getAudienceRestrictions();
         if (audienceRestrictions.isEmpty())
             throw new ValidationFailedException(
                     "SAML2 assertion validation audience=" + expectedAudience + " : no Audience Restrictions found in response assertion" );
 
         AudienceRestriction audienceRestriction = audienceRestrictions.get( 0 );
         List<Audience> audiences = audienceRestriction.getAudiences();
         if (audiences.isEmpty())
             throw new ValidationFailedException(
                     "SAML2 assertion validation audience=" + expectedAudience + " : no Audiences found in AudienceRestriction" );
 
         Audience audience = audiences.get( 0 );
         if (!expectedAudience.equals( audience.getAudienceURI() ))
             throw new ValidationFailedException(
                     "SAML2 assertion validation: audience name not correct, expected: " + expectedAudience + " was: "
                     + audience.getAudienceURI() );
     }
 
     /**
      * Returns the SAML v2.0 {@link LogoutResponse} embedded in the request. Throws a {@link ValidationFailedException} if not found, the
      * signature isn't valid or of the wrong type.
      *
      * @param request                 HTTP Servlet Request
      * @param contexts                map of {@link ProtocolContext}'s, one matching the original authentication request will be looked up
      * @param applicationCertificate  application certificate used in the STS WS Request
      * @param applicationPrivateKey   application private key used for signing the STS WS request
      * @param serviceCertificates     optional LinkID service certificates for validation of the signature on the logout response (e.g. for
      *                                SAML v2.0 with HTTP-Redirect binding).
      * @param serviceRootCertificates The linkID service root certificates, optionally used for trust validation of the cert.chain returned
      *                                in signed authentication responses.
      *
      * @return The SAML2 response containing the {@link LogoutResponse} in the HTTP request and the optional certificate chain embedded in
      *         the signature of the signed response..<br> <code>null</code> if there is no SAML message in the HTTP request.
      *
      * @throws ValidationFailedException validation failed for some reason
      */
     public static Saml2ResponseContext findAndValidateLogoutResponse(HttpServletRequest request, Map<String, ProtocolContext> contexts,
                                                                      X509Certificate applicationCertificate,
                                                                      PrivateKey applicationPrivateKey,
                                                                      List<X509Certificate> serviceCertificates,
                                                                      List<X509Certificate> serviceRootCertificates)
             throws ValidationFailedException {
 
         LogoutResponse logoutResponse = findLogoutResponse( request );
         if (logoutResponse == null)
             return null;
 
         // validate signature
         List<X509Certificate> certificateChain = Saml2Util.validateSignature( logoutResponse.getSignature(), serviceCertificates, request );
 
         // validate cert.chain trust
         if (null != serviceRootCertificates && !serviceRootCertificates.isEmpty()) {
             LOG.debug( "Logout Response: LinkID certificate chain trust validation" );
             Saml2Util.validateCertificateChain( serviceRootCertificates, certificateChain );
         }
 
         // Check whether the response is indeed a response to a previous request by comparing the InResponseTo fields
         ProtocolContext logoutRequest = contexts.get( logoutResponse.getInResponseTo() );
         if (logoutRequest == null || !logoutResponse.getInResponseTo().equals( logoutRequest.getId() ))
             throw new RuntimeException( "Request's SAML response ID does not match that of any active requests." );
 
         return new Saml2ResponseContext( logoutResponse, certificateChain );
     }
 
     /**
      * @param request HTTP Servlet Request
      *
      * @return The {@link LogoutResponse} in the HTTP request.<br> <code>null</code> if there is no SAML message in the HTTP request.
      */
     public static LogoutResponse findLogoutResponse(HttpServletRequest request) {
 
         return BindingUtil.findSAMLObject( request, LogoutResponse.class );
     }
 }
