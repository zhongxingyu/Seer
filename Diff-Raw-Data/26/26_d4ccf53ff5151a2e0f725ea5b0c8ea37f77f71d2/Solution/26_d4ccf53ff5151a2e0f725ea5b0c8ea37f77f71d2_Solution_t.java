 /**
  * Definition for binary tree with next pointer.
  * public class TreeLinkNode {
  *     int val;
  *     TreeLinkNode left, right, next;
  *     TreeLinkNode(int x) { val = x; }
  * }
  */
 public class Solution {
     private TreeLinkNode findNextParentWithChild(TreeLinkNode node) {
         while(node.next != null) {
             node = node.next;
             if (node.left != null || node.right != null)
                 return node;
         }
         return null;
     }
     public void connect(TreeLinkNode root) {
         // Start typing your Java solution below
         // DO NOT write main() function
         TreeLinkNode parent = root;
         TreeLinkNode nextParent = null;
        while(parent != null || nextParent != null) {
             while(parent != null) {
                 if(parent.left != null){
                    if(nextParent == null){
                        nextParent = parent.left;
                    }
                     if(parent.right != null){
                         parent.left.next = parent.right;
                     }
                     else {
                         TreeLinkNode nextParentWithChild = findNextParentWithChild(parent);
                         if(nextParentWithChild == null) {
                             parent.left.next = null;
                         }
                         else{
                             parent.left.next = (nextParentWithChild.left != null)? 
                                 (nextParentWithChild.left):(nextParentWithChild.right);
                         }
                     }
                 }
                 if(parent.right != null) {
                    if(nextParent == null) {
                        nextParent = parent.right;
                    }
                    TreeLinkNode nextParentWithChild = findNextParentWithChild(parent);
                    if(nextParentWithChild == null) {
                        parent.right.next = null;
                    }
                    else{
                        parent.right.next = (nextParentWithChild.left != null)? 
                            (nextParentWithChild.left):(nextParentWithChild.right);
                    }
                 }
                 parent = parent.next;
             }         
             if(nextParent != null) {
                 parent = nextParent;
                 nextParent = null;
             }
         }
     }
 }
