 /*
  * Matt Adelman
  * RSA Final Project
  */
 
 import java.math.BigInteger;
 import java.util.Scanner;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 
 public class FormatNewData {
 
     public static void main(String[] args) {
         
         String[] lines = new String[125723];
 
         try { 
             // Getting the file
             String fileName = "formatted-certs-all.csv";
             Scanner file = new Scanner(new FileReader(fileName));
             // Looping through the file
             for (int i = 0; i < lines.length; i++) {
                 String line = file.nextLine();
                 String add = "";
                 int start = line.indexOf("Modulus");
                 String rest = line.substring(start);
                 start = rest.indexOf(":");
                 rest = rest.substring(start);
                 String modulus = (rest.split(",")[0]).replaceAll(":","");
                 String[] splits = line.split(",");
                 add = add + splits[0] + ",";
                 add = add + splits[2] + ",";
                add = add + modulus + "\n";
                 lines[i] = add;
             }
 
             for (int i = 0; i < lines.length; i++) {
                System.out.print(lines[i]);
             }
 
         }
 
 
 
 		// Catching a FileNotFoundException exception
         catch (FileNotFoundException fnfe) {
             System.out.println("File was not found");
             System.exit(0);
         }
 
     }
 
 }       
