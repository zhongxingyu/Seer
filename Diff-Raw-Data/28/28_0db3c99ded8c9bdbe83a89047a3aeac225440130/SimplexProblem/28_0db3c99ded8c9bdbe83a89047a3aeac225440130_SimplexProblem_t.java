 
 package OR;
 
 
 /**
  *
  * 
  */
 public class SimplexProblem {
     Matrix A;
     Matrix C;
     
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
        
        int[] width = new int[m];
        for (int i=0;i<m;i++) width[i] = i;
        //extends the matrix by an identity matrix of slacks
        mA.setMatrix(n, n+m-1,new int[m], Matrix.identity(m, m));
        this.A = mA;
        //this.C = new Matrix;
     }
     
     /**
      * method for creating Simplex Problem by Entering the Basic Form
      */
     public static SimplexProblem fromBasicFrom() {
         return new SimplexProblem();
     }
 }
