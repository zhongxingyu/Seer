 /**
  * Serial Matrix Operation Wrappers
  */
 public class MatrixOp {
 
   /**
    * Add a double matrix with a scalar integer value.
    *
    * @param m Matrix of type double 
    * @param v The integer value to add the matrix with
    * @return The resulting matrix
    */
   public static MatrixDouble add(MatrixDouble m, int v) {
     return add(v,m);
   }
 
   /**
    * Add a double matrix with a scalar integer value.
    * 
    * @param v The integer value to add the matrix by
    * @param m Matrix of type double 
    * @return The resulting matrix
    */
   public static MatrixDouble add(int v, MatrixDouble m) {
     MatrixDouble output = new MatrixDouble(m.rows(), m.cols());
     for (int r = 0; r < m.rows(); r++) {
       for (int c = 0; c < m.cols(); c++) {
         output.data[r][c] = m.data[r][c] + v;
       }
     }
     return output;
   }
 
   /**
    * Add a integer matrix with a scalar integer value.
    * 
    * @param m Matrix of type integer
    * @param v The integer value to add the matrix with
    * @return The resulting matrix
    */
   public static MatrixInt add(MatrixInt m, int v) {
     return add(v,m);
   }
 
   /**
    * Add a integer matrix with a scalar integer value.
    * 
    * @param v The integer value to add the matrix by
    * @param m Matrix of type double 
    * @return The resulting matrix
    */
   public static MatrixInt add(int v, MatrixInt m) {
     MatrixInt output = new MatrixInt(m.rows(), m.cols());
     for (int r = 0; r < m.rows(); r++) {
       for (int c = 0; c < m.cols(); c++) {
         output.data[r][c] = m.data[r][c] + v;
       }
     }
     return output;
   }
 
 
   /**
    * Multiply a double matrix by a scalar integer value.
    * 
    * @param m Matrix of type double 
    * @param v The integer value to multiply the matrix by
    * @return The resulting matrix
    */
   public static MatrixDouble mult(MatrixDouble m, int v) {
     return mult(v, m);
   }
 
   /**
    * Multiply a double matrix by a scalar integer value.
    * 
    * @param v The integer value to multiply the matrix by
    * @param m Matrix of type double 
    * @return The resulting matrix
    */
   public static MatrixDouble mult(int v, MatrixDouble m) {
     MatrixDouble output = new MatrixDouble(m.rows(), m.cols());
     for (int r = 0; r < m.rows(); r++) {
       for (int c = 0; c < m.cols(); c++) {
         output.data[r][c] = m.data[r][c] * v;
       }
     }
     return output;
   }
 
   /**
    * Multiply a double matrix by a scalar double value.
    * 
    * @param m Matrix of type double 
    * @param v The double value to multiply the matrix by
    * @return The resulting matrix
    */
   public static MatrixDouble mult(MatrixDouble m, double v) {
     return mult(v, m);
   }
 
   /**
    * Multiply a double matrix by a scalar double value.
    * 
    * @param v The double value to multiply the matrix by
    * @param m Matrix of type double 
    * @return The resulting matrix
    */
   public static MatrixDouble mult(double v, MatrixDouble m) {
     MatrixDouble output = new MatrixDouble(m.rows(), m.cols());
     for (int r = 0; r < m.rows(); r++) {
       for (int c = 0; c < m.cols(); c++) {
         output.data[r][c] = m.data[r][c] * v;
       }
     }
     return output;
   }
 
   /**
    * Multiply a integer matrix by a scalar double value.
    * 
    * @param m Matrix of type integer
    * @param v The double value to multiply the matrix by
    * @return The resulting matrix
    */
   public static MatrixDouble mult(MatrixInt m, double v) {
     return mult(v, m);
   }
 
   /**
    * Multiply a integer matrix by a scalar double value.
    * 
    * @param v The double value to multiply the matrix by
    * @param m Matrix of type integer
    * @return The resulting matrix
    */
   public static MatrixDouble mult(double v, MatrixInt m) {
     MatrixDouble output = new MatrixDouble(m.rows(), m.cols());
     for (int r = 0; r < m.rows(); r++) {
       for (int c = 0; c < m.cols(); c++) {
         output.data[r][c] = m.data[r][c] * v;
       }
     }
     return output;
   }
 
   /**
    * Multiply a integer matrix by a scalar integer value.
    * 
    * @param m Matrix of type integer
    * @param v The integer value to multiply the matrix by
    * @return The resulting matrix
    */
   public static MatrixInt mult(MatrixInt m, int v) {
     return mult(v, m);
   }
 
   /**
    * Multiply a integer matrix by a scalar integer value.
    * 
    * @param v The integer value to multiply the matrix by
    * @param m Matrix of type integer
    * @return The resulting matrix
    */
   public static MatrixInt mult(int v, MatrixInt m) {
     MatrixInt output = new MatrixInt(m.rows(), m.cols());
     for (int r = 0; r < m.rows(); r++) {
       for (int c = 0; c < m.cols(); c++) {
         output.data[r][c] = m.data[r][c] * v;
       }
     }
     return output;
   }
 
   public static MatrixDouble mult( MatrixInt b, MatrixDouble a ) {
     return mult( a, b );
   }
 
   /**
    * Multiply a integer matrix by a integer matrix.
    * 
    * @param a The integer value to multiply the matrix by
    * @param a The other integer value to multiply the matrix by
    * @return The resulting matrix
    */
   public static MatrixDouble mult( MatrixDouble a, MatrixInt b ) {
 
     if ( ! a.correctDim( b ) ) {
       throw new RuntimeException("Multiplication of different sized matricies");
     }
 
     MatrixDouble output = new MatrixDouble(a.rows(), b.cols());
 
     for (int r = 0; r < output.rows(); r++) {
       for (int c = 0; c < output.cols(); c++) {
         for (int k = 0; k < a.cols(); k++) {
           output.data[r][c] += (a.data[r][k] * b.data[k][c]);
         }
       }
     }
     return output;
   }
 
   /**
    * Multiply a integer matrix by a integer matrix.
    * 
    * @param a The integer value to multiply the matrix by
    * @param b The other integer value to multiply the matrix by
    * @return The resulting matrix
    */
   public static MatrixInt mult( MatrixInt a, MatrixInt b ) {
 
     if ( ! a.correctDim( b ) ) {
       throw new RuntimeException("Multiplication of different sized matricies");
     }
 
     MatrixInt output = new MatrixInt(a.rows(), b.cols());
 
     for (int r = 0; r < output.rows(); r++) {
       for (int c = 0; c < output.cols(); c++) {
         for (int k = 0; k < a.cols(); k++) {
           output.data[r][c] += (a.data[r][k] * b.data[k][c]);
         }
       }
     }
     return output;
   }
 
 
 
 
 
 
   /**
    * Multiply a integer matrix by a integer vector.
    *
    * @param a The integer matrix
    * @param b The integer vector
    * @return The resulting matrix
    */
   public static MatrixInt mult( MatrixInt a, VectorInt b ) {
 
    if ( ! (a.cols() == b.length()) ) {
       throw new RuntimeException("Multiplication of different sized matricie & vector");
     }
 
     MatrixInt output = new MatrixInt(a.rows(), 1);
 
     for (int r = 0; r < a.rows(); r++) {
       int sum = 0;
       for (int c = 0 ; c < a.cols(); c++) {
         sum += (a.data[r][c] * b.data[c]);
       }
       output.data[r][0] = sum;
     }
     return output;
   }
 
   /**
    * Multiply a double matrix by a integer vector.
    *
    * @param a The double matrix
    * @param b The integer vector
    * @return The resulting matrix
    */
   public static MatrixDouble mult( MatrixDouble a, VectorInt b ) {
     if ( !(a.cols() == b.length()) ) {
       throw new RuntimeException("Multiplication of different sized matricie & vector");
     }
     MatrixDouble output = new MatrixDouble( a.rows(), 1 );
     for (int r = 0; r < a.rows(); r++) {
       double sum = 0;
       for (int c = 0 ; c < a.cols(); c++) {
         sum += (a.data[r][c] * b.data[c]);
       }
       output.data[r][0] = sum;
     }
     return output;
   }
 
   /**
    * Multiply a integer matrix by a double vector.
    *
    * @param a The integer matrix
    * @param b The double vector
    * @return The resulting matrix
    */
   public static MatrixDouble mult( MatrixInt a, VectorDouble b ) {
    if ( ! (a.cols() == b.length()) ) {
       throw new RuntimeException("Multiplication of different sized matricie & vector");
     }
     MatrixDouble output = new MatrixDouble( a.rows(), 1 );
 
     for (int r = 0; r < a.rows(); r++) {
       double sum = 0;
       for (int c = 0 ; c < a.cols(); c++) {
         sum += (a.data[r][c] * b.data[c]);
       }
       output.data[r][0] = sum;
     }
     return output;
   }
 
   public static void main( String args[] ) {
 
       /*
     MatrixInt A = MatrixInt.random( 900, 900 );
     MatrixInt B = MatrixInt.random( 900, 900 );
 
     int loop = 20;
     long start = System.currentTimeMillis();
     for (int i = 0; i < loop; i++ ) {
       long lstart = System.currentTimeMillis();
       MatrixInt C = MatrixOp.mult( A, B );
       System.out.println( "Running time = " +
                         (System.currentTimeMillis()-lstart) + " msec" );
     }
     System.out.println( "Avg time = " +
                         (System.currentTimeMillis()-start)/loop + " msec" );
                         */
       MatrixInt B = new MatrixInt( 4, 3 );
       B.data[0][0] = 1; B.data[0][1] = 2; B.data[0][2] = 3;
       B.data[1][0] = 4; B.data[1][1] = 5; B.data[1][2] = 6;
       B.data[2][0] = 7; B.data[2][1] = 8; B.data[2][2] = 9;
       B.data[3][0] = 10; B.data[3][1] = 11; B.data[2][2] = 12;
 
       VectorInt vec = new VectorInt( 3 );
       vec.data[0] = -2;
       vec.data[1] = 1;
       vec.data[2] = 0;
 
       MatrixInt C = MatrixOp.mult( B, vec );
       C.display();
   }
 
 }
