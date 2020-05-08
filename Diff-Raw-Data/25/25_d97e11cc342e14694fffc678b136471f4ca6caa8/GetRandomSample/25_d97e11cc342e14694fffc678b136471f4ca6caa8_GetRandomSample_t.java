 /*
  * Matt Adelman
  * RSA Final Project
  */
 
 import java.util.Scanner;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.HashSet;
 import java.util.Random;
 
 public class GetRandomSample {
 
 
     public static void main(String[] args) {
         
         int count = 0;
         int samples = 10000;
 
         String[] allLines = new String[125723];
 
         String[] sample = new String[10000];
         String[] modArr = new String[allLines.length];
         Random rand = new Random();
         HashSet mods = new HashSet(20000);
 
         try { 
             // Getting the file
             String fileName = "new-form-data.csv";
             Scanner file = new Scanner(new FileReader(fileName));
             // Looping through the file
             for (int i = 0; i < allLines.length; i++) {
                 String line = file.nextLine();
                allLines[i] = line + "\n";
                 modArr[i] = line.split(",")[2];
             }
         }
         
         catch (FileNotFoundException fnfe) {
             System.out.println("File was not found");
             System.exit(0);
         }
 
         while (count < samples) {
             int index = rand.nextInt(allLines.length);
             if (modArr[index].length() > 10 && mods.add(modArr[index])) {
                 sample[count] = allLines[index];
                 count++;
             }
         }
         
         for (int i = 0; i < sample.length; i++) {
            System.out.print(sample[i]);
         }
 
     }
 
 }
