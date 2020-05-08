 package guibin.zhang.leetcode.listAndArray;
 
 /**
  *
  * Given a linked list, reverse the nodes of a linked list k at a time and return its modified list.
  * 
  * If the number of nodes is not a multiple of k then left-out nodes in the end should remain as it is.
  * 
  * You may not alter the values in the nodes, only nodes itself may be changed.
  * 
  * Only constant memory is allowed.
  * 
  * For example,
  * Given this linked list: 1->2->3->4->5
  * 
  * For k = 2, you should return: 2->1->4->3->5
  * 
  * For k = 3, you should return: 3->2->1->4->5
  * 
  * 
  * http://www.cnblogs.com/lichen782/p/leetcode_Reverse_Nodes_in_kGroup.html
  * 
  * @author Gubin Zhang <guibin.beijing@gmail.com>
  */
 public class ReverseNodesInKGroup {
     
     public class ListNode {
         int val;
         ListNode next;
         
         public ListNode(int x) {
             this.val = x;
             this.next = null;
         }
     }
     
     public ListNode reverseKGroup_v2(ListNode head, int k) {
         
         if(head == null || k == 1) return head;
         ListNode dummy = new ListNode(0);
         dummy.next = head;
         
         ListNode curr = head;
         ListNode p1 = dummy;
         int i = 0;
         while (curr != null) {
             i++;
             if (i % k == 0) {
                reverse_v2(p1, curr.next);
                p1 = curr;
             } else {
                 curr = curr.next;
             }
         }
         return dummy.next;
     }
     
     /**
      * Reverse list between p1 and p2 exclusively.
      * @param p1
      * @param p2 
      */
    public void reverse_v2(ListNode p1, ListNode p2) {
         
         ListNode curr = p1.next;
         ListNode tail = curr;
         ListNode prev = p1;
         ListNode nxt = null;
         
         while (curr != p2) {
             nxt = curr.next;
             curr.next = prev;
             prev = curr;
             curr = nxt;
         }
         tail.next = p2;
         p1.next = prev;
     }
     
     
     public ListNode reverseKGroup(ListNode head, int k) {
         
         if(head == null || k == 1) return head;
         ListNode dummy = new ListNode(0);
         dummy.next = head;
         ListNode pre = dummy;
         int i = 0;
         while (head != null) {
             i ++;
             if (i % k == 0) {
                 pre = reverse(pre, head.next);
                 head = pre.next;
             } else {
                 head = head.next;
             }
         }
         
         return dummy.next;
     }
     
     /**
      * Reverse a list between pre and next exclusively.
      * 
      * an example:
      * a linked list:
      * 0->1->2->3->4->5->6
      * |           |   
      * pre        next
      * 
      * after call pre = reverse(pre, next)
      * 0->3->2->1->4->5->6
      *          |  |
      *          pre next
      * 
      * @param pre
      * @param next
      * @return the reversed list's last node, which is the precedence of parameter next
      */
     public ListNode reverse(ListNode pre, ListNode next) {
         
         ListNode tail = pre.next; //where first will be doomed "last"
         ListNode curr = tail.next;
         while(curr != next) {//Note here is the next instead of null.
             tail.next = curr.next;
             curr.next = pre.next;
             pre.next = curr;
             curr = tail.next;
         }
         return tail;
     }
     
     public static void main(String[] args) {
         ReverseNodesInKGroup rn = new ReverseNodesInKGroup();
         ListNode a = rn.new ListNode(1);
         ListNode b = rn.new ListNode(2);
         ListNode c = rn.new ListNode(3);
         ListNode d = rn.new ListNode(4);
         a.next = b;
         b.next = c;
         c.next = d;
        ListNode res = rn.reverseKGroup_v2(a, 2);
         
         while(res != null) {
             System.out.print(res.val + ",");
             res = res.next;
         }
         System.out.println("");
     }
 }
