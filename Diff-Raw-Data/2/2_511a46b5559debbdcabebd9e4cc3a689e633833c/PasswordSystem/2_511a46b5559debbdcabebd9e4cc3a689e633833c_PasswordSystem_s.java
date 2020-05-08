 package de.damarus.mcdesktopinfo;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import org.bukkit.configuration.file.FileConfiguration;
 
 public class PasswordSystem {
 
     private static FileConfiguration config;
 
     public static String generateMD5(String x) {
         try {
             MessageDigest md = MessageDigest.getInstance("MD5");
 
             byte[] digestBytes = md.digest(x.getBytes());
             String digestString = "";
             int low, hi;
 
             for(int i = 0; i < digestBytes.length; i++) {
                 low = (digestBytes[i] & 0x0f);
                 hi = ((digestBytes[i] & 0xf0) >> 4);
                 digestString += Integer.toHexString(hi);
                 digestString += Integer.toHexString(low);
             }
 
             return digestString;
         } catch(NoSuchAlgorithmException e) {
             e.printStackTrace();
         }
 
         return null;
     }
 
     public static boolean checkAdminPW(String pwHash) {
         if(pwHash == null) throw new NullPointerException();
 
         // If password was delivered as clear text first create a MD5 from it
         if(pwHash.length() != 32) pwHash = generateMD5(pwHash);
 
         // Check if password is correct
         if(config.getString("adminPw").equals(pwHash)) return true;
 
         return false;
     }
 
     public static void digestPWs() {
         if(config.getString("adminPw").length() != 32)
            config.set("adminPw", generateMD5("'" + config.getString("adminPw") + "'"));
     }
 
     public static void setConfig(FileConfiguration fc) {
         config = fc;
     }
 }
