 package matrixcalculator;
 
 public class MatrixCalculator {
 
     public static void main(String[] args) {
         UI ui = new UI();
         
         double[][] matA = {{1,4,4}};
         double[][] matB = {{1},{2},{3}};
         
         Matrix A = new Matrix(matA);
         Matrix B = new Matrix(matB);
         
         System.out.println(A);
         System.out.println(B);
         
         System.out.println(A.multiply(B));
        System.out.println(B.multiply(A));
        ui.start();
     }
 }
