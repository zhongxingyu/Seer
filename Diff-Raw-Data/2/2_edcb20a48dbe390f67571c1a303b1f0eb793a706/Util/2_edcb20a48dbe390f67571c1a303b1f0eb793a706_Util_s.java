 package com.id.util;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.id.platform.FileSystem;
 
 public class Util {
   public static List<String> readFile(String filename) {
     try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("resources/diff")));
       List<String> result = new ArrayList<String>();
       String line;
       while ((line = reader.readLine()) != null) {
         result.add(line);
       }
       reader.close();
       return result;
     } catch (IOException e) {
       e.printStackTrace();
     }
     return null;
   }
 
   public static List<String> exec(String command) {
     try {
       Process process = Runtime.getRuntime().exec(command);
       BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
       List<String> result = new ArrayList<String>();
       String line;
       while ((line = br.readLine()) != null) {
         result.add(line);
       }
       return result;
     } catch (IOException e) {
       e.printStackTrace();
     }
     return null;
   }
 
   public interface FileWalker {
     void visit(String filename);
   }
 
   public static void walkFiles(
       FileSystem fileSystem, String root, FileWalker walker) {
     List<String> toWalk = new ArrayList<String>();
     for (String subDirectory : fileSystem.getSubdirectories(root)) {
       String file = root + "/" + subDirectory;
       if (fileSystem.isFile(file)) {
         if (file.endsWith(".class")) {
           continue;
         }
         walker.visit(StringUtils.normalizePath(file));
       } else if (fileSystem.isDirectory(file)) {
         if (getBasename(file).startsWith(".")) {
           continue;
         }
         toWalk.add(file);
       }
     }
     for (String subRoot : toWalk) {
       walkFiles(fileSystem, subRoot, walker);
     }
   }
 
   private static String getBasename(String filename) {
     String[] parts = filename.split("/");
     return parts[parts.length - 1];
   }
 }
