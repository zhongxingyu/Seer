 import java.util.Arrays;
 import java.util.Iterator;
 
 public class RandomizedQueue<Item> implements Iterable<Item> {
 
     private final int queueNumber = 10;
     private Item[] elementData;
     private int arraySize;
     private int objSize;
 
 
    
     // construct an empty randomized queue
     
     public RandomizedQueue() {
         this.arraySize = queueNumber;
         this.objSize = 0;
         this.elementData = (Item[]) new Object[queueNumber];
 
     }
     
     // is the queue empty?
     public boolean isEmpty() {
         return objSize == 0;
     }
     
     // return the number of items on the queue
     public int size() {
         return objSize;
     }
     
     // resize array to new capacity
     private void resize(int capacity)
     {
         Item[] copy = (Item[]) new Object[capacity];
        copy = Arrays.copyOf(elementData, objSize);
         elementData = copy;
         arraySize = capacity;
     }
     
     private void exchange(int a, int b)
     {
         Item temp = elementData[a];
         elementData[a] = elementData[b];
         elementData[b] = temp;
     }
     
     
     // add the item
     public void enqueue(Item item) {
         if (item == null)
             throw new java.lang.NullPointerException(); 
        
         if (objSize == arraySize) {
             resize(arraySize*2);
         }
        
         elementData[objSize++] = item;

     }
     
     // delete and return a random item
     public Item dequeue() {
         if (objSize == 0)
             throw new java.util.NoSuchElementException();
         int last = objSize-1;
         
         if (last != 0)
         {
             int idx = StdRandom.uniform(objSize);
             
             if (idx !=  last)
                 exchange(idx, last);
         }
 
         
         Item out = elementData[last];
         elementData[last] = null;
         objSize--;
         if (objSize < arraySize/4)
             resize(arraySize/2);
         return out;
     }
     
     // return (but do not delete) a random item
     public Item sample() {
         if (objSize == 0)
             throw new java.util.NoSuchElementException();
         int idx = StdRandom.uniform(objSize);
         return elementData[idx];
     }
     
     private class RandomIterator implements Iterator<Item> {
         private int[] sequence;
         private int seqPointer;
         public RandomIterator() {
             sequence = new int [objSize];
             for (int i = 0; i < objSize; i++)
                 sequence[i] = i;
             StdRandom.shuffle(sequence);
             seqPointer = 0;
             
         }
         public void remove() {
             throw new java.lang.UnsupportedOperationException();
         }
         public boolean hasNext() {
             return seqPointer < objSize;
         }
         public Item next() {
             if (seqPointer >= objSize)
                 throw new java.util.NoSuchElementException();
             Item out = elementData[sequence[seqPointer]];
             seqPointer++;
             return out;
         }
     
     }
     
     // return an independent iterator over items in random order
     public Iterator<Item> iterator() {
         return new RandomIterator();
     }
     
 }
