 package gov.nih.nci.evs.browser.utils;
 
 import java.util.*;
 
 import javax.servlet.http.HttpServletRequest;
 
 public class WebUtils {
     public static void debugHeaders(HttpServletRequest request) {
         ArrayList<String> list = getHeaders(request);
         list = ListUtils.sort(list);
         ListUtils.debugList("Request Headers", list);
     }
     
    private static ArrayList<String> getHeaders(HttpServletRequest request) {
         Enumeration<?> enumeration = request.getHeaderNames();
         ArrayList<String> list = new ArrayList<String>();
         while (enumeration.hasMoreElements()) {
             String name = (String) enumeration.nextElement();
             String value = request.getHeader(name);
             list.add(name + ": " + value);
         }
         return list;
     }
 }
