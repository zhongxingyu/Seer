 package nmd.rss.collector.util;
 
 import java.nio.charset.Charset;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static nmd.rss.collector.util.Assert.assertNotNull;
 
 /**
  * Author : Igor Usenko ( igors48@gmail.com )
  * Date : 14.05.13
  */
 public final class CharsetTools {
 
    private static final Pattern CHARSET_PATTERN = Pattern.compile("charset=(.+?)\"", Pattern.CASE_INSENSITIVE);
     private static final Pattern ENCODING_PATTERN = Pattern.compile("encoding=\"(.+?)\"", Pattern.CASE_INSENSITIVE);
 
     private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
 
     public static String detectCharSet(final String data) {
         assertNotNull(data);
 
         String result = null;
 
         final Matcher charsetPatternMatcher = CHARSET_PATTERN.matcher(data);
 
         if (charsetPatternMatcher.find()) {
             result = charsetPatternMatcher.group(1);
         } else {
             final Matcher encodingPatternMatcher = ENCODING_PATTERN.matcher(data);
 
             if (encodingPatternMatcher.find()) {
                 result = encodingPatternMatcher.group(1);
             }
         }
 
         return result;
     }
 
     public static String convertToUtf8(final String string) {
         assertNotNull(string);
 
         final byte[] bytes = string.getBytes(UTF8_CHARSET);
 
         return new String(bytes, UTF8_CHARSET);
     }
 
     private CharsetTools() {
         // empty
     }
 
 }
