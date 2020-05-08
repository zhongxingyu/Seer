 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package speedTests;
 
 import java.util.Scanner;
 import matrix.Matrix;
 
 /**
  *
  * @author Lasse
  */
 public class StrassenComparison {
 
     private Scanner scanner;
 
     public StrassenComparison() {
         scanner = new Scanner(System.in);
     }
 
     public void run() {
 
         System.out.println();
         System.out.println("Entered matrix multiplication comparison.");
         System.out.println("This program will compare Strassen's algorithm "
                 + "for matrix multiplication with the standard method.");
 
         chooseComparison();
 
     }
 
     private void chooseComparison() {
         String input;
         while (true) {
             System.out.println();
             System.out.println("Select comparison method.");
             System.out.println("1: Custom comparison");
             System.out.println("2: Default comparison");
             System.out.println("0: Quit comparison program");
             System.out.println();
 
             System.out.println("Enter selection:");
             System.out.print(">> ");
 
             input = scanner.nextLine();
 
             switch (input) {
                 case "1":
                     runCustomComparison();
                     continue;
                 case "2":
                     runDefaultComparison();
                     continue;
                 case "0":
                     return;
                 default:
                     System.out.println("Unrecognised command: '" + input + "'");
             }
         }
     }
 
     private void runDefaultComparison() {
         compareMultiplicationMultiplicative(64, 1024, 2, 1);
     }
 
     private void runCustomComparison() {
         int minSize, maxSize, dSize, threshold;
 
         while (true) {
             try {
                 System.out.println();
 
                 System.out.println("Enter minimum matrix size:");
                 System.out.print(">> ");
                 minSize = Integer.parseInt(scanner.nextLine());
 
                 if (minSize < 1) {
                     reportInvalidSelection("Size must be a positive integer!");
                     continue;
                 }
 
                 System.out.println("Enter maximum matrix size:");
                 System.out.print(">> ");
                 maxSize = Integer.parseInt(scanner.nextLine());
 
                 if (maxSize < minSize) {
                     reportInvalidSelection("Maximum size must be at least minimum size!");
                     continue;
                 }
 
                 System.out.println("Enter step to increase matrix size:");
                 System.out.print(">> ");
                 dSize = Integer.parseInt(scanner.nextLine());
 
                 if (dSize < 1) {
                     reportInvalidSelection("Step must be a positive integer!");
                     continue;
                 }
 
                 System.out.println("Enter threshold for Strassen's algorithm: (1 for pure Strassen)");
                 System.out.print(">> ");
                 threshold = Integer.parseInt(scanner.nextLine());
 
                 if (threshold < 1) {
                     reportInvalidSelection("Threshold must be a positive integer!");
                     continue;
                 }
 
                 compareMultiplicationAdditive(minSize, maxSize, dSize, threshold);
 
                 if (!wantsNewComparison()) {
                     return;
                 }
 
             } catch (NumberFormatException e) {
                 System.out.println("Invalid input!");
                 continue;
             }
         }
     }
 
     private void reportInvalidSelection(String message) {
         System.out.println("Invalid selection!");
         System.out.println(message);
     }
 
     private boolean wantsNewComparison() {
         System.out.println();
         String input;
         while (true) {
             System.out.println("Do you want to run another comparison? (Y/N)");
            System.out.print(">> ");
             input = scanner.nextLine();
             if (input.equalsIgnoreCase("Y")) {
                 return true;
             }
             if (input.equalsIgnoreCase("N")) {
                 return false;
             }
             System.out.println("Invalid input!");
         }
     }
 
     private void compareMultiplicationAdditive(int minSize, int maxSize, int dSize, int threshold) {
         System.out.println();
 
         Matrix a;
         long startNaive, startStrassen, endNaive, endStrassen;
         double timeNaive, timeStrassen;
         System.out.format("%-6s%-12s%-16s%-12s\n", "n:  ", "Naive (s):", "Strassen (s):", "ratio:");
         for (int i = minSize; i <= maxSize; i += dSize) {
             a = Matrix.rand(i);
 
             System.out.format("%-6d", i);
 
             startNaive = System.currentTimeMillis();
             a.mulNaive(a);
             endNaive = System.currentTimeMillis();
             timeNaive = (endNaive - startNaive) * 1.0 / 1000;
 
             System.out.format("%-12.4f", timeNaive);
 
             startStrassen = System.currentTimeMillis();
             a.mulStrassen(a, threshold);
             endStrassen = System.currentTimeMillis();
             timeStrassen = (endStrassen - startStrassen) * 1.0 / 1000;
 
             System.out.format("%-16.4f", timeStrassen);
 
             System.out.format("%-12.4f\n", timeStrassen / timeNaive);
         }
     }
 
     private void compareMultiplicationMultiplicative(int minSize, int maxSize, int dSize, int threshold) {
         System.out.println();
 
         Matrix a;
         long startNaive, startStrassen, endNaive, endStrassen;
         double timeNaive, timeStrassen;
         System.out.format("%-6s%-12s%-16s%-12s\n", "n:  ", "Naive (s):", "Strassen (s):", "ratio:");
         for (int i = minSize; i <= maxSize; i *= dSize) {
             a = Matrix.rand(i);
 
             System.out.format("%-6d", i);
 
             startNaive = System.currentTimeMillis();
             a.mulNaive(a);
             endNaive = System.currentTimeMillis();
             timeNaive = (endNaive - startNaive) * 1.0 / 1000;
 
             System.out.format("%-12.4f", timeNaive);
 
             startStrassen = System.currentTimeMillis();
             a.mulStrassen(a, threshold);
             endStrassen = System.currentTimeMillis();
             timeStrassen = (endStrassen - startStrassen) * 1.0 / 1000;
 
             System.out.format("%-16.4f", timeStrassen);
 
             System.out.format("%-12.4f\n", timeStrassen / timeNaive);
         }
     }
 }
