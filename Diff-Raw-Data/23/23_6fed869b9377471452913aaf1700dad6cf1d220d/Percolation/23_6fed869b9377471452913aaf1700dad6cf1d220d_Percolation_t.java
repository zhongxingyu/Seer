 public class Percolation {
    private final byte[] matrix;
    private int N;
    private int inputIdx;
    private int outputIdx;
    private WeightedQuickUnionUF uf;
     public Percolation(int N) {             // create N-by-N grid, with all sites blocked
         this.N = N;
         int size = N*N;
         matrix = new byte[size];
         inputIdx = size;
         outputIdx = size+1;
         uf = new WeightedQuickUnionUF(size + 2);
         for (int i = 0; i < N; i++) {
             uf.union(inputIdx, i);
             uf.union(outputIdx, i+N*(N-1));
         }
     }
 
     private void check(int i, int j) {
         if (i <= 0 || i > N) throw new IndexOutOfBoundsException("row index i out of bounds");
         if (j <= 0 || j > N) throw new IndexOutOfBoundsException("row index j out of bounds");
     }
     public void open(int i, int j) {         // open site (row i, column j) if it is not already
         check(i, j);
         int idx = (i-1)*N + (j-1);
         matrix[idx] = 1;
         if (i > 1) {
             int idxUp = (i-2)*N + (j-1);
             if (isOpen(i-1, j))
                 uf.union(idx, idxUp);
         }
         if (i < N) {
             int idxDown = (i)*N + (j-1);
             if (isOpen(i+1, j))
                 uf.union(idx, idxDown);
         }
         if (j > 1) {
             int idxLeft = (i-1)*N + (j-2);
             if (isOpen(i, j-1))
                 uf.union(idx, idxLeft);
         }
         if (j < N) {
             int idxRight = (i-1)*N + (j);
             if (isOpen(i, j+1))
                 uf.union(idx, idxRight);
         }
     }
     public boolean isOpen(int i, int j) {    // is site (row i, column j) open?
         check(i, j); i--; j--;
         int idx = i*N + j;
         return matrix[idx] > 0;
     }
     public boolean isFull(int i, int j) {   // is site (row i, column j) full?
         check(i, j); i--; j--;
         int idx = i*N + j;
         return uf.connected(inputIdx, idx);
     }
     public boolean percolates() {          // does the system percolate?
         return uf.connected(inputIdx, outputIdx);
     }
 }
