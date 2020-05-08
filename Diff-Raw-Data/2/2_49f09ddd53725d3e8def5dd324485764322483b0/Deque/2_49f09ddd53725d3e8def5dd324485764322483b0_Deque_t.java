 //Deque.java: wenlong
 //Description: A doulbe-ended queue, a generalization of a stack and a queue that supports inserting and removing items from either
 //             the front or the back of the data structure
 //
 //Performance: each deque operation in constant worst-cast time
 //             use space proportional to the number of items currently in the deque
 //
// TODO: fix some bugs
//
 // %java Deque < tobe.txt
 // 3 1 5 4 2 6 
 // 6
 // 3
 // 1 5 4 2
 //---------------------------------------------------------
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 public class Deque<Item> implements Iterable<Item>
 {
     private class Node //double-linked list
     {
         private Item item;
         private Node next;
         private Node prev;
     }
 
     private Node head;
     private Node last;
     private int N; // number of item
     
     
     public Deque(){
         head = null;
         last = null;
     }
 
     public boolean isEmpty()
     {
         return N == 0;
     }
     
     public int size()
     {
         return N;
     }
 
     //insert the item at the front
     public void addFirst(Item item)
     {
         //a null item?
         if(item == null)
             throw new NullPointerException("null item");
         
         Node x = new Node();
         x.item = item;
 
         if(isEmpty()){
             head = x;
             head.next = null; //Node default constructor create it as null
             head.prev = null;
             last = x;
         }else{
             //Node old_head = head;
             //head = x;
 
             x.next = head; //old head
             x.prev = null;
             head.prev = x;
 
             head = x;
           
         }
         
         N++;
     }
     
     //insert the item at the end
     public void addLast(Item item)
     {
         if(item == null) throw new NullPointerException("null item");
         
         Node x = new Node();
         x.item = item;
 
         if(isEmpty()){
             head = x;
             head.next = null;
             head.prev = null;
             last = x;
         }else{
             last.next = x;
             x.next = null;
             x.prev = last;
             
             last = x;
         }
 
         N++;
     }
 
     //delete and return the item at the front
     public Item removeFirst()
     {
         if(isEmpty()) throw new NoSuchElementException("Deque underflow");
         
         Item item = head.item;
         head = head.next;
         N--;
 
         if(isEmpty()){
             head = null;
             last = null;
         }
 
         return item;
     }
 
     //delete and return the item at the end
     public Item removeLast()
     {
         if(isEmpty()) throw new NoSuchElementException("Deque underflow");
 
         Item item = last.item;
         last = last.prev;
         last.next = null;  //
         N--;
 
         if(isEmpty()){
             head = null;
             last = null;
         }
 
         return item;
     }
 
     public Iterator<Item> iterator()
     {
         return new DequeIterator();
     }
 
     private class DequeIterator implements Iterator<Item>
     {
         private Node current = head;
 
         public boolean hasNext()
         {
             return current != null;
         }
 
         public void remove() {
             throw new UnsupportedOperationException();
         }
 
         public Item next()
         {
             if(!hasNext()) throw new NoSuchElementException();
 
             Item item = current.item;
             current = current.next;
             return item;
             
         }
     }
 
     public static void main(String[] args)
     {
         Deque<String> deque = new Deque<String>();
 
         while(!StdIn.isEmpty()){
             String item = StdIn.readString();
             //StdOut.print(item + " ");
             deque.addLast(item);
         }
         /*
         Iterator<String> i = deque.iterator();
         while(i.hasNext()){
             String s = i.next();
             StdOut.print(s + " ");
         }
         StdOut.println();
         */
         
         for(String s : deque){
             StdOut.print(s + " ");
         }
         StdOut.println();
         
         String str = deque.removeLast();
         StdOut.println(str);
 
         String str1 = deque.removeFirst();
         StdOut.println(str1);
 
         Iterator<String> j = deque.iterator();
         while(j.hasNext()){
             String s = j.next();
             StdOut.print(s + " ");
         }
         StdOut.println();
 
     }
     
 }
