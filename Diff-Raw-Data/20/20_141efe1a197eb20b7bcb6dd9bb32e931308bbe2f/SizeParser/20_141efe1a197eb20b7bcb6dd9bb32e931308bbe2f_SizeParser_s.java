 package com.abudko.reseller.huuto.query.service.list;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class SizeParser {
 
     public static String getSize(String description) {
         Matcher matcher = Pattern.compile("koko\\s*(\\d+)").matcher(description);
         if (matcher.find()) {
             return matcher.group(1);
         }
         matcher = Pattern.compile("(\\d+)\\s*cm").matcher(description);
         if (matcher.find()) {
             return matcher.group(1);
         }
         matcher = Pattern.compile("\\d+").matcher(description);
         String size = null;
         while (matcher.find()) {
            if (isPrice(matcher.group(), description) == false) {
                 size = matcher.group();
             }
         }
         return size;
     }
 
    private static boolean isPrice(String str, String description) {
        Matcher matcher = Pattern.compile("ovh\\.*\\s*(\\d+)").matcher(description);
         if (matcher.find()) {
             return matcher.group(1).equals(str);
         }
 
         return false;
     }
 }
