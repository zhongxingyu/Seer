 package guibin.zhang.onlinecourse;
 
 import guibin.zhang.leetcode.listAndArray.LinkedListCycle;
 
 /**
  * Detect cycle in a linked list. 
  * A singly-linked data structure is a data structure made up of nodes 
  * where each node has a pointer to the next node (or a pointer to null). 
  * Suppose that you have a pointer to the first node of a singly-linked list data structure:
  * 
  * Determine whether a singly-linked data structure contains a cycle. 
  * You may use only two pointers into the list (and no other variables). 
  * The running time of your algorithm should be linear in the number of nodes in the data structure.
  * 
  * If a singly-linked data structure contains a cycle, 
  * determine the first node that participates in the cycle. 
  * you may use only a constant number of pointers into the list (and no other variables). 
  * The running time of your algorithm should be linear in the number of nodes in the data structure.
  * 
  * You may not modify the structure of the linked list.
  * 
  * @author Guibin Zhang <guibin.beijing@gmail.com>
  * 
  */
 public class DetectCycleInLinkedList {
     
     public static void main(String[] args) {
         
         LinkedListCycle detector = new LinkedListCycle();
         LinkedListCycle.ListNode node1 = detector.new ListNode(1);
         LinkedListCycle.ListNode node2 = detector.new ListNode(2);
         node1.next = node2;
         LinkedListCycle.ListNode node3 = detector.new ListNode(3);
         node2.next = node3;
         LinkedListCycle.ListNode node4 = detector.new ListNode(4);
         node3.next = node4;
         node4.next = node2;
         System.out.println(detector.hasCycle(node1));
         
         System.out.println("The first node on cycle is:");
        System.out.println(detector.detectCycle(node1).val);
     }
 }
