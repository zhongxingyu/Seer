 public class Percolation {
     
     boolean[][] grid;
     
     /* The constructor will create and N-by-N grid with all sites
      * closed. 
      */
     public Percolation (int N) {
         //create an N-by-N grid represented by a 2-d array
         this.grid = new boolean[N][N];
         
         //initialize all array locations [aka sites] to false [not-open]
         for (int i=0; i<N; i++) {
             for (int j=0; j<N; j++) {
                 this.grid[i][j]=false;
             }
         }
         
       
     } 
     
     public boolean isOpen(int i, int j) {
       return this.grid[i][j];
     }
     
     public static void main(String[] args) {
         final boolean logging = true;
         final int temp = 1;
         
         if (logging) System.out.println("Test.");
         
         if (logging) System.out.println("Instantiating Grid");
         Percolation p = new Percolation(100);
         
         if (logging) System.out.println("Test 0,0");
         System.out.println(p.isOpen(0,0));
         
         if (logging) System.out.println("Test N-1,N-1");
         System.out.println(p.isOpen(99,99));
         
         //if (logging) System.out.println("Test N, N:  SHOULD FAIL");
         //System.out.println(p.isOpen(100,100));
         
     }   
 }
 
