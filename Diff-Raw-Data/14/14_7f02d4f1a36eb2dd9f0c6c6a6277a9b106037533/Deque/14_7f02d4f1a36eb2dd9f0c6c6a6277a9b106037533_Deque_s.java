 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 /**
  * Deques represents a generalization of a stack and a queue that supports
  * inserting and removing items from either the front or the back of the data
  * structure.
  * 
  * @author marcianoskate
  * 
  * @param <Item>
  *            The items to hold in the Deque
  */
 public class Deque<Item> implements Iterable<Item> {
 
     private boolean debuggin = true;
 
     /** index of the first item. */
     private Node<Item> first = null;
     /** index of the last item. */
     private Node<Item> last = null;
     /** counter of items in the Deque. */
     private int size = 0;
     
 
     /**
      * Constructs an empty deque with 10 reserved positions.
      */
     public Deque() {
 
     }
 
     /**
      * Returns wheter the Deque is empty or not
      * 
      * @return True if it is empty, false otherwise
      */
     public boolean isEmpty() {
 
         return size == 0;
     }
 
     /**
      * Returns the size of the Deque
      * 
      * @return Item count holded by the Deque
      */
     public int size() {
 
         return size;
     }
 
     private void addFirstNode(Node<Item> node) {
     
         first = node;
         last = node;
     }
 
     /**
      * Adds an item I to the front of the Deque
      * 
      * @param item
      *            The item to add to the Deque
      */
     public void addFirst(Item item) {
 
         if (item == null) {
             throw new NullPointerException("Item can't be null.");
         }
         assert item != null;
         
         Node<Item> node = getNode(item);
         
         if (size == 0) {
             addFirstNode(node);
         } else {
             
             Node<Item> prevFirst = first;
             node.setNext(prevFirst);
             prevFirst.setPrev(node);
             first = node;
         }
         
         size++;
     }
 
     /**
      * Adds and item I to the end of the Deque
      * 
      * @param item
      *            The item to add to the Deque
      */
     public void addLast(Item item) {
 
         if (item == null) {
             throw new NullPointerException("Item can't be null.");
         }
         assert item != null;
         
         Node<Item> node = getNode(item);
         
         if (size == 0) {
             addFirstNode(node);
         } else {
 
             Node<Item> prevLast = last;
             prevLast.setNext(node);
             node.setPrev(prevLast);
             last = node;
         }
         size++;
     }
 
     /**
      * Deletes and returns the item at the front
      * 
      * @return The item at the front
      */
     public Item removeFirst() {
 
         validateDequeIsNotEmpty();
         
        Item item = first.getItem();
        first = first.getNext();
         size--;
         return item;
     }
 
     /**
      * Deletes and returns the item at the end
      * 
      * @return The item at the end
      */
     public Item removeLast() {
 
         validateDequeIsNotEmpty();
         
         Node<Item> auxLast = last;
         Item item = auxLast.getItem();
         
         if (first == last) {
             first = null;
             last = null;
         } else {
             
             Node<Item> prev = auxLast.getPrev();
             if (prev != null) {
                 prev.setNext(null);
                 last = auxLast.getPrev();
             }
         }
         
         size--;
         return item;
     }
 
     /**
      * Returns an iterator over items of a Deque in order from front to end.
      */
     public Iterator<Item> iterator() {
 
         return new DequeIterator();
     }
 
     private void validateDequeIsNotEmpty() {
 
         if (size == 0) {
             throw new NoSuchElementException("The Deque can't be empty");
         }
     }
 
     private final class DequeIterator implements Iterator<Item> {
         
         private Node<Item> current = first;
 
         @Override
         public boolean hasNext() {
             return current != null;
         }
 
         @Override
         public Item next() {
 
             if (!hasNext()) {
                 throw new NoSuchElementException(
                         "There is no more elements to retrieve");
             }
             Item item  = current.getItem();
             current = current.getNext();
             return item;
         }
 
         @Override
         public void remove() {
 
             throw new UnsupportedOperationException(
                     "You can't remove from an iterator.");
         }
         
     }
     
     private <I> Node<I> getNode(I item) {
         
         return new Node<I>(item);
     }
     
     private final class Node<T> {
         
         private final T item;
         private Node<T> next;
         private Node<T> prev;
         
         public Node(T item) {
             this.item = item;
         }
         
         public T getItem() {
             return item;
         }
         
         public void setNext(Node<T> newNext) {
             
             this.next = newNext;
         }
         
         public Node<T> getNext() {
             
             return this.next;
         }
         
         public void setPrev(Node<T> newPrev) {
             
             this.prev = newPrev;
         }
 
         public Node<T> getPrev() {
             return prev;
         }
     }
 }
 
