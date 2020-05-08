 package com.vance.quest2012;
 
 /**
  * Application class for utility to retrieve web content.
  *
  * @author srvance
  */
 public class WebRetriever {
     private String target;
     private String protocol;
 
     public WebRetriever(String target) {
         this.target = target;
     }
 
     public String getTarget() {
         return target;
     }
 
     public String getProtocol() {
         return protocol;
     }
 }
