 package gov.nih.nci.evs.browser.utils;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.util.regex.*;
 
 import javax.servlet.http.*;
 
 public class HTTPUtils {
     public static final String[] EMPTY_PARAMETERS = new String[] {};
     
     public static String getParameter(HttpServletRequest request, String name,
             boolean convertNullToBlankString) {
         String value = request.getParameter(name);
         if (convertNullToBlankString && (value == null || value.length() <= 0))
             return "";
         return cleanXSS(value);
     }
     
     public static String getParameter(HttpServletRequest request, String name) {
         return getParameter(request, name, true);
     }
 
     public static String getAttributeString(HttpServletRequest request,
         String name, boolean convertNullToBlankString, boolean clear) {
         String value = (String) request.getAttribute(name);
         if (convertNullToBlankString && (value == null || value.length() <= 0))
             return "";
         if (clear)
             request.setAttribute(name, null);
         return cleanXSS(value);
     }
 
     public static String getAttributeString(HttpServletRequest request,
         String name) {
         return getAttributeString(request, name, true, false);
     }
 
    public static String getSessionAttributeString(HttpServletRequest request,
        String name, boolean convertNullToBlankString, boolean clear) {
        String value = (String) request.getSession().getAttribute(name);
        if (convertNullToBlankString && (value == null || value.length() <= 0))
            return "";
        if (clear)
            request.setAttribute(name, null);
        return cleanXSS(value);
    }

    public static String getSessionAttributeString(HttpServletRequest request,
        String name) {
        return getSessionAttributeString(request, name, true, false);
    }
    
     public static String cleanXSS(String value) {
         if (value == null || value.length() < 1)
             return value;
 
         try {
             value = URLDecoder.decode(value, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             // Do nothing, just use the input
         }
 
         // Remove XSS attacks
         value = replaceAll(value, "<\\s*script\\s*>.*</\\s*script\\s*>", "");
         value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
         value = value.replaceAll("\\(", "&#40;").replaceAll("\\)", "&#41;");
         value = value.replaceAll("'", "&#39;");
         value = value.replaceAll("eval\\((.*)\\)", "");
         value = replaceAll(value, "[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']",
                 "\"\"");
         value = value.replaceAll("\"", "&quot;");
         return value;
     }
 
     public static String replaceAll(String string, String regex,
             String replaceWith) {
         Pattern myPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
         string = myPattern.matcher(string).replaceAll(replaceWith);
         return string;
     }
     
     public static HashMap<String, String> getParametersHashMap(
         HttpServletRequest request, String[] parameters) {
         HashMap<String, String> hashMap = new HashMap<String, String>();
         for (int i = 0; i < parameters.length; ++i) {
             String key = parameters[i];
             String value = (String) request.getParameter(key);
             if (value == null)
                 value = "[Not Set]";
             hashMap.put(key, value);
         }
         return hashMap;
     }
     
     public static String debugParameters(String text, String[] parameters, 
         HashMap<String, String> parametersHashMap) {
         StringBuffer buffer = new StringBuffer();
         buffer.append(text + "\n");
         for (int i = 0; i < parameters.length; ++i) {
             String parameter = parameters[i];
             buffer.append("  * ");
             buffer.append(parameter + ": ");
             buffer.append(parametersHashMap.get(parameter));
             buffer.append("\n");
         }
         Debug.println(buffer.toString());
         return buffer.toString();
     }
     
     public static void updateSessionAttributes(HttpServletRequest request, 
         String[] parameters, HashMap<String, String> parametersHashMap) {
         for (int i = 0; i < parameters.length; ++i) {
             String parameter = parameters[i];
             request.setAttribute(parameter,
                 parametersHashMap.get(parameter));
         }
     }
     
     public static void clearSessionAttributes(HttpServletRequest request, 
             String[] parameters) {
         for (int i = 0; i < parameters.length; ++i) {
             String parameter = parameters[i];
             request.setAttribute(parameter, null);
         }
     }
 }
