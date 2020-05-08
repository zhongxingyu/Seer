 /*----------------------------------------------------------------
  *  Author:        Matt Farmer
  *  Written:       08/17/2012
  *  Last updated:  08/25/2012
  *
  *  Compilation:   javac Percolation.java
  *  Execution:     java Percolation
  *
  *  Tests the percolation as per the specification available at:
  *    http://coursera.cs.princeton.edu/algs4/assignments/percolation.html
  *
  *----------------------------------------------------------------*/
 public class Percolation {
 
     private int rowLen;
     private int topIndex;
     private int bottomIndex;
     private int gridSize;
     private boolean[] grid;
 
     private WeightedQuickUnionUF uf;
 
     // create N-by-N grid, with all sites blocked
     public Percolation(int N) {
         if (N <= 0) {
             throw new java.lang.IllegalArgumentException(
                 "N must be larger than 0"
             );
         }
         rowLen = N;
         gridSize = N*N;
         uf = new WeightedQuickUnionUF(gridSize + 2);
         grid = new boolean[gridSize];
         topIndex = gridSize;
         bottomIndex = gridSize + 1;
     }
 
     private void checkInput(int i, int j) {
         if (1 > i || rowLen < i) {
             throw new java.lang.IndexOutOfBoundsException(
                 "i must be between 1 and "+ rowLen
             );
         }
         if (1 > j || rowLen < j) {
             throw new java.lang.IndexOutOfBoundsException(
                 "j must be between 1 and "+ rowLen
             );
         }
     }
 
     // open site (row i, column j) if it is not already
     public void open(int iOne, int jOne) {
         checkInput(iOne, jOne);
 
         // Change indexes to start at 1, not 0
         int i = iOne - 1;
         int j = jOne - 1;
 
         int index = getIndex(i, j);
 
         if (!grid[index]) {
             grid[index] = true;
 
             // If the spot we just opened has any open neighbors, connect them
             int n; // Neighbor's index
             boolean hasN = false;
             for (int d = 0; d < 4; d++) {
                 n = getNeighborIndex(i, j, d);
                 if (-1 != n && isOpen(n)) {
                     uf.union(index, n);
                     hasN = true;
                 }
             }
 
             // If it is in the top row, connect it with the top node
             if (0 == i) {
                 uf.union(index, topIndex);
             }
             if (hasN) {
                 // check if this made any of the bottom nodes connected
                 // to the top
                for (int b = gridSize-1; b >= gridSize-rowLen; b--) {
                     if (isOpen(b) && uf.connected(topIndex, b)) {
                         uf.union(b, bottomIndex);
                         break;
                     }
                 }
             }
         }
     }
 
     private int getIndex(int i, int j) {
         return i*rowLen + j;
     }
 
     /*
      * Get the index of a neighbor in the specified direction.
      *
      * @param (int) i the index
      * @param (int) j the index
      * @param (int) d the direction of the neighbor:
      *                  0 = UP, 1 = RIGHT, 2 = DOWN, 3 = LEFT
      *
      * @return (int) the index of the neighbor or -1 if it is out of bounds
      *
      */
     private int getNeighborIndex(int i, int j, int d) {
         if (0 > d || 3 < d) {
             throw new java.lang.IllegalArgumentException(
                 "Direction must be between 0 and 3"
             );
         }
         switch (d) {
             case 0:  // UP
                 if (0 == i) {
                     return -1;
                 }
                 return getIndex(i-1, j);
             case 1:  // RIGHT
                 if (j+1 == rowLen) {
                     return -1;
                 }
                 return getIndex(i, j+1);
             case 2:  // DOWN
                 if (1+i == rowLen) {
                     return -1;
                 }
                 return getIndex(i+1, j);
             case 3:  // LEFT
                 if (0 == j) {
                     return -1;
                 }
                 return getIndex(i, j-1);
             default:
         }
         return -1;
     }
 
     // is site at given index open
     private boolean isOpen(int index) {
         return grid[index];
     }
 
     // is site (row i, column j) open?
     public boolean isOpen(int i, int j) {
         checkInput(i, j);
 
         // Change indexes to start at 1, not 0
         return isOpen(getIndex(i-1, j-1));
     }
 
     // is site (row i, column j) full?
     public boolean isFull(int i, int j) {
         checkInput(i, j);
 
         // Change indexes to start at 1, not 0
         return uf.connected(topIndex, getIndex(i-1, j-1));
     }
 
     // does the system percolate?
     public boolean percolates() {
         return uf.connected(topIndex, bottomIndex);
     }
 
     public static void main(String[] args) {
         Percolation p = new Percolation(4);
         p.open(1, 1);
         p.open(2, 1);
         p.open(2, 2);
         p.open(3, 2);
         p.open(3, 3);
         p.open(4, 3);
     }
 
 
 }
