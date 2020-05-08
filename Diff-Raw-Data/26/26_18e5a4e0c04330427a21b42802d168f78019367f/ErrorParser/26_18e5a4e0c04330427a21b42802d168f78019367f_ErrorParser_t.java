 package com.globalsight.tools;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 // The errors we return are not even valid XML, so this uses regexes.
 public class ErrorParser {
 
     static final String S_FAILED = "Failed";
     static final String E_TOKEN = 
         "The security information passed to the web service is not consistent.";
     public Error parse(String errorXml) {
        String status = null, error = null;
         
         Matcher m = Pattern.compile("<status>(.*)</status>").matcher(errorXml);
         if (m.find()) {
             status = m.group(1);
         }
         Matcher m2 = Pattern.compile("<error>(.*)</error>").matcher(errorXml);
         if (m2.find()) {
             error = m2.group(1);
         }
        if (status == null) {
            status = "Unknown";
        }
        if (error == null) {
            error = errorXml;
        }
         
         if (S_FAILED.equals(status) && E_TOKEN.equals(error)) {
             return new InvalidTokenError(status, error);
         }
         return new Error(status, error);
     }
 }
