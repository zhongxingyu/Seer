 package com.atlassian.refapp.decorator;
 
 import com.atlassian.plugin.webresource.WebResourceManager;
 
 import java.io.CharArrayWriter;
 
 public class WebResourceIncluder
 {
     private final WebResourceManager webResourceManager;
 
     public WebResourceIncluder(WebResourceManager webResourceManager)
     {
         this.webResourceManager = webResourceManager;
     }
 
     public CharArrayWriter includeResources()
     {
         CharArrayWriter writer = new CharArrayWriter();
         webResourceManager.includeResources(writer);
         return writer;
     }
 }
