 package interiores.core;
 
 /**
  * Simple static class of utilities.
  * @author hector
  */
 public class Utils
 {
     /**
      * Method that returns the given string with the first letter in uppercase.
      * @param s The string to capitalize
      * @return The capitalized string
      */
     public static String capitalize(String s) {
         char[] arrayString = s.toCharArray();
         arrayString[0] = Character.toUpperCase(arrayString[0]);
         
         return new String(arrayString);
     }
     
     public static String decapitalize(String s) {
         char[] arrayString = s.toCharArray();
         arrayString[0] = Character.toLowerCase(arrayString[0]);
         
         return new String(arrayString);
     }
     
     /**
      * Pads a string to a certain length with empty spaces.
      * @param s The string to pad
      * @param n The length of the resulting string
      * @return The padded string
      */
     public static String padRight(String s, int n) {
         return String.format("%1$-" + n + "s", s);
     }
    
    public static String timeString(long time) {
    // This might be ugly
        String[] scale = {"ns", "us", "ms", "s"};
        int iters = 0;
        float d_time = time;
        while (d_time > 100 && iters < 3) {
            d_time /= 1000.0;
            ++iters;
        }
        String timeString;
        if (d_time > 100) {
            //we are in the minutes range
            int min = (int)d_time / 60;
            d_time %= 60;
            timeString = String.valueOf(min) + "m" + String.valueOf(d_time) + "s";
        }
        else {
            timeString = String.valueOf(d_time) + scale[iters];
        }
        
        return timeString;
    }
 }
