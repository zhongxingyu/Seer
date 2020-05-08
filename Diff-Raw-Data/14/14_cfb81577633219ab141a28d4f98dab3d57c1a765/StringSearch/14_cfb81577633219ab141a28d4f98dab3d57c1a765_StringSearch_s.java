 package net.bodz.bas.string;
 
 import java.util.Locale;
 
import org.apache.commons.lang.ArrayUtils;

 public class StringSearch {
 
     public static int indexOf(String s, char[] charSet, int fromIndex) {
         int len = s.length();
         for (int i = fromIndex; i < len; i++) {
             char c = s.charAt(i);
            if (ArrayUtils.indexOf(charSet, c) != -1)
                return i;
         }
         return -1;
     }
 
     public static int indexOf(String s, char... charSet) {
         return indexOf(s, charSet, 0);
     }
 
     public static int indexOf(String s, int charType, int fromIndex) {
         int len = s.length();
         for (int i = fromIndex; i < len; i++) {
             char c = s.charAt(i);
             int type = Character.getType(c);
             if (type == charType)
                 return i;
         }
         return -1;
     }
 
     public static int indexOf(String s, int charType) {
         return indexOf(s, charType, 0);
     }
 
     public static int lastIndexOf(String s, char[] charSet, int fromIndex) {
         int len = s.length();
         if (fromIndex >= len)
             fromIndex = len - 1;
         for (int i = fromIndex; i >= 0; i--) {
             char c = s.charAt(i);
            if (ArrayUtils.indexOf(charSet, c) != -1)
                return i;
         }
         return -1;
     }
 
     public static int lastIndexOf(String s, char[] charSet) {
         return lastIndexOf(s, charSet, s.length() - 1);
     }
 
     public static int lastIndexOf(String s, int charType, int fromIndex) {
         int len = s.length();
         if (fromIndex >= len)
             fromIndex = len - 1;
         for (int i = fromIndex; i >= 0; i--) {
             char c = s.charAt(i);
             int type = Character.getType(c);
             if (type == charType)
                 return i;
         }
         return -1;
     }
 
     public static int lastIndexOf(String s, int charType) {
         return lastIndexOf(s, charType, s.length() - 1);
     }
 
     public static int indexOfIgnoreCase(String s, String needle, Locale locale) {
         return s.toLowerCase(locale).indexOf(needle.toLowerCase(locale));
     }
 
     public static int indexOfIgnoreCase(String s, String needle) {
         return s.toLowerCase().indexOf(needle.toLowerCase());
     }
 
     public static int indexOfIgnoreCase(String s, char needle, Locale locale) {
         return s.toLowerCase(locale).indexOf(Character.toLowerCase(needle));
     }
 
     public static int indexOfIgnoreCase(String s, char needle) {
         return s.toLowerCase().indexOf(Character.toLowerCase(needle));
     }
 
 }
