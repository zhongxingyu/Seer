 
 import java.util.Iterator;
 
 /**
  *
  * @author oleg.chumakov
  */
 public class Deque<Item> implements Iterable<Item> {
 
   private Node first = null;
   private Node last = null;
   private int size = 0;
 
   /**
    * construct an empty deque
    */
   public Deque() {
   }
 
   /**
    * @return is the deque empty
    */
   public boolean isEmpty() {
     return size == 0;
   }
 
   /**
    * @return the number of items on the deque
    */
   public int size() {
     return size;
   }
 
   private void addInitElement(Item item) {
     Node elem = new Node(item);
     first = elem;
     last = elem;
   }
 
   /**
    * insert the item at the front
    *
    * @param item
    */
   public void addFirst(Item item) {
     checkForNull(item);
     if (size == 0) {
       addInitElement(item);
     } else {
       Node newFirst = new Node(item);
       newFirst.next = first;
       first.prev = newFirst;
       first = newFirst;
     }
     size++;
   }
 
   /**
    * insert the item at the end
    *
    * @param item
    */
   public void addLast(Item item) {
     checkForNull(item);
     if (size == 0) {
       addInitElement(item);
     } else {
       Node newLast = new Node(item);
       newLast.prev = last;
       last.next = newLast;
       last = newLast;
     }
     size++;
   }
 
   /**
    * check that item not null throws exception if validation failed
    *
    * @param item
    */
   private void checkForNull(Item item) {
     if (item == null) {
       throw new java.lang.NullPointerException();
     }
   }
 
   /**
    * throwing exception if deque is empty
    */
   private void checkForEmptyDeque() {
     if (isEmpty()) {
       throw new java.util.NoSuchElementException();
     }
   }
 
   /**
    *
    * @return deleted item from the front
    */
   public Item removeFirst() {
     checkForEmptyDeque();
 
     Item item = first.item;
     Node oldFirst = first;
     if (first.next != null) {
       first.next.prev = null;
     }
     first = first.next;
    oldFirst.prev = null;
    oldFirst.next = null;
    oldFirst.item = null;
     oldFirst = null;
     size--;
     return item;
   }
 
   /**
    *
    * @return deleted item from the end
    */
   public Item removeLast() {
     checkForEmptyDeque();
 
     Item item = last.item;
    Node oldLast = last;
     if (last.prev != null) {
       last.prev.next = null;
     }
     last = last.prev;
    oldLast.next = null;
    oldLast.prev = null;
    oldLast.item = null;
    oldLast = null;
     size--;
     return item;
   }
 
   /**
    *
    * @return an iterator over items in order from front to end
    */
   @Override
   public Iterator<Item> iterator() {
     checkForEmptyDeque();
     return new DequeIterator(first);
   }
 
  public static void main(final String[] args) {
    Deque<String> s = new Deque<String>();

    for (int i = 0; i < 10000; i++) {
      s.addFirst("item");
    }

    for (int i = 0; i < 10000; i++) {
      s.removeLast();
    }

  }

   private class DequeIterator implements Iterator<Item> {
 
     private Node current = null;
 
     public DequeIterator(Node first) {
       current = first;
     }
 
     @Override
     public boolean hasNext() {
       return (current != null);
     }
 
     @Override
     public Item next() {
       checkNextExisting();
       Item result = current.item;
       current = current.next;
       return result;
     }
 
     private void checkNextExisting() {
       if (!hasNext()) {
         throw new java.util.NoSuchElementException();
       }
     }
 
     @Override
     public void remove() {
       checkNextExisting();
       throw new java.lang.UnsupportedOperationException();
     }
   }
 
   private class Node {
 
     private Item item;
     private Node next;
     private Node prev;
 
     public Node() {
     }
 
     public Node(Item item) {
       this.item = item;
     }
 
     public Node(Item item, Node next, Node prev) {
       this.item = item;
       this.next = next;
       this.prev = prev;
     }
   }
 }
