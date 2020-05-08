 /*
  * Created on 02-Feb-2006
  */
 package uk.org.ponder.stringutil;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.Map;
 import java.util.StringTokenizer;
 
import uk.org.ponder.arrayutil.ArrayUtil;
 import uk.org.ponder.util.Logger;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 /** Utilities for operating on URLs */
 
 public class URLUtil {
   
   public static boolean isAbsolute(String url) {
     if (url.startsWith("javascript:") || url.startsWith("mailto:") || url.startsWith("file:")) 
         return true;
     int protpos = url.indexOf("://");
     if (protpos == -1) return false;
     for (int i = 0; i < protpos; ++ i) {
       if (!Character.isLetterOrDigit(url.charAt(i))) return false;
     }
     return true;
   }
   /** Append the supplied name/value pair to the end of the supplied URL, 
    * after URLencoding name and value.
    */
   
   public static String appendAttribute(String url, String name, String value) {
     CharWrap togo = new CharWrap(url);
     int qpos = url.indexOf('?');
     appendAttribute(togo, qpos == -1, name, value);
     return togo.toString();
   }
 
   /** Append the supplied name/value pair to the end of the supplied URL, 
    * after URLencoding name and value. The attribute will use the ? or &amp; 
    * character according to whether <code>isfirst</code> is true or false.
    */
   
   public static void appendAttribute(CharWrap togo, boolean isfirst, String name, String value) {
     togo.append(isfirst ? '?' : '&');
     togo.append(URLEncoder.encode(name));
     togo.append("=");
     togo.append(URLEncoder.encode(value));
   }
   
   /** Convert list of URL-form name/value pairs into a Map representation */
   // TODO: backport vector values code
   public static Map paramsToMap(String extraparams,
         Map target) {
       if (Logger.log.isDebugEnabled()) {
         Logger.log
           .debug("Action link requires extra parameters from " + extraparams);
       }
       StringTokenizer st = new StringTokenizer(extraparams, "&");
       while (st.hasMoreTokens()) {
         String token = st.nextToken();
         int eqpos = token.indexOf("=");
         String key = decodeURL(token.substring(0, eqpos));
         String value = decodeURL(token.substring(eqpos + 1));
         target.put(key, value);
       }
       return target;
     }
 
   public static String[] splitPathInfo(String pathinfo) {
    String[] togo = pathinfo.split("/");
    if (togo.length > 0 && togo[0].equals("")) {
      togo = (String[]) ArrayUtil.subArray(togo, 1, togo.length);
    }
    return togo;
   }
  
  /** Convert a pathinfo array into an array of segments **/
  public static String toPathInfo(String[] paths) {
    CharWrap togo = new CharWrap("/");
    for (int i = 0; i < paths.length; ++ i) {
      togo.append(URLEncoder.encode(paths[i])).append("/");
    }
    return togo.toString();
  }
  
   /** Decodes a URL assuming UTF-8, and wrapping any tedious exceptions */
   public static String decodeURL(String url) {
     try {
       return URLDecoder.decode(url, "UTF-8");
     }
     catch (UnsupportedEncodingException e) {
       throw UniversalRuntimeException.accumulate(e, "Error decoding URL " + 
           url + " using UTF-8");
     }
   }
   
   /** URL-encodes only the whitespace characters in a URL (necessary for some
    * faulty incomplete encodings).
    */
   
   public static String deSpace(String URL) {   
     CharWrap togo = new CharWrap(URL.length());
     for (int i = 0; i < URL.length(); ++ i) {
       char c = URL.charAt(i);
       if (Character.isWhitespace(c)) {
         URLEncoder.appendURLHex(c, togo);
       }
       else togo.append(c);
     }
     return togo.toString();
   }
   
 }
