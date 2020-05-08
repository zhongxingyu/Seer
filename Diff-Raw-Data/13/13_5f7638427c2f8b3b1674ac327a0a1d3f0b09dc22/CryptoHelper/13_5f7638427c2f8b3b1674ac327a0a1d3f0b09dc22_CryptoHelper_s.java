 package com.swareg.onsite.crypto;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 
 public class CryptoHelper {
    public static byte[] hmacMD5(byte[] key, byte[] data) {
         try {
             Mac mac = Mac.getInstance("HmacMD5");
             mac.init(new SecretKeySpec(key, "HmacMD5"));
             mac.update(data);
            return mac.doFinal();
         } catch (Exception e) {
             return null;
         }
     }
  }
