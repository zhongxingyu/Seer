 package weka.subspaceClusterer;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.nio.file.DirectoryStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.Scanner;
 
 public class Consolidator {
 
     public static void main(String[] args) throws IOException {
 
         consolidate(args[0], args[1]);
 
     }
 
     static void consolidate(String path, String consolidatedFilename) throws IOException {
 
         DirectoryStream<Path> stream;
         Scanner scanner;
         PrintWriter writer;
        String fname;

         // Create output file
         // fname = ResultsWriter.separatedPath(path) + name;
         writer = new PrintWriter(new BufferedWriter(new FileWriter(consolidatedFilename)));
 
         // Grab all result files
         stream = Files.newDirectoryStream(Paths.get(path), "RSLT*.csv");
 
         boolean headerIsWritten = false;
 
         // Iterate over result files
         for (Path aPath : stream) {
             File file = aPath.toFile();
             FileInputStream fis = new FileInputStream(file);
             scanner = new Scanner(fis);
 
             // Each file should have exactly two lines: a header and values. Grab them.
             String header = scanner.nextLine();
             String result = scanner.nextLine();
 
             if (!headerIsWritten) {
                 writer.println(header);
                 headerIsWritten = true;
             }// if
 
             // Write data
             writer.println(result);
 
             // Shutdown IO
             scanner.close();
 
         }// for
 
         // Shutdown IO
         writer.close();
 
     }// method
 
 }
