 /**
  * Definition for binary tree
  * public class TreeNode {
  *     int val;
  *     TreeNode left;
  *     TreeNode right;
  *     TreeNode(int x) { val = x; }
  * }
  */
 public class Solution {
 
     class Pair{
         TreeNode left;
         TreeNode right;
         public Pair(TreeNode left, TreeNode right){
             this.left = left;
             this.right = right;
         }
     }
 
     public void recoverTree(TreeNode root) {
         TreeNode[] prev = new TreeNode[1]; 
         prev[0] = null;
         Pair pair = new Pair(null, null);
         findSwappedPair(root,prev,pair);
 
         if(pair.left == null || pair.right == null)
             return;
         int tmp = pair.left.val;
         pair.left.val = pair.right.val;
         pair.right.val = tmp;
     }
 
     public void findSwappedPair( TreeNode root, TreeNode [] prev, Pair pair ){
         if(root == null)
             return;
         //has left, go left
         if(root.left != null)
             findSwappedPair(root.left, prev, pair);
         //no left anymore, root is smallest
         if(prev[0] != null && prev[0].val > root.val){
             if(pair.left == null) {
                 pair.left = prev[0];
                 pair.right = root;
             }
             else
                 pair.right = root;
         }
         prev[0] = root;
         if(root.right != null)
             findSwappedPair(root.right, prev, pair);
     } 
 }
