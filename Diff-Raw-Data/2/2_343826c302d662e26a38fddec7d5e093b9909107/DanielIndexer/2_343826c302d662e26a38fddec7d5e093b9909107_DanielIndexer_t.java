 import java.io.*;
 import java.util.*;
 
 public class DanielIndexer {
 
     private File input;
     private File output;
     private BufferedWriter writer;
     private BufferedReader reader;
     private final static String ERROR = "ERROR!";
 
     public DanielIndexer() throws Exception {
         reader = new BufferedReader(new InputStreamReader(System.in));
     }
 
     public void getInput() throws Exception {
         int count = 0;
         while (!(this.input = getInputFile(count++)).exists());
         if (!this.input.isDirectory()) {
             System.out.println(ERROR);
             System.out.println("Input is not a folder!");
             getInput();
         }
     }
 
     private File getInputFile(int count) throws Exception {
         if (count > 0) {
             System.out.println(ERROR);
             System.out.println("Folder does not exists!");
         }
         System.out.println("Choose what folder to index!");
         return new File(reader.readLine());
     }
 
     public void getOutput() throws Exception {
         System.out.println("Choose where to put the index file!");
         this.output = new File(reader.readLine());
         if (this.output.exists()) {
             System.out.println("Output file already exists, do you want to overwrite? (Y/N)");
             String response = reader.readLine();
             if (!response.equalsIgnoreCase("Y")) {
                 getOutput();
             }
         }
         writer = new BufferedWriter(new FileWriter(this.output));
     }
 
     public void executeIndex() throws Exception {
         indexFolder(input);
         writer.close();
     }
 
     private void indexFolder(File folder) throws Exception {
         File[] files = folder.listFiles();
         for (File file : files) {
             if (file.isDirectory()) {
                 indexFolder(file);
             } else {
                 String filename = file.getAbsolutePath();
                filename = filename.substring(input.getAbsolutePath().length());
                 writer.write(filename + "\n");
             }
         }
     }
 }
