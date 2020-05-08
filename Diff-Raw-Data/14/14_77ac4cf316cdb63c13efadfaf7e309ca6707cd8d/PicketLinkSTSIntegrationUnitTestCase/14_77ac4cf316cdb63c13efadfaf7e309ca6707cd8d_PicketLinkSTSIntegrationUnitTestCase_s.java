 /*
  * JBoss, Home of Professional Open Source. Copyright 2008, Red Hat Middleware LLC, and individual contributors as
  * indicated by the @author tags. See the copyright.txt file in the distribution for a full listing of individual
  * contributors.
  * 
  * This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any
  * later version.
  * 
  * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License along with this software; if not, write to
  * the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF site:
  * http://www.fsf.org.
  */
 package org.picketlink.test.integration.sts;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.math.BigInteger;
 import java.net.URI;
 import java.security.KeyFactory;
 import java.security.KeyStore;
 import java.security.Principal;
 import java.security.cert.Certificate;
 import java.security.cert.CertificateFactory;
 import java.security.interfaces.RSAPublicKey;
 import java.security.spec.RSAPublicKeySpec;
 import java.util.List;
 
 import javax.xml.datatype.DatatypeConstants;
 import javax.xml.ws.WebServiceException;
 
 import junit.framework.Assert;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.picketlink.identity.federation.api.wstrust.WSTrustClient;
 import org.picketlink.identity.federation.api.wstrust.WSTrustClient.SecurityInfo;
 import org.picketlink.identity.federation.core.util.Base64;
 import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
 import org.picketlink.identity.federation.core.wstrust.WSTrustUtil;
 import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
 import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
 import org.picketlink.identity.federation.newmodel.saml.v2.assertion.AssertionType;
 import org.picketlink.identity.federation.newmodel.saml.v2.assertion.AudienceRestrictionType;
 import org.picketlink.identity.federation.newmodel.saml.v2.assertion.ConditionAbstractType;
 import org.picketlink.identity.federation.newmodel.saml.v2.assertion.ConditionsType;
 import org.picketlink.identity.federation.newmodel.saml.v2.assertion.NameIDType;
 import org.picketlink.identity.federation.newmodel.saml.v2.assertion.SubjectConfirmationDataType;
 import org.picketlink.identity.federation.newmodel.saml.v2.assertion.SubjectConfirmationType;
 import org.picketlink.identity.federation.newmodel.saml.v2.assertion.SubjectType;
 import org.picketlink.identity.federation.ws.trust.BinarySecretType;
 import org.picketlink.identity.federation.ws.trust.EntropyType;
 import org.picketlink.identity.federation.ws.trust.UseKeyType;
 import org.picketlink.identity.xmlsec.w3.xmldsig.KeyInfoType;
 import org.picketlink.identity.xmlsec.w3.xmldsig.KeyValueType;
 import org.picketlink.identity.xmlsec.w3.xmldsig.RSAKeyValueType;
 import org.picketlink.identity.xmlsec.w3.xmldsig.X509CertificateType;
 import org.picketlink.identity.xmlsec.w3.xmldsig.X509DataType;
 import org.w3c.dom.Element;
 
 /**
  * <p>
  * Integration tests for the PicketLink STS
  * </p>
  * 
  * @author Anil.Saldhana@redhat.com
  * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
  * @since Jun 8, 2010
  */
 public class PicketLinkSTSIntegrationUnitTestCase
 {
    private static WSTrustClient client;
 
    private static Certificate certificate;
 
    @BeforeClass
    public static void initClient() throws Exception
    {
       // create the WSTrustClient instance.
       client = new WSTrustClient("PicketLinkSTS", "PicketLinkSTSPort",
             "http://localhost:8080/picketlink-sts/PicketLinkSTS", new SecurityInfo("admin", "admin"));
 
       // get the certificate used in the public key scenarios.
       InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(
             "keystore/sts_keystore.jks");
       KeyStore keyStore = KeyStore.getInstance("JKS");
       keyStore.load(stream, "testpass".toCharArray());
       certificate = keyStore.getCertificate("service2");
    }
 
    /**
     * <p>
     * This tests sends a SAMLV2.0 security token request to PicketLinkSTS. This request should be handled by the
     * standard {@code SAML20TokenProvider} and should result in a SAMLV2.0 assertion that looks like the following:
     * 
     * <pre>
     * &lt;saml2:Assertion xmlns:saml2=&quot;urn:oasis:names:tc:SAML:2.0:assertion&quot; 
     *                  xmlns:ds=&quot;http://www.w3.org/2000/09/xmldsig#&quot; 
     *                  xmlns:xenc=&quot;http://www.w3.org/2001/04/xmlenc#&quot; 
     *                  ID=&quot;ID-cc541137-74dc-4fc0-8bcc-7e9e3a4c899d&quot;
     *                  IssueInstant=&quot;2009-05-29T18:02:13.458Z&quot;&gt;
     *     &lt;saml2:Issuer&gt;
     *         PicketLinkSTS
     *     &lt;/saml2:Issuer&gt;
     *     &lt;saml2:Subject&gt;
     *         &lt;saml2:NameID NameQualifier=&quot;http://www.jboss.org&quot;&gt;
     *             admin
     *         &lt;/saml2:NameID&gt;
     *         &lt;saml2:SubjectConfirmation Method=&quot;urn:oasis:names:tc:SAML:2.0:cm:bearer&quot;/&gt;
     *     &lt;/saml2:Subject&gt;
     *     &lt;saml2:Conditions NotBefore=&quot;2009-05-29T18:02:13.458Z&quot; NotOnOrAfter=&quot;2009-05-29T19:02:13.458Z&quot;/&gt;
     *     &lt;ds:Signature&gt;
     *         ...
     *     &lt;/ds:Signature&gt;
     * &lt;/saml2:Assertion&gt;
     * </pre>
     * 
     * </p>
     * 
     * @throws Exception
     *            if an error occurs while running the test.
     */
    @Test
    public void testIssueSAML20() throws Exception
    {
       Element assertionElement = client.issueToken(SAMLUtil.SAML2_TOKEN_TYPE);
       Assert.assertNotNull("Invalid null assertion element", assertionElement);
 
       // validate the contents of the SAML assertion.
       AssertionType assertion = this.validateSAML20Assertion(assertionElement, "admin", SAMLUtil.SAML2_BEARER_URI);
 
       // in this scenario, the conditions section should NOT have an audience restriction.
       ConditionsType conditionsType = assertion.getConditions();
 
       List<ConditionAbstractType> conditions = conditionsType.getConditions();
       Assert.assertEquals("Unexpected restriction list size", 0, conditions.size());
    }
 
    /**
     * <p>
     * This test requests a token to the STS using the {@code AppliesTo} to identify the service endpoint. The STS must
     * be able to find out the type of the token that must be issued using the service endpoint URI. In this specific
     * case, the request should be handled by the standard {@code SAML20TokenProvider}.
     * </p>
     * 
     * @throws Exception
     *            if an error occurs while running the test.
     */
    @Test
    public void testIssueSAML20AppliesTo() throws Exception
    {
       Element assertionElement = client.issueTokenForEndpoint("http://services.testcorp.org/provider1");
       Assert.assertNotNull("Invalid null assertion element", assertionElement);
 
       // validate the contents of the SAML assertion.
       AssertionType assertion = this.validateSAML20Assertion(assertionElement, "admin", SAMLUtil.SAML2_BEARER_URI);
 
       // in this scenario, the conditions section should have an audience restriction.
       ConditionsType conditionsType = assertion.getConditions();
       List<ConditionAbstractType> conditions = conditionsType.getConditions();
 
       Assert.assertEquals("Unexpected restriction list size", 1, conditions.size());
       ConditionAbstractType abstractType = conditions.get(0);
       Assert.assertTrue("Unexpected restriction type", abstractType instanceof AudienceRestrictionType);
       AudienceRestrictionType audienceRestriction = (AudienceRestrictionType) abstractType;
       Assert.assertEquals("Unexpected audience restriction list size", 1, audienceRestriction.getAudience().size());
       Assert.assertEquals("Unexpected audience restriction item", "http://services.testcorp.org/provider1",
             audienceRestriction.getAudience().get(0).toString());
 
    }
 
    /**
     * <p>
     * This test requests a SAMLV2.0 assertion on behalf of another identity. The STS must issue an assertion for the
     * identity contained in the {@code OnBehalfOf} section of the WS-Trust request (and not for the identity that sent
     * the request).
     * </p>
     * 
     * @throws Exception
     *            if an error occurs while running the test.
     */
    @Test
    public void testIssueSAML20OnBehalfOf() throws Exception
    {
       // issue a SAML 2.0 assertion for jduke.
       Element assertionElement = client.issueTokenOnBehalfOf(null, SAMLUtil.SAML2_TOKEN_TYPE, new Principal()
       {
          @Override
          public String getName()
          {
             return "jduke";
          }
       });
       Assert.assertNotNull("Invalid null assertion element", assertionElement);
 
       // this scenario results in the sender vouches confirmation method being used.
       AssertionType assertion = this.validateSAML20Assertion(assertionElement, "jduke",
             SAMLUtil.SAML2_SENDER_VOUCHES_URI);
 
       // we haven't specified the service endpoint URI, so no restrictions should be visible.
       ConditionsType conditions = assertion.getConditions();
       Assert.assertEquals("Unexpected restriction list size", 0, conditions.getConditions().size());
    }
 
    /**
     * <p>
     * This test requests a SAMLV2.0 assertion and requires a symmetric key to be used as a proof-of-possession token. As
     * the request doesn't contain any client-specified key, the STS is responsible for generating a random key and use
     * this key as the proof token. The WS-Trust response should contain the STS-generated key.
     * </p>
     * 
     * @throws Exception
     *            if an error occurs while running the test.
     */
    @Test
    public void testIssueSAML20WithSTSGeneratedSymmetricKey() throws Exception
    {
       // create a WS-Trust request for a SAML assertion.
       RequestSecurityToken request = new RequestSecurityToken();
       request.setRequestType(URI.create(WSTrustConstants.ISSUE_REQUEST));
       request.setAppliesTo(WSTrustUtil.createAppliesTo("http://services.testcorp.org/provider1"));
       // add a symmetric key type to the request, but don't supply any client key - STS should generate one.
       request.setKeyType(URI.create(WSTrustConstants.KEY_TYPE_SYMMETRIC));
 
       // dispatch the request and get the issued assertion.
       Element assertionElement = client.issueToken(request);
       Assert.assertNotNull("Invalid null assertion element", assertionElement);
 
       // the usage of a key as proof-of-possession token results in the holder-of-key confirmation method being used.
       AssertionType assertion = this.validateSAML20Assertion(assertionElement, "admin",
             SAMLUtil.SAML2_HOLDER_OF_KEY_URI);
       // validate the holder of key contents.
       SubjectConfirmationType subjConfirmation = (SubjectConfirmationType) assertion.getSubject().getConfirmation()
             .get(0);
       this.validateHolderOfKeyContents(subjConfirmation, WSTrustConstants.KEY_TYPE_SYMMETRIC, null, false);
 
       // TODO: client API must allow access to the WS-Trust response for retrieval of the proof token.
    }
 
    /**
     * <p>
     * This test requests a SAMLV2.0 assertion and requires a symmetric key to be used as a proof-of-possession token. In
     * this case, the client supplies a secret key in the WS-Trust request, so the STS should combine the client-
     * specified key with the STS-generated key and use this combined key as the proof token. The WS-Trust response
     * should include the STS key to allow reconstruction of the combined key and the algorithm used to combine the keys.
     * </p>
     * 
     * @throws Exception
     *            if an error occurs while running the test.
     */
    @Test
    public void testIssueSAML20WithCombinedSymmetricKey() throws Exception
    {
       // create a WS-Trust request for a SAML assertion.
       RequestSecurityToken request = new RequestSecurityToken();
       request.setRequestType(URI.create(WSTrustConstants.ISSUE_REQUEST));
       request.setAppliesTo(WSTrustUtil.createAppliesTo("http://services.testcorp.org/provider1"));
 
       // add a symmetric key type to the request.
       request.setKeyType(URI.create(WSTrustConstants.KEY_TYPE_SYMMETRIC));
 
       // create a 128-bit (16 bytes) random client secret.
       byte[] clientSecret = WSTrustUtil.createRandomSecret(16);
       BinarySecretType clientBinarySecret = new BinarySecretType();
       clientBinarySecret.setType(WSTrustConstants.BS_TYPE_NONCE);
       clientBinarySecret.setValue(Base64.encodeBytes(clientSecret).getBytes());
 
       // set the client secret in the client entropy.
       EntropyType clientEntropy = new EntropyType();
       clientEntropy.getAny().add(clientBinarySecret);
       request.setEntropy(clientEntropy);
 
       // dispatch the request and get the issued assertion.
       Element assertionElement = client.issueToken(request);
       Assert.assertNotNull("Invalid null assertion element", assertionElement);
 
       // the usage of a key as proof-of-possession token results in the holder-of-key confirmation method being used.
       AssertionType assertion = this.validateSAML20Assertion(assertionElement, "admin",
             SAMLUtil.SAML2_HOLDER_OF_KEY_URI);
       // validate the holder of key contents.
       SubjectConfirmationType subjConfirmation = (SubjectConfirmationType) assertion.getSubject().getConfirmation()
             .get(0);
       this.validateHolderOfKeyContents(subjConfirmation, WSTrustConstants.KEY_TYPE_SYMMETRIC, null, false);
 
       // TODO: client API must allow access to the WS-Trust response for retrieval of the server entropy and algorithm.
    }
 
    /**
     * <p>
     * This test requests a SAMLV2.0 assertion and sends a X.509 certificate to be used as the proof-of-possession token.
     * The STS must include the specified certificate in the SAML subject confirmation.
     * </p>
     * 
     * @throws Exception
     *            if an error occurs while running the test.
     */
    @Test
    public void testIssueSAML20WithCertificate() throws Exception
    {
       // create a simple token request with a public key type.
       RequestSecurityToken request = new RequestSecurityToken();
       request.setRequestType(URI.create(WSTrustConstants.ISSUE_REQUEST));
       request.setAppliesTo(WSTrustUtil.createAppliesTo("http://services.testcorp.org/provider1"));
       request.setKeyType(URI.create(WSTrustConstants.KEY_TYPE_PUBLIC));
 
       // include a UseKey section that specifies the certificate in the request.
       UseKeyType useKey = new UseKeyType();
       useKey.setAny(Base64.encodeBytes(certificate.getEncoded()).getBytes());
       request.setUseKey(useKey);
 
       // dispatch the request and get the issued assertion.
       Element assertionElement = client.issueToken(request);
       Assert.assertNotNull("Invalid null assertion element", assertionElement);
 
       AssertionType assertion = this.validateSAML20Assertion(assertionElement, "admin",
             SAMLUtil.SAML2_HOLDER_OF_KEY_URI);
       // validate the holder of key contents.
       SubjectConfirmationType subjConfirmation = (SubjectConfirmationType) assertion.getSubject().getConfirmation()
             .get(0);
       this.validateHolderOfKeyContents(subjConfirmation, WSTrustConstants.KEY_TYPE_PUBLIC, certificate, false);
    }
 
    /**
     * <p>
     * This test requests a SAMLV2.0 assertion and sends a public key to be used as the proof-of-possession token. The
     * STS must include the specified public key in the SAML subject confirmation.
     * </p>
     * 
     * @throws Exception
     *            if an error occurs while running the test.
     */
    @Test
    public void testIssueSAML20WithPublicKey() throws Exception
    {
       // create a simple token request with a public key type.
       RequestSecurityToken request = new RequestSecurityToken();
       request.setRequestType(URI.create(WSTrustConstants.ISSUE_REQUEST));
       request.setAppliesTo(WSTrustUtil.createAppliesTo("http://services.testcorp.org/provider1"));
       request.setKeyType(URI.create(WSTrustConstants.KEY_TYPE_PUBLIC));
 
       // include a UseKey section that sets the public key in the request.
       KeyValueType keyValue = WSTrustUtil.createKeyValue(certificate.getPublicKey());
       UseKeyType useKey = new UseKeyType();
       useKey.setAny(keyValue);
       request.setUseKey(useKey);
 
       // dispatch the request and get the issued assertion.
       Element assertionElement = client.issueToken(request);
       Assert.assertNotNull("Invalid null assertion element", assertionElement);
 
       AssertionType assertion = this.validateSAML20Assertion(assertionElement, "admin",
             SAMLUtil.SAML2_HOLDER_OF_KEY_URI);
       // validate the holder of key contents.
       SubjectConfirmationType subjConfirmation = (SubjectConfirmationType) assertion.getSubject().getConfirmation()
             .get(0);
       this.validateHolderOfKeyContents(subjConfirmation, WSTrustConstants.KEY_TYPE_PUBLIC, certificate, true);
    }
 
    /**
     * <p>
     * This test case first generates a SAMLV2.0 assertion and then sends a WS-Trust renew message to the STS to get the
     * assertion renewed (i.e. get a new assertion with an updated lifetime).
     * </p>
     * 
     * @throws Exception
     *            if an error occurs while running the test.
     */
    @Test
    public void testRenewSAML20() throws Exception
    {
       // issue a simple SAML assertion.
       Element assertionElement = client.issueToken(SAMLUtil.SAML2_TOKEN_TYPE);
       Assert.assertNotNull("Invalid null assertion element", assertionElement);
       // validate the contents of the original assertion.
       AssertionType originalAssertion = this.validateSAML20Assertion(assertionElement, "admin",
             SAMLUtil.SAML2_BEARER_URI);
 
       // now use the client API to renew the assertion.
       Element renewedAssertionElement = client.renewToken(SAMLUtil.SAML2_TOKEN_TYPE, assertionElement);
       Assert.assertNotNull("Invalid null assertion element", assertionElement);
       // validate the contents of the renewed assertion.
       AssertionType renewedAssertion = this.validateSAML20Assertion(renewedAssertionElement, "admin",
             SAMLUtil.SAML2_BEARER_URI);
 
       // assertions should have different ids and lifetimes.
       Assert.assertFalse("Renewed assertion should have a unique id", originalAssertion.getID().equals(
             renewedAssertion.getID()));
       Assert.assertEquals(DatatypeConstants.LESSER, originalAssertion.getConditions().getNotBefore().compare(
             renewedAssertion.getConditions().getNotBefore()));
       Assert.assertEquals(DatatypeConstants.LESSER, originalAssertion.getConditions().getNotOnOrAfter().compare(
             renewedAssertion.getConditions().getNotOnOrAfter()));
    }
 
    /**
     * <p>
     * This test case first generates a SAMLV2.0 assertion and then sends a WS-Trust validate message to the STS to get
     * the assertion validated, checking the validation results.
     * </p>
     * 
     * @throws Exception
     *            if an error occurs while running the test.
     */
    @Test
    public void testValidateSAML20() throws Exception
    {
       // issue a simple SAML assertion.
       Element assertionElement = client.issueToken(SAMLUtil.SAML2_TOKEN_TYPE);
       Assert.assertNotNull("Invalid null assertion element", assertionElement);
 
       // now use the client API to have the assertion validated by the STS.
       boolean isValid = client.validateToken(assertionElement);
       Assert.assertTrue("Found unexpected invalid assertion", isValid);
 
       // now let's temper the SAML assertion and try to validate it again.
       assertionElement.getFirstChild().getFirstChild().setNodeValue("Tempered Issuer");
       isValid = client.validateToken(assertionElement);
       Assert.assertFalse("The assertion should be invalid", isValid);
    }
 
    /**
     * <p>
     * This test case first generates a SAMLV2.0 assertion and then sends a WS-Trust cancel message to the STS to cancel
     * the assertion. A canceled assertion cannot be renewed or considered valid anymore.
     * </p>
     * 
     * @throws Exception
     *            if an error occurs while running the test.
     */
    @Test
    public void testCancelSAML20() throws Exception
    {
       // issue a simple SAML assertion.
       Element assertionElement = client.issueToken(SAMLUtil.SAML2_TOKEN_TYPE);
       Assert.assertNotNull("Invalid null assertion element", assertionElement);
 
       // before being canceled, the assertion shold be considered valid by the STS.
       Assert.assertTrue("Found unexpected invalid assertion", client.validateToken(assertionElement));
 
       // now use the client API to have the assertion canceled by the STS.
       boolean canceled = client.cancelToken(assertionElement);
       Assert.assertTrue(canceled);
 
       // now that the assertion has been canceled, it should be considered invalid by the STS.
       Assert.assertFalse("The assertion should be invalid", client.validateToken(assertionElement));
 
       // trying to renew an invalid assertion should result in an exception being thrown.
       try
       {
          client.renewToken(SAMLUtil.SAML2_TOKEN_TYPE, assertionElement);
          Assert.fail("An exception should have been raised by the security token service");
       }
       catch (WebServiceException we)
       {
         /*Assert.assertEquals("Unexpected exception message", "Exception in handling token request: Assertion with id "
               + assertionElement.getAttribute("ID") + " has been canceled and cannot be renewed", we.getMessage());*/
       }
    }
 
    /**
     * <p>
     * This test tries to request a token of an unknown type, checking if an exception is correctly thrown by the
     * security token service.
     * </p>
     * 
     * @throws Exception
     *            if an error occurs while running the test.
     */
    @Test
    public void testIssueUnknownTokenType() throws Exception
    {
       // invoke the security token service using an unknown token type.
       try
       {
          client.issueToken("http://www.tokens.org/UnknownToken");
          Assert.fail("An exception should have been raised by the security token service");
       }
       catch (WebServiceException we)
       {
         /*Assert.assertEquals("Unexpected exception message",
               "Exception in handling token request: Unable to find a token provider for the token request", we
                     .getMessage());*/
       }
    }
 
    /**
     * <p>
     * Validates the contents of the specified SAML 2.0 assertion.
     * </p>
     * 
     * @param assertionElement
     *           the SAML 2.0 assertion to be validated.
     * @param assertionPrincipal
     *           the principal that is expected to be seen in the assertion subject.
     * @param confirmationMethod
     *           the expected confirmation method.
     * @return The SAML assertion JAXB representation. This object can be used by the test methods to perform extra
     *         validations depending on the scenario being tested.
     * @throws Exception
     *            if an error occurs while validating the assertion.
     */
    private AssertionType validateSAML20Assertion(Element assertionElement, String assertionPrincipal,
          String confirmationMethod) throws Exception
    {
       // unmarshall the SAMLV2.0 assertion.
       AssertionType assertion = SAMLUtil.fromElement(assertionElement);
 
       // validate the assertion issuer.
       Assert.assertNotNull("Invalid null assertion ID", assertion.getID());
       Assert.assertNotNull("Unexpected null assertion issuer", assertion.getIssuer());
       Assert.assertEquals("Unexpected assertion issuer name", "PicketLinkSTS", assertion.getIssuer().getValue());
 
       // validate the assertion subject.
       Assert.assertNotNull("Unexpected null subject", assertion.getSubject());
 
       SubjectType subject = assertion.getSubject();
       NameIDType nameID = (NameIDType) subject.getSubType().getBaseID();
 
       Assert.assertEquals("Unexpected name id qualifier", "urn:picketlink:identity-federation", nameID
             .getNameQualifier());
       Assert.assertEquals("Unexpected name id value", assertionPrincipal, nameID.getValue());
       SubjectConfirmationType subjType = (SubjectConfirmationType) subject.getConfirmation().get(0);
       Assert.assertEquals("Unexpected confirmation method", confirmationMethod, subjType.getMethod());
 
       // validate the assertion conditions.
       Assert.assertNotNull("Unexpected null conditions", assertion.getConditions());
       Assert.assertNotNull(assertion.getConditions().getNotBefore());
       Assert.assertNotNull(assertion.getConditions().getNotOnOrAfter());
 
       // verify if the assertion has been signed.
       Assert.assertNotNull("Assertion should have been signed", assertion.getSignature());
 
       return assertion;
    }
 
    /**
     * <p>
     * Validates the contents of the specified {@code SubjectConfirmationType} when the {@code HOLDER_OF_KEY}
     * confirmation method has been used.
     * </p>
     * 
     * @param subjectConfirmation
     *           the {@code SubjectConfirmationType} to be validated.
     * @param keyType
     *           the type of the proof-of-possession key (Symmetric or Public).
     * @param certificate
     *           the certificate used in the Public Key scenarios.
     * @param usePublicKey
     *           {@code true} if the certificate's Public Key was used as the proof-of-possession token; {@code false}
     *           otherwise.
     * @throws Exception
     *            if an error occurs while performing the validation.
     */
    private void validateHolderOfKeyContents(SubjectConfirmationType subjectConfirmation, String keyType,
          Certificate certificate, boolean usePublicKey) throws Exception
    {
       SubjectConfirmationDataType subjConfirmationDataType = subjectConfirmation.getSubjectConfirmationData();
       Assert.assertNotNull("Unexpected null subject confirmation data", subjConfirmationDataType);
 
       KeyInfoType keyInfo = (KeyInfoType) subjConfirmationDataType.getAnyType();
       Assert.assertEquals("Unexpected key info content size", 1, keyInfo.getContent().size());
 
       // if the key is a symmetric key, the KeyInfo should contain an encrypted element.
       if (WSTrustConstants.KEY_TYPE_SYMMETRIC.equals(keyType))
       {
          Element encKeyElement = (Element) keyInfo.getContent().get(0);
          Assert.assertEquals("Unexpected key info content type", WSTrustConstants.XMLEnc.ENCRYPTED_KEY, encKeyElement
                .getLocalName());
       }
       // if the key is public, KeyInfo should either contain an encoded certificate or an encoded public key.
       else if (WSTrustConstants.KEY_TYPE_PUBLIC.equals(keyType))
       {
          // if the public key has been used as proof, we should be able to retrieve it from KeyValueType.
          if (usePublicKey == true)
          {
             KeyValueType keyValue = (KeyValueType) keyInfo.getContent().get(0);
             List<Object> keyValueContent = keyValue.getContent();
             Assert.assertEquals("Unexpected key value content size", 1, keyValueContent.size());
            Assert.assertEquals("Unexpected key value content type", RSAKeyValueType.class, keyValueContent.get(0).getClass());
             RSAKeyValueType rsaKeyValue = (RSAKeyValueType) keyValueContent.get(0);
 
             // reconstruct the public key and check if it matches the public key of the provided certificate.
             BigInteger modulus = new BigInteger(1, Base64.decode(new String(rsaKeyValue.getModulus())));
             BigInteger exponent = new BigInteger(1, Base64.decode(new String(rsaKeyValue.getExponent())));
             KeyFactory factory = KeyFactory.getInstance("RSA");
             RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
             RSAPublicKey genKey = (RSAPublicKey) factory.generatePublic(spec);
             Assert.assertEquals("Invalid public key", certificate.getPublicKey(), genKey);
          }
          // if the whole certificate was used as proof, we should be able to retrieve it from X509DataType.
          else
          {
             X509DataType x509Data = (X509DataType) keyInfo.getContent().get(0);
             Assert.assertEquals("Unexpected X509 data content size", 1, x509Data.getDataObjects().size());
             Object content = x509Data.getDataObjects().get(0);
             Assert.assertTrue("Unexpected X509 data content type", content instanceof X509CertificateType);
             byte[] encodedCertificate = ((X509CertificateType) content).getEncodedCertificate();
 
             // reconstruct the certificate and check if it matches the provided certificate.
             ByteArrayInputStream byteInputStream = new ByteArrayInputStream(Base64.decode(encodedCertificate, 0,
                   encodedCertificate.length));
             Assert.assertEquals("Invalid certificate in key info", certificate, CertificateFactory.getInstance("X.509")
                   .generateCertificate(byteInputStream));
          }
       }
    }
 }
