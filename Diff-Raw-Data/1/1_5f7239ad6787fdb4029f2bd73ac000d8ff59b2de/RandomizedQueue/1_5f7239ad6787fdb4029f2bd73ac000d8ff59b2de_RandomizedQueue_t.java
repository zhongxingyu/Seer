 import java.util.Iterator;
 
 public class RandomizedQueue<Item> implements Iterable<Item> {
     private Item[] data;
     private int sz;
     private int capacity;
 
     public RandomizedQueue() {
         this.sz = 0;
         this.capacity = 1;
         data = (Item[]) new Object[1];
     }
 
     public boolean isEmpty() {
         return size() == 0;
     }
 
     public int size() {
         return sz;
     }
 
     public void enqueue(Item item) {
         if (item == null) {
             throw new java.lang.NullPointerException();
         }
 
         if (size() == capacity) {
             increaseCapacity();
         }
 
         data[sz++] = item;
         swapLastWithAnotherUniformly();
     }
 
     public Item dequeue() {
         checkEmpty();
         swapLastWithAnotherUniformly();
         Item result = data[--sz];
        data[sz] = null;
 
         if (size() * 4 == capacity) {
             decreaseCapacity();
         }
 
         return result;
     }
 
     public Item sample() {
         checkEmpty();
         swapLastWithAnotherUniformly();
         return data[size() - 1];
     }
 
     public Iterator<Item> iterator() {
         return new RandomizedQueueIterator();
     }
 
     public static void main(String[] args) {
     }
 
     private void increaseCapacity() {
         Item[] oldData = data;
         Item[] newData = reallocateNewArrayAndCopyOldData(data.length * 2);
 
         capacity *= 2;
         data = newData;
         oldData = null;
     }
 
     private void decreaseCapacity() {
         Item[] oldData = data;
         Item[] newData = reallocateNewArrayAndCopyOldData(data.length / 2);
 
         capacity /= 2;
         data = newData;
         oldData = null;
     }
 
     private Item[] reallocateNewArrayAndCopyOldData(int newSize) {
         Item[] newData = (Item[]) new Object[newSize];
         for (int i = 0; i < newSize; ++i) {
             if (i < size()) {
                 newData[i] = data[i];
             } else {
                 newData[i] = null;
             }
         }
 
         return newData;
     }
 
     private void swapLastWithAnotherUniformly() {
         swapItems(StdRandom.uniform(size()), size() - 1);
     }
 
     private void swapItems(int firstIndex, int secondIndex) {
         Item temp = data[firstIndex];
         data[firstIndex] = data[secondIndex];
         data[secondIndex] = temp;
         temp = null;
     }
 
     private void checkEmpty() {
         if (isEmpty()) {
             throw new java.util.NoSuchElementException();
         }
     }
 
     private class RandomizedQueueIterator implements Iterator<Item> {
         private int current;
         private int[] order;
 
         public RandomizedQueueIterator() {
             this.current = 0;
             this.order = new int[size()];
             for (int i = 0; i < size(); ++i) {
                 order[i] = i;
             }
             StdRandom.shuffle(order);
         }
 
         public boolean hasNext() {
             return current != size();
         }
 
         public Item next() {
             if (!hasNext()) {
                 throw new java.util.NoSuchElementException();
             }
 
             return data[order[current++]];
         }
 
         public void remove() {
             throw new java.lang.UnsupportedOperationException();
         }
     }
 }
