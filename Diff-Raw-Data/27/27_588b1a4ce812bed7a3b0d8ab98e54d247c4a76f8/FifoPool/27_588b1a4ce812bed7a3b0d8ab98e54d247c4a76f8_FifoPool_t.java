 package org.racenet.framework;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * First-in-first-out pool of reusable objects
  *
  * @author soh#zolex
  *
  * @param <T>
  */
 public class FifoPool<T> {
 
 	/**
 	 * Factory interface
 	 *
 	 * @param <T>
 	 */
 	public interface PoolObjectFactory<T> {
 		
 		public T createObject();
 	}
 	
	public final List<T> pool;
 	private final PoolObjectFactory<T> factory;
 	private final int maxSize;
 	private int reuseIndex = 0;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param PoolObjectFactory<T> f
 	 * @param int s
 	 */
 	public FifoPool(PoolObjectFactory<T> f, int s) {
 		
 		factory = f;
 		maxSize = s;
 		pool = new ArrayList<T>(maxSize);
 	}
 
 	/**
 	 * Get the size of the pool
 	 * 
 	 * @return int
 	 */
 	public int length() {
 		
 		return pool.size();
 	}
 	
 	/**
 	 * Get an object from the pool
 	 * 
 	 * @param int index
 	 * @return T
 	 */
 	public T get(int index) {
 		
 		return pool.get(index);
 	}
 	
 	/**
 	 * Create and return a new Object in the pool
 	 * or return the oldest one
 	 * 
 	 * @return T
 	 */
 	public T newObject() {
 		
 		T object = null;
 		if (pool.size() == maxSize) {
 			
 			object = pool.get(reuseIndex);
 			reuseIndex++;
 			if (reuseIndex == maxSize) {
 				
 				reuseIndex = 0;
 			}
 			
 		} else {
 			
 			object = factory.createObject();
 			pool.add(object);
 		}
 		
 		return object;
 	}
 	
 	/**
 	 * Empty the pool
 	 */
 	public void free() {
 		
 		pool.clear();
 	}
 }
