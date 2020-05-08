 package edu.msergey.jalg.exercises.ch3.ex40;
 
 public class DoubleLinkedList<E extends Comparable<E>> {
     private Node<E> head;
     private Node<E> tail;
 
     public Node<E> getHead() {
         return head;
     }
 
     public Node<E> getTail() {
         return tail;
     }
 
     public DoubleLinkedList(Node<E> node) {
         head = node;
         tail = node;
         head.prev = null;
         tail.next = null;
     }
 
     public void addTail(Node<E> node) {
         tail.next = node;
         node.prev = tail;
         node.next = null;
         tail = node;
     }
 
    public void remove(IRemoveChecker removeChecker) {
         if (head != tail) {
             for (Node<E> current = head.next; current != tail; current = current.next) {
                 if (removeChecker.needToRemove(current)) {
                     current.prev.next = current.next;
                     current.next.prev = current.prev;
 
                     Node<E> prevCurrent = current.prev;
                     current.next = null;
                     current.prev = null;
 
                     current = prevCurrent;
                 }
             }
         }
 
         if (removeChecker.needToRemove(head)) {
             Node<E> nextHead = head.next;
             head.next = null;
             head = nextHead;
             if (head == null) return;
             head.prev = null;
         }
 
         if (removeChecker.needToRemove(tail)) {
             Node<E> prevTail = tail.prev;
             tail.prev = null;
             tail = prevTail;
             if (tail == null) return;
             tail.next = null;
         }
     }
 }
