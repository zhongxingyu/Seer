 /******************************************************
  * Copyright (C) 2012 Felix Wiemuth                   *
  * Licensed under the GNU GENERAL PUBLIC LICENSE      *
  * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
  ******************************************************/
 package shoptool;
 
 import java.io.*;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Felix Wiemuth
  */
 public class Util {
     //global control characters for all files
 
     private final static char COMMENT_BEGIN = '#';
    private static String seperator = null; //must be set by callers of the static methods
     private final static String assign = "=";
 
    public static void setSeperator(String s) {
        seperator = s;
    }

     public static boolean checkFile(File file, String extension) {
         //DEBUG
 //        log("[" + file.toString() + "]" + "isFile:" + file.isFile());
 //        log("extension:" + file.getPath().substring(
 //                file.getPath().lastIndexOf('.') + 1).equals(extension));
         if (!file.isFile()) {
             err("File '" + file.toString() + "' does not exist!");
             return false;
         }
         if (!file.getPath().substring(file.getPath().lastIndexOf('.') + 1).equals(extension)) {
             return false;
         }
         return true;
     }
 
     /**
      * Get all shops in directory 'dir'.
      * The contained data, including those in the pricelists,
      * is guaranteed to be in the correct format.
      * @param dir
      * @return 
      */
     public static LinkedList<? extends ShopInterface> getShops(String dir) {
         LinkedList<Shop> shops = Shop.getShops(new File(dir));
         //revise the list / remove shops that cannot be corrected
         Iterator<Shop> it = shops.iterator();
         while (it.hasNext()) {
             Shop s = it.next();
             if (!s.revise()) {
                 err("Could not revise shop '" + s.name() + "' - removing shop!");
                 it.remove();
             }
 
         }
         return shops;
     }
 
    //extract the import part of the line (without comment)
     public static String extractCode(String raw) {
         String line; //line without comment
         try {
             line = raw.substring(0, raw.indexOf(COMMENT_BEGIN));
         } catch (IndexOutOfBoundsException e) {
             line = raw;
         }
         return line;
     }
 
     //Extract a string value
     public static String extract(String line, String key) {
         if (line == null) {
             return null;
         }
        String code = extractCode(line);
        int i = code.indexOf(key + assign);
         if (i == -1) //key not found
         {
             return null;
         }
        int end = code.indexOf(seperator, i);
         if (end == -1) {
            end = code.length();
         }
         try {
            String ret = code.substring(code.indexOf(assign, i) + 1, end);
             if (ret.length() == 0) {
                 return null;
             }
             return ret;
         } catch (IndexOutOfBoundsException e) {
             return null;
         }
     }
 
     //Extract an Integer value - '-1' on failure
     public static int extractInt(String line, String key) {
         try {
             return Integer.parseInt(extract(line, key));
         } catch (NumberFormatException e) {
             return -1;
         }
     }
 
     public static String extractCoord(String line, String key) {
         String raw = extract(line, key);
         int x, y, z;
         int start, end;
         try {
             start = 0;
             end = raw.indexOf(',', start);
             x = Integer.parseInt(raw.substring(start, end));
             start = end + 1;
             end = raw.indexOf(',', start);
             y = Integer.parseInt(raw.substring(start, end));
             start = end + 1;
             z = Integer.parseInt(raw.substring(start, raw.length()));
         } catch (NumberFormatException e1) {
             return null;
         } catch (IndexOutOfBoundsException e2) {
             return null;
         }
         return Integer.toString(x) + "," + y + "," + z;
     }
 
     //Extract a Double value - '-1' on failure
     public static double extractDouble(String line, String key) {
         try {
             return Double.parseDouble(extract(line, key));
         } catch (NumberFormatException e) {
             return -1;
         }
     }
 
     public static String svalToStr(String key, String value) {
         if (value == null) {
             return valToStr(key, "");
         }
         return valToStr(key, value);
     }
 
     public static String ivalToStr(String key, int value) {
         if (value == -1) {
             return valToStr(key, "");
         }
         return valToStr(key, Integer.toString(value));
     }
 
     private static String valToStr(String key, String value) {
         return key + assign + value + seperator;
     }
 
     public static BufferedReader getReader(File file) {
         FileReader input = null;
         try {
             input = new FileReader(file);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
         }
         BufferedReader bufRead = new BufferedReader(input);
         return bufRead;
     }
 
     public static void closeReader(BufferedReader reader) {
         try {
             reader.close();
         } catch (IOException e) {
         }
     }
 
     public static void revErr(String key) {
         err(key + " not set - abort revision.");
     }
 
     public static void revWarn(String key, String object) {
         warn(key + " not set in '" + object + "' - setting defaults.");
     }
 
     public static void log(Object o) {
         System.out.println(o);
     }
 
     public static void info(Object o) {
         log("[INFO] " + o);
     }
 
     public static void warn(Object o) {
         log("[WARNING] " + o);
     }
 
     public static void err(Object o) {
         log("[ERROR] " + o);
     }
 }
