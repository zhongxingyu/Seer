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
     
    //method to open a site if it is not already open
    public void open(int i, int j) {
        //first check to see if site is already open,
        if (isOpen(i,j)) {
            System.out.println("Site is already open.");
        }
        else {
            this.grid[i][j] = true;
            //connect this site to adjacent sites
        }
    }
    
     public static void main(String[] args) {
        
        WeightedQuickUnionUF uf = new WeightedQuickUnionUF();
         final boolean logging = true;
         final int temp = 1;
         
         if (logging) System.out.println("Test.");
         
         if (logging) System.out.println("Instantiating Grid");
         Percolation p = new Percolation(100);
         
         if (logging) System.out.println("Test 0,0");
         System.out.println(p.isOpen(0,0));
         
         if (logging) System.out.println("Test N-1,N-1");
         System.out.println(p.isOpen(99,99));
         
        if (logging) System.out.println("Opening 1,1.");
        p.open(1,1);
        
        if (logging) System.out.println("Checking status of 1,1.");
        System.out.println(p.isOpen(1,1));
        
        if (logging) System.out.println("Checking status of 1,1.");
        p.open(1,1);
         //if (logging) System.out.println("Test N, N:  SHOULD FAIL");
         //System.out.println(p.isOpen(100,100));
         
     }   
 }
 
