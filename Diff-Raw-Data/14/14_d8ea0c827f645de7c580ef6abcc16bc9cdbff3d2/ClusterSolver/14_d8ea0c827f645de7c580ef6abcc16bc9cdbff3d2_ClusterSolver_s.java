 /**
  * @author Harold Mendoza
  *
  * Cluster Implementation for maze solver
  *
  */
 import edu.rit.util.Random;
 import edu.rit.pj.Comm;
 import edu.rit.mp.IntegerBuf;
 import edu.rit.mp.ObjectBuf;
 import edu.rit.util.Arrays;
 import edu.rit.util.Range;
 import java.io.*;
 import java.util.*;
 public class ClusterSolver {
     
     private static int[][] fir,sec,prod;
     private static int n, root_n, max_rand;
     private static long seed;
     private static Random gen;
     private static PrintStream out;
     private static boolean pretties;
 
     static Comm world;
     static int size;
     static int rank;
 
     static IntegerBuf[] bigfirst, bigsecond, bigresult;
     static IntegerBuf myfirst, mysecond, myresult;
     static Range[] ranges_cols, ranges_rows;
     static int mylb_rows, myub_rows, mylb_cols, myub_cols;
     static String filename= "ClusterOuput";
 	
 	
 	
     public static void main( String[] args ) throws Exception {
        
         // setup world variables
         Comm.init( args );
         world = Comm.world();
         size = world.size();
         rank = world.rank();
         
 
         if(args.length!=2)
             {
                 System.out.println("Please enter the size for the matrix generation and the Seed: N & S");
                 System.exit(-1);
             }
         
          //Matrix size read in from the command line
         int matrixSize = Integer.parseInt(args[0]); 
 
         int matrix [][] = new int [matrixSize][matrixSize];        
 
         //Set the seed for the Random genrator
         int seed = Integer.parseInt(args[1]); 
         
         //Give random a seed
         gen = Random.getInstance(seed);
         
         //Create an arraylist of storage 
         ArrayList<Pair> storage = new ArrayList <Pair>();
 
         ///Filled matrix with zeros
         for(int i=0; i<matrixSize; i++)
             {
                 for(int j=0; j< matrixSize; j++)
                     {
                         matrix[i][j]=0;                      
                     }
             }
 
         MazeGenerator maze = new MazeGenerator(matrix, matrixSize);
 
         maze.initializeMatrix();
 
         int root = (int)(Math.sqrt(size));
 
         // (split into patches)
         ranges_rows = new Range( 0, matrixSize-1).subranges(root);
 
         ranges_cols = new Range( 0, matrixSize-1).subranges(root);
         
 		Range rows_range = ranges_rows[rank/root];
 		Range cols_range = ranges_cols[rank%root];
 		
         mylb_rows = rows_range.lb();
         myub_rows = rows_range.ub();
 		
         mylb_cols = cols_range.lb();
         myub_cols = cols_range.ub();
 		
 
         MazeMatrixInt tp =  new MazeMatrixInt(matrix, filename);
         
        dftseq poop = new dftseq (tp);
 
         //Counter
         int counter=0;
         
        for(int i=mylb_rows; i<myub_rows; i++)
 		{
                 for(int j=mylb_cols; j<myub_cols; j++)
                 {
 					if(i==mylb_rows || i==myub_rows )
 					{
 						if(matrix[i][j]==1)
 						{
 							int color = rank+size*counter;
							poop.traverse(new Pair(i,j), color, rows_range, cols_range);
 							
 						}
 					}
 					else if(j==mylb_cols || j==myub_cols)
 					{
 						if(matrix[i][j]==1)
 						{
 							int color = rank+size*counter;
							poop.traverse(new Pair(i,j), color, rows_range, cols_range);
 						}		
 					}
 				}
         }

        world.gather(0, IntegerBuf.buffer(matrix), IntegerBuf.patchBuffers(matrix, ranges_rows, ranges_cols));
 	 if(rank == 0)
 	 {
 		tp.printImage();
 	 }
         
         /*
         root_n = (int)Math.sqrt( n );
         max_rand = (int)Math.ceil( Math.sqrt( Integer.MAX_VALUE ) / n );
 
         // initialize the arrays
         fir = new int[n][n];
         sec = new int[n][n];
         prod = new int[n][n];
         long[] t = new long[ 4 ];
 
         // set up message passing; we need our own range, our personal
         // patch of the matrix to work on, and the entire matri        // (split into patches)
         ranges = new Range( 0, n - 1).subranges( size );
         mylb = ranges[rank].lb();
         myub = ranges[rank].ub();
 
         myfirst = IntegerBuf.patchBuffer( fir, ranges[rank], ranges[rank] );
         mysecond = IntegerBuf.patchBuffer( sec, ranges[rank], ranges[rank] );
         myresult = IntegerBuf.patchBuffer( prod, ranges[rank], ranges[rank] );
 
         bigfirst = IntegerBuf.patchBuffers( fir, ranges, ranges);
         bigsecond = IntegerBuf.patchBuffers( sec, ranges, ranges );
         bigresult = IntegerBuf.patchBuffers( prod, ranges, ranges );
 
         // start timing
         t[0] = System.currentTimeMillis();
         // generate the random values for my range
         for( int r = mylb; r <= myub; ++r ) {
             for( int c = mylb ; c <= myub; ++c ) {
                 fir[r][c] = rng.nextInt(max_rand);
                 sec[r][c] = rng.nextInt(max_rand);
             }
         }
         t[1] = System.currentTimeMillis();
         
         // gather values from other processes
         world.allGather( myfirst, bigfirst );
         world.allGather( mysecond, bigsecond );
         //        world.allGather( myresult, bigresult );
                 
         // compute product
         for( int r = mylb; r <= myub; ++r ) {
             for( int c = mylb; c <= myub; ++c ) {
                 int sum = 0;
                 for( int index = 0; index < n; ++index ) {
                     sum += fir[r][index] * sec[index][c];
                 }
                 prod[r][c] = sum;
             }
         }
         world.gather( 0, myresult, bigresult );
         t[2] = System.currentTimeMillis();
         
         // print
         if( rank == 0 ) {
             for( int r = 0; r < n; ++r ) {
                 for( int c = 0; c < n; ++c ) {
                     out.print( prod[r][c] );
                     if( pretties ) out.print( ' ' );
                 }
                 if( pretties ) out.println();
             }
             //out.flush();
         
             t[3] = System.currentTimeMillis();
             System.out.println( (t[1]-t[0]) + "ms to generate" );
             System.out.println( (t[2]-t[1]) + "ms to multiply" );
             System.out.println( (t[3]-t[2]) + "ms to print");
         
             out.close();*/
         
     }
   
 }
