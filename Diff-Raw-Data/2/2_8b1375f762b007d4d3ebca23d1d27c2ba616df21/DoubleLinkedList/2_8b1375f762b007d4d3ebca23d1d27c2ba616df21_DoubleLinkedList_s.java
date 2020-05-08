 package edu.msergey.jalg.exercises.ch3.ex41;
 
 public class DoubleLinkedList<E extends Comparable<E>> {
     private Node<E> head;
     private Node<E> tail;
 
     public Node<E> getHead() {
         return head;
     }
 
     public Node<E> getTail() {
         return tail;
     }
 
     public void addTail(Node<E> node) {
         if (tail != null) {
             tail.next = node;
             node.prev = tail;
             node.next = null;
             tail = node;
         } else {
             head = node;
             tail = node;
             head.prev = null;
             tail.next = null;
         }
     }
 
     public void addHead(Node<E> node) {
         if (head != null) {
             head.prev = node;
             node.prev = null;
             node.next = head;
             head = node;
         } else {
             head = node;
             tail = node;
             head.prev = null;
             tail.next = null;
         }
     }
 
    public DoubleLinkedList<E> removeAndCopy(IRemoveChecker removeChecker) {
         DoubleLinkedList<E> newList = new DoubleLinkedList<E>();
 
         if (head != tail) {
             for (Node<E> current = head.next; current != tail; current = current.next) {
                 if (removeChecker.needToRemove(current)) {
                     current.prev.next = current.next;
                     current.next.prev = current.prev;
 
                     Node<E> prevCurrent = current.prev;
                     current.next = null;
                     current.prev = null;
 
                     newList.addTail(new Node<E>(current.value));
 
                     current = prevCurrent;
                 }
             }
         }
 
         if (removeChecker.needToRemove(head)) {
             Node<E> nextHead = head.next;
             head.next = null;
 
             newList.addHead(new Node<E>(head.value));
 
             head = nextHead;
             if (head == null) return newList;
             head.prev = null;
         }
 
         if (removeChecker.needToRemove(tail)) {
             Node<E> prevTail = tail.prev;
             tail.prev = null;
 
             newList.addTail(new Node<E>(tail.value));
 
             tail = prevTail;
             if (tail == null) return newList;
             tail.next = null;
         }
 
         return newList;
     }
 }
