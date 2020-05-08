 /* This class is part of the XP framework
  *
  * $Id$ 
  */
 
 package net.xp_framework.text;
 
 import java.util.StringTokenizer;
 
 /**
  * Provides methods to scan a string 
  *
  * @see http://php.net/sscanf
  */
 public abstract class Scanner {
 
     /**
      * Returns a span of a string comparing it to a mask
      *
      */
     protected static String span(String s, String mask, int offset, int length, boolean not) {
         StringBuffer result= new StringBuffer();
        for (int i= offset; i < Math.min(length, s.length()); i++) {
             char c= s.charAt(i);
             int p= mask.indexOf(c);
             
             if (not && p != -1 || !not && p == -1) break;
             result.append(c);
         }
         return result.toString();
     }
     
     /**
      * Returns a mask from a character class
      *
      */
     protected static String mask(String s) {
         StringBuffer mask= new StringBuffer();
         for (int i= 0; i < s.length()- 1; i++) {
             if ('-' == s.charAt(i+ 1)) {
                 for (char c= s.charAt(i); c < s.charAt(i+ 2); c++) {
                     mask.append(c);
                 }
                 i+= 2;
             } else {
                 mask.append(s.charAt(i));
             }
         }
         return mask.append(s.charAt(s.length()- 1)).toString();
     }
 
     /**
      * Scans a string 
      *
      * @param   input input string
      * @param   format format string with tokens
      * @return  scan result
      */
     public static Scanned scan(String input, String format) {
         StringTokenizer t= new StringTokenizer(format, "%", true);
         Scanned result= new Scanned();
         int offset= 0;
 
         while (t.hasMoreTokens()) {
             String token= t.nextToken();
             if ('%' == token.charAt(0)) {
                 int length, i= 0;
                 char c;
 
                 token= t.nextToken();
                 
                 // Length:
                 // * %10s, %1d, %2x
                 do { c= token.charAt(i++); } while (c >= '0' && c <= '9');
                 if (i > 1) {
                    length= Integer.parseInt(token.substring(0, i- 1)) + offset;
                 } else {
                     length= input.length();
                 }
                 
                 // Format:
                 // * %d - an integer (0-9)
                 // * %f - a float (0-9.)
                 // * %x - a hex number (0-9A-F), optionally prefixed w/ "0x"
                 // * %s - a string (scan until next whitespace, \r\n\s\t)
                 // * %c - a single character
                 // * %[<class>] - string (scan until any character outside <class> occurs)
                 // * %[^<class>] - string (scan until any character inside <class> occurs)
                 switch (c) {
                     case 'd': {
                        StringBuffer number= new StringBuffer(span(input, "-", offset, offset+ 1, false));
                        number.append(span(input, "0123456789", offset+ number.length(), length, false));
                         if (0 == number.length()) return result;
 
                        result.add(Integer.parseInt(number.toString()));
                         offset+= number.length();
                         token= token.substring(i, token.length());
                         break;
                     }
 
                     case 'x': {
                         String hex= span(input, "0123456789xABCDEFabcdef", offset, length, false);
                         if (0 == hex.length()) return result;
 
                         if ("0x".equals(hex.substring(0, 2))) {
                             result.add(Integer.parseInt(hex.substring(2, hex.length()), 16));
                         } else {
                             result.add(Integer.parseInt(hex, 16));
                         }
                         offset+= hex.length();
                         token= token.substring(i, token.length());
                         break;
                     }
 
                     case 'f': {
                        StringBuffer number= new StringBuffer(span(input, "-", offset, offset+ 1, false));
                        number.append(span(input, "0123456789.", offset+ number.length(), length, false));
                         if (0 == number.length()) return result;
 
                         result.add(Float.parseFloat(number.toString()));
                         offset+= number.length();
                         token= token.substring(i, token.length());
                         break;
                     }
 
                     case 's': {
                         String string= span(input, "\r\n\t ", offset, length, true);
                         if (0 == string.length()) return result;
 
                         result.add(string);
                         offset+= string.length();
                         token= token.substring(i, token.length());
                         break;
                     }
 
                     case 'c': {
                         if (offset >= input.length()) return result;
 
                         result.add(input.charAt(offset));
                         offset+= 1;
                         token= token.substring(i, token.length());
                         break;
                     }
 
                     case '[': {
                         String characterClass= token.substring(i, token.indexOf(']'));
                         String string= null;
                         if ('^' == characterClass.charAt(0)) {
                             string= span(input, mask(characterClass.substring(1, characterClass.length())), offset, length, true);
                         } else {
                             string= span(input, mask(characterClass), offset, length, false);
                         }
                         result.add(string);
                         offset+= string.length();
                         token= token.substring(i + characterClass.length() + 1, token.length());
                         break;
                     }
                 }
             }
 
             if (!token.equals(input.substring(offset, offset + token.length()))) return result;
             offset+= token.length();
         }
         
         return result;
     }
 }
