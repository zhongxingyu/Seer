 package com.harcourtprogramming.utils;
 
 import java.io.File;
 
 /**
  * File Untilites funtions
  * @author Benedict Harcourt
  */
 public final class FileUtils
 {
 
  public static String realtivePath(File base, File target)
   {
     if (!base.isDirectory())
     {
      base = base.getParentFile();
     }
 
    String[] fromPath = explodedPath(base);
    String[] toPath = explodedPath(target);
 
    int common;
     int i;
 
     for (i = 0; i < Math.min(fromPath.length, toPath.length); i++)
     {
       if (!fromPath[i].equals(toPath[i]))
       {
         break;
       }
     }
 
     common = i;
 
     if (i == 0)
     {
       return target.getPath();
     }
 
     StringBuilder ret = new StringBuilder();
 
     for (i = common; i < fromPath.length; i++)
     {
       ret.append("..");
       ret.append(File.separatorChar);
     }
 
     for (i = common; i < toPath.length; i++)
     {
       ret.append(toPath[i]);
       ret.append(File.separatorChar);
     }
 
     ret.deleteCharAt(ret.length() - 1);
     return ret.toString();
 
   }
 
   public static String[] explodedPath(File f)
   {
     if (File.separatorChar == '\\')
     {
       return f.getPath().split("\\\\"); // Because I hate you java and you lake of split on char or string.
     }
     return f.getPath().split(File.separator);
   }
 
   private FileUtils()
   {
     // Nothing to see here. Move along, citizen.
   }
 
 }
