 /*
  * Quick-Union Weighted with Path Compression Analysis
  *
  * Initialize:  O(n)
  * Connected:   O(log* n)
  * Union:       O(log* n) (includes cost of finding roots)
  *
  * Note: log* = Amortized cost (very, very nearly, but not quite 1)
  */
 
 public class QuickUnionWPCUF {
     private int[] id;
     private int[] sz;
     private int count;
 
     public QuickUnionWPCUF(int count) {
         this.count = count;
         this.id = new int[count];
         this.sz = new int[count];
 
         /* Initialize roots */
         for (int i = 0; i < id.length; i++) {
             id[i] = i;
         }
 
         /* Initialize sizes */
         for (int i = 0; i < sz.length; i++) {
             sz[i] = 1;
         }
     }
 
     public int count() {
         return count;
     }
 
     public int find(int p) {
         if (p < 0 || p >= id.length)
             throw new ArrayIndexOutOfBoundsException("Index: " + p + " - Size: " + id.length);
         
         int root = p;
 
         while(root != id[root]) {
             root = id[root];
         }
 
         /* Link every node to the root (keeps tree almost completely flat) */
         while(p != root) {
             int tmpP = id[p];
             id[p] = root;
             p = tmpP;
         }
 
         return root;
     }
 
     public int size(int p) {
         if (p < 0 || p >= sz.length)
             throw new ArrayIndexOutOfBoundsException("Index: " + p + " - Size: " + sz.length);
 
         return sz[p];
     }
 
     public void union(int p, int q) {
         int bucketP = find(p);
         int bucketQ = find(q);
         int sizeP = size(p);
         int sizeQ = size(q);
 
         /* Link roots, if not linked */
         if (bucketP != bucketQ) {
             /* Link root of smaller tree to root of bigger tree (prevents tall trees) */
             if (sizeQ > sizeP) {
                 id[bucketP] = bucketQ;
                 sz[bucketQ] += sizeP;
                sz[bucketP] = 0;
             } else {
                 id[bucketQ] = bucketP;
                 sz[bucketP] += sizeQ;
                sz[bucketQ] = 0;
             }
         }
 
         /* Decrement roots count */
         count--;
     }
 
     public boolean connected(int p, int q) {
         /* Linked nodes have the same root */
         return find(p) == find(q);
     }
 
 	public static void main(String[] args) {
 	    int size = StdIn.readInt();
         QuickUnionWPCUF uf = new QuickUnionWPCUF(size);
 
         while (!StdIn.isEmpty()) {
             int p = StdIn.readInt();
             int q = StdIn.readInt();
 
             if (!uf.connected(p, q)) {
                 uf.union(p, q);
                 StdOut.println(p + " " + q);
             }
         }
 
         StdOut.println(uf.count() + " components");
 	}
 }
