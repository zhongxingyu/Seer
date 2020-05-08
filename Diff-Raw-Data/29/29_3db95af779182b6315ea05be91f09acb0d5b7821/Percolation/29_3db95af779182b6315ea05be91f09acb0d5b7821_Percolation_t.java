 /**
  *
  * @author Eric
  *
  */
 public class Percolation {
 
     private boolean[][] matrix;
     private WeightedQuickUnionUF union;
     private int virtualTop;
    // private int virtualBottom;
     private int side;
    private boolean percolatesFlag;
 
     // create N-by-N grid, with all sites blocked
     public Percolation(int N)
     {
 
         int i, j;
         matrix = new boolean[N][N];
 
         for (i = 0; i < N; i++) {
             for (j = 0; j < N; j++)
                 matrix[i][j] = false;
         }
         union = new WeightedQuickUnionUF(N * N + 2);
         
         virtualTop = N * N;
        // virtualBottom = N * N + 1;
 
        // j = N * (N - 1);
         
         for (i = 0; i < N; i++) {
             union.union(i, virtualTop);
            // union.union(j + i, virtualBottom);
         }
         side = N;
         
        percolatesFlag = false;
        
     }
     
     private int getIndex(int i, int j)
     {
         return side*i+j;
     }
     
     // open site (row i, column j) if it is not already
     public void open(int i, int j) throws Exception
     {
         if (i > side || j > side 
                 || i <= 0 || j <= 0)
             throw new java.lang.IndexOutOfBoundsException();
 
         int x = i-1;
         int y = j-1;
         matrix[x][y] = true;
         // left
         if (x > 0 && matrix[x-1][y])
             union.union(getIndex(x-1, y), getIndex(x, y));
         // right
         if (x < side-1 && matrix[x+1][y])
             union.union(getIndex(x+1, y), getIndex(x, y));
         // up
         if (y > 0 && matrix[x][y-1])
             union.union(getIndex(x, y-1), getIndex(x, y));
         // down
         if (y < side-1 && matrix[x][y+1])
             union.union(getIndex(x, y+1), getIndex(x, y));
        
        // if it is the site at bottom line, check the grid percolates Flag
        if (x == side-1) {
            if (isFull(i, j))
                percolatesFlag = true;
        }
            
     }
     
     // is site (row i, column j) open?
     public boolean isOpen(int i, int j)throws Exception
     {
         if (i > side || j > side 
                 || i <= 0 || j <= 0)
             throw new java.lang.IndexOutOfBoundsException();
         return matrix[i-1][j-1];
     }
     
     // is site (row i, column j) full?
     // A full site is an open site that can be
     // connected to an open site in the top row via
     // a chain of neighboring (left, right, up, down) open sites.
     public boolean isFull(int i, int j) throws Exception
     {
         if (i > side || j > side 
                 || i <= 0 || j <= 0)
             throw new java.lang.IndexOutOfBoundsException();        
         return (isOpen(i, j) && union.connected(getIndex(i-1, j-1), virtualTop));
     }
     
     
     public boolean percolates()
     {
        return percolatesFlag;
     }
 
 }
