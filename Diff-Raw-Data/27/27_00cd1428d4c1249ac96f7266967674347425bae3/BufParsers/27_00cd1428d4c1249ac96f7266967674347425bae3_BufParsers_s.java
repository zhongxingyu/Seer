 package net.bodz.bas.util.regex;
 
 import java.nio.CharBuffer;
 
 import javax.annotation.Generated;
 
 import net.bodz.bas.cdata.chars.CharFeature;
 
 public class BufParsers {
 
     /**
      * @return -1 if no digit
      */
     public static int getInt(CharBuffer buf, int radix, int max) {
         int limit = buf.limit();
         int val = 0; // T
         int start = buf.position();
         int end = start;
        int safeDigits = CharFeature.getDigits(radix, max);
         if (safeDigits < 0)
             safeDigits = -safeDigits;
         int digits = 0;
         boolean tryMore = false;
         while (end < limit) {
             char c = buf.get(end);
             int d = CharFeature.c2n[c];
             if (d == -1 || d >= radix)
                 break;
             if (tryMore) {
                 int den = max / val; // T
                 if (den < radix)
                     break;
                 if (den == radix) {
                     int rem = max % val; // T
                     if (rem < d)
                         break;
                 }
                 val = val * radix + d;
                 end++;
                 break;
             }
             val = val * radix + d;
             end++;
             if (val != 0)
                 digits++;
             if (max != 0 && digits == safeDigits) {
                 // if (safeDigits < 0) break;
                 tryMore = true;
             }
         }
         if (end == start)
             return -1;
         buf.position(end);
         return val;
     }
 
     public static int getInt(CharBuffer buf, int radix) {
        return getInt(buf, radix, Integer.MAX_VALUE);
     }
 
     public static int getInt(CharBuffer buf) {
         return getInt(buf, 10);
     }
 
     /**
      * @see #getInt(CharBuffer, int, int)
      */
     @Generated("getInt")
     public static long getLong(CharBuffer buf, int radix, long max) {
         int limit = buf.limit();
         long val = 0;
         int start = buf.position();
         int end = start;
        int safeDigits = CharFeature.getDigits(radix, max);
         if (safeDigits < 0)
             safeDigits = -safeDigits;
         int digits = 0;
         boolean tryMore = false;
         while (end < limit) {
             char c = buf.get(end);
             int d = CharFeature.c2n[c];
             if (d == -1 || d >= radix)
                 break;
             if (tryMore) {
                 int den = (int) (max / val);
                 if (den < radix)
                     break;
                 if (den == radix) {
                     int rem = (int) (max % val);
                     if (rem < d)
                         break;
                 }
                 val = val * radix + d;
                 end++;
                 break;
             }
             val = val * radix + d;
             end++;
             if (val != 0)
                 digits++;
             if (max != 0 && digits == safeDigits) {
                 // if (safeDigits < 0) break;
                 tryMore = true;
             }
         }
         if (end == start)
             return -1;
         buf.position(end);
         return val;
     }
 
     public static long getLong(CharBuffer buf, int radix) {
        return getLong(buf, radix, Long.MAX_VALUE);
     }
 
     public static long getLong(CharBuffer buf) {
         return getLong(buf, 10);
     }
 
 }
