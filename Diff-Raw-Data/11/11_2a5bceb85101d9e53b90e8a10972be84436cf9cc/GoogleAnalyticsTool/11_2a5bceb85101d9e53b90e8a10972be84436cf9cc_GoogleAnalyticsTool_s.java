 package org.otherobjects.cms.tools;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 import org.otherobjects.cms.views.Tool;
 import org.springframework.stereotype.Component;
 
 /**
  * @author rich
  */
 @Component
 @Tool
 public class GoogleAnalyticsTool
 {
     private static final Pattern externalLinkPattern = Pattern.compile("http://([^/]*)\\/?([^?]*)(.*)");
 
     public String getPath(String url)
     {
         // Remove any query string
 
         if (url.startsWith("mailto"))
         {
             // Email links
             url = StringUtils.substringBeforeLast(url, "?");
             String account = url.substring("mailto:".length(), url.indexOf("@"));
             String domain = url.substring(url.indexOf("@") + 1);
             return "/outgoing/mailto/" + domain + "/" + account;
         }
         else
         {
             // External links
             Matcher m = externalLinkPattern.matcher(url);
             if (m.matches())
                 return "/outgoing/" + m.group(1) + "/" + m.group(2);
         }
        return null;
     }
 }
