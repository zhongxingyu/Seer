 package cryptocast.util;
 
import java.io.UnsupportedEncodingException;
 
 public class ByteStringUtils {
     public static byte[] str2bytes(String str) {
        try {
            return str.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            // cannot happen, as ISO-8859-1 must be supported
            // according to specs
            return null;
        }
     }
 }
