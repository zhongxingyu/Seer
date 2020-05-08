 // queue.java: wenlong
 // queue: the first item inserted is the first to be removed (FIFO)
 // This class inclues the insert, remove, peek, isEmpty and size methods
 //
//TODO: There are still bugs
//
 //to maintain encapsulation, don't want to reveal the internal representation of the queue (array or linked list) to the client
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 /*
 //java's Iterable interface
 public interface Iterable<Item> 
 {
     Iterator<Item> iterator(); 
     
 }
 */
 public class Queue<Item> implements Iterable<Item> { // implement java's Iterable interface
     private Node head;     // head of the queue
     private Node last;      // last of the queue
     private int N;    // number of item in queue
 
     private class Node
     {
        private Item item;
        private Node next;
     }
     
     public Queue(){    // constructor
         head = null;
         last = null;
         //N = 0;                   
     }
     
     //add the item to the queue
     public void insert(Item item){
         Node x = new Node();
         x.item = item;
         
         if (isEmpty()){
             head = x;
             last = x;
         }else{
             last.next = x;
             last = x;
         }
 
         N++;
     }
 
     //remove and return the item on the queue least recently added
     public Item remove(){
         if(isEmpty()) throw new RuntimeException("Queue underflow");
         
         Item item = head.item;
         head = head.next;
         N--;
 
         if(isEmpty())
             last = null; //avoid loitering
 
         return item;
     }
     
     public Item peek(){   //return the item least recently added to the QUEUE
         if(isEmpty()) throw new RuntimeException("Queue underflow");
         
         return head.item;
     }
     
     public boolean isEmpty(){   // true if queue is empty
         return head == null;
     }
     
     public int size(){    // number of items
        return N;
     }
 
     /*
     // Java's java.util.Iterator interface
     public interface Iterator<Item>
     {
         boolean hasNext();
         Item next();
         void remove(); //optional
     }
     */
     //It's going to have a method iterator() that returns a iterator
     //the inner class QueueIterator implements the iterator
     public Iterator<Item> iterator()
     {
         return new QueueIterator();
     }
 
     private class QueueIterator implements Iterator<Item> 
     {
         private Node current = head;
         public boolean hasNext()
         {
             return current != null;
         }
 
         public Item next()
         {
             if(!hasNext()) throw new NoSuchElementException();
             
             Item item = current.item;
             current = current.next;
             return item;
         }
 
         public void remove() 
         {
             throw new UnsupportedOperationException();
         }
         
     }
 
     // test client
     public static void main(String[] args)
     {
         Queue<String> queue = new Queue<String>();
         while(!StdIn.isEmpty()){
             String item = StdIn.readString();
             queue.insert(item);
         }
         
         Iterator<String> i = queue.iterator();
         while(i.hasNext()){
             String s = i.next();
             StdOut.print(s + " ");
         }
         StdOut.println();
 
         //simpler
         for(String s: queue){
             StdOut.print(s + " ");
         }
         StdOut.println();
         
     }
     
 }   // end class queue
