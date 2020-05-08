 import java.io.*;
 import java.util.*;
 
 public class DanielIndexer {
 
     private File input;
     private BufferedWriter writer;
     private BufferedReader reader;
 
     public DanielIndexer() throws Exception {
         reader = new BufferedReader(new InputStreamReader(System.in));
     }
 
     public void getInput() throws Exception {
         for (int count = 0; !(input = getInputFile(count++)).exists(); );
         if (!input.isDirectory()) {
             printError("Input is not a folder!");
             getInput();
         } else {
             System.out.println("You are good to go!");
         }
     }
 
     private File getInputFile(int count) throws Exception {
         if (count > 0) {
             printError("Folder does not exists!");
         }
         System.out.println("Choose what folder to index!");
         return new File(reader.readLine());
     }
 
     public void getOutput() throws Exception {
         writer = new BufferedWriter(new FileWriter(getOutputFile()));
         System.out.println("You are good to go!");
     }
 
     private File getOutputFile() throws Exception {
         System.out.println("Choose where to put the index file!");
         File temp = new File(reader.readLine());
         if (temp.exists()) {
             System.out.println("\nOutput file already exists, do you want to overwrite? (Y/N)");
             if (!reader.readLine().equalsIgnoreCase("Y")) {
                 return getOutputFile();
             }
         }
         return temp;
     }
 
     public void executeIndex() throws Exception {
         indexFolder(input);
         writer.close();
     }
 
     private void indexFolder(File folder) throws Exception {
         for (File file : folder.listFiles()) {
             if (file.isDirectory()) {
                 indexFolder(file);
             } else {
                writer.write(file.getAbsolutePath().substring(input.getAbsolutePath().length() + 1) + "\n");
             }
         }
     }
 
     private void printError(String message) {
         System.out.println("\nERROR!");
         System.out.println(message + "\n");
     }
 }
