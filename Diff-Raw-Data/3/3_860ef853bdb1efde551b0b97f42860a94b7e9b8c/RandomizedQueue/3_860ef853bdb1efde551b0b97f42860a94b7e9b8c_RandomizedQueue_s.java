 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 public class RandomizedQueue<Item> implements Iterable<Item> {
 	private static int INIT_COUNT = 100;
 	private int size = 0;
 	private Item[] items = null;
 
 	public RandomizedQueue() { // construct an empty randomized queue
 		size = 0;
 		items = (Item[]) new Object[INIT_COUNT];
 	}
 
 	public boolean isEmpty() { // is the queue empty?
 		return size == 0;
 	}
 
 	public int size() { // return the number of items on the queue
 		return size;
 	}
 
 	public void enqueue(Item item) { // add the item
 		if(item == null)
 			throw new NullPointerException();
 		
 		if (size == items.length) {
 			Item[] newItems = (Item[]) new Object[size * 2];
 			for (int i = 0; i < size; i++)
 				newItems[i] = items[i];
 			items = newItems;
 		}
 		items[size++] = item;
 	}
 
 	public Item dequeue() { // delete and return a random item
 		int index = StdRandom.uniform(0, size);
 		Item result = items[index];
 		if (size - 1 < items.length / 4) {
 			Item[] newItems = (Item[]) new Object[items.length / 2];
 			for (int i = 0, j = 0; i < size; i++) {
 				if (i != index)
 					newItems[j++] = items[i];
 			}
 			items = newItems;
 		}
 		size -= 1;
 		return result;
 	}
 
 	public Item sample() { // return (but do not delete){a random item
 		if (size == 0)
 			throw new NoSuchElementException();
 		
 		int index = StdRandom.uniform(0, size);
 		return items[index];
 	}
 
 	private class RandomizedQueueIterator implements Iterator<Item> {
 		private int index = 0;
 		private int[] order = null;
 
 		public RandomizedQueueIterator() {
 			index = 0;
 			order = new int[size];
 			for (int i = 0; i < size; i++)
 				order[i] = i;
 			StdRandom.shuffle(order);
 		}
 
 		public boolean hasNext() {
 			return index != order.length;
 		}
 
 		@Override
 		public Item next() {
 			if (index == order.length)
 				throw new NoSuchElementException();
 
 			Item item = items[order[index]];
 			index += 1;
 			return item;
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 	}
 
 	public Iterator<Item> iterator() { // return an independent iterator over
 										// items in random order
 		return new RandomizedQueueIterator();
 	}
 }
