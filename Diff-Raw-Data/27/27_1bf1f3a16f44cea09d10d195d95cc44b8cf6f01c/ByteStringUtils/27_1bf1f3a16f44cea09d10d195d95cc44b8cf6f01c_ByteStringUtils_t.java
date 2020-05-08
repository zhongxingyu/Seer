 package cryptocast.util;
 
import com.google.common.base.Charsets;
 
 public class ByteStringUtils {
     public static byte[] str2bytes(String str) {
        return str.getBytes(Charsets.ISO_8859_1);
     }
 }
