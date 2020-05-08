 public class Percolation {
     private WeightedQuickUnionUF wQuickUnion;
    //TO fix the back wash problem
    private WeightedQuickUnionUF backUp;
     private int width;
     // false means block, while true means open
     private boolean[] site;
     private int numOfObjects;
     private int topRootNode;
     private int botomRootNode;
 
     public Percolation(int N) {
         numOfObjects = N * N;
         site = new boolean[numOfObjects];
         width = N;
         wQuickUnion = new WeightedQuickUnionUF(N * N + 2);
        backUp = new WeightedQuickUnionUF(N*N + 2);
         for (int i = 0; i < numOfObjects; i++) {
             site[i] = false;
         }
         topRootNode = N * N;
         botomRootNode = N * N + 1;
     }
 
     public void open(int i, int j) {
         if (!valid(i, j)) {
             throw new IndexOutOfBoundsException("Site: [" + i + "][" + j + "]");
         }
         int position = index(i, j);
         site[position] = true;
         // virtulization of the root node
         if (i == 1) {
             wQuickUnion.union(topRootNode, position);
            backUp.union(topRootNode, position);
         }
         if (i == width && !percolates()) {
             wQuickUnion.union(position, botomRootNode);
         }
         int[] neighbors = getNeighbors(i, j);
         for (int n = 0; n < neighbors.length; n++) {
             if (site[neighbors[n]]) {
                 wQuickUnion.union(position, neighbors[n]);
                backUp.union(position, neighbors[n]);
             }
         }
     }
 
     public boolean isOpen(int i, int j) {
         if (!valid(i, j)) {
             throw new IndexOutOfBoundsException("Site: [" + i + "][" + j + "]");
         }
         return site[index(i, j)];
     }
 
     public boolean isFull(int i, int j) {
         if (!valid(i, j)) {
             throw new IndexOutOfBoundsException("Site: [" + i + "][" + j + "]");
         }
         int position = index(i, j);
         if (!site[position]) {
             return false;
         }
        
        return backUp.connected(position, topRootNode);
     }
 
     public boolean percolates() {
         return wQuickUnion.connected(topRootNode, botomRootNode);
     }
 
     protected int index(int i, int j) {
         return (i - 1) * width + j - 1;
     }
 
     protected int[] getNeighbors(int i, int j) {
         int[] positions = new int[4];
         // up
         int count = 0;
         if (valid(i - 1, j)) {
             positions[count] = index(i - 1, j);
             count++;
         }
         // right
         if (valid(i, j + 1)) {
             positions[count] = index(i, j + 1);
             count++;
         }
         // down
         if (valid(i + 1, j)) {
             positions[count] = index(i + 1, j);
             count++;
         }
         // left
         if (valid(i, j - 1)) {
             positions[count] = index(i, j - 1);
             count++;
         }
         int[] result = new int[count];
         for (int ix = 0; ix < count; ix++) {
             result[ix] = positions[ix];
         }
         return result;
     }
 
     protected boolean valid(int i, int j) {
         if (i > width || j > width || i < 1 || j < 1) {
             return false;
         }
         return true;
     }
 
 }
