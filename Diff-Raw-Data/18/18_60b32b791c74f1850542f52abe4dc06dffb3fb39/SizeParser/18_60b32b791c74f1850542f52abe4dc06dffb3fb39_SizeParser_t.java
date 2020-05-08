 package com.abudko.reseller.huuto.query.service.list;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class SizeParser {
 
     public static String getSize(String description) {
         description = replace(description);
         Matcher matcher = Pattern.compile("koko\\s*(\\d+)").matcher(description);
         if (matcher.find()) {
             return matcher.group(1);
         }
         matcher = Pattern.compile("((\\d+)\\(*\\d*\\)*)\\s*cm", Pattern.CASE_INSENSITIVE).matcher(description);
         if (matcher.find()) {
             return matcher.group(2);
         }
         matcher = Pattern.compile("\\d+").matcher(description);
         String size = null;
         while (matcher.find()) {
             if (isNotSizeDigit(matcher.group(), description) == false) {
                 size = matcher.group();
             }
         }
         return size;
     }
 
     private static boolean isNotSizeDigit(String str, String description) {
         Matcher matcher = Pattern.compile("[ovh(euroa)]\\.*\\s*(\\d+)", Pattern.CASE_INSENSITIVE).matcher(description);
         
         int i = description.indexOf(str) - 1;
         if (i >= 0 && description.substring(i, i + 1).equals("/")) {
             return true;
         }
         
        int i1 = description.indexOf(str) + str.length();
        if (i1 >= 0 && i1 < description.length()) {  
            String substring = description.substring(i1, i1 + 1);
            if (substring.equals("€")) {
                return true;
            }
        }
        
         if (matcher.find()) {
             return matcher.group(1).equals(str);
         }
 
         return false;
     }
     
     private static String replace(String description) {
         Matcher matcher = Pattern.compile("\\d+,\\d+").matcher(description);
         if (matcher.find()) {
             String group = matcher.group(0);
             return description.replace(group, "");
         }
         
         return description;
     }
 }
