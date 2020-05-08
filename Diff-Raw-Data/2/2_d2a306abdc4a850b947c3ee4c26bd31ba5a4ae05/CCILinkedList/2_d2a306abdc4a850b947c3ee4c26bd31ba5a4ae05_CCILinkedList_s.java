 package com.hceris.datastructures;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 import com.google.common.base.Joiner;
 
 public class CCILinkedList<T> implements Iterable<T> {
 
     private Node<T> head;
 
     public CCILinkedList(Iterable<? extends T> elements) {
     	for(T elem : elements) {
     		addLast(elem);
     	}
     }
 
     public CCILinkedList() {
     }
 
     public int size() { 
     	int n = 0;
     	for(@SuppressWarnings("unused") T data : this) {
     		n++;
     	}
    	return n++;
     }
 
     public void addLast(T data) {
         if(head == null) {
             head = new Node<T>(data, null);
             return;
         }
 
         Node<T> last = head;
         while(last.next != null) {
             last = last.next;
         }
 
         last.next = new Node<T>(data, null);
     }
 
     public void addFirst(T data) {
         if(head == null) {
             head = new Node<T>(data, null);
         } else {
             head = new Node<T>(data, head);
         }
     }
 
     public T get(int index) {
         if(index < 0 || index >= size()) {
             throw new IllegalArgumentException();
         }
 
         int i = 0;
         Node<T> current = head;
 
         while(i != index) {
             i++;
             current = current.next;
         }
 
         return current.data;
     }
 
     public Iterator<T> iterator() {
         return new Iterator<T>() {
             Node<T> current = head;
             
             public boolean hasNext() {
                 return current != null;
             }
 
             public T next() {
                 if(!hasNext()) {
                     throw new NoSuchElementException();
                 }
 
                 T value = current.data;
                 current = current.next;
                 return value;
             }
 
             public void remove() {
                 throw new UnsupportedOperationException();
             }
         };
     }
     
     @Override public String toString() {
     	return "[" + Joiner.on(',').join(iterator()) + "]";
     };
 
     public void removeDuplicates() {
         Set<T> seen = new HashSet<T>();
 
         if(size() < 2) { return; }
 
         Node<T> prev = head;
         Node<T> current = head.next;
 
         seen.add(prev.data);
         while(current != null) {
             if(!seen.add(current.data)) {
                 prev.next = current.next;
             } else {
                 prev = current;
             }
 
             current = current.next;
         }
     }
 
     public void removeDuplicatesInPlace() {
         if(size() < 2) { return; }
 
         Node<T> mark = head;
         while(mark != null) {
             Node<T> prev = mark;
             while(prev.next != null) {
                 if(prev.next.data.equals(mark.data)) {
                     prev.next = prev.next.next;
                 } else {
                     prev = prev.next;
                 }
             }
             mark = mark.next;
         }
     }
 
     private static class Node<T> {
         T data;
         Node<T> next;
 
         Node(T data, Node<T> next) {
             this.data = data;
             this.next = next;
         }
     }
 
 }
