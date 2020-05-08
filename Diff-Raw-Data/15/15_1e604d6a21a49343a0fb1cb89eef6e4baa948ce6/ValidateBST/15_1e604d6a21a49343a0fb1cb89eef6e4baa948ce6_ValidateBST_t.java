 /*
  * Given a binary tree, determine if it is a valid binary search tree (BST).
 
 Assume a BST is defined as follows:
 
 1. The left subtree of a node contains only nodes 
 with keys less than the node's key.
 2. The right subtree of a node contains only nodes 
 with keys greater than the node's key.
 3. Both the left and right subtrees must also be 
 binary search trees.
  */
 public class ValidateBST {
 
    int cur;
    boolean flag;
     
     public void checkBST(TreeNode node) {
         if (node == null)
             return;
         checkBST(node.left);
        if (node.val <= cur) { 
             flag = false;
             return;
         }
         else
             cur = node.val;
         checkBST(node.right);
     }
     
     public boolean isValidBST(TreeNode root) {
        cur = Integer.MIN_VALUE;
        flag = true;
 
         checkBST(root);
         return flag;
     }
     
     public static void main(String[] args) {
        TreeNode root = new TreeNode(-1);
        TreeNode left = new TreeNode(0);
         root.right = left;
         ValidateBST bst = new ValidateBST();
         System.out.println(bst.isValidBST(root));
         
     }
 
 }
