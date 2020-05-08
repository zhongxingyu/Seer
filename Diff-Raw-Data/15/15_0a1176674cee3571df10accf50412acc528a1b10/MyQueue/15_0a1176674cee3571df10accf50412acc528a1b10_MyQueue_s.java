 import java.io.Serializable;
 
 public class MyQueue<T> implements Serializable {
     private Object [] store;
     private int size;
     private int capacity;
     
     public MyQueue(int capacity) {
         this.capacity = capacity;
         store = new Object[capacity];
     }
 
     public boolean offer(T item) {
         if (size == capacity) {
             return false;
         }
         store[size++] = item;
         return true;
     }
 
     public T poll() {
         if (size == 0) {
             throw new NullPointerException();
         }
 
         T item = (T) store[0];
         for (int i = 0; i < size - 1; i++) {
             store[i] = store [i+1];
         }
 
         size--;
         return item;
     }
 
     public static void main (String [] args)
     {
         MyQueue<String> msg = new MyQueue<String>(4);
         msg.offer("CheckedOut");
         msg.offer("Received");
         msg.offer("Extended");
         msg.offer("Returned");
 
         if (! msg.offer("LongExtension")) {
             System.out.println("LongExtension can not be inserted at this point");
         }
 
         String checkedOut = msg.poll();
         System.out.println("Message " + checkedOut);
 
         if (msg.offer("LongExtension")) {
             System.out.println("LongExtension inserted at this point");
         }
 
         /*
         String received = msg.poll();
         System.out.println("Message " + received);
         String extened = msg.poll();
         System.out.println("Message " + extened);
         String returned = msg.poll();
         System.out.println("Message " + returned);
         */
         
     }
 }
