 /*
  *
  */
 
 package simpleWebServer;
  
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 
 class FileExtensionToContentTypeMapper {
 
    public java.util.Hashtable<String,String> extentionsToContent
                 = new java.util.Hashtable<String,String>();
 
     public FileExtensionToContentTypeMapper() {
         fillExtensionsWithContent();
     }
 
     protected void fillExtensionsWithContent() {
         addExtension("", "content/unknown");
         addExtension(".uu", "application/octet-stream");
         addExtension(".exe", "application/octet-stream");
         addExtension(".ps", "application/postscript");
         addExtension(".zip", "application/zip");
         addExtension(".sh", "application/x-shar");
         addExtension(".tar", "application/x-tar");
         addExtension(".snd", "audio/basic");
         addExtension(".au", "audio/basic");
         addExtension(".wav", "audio/x-wav");
         addExtension(".gif", "image/gif");
         addExtension(".jpg", "image/jpeg");
         addExtension(".jpeg", "image/jpeg");
         addExtension(".htm", "text/html");
         addExtension(".html", "text/html");
         addExtension(".text", "text/plain");
         addExtension(".c", "text/plain");
         addExtension(".cc", "text/plain");
         addExtension(".c++", "text/plain");
         addExtension(".h", "text/plain");
         addExtension(".pl", "text/plain");
         addExtension(".txt", "text/plain");
         addExtension(".java", "text/plain");
     }
 
     protected void addExtension(String extension, String content) {
        extentionsToContent.put(extension, content);
     }
 }
 
