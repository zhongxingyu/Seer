 /** 
  * Maria Pacana (mariapacana)
  * 10/15/2013 (Algorithms, Part I)
  *
  * The Randomized Queue class implements a double-ended queue with a linked list.
  */ 
 
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 public class RandomizedQueue<Item> implements Iterable<Item> {
 
    private Item[] contents;         // array of items
    private int numItems;            // number of elements on queue
 
    // construct an empty randomized queue
    public RandomizedQueue(){
      contents = (Item[]) new Object[2];
      numItems = 0;
    }
 
    // is the queue empty?
    public boolean isEmpty(){
       return numItems == 0;
    }
 
   // Throws an error if the queue is empty
   private void notWithEmpty(){
     if (this.isEmpty()) {
       throw new java.util.NoSuchElementException();
     }
   }
 
    // return the number of items on the queue
    public int size(){
       return numItems;
    }                  
 
    // resize the underlying array holding the elements
    private void resize(int capacity) {
       assert capacity >= numItems;
       Item[] temp = (Item[]) new Object[capacity];
       for (int i = 0; i < numItems; i++) {
          temp[i] = contents[i];
       }
       contents = temp;
    }
 
    // Add the item
    public void enqueue(Item item){
       if (item == null) { throw new NullPointerException(); }
 
       if (numItems == contents.length) {
          resize(2*contents.length);    // double size of array
       }
        contents[numItems++] = item;          // add the item
    }
 
    // Delete and return a random item
    public Item dequeue() {
       notWithEmpty();
 
       int rand = StdRandom.uniform(numItems);
       Item item = contents[rand];
 
       contents[rand] = null;
       System.arraycopy(contents, rand+1, contents, rand, numItems-rand);
 
       numItems--;

       // shrink size of array if necessary
       if (numItems > 0 && numItems == contents.length/4) {
          resize(contents.length/2);
       }
       return item;
    }
 
    // Return (but do not delete) a random item
    public Item sample(){
       notWithEmpty();
       return contents[StdRandom.uniform(numItems)];
    }
 
    // Return an independent iterator over items in random order
    public Iterator<Item> iterator(){ return new ArrayIterator(); }
 
    private class ArrayIterator implements Iterator<Item> {
       private int current;
 
       public ArrayIterator() {
          current = 0;
          Item[] randContents = (Item[]) new Object[numItems];
 
          for (int i=0; i<numItems; i++) {
             while (contents[numItems] != null) {
                int randy = StdRandom.uniform(numItems);
                if (randContents[randy] == null) {
                   randContents[randy] = contents[numItems];
                   contents[numItems] = null;
                }
             }
          }
          contents = randContents;
          contents = null;
       }
 
       public boolean hasNext() { 
          return current < numItems-1;
       }
 
       public void remove() {  
         throw new java.lang.UnsupportedOperationException();
       } 
       
       public Item next() {
          if (!hasNext()) throw new NoSuchElementException();
          return contents[++current];
       }
   }
 }
