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
      if (root == null) return true;
 
      int leftHeight = height(root.left);
      int rightHeight = height(root.right);

      boolean isRootBalanced = Math.abs(leftHeight - rightHeight) <= 1;

      if (isRootBalanced)
         return isBalanced(root.left) && isBalanced(root.right);
      else
         return false;

      // return isRootBalanced && isBalanced(root.left) && isBalanced(root.right);
    }
 
    public int height(Node x) {
       if (x == null) return 0;
 
       return 1 + Math.max(height(x.left), height(x.right));
    }

   // an improved solution:
   // 1) One of the problems of isBalanced above is that, the space complexity
   // is consistently O(lgN), since we need to check from the root.
   // Actually, if a subtree is unbalanced, the whole tree will be as well.
   // Therefore, we could potentially reduce the space complexity by checking
   // if one subtree is balanced, and will return false once we find an
   // unbalanced one.
   // 2) every time we calculate height, the heights of subtrees (subtrees of 
   // subtrees) are repeatedly calculated, we coudl avaoid this redundancy by
   // adding extra variables to record the heights of subtrees.
   public boolean isBalanced2(Node root) {
      if (root == null) return true;
   }
 }
