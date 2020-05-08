 /*
  * SafeOnline project.
  *
  * Copyright 2006-2007 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 
 package net.link.safeonline.sdk.auth.protocol.saml2;
 
 import com.google.common.base.Charsets;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.security.KeyPair;
 import java.security.cert.X509Certificate;
 import java.util.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.*;
 import net.link.safeonline.sdk.auth.protocol.AuthnProtocolRequestContext;
 import net.link.safeonline.sdk.auth.protocol.ProtocolContext;
 import net.link.util.common.CertificateChain;
 import net.link.util.common.DomUtils;
 import net.link.util.error.ValidationFailedException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.bouncycastle.util.encoders.Base64;
 import org.joda.time.DateTime;
 import org.opensaml.saml2.core.*;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 
 /**
  * Utility class for SAML2 responses.
  *
  * @author lhunath
  */
 public abstract class ResponseUtil {
 
     private static final Log LOG = LogFactory.getLog( ResponseUtil.class );
     private static final DocumentBuilder documentBuilder;
 
     static {
         try {
             DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
             documentBuilderFactory.setNamespaceAware( true );
             documentBuilder = documentBuilderFactory.newDocumentBuilder();
         }
         catch (ParserConfigurationException e) {
             throw new RuntimeException( e );
         }
     }
 
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
      * @param breakFrame           Used in browser post form to jump out of an iframe if wanted ( target="_top" )
      *
      * @throws IOException IO Exception
      */
     public static void sendResponse(String consumerUrl, SAMLBinding responseBinding, StatusResponseType samlResponse,
                                     KeyPair signingKeyPair, CertificateChain certificateChain, HttpServletResponse response,
                                    String relayState, String postTemplateResource, Locale language, boolean breakFrame)
             throws IOException {
 
         switch (responseBinding) {
             case HTTP_POST:
                 PostBindingUtil.sendResponse( samlResponse, signingKeyPair, certificateChain, relayState, postTemplateResource, consumerUrl,
                         response, language, breakFrame );
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
      * @param request             HTTP Servlet Request
      * @param contexts            map of {@link ProtocolContext}'s, one matching the original authentication request will be looked up
      * @param trustedCertificates The linkID service certificates for validation of the HTTP-Redirect signature (else can be
      *                            <code>null</code> or empty)
      *
      * @return The SAML {@link Saml2ResponseContext} that is in the HTTP request<br> <code>null</code> if there is no SAML message in the
      *         HTTP request. Also contains (if present) the certificate chain embedded in the SAML {@link Response}'s signature.
      *
      * @throws ValidationFailedException validation failed for some reason
      */
     public static Saml2ResponseContext findAndValidateAuthnResponse(HttpServletRequest request, Map<String, ProtocolContext> contexts,
                                                                     Collection<X509Certificate> trustedCertificates)
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
         CertificateChain certificateChain = LinkIDSaml2Utils.validateSignature( authnResponse.getSignature(), request,
                 trustedCertificates );
 
         // validate response
         validateResponse( authnResponse, authnRequest.getIssuer() );
 
         return new Saml2ResponseContext( authnResponse, certificateChain );
     }
 
     public static Assertion findAuthnAssertion(HttpServletRequest request)
             throws ValidationFailedException {
 
         String b64Assertion = request.getParameter( "SAMLAssertion" );
         if (b64Assertion == null || b64Assertion.isEmpty()) {
             LOG.debug( "No Authn Assertion in request." );
             return null;
         }
 
         byte[] assertionBytes = Base64.decode( b64Assertion );
         try {
             Element assertionElement = documentBuilder.parse( new ByteArrayInputStream( assertionBytes ) ).getDocumentElement();
             if (LOG.isDebugEnabled())
                 LOG.debug( "Found assertion:\n" + DomUtils.domToString( assertionElement ) );
 
             return (Assertion) LinkIDSaml2Utils.unmarshall( assertionElement );
         }
         catch (SAXException e) {
             throw new RuntimeException( "Cannot parse assertion: <" + new String( assertionBytes, Charsets.UTF_8 ) + ">", e );
         }
         catch (IOException e) {
             throw new RuntimeException( e );
         }
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
 
         if (StatusCode.SUCCESS_URI.equals( samlStatusCode )) {
             List<Assertion> assertions = response.getAssertions();
             if (assertions.isEmpty()) {
                 throw new ValidationFailedException( "missing Assertion in SAML2 Response" );
             }
 
             for (Assertion assertion : assertions) {
                 validateAssertion( assertion, now, audience );
             }
         }
     }
 
     /**
      * Validates the specified assertion.
      * <p/>
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
             validateAudienceRestriction( conditions, expectedAudience );
     }
 
     /**
      * Check whether the audience of the response corresponds to the original audience restriction
      */
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
      * @param request  HTTP Servlet Request
      * @param contexts map of {@link ProtocolContext}'s, one matching the original authentication request will be looked up
      *
      * @return The SAML2 response containing the {@link LogoutResponse} in the HTTP request and the optional certificate chain embedded in
      *         the signature of the signed response..<br> <code>null</code> if there is no SAML message in the HTTP request.
      *
      * @throws ValidationFailedException validation failed for some reason
      */
     public static Saml2ResponseContext findAndValidateLogoutResponse(HttpServletRequest request, Map<String, ProtocolContext> contexts,
                                                                      Collection<X509Certificate> trustedCertificates)
             throws ValidationFailedException {
 
         LogoutResponse logoutResponse = findLogoutResponse( request );
         if (logoutResponse == null)
             return null;
 
         // validate signature
         CertificateChain certificateChain = LinkIDSaml2Utils.validateSignature( logoutResponse.getSignature(), request,
                 trustedCertificates );
 
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
