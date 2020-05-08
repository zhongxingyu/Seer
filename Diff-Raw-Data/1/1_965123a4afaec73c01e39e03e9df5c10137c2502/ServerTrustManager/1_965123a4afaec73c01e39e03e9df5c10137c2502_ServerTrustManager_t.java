 /**
  * $RCSfile$
  * $Revision: $
  * $Date: $
  *
  * Copyright 2003-2005 Jive Software.
  *
  * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.jivesoftware.smack;
 
 import javax.net.ssl.X509TrustManager;
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.security.*;
 import java.security.cert.Certificate;
 import java.security.cert.CertificateException;
 import java.security.cert.CertificateParsingException;
 import java.security.cert.X509Certificate;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Trust manager that checks all certificates presented by the server. This class
  * is used during TLS negotiation. It is possible to disable/enable some or all checkings
  * by configuring the {@link ConnectionConfiguration}. The truststore file that contains
  * knows and trusted CA root certificates can also be configure in {@link ConnectionConfiguration}.
  *
  * @author Gaston Dombiak
  */
 class ServerTrustManager implements X509TrustManager {
 
     private static Pattern cnPattern = Pattern.compile("(?i)(cn=)([^,]*)");
 
     private ConnectionConfiguration configuration;
 
     /**
      * Holds the domain of the remote server we are trying to connect
      */
     private String server;
     private KeyStore trustStore;
 
     public ServerTrustManager(String server, ConnectionConfiguration configuration) {
         this.configuration = configuration;
         this.server = server;
 
         try {
             trustStore = getKeyStore(configuration.getTruststorePath(), configuration.getTruststoreType(), configuration.getTruststorePassword());
         }
        catch (RuntimeException e) { throw e; } // don't catch unchecked exceptions below
         catch (Exception e) {
             e.printStackTrace();
             // Disable root CA checking
             configuration.setVerifyRootCAEnabled(false);
         }
     }
 
     private static InputStream getTruststoreStream(String path) throws IOException {
         // If an explicit path was specified, only use it.
         if(path != null)
             return new FileInputStream(path);
 
         // If an explicit root certificate path isn't specified, search for one
         // using the paths described here:
         // http://download.oracle.com/javase/1,5.0/docs/guide/security/jsse/JSSERefGuide.html
         String javaHome = System.getProperty("java.home");
         String[] defaultTruststorePaths = {
                 System.getProperty("javax.net.ssl.trustStore"),
                 javaHome + "/lib/security/jssecacerts",
                 javaHome + "/lib/security/cacerts"
         };
 
         for(String candidate: Arrays.asList(defaultTruststorePaths)) {
             try {
                 return new FileInputStream(candidate);
             } catch(IOException e) {
                 // Ignore and keep searching.
             }
         }
 
         throw new IOException("No truststore path located");
     }
 
     private static KeyStore getKeyStore(String path, String type, String password)
     throws Exception
     {
         KeyStore trustStore;
         InputStream in = null;
         try {
             in = new BufferedInputStream(getTruststoreStream(path));
             trustStore = KeyStore.getInstance(type);
             trustStore.load(in, password != null? password.toCharArray():null);
         } finally {
             if (in != null) {
                 try {
                     in.close();
                 }
                 catch (IOException ioe) {
                     // Ignore.
                 }
             }
         }
 
         return trustStore;
     }
 
     /**
      * This exception contains details about a certificate verification failure.
      */
     public static class CertificateExceptionDetail extends CertificateException {
         private static final long serialVersionUID = 9002117483237932715L;
         X509Certificate[] certificates;
 
         CertificateExceptionDetail(X509Certificate[] certificates, Throwable t) {
             super(t);
             this.certificates = certificates;
         }
         CertificateExceptionDetail(X509Certificate[] certificates, String message) {
             super(message);
             this.certificates = certificates;
         }
         CertificateExceptionDetail(X509Certificate[] certificates, String message, Throwable t) {
             super(message, t);
             this.certificates = certificates;
         }
 
     };
 
     public X509Certificate[] getAcceptedIssuers() {
         return new X509Certificate[0];
     }
 
     public void checkClientTrusted(X509Certificate[] arg0, String arg1)
             throws CertificateException {
     }
 
     public void checkServerTrusted(X509Certificate[] x509Certificates, String authType)
     throws CertificateException {
         checkCertificates(x509Certificates);
     }
 
     public void checkCertificates(X509Certificate[] x509Certificates)
             throws CertificateExceptionDetail {
 
         int nSize = x509Certificates.length;
 
         List<String> peerIdentities = getPeerIdentity(x509Certificates[0]);
 
         if (configuration.isVerifyChainEnabled()) {
             // Working down the chain, for every certificate in the chain,
             // verify that the subject of the certificate is the issuer of the
             // next certificate in the chain.
             Principal principalLast = null;
             for (int i = nSize -1; i >= 0 ; i--) {
                 X509Certificate x509certificate = x509Certificates[i];
                 Principal principalIssuer = x509certificate.getIssuerDN();
                 Principal principalSubject = x509certificate.getSubjectDN();
                 if (principalLast != null) {
                     if (principalIssuer.equals(principalLast)) {
                         try {
                             PublicKey publickey =
                                     x509Certificates[i + 1].getPublicKey();
                             x509Certificates[i].verify(publickey);
                         }
                         catch (GeneralSecurityException generalsecurityexception) {
                             throw new CertificateExceptionDetail(x509Certificates,
                                     "signature verification failed of " + peerIdentities.get(i));
                         }
                     }
                     else {
                         throw new CertificateExceptionDetail(x509Certificates,
                                 "subject/issuer verification failed of " + peerIdentities.get(i));
                     }
                 }
                 principalLast = principalSubject;
             }
         }
 
         if (configuration.isVerifyRootCAEnabled()) {
             // Verify that the the last certificate in the chain was issued
             // by a third-party that the client trusts.
             boolean trusted = false;
             try {
                 trusted = trustStore.getCertificateAlias(x509Certificates[nSize - 1]) != null;
                 if (!trusted && nSize == 1 && configuration.isSelfSignedCertificateEnabled())
                 {
                     System.out.println("Accepting self-signed certificate of remote server: " +
                             peerIdentities);
                     trusted = true;
                 }
             }
             catch (KeyStoreException e) {
                 e.printStackTrace();
             }
             if (!trusted) {
                 throw new CertificateExceptionDetail(x509Certificates,
                         "Certificate \"" + getPeerIdentity(x509Certificates[0]) + "\" is self-signed");
             }
         }
 
         if (configuration.isNotMatchingDomainCheckEnabled()) {
             // Verify that the first certificate in the chain corresponds to
             // the server we desire to authenticate.
             // Check if the certificate uses a wildcard indicating that subdomains are valid
             if (peerIdentities.size() == 1 && peerIdentities.get(0).startsWith("*.")) {
                 // Remove the wildcard
                 String peerIdentity = peerIdentities.get(0).replace("*.", "");
                 // Check if the requested subdomain matches the certified domain
                 if (!server.endsWith(peerIdentity)) {
                     throw new CertificateExceptionDetail(x509Certificates, "target verification failed of " + peerIdentities);
                 }
             }
             else if (!peerIdentities.contains(server)) {
                 throw new CertificateExceptionDetail(x509Certificates, "target verification failed of " + peerIdentities);
             }
         }
 
         if (configuration.isExpiredCertificatesCheckEnabled()) {
             // For every certificate in the chain, verify that the certificate
             // is valid at the current time.
             Date date = new Date();
             for (int i = 0; i < nSize; i++) {
                 try {
                     x509Certificates[i].checkValidity(date);
                 }
                 catch (GeneralSecurityException generalsecurityexception) {
                     throw new CertificateExceptionDetail(x509Certificates, generalsecurityexception);
                 }
             }
         }
 
     }
 
     /**
      * Verify a certificate chain.  On success, return normally.  On verification failure,
      * throws {@link CertificateExceptionDetail}.
      */
     public void checkCertificates(Certificate[] certificates) throws CertificateExceptionDetail {
         X509Certificate[] x509Certificates;
         try {
             x509Certificates = new X509Certificate[certificates.length];
 
             for(int i = 0; i < certificates.length; ++i) {
                 X509Certificate cert = (X509Certificate) certificates[i];
                 x509Certificates[i] = cert;
             }
         } catch(ClassCastException e) {
             // One of the certificates wasn't an X509Certificate.  Assume the connection
             // is insecure.
             throw new CertificateExceptionDetail(new X509Certificate[]{}, "Received a non-X509 certificate", e);
         }
 
         checkCertificates(x509Certificates);
     }
 
     /**
      * Returns the identity of the remote server as defined in the specified certificate. The
      * identity is defined in the subjectDN of the certificate and it can also be defined in
      * the subjectAltName extension of type "xmpp". When the extension is being used then the
      * identity defined in the extension in going to be returned. Otherwise, the value stored in
      * the subjectDN is returned.
      *
      * @param x509Certificate the certificate the holds the identity of the remote server.
      * @return the identity of the remote server as defined in the specified certificate.
      */
     public static List<String> getPeerIdentity(X509Certificate x509Certificate) {
         // Look the identity in the subjectAltName extension if available
         List<String> names = getSubjectAlternativeNames(x509Certificate);
         if (names.isEmpty()) {
             String name = x509Certificate.getSubjectDN().getName();
             Matcher matcher = cnPattern.matcher(name);
             if (matcher.find()) {
                 name = matcher.group(2);
             }
             // Create an array with the unique identity
             names = new ArrayList<String>();
             names.add(name);
         }
         return names;
     }
 
     /**
      * Returns the JID representation of an XMPP entity contained as a SubjectAltName extension
      * in the certificate. If none was found then return <tt>null</tt>.
      *
      * @param certificate the certificate presented by the remote entity.
      * @return the JID representation of an XMPP entity contained as a SubjectAltName extension
      *         in the certificate. If none was found then return <tt>null</tt>.
      */
     private static List<String> getSubjectAlternativeNames(X509Certificate certificate) {
         List<String> identities = new ArrayList<String>();
         try {
             Collection<List<?>> altNames = certificate.getSubjectAlternativeNames();
             // Check that the certificate includes the SubjectAltName extension
             if (altNames == null) {
                 return Collections.emptyList();
             }
             // Use the type OtherName to search for the certified server name
             /*for (List item : altNames) {
                 Integer type = (Integer) item.get(0);
                 if (type == 0) {
                     // Type OtherName found so return the associated value
                     try {
                         // Value is encoded using ASN.1 so decode it to get the server's identity
                         ASN1InputStream decoder = new ASN1InputStream((byte[]) item.toArray()[1]);
                         DEREncodable encoded = decoder.readObject();
                         encoded = ((DERSequence) encoded).getObjectAt(1);
                         encoded = ((DERTaggedObject) encoded).getObject();
                         encoded = ((DERTaggedObject) encoded).getObject();
                         String identity = ((DERUTF8String) encoded).getString();
                         // Add the decoded server name to the list of identities
                         identities.add(identity);
                     }
                     catch (UnsupportedEncodingException e) {
                         // Ignore
                     }
                     catch (IOException e) {
                         // Ignore
                     }
                     catch (Exception e) {
                         e.printStackTrace();
                     }
                 }
                 // Other types are not good for XMPP so ignore them
                 System.out.println("SubjectAltName of invalid type found: " + certificate);
             }*/
         }
         catch (CertificateParsingException e) {
             e.printStackTrace();
         }
         return identities;
     }
 
 }
