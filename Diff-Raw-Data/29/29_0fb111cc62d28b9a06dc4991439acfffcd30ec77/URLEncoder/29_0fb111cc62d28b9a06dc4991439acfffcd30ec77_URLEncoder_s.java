 package uk.org.ponder.stringutil;
 
 import uk.org.ponder.util.UniversalRuntimeException;
 
 /**
  * A light wrapper for <CODE>java.net.URLEncoder</CODE>. Consult SVN rev 194
  * for the historical implementation described in the following comment, which
  * refers to a bug that was fixed in JDK 1.4.
  * <p>
  * This class is similar to <CODE>java.net.URLEncoder</CODE> except that it
  * can handle non-Latin characters, whereas Java's version is <A
  * HREF="http://developer.java.sun.com/developer/bugParade/bugs/4257115.html">
  * documented</A> to use only <CODE>ISO 8859-1</CODE>.
  */
 
 public abstract class URLEncoder {
 
   /** Encode the supplied URL into a UTF-8 based URL encoding */
 
   public static String encode(String s) {
     try {
      return java.net.URLEncoder.encode(s, "UTF-8");
     }
     catch (Exception e) { // should never happen
       throw UniversalRuntimeException.accumulate(e, "Error encoding URL " + s);
     }
   }
 
   /**
    * URL-encodes a string using any available encoding.
    * 
   * @param s
   *          what to encode
   * @param encoding
   *          name of the encoding to use;
    * @return URL-encoded version of <CODE>s</CODE>
    * @throws java.io.UnsupportedEncodingException
    *           if <CODE>encoding</CODE> isn't supported by the Java VM and/or
    *           class-libraries
    * @pre s != null
    * @pre encoding != null
    */
   public static String encode(String s, String encoding) {
     try {
       return java.net.URLEncoder.encode(s, encoding);
     }
     catch (Exception e) {
       throw UniversalRuntimeException.accumulate(e, "Error encoding URL " + s);
     }
   }
 
   static final int caseDiff = ('a' - 'A');
 
   /**
    * Converts a single character (byte) into its upper-case URL hex
    * representation %E0, say.
    */
 
   public static void appendURLHex(char c, CharWrap target) {
     target.append('%');
     char ch = Character.forDigit((c >> 4) & 0xF, 16);
     // converting to use uppercase letter as part of
     // the hex value if ch is a letter.
     if (Character.isLetter(ch)) {
       ch -= caseDiff;
     }
     target.append(ch);
     ch = Character.forDigit(c & 0xF, 16);
     if (Character.isLetter(ch)) {
       ch -= caseDiff;
     }
     target.append(ch);
   }
 
 }
