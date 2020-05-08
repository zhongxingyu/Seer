 import java.util.Iterator;
 
 /**
  * A randomized queue. Elements can be added and removed in constant ammortized time.
  * The element to be returned is selected uniformly at random.
 **/
 public class RandomizedQueue<Item> implements Iterable<Item> {
   /*
    * Store elements in a dynamically resizing array. Double the array size once the array fills up.
    * Shrink the array once it's 3/4 empty. This guarantees constant amortized work
    *
    * Use a queue to keep track of the indices that correspond to empty array cells.
    *
    * To retrieve a random element: select a random number between (0, array size - 1). If the cell
    * is empty, try again. Expected worst case work = 4 here since array is never less than 1/4 empty.
    *
    * To store an element: deque an index value from our emptyIndices queue, and store the element
    * at that index.
    */
 
   private Item[] array;
   private int N;
 
   // construct an empty randomized queue
   public RandomizedQueue() {
     array = (Item[]) new Object[2];
     N = 0;
   }
 
   // is the queue empty?
   public boolean isEmpty() { return N == 0; }
 
   // return the number of items on the queue
   public int size() { return N; }
 
   // add the item
   public void enqueue(Item item) {
     if (item == null) throw new java.lang.NullPointerException();
     if (N == array.length) resize(2*array.length);
     array[N] = item;
     swap(N, StdRandom.uniform(0,N+1));
     N++;
   }
 
   // delete and return a random item
   public Item dequeue() {
     if (isEmpty()) throw new java.util.NoSuchElementException();
     Item ret = array[N-1];
     array[N-1] = null;
     N--;
     if (N > 0 && N == array.length/4) resize(array.length/2);
     return ret;
   }
 
   // return (but do not delete) a random item
   public Item sample() {
     if (isEmpty()) throw new java.util.NoSuchElementException();
    return array[N-1];
   }
 
   private void resize(int capacity) {
     assert capacity >= N;
     Item[] temp = (Item[]) new Object[capacity];
     for (int i = 0; i < N; i++) {
       temp[i] = array[i];
     }
     array = temp;
   }
 
   private void swap(int a, int b) {
     Item temp = array[a];
     array[a] = array[b];
     array[b] = temp;
   }
 
   // return an independent iterator over items in random order
   @Override
   public Iterator<Item> iterator() {
     return new RandIterator();
   }
 
   private class RandIterator implements Iterator<Item> {
     int[] indexArray = new int[N];
     int count = 0;
 
     public RandIterator() {
       int i = 0;
       // get indices of all items
       for (int j = 0; j < N; j++) {
         indexArray[i] = j;
         i++;
       }
 
       StdRandom.shuffle(indexArray);
     }
 
     @Override
     public boolean hasNext() {
       return count < N;
     }
 
     @Override
     public Item next() {
       if (count == N) throw new java.util.NoSuchElementException();
       Item ret = array[indexArray[count]];
       count++;
       return ret;
     }
 
     @Override
     public void remove() {
       throw new java.lang.UnsupportedOperationException();
     }
 
   }
 
   public static void main(String args[]) {
     RandomizedQueue<String> Q = new RandomizedQueue<String>();
      for (Integer i = 1; i < 200; i++) {
      Q.enqueue(Integer.toString(i));
      }
      for (Integer i = 1; i < 190; i++) {
      String s = Q.dequeue();
      }
 
     for (String x : Q) {
       StdOut.print(x + " ");
     }
     StdOut.println("");
 
     int max = Q.size();
     for (int i = 0; i < max/2; i++) {
       String x = Q.dequeue();
       StdOut.print(x + " ");
     }
 
     StdOut.println("");
 
     for (String x : Q) {
       StdOut.print(x + " ");
     }
   }
 }
