 package extensions;
 
 import play.templates.JavaExtensions;
 import org.apache.commons.lang.StringUtils;
 import com.google.common.collect.Lists;
 import com.google.common.base.Joiner;
 
 import java.util.List;
 
 import net.sparkmuse.common.NullTo;
 
 /**
  * @author neteller
  * @created: Dec 27, 2010
  */
 public class StringExtensions extends JavaExtensions {
 
   /**
    * Abbreviates to the maximum length but does not chop word boundaries.
    * Any markup is cleaned.
    *
    * @param s
    * @param maxLength
    * @return
    */
   public static String abbreviate(String s, int maxLength) {
     if (StringUtils.isBlank(s) || StringUtils.length(s) <= maxLength) return s;
 
     List<String> words = Lists.newArrayList();
    maxLength -= 3; //elipsis
     final String[] tokens = s.replaceAll("<p>", "")
         .replaceAll("</p>", "")
         .replaceAll("<.*>.*</.*>", "")
         .split(" ");
    for (int i = 0; maxLength > 0 && i < tokens.length; i++) {
       words.add(tokens[i]);
      maxLength -= tokens[i].length() + 1; //1 for implicit space
     }
 
     return Joiner.on(" ").join(words) + "...";
   }
 
   /**
    * Formats a string as an url.
    *
    * @param url
    * @return
    */
   public static String url(String url) {
     String toReturn;
 
     if (StringUtils.isBlank(url)) toReturn =  "#";
     else if (url.startsWith("http://") || url.startsWith("https://")) toReturn = url;
     else toReturn = "http://" + url;
 
     return toReturn;
   }
 
 }
