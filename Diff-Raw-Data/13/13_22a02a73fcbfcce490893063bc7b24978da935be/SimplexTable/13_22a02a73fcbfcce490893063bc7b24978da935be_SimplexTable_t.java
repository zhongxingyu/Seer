 package OR;
 import Jama.*;
 
 /**
  *
  * 
  */
 public class SimplexTable {
     
 //    private int m,n;
 //    private double[][] table;
 //    
 //    public SimplexTable(int n,int m) {
 //        this.m = m;
 //        this.n = n;
 //        table = new double[n][m];
 //    }
     
     private Matrix items;
     
     private int numberOfVariable;
     private int numberOfEquation;
     
     private int numberOfBasicVariables;
     private int numberOfNonBasicVariables;
     
     // is objective function Max
     private boolean isMax;
     
     //constant of variables evrey equation
     private Matrix A;
     
     //constant of objective function variables
    private Double[] C;
     
     //RHS of Table
     double[] b;
     
     //Concat between A and C
     Matrix ACb;
     
     //Vector of Basic variables
    private Integer[] Basic;
     
    public SimplexTable(Matrix A,Double[] C,double[] b) {
         //Sets Value
         this.A = A;
         this.C = C; //must be -C and RHS of C Zero
         //this.ACb = //Concat A and C and b;
         // and set zeros in first column
     }
     
     
     ///
     /// search variable in basic variables vector 
     /// and return the index of variable
     ///
     /**
      * 
      * @param basicVariable the variable that we want to look for
      * @return the index of variable in basic variable vector
      */
     
     private < T > int getIndexOfVariable(T[] vector,T Variable) {
         for (int i = 0; i < vector.length; i++) {
             if (vector[i] == Variable) {
                 return i;
             }
         }
         return -1;
      }
     
     /**
      * 
      * @return true if it is best solution else return false
      */
     public boolean isItBestSolution() {
         for (int i = 0; i < C.length; i++) {
             if (isMax) {
                 if (C[i] < 0) {
                     return false;
                 }
             }
             else {
                 if (C[i] > 0) {
                     return false;
                 }
             }
         }
         return true;
     }
     
     private int getIndexOfInVariable() {
         // Muximun in absolute
         double maximum = 0;
         
         // for all instance of objective function variables
         for (int i = 0; i < C.length; i++) {
             if (isMax) {
                 if (maximum > C[i])
                     maximum = C[i];
             }
             else {
                 if (maximum < C[i])
                     maximum = C[i];
             }
         }
         return getIndexOfVariable(C, maximum);
     }
 
     private int getIndexOfOutVariable() {
         //return index of out Variable
        return -1;
     }
     
    public Integer[] getBasicVariables() {
         return Basic;
     }
     
     public double[] getSolution() {
         return b;
     }
     
     
     
     ///
     /// update teble to get best solution
     ///
     public void updateTable() {
         ACb = getNewTable(getIndexOfOutVariable(), getIndexOfInVariable());
     }
     
     ///
     /// update table after entry variable to basic variables vector
     /// and out variable from basic variables vector
     ///
     /**
      * @param inVariable the variable which will in the basic solution
      *        outVariable the variable which will out the basic solution
      */
     private Matrix getNewTable(int outVariable,int inVariable) {
         
         int indexOfOutVariable = getIndexOfVariable(Basic,outVariable);
         
         //New matrix to set new value in it
         Matrix updatedMatrix = new Matrix(numberOfEquation,numberOfVariable);
         
         //Entry inVariables instead of outVariable
         Basic[indexOfOutVariable] = inVariable;
         
         //get dependence item
         double a = ACb.get(indexOfOutVariable, inVariable);
         
         //for all items in table
         for (int i = 0; i < numberOfEquation +1/*for Ojective function*/; i++) {
             for (int j = 0; j < numberOfVariable; j++) {
                 if (i == indexOfOutVariable) {
                     //if it's the same line a' = b/a
                     // b: item we want to update it
                     // a: dependence item
                     updatedMatrix.set(i, j, ACb.get(i, j)/*b*//a);
                 }
                 else {
                     //else a' = d - ((cb)/a)
                     // d: item we want to update it
                     // b: the intersection between 'd' column and 'a' Line
                     // c: the intersection between 'd' Line and 'a' column
                     // d: dependence point
                     updatedMatrix.set(i, j, ACb.get(i, j)/*d*/ - (ACb.get(indexOfOutVariable, j)/*b*/ *
                             ACb.get(i, inVariable)/*c*/)/a);
                 }
             }
         }
         return updatedMatrix;
     }
 
 }
