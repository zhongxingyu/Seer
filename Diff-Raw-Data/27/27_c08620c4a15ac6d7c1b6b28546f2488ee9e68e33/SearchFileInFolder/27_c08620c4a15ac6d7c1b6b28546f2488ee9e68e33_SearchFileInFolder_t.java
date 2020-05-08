 package com.lohika.itkachuk.javatc.lesson3;
 
 import com.lohika.itkachuk.javatc.lesson3.exceptions.FileSearcherException;
 import org.apache.commons.lang3.StringUtils;
 
 import java.io.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: itkachuk
  * Date: 3/11/13 5:00 PM
  */
 public class SearchFileInFolder {
 
     private String targetDirectoryName;
 
     private String fileName;
 
     private long scannedDirectoriesCounter;
 
     public SearchFileInFolder(String targetDirectoryName, String fileName) {
         this.targetDirectoryName = targetDirectoryName;
         this.fileName = fileName;
     }
 
     private void validateInputs() throws IllegalArgumentException {
         if (StringUtils.isEmpty(targetDirectoryName)) {
            throw new IllegalArgumentException("Input error: targetDirectoryName can not be empty");
         }
         if (StringUtils.isEmpty(fileName)) {
            throw new IllegalArgumentException("Input error: fileName can not be empty");
         }
         if (fileName.length() > 255) {
            throw new IllegalArgumentException("Input error: the fileName length can not be more than 255 characters");
         }
         if ((targetDirectoryName.length() + fileName.length()) > 260) {
            throw new IllegalArgumentException("Input error: the total path length can not be more than 260 characters");
         }
 
         File directory = new File(targetDirectoryName);
         if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Input error: \"" + targetDirectoryName + "\" is not a directory");
         }
         if (!directory.canRead()) {
            throw new IllegalArgumentException("Directory \"" + targetDirectoryName + "\" can not be read");
         }
     }
 
     public void searchFileAndPrint() throws FileSearcherException {
        //let's validate user inputs first
        validateInputs();
 
         File file = processDirectoryRecursively(new File(targetDirectoryName), fileName);
         if (file == null) {
             throw new FileSearcherException("The FileSearcher was unable to find the file \""
                     + fileName + "\" in the directory \"" + targetDirectoryName + "\" and all its subdirectories");
         }
 
         System.out.println("Found file at: " + file.getAbsolutePath());
         System.out.println("Scanned directories count: " + scannedDirectoriesCounter);
         System.out.println("Printing file content:\n");
         BufferedReader in = null;
 
         try {
             // print file content to console
             in = new BufferedReader(new FileReader(file));
             String line = in.readLine();
             while (line != null) {
                 System.out.println(line);
                 line = in.readLine();
             }
         } catch (FileNotFoundException e) {
             throw new FileSearcherException("The FileSearcher internal error occurred, caused by FileNotFoundException: " + e.getMessage(), e.getCause());
         } catch (IOException e) {
             throw new FileSearcherException("The FileSearcher internal error occurred, caused by IOException: " + e.getMessage(), e.getCause());
         } finally {
             try {
                 in.close();
             } catch (Exception e) {
                 throw new FileSearcherException("The FileSearcher internal error occurred, caused by Exception: " + e.getMessage(), e.getCause());
             }
 
         }
     }
     //TODO: think about how you can find it without recursion also
     private File processDirectoryRecursively(File directory, String fileName) {
         scannedDirectoriesCounter++;
         // check, if we have target file in current directory
         File file = new File(directory + File.separator + fileName);
         if (file.isFile()) return file;
         else { // if not, iterate over all subdirectories and do the same job
             File[] files = directory.listFiles();
             if (files != null) {
                 for (File subdirectory : files) {
                     if (subdirectory.isDirectory()) {
                         File returnedFile = processDirectoryRecursively(subdirectory, fileName);
                         if (returnedFile != null) return returnedFile;
                     }
                 }
             }
             return null;
         }
     }
 
     public static void main(String[] args){
         if (args.length != 2) {
             System.out.println("Usage: SearchFileInFolder <targetDirectoryName> <fileName>");
             System.exit(1);
         }
 
         try {
             SearchFileInFolder searcher = new SearchFileInFolder(args[0], args[1]);
             searcher.searchFileAndPrint();
         } catch (IllegalArgumentException iae) {
             System.out.println("One or more input arguments are not valid:\n" + iae.getMessage());
         } catch (FileSearcherException fse) {
             System.out.println(fse.getMessage());
         }
     }
 }
