 /* 2.3 Implement an algorithm to delete a node in the middle of a single
  * linked list, given ony access to that node.
  *
  * EXAMPLE:
  * Input: the node 'c' from the linked list a->b->c->d->e
  * Result: nothing is returned, but the new linked list looks like
  * a->b->d->e
  * */
 import java.util.NoSuchElementException;
 
 public class P0203 {
    private class Node {
       private int data;
       private Node next;
    }
    
    public static Node deleteMidNode(Node head) {
       // Boundary checks:
       // 1. there is only one node in the list
       // 2. when the number of nodes is even, which node shall we delete?
       if (head == null || head.next == null)
          return head;
 
       // head is a sentinel
       Node slow = head.next;
       Node fast = head.next.next;
 
       // fast != null for odd node numbers
       // fast.next != null for even node numbers
       while (fast != null && fast.next != null) {
          slow = slow.next;
          fast = fast.next.next;
       }
 
       if (deleteNode(slow)) return head;
       else {
          return null;
       }
    }
    
    /* Delete a given node with the only access to this node.
     * We can achieve this by simply copying the data and next pointer of the
     * next node to the node to be deleted.
     * */
    public static boolean deleteNode(Node node) {
      if (node == null || node.next == null)
          return false;
 
       Node next = node.next;
      node.data = next.data;
      node.next = next.next;
       return true;
    }
 
    private Node initialize(int[] data) {
       Node head = new Node();
       head.data = Integer.MIN_VALUE;
       head.next = null;
 
       if (data == null) {
          throw new NoSuchElementException("Empty inputs");
       }
 
       Node current = head;
       int len = data.length;
       for (int i = 0; i < len; i++) {
          Node tmp = new Node();
          tmp.data = data[i];
          tmp.next = null;
          current.next = tmp;
          current = tmp;
       }
       return head;
    }
 
    private String toString(Node list) {
       StringBuilder s = new StringBuilder();
       Node current = list;
       while (current != null) {
          s.append(current.data + " ");
          current = current.next;
       }
 
       return s.toString();
    }
 
    public static void main(String[] args) {
       int[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
       int[] data1 = {1};
       P0203 p0203 = new P0203();
       Node list = p0203.initialize(data);
       list = deleteMidNode(list);
       System.out.println(p0203.toString(list));
       
    }
 }
