 package hadoopSameContent;
 
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 public class IdentifyingJsonSubset {
     private String clientIP;
     private String timestamp;
     private String url;
 
     public void setClientIP(String clientIP){
         this.clientIP = clientIP;
     }
 
     public void setTimestamp(String timestamp){
         this.timestamp = timestamp.split("\\.")[0];
     }
 
     public void setURL(String url){
         this.url = url;
     }
 
     public String getIdentifyingHash(){
         if (clientIP == null || timestamp == null || url == null){
             System.err.println("Fields required for hash not present in json, ignoring record");
             return null;
         }else{
             return SHA1(clientIP + timestamp + url);
         }
     }
     
     private static String SHA1(String text) {
         MessageDigest md;
         try {
             md = MessageDigest.getInstance("SHA-1");
             byte[] sha1hash = new byte[40];
             md.update(text.getBytes("iso-8859-1"), 0, text.length());
             sha1hash = md.digest();
             return convToHex(sha1hash);
         } catch (NoSuchAlgorithmException e) {
             e.printStackTrace();
             return text;
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
             return text;
         } catch (IllegalArgumentException e) {
             e.printStackTrace();
             System.err.println(text);
             return text;
         }
     }
 
     private static String convToHex(byte[] data) {
         StringBuilder buf = new StringBuilder();
         for (int i = 0; i < data.length; i++) {
             int halfbyte = (data[i] >>> 4) & 0x0F;
             int two_halfs = 0;
             do {
                 if ((0 <= halfbyte) && (halfbyte <= 9))
                     buf.append((char) ('0' + halfbyte));
                 else
                     buf.append((char) ('a' + (halfbyte - 10)));
                 halfbyte = data[i] & 0x0F;
             } while (two_halfs++ < 1);
         }
         return buf.toString();
     }
 }
