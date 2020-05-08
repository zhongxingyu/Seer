 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
public class RandomizedQueue<Item> {
 
	private static final int MIN_COUNT = 10;
	private Object[] items;
	private int size;
 
 	/**
 	 * construct an empty randomized queue
 	 */
 	public RandomizedQueue() {
 		items = new Object[MIN_COUNT];
 		size = 0;
 	}
 
 	/**
 	 * is the queue empty?
 	 * 
 	 * @return
 	 */
 	public boolean isEmpty() {
 		return size == 0;
 
 	}
 
 	/**
 	 * return the number of items on the queue
 	 * 
 	 * @return
 	 */
 	public int size() {
 		return size;
 	}
 
 	/**
 	 * add the item
 	 * 
 	 * @param item
 	 */
 	public void enqueue(Item item) {
 		if (item == null) {
 			throw new NullPointerException();
 		}
 		if (size + 1 == items.length) {
 			int newCapacity = items.length << 1;
 			Object[] copy = new Object[newCapacity];
 			System.arraycopy(items, 0, copy, 0, items.length);
 			items = copy;
 		}
 		items[size] = item;
 		size++;
 	}
 
 	/**
 	 * delete and return a random item
 	 * 
 	 * @return
 	 */
 	public Item dequeue() {
 		if (size == 0) {
 			throw new NoSuchElementException();
 		}
 		int pos = StdRandom.uniform(size);
 		Item item = (Item) items[pos];
 		items[pos] = items[--size];
 		if (size < items.length / 3 && items.length > MIN_COUNT) {
 			int newCapacity = items.length >> 1;
 			Object[] copy = new Object[newCapacity];
 			System.arraycopy(items, 0, copy, 0, copy.length);
 			items = copy;
 		}
 		return item;
 	}
 
 	/**
 	 * return (but do not delete) a random item
 	 * 
 	 * @return
 	 */
 	public Item sample() {
 		if (size == 0) {
 			throw new NoSuchElementException();
 		}
 		return (Item) items[StdRandom.uniform(size)];
 	}
 
 	/**
 	 * return an independent iterator over items in random order
 	 * 
 	 * @return
 	 */
 	public Iterator<Item> iterator() {
 		return new RandomizedQueueIterator<Item>();
 	}
 
 	private final class RandomizedQueueIterator<T> implements Iterator<T> {
 		
 		private Object[] elements; 
 		private int current;
 		
 		public RandomizedQueueIterator() {
 			elements = new Object[size];
 			current = 0;
 			if (size > 0) {
 				elements[0] = items[0];
 				//FisherYates shuffle, The "inside-out" algorithm
 				for (int i = 1; i < elements.length; i++) {
 					int j = StdRandom.uniform(i+1);
 					elements[i] = elements[j];
 					elements[j] = items[i];
 				}
 			}
 		}
 		@Override
 		public boolean hasNext() {
 			return current < elements.length;
 		}
 
 		@Override
 		public T next() {
 			if (!hasNext()) {
 				throw new NoSuchElementException();
 			}
			T t = (T)elements[current];
 			current++;
 			return t;
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 		
 	}
 }
