 package it.polimi.elet.selflet.utilities;
 
 import java.util.Iterator;
 
 import org.apache.commons.collections.buffer.CircularFifoBuffer;
 
 /**
  * A generified version of <code>CircularFifoBuffer</code>
  * 
  * @author Nicola Calcavecchia <calcavecchia@gmail.com>
  * */
 public class CircularFifoQueue<T> implements Iterable<T> {
 
	private final CircularFifoBuffer buffer;
 
 	public CircularFifoQueue(int size) {
		buffer = new CircularFifoBuffer(size);
 	}
 
 	/**
 	 * Sets an initial value for the queue
 	 * */
 	public CircularFifoQueue(int size, T initialValue) {
		buffer = new CircularFifoBuffer(size);
 		init(initialValue);
 	}
 
 	private void init(T initialValue) {
		for (int i = 0; i < buffer.maxSize(); i++) {
 			add(initialValue);
 		}
 	}
 
 	public boolean add(T data) {
 		return buffer.add(data);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Iterator<T> iterator() {
 		return buffer.iterator();
 	}
 
 }
