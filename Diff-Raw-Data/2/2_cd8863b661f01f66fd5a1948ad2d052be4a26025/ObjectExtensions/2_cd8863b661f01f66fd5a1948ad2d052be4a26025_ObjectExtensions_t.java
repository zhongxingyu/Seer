 package extensions;
 
 import org.apache.commons.lang.StringUtils;
 import play.templates.JavaExtensions;
 
 /**
  * Created by IntelliJ IDEA.
  *
  * @author neteller
  * @created: Nov 26, 2010
  */
 public class ObjectExtensions extends JavaExtensions {
 
   public static String toWords(Object object) {
    final String s = object.toString().replaceAll("_", " ");
     return StringUtils.capitalize(s.toLowerCase());
   }
 
 }
 
 
