 package org.zeroturnaround.example.p2.spells;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 public class TxtFile {
 
   private final File file = new File(System.getProperty("user.home"), "org.zeroturnaround.example.p2.txt");
 
   public static TxtFile get() {
     return new TxtFile();
   }
 
   public String path() {
     return file.getPath();
   }
 
   public void appendLine(String line) throws IOException {
     FileWriter out = new FileWriter(file, true);
    out.append(line + "\n");
     out.flush();
     out.close();
   }
 
   public void delete() {
     file.delete();
   }
 
 }
