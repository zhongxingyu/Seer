 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package heartsim.util;
 
 /**
  * Utility class for String manipulation
  * @author Lee Boynton
  */
 public class StringUtils
 {
     public static String padRight(String s, int n)
     {
         return String.format("%1$-" + n + "s", s);
     }
 
     public static String firstToUpper(String s)
     {
         return s.substring(0,1).toUpperCase() + s.substring(1);
     }
 
     public static String prettify(String s)
     {
         // make first letter uppercase
         s = firstToUpper(s);
 
         // convert underscores to space
         s = s.replaceAll("_", " ");
 
         return s;
     }
 
     public static String removeNewLines(String s)
     {
         return s.replaceAll("\n", "");
     }
 
     public static String removeTabs(String s)
     {
         return s.replaceAll("\t", "");
     }
 }
