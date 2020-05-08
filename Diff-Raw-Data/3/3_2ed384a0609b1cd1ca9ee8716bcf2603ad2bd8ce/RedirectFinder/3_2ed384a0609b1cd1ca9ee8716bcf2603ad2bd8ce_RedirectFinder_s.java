 package com.d2fn.jester.rewrite;
 
 import com.google.common.base.Strings;
 
 import javax.ws.rs.core.HttpHeaders;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 public class RedirectFinder {
     public String findRedirect(String link) throws Exception {
        if (!link.startsWith("http://")) {
             link = "http://" + link;
         }
         final URL url = new URL(link);
         final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
         connection.setInstanceFollowRedirects(false);
 
         if (connection.getResponseCode() == 301) {
             final String location = connection.getHeaderField(HttpHeaders.LOCATION);
             if (!Strings.isNullOrEmpty(location)) {
                 return location;
             }
         }
         return null;
     }
 }
