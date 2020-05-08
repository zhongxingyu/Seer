 
 import edu.rit.pj.Comm;
 import edu.rit.pj.ParallelRegion;
 import edu.rit.pj.ParallelTeam;
 import edu.rit.pj.IntegerForLoop;
 
 
 
 /**
 * Serial Matrix Operation Wrappers
  */
 public class MatrixParallelOp {
 
   /**
    * Multiply a integer matrix by a integer matrix.
    * 
    * @param a The integer value to multiply the matrix by
    * @param a The other integer value to multiply the matrix by
    * @return The resulting matrix
    */
   public static MatrixInt mult( final MatrixInt a, final MatrixInt b ) throws Exception {
 
     if ( ! a.equalDim( b ) ) {
       throw new RuntimeException("Multiplication of different sized matricies");
     }
 
     final MatrixInt output = new MatrixInt(a.rows(), a.cols());
 
     new ParallelTeam().execute( new ParallelRegion() {
       public void run() throws Exception {
         execute( 0, output.rows()-1, new IntegerForLoop() {
           public void run( int first, int last ) {
             for ( int r = first; r <= last; r++ ) {
               for (int c = 0; c < output.cols(); c++) {
                 for (int k = 0; k < a.cols(); k++) {
                   output.data[r][c] += (a.data[r][k] * b.data[k][c]);
                 }
               }
             }
           }
         });
       }
     });
 
     return output;
   }
 
   public static MatrixDouble mult( final MatrixDouble b, final MatrixInt a ) throws Exception {
     return mult( a, b );
   }
 
   /**
    * Multiply a integer matrix by a double matrix.
    * 
    * @param a The integer value to multiply the matrix by
    * @param a The double matrix to multiply the matrix by
    * @return The resulting matrix
    */
   public static MatrixDouble mult( final MatrixInt a, final MatrixDouble b ) throws Exception {
 
     if ( ! a.equalDim( b ) ) {
       throw new RuntimeException("Multiplication of different sized matricies");
     }
 
     final MatrixDouble output = new MatrixDouble(a.rows(), a.cols());
 
     new ParallelTeam().execute( new ParallelRegion() {
       public void run() throws Exception {
         execute( 0, output.rows()-1, new IntegerForLoop() {
           public void run( int first, int last ) {
             for ( int r = first; r <= last; r++ ) {
               for (int c = 0; c < output.cols(); c++) {
                 for (int k = 0; k < a.cols(); k++) {
                   output.data[r][c] += (a.data[r][k] * b.data[k][c]);
                 }
               }
             }
           }
         });
       }
     });
 
     return output;
   }
 
 
   public static void main( String args[] ) throws Exception {
 
     // Initialize parallel infrastructure
     Comm.init( args );
 
     MatrixInt A = MatrixInt.random( 900, 900 );
     MatrixInt B = MatrixInt.random( 900, 900 );
 
     int loop = 20;
     long start = System.currentTimeMillis();
     for (int i = 0; i < loop; i++ ) {
       long lstart = System.currentTimeMillis();
       MatrixInt C = MatrixParallelOp.mult( A, B );
       System.out.println( "Running time = " +
                         (System.currentTimeMillis()-lstart) + " msec" );
     }
     System.out.println( "Avg time = " +
                         (System.currentTimeMillis()-start)/loop + " msec" );
 
   }
 
 }
