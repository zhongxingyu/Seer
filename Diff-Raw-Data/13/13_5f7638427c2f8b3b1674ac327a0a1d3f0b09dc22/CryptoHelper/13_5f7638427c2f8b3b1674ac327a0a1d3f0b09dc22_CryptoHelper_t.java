 package com.swareg.onsite.crypto;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 
 public class CryptoHelper {
    public static String convertBytesToHexString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
    
    public static String hmacMD5(byte[] key, byte[] data) {
         try {
             Mac mac = Mac.getInstance("HmacMD5");
             mac.init(new SecretKeySpec(key, "HmacMD5"));
             mac.update(data);
            return convertBytesToHexString(mac.doFinal());
         } catch (Exception e) {
             return null;
         }
     }
  }
