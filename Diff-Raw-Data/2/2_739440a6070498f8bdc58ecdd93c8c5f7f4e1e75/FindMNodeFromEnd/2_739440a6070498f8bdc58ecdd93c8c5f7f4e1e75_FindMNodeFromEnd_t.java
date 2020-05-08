 package edu.msergey.jalg.linked_list;
 
 /**
  *  Given a singly-linked list, devise a time- and space-efficient algorithm
  *  to find the mth-to-last element of the list.
  *  Implement your algorithm, taking care to handle relevant error conditions.
  *  Define mth to last such that when m = 0, the last element of the list is returned.
  *
  *
 *  It is check for hand written code, so it's very draft code.
  */
 public class FindMNodeFromEnd {
     public static class Node {
         public Node next;
         public Object data;
     }
 
     public static Node find1(int m, Node head) {
         Node nNode = head;
         int i = 0;
         while (i < m) {
             nNode = nNode.next;
             i++;
         }
         Node mNode = head;
         while (nNode.next != null) {
             nNode = nNode.next;
             mNode = mNode.next;
         }
         return mNode;
     }
 }
