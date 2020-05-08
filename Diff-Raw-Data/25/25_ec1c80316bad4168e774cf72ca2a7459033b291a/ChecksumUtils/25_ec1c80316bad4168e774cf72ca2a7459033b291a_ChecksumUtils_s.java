 /*
  * #%L
  * Bitrepository Modifying Client
  * 
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.Key;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 
 import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumType;
 import org.bitrepository.protocol.CoordinationLayerException;
 
 /**
  * Utility class for handling checksum calculations.
  * Uses the HMAC method for calculating the checksums with salt.
  */
 public final class ChecksumUtils {
     
     /** The number of bytes per hexadecimal digit.*/
     private static final int BYTES_PER_HEX = 4;
     /** The maximal value of a single hexadecimal digit: '0x0F'.*/
     private static final int SINGLE_HEX_MAX = 0x0F;
     /** The maximal size of the byte array for digest.*/
     private static final int BYTE_ARRAY_SIZE_FOR_DIGEST = 4096;
     
     /** 
      * Private constructor. To prevent instantiation of this utility class.
      */
     private ChecksumUtils() { }
     
     /**
      * Calculates the checksum for a file based on the given checksum algorith, where the calculcation is salted.
      * 
      * @param file The file to calculate the checksum for.
      * @param csSpec The checksum specification for the calculation of the checksum. 
      * @return The checksum of the file in hexadecimal.
      */
     public static String generateChecksum(File file, ChecksumSpecTYPE csSpec) {
         try {
             return generateChecksum(new FileInputStream(file), csSpec);
         } catch (IOException e) {
             throw new CoordinationLayerException("Could not calculate the checksum for the file '" 
                     + file.getAbsolutePath() + "'.", e);
         }
     }
     
     /**
      * Calculates a checksum based on 
      * 
      * @param content The inputstream for the data to calculate the checksum of.
      * @param algorithm The algorithm to use for calculation. If it is not prefixed with 'Hmac', then it is added.
      * @param salt The salt for the calculation. 
      * @return The HMAC calculated checksum in hexadecimal.
      */
     public static String generateChecksum(InputStream content, ChecksumSpecTYPE csSpec) {
         
         byte[] digest = null;
         ChecksumType algorithm = csSpec.getChecksumType();
         
         if((algorithm == ChecksumType.MD5) 
                 || (algorithm == ChecksumType.SHA1)
                 || (algorithm == ChecksumType.SHA256)
                 || (algorithm == ChecksumType.SHA384)
                 || (algorithm == ChecksumType.SHA512)) {
             if(csSpec.getChecksumSalt() != null && csSpec.getChecksumSalt().length > 0) {
                 throw new IllegalArgumentException("Cannot perform a message-digest checksum calculation with salt "
                         + "as requested:" + csSpec);
             }
             digest = CalculateChecksumWithMessageDigest(content, algorithm);
         } else if((algorithm == ChecksumType.HMAC_MD5) 
                 || (algorithm == ChecksumType.HMAC_SHA1)
                 || (algorithm == ChecksumType.HMAC_SHA256)
                 || (algorithm == ChecksumType.HMAC_SHA384)
                 || (algorithm == ChecksumType.HMAC_SHA512)) {
            if(csSpec.getChecksumSalt() == null) {
                 throw new IllegalArgumentException("Cannot perform a HMAC checksum calculation without salt as requested:" 
                         + csSpec);
             }
             digest = CalculateChecksumWithHMAC(content, algorithm, csSpec.getChecksumSalt());
         } else {
             throw new IllegalStateException("The checksum algorithm '" + csSpec.getChecksumType().name() 
                     + "' is not supported.");
         }
         
         return toHex(digest);
     }
     
     /**
      * Calculation of the checksum for a given input stream through the use of message digestion on the checksum 
      * type as algorithm.
      * 
      * NOTE: the 'SHA' algorithm need a dash, '-', after the SHA, which is currently not in the protocol defined 
      * algorithm names.
      * 
      * @param content The input stream with the content to calculate the checksum of.
      * @param csType The type of checksum to calculate, e.g. the algorithm.
      * @return The calculated checksum.
      */
     private static byte[] CalculateChecksumWithMessageDigest(InputStream content, ChecksumType csType) {
         byte[] bytes = new byte[BYTE_ARRAY_SIZE_FOR_DIGEST];
         int bytesRead;
         
         try {
             String algorithmName = csType.name();
             if(algorithmName.startsWith("SHA")) {
                 algorithmName = algorithmName.replace("SHA", "SHA-");
             }
             
             MessageDigest digester = MessageDigest.getInstance(algorithmName);
             while ((bytesRead = content.read(bytes)) > 0) {
                 digester.update(bytes, 0, bytesRead);
             }
             return digester.digest();
         } catch (Exception e) {
             throw new CoordinationLayerException("Cannot calculate the checksum.", e);
         }
     }
     
     /**
      * Calculation of the checksum for a given input stream through the use of HMAC based on the checksum type as 
      * algorithm and the optional salt.
      * 
      * NOTE: the 'HMAC' algorithms need to have the underscore, '_', after the HMAC removed, which is currently in the 
      * protocol defined algorithm names.
      * 
      * @param content The input stream with the content to calculate the checksum of.
      * @param csType The type of checksum to calculate, e.g. the algorithm.
      * @param salt The salt for key encrypting the HMAC calculation.
      * @return The calculated checksum.
      */
     private static byte[] CalculateChecksumWithHMAC(InputStream content, ChecksumType csType, byte[] salt) {
         byte[] bytes = new byte[BYTE_ARRAY_SIZE_FOR_DIGEST];
         int bytesRead;
         
         try {
            String algorithmName = csType.name().replace("_", "");
            
             Mac messageAuthenticationCode = Mac.getInstance(algorithmName);
             Key key = new SecretKeySpec(salt, algorithmName);
             
             // digest the content for calculating the checksum.
             messageAuthenticationCode.init(key);
             while ((bytesRead = content.read(bytes)) > 0) {
                 messageAuthenticationCode.update(bytes, 0, bytesRead);
             }
             
             return messageAuthenticationCode.doFinal();
         } catch (Exception e) {
            throw new CoordinationLayerException("Cannot calculate the checksum.", e);
         }
     }
     
     /**
      * Method for extracting a array of bytes as hex.
      * 
      * @param byteArray The byte array to make into hex.
      * @return The hexadecimal representation of the byte array.
      */
     private static String toHex(byte[] byteArray) {
         // The list of value representations for hexadecimals.
         char[] hexdigit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
         
         StringBuffer sb = new StringBuffer("");
         for (byte b : byteArray) {
             sb.append(hexdigit[(b >> BYTES_PER_HEX) & SINGLE_HEX_MAX]);
             sb.append(hexdigit[b & SINGLE_HEX_MAX]);
         }
         return sb.toString();
     }
     
     /**
      * Returns whether a given checksum calculation algorithm exists.
      * This validates both whether the ChecksumType is implemented and whether the salt is put the correct place. 
      * 
      * @param checksumSpec The specification for the checksum calculation to validate.
      * @throws NoSuchAlgorithmException If the algorithm does not exist.
      */
     public static void verifyAlgorithm(ChecksumSpecTYPE checksumSpec) throws NoSuchAlgorithmException {
         ChecksumType algorithm = checksumSpec.getChecksumType();
         if(algorithm == ChecksumType.OTHER) {
             throw new NoSuchAlgorithmException("Cannot handle non-predefined checksum algorithms: '"
                     + checksumSpec + "'.");
         }
         
         if((algorithm == ChecksumType.MD5) 
                 || (algorithm == ChecksumType.SHA1)
                 || (algorithm == ChecksumType.SHA256)
                 || (algorithm == ChecksumType.SHA384)
                 || (algorithm == ChecksumType.SHA512)) {
             if(checksumSpec.getChecksumSalt() != null && checksumSpec.getChecksumSalt().length > 0) {
                 throw new NoSuchAlgorithmException("Cannot perform a message-digest checksum calculation with salt "
                         + "as requested:" + checksumSpec);
             }
         } else if((algorithm == ChecksumType.HMAC_MD5) 
                 || (algorithm == ChecksumType.HMAC_SHA1)
                 || (algorithm == ChecksumType.HMAC_SHA256)
                 || (algorithm == ChecksumType.HMAC_SHA384)
                 || (algorithm == ChecksumType.HMAC_SHA512)) {
             if(checksumSpec.getChecksumSalt() == null) {
                 throw new NoSuchAlgorithmException("Cannot perform a HMAC checksum calculation without salt as "
                         + "requested:" + checksumSpec);
             }
         } else {
             throw new NoSuchAlgorithmException("The checksum specification '" + checksumSpec + "' is not supported.");
         }
     }
 }
