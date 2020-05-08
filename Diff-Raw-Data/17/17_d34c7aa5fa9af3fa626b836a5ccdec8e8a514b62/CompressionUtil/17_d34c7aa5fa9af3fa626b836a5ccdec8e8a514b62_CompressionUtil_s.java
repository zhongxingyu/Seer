 package com.googlecode.usc.svnfolder.compression.utils;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 
 /**
  *
  * @author lan
  */
 public final class CompressionUtil {
     public final static List<String> YES_LIST = Arrays.asList("Y", "y", "true", "TRUE", "是");
     public final static String SPILT_CHAR = " | ";
     public final static String DEFAULT_EXCLUDED_WORDS = ".svn" + SPILT_CHAR + "target" + SPILT_CHAR + ".classpath" + SPILT_CHAR + ".project" + SPILT_CHAR + ".settings";
 
     public static boolean isYse(String next) {
         // StringUtils.isBlank(next) ||
         if (YES_LIST.contains(next)) {
             return true;
         }
 
         return false;
     }
 
     public static List<String> initExcludedKeys(String excludedWords) {
         List<String> excludedKeys = new ArrayList<String>();
 
         if (StringUtils.isNotBlank(excludedWords)) {
             excludedKeys.addAll(Arrays.asList(excludedWords.split("\\s" + SPILT_CHAR.trim() + "\\s")));
         }
 
         return excludedKeys;
     }
 
     public static void list(File f, String parent, Map<String, File> map, List<String> excludedKeys) {
         String name = f.getName();
 
         if (parent != null) {
             name = parent + "/" + name;// set zip parent location.
         }
         // is exclude?
         if (!isExcluded(f, excludedKeys)) {
            if (f.isFile()) {
                map.put(name, f);
            } else if (f.isDirectory()) {
                File[] subFiles = f.listFiles();
 
                if (subFiles.length > 0) {
                    for (File file : subFiles) {
                        list(file, name, map, excludedKeys);
                    }
                } else { // empty directory
                    map.put(name, f);
                 }
             }
         }
     }
 
     /**
      * is exclude?
      *
      * @param f
      * @return
      */
     protected static boolean isExcluded(File f, List<String> excludedKeys) {
         for (String key : excludedKeys) {
             if (key.equalsIgnoreCase(f.getName())) {
                 return true;
             }
 
         }
         return false;
     }
 
     /**
      * A common method for all enums since they can't have another base class
      *
      * @param <T>
      *            Enum type
      * @param c
      *            enum type. All enums must be all caps.
      * @param string
      *            case insensitive
      * @return corresponding enum, or null
      */
     public static <T extends Enum<T>> T getEnumFromString(Class<T> c, String string) {
         if (c != null && string != null) {
             try {
                 return Enum.valueOf(c, string.trim().toUpperCase());
             } catch (IllegalArgumentException ex) {
             }
         }
         return null;
     }
 }
