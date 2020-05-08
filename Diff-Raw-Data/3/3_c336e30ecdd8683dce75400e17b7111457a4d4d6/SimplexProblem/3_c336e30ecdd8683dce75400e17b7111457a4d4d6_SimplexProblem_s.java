 
 package OR;
 
 
 /**
  *
  * 
  */
 public class SimplexProblem {
     Matrix A;
     Matrix C;
     Matrix b;
     /**
      * a constructor for internal uses only
      */
     private SimplexProblem() {
         
     }
     
     public SimplexProblem(double[][] A,double[] C , double[] b) {
        // we consider that all conditions are <=  .... simple simplex
         
        //creating Matrix[m,n+m]; slack for every condition
        int m = A.length;
        int n = A[0].length ;
        Matrix mA = new Matrix(m, n + m);
        // filling Matrix A
        mA.setMatrix(0,m-1,0,n-1, new Matrix(A));
        //extends the matrix by an identity matrix of slacks
        mA.setMatrix(0,m-1,n,n+m-1, Matrix.identity(m, m));
        this.A = mA;
        this.C = new Matrix(1,n+m);
        this.C.setMatrix(0, 0, 0, n-1, new Matrix(new double[][] {C}));
        this.C.setMatrix(0, 0, n, n+m-1, new Matrix(new double[1][m]));
        this.b = new Matrix(new double[][] {b}).transpose();
     }
     
     /**
      * method for creating Simplex Problem by Entering the Basic Form
      */
     public static SimplexProblem fromBasicFrom() {
         return new SimplexProblem();
     }
     
     public Solution solveByTableSimplex() {
         return new Solution();
     }
 }
