 /**
  * Definition for singly-linked list.
  * public class ListNode {
  *     int val;
  *     ListNode next;
  *     ListNode(int x) {
  *         val = x;
  *         next = null;
  *     }
  * }
  */
 public class Solution {
     
    
     private int getSize(ListNode head){
         int size = 0;
         while(head!= null){
             size++;
             head = head.next;
         }
         return size;
     }
     
    
    
     public ListNode rotateRight(ListNode head, int n) {
        
         int size = getSize(head);
         //if empty list or same times
         if(size ==0 || n%size == 0)
             return head;
         int shift = n%size;
         int newHead = size - shift+1;
 
         int index = 1;
         ListNode helper = new ListNode(-1);
         helper.next = head;
         ListNode curr = helper.next;
         while(index ++< newHead){
             helper = helper.next;
             curr = helper.next; 
         }
         helper.next = null;
        curr.next = head;
        return curr;
     }
 }
