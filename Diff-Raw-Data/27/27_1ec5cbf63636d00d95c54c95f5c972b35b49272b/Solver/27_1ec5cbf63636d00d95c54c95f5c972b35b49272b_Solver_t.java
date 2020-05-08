 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ui;
 
 import java.util.Scanner;
 import matrix.LUSolver;
 import matrix.Matrix;
 
 /**
  *
  * @author Lasse
  */
 public class Solver {
 
     private Scanner scanner;
 
     public Solver() {
         this.scanner = new Scanner(System.in);
     }
 
     public void run() {
         System.out.println();
         System.out.println("Welcome to the equation solver!");
         System.out.println("This program will solve an NxN system of linear equations.");
         System.out.println();
 
         while (solveEquation());
 
     }
 
     private boolean solveEquation() {
 
         int n;
         while (true) {
             System.out.println("Input N, zero or empty line exits:");
             System.out.print(">> ");
             String line = scanner.nextLine().trim();
             if (line.isEmpty() || line.equals("0")) {
                 return false;
             }
             try {
                 n = Integer.parseInt(line);
             } catch (Exception e) {
                 System.out.println("Not a valid number!");
                 continue;
             }
             break;
         }
 
         parseEquation(n);
 
         return true;
     }
 
     private void parseEquation(int n) {
         System.out.println();
         String line;
         double[][] a = new double[n][n + 1];
         for (int i = 0; i < n; i++) {
             System.out.println("Input coefficients in equation " + (i + 1) + " separated with comma or space.");
             System.out.print(">> ");
             line = scanner.nextLine().trim();
             if (line.isEmpty()) {
                 System.out.println("Aborted.");
                 System.out.println();
                 return;
             }
             try {
                 parseLine(line, a, i);
             } catch (Exception e) {
                System.out.println("Not a parsable line or wrong number of inputs!");
                System.out.println("Should be " + (n + 1) + " numbers!");
                System.out.println();
                 --i;
                 continue;
             }
         }
         solve(a);
     }
 
     private void parseLine(String line, double[][] a, int k) {
         line = line.replaceAll("(,|\\s)+", " ");
         String[] split = line.split("\\s");
         if (split.length != a[0].length) {
             throw new IllegalArgumentException();
         }
         for (int i = 0; i < split.length; i++) {
             a[k][i] = ParseUtils.parseDouble(split[i]);
         }
     }
 
     private void solve(double[][] a) {
         int n = a.length;
 
         double[][] mat = new double[n][n];
         double[][] b = new double[n][1];
 
         for (int i = 0; i < n; i++) {
             System.arraycopy(a[i], 0, mat[i], 0, n);
             b[i][0] = a[i][n];
         }
 
         Matrix m = new Matrix(mat);
         Matrix rhs = new Matrix(b);

         System.out.println();
         System.out.println("Equation Ax = b:");
         System.out.println();

         System.out.println("A =");
         m.print();
         System.out.println("b =");
         rhs.print();

         if (m.det() == 0) {
             System.out.println("No solution found!");
             System.out.println("Determinant of A 0.");
             System.out.println();
             return;
         }
         Matrix solution = LUSolver.solveLinearEquation(m.lu(), rhs);

         System.out.println("x =");
         solution.print();
     }
 }
