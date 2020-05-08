 /*
  * Copyright [2007] [University Corporation for Advanced Internet Development, Inc.]
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.opensaml.xml.security;
 
 import java.io.ByteArrayInputStream;
 import java.math.BigInteger;
 import java.security.Key;
 import java.security.KeyException;
 import java.security.KeyFactory;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.NoSuchAlgorithmException;
 import java.security.NoSuchProviderException;
 import java.security.PublicKey;
 import java.security.cert.CRLException;
 import java.security.cert.CertificateException;
 import java.security.cert.CertificateFactory;
 import java.security.interfaces.DSAPublicKey;
 import java.security.interfaces.RSAPublicKey;
 import java.security.spec.InvalidKeySpecException;
 import java.security.spec.KeySpec;
 import java.security.spec.X509EncodedKeySpec;
 
 import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
 
 import org.apache.xml.security.algorithms.JCEMapper;
 import org.opensaml.xml.util.Base64;
 
 /**
  * Some utility methods for doing security, credential, key and JCE related tests.
  */
 public class SecurityTestHelper {
     
     /**
      * Constructor.
      */
     protected SecurityTestHelper() { }
     
     /**
      * Build Java certificate from base64 encoding.
      * 
      * @param base64Cert base64-encoded certificate
      * @return a native Java X509 certificate
      * @throws CertificateException thrown if there is an error constructing certificate
      */
     public static java.security.cert.X509Certificate buildJavaX509Cert(String base64Cert) throws CertificateException {
         CertificateFactory  cf = null;
         cf = CertificateFactory.getInstance("X.509");
         
         ByteArrayInputStream input = new ByteArrayInputStream(Base64.decode(base64Cert));
         java.security.cert.X509Certificate newCert = null;
         newCert = (java.security.cert.X509Certificate) cf.generateCertificate(input);
         return newCert;
     }
     
     /**
      * Build Java CRL from base64 encoding.
      * 
      * @param base64CRL base64-encoded CRL
      * @return a native Java X509 CRL
      * @throws CertificateException thrown if there is an error constructing certificate
      * @throws CRLException  thrown if there is an error constructing CRL
      */
     public static java.security.cert.X509CRL buildJavaX509CRL(String base64CRL) 
         throws CertificateException, CRLException {
         CertificateFactory  cf = null;
         cf = CertificateFactory.getInstance("X.509");
         
         ByteArrayInputStream input = new ByteArrayInputStream(Base64.decode(base64CRL));
         java.security.cert.X509CRL newCRL = null;
         newCRL = (java.security.cert.X509CRL) cf.generateCRL(input);
         return newCRL;
     }
     
     /**
      * Build Java DSA public key from base64 encoding.
      * 
      * @param base64EncodedKey base64-encoded DSA public key
      * @return a native Java DSAPublicKey
      * @throws KeyException thrown if there is an error constructing key
      */
     public static DSAPublicKey buildJavaDSAPublicKey(String base64EncodedKey) throws KeyException {
         X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(base64EncodedKey));
         DSAPublicKey key = null;
         key = (DSAPublicKey) buildKey(keySpec, "DSA");
         return key;
     }
     
     /**
      * Build Java RSA public key from base64 encoding.
      * 
      * @param base64EncodedKey base64-encoded RSA public key
      * @return a native Java RSAPublicKey
      * @throws KeyException thrown if there is an error constructing key
      */
     public static RSAPublicKey buildJavaRSAPublicKey(String base64EncodedKey) throws KeyException {
         X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(base64EncodedKey));
         RSAPublicKey key = null;
         key = (RSAPublicKey) buildKey(keySpec, "RSA");
         return key;
     }
     
     
     /**
      * Build a Java BigInteger from the base64 encoded string.
      * 
      * @param base64Value a base64 encoded large integer
      * @return a BigInteger instance
      */
     public static BigInteger getBigInt(String base64Value) {
         return new BigInteger(Base64.decode(base64Value));  
     }
     
     /**
      * Generates a public key from the given key spec.
      * 
      * @param keySpec {@link KeySpec} specification for the key
      * @param keyAlgorithm key generation algorithm, only DSA and RSA supported
      * 
      * @return the generated {@link PublicKey}
      * 
      * @throws KeyException thrown if the key algorithm is not supported by the JCE or the key spec does not
      *             contain valid information
      */
     public static PublicKey buildKey(KeySpec keySpec, String keyAlgorithm) throws KeyException {
         try {
             KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
             return keyFactory.generatePublic(keySpec);
         } catch (NoSuchAlgorithmException e) {
             throw new KeyException(keyAlgorithm + "algorithm is not supported by the JCE", e);
         } catch (InvalidKeySpecException e) {
             throw new KeyException("Invalid key information", e);
         }
     }
     
     /**
      * Randomly generates a Java JCE symmetric Key object from the specified XML Encryption algorithm URI.
      * 
      * @param algoURI  The XML Encryption algorithm URI
     * @return a randomly-generated symmteric key
      * @throws NoSuchProviderException  provider not found
      * @throws NoSuchAlgorithmException algorithm not found
      */
    public static SecretKey generateKeyFromURI(String algoURI) 
        throws NoSuchAlgorithmException, NoSuchProviderException {
         String jceAlgorithmName = JCEMapper.getJCEKeyAlgorithmFromURI(algoURI);
         int keyLength = JCEMapper.getKeyLengthFromURI(algoURI);
         return generateKey(jceAlgorithmName, keyLength, null);
     }
     
     /**
      * Randomly generates a Java JCE KeyPair object from the specified XML Encryption algorithm URI.
      * 
      * @param algoURI  The XML Encryption algorithm URI
      * @param keyLength  the length of key to generate
      * @return a randomly-generated KeyPair
      * @throws NoSuchProviderException  provider not found
      * @throws NoSuchAlgorithmException  algorithm not found
      */
     public static KeyPair generateKeyPairFromURI(String algoURI, int keyLength) 
         throws NoSuchAlgorithmException, NoSuchProviderException {
         String jceAlgorithmName = JCEMapper.getJCEKeyAlgorithmFromURI(algoURI);
         return generateKeyPair(jceAlgorithmName, keyLength, null);
     }
     
     /**
      * Generate a random symmetric key.
      * 
      * @param algo key algorithm
      * @param keyLength key length
      * @param provider JCA provider
     * @return randomly generated symmetric key
      * @throws NoSuchAlgorithmException algorithm not found
      * @throws NoSuchProviderException provider not found
      */
    public static SecretKey generateKey(String algo, int keyLength, String provider) 
         throws NoSuchAlgorithmException, NoSuchProviderException {
        SecretKey key = null;
         KeyGenerator keyGenerator = null;
         if (provider != null) {
             keyGenerator = KeyGenerator.getInstance(algo, provider);
         } else {
             keyGenerator = KeyGenerator.getInstance(algo);
         }
         keyGenerator.init(keyLength);
         key = keyGenerator.generateKey();
         return key;
     }
     
     /**
      * Generate a random asymmetric key pair.
      * 
      * @param algo key algorithm
      * @param keyLength key length
      * @param provider JCA provider
      * @return randomly generated key
      * @throws NoSuchAlgorithmException algorithm not found
      * @throws NoSuchProviderException provider not found
      */
     public static KeyPair generateKeyPair(String algo, int keyLength, String provider) 
         throws NoSuchAlgorithmException, NoSuchProviderException {
         KeyPair keyPair = null;
         KeyPairGenerator keyGenerator = null;
         if (provider != null) {
             keyGenerator = KeyPairGenerator.getInstance(algo, provider);
         } else {
             keyGenerator = KeyPairGenerator.getInstance(algo);
         }
         keyGenerator.initialize(keyLength);
         keyPair = keyGenerator.generateKeyPair();
         return keyPair;
     }
 
 
 }
