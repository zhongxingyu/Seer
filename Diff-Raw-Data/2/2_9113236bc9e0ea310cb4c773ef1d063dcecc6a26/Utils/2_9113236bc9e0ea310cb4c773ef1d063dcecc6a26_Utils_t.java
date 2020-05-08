 package com.github.detro.rps;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.MarkerFactory;
 
 import java.security.MessageDigest;
 import java.util.Formatter;
 
 /**
  * General purpose utility container
  */
 public class Utils {
     private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
 
     public static String generateStringSHA1(String input) {
         return generateStringDigest(input, "SHA-1");
     }
 
     public static String generateStringDigest(String input, String algorithm) {
         String result = "";
 
         try {
            MessageDigest digester = MessageDigest.getInstance(algorithm);
             digester.reset();
             digester.update(input.getBytes("UTF-8"));
 
             byte[] digest = digester.digest();
 
             Formatter f = new Formatter();
             for (byte b : digest) {
                 f.format("%02X", b);
             }
 
             result = f.toString();
             f.close();
         } catch (Exception e) {
             LOG.error(MarkerFactory.getMarker("FATAL"), "Couldn't Hash a String with SHA-1");
             System.exit(1);
         }
 
         return result;
     }
 }
