 /*
  * Copyright (C) 2013 University of Edinburgh.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.org.ukfederation.mda.validate;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.PublicKey;
 import java.security.cert.X509Certificate;
 import java.security.interfaces.RSAPublicKey;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.annotation.concurrent.ThreadSafe;
 
 import net.shibboleth.metadata.Item;
 import net.shibboleth.metadata.pipeline.StageProcessingException;
 import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
 import net.shibboleth.utilities.java.support.component.ComponentSupport;
 import net.shibboleth.utilities.java.support.logic.Constraint;
 import net.shibboleth.utilities.java.support.resource.Resource;
 import net.shibboleth.utilities.java.support.resource.ResourceException;
 
 import org.apache.commons.codec.binary.Hex;
 
 /**
  * Validator class to check RSA moduli in X.509 certificates against a OpenSSL-format
  * blacklist. Appropriate blacklists are available as part of the Debian 7.x
  * openssl-blacklist and openssl-blacklist-extra packages.
  */
 @ThreadSafe
 public class X509CertificateRSAOpenSSLBlacklistValidator extends AbstractX509CertificateValidator {
     
     /** Sequence of bytes put on the front of the string to be hashed. */
     private final byte[] openSSLprefix = {
             'M', 'o', 'd', 'u', 'l', 'u', 's', '=',
     };
     
     /** Resource that provides the blacklist. */
     private Resource blacklistResource;
     
     /** Restrict checking to a given key size. Default: no restriction (0). */
     private int keySize;
 
     /** Set of digest values blacklisted by this validator. */
     private final Set<String> blacklistedValues = new HashSet<>();
     
     /**
      * Constructor.
      */
     public X509CertificateRSAOpenSSLBlacklistValidator() {
         super();
         setId("OpenSSLBlacklist");
     }
 
     /**
      * Gets the resource that provides the blacklist.
      * 
      * @return resource that provides the blacklist
      */
     @Nullable public Resource getBlacklistResource() {
         return blacklistResource;
     }
 
     /**
      * Sets the resource that provides the blacklist.
      * 
      * @param resource resource that provides the blacklist
      */
     public synchronized void setBlacklistResource(@Nonnull final Resource resource) {
         ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
         ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
 
         blacklistResource = Constraint.isNotNull(resource, "blacklist resource can not be null");
     }
     
     /**
      * Sets a key size restriction for this blacklist.
      * 
      * @param size restricted key size, or 0 for no restriction
      */
     public void setKeySize(final int size) {
         keySize = size;
     }
     
     /**
      * Gets the key size restriction for this blacklist.
      * 
      * @return restricted key size for this blacklist, or 0 if no restriction
      */
     public int getKeySize() {
         return keySize;
     }
 
     /**
      * Computes the OpenSSL digest value for the given modulus.
      * 
      * @param modulus RSA public modulus to be digested
      * @return value to be compared against the blacklist
      * @throws StageProcessingException if SHA1 digester can not be acquired, or for internal
      *      errors related to {@link ByteArrayOutputStream}
      */
     @Nonnull
     private String openSSLDigest(@Nonnull final BigInteger modulus) throws StageProcessingException {
         try {
             // Acquire a representation of the modulus
             byte[] modulusBytes = modulus.toByteArray();
             if (modulusBytes[0] == 0) {
                 // drop first 00 byte of modulus representation
                 modulusBytes = Arrays.copyOfRange(modulusBytes, 1, modulusBytes.length);
             }
             
             // Encode the modulus into upper-case hex characters
             final char[] encodedModulus = Hex.encodeHex(modulusBytes,  false);
             
             // Now construct the thing we want to hash
             ByteArrayOutputStream bb = new ByteArrayOutputStream();
             try {
                 bb.write(openSSLprefix);
                 for (char c : encodedModulus) {
                     bb.write((byte) c);
                 }
                 bb.write('\n');
             } catch (IOException e) {
                 throw new StageProcessingException("internal error writing to ByteArrayStream", e);
             }
             //System.out.println("To be digested: " + bb.toString());
     
             // Make the digest
             final MessageDigest digest = MessageDigest.getInstance("SHA1");
             digest.update(bb.toByteArray());
             final byte[] bytes = digest.digest();
             
             // Convert the digest to a lower-case hex string
             char [] encodedDigest = Hex.encodeHex(bytes, true);
             final String strValue = String.valueOf(encodedDigest);
             final String trimmed = strValue.substring(20);
             //System.out.println("Digest: " + strValue + " trimmed " + trimmed);
             return trimmed;
         } catch (NoSuchAlgorithmException e) {
             throw new StageProcessingException("could not create message digester", e);
         }
     }
     
     /** {@inheritDoc} */
     public void validate(@Nonnull final X509Certificate cert, @Nonnull final Item<?> item,
             @Nonnull final String stageId) throws StageProcessingException {
         ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
         final PublicKey key = cert.getPublicKey();
         if ("RSA".equals(key.getAlgorithm())) {
             final RSAPublicKey rsaKey = (RSAPublicKey) key;
             final BigInteger modulus = rsaKey.getModulus();
             if (keySize == 0 || keySize == modulus.bitLength()) {
                 final String value = openSSLDigest(modulus);
                 if (blacklistedValues.contains(value)) {
                    addError("RSA modulus included in key blacklist", item, stageId);
                 }
             }
         }
     }
 
     /** {@inheritDoc} */
     protected void doDestroy() {
         blacklistResource.destroy();
         blacklistResource = null;
         blacklistedValues.clear();
 
         super.doDestroy();
     }
 
     /** {@inheritDoc} */
     protected void doInitialize() throws ComponentInitializationException {
         super.doInitialize();
 
         if (blacklistResource == null) {
             throw new ComponentInitializationException("Unable to initialize " + getId()
                     + ", blacklistResource must not be null");
         }
 
         if (!blacklistResource.isInitialized()) {
             blacklistResource.initialize();
         }
 
         try {
             if (!blacklistResource.exists()) {
                 throw new ComponentInitializationException("Unable to initialize " + getId() + ", blacklistResource "
                         + blacklistResource.getLocation() + " does not exist");
             }
             
             try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(blacklistResource.getInputStream()))) {
                 while (true) {
                     final String line = reader.readLine();
                     if (line == null) {
                         break;
                     }
                     if (line.charAt(0) != '#') {
                         blacklistedValues.add(line);
                     }
                 }
             } catch (IOException e) {
                 throw new ComponentInitializationException("Unable to initialize " + getId()
                         + ", error reading blacklistResource " + blacklistResource.getLocation() + " information", e);
             }
 
         } catch (ResourceException e) {
             throw new ComponentInitializationException("Unable to initialize " + getId()
                     + ", error reading blacklistResource " + blacklistResource.getLocation() + " information", e);
         }
     }
 
 }
