 /*
  * Matt Adelman
  * RSA Final Project
  */
 
 import java.math.BigInteger;
 import java.util.Scanner;
 import java.io.FileNotFoundException;
 import java.lang.NumberFormatException;
 import java.lang.StringIndexOutOfBoundsException;
 import java.io.FileReader;
 import java.lang.Long;
 import java.util.StringTokenizer;
 
 
 public class NewCertsParser {
 
     public static void main(String[] args) {
 
         int count1 = 0;
         int count2 = 0;
         // Public moduli
         BigInteger[] keys1 = new BigInteger[10000];
         BigInteger[] keys2 = new BigInteger[125723];
         // associated data
         String[] data1 = new String[keys1.length];
         String[] data2 = new String[keys2.length];        
         BigInteger big;
 
         try {
             // Getting the file
             // Change each time
             String fileName1 = "formaa";
             Scanner file1 = new Scanner(new FileReader(fileName1));
             String fileName2 = "form-new-data.csv";
             Scanner file2 = new Scanner(new FileReader(fileName2));
             // Looping through the file
             for (int i = 0; i < keys1.length; i++) {
                 // Getting the line of the file with pertitant info
                 String line = file1.nextLine();
                 // Break the line, because it's a CSV
                 //
                 
                 String[] lineScan = line.split(",");
                 data1[i] = lineScan[0]; //id number
                 String modulus = lineScan[1].replace(":", "");
                 big = new BigInteger(modulus, 16);
                 keys1[count1] = big;
                 count1++;
             }
             for (int i = 0; i < keys2.length; i++) {
                 // Getting the line of the file with pertitant info
                 String line = file2.nextLine();
                 // Break the line, because it's a CSV
                 //
                 
                 String[] lineScan = line.split(",");
                 data2[i] = lineScan[0]; //id number
                 String modulus = lineScan[1].replace(":", "");
                 big = new BigInteger(modulus, 16);
                 keys2[count2] = big;
                 count2++;
             }
             System.out.println("Done!");
             // Timing
             Long startTime = new Long(System.currentTimeMillis());
             // Pairwise GCD of moduli
             for (int i = 0; i < count1; i++) {
               System.out.println("Working on i = " + i);
                for (int j = 0; j < count2; j++) {
                     BigInteger gcd = keys1[i].gcd(keys2[j]);
                     BigInteger one = BigInteger.ONE;
                     // If the Moduli are equal we get no useful info
                     if (!(one.equals(gcd)) && !(keys1[i].equals(keys2[j]))) {
                         System.out.println(data1[i] + ", " + data2[j] + 
                         ", " + gcd + gcd.isProbablePrime(80));
                     }
                 }
             }
             
 		    Long endTime = new Long(System.currentTimeMillis());
 
 		    //System.out.println(endTime.intValue() - startTime.intValue());
             //System.out.println(count);
             file1.close();
             file2.close();
         }
 
 		// Catching a FileNotFoundException exception
         catch (FileNotFoundException fnfe) {
             System.out.println("File was not found");
             System.exit(0);
         }
         catch (NumberFormatException nfe) {
             System.out.println(count1 + 1);
             big = BigInteger.ONE;
         }
         catch (StringIndexOutOfBoundsException sioobe) {
             System.out.println(count1 +1);
             System.out.println("out of bounds");
         }
 
 
 
     }
 
 
 }
