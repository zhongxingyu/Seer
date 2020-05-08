 package careercup.google;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Created by sobercheg on 12/10/13.
  * A real Google phone interview question.
  */
 
 interface Receiver<T> {
     void receive(T object);
 }
 
 interface Stream<T extends Comparable<T>> {
     T next();
 
     T peek();
 
     boolean hasNext();
 }
 
 class IntegerStream implements Stream<Integer> {
     List<Integer> integers;
     Iterator<Integer> iterator;
     int currentValue;
 
     IntegerStream(List<Integer> integers) {
         this.integers = integers;
         iterator = integers.iterator();
         currentValue = Integer.MIN_VALUE;
     }
 
     @Override
     public Integer next() {
         Integer nextValue = iterator.next();
         currentValue = nextValue;
         return nextValue;
     }
 
     @Override
     public Integer peek() {
         return currentValue;
     }
 
     @Override
     public boolean hasNext() {
         return iterator.hasNext();
     }

    @Override
    public String toString() {
        return "" + currentValue;
    }
 }
 
 class StreamHeap<T extends Comparable<T>> {
     private Stream<T>[] elements;
     private int size;
 
     public StreamHeap(Collection<? extends Stream<T>> streams) {
         this.elements = (Stream<T>[]) new Stream[streams.size()];
         this.size = 0;
     }
 
     public boolean hasNext() {
         return size > 0;
     }
 
     public Stream<T> getNext() {
         if (!hasNext()) return null;
 
         Stream<T> min = elements[0];
         size--;
         elements[0] = elements[size];
 
         siftDown(0);
 
         return min;
     }
 
     private void siftDown(int i) {
         int newIndex = i;
        if (left(i) < size && getPeek(i).compareTo(getPeek(left(i))) > 0) {
             newIndex = left(i);
         }
 
        if (right(i) < size && getPeek(newIndex).compareTo(getPeek(right(i))) > 0) {
             newIndex = right(i);
         }
 
         if (i != newIndex) {
             swap(i, newIndex);
             siftDown(newIndex);
         }
     }
 
     private T getPeek(int index) {
         return elements[index].peek();
     }
 
     private void swap(int i, int newIndex) {
         Stream<T> tmp = elements[newIndex];
         elements[newIndex] = elements[i];
         elements[i] = tmp;
     }
 
     private int parent(int index) {
         return (index - 1) / 2;
     }
 
     private int left(int index) {
         return index * 2 + 1;
     }
 
     private int right(int index) {
         return index * 2 + 2;
     }
 
     public void add(Stream<T> element) {
         elements[size] = element;
         siftUp(size);
         size++;
     }
 
     private void siftUp(int index) {
         if (index == 0) return;
         if (elements[index].peek().compareTo(elements[parent(index)].peek()) < 0) {
             swap(index, parent(index));
             siftUp(parent(index));
         }
     }
 }
 
 public class MergeSortedStreams<T extends Comparable<T>> {
 
     public void mergeStreams(List<? extends Stream<T>> streams, Receiver<T> receiver) {
         StreamHeap<T> heap = new StreamHeap<T>(streams);
         for (Stream<T> stream : streams) {
             if (stream.hasNext()) {
                 stream.next();
                 heap.add(stream);
             }
         }
 
         while (heap.hasNext()) {
             Stream<T> stream = heap.getNext();
             receiver.receive(stream.peek());
             if (!stream.hasNext()) continue;
             stream.next();
             heap.add(stream);
         }
     }
 
     public static void main(String[] args) {
         MergeSortedStreams<Integer> mergeSortedStreams = new MergeSortedStreams<Integer>();
         IntegerStream stream1 = new IntegerStream(Arrays.asList(1, 5, 10, 50));
         IntegerStream stream2 = new IntegerStream(Arrays.asList(3, 500));
         IntegerStream stream3 = new IntegerStream(Arrays.asList(2, 75, 200, 300));
        IntegerStream stream4 = new IntegerStream(Arrays.asList(20));
        mergeSortedStreams.mergeStreams(Arrays.asList(stream1, stream2, stream3, stream4), new Receiver<Integer>() {
             @Override
             public void receive(Integer object) {
                 System.out.println(object);
             }
         });
 
     }
 }
