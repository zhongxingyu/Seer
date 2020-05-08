 package com.nateriver.app.cracking;
 
 import com.nateriver.app.models.SingleLink;
 import com.nateriver.app.utils.PrintUtil;
 
 import java.util.HashMap;
 
 /**
  * Write code to remove duplicates from an unsorted linked list.
  * FOLLOW UP
  * How would you solve this problem if a temporary buffer is not allowed?
  */
 
 public class Q21 {
     /**
      * Use a hash map to mark value
      */
     public static void removeDuplicateLink(SingleLink head){
         if(head == null || head.next == null)
             return;
         SingleLink node = head;
         HashMap<String,Integer> map = new HashMap<String, Integer>();
 
         while (node != null && node.next != null){
             SingleLink nextNode = node.next;
             if(map.containsKey(nextNode.value)){
                 //remove node
                 node.next  = nextNode.next;
                 nextNode.next = null;
             }
             else{
                 map.put(nextNode.value,1);
                node = node.next;
             }
         }
     }
 
     /**
      * use two link to traverse
      * complexity would be O(n2)
      */
     public static void removeDuplicateLink1(SingleLink head){
         if(head == null || head.next == null)
             return;
         SingleLink nodeFirst = head;
         SingleLink nodeSecond;
         while (nodeFirst.next != null){
             nodeSecond = nodeFirst.next;
             while (nodeSecond != null && nodeSecond.next != null){
                 SingleLink nextSecondNode = nodeSecond.next;
                 if(nextSecondNode.value.equals(nodeFirst.next.value)){
                     nodeSecond.next = nextSecondNode.next;
                     nextSecondNode.next = null;
                 }
                 nodeSecond = nodeSecond.next;
             }
 
             nodeFirst = nodeFirst.next;
         }
     }
 
     public static void main(String[] args) {
         SingleLink head = new SingleLink();
         SingleLink node = head;
         String[] values = new String[]{"a","b","c","c","a","d","a"};
         for(String value : values ){
             SingleLink tempNode = new SingleLink();
             tempNode.value = value;
 
             node.next = tempNode;
             node = node.next;
             node.next = null;
         }
 
         PrintUtil.printSingleLink(head);
         removeDuplicateLink1(head);
         PrintUtil.printSingleLink(head);
 
     }
 
 }
