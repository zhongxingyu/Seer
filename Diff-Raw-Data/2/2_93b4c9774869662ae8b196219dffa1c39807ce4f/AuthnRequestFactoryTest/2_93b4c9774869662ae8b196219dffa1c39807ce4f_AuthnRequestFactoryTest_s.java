 /*
  * SafeOnline project.
  *
  * Copyright 2006-2007 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 
 package test.unit.net.link.safeonline.sdk.auth.saml2;
 
 import static org.junit.Assert.*;
 
 import com.google.common.collect.Maps;
 import com.lyndir.lhunath.opal.system.logging.Logger;
 import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
 import java.io.Serializable;
 import java.security.KeyPair;
 import java.security.cert.X509Certificate;
 import java.util.*;
 import net.link.safeonline.sdk.api.payment.*;
 import net.link.safeonline.sdk.api.payment.Currency;
 import net.link.safeonline.sdk.auth.protocol.saml2.AuthnRequestFactory;
 import net.link.safeonline.sdk.auth.protocol.saml2.LinkIDSaml2Utils;
 import net.link.safeonline.sdk.auth.protocol.saml2.devicecontext.DeviceContext;
 import net.link.safeonline.sdk.auth.protocol.saml2.paymentcontext.PaymentContext;
 import net.link.safeonline.sdk.auth.protocol.saml2.subjectattributes.SubjectAttributes;
 import net.link.util.common.CertificateChain;
 import net.link.util.common.DomUtils;
 import net.link.util.saml.Saml2Utils;
 import net.link.util.saml.SamlUtils;
 import net.link.util.test.pkix.PkiTestUtils;
 import net.link.util.test.web.DomTestUtils;
 import org.joda.time.DateTime;
 import org.junit.Test;
 import org.opensaml.common.xml.SAMLConstants;
 import org.opensaml.saml2.core.Attribute;
 import org.opensaml.saml2.core.AuthnRequest;
 import org.opensaml.xml.XMLObject;
 import org.opensaml.xml.security.keyinfo.KeyInfoHelper;
 import org.w3c.dom.Document;
 
 
 /**
  * Unit test for authentication request factory.
  *
  * @author fcorneli
  */
 public class AuthnRequestFactoryTest {
 
     private static final Logger logger = Logger.get( AuthnRequestFactoryTest.class );
 
     @Test
     public void createAuthnRequest()
             throws Exception {
 
         // Setup Data
         String applicationName = "test-application-id";
         KeyPair keyPair = PkiTestUtils.generateKeyPair();
         String assertionConsumerServiceURL = "http://test.assertion.consumer.service";
         String destinationURL = "https://test.idp.com/entry";
         String device = "device";
         String session = "test-session-info";
 
         // Test
         long begin = System.currentTimeMillis();
         Set<String> devices = Collections.singleton( device );
         AuthnRequest samlAuthnRequest = AuthnRequestFactory.createAuthnRequest( applicationName, null, null, assertionConsumerServiceURL, destinationURL,
                 devices, false, session, null, null, null );
         String samlAuthnRequestToken = DomUtils.domToString( SamlUtils.sign( samlAuthnRequest, keyPair, null ) );
 
         logger.dbg( DomUtils.domToString( SamlUtils.marshall( samlAuthnRequest ) ) );
 
         long end = System.currentTimeMillis();
 
         // Verify
         assertNotNull( samlAuthnRequest );
         logger.dbg( "duration: %d ms", end - begin );
         logger.dbg( "result message: %s", samlAuthnRequest );
 
         Document resultDocument = DomTestUtils.parseDocument( samlAuthnRequestToken );
         AuthnRequest resultAuthnRequest = LinkIDSaml2Utils.unmarshall( resultDocument.getDocumentElement() );
 
         assertNotNull( resultAuthnRequest );
         assertNotNull( resultAuthnRequest.getSignature() );
 
         assertNotNull( resultAuthnRequest.getIssuer() );
         assertEquals( applicationName, resultAuthnRequest.getIssuer().getValue() );
 
         assertNotNull( resultAuthnRequest.getAssertionConsumerServiceURL() );
         assertEquals( assertionConsumerServiceURL, resultAuthnRequest.getAssertionConsumerServiceURL() );
 
         assertNotNull( resultAuthnRequest.getProtocolBinding() );
         assertEquals( SAMLConstants.SAML2_POST_BINDING_URI, resultAuthnRequest.getProtocolBinding() );
 
         assertNotNull( resultAuthnRequest.getDestination() );
         assertEquals( destinationURL, resultAuthnRequest.getDestination() );
 
         assertNotNull( resultAuthnRequest.getNameIDPolicy() );
         assertNotNull( resultAuthnRequest.getNameIDPolicy().getAllowCreate() );
         assertTrue( resultAuthnRequest.getNameIDPolicy().getAllowCreate() );
 
         // verify signature
         Saml2Utils.validateSignature( resultAuthnRequest.getSignature(), null, null );
     }
 
     @Test
     public void createAuthnRequestWithCertificateChain()
             throws Exception {
 
         // Setup Data
         String applicationName = "test-application-id";
         String assertionConsumerServiceURL = "http://test.assertion.consumer.service";
         String destinationURL = "https://test.idp.com/entry";
         String device = "device";
         String session = "test-session-info";
 
         KeyPair rootKeyPair = PkiTestUtils.generateKeyPair();
         X509Certificate rootCertificate = PkiTestUtils.generateSelfSignedCertificate( rootKeyPair, "CN=Root" );
         KeyPair keyPair = PkiTestUtils.generateKeyPair();
         DateTime notBefore = new DateTime();
         DateTime notAfter = notBefore.plusYears( 1 );
         X509Certificate certificate = PkiTestUtils.generateCertificate( keyPair.getPublic(), "CN=Test", rootKeyPair.getPrivate(), rootCertificate, notBefore,
                 notAfter, null, true, false, false, null );
         CertificateChain certificateChain = new CertificateChain( rootCertificate, certificate );
 
         // Test
         long begin = System.currentTimeMillis();
         Set<String> devices = Collections.singleton( device );
         AuthnRequest samlAuthnRequest = AuthnRequestFactory.createAuthnRequest( applicationName, null, null, assertionConsumerServiceURL, destinationURL,
                 devices, false, session, null, null, null );
         String samlAuthnRequestToken = DomUtils.domToString( SamlUtils.sign( samlAuthnRequest, keyPair, certificateChain ) );
         long end = System.currentTimeMillis();
 
         // Verify
         assertNotNull( samlAuthnRequest );
         logger.dbg( "duration: %d ms", end - begin );
         logger.dbg( "result message: %s", samlAuthnRequest );
 
         Document resultDocument = DomTestUtils.parseDocument( samlAuthnRequestToken );
         AuthnRequest resultAuthnRequest = LinkIDSaml2Utils.unmarshall( resultDocument.getDocumentElement() );
 
         // verify signature
         assertNotNull( resultAuthnRequest.getSignature() );
         assertNotNull( resultAuthnRequest.getSignature().getKeyInfo() );
 
         CertificateChain resultCertificateChain = new CertificateChain( KeyInfoHelper.getCertificates( resultAuthnRequest.getSignature().getKeyInfo() ) );
         assertEquals( 2, resultCertificateChain.getOrderedCertificateChain().size() );
         assertEquals( rootCertificate, resultCertificateChain.getRootCertificate() );
         assertEquals( certificate, resultCertificateChain.getIdentityCertificate() );
 
         Saml2Utils.validateSignature( resultAuthnRequest.getSignature(), null, null );
     }
 
     @Test
     public void createAuthnRequestWithDeviceContextAndSubjectAttributesAndPaymentContext()
             throws Exception {
 
         // Setup Data
         String applicationName = "test-application-id";
         String assertionConsumerServiceURL = "http://test.assertion.consumer.service";
         String destinationURL = "https://test.idp.com/entry";
         String device = "device";
         String session = "test-session-info";
 
         KeyPair keyPair = PkiTestUtils.generateKeyPair();
 
         // Setup device context map
         Map<String, String> deviceContextMap = Maps.newHashMap();
         deviceContextMap.put( "devicecontext1", UUID.randomUUID().toString() );
         deviceContextMap.put( "devicecontext2", UUID.randomUUID().toString() );
 
         // Setup subject attributes map
         Map<String, List<Serializable>> subjectAttributesMap = Maps.newHashMap();
         String testAttributeString = "test.attribute.string";
         String testAttributeBoolean = "test.attribute.boolean";
         String testAttributeDate = "test.attribute.date";
 
         subjectAttributesMap.put( testAttributeString, Arrays.<Serializable>asList( "value1", "value2", "value3" ) );
         subjectAttributesMap.put( testAttributeBoolean, Arrays.<Serializable>asList( true ) );
         subjectAttributesMap.put( testAttributeDate, Arrays.<Serializable>asList( new Date(), new Date() ) );
 
         // Setup Payment context
         PaymentContextDO paymentContext = new PaymentContextDO( 50, Currency.EUR );
 
         // Test
         long begin = System.currentTimeMillis();
         Set<String> devices = Collections.singleton( device );
         AuthnRequest samlAuthnRequest = AuthnRequestFactory.createAuthnRequest( applicationName, null, null, assertionConsumerServiceURL, destinationURL,
                 devices, false, session, deviceContextMap, subjectAttributesMap, paymentContext );
         String samlAuthnRequestToken = DomUtils.domToString( SamlUtils.sign( samlAuthnRequest, keyPair, null ) );
         long end = System.currentTimeMillis();
 
         // Verify
         assertNotNull( samlAuthnRequest );
         logger.dbg( "duration: %d ms", end - begin );
         logger.dbg( "result message: %s", samlAuthnRequest );
 
         Document resultDocument = DomTestUtils.parseDocument( samlAuthnRequestToken );
         AuthnRequest resultAuthnRequest = LinkIDSaml2Utils.unmarshall( resultDocument.getDocumentElement() );
 
         // verify signature
         assertNotNull( resultAuthnRequest.getSignature() );
         assertNotNull( resultAuthnRequest.getSignature().getKeyInfo() );
 
         Saml2Utils.validateSignature( resultAuthnRequest.getSignature(), null, null );
 
         // validate device context map
         List<XMLObject> deviceContexts = resultAuthnRequest.getExtensions().getUnknownXMLObjects( DeviceContext.DEFAULT_ELEMENT_NAME );
         assertNotNull( deviceContexts );
         assertEquals( 1, deviceContexts.size() );
         DeviceContext deviceContext = (DeviceContext) deviceContexts.get( 0 );
         assertEquals( 2, deviceContext.getAttributes().size() );
 
         // validate subject attributes map
         List<XMLObject> saList = resultAuthnRequest.getExtensions().getUnknownXMLObjects( SubjectAttributes.DEFAULT_ELEMENT_NAME );
         assertNotNull( saList );
         assertEquals( 1, saList.size() );
         SubjectAttributes subjectAttributes = (SubjectAttributes) saList.get( 0 );
         assertEquals( 3, subjectAttributes.getAttributes().size() );
         for (Attribute attribute : subjectAttributes.getAttributes()) {
             if (attribute.getName().equals( testAttributeString )) {
                 assertEquals( 3, attribute.getAttributeValues().size() );
             } else if (attribute.getName().equals( testAttributeBoolean )) {
                 assertEquals( 1, attribute.getAttributeValues().size() );
             } else if (attribute.getName().equals( testAttributeDate )) {
                 assertEquals( 2, attribute.getAttributeValues().size() );
             } else {
                 throw new InternalInconsistencyException( String.format( "Unexpected attribute in SubjectAttributesExtension: %s", attribute.getName() ) );
             }
         }
 
         // validate payment context map
         List<XMLObject> paymentContexts = resultAuthnRequest.getExtensions().getUnknownXMLObjects( PaymentContext.DEFAULT_ELEMENT_NAME );
         assertNotNull( paymentContexts );
         assertEquals( 1, paymentContexts.size() );
         PaymentContext paymentContextMap = (PaymentContext) paymentContexts.get( 0 );
        assertEquals( 3, paymentContextMap.getAttributes().size() );
     }
 }
