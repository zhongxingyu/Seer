 package com.thefind.util;
 
 import java.io.*;
 import java.util.*;
 
 /**
  * This class contains utility functions for File-related operations
  *
  * @author Eric Gaudet
  */
 public class IOUtil
 {
   public final static long KB = 1024;
   public final static long MB = 1024 * KB;
   public final static long GB = 1024 * MB;
   public final static long TB = 1024 * GB;
 
   public static boolean deleteDirectory(String path)
   { return deleteDirectory(new File(path)); }
 
   public static boolean deleteDirectory(File path)
   {
     if (path.isDirectory()) {
       for (File file : path.listFiles()) {
         deleteDirectory(file);
       }
     }
     return path.delete();
   }
 
   public static boolean rename(String was_name, String new_name)
   {
     File was_file = new File(was_name);
     File new_file = new File(new_name);
     System.err.println("[IOUtils] renaming: "+was_file.getAbsolutePath()+" to: "+new_file.getAbsolutePath());
     return was_file.renameTo(new_file);
   }
 
   public static void replace(String was_name, String new_name, String bak_name)
   {
     System.err.println("[IOUtils] deleting: "+bak_name);
     deleteDirectory(bak_name);
     rename(new_name, bak_name);
     rename(was_name, new_name);
   }
 
   public static void replace(String was_name, String new_name)
   {
     System.err.println("[IOUtils] deleting: "+new_name);
     deleteDirectory(new_name);
     rename(was_name, new_name);
   }
 
   public static boolean delete(String name)
   {
     File was_file = new File(name);
     System.err.println("[IOUtils] deleting: "+was_file.getAbsolutePath());
     return was_file.delete();
   }
 
   public static void assertDir(String path, boolean present)
   {
     if (present) {
       if (!new File(path).isDirectory()) {
         throw new IllegalStateException("Directory does not exist: "+path);
       }
     }
     else {
       if (!new File(path).isDirectory()) {
         throw new IllegalStateException("Directory already exists: "+path);
       }
     }
   }
 
   public static void assertFile(String path, boolean present)
   {
     if (present) {
       if (!new File(path).isFile()) {
         throw new IllegalStateException("File does not exist: "+path);
       }
     }
     else {
      if (new File(path).isFile()) {
         throw new IllegalStateException("File already exists: "+path);
       }
     }
   }
 
   public static void assertSpace(String path, long min_size)
   {
     long free = new File(path).getUsableSpace();
     if (free==0L) {
       try {
         String fullpath = new File(path).getCanonicalPath();
         while (free==0L && !fullpath.isEmpty()) {
           fullpath = fullpath.substring(0, fullpath.lastIndexOf('/'));
           free = new File(fullpath).getUsableSpace();
         }
       }
       catch (IOException ioex) {
         throw new IllegalStateException(ioex.toString());
       }
     }
     if (free<min_size) {
       throw new IllegalStateException("Not enough space in "+path+": "+String.format("%,d", free));
     }
   }
 
   public static List<String> readFileLines(String fileName)
   throws FileNotFoundException, IOException
   {
     List<String> result = new ArrayList();
 
     BufferedReader br = new BufferedReader(new FileReader(fileName));
     String line = null;
     while ((line = br.readLine()) != null) {
       result.add(line);
     }
 
     return result;
   }
 }
 
