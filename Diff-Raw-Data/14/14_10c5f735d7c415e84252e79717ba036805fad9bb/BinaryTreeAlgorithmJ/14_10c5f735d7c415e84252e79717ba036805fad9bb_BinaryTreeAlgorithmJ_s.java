 package guibin.zhang.datastructure;
 
 import java.util.ArrayList;
 import java.util.Queue;
 import java.util.LinkedList;
 
 /**
  *
  * @author guibin
  */
 public class BinaryTreeAlgorithmJ {
 
     /**
      * Suppose there is only one node in the tree which is root, the depth is 1;
      * If the root node has left sub tree, but doesn't have the right sub tree,
      * the depth should be the depth of the left sub tree + 1; If the root node
      * has right sub tree, but doesn't have the left sub tree, the depth should
      * be the depth of the right sub tree + 1; If the root has both the left sub
      * tree and the right sub tree, the depth should be the max depth of both
      * sub trees + 1;
      *
      * @param root The root node of the binary tree.
      * @return The depth of the binary tree.
      */
     public int depthOfBinaryTree(BinaryNodeJ root) {
         if (root == null) {
             return 0;
         } else {
             if (root.left == null && root.right != null) {
                 return depthOfBinaryTree(root.right) + 1;
             } else if (root.left != null && root.right == null) {
                 return depthOfBinaryTree(root.left) + 1;
             } else {
                 int leftDepth = depthOfBinaryTree(root.left);
                 int rightDepth = depthOfBinaryTree(root.right);
                 if (leftDepth >= rightDepth) {
                     return leftDepth + 1;
                 } else {
                     return rightDepth + 1;
                 }
             }
         }
 
     }
     /**
      * A more simple version.
      */
     public int depthOfBinaryTree2(BinaryNodeJ root) {
         if (root == null) {
             return 0;
         } else {
             int leftDepth = depthOfBinaryTree(root.left);
             int rightDepth = depthOfBinaryTree(root.right);
             if (leftDepth >= rightDepth) {
                 return leftDepth + 1;
             } else {
                 return rightDepth + 1;
             }
         }
     }
 
     /**
      * Traverse the tree by depth first way to compute the maximum depth
      */
    public int depthOfBinaryTreeByDepthTraverse(BinaryNodeJ root) {
         
         ArrayList<BinaryNodeJ> result = new ArrayList<BinaryNodeJ>();
         Queue<BinaryNodeJ> queue = new LinkedList<BinaryNodeJ>();
         
         root.setDepth(1);
         BinaryNodeJ current = root;
         
         while(current != null || queue.size() > 0) {
             result.add(current);
             if(current.left != null) {
                 current.left.setDepth(current.depth + 1);
                 queue.add(current.left);
             }
             if(current.right != null) {
                 current.right.setDepth(current.depth + 1);
                 queue.add(current.right);
             }
             
             current = queue.peek() == null ? null : queue.poll();
         } 
         
         //----------------------- Print the tree
         for(BinaryNodeJ n : result) {
             System.out.print(n.data.toString());
         }
         System.out.println();
         //----------------------
         
         BinaryNodeJ lastOne = result.get(result.size() - 1);
         return lastOne.getDepth();
     }
     
     public static void main(String[] args) {
         //      A
         //     / \
         //    B   C
         //   /   / \
         //  D  E   F
         // /
         //G
         BinaryNodeJ G = new BinaryNodeJ("G");
         BinaryNodeJ D = new BinaryNodeJ(G, "D", null);
         BinaryNodeJ E = new BinaryNodeJ("E");
         BinaryNodeJ F = new BinaryNodeJ("F");
         BinaryNodeJ B = new BinaryNodeJ(D, "B", null);
         BinaryNodeJ C = new BinaryNodeJ(E, "C", F);
         BinaryNodeJ A = new BinaryNodeJ(B, "A", C);
         
         BinaryTreeAlgorithmJ bta = new BinaryTreeAlgorithmJ();
         System.out.println("depthOfBinaryTree A is " + bta.depthOfBinaryTree(A));
         System.out.println("depthOfBinaryTree2 A is " + bta.depthOfBinaryTree2(A));
        System.out.println("depthOfBinaryTreeByDepthTraverse A is " + bta.depthOfBinaryTreeByDepthTraverse(A));
         System.out.println("depthOfBinaryTree C is " + bta.depthOfBinaryTree(C));
         System.out.println("depthOfBinaryTree2 C is " + bta.depthOfBinaryTree2(C));
        System.out.println("depthOfBinaryTreeByDepthTraverse C is " + bta.depthOfBinaryTreeByDepthTraverse(C));
         System.out.println("depthOfBinaryTree B is " + bta.depthOfBinaryTree(B));
         System.out.println("depthOfBinaryTree2 B is " + bta.depthOfBinaryTree2(B));
        System.out.println("depthOfBinaryTreeByDepthTraverse B is " + bta.depthOfBinaryTreeByDepthTraverse(B));
         System.out.println("depthOfBinaryTree G is " + bta.depthOfBinaryTree(G));
         System.out.println("depthOfBinaryTree2 G is " + bta.depthOfBinaryTree2(G));
        System.out.println("depthOfBinaryTreeByDepthTraverse G is " + bta.depthOfBinaryTreeByDepthTraverse(G));
     }
 }
