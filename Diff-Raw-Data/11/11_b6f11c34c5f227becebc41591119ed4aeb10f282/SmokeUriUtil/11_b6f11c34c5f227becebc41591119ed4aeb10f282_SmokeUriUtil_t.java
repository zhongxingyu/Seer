 package org.selenium.base;
 
 import org.selenium.spring.SpringContext;
 
 public class SmokeUriUtil {
 
     private SmokeUriUtil() {
         throw new AssertionError();
     }
 
     // API
 
     public static String getBaseUri() {
         return getRootUri() + get("http.web.path");
     }
 
     public static String getRootUri() {
        return get("http.protocol") + "://" + get("http.host") + getPort();
     }
 
     // util
 
    static String getPort() {
        final String port = get("http.port");
        if (port.equals("80")) {
            return "";
        }
        return ":" + port;
    }

     static String get(final String key) {
         return SpringContext.context().getEnvironment().getProperty(key);
     }
 
 }
