 /* 4.1 Implement a function to check if a binary tree is balanced. For the 
  * purposes of this question, a balanced tree is defined to be a tree such
  * that the heights of the two subtrees of any node never differ by more than
  * one.
  */
 
 public class P0401 {
    private class Node {
       int value;
       Node left, right;
    }
 
    public boolean isBalanced(Node root) {
       return Math.abs(height(root.left) - height(root.right)) <= 1; 
    }
 
    public int height(Node x) {
       if (x == null) return 0;
 
       return 1 + Math.max(height(x.left), height(x.right));
    }
 }
