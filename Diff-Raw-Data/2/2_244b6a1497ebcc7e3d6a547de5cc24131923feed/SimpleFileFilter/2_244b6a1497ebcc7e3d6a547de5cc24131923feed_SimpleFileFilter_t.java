 package org.makumba.parade.tools;
 
 import java.io.File;
 import java.io.FileFilter;
 
 public class SimpleFileFilter implements FileFilter {
     public boolean accept(File f) {
         String name = f.getName();
         
         // if this is a temporary file or a class
         if (name.endsWith("~") || name.endsWith(".class") || name.endsWith(".save"))
             return false;
         
         // if this is a CVS directory
        if (f.isDirectory() && (name.trim().equals("CVS")))
             return false;
         
         // if this is a CVS information file
         if (f.getParentFile().getName().equals("CVS"))
             return false;
         
         // if this is a serialized file
         if(name.endsWith("/serialized") || name.equals("serialized"))
             return false;
         
         // if this is a tomcat directory
         if ((name.equals("work") || name.equals("logs")) && f.isDirectory()
                 && f.getParentFile().getName().startsWith("tomcat"))
             return false;
         
         // if this is an internal parade file
         if(name.startsWith("_new_"))
             return false;
         
         // if this is a temporary unison file
         if (name.endsWith(".unison.tmp"))
             return false;
         
         //TODO this should be customisable, parade-wide in parade.properties
         if(name.equals("PageCount.txt"))
             return false;
         
         return true;
     }
 }
