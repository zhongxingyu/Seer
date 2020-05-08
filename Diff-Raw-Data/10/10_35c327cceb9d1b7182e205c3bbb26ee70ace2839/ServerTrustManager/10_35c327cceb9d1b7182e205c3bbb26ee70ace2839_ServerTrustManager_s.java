 /**
  * $RCSfile$
  * $Revision: $
  * $Date: $
  *
  * Copyright 2003-2005 Jive Software.
  * Copyright 2001-2006 The Apache Software Foundation.
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
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.GeneralSecurityException;
 import java.security.KeyStore;
 import java.security.cert.CertPath;
 import java.security.cert.CertPathValidator;
 import java.security.cert.CertPathValidatorException;
 import java.security.cert.Certificate;
 import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
 import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
 import java.security.cert.CertificateParsingException;
 import java.security.cert.PKIXParameters;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.net.ssl.X509TrustManager;
 
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
     private boolean secureConnectionRequired;
 
     /**
      * @param secureConnectionRequired If true, the connection will be rejected if the certificate
      * can't be verified.  If false, the connection will be allowed, but isSecureConnection will
      * return false.
      */
     public ServerTrustManager(String server, ConnectionConfiguration configuration, boolean secureConnectionRequired)
     throws XMPPException
     {
        this.configuration = configuration;
         this.server = server;
         this.secureConnectionRequired = secureConnectionRequired;
 
         try {
             trustStore = getKeyStore(configuration.getTruststorePath(), configuration.getTruststoreType(), configuration.getTruststorePassword());
         }
         catch (RuntimeException e) { throw e; } // don't catch unchecked exceptions below
         catch (Exception e) {
             if(secureConnectionRequired)
                 throw new XMPPException("Error creating keystore", e);
 
             // If a secure connection isn't required anyway, just clear trustStore.
             trustStore = null;
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
             if(candidate == null)
                 continue;
             try {
                 return new FileInputStream(candidate);
             } catch(IOException e) {
                 // Ignore and keep searching.
             }
         }
 
         throw new IOException("No truststore path located");
     }
 
     /* Loading the keystore can take some time (almost a second) on slower systems,
      * so load it the first time we need it and then cache it.  The keystore we load
      * depends on the configuration. */
     private static class KeyStoreCacheParams {
         public String path, type, password;
         public boolean equals(Object rhs) {
             if(!(rhs instanceof KeyStoreCacheParams))
                 return false;
             KeyStoreCacheParams rhsParams = (KeyStoreCacheParams) rhs;
             return path == rhsParams.path && type == rhsParams.type && password == rhsParams.password;
         }
         public int hashCode() {
             int hash = 0;
             if(path != null)            hash += path.hashCode();
             if(type != null)            hash += type.hashCode();
             if(password != null)        hash += password.hashCode();
             return hash;
         }
     };
     private static HashMap<KeyStoreCacheParams, KeyStore> trustStoreCache = new HashMap<KeyStoreCacheParams, KeyStore>();
 
     /** Load a KeyStore of root certificates from disk, caching the result. */
     private static synchronized KeyStore getKeyStore(String path, String type, String password)
     throws Exception
     {
         KeyStoreCacheParams params = new KeyStoreCacheParams();
         params.path = path;
         params.type = type;
         params.password = password;
         KeyStore trustStore = trustStoreCache.get(params);
         if(trustStore != null)
             return trustStore;
 
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
 
         trustStoreCache.put(params, trustStore);
         return trustStore;
     }
 
     public X509Certificate[] getAcceptedIssuers() {
         return new X509Certificate[0];
     }
 
     public void checkClientTrusted(X509Certificate[] arg0, String arg1)
             throws CertificateException {
     }
 
     public void checkServerTrusted(X509Certificate[] x509Certificates, String authType)
     throws CertificateException {
         // If a secure connection isn't required, don't perform checks here.  The caller
         // can still run certificate checks separately, using SSLSession.getPeerCertificates
         // and calling checkCertificates directly.
         //
         // This is important, because if the caller only wants to display warnings on
         // insecure connections and not prevent using encryption entirely, we must not
         // throw an exception here, or getPeerCertificates will refuse to tell what the
         // certificates are.
         if(!secureConnectionRequired)
             return;
 
         checkCertificates(x509Certificates);
     }
 
     public void checkCertificates(X509Certificate[] x509Certificates)
             throws CertificateException {
         List<X509Certificate> certList = new ArrayList<X509Certificate>();
         for(int i = 0; i < x509Certificates.length; ++i)
             certList.add(x509Certificates[i]);
 
         CertificateFactory cf = CertificateFactory.getInstance("X.509");
         CertPath certPath = cf.generateCertPath(certList);
 
         List<String> peerIdentities = getPeerIdentity(x509Certificates[0]);
         if(!hostMatchesCertificate(peerIdentities, server)) {
             CertPathValidatorException e = new CertPathValidatorException("Hostname verification failed", null,
                 certPath, 0);
             throw new CertificateException(e);
         }
 
         try {
             PKIXParameters params = new PKIXParameters(trustStore);
 
             // Work around "No CRLs found for issuer" being thrown for every certificate.
             params.setRevocationEnabled(false);
 
             CertPathValidator validator = CertPathValidator.getInstance("PKIX");
             validator.validate(certPath, params);
         } catch (GeneralSecurityException e) {
             throw new CertificateException(e);
         }
     }
 
     // Parts of the following based on org.apache.http:
     static boolean hostMatchesCertificate(List<String> peerIdentities, String fqdn) {
         for(String identity: peerIdentities) {
             boolean doWildcard =
                 identity.startsWith("*.") &&
                 countDots(identity) >= 2 && // never allow *.com
                 acceptableCountryWildcard(identity) &&
                 !isIPv4Address(fqdn);
 
             if (doWildcard) {
                 // Remove the wildcard
                 String peerIdentity = peerIdentities.get(0).substring(1);
 
                 // Check if the requested subdomain matches the certified domain
                 if (fqdn.endsWith(peerIdentity) && countDots(peerIdentity) == countDots(fqdn))
                     return true;
             } else if(fqdn.equals(identity)) {
                 return true;
             }
         }
 
         return false;
     }
 
     private final static String[] BAD_COUNTRY_2LDS = {
         "ac", "co", "com", "ed", "edu", "go", "gouv", "gov", "info",
         "lg", "ne", "net", "or", "org"
     };
 
     private static boolean acceptableCountryWildcard(String cn) {
         int cnLen = cn.length();
         if(cnLen >= 7 && cnLen <= 9) {
             // Look for the '.' in the 3rd-last position:
             if(cn.charAt(cnLen - 3) == '.') {
                 // Trim off the [*.] and the [.XX].
                 String s = cn.substring(2, cnLen - 3);
                 // And test against the sorted array of bad 2lds:
                 int x = Arrays.binarySearch(BAD_COUNTRY_2LDS, s);
                 return x < 0;
             }
         }
         return true;
     }
 
     private static final Pattern IPV4_PATTERN =
         Pattern.compile(
                 "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");
 
     private static boolean isIPv4Address(final String input) {
         return IPV4_PATTERN.matcher(input).matches();
     }
 
     /**
      * Counts the number of dots "." in a string.
      * @param s string to count dots from
      * @return number of dots
      */
     public static int countDots(final String s) {
         int count = 0;
         for(int i = 0; i < s.length(); i++) {
             if(s.charAt(i) == '.')
                 count++;
         }
         return count;
     }
 
     /**
      * Verify a certificate chain.  On success, return normally.  On verification failure,
      * throws {@link CertificateExceptionDetail}.
      */
     public void checkCertificates(Certificate[] certificates) throws CertificateException {
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
             throw new CertificateException("Received a non-X509 certificate", e);
         }
 
         checkCertificates(x509Certificates);
     }
 
     /**
      * @param x509Certificate the certificate the holds the identity of the remote server.
      * @return the identity of the remote server as defined in the specified certificate.
      */
     public static List<String> getPeerIdentity(X509Certificate x509Certificate) {
         // Look the identity in the subjectAltName extension if available
         List<String> names = new Vector<String>();
         String name = x509Certificate.getSubjectDN().getName();
         Matcher matcher = cnPattern.matcher(name);
         if (matcher.find()) {
             name = matcher.group(2);
         }
         names.add(name);
 
         names.addAll(getSubjectAlternativeNames(x509Certificate));
 
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
         Collection<List<?>> altNames;
         try {
             altNames = certificate.getSubjectAlternativeNames();
         }
         catch (CertificateParsingException e) {
             e.printStackTrace();
             return Collections.emptyList();
         }
 
         // Check that the certificate includes the SubjectAltName extension
         if (altNames == null)
             return Collections.emptyList();
 
         List<String> identities = new ArrayList<String>();
         // Use the type OtherName to search for the certified server name
         for (List item: altNames) {
             Integer type = (Integer) item.get(0);
             if (type == 2) {
                 String s = (String) item.get(1);
                 identities.add(s);
             }
         }
         return identities;
     }
 
 }
