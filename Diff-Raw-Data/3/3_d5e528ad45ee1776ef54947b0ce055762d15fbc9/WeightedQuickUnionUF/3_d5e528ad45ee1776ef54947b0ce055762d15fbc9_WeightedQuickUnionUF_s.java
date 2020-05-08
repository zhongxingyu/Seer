 /****************************************************************************
  *  Compilation:  javac WeightedQuickUnionUF.java
  *  Execution:  java WeightedQuickUnionUF < input.txt
  *  Dependencies: StdIn.java StdOut.java
  *
  *  Weighted quick-union (without path compression).
  *
  ****************************************************************************/
 
 public class WeightedQuickUnionUF {
     private int[] id;    // id[i] = parent of i
     private int[] sz;    // sz[i] = number of objects in subtree rooted at i
     private int count;   // number of components
 
     private int[] connected; // connection map
 
     private int total;
 
     private boolean giantComponentReached = false;
     private boolean nonIsolatedReached = false;
 
     // Create an empty union find data structure with N isolated sets.
     public WeightedQuickUnionUF(int N) {
         total = N;
         count = N;
         id = new int[N];
         sz = new int[N];
 
         connected = new int[N];
 
         for (int i = 0; i < N; i++) {
             id[i] = i;
             sz[i] = 1;
         }
     }
 
     public boolean isGiantComponent() {
         return giantComponentReached;
     }
 
     public boolean isNonIsolated() {
         return nonIsolatedReached;
     }
 
     public boolean isConnected() {
         return count == 1;
     }
 
     // Return the number of disjoint sets.
     public int count() {
         return count;
     }
 
     // Return component identifier for component containing p
     public int find(int p) {
         while (p != id[p])
            id[i] = id[id[i]];
            p = id[p];
         return p;
     }
 
    // Are objects p and q in the same set?
     public boolean connected(int p, int q) {
         return find(p) == find(q);
     }
 
 
    // Replace sets containing p and q with their union.
     public void union(int p, int q) {
         int i = find(p);
         int j = find(q);
         if (i == j) return;
 
         // make smaller root point to larger one
         if (sz[i] < sz[j]) {
             id[i] = j;
             sz[j] += sz[i];
         } else {
             id[j] = i;
             sz[i] += sz[j];
         }
         count--;
 
         connected[p] = 1;
         connected[q] = 1;
 
         if (!nonIsolatedReached) {
             if (connected.length >= total) {
                 nonIsolatedReached = true;
             }
         }
 
         if (!giantComponentReached) {
             if (sz[i] >= total / 2) {
                 giantComponentReached = true;
             }
         }
     }
 
 
     public static void main(String[] args) {
         int N = StdIn.readInt();
         WeightedQuickUnionUF uf = new WeightedQuickUnionUF(N);
 
         // read in a sequence of pairs of integers (each in the range 0 to N-1),
         // calling find() for each pair: If the members of the pair are not already
         // call union() and print the pair.
         while (!StdIn.isEmpty()) {
             int p = StdIn.readInt();
             int q = StdIn.readInt();
             if (uf.connected(p, q)) continue;
             uf.union(p, q);
             StdOut.println(p + " " + q);
         }
         StdOut.println("# components: " + uf.count());
     }
 
 }
 
