 /*
  * #%L
  * Bitrepository Common
  * 
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.protocol.utils;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.security.NoSuchAlgorithmException;
 
 import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumType;
 import org.jaccept.structure.ExtendedTestCase;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 public class ChecksumTest extends ExtendedTestCase {
     
     @Test(groups = { "regressiontest" })
     public void calculateHmacChecksums() throws Exception {
         addDescription("Tests whether the utility class for calculating checksums with HMAC is able to "
                 + "correctly calculate predefined examples from : "
                 + "http://en.wikipedia.org/wiki/HMAC#Examples_of_HMAC_.28MD5.2C_SHA1.2C_SHA256_.29");
         addStep("Setup variables.", "Should be OK");
         ChecksumSpecTYPE csHmacMD5 = new ChecksumSpecTYPE();
         csHmacMD5.setChecksumType(ChecksumType.HMAC_MD5);
        csHmacMD5.setChecksumSalt(new byte[0]);
         ChecksumSpecTYPE csHmacSHA1 = new ChecksumSpecTYPE();
         csHmacSHA1.setChecksumType(ChecksumType.HMAC_SHA1);
        csHmacSHA1.setChecksumSalt(new byte[0]);
         ChecksumSpecTYPE csHmacSHA256 = new ChecksumSpecTYPE();
         csHmacSHA256.setChecksumType(ChecksumType.HMAC_SHA256);
        csHmacSHA256.setChecksumSalt(new byte[0]);
         
         addStep("Test with no text and no key for HMAC_MD5, HMAC_SHA1, and HMAC_SHA256", 
                 "Should give expected results.");
         InputStream data1 = new ByteArrayInputStream(new byte[0]);
         Assert.assertEquals(ChecksumUtils.generateChecksum(data1, csHmacMD5), 
                 "74e6f7298a9c2d168935f58c001bad88");
         Assert.assertEquals(ChecksumUtils.generateChecksum(data1, csHmacSHA1), 
                 "fbdb1d1b18aa6c08324b7d64b71fb76370690e1d");
         Assert.assertEquals(ChecksumUtils.generateChecksum(data1, csHmacSHA256), 
                 "b613679a0814d9ec772f95d778c35fc5ff1697c493715653c6c712144292c5ad");
         
         String message = "The quick brown fox jumps over the lazy dog";
         InputStream data2 = new ByteArrayInputStream(message.getBytes());
         String key = "key";
         csHmacMD5.setChecksumSalt(key.getBytes());
         csHmacSHA1.setChecksumSalt(key.getBytes());
         csHmacSHA256.setChecksumSalt(key.getBytes());
         
         addStep("Test with the text '" + message + "' and key '" + key + "' for MD5, SHA1, and SHA256", 
                 "Should give expected results.");
         Assert.assertEquals(ChecksumUtils.generateChecksum(data2, csHmacMD5),
                 "80070713463e7749b90c2dc24911e275");
         data2.reset();
         Assert.assertEquals(ChecksumUtils.generateChecksum(data2, csHmacSHA1),
                 "de7c9b85b8b78aa6bc8a7a36f70a90701c9db4d9");
         data2.reset();
         Assert.assertEquals(ChecksumUtils.generateChecksum(data2, csHmacSHA256),
                 "f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8");
         data2.reset();
         
         addStep("Try calculating HMAC with a null salt", "Should throw NoSuchAlgorithmException");
         csHmacMD5.setChecksumSalt(null);
         try {
             ChecksumUtils.generateChecksum(data2, csHmacMD5);
             Assert.fail("Should throw an IllegalArgumentException here!");
         }  catch (IllegalArgumentException e) {
             // expected
         }
     }
     
     @Test(groups = { "regressiontest" })
     public void calculateDigestChecksums() throws Exception {
         addDescription("Tests whether the utility class for calculating checksums with MessageDigest is able to "
                 + "correctly calculate the checksums.");
         addStep("Setup variables.", "Should be OK");
         ChecksumSpecTYPE csMD5 = new ChecksumSpecTYPE();
         csMD5.setChecksumType(ChecksumType.MD5);
         ChecksumSpecTYPE csSHA1 = new ChecksumSpecTYPE();
         csSHA1.setChecksumType(ChecksumType.SHA1);
         ChecksumSpecTYPE csSHA256 = new ChecksumSpecTYPE();
         csSHA256.setChecksumType(ChecksumType.SHA256);
         
         addStep("Test with no text and no key for HMAC_MD5, HMAC_SHA1, and HMAC_SHA256", 
                 "Should give expected results.");
         InputStream data1 = new ByteArrayInputStream(new byte[0]);
         Assert.assertEquals(ChecksumUtils.generateChecksum(data1, csMD5), 
                 "d41d8cd98f00b204e9800998ecf8427e");
         Assert.assertEquals(ChecksumUtils.generateChecksum(data1, csSHA1), 
                 "da39a3ee5e6b4b0d3255bfef95601890afd80709");
         Assert.assertEquals(ChecksumUtils.generateChecksum(data1, csSHA256), 
                 "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
         
         addStep("Test when a salt is added", "Should throw an exception");
         String key = "key";
         csMD5.setChecksumSalt(key.getBytes());
         
         try {
             ChecksumUtils.generateChecksum(data1, csMD5);
             Assert.fail("Should throw an IllegalArgumentException here!");
         }  catch (IllegalArgumentException e) {
             // expected
         }        
     }
 
     @Test(groups = { "regressiontest" })
     public void testChecksumAlgorithmValidation() throws Exception {
         addDescription("Test the algorithm validation for every single possible checksum algorithm.");
         for (ChecksumType csType : ChecksumType.values()) {
             if(csType == ChecksumType.OTHER) {
                 validateOtherChecksumType(csType);
             } else if(csType.name().startsWith("HMAC")) {
                 validateHmac(csType);
             } else {
                 validateMessageDigest(csType);
             }
         }
     }
     
     private void validateOtherChecksumType(ChecksumType algorithm) {
         addStep("Test '" + algorithm + "'", "Should be invalid no matter the salt!");
         ChecksumSpecTYPE csType = new ChecksumSpecTYPE();
         csType.setChecksumType(algorithm);
 
         try {
             ChecksumUtils.verifyAlgorithm(csType);
             Assert.fail("The 'OTHER' algorithms should be invalid without the salt: '" + csType);
         } catch (NoSuchAlgorithmException e) {
             // expected
         }
 
         csType.setChecksumSalt(new byte[]{0});
         try {
             ChecksumUtils.verifyAlgorithm(csType);
             Assert.fail("The 'OTHER' algorithms should be invalid with an empty salt: '" + csType);
         } catch (NoSuchAlgorithmException e) {
             // expected
         }
 
         csType.setChecksumSalt(new byte[]{1});
         try {
             ChecksumUtils.verifyAlgorithm(csType);
             Assert.fail("The 'OTHER' algorithms should be invalid with the salt: '" + csType);
         } catch (NoSuchAlgorithmException e) {
             // expected
         }
     }
     
     private void validateHmac(ChecksumType hmacType) throws NoSuchAlgorithmException {
         addStep("Test '" + hmacType + "'", "Should be invalid without salt, and valid with no matter whether "
                 + "the salt is empty.");
         ChecksumSpecTYPE csType = new ChecksumSpecTYPE();
         csType.setChecksumType(hmacType);
         
         try {
             ChecksumUtils.verifyAlgorithm(csType);
             Assert.fail("The HMAC algorithms should be invalid without the salt: '" + csType);
         } catch (NoSuchAlgorithmException e) {
             // expected
         }
 
         csType.setChecksumSalt(new byte[]{0});
         ChecksumUtils.verifyAlgorithm(csType);   
 
         csType.setChecksumSalt(new byte[]{1});
         ChecksumUtils.verifyAlgorithm(csType);   
     }
     
     private void validateMessageDigest(ChecksumType algorithmType) throws NoSuchAlgorithmException {
         addStep("Test '" + algorithmType + "'", "Should be valid without salt, valid with an empty salt, "
                 + "and invalid with a proper salt.");
         ChecksumSpecTYPE csType = new ChecksumSpecTYPE();
         csType.setChecksumType(algorithmType);
         
         ChecksumUtils.verifyAlgorithm(csType);   
         
         csType.setChecksumSalt(new byte[0]);
         ChecksumUtils.verifyAlgorithm(csType);
         
         csType.setChecksumSalt(new byte[]{1});
         try {
             ChecksumUtils.verifyAlgorithm(csType);
             Assert.fail("The MessaegDigest algorithms should be invalid with the salt: '" + csType);
         } catch (NoSuchAlgorithmException e) {
             // expected
         }
         
     }
 }
