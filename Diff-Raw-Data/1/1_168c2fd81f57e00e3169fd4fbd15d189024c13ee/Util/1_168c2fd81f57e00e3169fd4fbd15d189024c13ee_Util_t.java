 package de.fhb.polyencoder;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 /**
  * Collection of useful functions.
  * 
  * @author Mark Rambow (markrambow[at]gmail[dot]com)
  * @author Peter Pensold
  * @version 0.5
  * 
  */
 public class Util {
 
   /**
    * Calculates the following phrase with a and b:<br/>
    * {@code Math.sqrt(Math.pow(a,2) + Math.pow(b,2))}
    * 
    * @param a
    * @param b
    * @return the result of this phrase
    */
   public static double sqrtOfSquared(double a, double b) {
     return Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
   }
 
 
 
   public static double createCenter(double min, double max) {
     return min + (max - min)/2;
   }
 
 
 
   public static String readFile(String fileName) {
     String loadingFailedOutput = "";
     FileReader fr;
     StringBuilder sb = new StringBuilder();
     String line;
 
     try {
       fr = new FileReader(fileName);
       BufferedReader br = new BufferedReader(fr);
 
       while ((line = br.readLine()) != null) {
         sb.append(line + "\n");
       }
      sb.deleteCharAt(sb.length()-1);
 
       fr.close();
     } catch (FileNotFoundException e) {
       sb = new StringBuilder(loadingFailedOutput);
     } catch (IOException e) {
       sb = new StringBuilder(loadingFailedOutput);
     }
 
     return sb.toString();
   }
 
 
 
   /**
    * Replaces all possible appearances of marker inside a text. The marker is a
    * regular expression. The marker will be surrounded with braces. E. g. if you
    * use the marker {@code name} the method searches for {{@code name} inside
    * the text.
    * 
    * @param text
    *          the string to search for marker
    * @param marker
    *          the marker for the place where the new text from parameter replace
    *          should be
    * @param replace
    *          text that replaces the marker
    * @return the replaced content of the text
    */
   public static String replaceMarker(String text, String marker, String replace) {
     return text.replaceAll("\\{" + marker + "\\}",replace);
   }
 
 
 
   public static String replaceMarker(String text, HashMap<String, String> map) {
     for (Entry<String, String> entry : map.entrySet()) {
       text = replaceMarker(text, entry.getKey(), entry.getValue());
     }
     return text;
   }
 }
