 package com.vance.quest2012;
 
 /**
  * Application class for utility to retrieve web content.
  *
  * @author srvance
  */
 public class WebRetriever {
     private String target;
     private String protocol;
     private String host;
 
     public WebRetriever(String target) {
         this.target = target;
         String[] components = target.split(":", 2);
         protocol = components[0];
        host = components[1];
     }
 
     public String getTarget() {
         return target;
     }
 
     public String getProtocol() {
         return protocol;
     }
 
     public String getHost() {
         return host;
     }
 }
