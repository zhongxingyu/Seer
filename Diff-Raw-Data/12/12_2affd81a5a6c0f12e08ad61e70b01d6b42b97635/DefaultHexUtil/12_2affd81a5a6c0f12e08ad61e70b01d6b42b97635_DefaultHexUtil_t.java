 package au.net.netstorm.boost.util.format;
 
 public final class DefaultHexUtil implements HexUtil {
    private static final String HEXCHAR = "0123456789abcdef";
     private static final int MASK = 0x0f;
 
     public byte[] toBytes(String hex) {
         int length = hex.length();
         byte[] result = new byte[length / 2];
         for (int i = 0, j = 0; i < length; i += 2, j++) {
             int high = nibble(hex, i);
             int low = nibble(hex, i + 1);
             result[j] = (byte) ((high << 4) | low);
         }
         return result;
     }
 
     public String toString(byte[] bytes) {
         int length = bytes.length * 2;
         char[] result = new char[length];
         for (int j = 0, i = 0; i < bytes.length; i++, j += 2) {
             byte b = bytes[i];
             result[j] = charAt(b, 4);
             result[j + 1] = charAt(b, 0);
         }
         return new String(result);
     }
 
     // DEBT CyclomaticComplexity {
     private int charToNibble(char c) {
         if ('0' <= c && c <= '9') return c - '0';
         if ('a' <= c && c <= 'f') return c - 'a' + 0xa;
         if ('A' <= c && c <= 'F') return c - 'A' + 0xa;
         throw new IllegalArgumentException("Invalid hex character: " + c);
     }
     // } DEBT CyclomaticComplexity
 
     private int nibble(String hex, int i) {
         char ch = hex.charAt(i);
         return charToNibble(ch);
     }
 
     private char charAt(byte b, int shift) {
         return HEXCHAR.charAt((b >> shift) & MASK);
     }
 }
