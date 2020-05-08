 /*
  * Copyright (c) 2011. KloudTek Ltd
  */
 
 package com.kloudtek.util;
 
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
 import org.testng.annotations.Test;
 
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

 import static com.kloudtek.util.CryptoUtils.Algorithm.SHA1;
import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 public class CryptoUtilsTest {
     @Test
     public void testSSHA() {
         byte[] data = "HelloWorldThisIsADigestToEncode@#$gfms8i23o5m*T)6y891'".getBytes();
         byte[] digest = CryptoUtils.createDigest(data, SHA1);
         assertTrue(CryptoUtils.compareSaltedDigest(digest, data, SHA1));
         assertFalse(CryptoUtils.compareSaltedDigest(digest, "sfaiofdsadjiofsadjiosafjasfdo".getBytes(), SHA1));
     }
 
     @Test
     public void testSSHABase64() {
         String data = "HelloWorldThisIsADigestToEncode@#$gfms8i23o5m*T)6y891'";
         String digest = CryptoUtils.createB64SaltedDigest(data, SHA1);
         assertTrue(CryptoUtils.compareSaltedDigest(digest, data, SHA1));
         assertFalse(CryptoUtils.compareSaltedDigest(digest, "sfaiofdsadjiofsadjiosafjasfdo", SHA1));
     }
 
     @Test
     public void testCompareGeneratedBase64() {
         String value = "ASfdasfdfsdafsdajfsdaljfdslakjfsadkjf";
         String cryptedSaltedValue = CryptoUtils.createB64SaltedDigest(value, SHA1);
         assertTrue( CryptoUtils.compareSaltedDigest(cryptedSaltedValue,value,SHA1));
     }

    @Test
    public void createSHADigestFromStream() throws NoSuchAlgorithmException, IOException {
        byte[] value = "afdsfsdafasdafdsasfdsa".getBytes();
        byte[] digest = MessageDigest.getInstance("SHA-1").digest(value);
        assertEquals(CryptoUtils.createDigest(new ByteArrayInputStream(value),SHA1),digest);
    }
 }
