 package matrixcalculator;
 
 import java.util.Arrays;
 
 public class Matrix {
     
     /**
      * Matriisin korkeus
      */
     final public int m;
     
     /**
      * Matriisin leveys
      */
     final public int n;
     final private double[][] values;
     
     /**
      * Muodostaa matriisin annetun kaksiulotteisen double-taulukon pohjalta
      *
      * @param matrix
      */
     public Matrix(double[][] matrix) {
         this.m = matrix.length;
         this.n = matrix[0].length;
         this.values = matrix;
     }
     
     /**
      * Laske kaksi matriisia yhteen
      *
      * @param mat matriisi jonka kanssa yhteenlasku suoritetaan
      * @return Matrix-olion, jossa on yhteenlaskun tulos
      */
     public Matrix add(Matrix mat) {
         if (!checkDimensions(this, mat)) {
             return null;
         }
         
         double[][] retMatrix = new double[m][n];
         double[][] matrix = mat.getValues();
         
         for (int i = 0; i < m; i++) {
             for (int a = 0; a < n; a++) {
                 retMatrix[i][a] = values[i][a] + matrix[i][a];
             }
         }          
         
         return new Matrix(retMatrix);
     }
     
     /**
      * Laske kahden matriisin kertolasku
      *
      * @param mat - matriisi jonka kanssa kertolasku suoritetaan
      * @return Matrix-olio, jossa on kertolaskun tulos
      */
     public Matrix multiply(Matrix mat) {
         if (this.n != mat.m) {
             return null;
         }
         
        double[][] retMatrix = new double[this.n][mat.n];
         double[][] matrix = mat.getValues();
         
         for (int i = 0; i < retMatrix.length; i++) {
             for (int a = 0; a < retMatrix[0].length; a++) {
                retMatrix[i][a] = matrixPosSum(i, a, values, matrix);
             }
         }         
         
         return new Matrix(retMatrix);
     }
   
     private double matrixPosSum(int col, int row, double[][] mat1, double[][] mat2) {
         double sum = 0;
         
        for (int i = 0; i < mat1.length; i++) {
            sum += mat1[i][col] * mat2[row][i];
         }
         
         return sum;
 
     }
     
     /**
      * Kertoo matriisia kertoimella
      *
      * @param coefficent kerroin, jolla matriisia kerrotaan
      * @return
      */
     public Matrix multiply(double coefficent) {
         double[][] matrix = new double[m][n];
         
         for (int i = 0; i < m; i++) {
             for (int a = 0; a < n; a++) {
                 matrix[i][a] = values[i][a] * coefficent;
             }
         }        
         
         return new Matrix(matrix);
     }
     
     /**
      * Laskee matriisin determinantin
      *
      * @return 
      * @throws Exception
      */
     public int determinant() throws Exception {
         if (this.m != this.n) {
             throw new IllegalArgumentException();
         }
         
         return 4;
     }
     
     /**
      * Laskee matriisin käänteismatriisin
      *
      * @return
      */
     public Matrix inverse() {
         try {
             int det = this.determinant();
             
             double[][] matrix = this.multiply((1 / (double) det)).getValues();
         
             return new Matrix(matrix);
         }
         catch (Exception ex) {
             return null;
         }
     }    
     
     /**
      * Laskee matriisin redusoidun porrasmuodon
      *
      * @return Matrix-olion, jossa matriisi on redusoidussa porramuodossa
      */
     public Matrix rref() {
         return null;
     }
     
     /**
      * Palauttaa matriisin ominaisarvot
      *
      * @return 
      */
     public int[] eigenvalues() {
         return new int[2];
     }
 
     /**
      * Palauttaa matriisin ominaisvektorit
      *
      * @return
      */
     public int[] eigenvectors() {
         return new int[2];
     }    
     
     /**
      * Palauttaa matriisin arvot kaksiulotteisena double-taulukkona
      *
      * @return
      */
     public double[][] getValues() {
         return values;
     }
     
     private boolean checkDimensions(Matrix m1, Matrix m2) {
         if (m1.m != m2.m || m1.n != m2.n) {
             return false;
         }
         
         return true;
     }
     
     /**
      * Palauttaa matriisin tulostusmuodossa
      *
      * @return
      */
     @Override
     public String toString() {
         String ret = "";
         
         int padding = 6;
         
         for (int i = 0; i < m; i++) {
             ret += "| ";
             for (int a = 0; a < n; a++) {
                 if (values[i][a] < 0) {
                     ret += Utils.padRight(values[i][a] + "", padding);
                 }
                 else {
                     ret += Utils.padRight(" " + values[i][a], padding);
                 }
             }
             ret += "|\n";
         }
         
         return ret;
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 67 * hash + Arrays.deepHashCode(this.values);
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Matrix other = (Matrix) obj;
         
         if (!Arrays.deepEquals(this.values, other.values)) {
             return false;
         }
         return true;
     }
     
     
 }
