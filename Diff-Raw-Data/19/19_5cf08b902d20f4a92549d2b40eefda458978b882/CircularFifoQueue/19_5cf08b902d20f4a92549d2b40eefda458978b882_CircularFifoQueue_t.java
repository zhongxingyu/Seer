 package it.polimi.elet.selflet.utilities;
 
 import java.util.Iterator;
 
import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
 import org.apache.commons.collections.buffer.CircularFifoBuffer;
 
 /**
  * A generified version of <code>CircularFifoBuffer</code>
  * 
  * @author Nicola Calcavecchia <calcavecchia@gmail.com>
  * */
 public class CircularFifoQueue<T> implements Iterable<T> {
 
	private final Buffer buffer;
	private int maxSize;
 
 	public CircularFifoQueue(int size) {
		maxSize = size;
		buffer = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(maxSize));
 	}
 
 	/**
 	 * Sets an initial value for the queue
 	 * */
 	public CircularFifoQueue(int size, T initialValue) {
		maxSize = size;
		buffer = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(maxSize));
 		init(initialValue);
 	}
 
 	private void init(T initialValue) {
		for (int i = 0; i < maxSize; i++) {
 			add(initialValue);
 		}
 	}
 
	@SuppressWarnings("unchecked")
 	public boolean add(T data) {
 		return buffer.add(data);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Iterator<T> iterator() {
 		return buffer.iterator();
 	}
 
 }
