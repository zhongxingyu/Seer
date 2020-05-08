 package com.sun.identity.admin;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
 import java.util.Collection;
 import java.util.List;
 
 public class Functions {
     public static String indexOf(List l, Object o) {
         return Integer.toString(l.indexOf(o));
     }
 
     public static String truncate(String s, int length) {
         if (length >= s.length()) {
             return s;
         }
         return s.substring(0, length-1)+"...";
     }
 
     public static int size(Collection c) {
         if (c == null) {
             return 0;
         }
         return c.size();
     }
 
     public static int length(Object[] oa) {
         return oa.length;
     }
 
     public static int length(String s) {
         return s.length();
     }

    public static String scrape(String url) {
        try {
            URL u = new URL(url);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                    u.openStream()));

            String inputLine;
            StringBuffer b = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                b.append(inputLine);
            }

            in.close();
            return b.toString();
        } catch (IOException ioe) {
            return AdminResourceBundle.getString("scrapeError", url, ioe);
        }

    }
 }
