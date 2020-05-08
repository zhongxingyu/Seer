 /**-----------------------------------------------------------------------
 
  Copyright (c) 2009, The University of Manchester, United Kingdom.
  All rights reserved.
 
  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:
 
  * Redistributions of source code must retain the above copyright notice, 
  this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright 
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.
  * Neither the name of the The University of Manchester nor the names of 
  its contributors may be used to endorse or promote products derived
  from this software without specific prior written permission.
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  POSSIBILITY OF SUCH DAMAGE.
 
  Author........: Bruno Harbulot
 
  -----------------------------------------------------------------------*/
 package net.java.dev.sommer.foafssl.login;
 
 import net.java.dev.sommer.foafssl.keygen.CertCreator;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.bouncycastle.util.encoders.Base64;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.mortbay.jetty.servlet.ServletHolder;
 import org.mortbay.jetty.testing.HttpTester;
 import org.mortbay.jetty.testing.ServletTester;
 import org.restlet.data.Form;
 import org.restlet.data.Parameter;
 import org.restlet.data.Reference;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NameNotFoundException;
 import javax.servlet.*;
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigInteger;
 import java.net.*;
 import java.security.*;
 import java.security.cert.X509Certificate;
 import java.security.interfaces.RSAPublicKey;
 import java.security.spec.KeySpec;
 import java.security.spec.RSAPublicKeySpec;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Properties;
 
 import static org.junit.Assert.*;
 
 /**
  * @author Bruno Harbulot (Bruno.Harbulot@manchester.ac.uk)
  */
 public class ShortRedirectIdpServletTest {
     public final static String CERTIFICATES_DIRECTORY = "org/jsslutils/certificates/";
     public final static String KEYSTORE_PASSWORD_STRING = "testtest";
     public final static char[] KEYSTORE_PASSWORD = KEYSTORE_PASSWORD_STRING.toCharArray();
 
     @SuppressWarnings("unused")
     private static final String TEST_IDP_KEYNAME = "http://idp.example.org/idp/#pubkey";
     private static final String TEST_IDP_URI = "http://idp.example.org/idp/";
     private static final String TEST_SP_URI = "http://sp.example.com/sp/";
 
     public static final String TEST_FOAF_FILENAME = "dummy-foaf.rdf.xml";
     public static final String TEST_CERT_FILENAME = "dummy-foafsslcert.pem";
     public static final String TEST_WEBID = "http://foaf.example.net/bruno#me";
 
     private ServletTester idpServletTester;
 
     public ShortRedirectIdpServletTest() throws InvalidKeyException {
     }
 
     /**
      * Loads the 'localhost' keystore from the test keystore.
      * 
      * @return test keystore.
      * @throws Exception
      */
     public KeyStore getKeyStore() throws Exception {
         KeyStore ks = KeyStore.getInstance("PKCS12");
         InputStream ksis = ClassLoader.getSystemResourceAsStream(CERTIFICATES_DIRECTORY
                 + "localhost.p12");
         ks.load(ksis, KEYSTORE_PASSWORD);
         ksis.close();
         return ks;
     }
 
     /**
      * Returns the public key matching the private key used to sign the
      * assertion.
      * 
      * @return public key matching the private key used to sign the assertion.
      * @throws Exception
      */
     public PublicKey getPublicKey() throws Exception {
         KeyStore keyStore = getKeyStore();
         Enumeration<String> aliases = keyStore.aliases();
         while (aliases.hasMoreElements()) {
             String alias = aliases.nextElement();
             if (keyStore.isKeyEntry(alias)) {
                 return keyStore.getCertificate(alias).getPublicKey();
             }
         }
         return null;
     }
 
     /**
      * Sets up the servlet tester, loads the keystore and passes the appropriate
      * parameters.
      * 
      * @throws Exception
      */
     @Before
     public void setUp() throws Exception {
         /*
          * Passes the keystore via JNDI to the servlet.
          */
         Context ctx = null;
         try {
             Properties props = new Properties();
             props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                     "org.mortbay.naming.InitialContextFactory");
             ctx = (Context) new InitialContext(props).lookup("java:comp");
             try {
                 ctx = (Context) ctx.lookup("env");
             } catch (NameNotFoundException e) {
                 ctx = ctx.createSubcontext("env");
             }
             try {
                 ctx = (Context) ctx.lookup("foafsslidp");
             } catch (NameNotFoundException e) {
                 ctx = ctx.createSubcontext("foafsslidp");
             }
             ctx.rebind("signingKeyStore", getKeyStore());
             ctx.rebind("signingKeyPasswordArray", KEYSTORE_PASSWORD);
         } finally {
             if (ctx != null) {
                 ctx.close();
             }
         }
 
         /*
          * Creates a mock URLConnection not to make outside connections to
          * de-reference the FOAF file for the tests.
          */
         URLStreamHandlerFactory mockStreamHandlerFactory = new URLStreamHandlerFactory() {
             public URLStreamHandler createURLStreamHandler(String protocol) {
                 if ("http".equals(protocol) || "https".equals(protocol)) {
                     return new URLStreamHandler() {
                         @Override
                         protected URLConnection openConnection(URL u) throws IOException {
                             return new HttpURLConnection(u) {
                                 @Override
                                 public void disconnect() {
                                 }
 
                                 @Override
                                 public boolean usingProxy() {
                                     return false;
                                 }
 
                                 @Override
                                 public void connect() throws IOException {
                                 }
 
                                 @Override
                                 public String getContentType() {
                                     return "application/rdf+xml";
                                 }
 
                                 @Override
                                 public InputStream getInputStream() throws IOException {
                                     return ShortRedirectIdpServletTest.class
                                             .getResourceAsStream(TEST_FOAF_FILENAME);
                                 }
                             };
                         }
                     };
                 }
                 return null;
             }
         };
         try {
             URL.setURLStreamHandlerFactory(mockStreamHandlerFactory);
         } catch (Throwable e) {
         }
 
         /*
          * Creates the servlet tester.
          */
         idpServletTester = new ServletTester();
         idpServletTester.setContextPath("/idp");
         @SuppressWarnings("unused")
         ServletHolder servletHolder = idpServletTester.addServlet(ShortRedirectIdpServlet.class,
                 "/*");
         idpServletTester.start();
     }
 
     @Test
     public void testRequest() throws Exception {
         /*
          * Creates a fake simple authn request.
          */
         Reference authnReqResourceRef = new Reference(TEST_IDP_URI);
         authnReqResourceRef.addQueryParameter(ShortRedirectIdpServlet.AUTHREQISSUER_PARAMNAME,
                 TEST_SP_URI);
 
         /*
          * Sets up the request in the Jetty tester. The URL to which to send the
          * simple authn request is modelled by authnReqResourceRef.
          */
         HttpTester request = new HttpTester();
         HttpTester response = new HttpTester();
         request.setHeader("Host", authnReqResourceRef.getHostDomain());
        request.setMethod("POST");
         String authReqUrlQueryPart = authnReqResourceRef.getQuery();
         request.setURI(authnReqResourceRef.getPath()
                 + (authReqUrlQueryPart != null ? "?" + authReqUrlQueryPart : ""));
 
         /*
          * Fakes the presence of a client certificate.
          */
         idpServletTester.addFilter(FakeClientCertInsertionFilter.class, "/*", 0);
 
         /*
          * Performs the request.
          */
         response.parse(idpServletTester.getResponses(request.generate()));
 
         System.out.println("Request URI: " + authnReqResourceRef.toString());
         System.out.println("Response status: " + response.getStatus());
         String location = response.getHeader("Location");
         System.out.println("Response Location header: " + location);
         System.out.println("Response Location header length: " + location.length());
         System.out.println();
 
         /*
          * Process the response.
          */
         Reference authnRespResourceRef = new Reference(location);
         Form authnRespResourceRefQueryForm = authnRespResourceRef.getQueryAsForm();
 
         /*
          * Tries to verify the signature, if present.
          */
         String authnUriParam = authnRespResourceRefQueryForm
                 .getFirstValue(ShortRedirectIdpServlet.WEBID_PARAMNAME);
         String authnDateTimeParam = authnRespResourceRefQueryForm
                 .getFirstValue(ShortRedirectIdpServlet.TIMESTAMP_PARAMNAME);
         Parameter signatureParam = authnRespResourceRefQueryForm
                 .getFirst(ShortRedirectIdpServlet.SIGNATURE_PARAMNAME);
 
         assertNotNull("Signature?", signatureParam);
 
         authnRespResourceRefQueryForm.remove(signatureParam);
 
         authnRespResourceRef.setQuery(null);
         for (Parameter param : authnRespResourceRefQueryForm) {
             authnRespResourceRef.addQueryParameter(param.getName(), param.getValue());
         }
         String signedMessage = authnRespResourceRef.toString();
         System.out.println("SignedMessage: " + signedMessage);
 
         byte[] signatureBytes = Base64.decode(signatureParam.getValue());
         String sigAlg = null;
         if ("RSA".equals(getPublicKey().getAlgorithm())) {
             sigAlg = "SHA1withRSA";
         } else if ("DSA".equals(getPublicKey().getAlgorithm())) {
             sigAlg = "SHA1withDSA";
         } else {
             fail("Unsupported signature algorithm.");
         }
         Signature signature = Signature.getInstance(sigAlg);
         signature.initVerify(getPublicKey());
         signature.update(signedMessage.getBytes());
         assertTrue("Signature verified?", signature.verify(signatureBytes));
 
         assertEquals("ID verified?", TEST_WEBID, authnUriParam);
 
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
         Date authnDate = dateFormat.parse(authnDateTimeParam);
         assertTrue("Authn date in the past?", authnDate.before(new Date()));
         assertTrue("Authn date no older than 5s?", authnDate.after(new Date(System
                 .currentTimeMillis() - 5000)));
     }
 
     @After
     public void tearDown() throws Exception {
         if (idpServletTester != null) {
             idpServletTester.stop();
         }
     }
 
     /**
      * This filter is used for the test: it fakes the presence of a client
      * certificate in the request.
      * 
      * @author Bruno Harbulot.
      */
     public static class FakeClientCertInsertionFilter implements Filter {
         static {
             Security.addProvider(new BouncyCastleProvider());
             try {
                 KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                 KeySpec keySpec = new RSAPublicKeySpec(
                         new BigInteger(
                                 "a4615390921b3d28b05b409280dbe6f34283a9fed892b670e111aadd6c951f58b101bf1c1fd7b5bb5"
                                         + "493a9fa269ff1e3814747a24098c3e0b29b6d5a21eec655e1a60873803a2b7e9a158f25239c04608b0a32ed9c26ce6c"
                                         + "1c2741426204b4351d02633d5c9a6bf7e387cd514d93445b37b4bb3ed85a114739c82e5e769ec277",
                                 16), new BigInteger("65537"));
                 goodKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
             } catch (Exception e) {
                 throw new Error(e);
             }
 
         }
 
         final static RSAPublicKey goodKey;
 
         private X509Certificate x509Certificate;
 
       @Override
         public void destroy() {
         }
 
       @Override
         public void doFilter(ServletRequest request, ServletResponse response, FilterChain next)
                 throws IOException, ServletException {
             request.setAttribute("javax.servlet.request.X509Certificate",
                     new X509Certificate[] { x509Certificate });
             next.doFilter(request, response);
         }
 
       @Override
         public void init(FilterConfig config) throws ServletException {
             CertCreator create = null;
             try {
                 create = new CertCreator();
                 create.addDurationInHours("1");
                 create.setSubjectCommonName("TEST");
                 // avoid using the mock stream handler with commented code
                 // URL webIdDoc = X509ClaimTest.class.getResource(foaf);
                 // webIdDoc = new URL(webIdDoc.getProtocol(), "localhost",
                 // webIdDoc.getFile());
                 // URL webId = new URL(webIdDoc, "#me");
                 create.setSubjectWebID(TEST_WEBID);
                 create.setSubjectPublicKey(goodKey);
                 create.generate();
                 x509Certificate = create.getCertificate();
             } catch (InvalidKeyException e) {
                 throw new ServletException(e);
             } catch (Exception e) {
                 throw new ServletException(e);
             }
         }
     }
 }
