 package evolution.binPacking;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Scanner;
 
 public class Checker {
 
     public static void main(String[] args) {
 
         try {
             BufferedReader in = new BufferedReader(new FileReader(args[0]));
 
             double[] vahy = new double[10];
 
             String line;
             while ((line = in.readLine()) != null) {
                String[] nums = line.split(" ");
 
                double weight = Double.parseDouble(nums[0]);
                int bin = Integer.parseInt(nums[1]);
 
                 vahy[bin] += weight;
             }
 
             double min = Integer.MAX_VALUE;
             double max = Integer.MIN_VALUE;
 
             for (int i = 0; i < 10; i++) {
                 min = Math.min(vahy[i], min);
                 max = Math.max(vahy[i], max);
 
                 System.out.println("" + i + ": " + vahy[i]);
             }
 
             System.out.println("difference: " + (max - min));
            in.close();
 
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
 
     }
 }
