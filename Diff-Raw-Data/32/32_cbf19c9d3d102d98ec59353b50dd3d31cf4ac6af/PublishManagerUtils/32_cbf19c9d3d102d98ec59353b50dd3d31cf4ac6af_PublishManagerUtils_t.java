 package com.abudko.scheduled.service.huuto;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.springframework.stereotype.Component;
 
 @Component
 public class PublishManagerUtils {
 
     public String getId(String description) {
        Pattern pattern = Pattern.compile("\\[(.+)\\]");
         Matcher matcher = pattern.matcher(description);
         while (matcher.find()) {
             return matcher.group(1);
         }
         
         return null;
     }
 }
