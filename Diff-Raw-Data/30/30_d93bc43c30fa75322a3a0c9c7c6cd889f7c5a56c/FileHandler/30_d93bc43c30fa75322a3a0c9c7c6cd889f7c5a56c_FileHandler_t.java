 package net.daboross.engine;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
 * This class contains various helpful IO functions.
  */
 public final class FileHandler {
 
     /**
      * Writes a file with text lines to filePath/fileName
      *
      * @param file this is the file to write to
      * @param lines this is the text to put in the file
      * @return True if successful, false otherwise
      */
     public static boolean WriteFile(File file, List<String> lines) {
         if (lines == null || file == null) {
             return false;
         }
         printErrF("File Handler: Writing To File: " + file.toString());
         try {
             if (file.canWrite()) {
                 boolean madeDirectory = file.getParentFile().mkdirs();
                 if (madeDirectory) {
                     FileHandler.printErrF("File Handler: Made Directory");
                 }
                 if (!file.exists()) {
                     boolean madeFile = file.createNewFile();
                     if (madeFile) {
                         printErrF("File Handler: Made New File");
                     }
                 }
                 BufferedWriter bf = new BufferedWriter(new FileWriter(file));
                 for (int i = 0; i < lines.size(); i++) {
                     bf.write(lines.get(i));
                     bf.newLine();
                 }
                 bf.close();
             } else {
                 return false;
             }
         } catch (Exception e) {
             return false;
         }
         printErrF("File Handler: Finished Writing To File");
         return true;
     }
 
     /**
      * Reads a file into an array list of strings
      *
      * @param file this is the file to read from
      * @return The text in the file, or null if it doesn't exist
      */
     public static List<String> ReadFile(File file) {
         printErrF("File Handler: Reading File: " + file.toString());
         List<String> lines = new ArrayList<String>();
         if (file.canRead()) {
             try {
                 FileReader fr = new FileReader(file);
                 BufferedReader bf = new BufferedReader(fr);
                 while (true) {
                     String line = bf.readLine();
                     if (line == null) {
                         break;
                     }
                     lines.add(line);
                 }
                 bf.close();
             } catch (Exception e) {
                 printErrF("File Handler: Exception in reading File! : \n");
             }
             printErrF("File Handler: Finished Reading File");
         } else {
             printErrF("File Handler: Can't Read File");
         }
 
         return lines;
     }
 
     /**
      * This reads a file from within the .jar to an ArrayList of strings
      *
      * @param filePath this is the location of the file within the jar archive.
      * starts with a /
      * @return an ArrayList of strings that is the text of the file
      */
    public static List<String> ReadInternalFile(final String filePath) {
        String path = filePath.startsWith("/") ? filePath : "/" + filePath;
        printErrF("File Handler: Reading Internal File: " + path);
         List<String> lines = new ArrayList<String>();
        InputStream is = FileHandler.class.getResourceAsStream(path);
         if (is != null) {
             try {
                 InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader bf = new BufferedReader(isr);
                 while (true) {
                     String line = bf.readLine();
                     if (line == null) {
                         break;
                     }
                     lines.add(line);
                 }
                 bf.close();
             } catch (Exception e) {
                 printErrF("File Handler: Exception In Reading File!\n");
             }
             try {
                 is.close();
             } catch (IOException e) {
                 // Don't do anything with this exception
             }
         } else {
             printErrF("File Handler: Input Stream Reader Get Failed");
         }
         printErrF("File Handler: Finished Reading Internal File: " + filePath);
         return lines;
     }

     public static void printErrF(String str) {
        System.out.printf(str + "\n");
     }
 }
