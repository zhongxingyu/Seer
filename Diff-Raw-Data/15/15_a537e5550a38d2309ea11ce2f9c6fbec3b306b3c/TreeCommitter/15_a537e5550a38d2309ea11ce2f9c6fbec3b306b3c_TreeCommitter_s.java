 package ch.x42.terye.oak.mk.test.tests;
 
 import java.util.concurrent.Callable;
 
 import org.apache.jackrabbit.mk.api.MicroKernel;
 import org.apache.jackrabbit.oak.commons.PathUtils;
 
 /**
  * This callable commits a tree defined by the constructor arguments with a
  * specified commit rate. The name of the nodes will be the concatenation of a
  * constant prefix (NODE_PREFIX) and a number corresponding to the zero-based
  * numbering of the child nodes of a given node.
  */
 public class TreeCommitter implements Callable<String> {
 
    private static final String NODE_PREFIX = "node_";
 
     private MicroKernel mk;
     private String root;
     private int height;
     private int branchingFactor;
     private int rate;
 
     /**
      * Constructor.
      * 
      * @param mk the microkernel used to commit
      * @param root absolute path to the already-existing root node of the tree
      *            to be committed
      * @param height the height of the tree to be committed
      * @param branchingFactor the branching factor of the tree to be committed
      * @param rate the commit rate (i.e number of nodes per commit)
      */
     public TreeCommitter(MicroKernel mk, String root, int height,
             int branchingFactor, int rate) {
         this.mk = mk;
         this.root = root;
         this.height = height;
         this.branchingFactor = branchingFactor;
         this.rate = rate;
     }
 
     @Override
     public String call() throws Exception {
         String revisionId = null;
         String batch = "";
         int batchCount = 0;
         // loop through all levels
         for (int i = 1; i <= height; i++) {
             // number of nodes on this level
             int nbNodes = (int) Math.pow(branchingFactor, i);
             // loop through all nodes on this level
             for (int j = 0; j < nbNodes; j++) {
                 if (batchCount == rate) {
                    // commit batch
                     revisionId = mk.commit("/", batch, null, "");
                     batch = "";
                     batchCount = 0;
                 }
                // add new statement to batch for later commit
                String abs = generatePath(i, j);
                batch += "+\"" + PathUtils.relativize("/", abs) + "\":{} ";
                batchCount++;
             }
         }
         // commit remaining statements, if any
         if (batchCount > 0) {
             // commit batch
             revisionId = mk.commit("/", batch, null, "");
         }
         // return last revision id
         return revisionId;
     }
 
     /**
      * This method generates the path for a node in the tree rooted at 'root'.
      * 
      * @param level the level of the node (0 being the same level as the root of
      *            the tree)
      * @param index the zero-based index of the node on the specified level
      * @return the absolute path of the specified node
      */
     private String generatePath(int level, int index) {
         String path = root;
         if (level == 0) {
             return root;
         }
         // loop through all levels, starting at first sublevel of the root
         for (int i = 1; i <= level; i++) {
             // number of nodes at level 'level' that share the same ancestor
             // node on the current level
             int n = ((int) Math.pow(branchingFactor, level - i));
             path = PathUtils.concat(path, NODE_PREFIX
                     + ((index / n) % branchingFactor));
         }
         return path;
     }
 
 }
