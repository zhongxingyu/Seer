 /**
  * Definition for singly-linked list.
  * public class ListNode {
  *     int val;
  *     ListNode next;
  *     ListNode(int x) { val = x; next = null; }
  * }
  */
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
     public TreeNode sortedListToBST(ListNode head) {
         //no nodes
         if(head == null)
             return null;
         
         int size = getListSize(head);
         return sortedListToBST(head, size);
     }
 
     //start inclusive, size is number of nodes 
     private TreeNode sortedListToBST(ListNode head, int size){
 
         if(head == null || size == 0)
             return null;
 
         //only one node
         if(size == 1)
             return new TreeNode(head.val);
 
         ListNode node = null;
         for(int i = 0; i <= (size-1)/2; ++i){
             if( i == 0)
                 node = head;
            else
                node = node.next;
         }
 
         TreeNode left = sortedListToBST(head, size - size/2-1);
         TreeNode right = sortedListToBST(node.next, size/2);
 
         TreeNode treeNode = new TreeNode(node.val);
         treeNode.left =left;
         treeNode.right = right;
         return treeNode; 
     }
 
     private int getListSize(ListNode head){
         int size = 0;
         while(head != null){
             head = head.next;
             size ++;
         }
         return size;
     }
 }
 
