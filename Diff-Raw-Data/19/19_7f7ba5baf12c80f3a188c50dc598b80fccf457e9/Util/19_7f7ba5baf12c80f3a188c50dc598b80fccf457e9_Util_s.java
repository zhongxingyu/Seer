 /******************************************************
  * Copyright (C) 2012 Felix Wiemuth                   *
  * Licensed under the GNU GENERAL PUBLIC LICENSE      *
  * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
  ******************************************************/
 package shoptool;
 
 import java.io.*;
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
     private final static String seperator = " ";
     private final static String assign = "=";
 
     public static boolean checkFile(File file, String extension) {
         return file.isFile()
                 && file.getPath().substring(
                 file.getPath().lastIndexOf('.') + 1).equals(extension);
     }
 
     /**
      * Get all shops in directory 'dir'.
      * @param dir
      * @return 
      */
     public static LinkedList<? extends ShopInterface> getShops(File dir) {
         LinkedList<Shop> shops = new LinkedList<Shop>();
 
         File[] files = dir.listFiles(new FileFilter() {
 
             @Override
             public boolean accept(File file) {
                 return checkFile(file, Shop.extension());
             }
         });
 
         for (File file : files) {
             shops.add(new Shop(file));
             if (shops.peekFirst() == null) //file was no correct shop file
             {
                 shops.removeFirst();
             }
         }
 
         return shops;
     }
 
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
         int i = line.indexOf(key + assign);
         int end = line.indexOf(seperator, i);
         if (end == -1) {
             end = line.length();
         }
         try {
             return line.substring(line.indexOf(assign, i) + 1, end);
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
 
     public static void log(Object o) {
         System.out.println(o);
     }
 
     public static void warn(Object o) {
         log("[WARNING] " + o);
     }
 
     public static void err(Object o) {
         log("[ERROR] " + o);
     }
 }
