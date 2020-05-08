 /*
  * Copyright (c) 2012 Tao Ma. All rights reserved.
  */
 
 package groundwork.algorithms;
 
 /**
 * Singly Linked List implementation.
  * User: Tao Ma
  * Date: 4/23/12
  * Time: 12:15 AM
  */
 public class SinglyLinkedList<T> {
     private Node<T> head;
 
     public void insert(T data) {
         Node<T> node = new Node<>();
         node.data = data;
         node.next = head;
         head = node;
     }
 
    public void delete(final T data) {
 
     }
 
    public Node search(final T data) {
 
         return null;
     }
 
     public void reverse() {
         Node<T> node = head;
         head = null;
         while (null != node) {
             Node<T> next = node.next;
             node.next = head;
             head = node;
             node = next;
         }
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append(head);
         return sb.toString();
     }
 
     private static class Node<T> {
         private T data;
         private Node<T> next;
 
         @Override
         public String toString() {
             final StringBuilder sb = new StringBuilder();
             sb.append(data).append("->").append(next);
             return sb.toString();
         }
     }
 
     public static void main(String[] args) {
         SinglyLinkedList<String> list = new SinglyLinkedList<>();
         System.out.println(list);
         list.reverse();
         System.out.println(list);
     }
 }
