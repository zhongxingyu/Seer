 
 package OR;
 
 
 /**
  *
  * 
  */
 public class SimplexProblem {
     Matrix A;
     Double[] C;
     Double[] b;
     long time = 0;
     
     
     ProblemType Type;
 
     public enum ProblemType {
         Max,
         Min
     }
     /**
      * a constructor for internal uses only
      */
     private SimplexProblem() {
         
     }
     
     public SimplexProblem(ProblemType type,double[][] A,Double[] C , Double[] b) {
        // we consider that all conditions are <=  .... simple simplex
        this.Type = type;
        //creating Matrix[m,n+m]; slack for every condition
        int m = A.length;
        int n = A[0].length ;
        Matrix mA = new Matrix(m, n + m);
        // filling Matrix A
        mA.setMatrix(0,m-1,0,n-1, new Matrix(A));
        //extends the matrix by an identity matrix of slacks
        mA.setMatrix(0,m-1,n,n+m-1, Matrix.identity(m, m));
        this.A = mA;
        this.C = new Double[n+m];
        for (int i=0;i<n;i++)
            this.C[i] = C[i];
        for (int i=n;i<n+m;i++)
            this.C[i] = new Double(0);
        this.b = b;
        //Heeeeeeeeeeeeeeeeeeeeeeeeerrreeeeeeeeeeeeee
        //table = new SimplexTable(A, C.getArray()[0], b.transpose().getArray()[0], isMax());
     }
     
     private void setTime(long time) {
         this.time = time;
     }
     
     /**
      * 
      * @return Time of execution
      */
     public long getTime() {
         return time;
     }
     
     /** 
      * method for creating Simplex Problem by Entering the Basic Form
      */
     public static SimplexProblem fromBasicFrom() {
         return new SimplexProblem();
     }
     
     private boolean isMax() {
         return (Type == ProblemType.Max) ? true : false;
     }
     
     public SolutionList solveByTableSimplex() {
         SimplexTable table = new SimplexTable(A, C ,b ,isMax());
         SolutionList solution = table.getSolution();
         long t0 = System.nanoTime();
         while (!table.isItBestSolution()) {
             int solutionType = table.updateTable();
             if (solutionType == -2) {
                 solution = table.getSolution();
             }
             else if(solutionType == -1) {
                 break;
             }
             else {
             	solution = table.getSolution();
             }
         }
         if(table.isItBestSolution()) {
             int ZeroNonBasicNumber = table.getIndexOfNonBasicVariableZero();
             for (int i = 0; i < ZeroNonBasicNumber; i++) {
                 table.updateTable(table.getIndexOfNonBasicVariableZero());
                 solution.add(table.getSolution().get(0));
                 solution.setInfinity();
             }
         }
         long t1 = System.nanoTime();
         setTime(t1-t0);
         solution.time = getTime();
         solution.table = table;
         return solution;
     }
 }
