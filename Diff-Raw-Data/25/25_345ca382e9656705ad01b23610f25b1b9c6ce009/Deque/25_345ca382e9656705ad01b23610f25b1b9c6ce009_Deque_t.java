 import java.util.Iterator;
 
 public class Deque<Item> implements Iterable<Item> {
     private int sz;
 
     private Node<Item> head;
     private Node<Item> tail;
 
     private class Node<Item> {
         private Item item;
         private Node<Item> next;
         private Node<Item> prev;
 
         public Node(Item item) {
             this.item = item;
             this.next = null;
             this.prev = null;
         }
 
         public void setNext(Node<Item> refNext) {
             next = refNext;
         }
 
         public void setPrev(Node<Item> refPrev) {
             prev = refPrev;
         }
 
         public void setItem(Item newItem) {
             item = newItem;
         }
 
         public Node<Item> getNext() {
             return next;
         }
 
         public Node<Item> getPrev() {
             return prev;
         }
 
         public Item getItem() {
             return item;
         }
     }
 
     private class DequeIterator implements Iterator<Item> {
         private Node<Item> current;
         public DequeIterator() {
             this.current = head;
         }
 
         public boolean hasNext() {
             return current != null;
         }
 
         public void remove() {
             throw new java.lang.UnsupportedOperationException();
         }
 
         public Item next() {
             Item result = current.getItem();
            current = current.getPrev();
 
             return result;
         }
     }
 
     public Deque() {
         this.sz = 0;
         this.head = null;
         this.tail = null;
     }
 
     public boolean isEmpty() {
        return size() == 0;
     }
 
     public int size() {
         return sz;
     }
 
     public void addFirst(Item item) {
         checkNull(item);
 
         if (isEmpty()) {
             head = new Node<Item>(item);
             tail = head;
             ++sz;
             return;
         }
 
         ++sz;
         Node<Item> newNode = new Node<Item>(item);
         newNode.setPrev(head);
         head.setNext(newNode);
         head = newNode;
     }
 
     public void addLast(Item item) {
         checkNull(item);
 
         if (isEmpty()) {
             head = new Node<Item>(item);
             tail = head;
             ++sz;
 
             return;
         }
 
         ++sz;
         Node<Item> newNode = new Node<Item>(item);
         newNode.setNext(tail);
         tail.setPrev(newNode);
         tail = newNode;
     }
 
     public Item removeFirst() {
         checkIsEmpty();
         Item result = head.getItem();
         --sz;
 
         if (size() == 0) {
             tail = null;
             head = null;
             return result;
         }
 
         head = head.getPrev();
         head.setNext(null);
         return result;
     }
 
     public Item removeLast() {
         checkIsEmpty();
         Item result = tail.getItem();
         --sz;
 
         if (size() == 0) {
             tail = null;
             head = null;
             return result;
         }
 
         tail = tail.getNext();
         tail.setPrev(null);
         return result;
     }
 
     public Iterator<Item> iterator() {
         return new DequeIterator();
     }
 
     private void checkNull(Item item) {
         if (item == null) {
             throw new java.lang.NullPointerException();
         }
     }
 
     private void checkIsEmpty() {
         if (isEmpty()) {
             throw new java.util.NoSuchElementException();
         }
     }
 
 }
