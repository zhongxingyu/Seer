 package sample.utilities;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dushyant
  * Date: 13/8/12
  * Time: 4:23 PM
  * To change this template use File | Settings | File Templates.
  */
 public class MD5Encoder {
     MessageDigest md;
 
     public MD5Encoder(){
         this.md = null;
         try {
             this.md = MessageDigest.getInstance("MD5");
         } catch (NoSuchAlgorithmException e) {
             e.printStackTrace();
         }
     }
 
     public String encodeString(String input_str){
        byte[] bytesOfMessage = new byte[0];
         bytesOfMessage = input_str.getBytes();
 
         byte[] md5_url = md.digest(bytesOfMessage);
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < md5_url.length; ++i) {
             sb.append(Integer.toHexString((md5_url[i] & 0xFF) | 0x100).substring(1, 3));
         }
         return sb.toString();
     }
 
 }
