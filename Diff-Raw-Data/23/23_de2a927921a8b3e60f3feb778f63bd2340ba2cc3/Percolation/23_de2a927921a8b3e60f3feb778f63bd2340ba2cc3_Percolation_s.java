 public class Percolation {
     private int N;
     private int virtualBottom;
     private boolean[] siteOpenState;
     private WeightedQuickUnionUF quickFindUF;
 
     public Percolation(int N) {
         this.N = N;
         this.virtualBottom = N * N + 1;
         siteOpenState = new boolean[N * N + 2];
         quickFindUF = new WeightedQuickUnionUF(N * N + 2);
     }
 
     public void open(int i, int j) {
         int p = getLinearIndex(i, j);
         siteOpenState[p] = true;
         updateVirtualSiteUnions(i, p);
         updateNeighborUnions(i, j, p);
     }
 
     private void updateNeighborUnions(int i, int j, int p) {
         //union with above
         if (i > 1 && siteOpenState[p - N]) {
             quickFindUF.union(p, p - N);
         }
         //union with below
         if (i < N && siteOpenState[p + N]) {
             quickFindUF.union(p, p + N);
         }
         //union with left
         if (j > 1 && siteOpenState[p - 1]) {
             quickFindUF.union(p, p - 1);
         }
         //union with right
         if (j < N && siteOpenState[p + 1]) {
             quickFindUF.union(p, p + 1);
         }
     }
 
     private void updateVirtualSiteUnions(int i, int p) {
         //if top union with virtual top
         if (i == 1) {
             siteOpenState[0] = true;
             quickFindUF.union(p, 0);
         }
         //if bottom union with virtual bottom
         if (i == N) {
             siteOpenState[virtualBottom] = true;
             quickFindUF.union(p, virtualBottom);
         }
     }
 
     public boolean isOpen(int i, int j) {
         return siteOpenState[getLinearIndex(i, j)];
     }
 
     public boolean isFull(int i, int j) {
        return isConnectedToTop(getLinearIndex(i, j));
     }
 
     public boolean percolates() {
        return isConnectedToTop(virtualBottom);
    }

    private boolean isConnectedToTop(int linearIndex) {
        return quickFindUF.connected(0, linearIndex);
     }
 
     private int getLinearIndex(int i, int j) {
         if (i < 1 || i > N || j < 1 || j > N) {
             String errorMsg = "Rows and column indices must be between 1 and "
                     + N;
             throw new IndexOutOfBoundsException(errorMsg);
         }
         return (i - 1) * N + j;
     }
 }
