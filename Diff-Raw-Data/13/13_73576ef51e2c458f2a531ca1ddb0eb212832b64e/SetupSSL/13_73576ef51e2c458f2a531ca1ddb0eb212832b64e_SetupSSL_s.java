 package de.zib.gndms.common.kit.security;
 /*
  * Copyright 2008-2011 Zuse Institute Berlin (ZIB)
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 import javax.net.ssl.KeyManagerFactory;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManagerFactory;
 import java.io.*;
 import java.security.*;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 
 /**
  * @author Maik Jorra
  * @email jorra@zib.de
  * @date 05.03.12  16:52
  * @brief This class helps in producing an SSL Context.
  *
  *
  */
 public class SetupSSL {
 
     public static final String TRUST_STORE_TYPE = "JKS";
     public static final String KEY_STORE_TYPE = "PKCS12";
 
     private String keyStoreLocation;
     private String trustStoreLocation;
     private TrustManagerFactory trustManagerFactory;
     private KeyManagerFactory keyManagerFactory;
     private KeyStore keyStore;
     private KeyStore trustStore;
     
     
     public void setKeyStore( final KeyStore keyStore, final String keyPassword )
             throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException
     {
         setKeyStoreLocation(null);
         this.keyStore = keyStore;
         
         initKeyManagerFactory( keyPassword );
     }
 
 
     public void prepareKeyStore( final String keyStorePassword )
             throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException,
             UnrecoverableKeyException
     {
         if( getKeyStoreLocation().endsWith( ".pkcs12" ) || getKeyStoreLocation().endsWith( ".p12" ) )
             prepareKeyStore( keyStorePassword, "PKCS12" );
         else
             prepareKeyStore( keyStorePassword, "JKS" );
     }
 
 
     // todo add support for encypted private keys as it was in the good old days
     public void prepareKeyStore( final String keyStorePassword, final String keyStoreType )
             throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException,
             UnrecoverableKeyException
     {
         // create an empty keyStore
         if( null == keyStoreLocation ) {
             keyStore = KeyStore.getInstance( keyStoreType );
             keyStore.load( null, keyStorePassword.toCharArray() );
         }
         else {
             InputStream kis = new FileInputStream( keyStoreLocation );
             keyStore = KeyStore.getInstance( keyStoreType );
             keyStore.load( kis, keyStorePassword.toCharArray() );
         }
     }
     
     private void initKeyManagerFactory( final String keyPassword )
             throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException
     {
         keyManagerFactory = KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );
         keyManagerFactory.init( keyStore, keyPassword.toCharArray() );
     }
 
 
     public void addCertificate( PrivateKey privateKey, X509Certificate[] certs, String alias, String password )
             throws KeyStoreException
     {
         if( null == keyStore ) {
             throw new IllegalStateException( "No KeyStore set." );
         }
 
         keyStore.setKeyEntry(alias, privateKey, password.toCharArray(), certs);
     }
     
     
     public void setTrustStore( final KeyStore trustStore ) {
         setTrustStoreLocation( null );
         this.trustStore = trustStore;
     }
 
 
     public void prepareTrustStore( final String trustStorePassword )
             throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException
     {
         prepareTrustStore( trustStorePassword, TRUST_STORE_TYPE );
     }
 
 
     public void prepareTrustStore( final String trustStorePassword, final String trustStoreType )
             throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException
     {
         InputStream tis = new FileInputStream( trustStoreLocation );
 
         trustStore = KeyStore.getInstance( trustStoreType );
        trustStore.load( tis, trustStorePassword.toCharArray() );
 
         initTrustManagerFactory();
     }
     
     
     private void initTrustManagerFactory()
             throws NoSuchAlgorithmException, KeyStoreException
     {
         trustManagerFactory = TrustManagerFactory.getInstance( TrustManagerFactory.getDefaultAlgorithm() );
         trustManagerFactory.init( trustStore );
     }
 
 
     public void setupDefaultSSLContext( final String keyPassword  )
             throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException
     {
         SSLContext.setDefault( setupSSLContext( keyPassword ) );
     }
 
 
     public SSLContext setupSSLContext( final String keyPassword )
             throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
 
         initKeyManagerFactory( keyPassword );
 
         if( null == keyManagerFactory ) {
             throw new IllegalStateException( "No KeyStore set." );
         }
 
         SSLContext sslContext = SSLContext.getInstance( "TLS" );
         sslContext.init( keyManagerFactory.getKeyManagers(),
                 trustManagerFactory != null ?  trustManagerFactory.getTrustManagers() : null,
                 new SecureRandom());
         return sslContext;
     }
 
 
     public String getKeyStoreLocation() {
 
         return keyStoreLocation;
     }
 
 
     public void setKeyStoreLocation(final String keyStoreLocation) {
 
         this.keyStoreLocation = keyStoreLocation;
     }
 
 
     public void setTrustStoreLocation( final String trustStoreLocation ) {
 
         this.trustStoreLocation = trustStoreLocation;
     }
 
 
     public KeyStore getKeyStore() {
 
         return keyStore;
     }
 
 
     public KeyStore getTrustStore() {
 
         return trustStore;
     }
 }
