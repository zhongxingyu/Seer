 import java.io.*;
 import java.util.*;
 
 public class DanielIndexer {
 
     private File input;
     private File output;
     private BufferedWriter writer;
     private BufferedReader reader;
 
     public DanielIndexer() throws Exception {
         reader = new BufferedReader(new InputStreamReader(System.in));
     }
 
     public void getInput() throws Exception {
         int count = 0;
         while (!(this.input = getInputFile(count++)).exists());
         if (!this.input.isDirectory()) {
            printError("Input is not a folder!");
             getInput();
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
         System.out.println("Choose where to put the index file!");
         this.output = new File(reader.readLine());
         if (this.output.exists()) {
            System.out.println("\nOutput file already exists, do you want to overwrite? (Y/N)");
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
                 writer.write(file.getAbsolutePath().substring(input.getAbsolutePath().length() + 1) + "\n");
             }
         }
     }

    private void printError(String message) {
        System.out.println("\nERROR!");
        System.out.println(message + "\n");
    }
 }
