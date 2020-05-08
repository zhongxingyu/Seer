 package project.phase2.file;
 
 import project.phase2.structs.StringMatchList;
 import project.phase2.structs.StringMatchTuple;
 
 import java.io.*;
 import java.util.*;
 
 /**
  * File editing tool.
  */
 public class FileIO {
 
     private FileIO() {
     }
 
     public static void writeFile(final File dest, final String toWrite) throws IOException {
         BufferedWriter out = new BufferedWriter(new FileWriter(dest));
         out.write(toWrite);
         out.close();
     }
 
     /**
      * Convert an entire file to a string.
      *
      * @param file the file
      * @return the string representing the file
      * @throws IOException file reading has been blocked
      */
     public static String readEntireFile(final File file) throws IOException {
         StringBuffer fileData = new StringBuffer(1000);
         BufferedReader reader = new BufferedReader(
                 new FileReader(file));
         char[] buf = new char[1024];
         int numRead = 0;
         while ((numRead = reader.read(buf)) != -1) {
             String readData = String.valueOf(buf, 0, numRead);
             fileData.append(readData);
             buf = new char[1024];
         }
         reader.close();
         return fileData.toString();
     }
 
     public static List<String> readEntireFileIntoLines(final File file) throws IOException {
        return Arrays.asList(readEntireFile(file).split("\n"));
     }
 }
