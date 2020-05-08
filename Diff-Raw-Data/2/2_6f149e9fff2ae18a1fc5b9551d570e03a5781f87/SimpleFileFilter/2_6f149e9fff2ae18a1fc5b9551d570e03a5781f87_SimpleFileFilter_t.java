 package org.makumba.parade.tools;
 
 import java.io.File;
 import java.io.FileFilter;
 
 public class SimpleFileFilter implements FileFilter {
     public boolean accept(File f) {
         String name = f.getName();
         if (name.endsWith("~") || name.endsWith(".class") || name.endsWith(".save"))
             return false;
        if (f.isDirectory() && (name.indexOf(("CVS")) > -1 || name.equals("serialized")))
             return false;
         if ((name.equals("work") || name.equals("logs")) && f.isDirectory()
                 && f.getParentFile().getName().startsWith("tomcat"))
             return false;
         if(name.startsWith("_new_"))
             return false;
         if (f.getParentFile().getName().equals("CVS"))
             return false;
         if (name.endsWith(".unison.tmp"))
             return false;
         //TODO this should be customisable, parade-wide in parade.properties
         if(name.equals("PageCount.txt"))
             return false;
         
         
         return true;
     }
 }
