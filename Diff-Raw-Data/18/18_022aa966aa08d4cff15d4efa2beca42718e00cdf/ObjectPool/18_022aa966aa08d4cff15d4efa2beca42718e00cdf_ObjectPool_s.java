 package com.barchart.http.server;
 
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.Callable;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * A pool of reusable objects which creates new instances on demand.
  * 
  * @param <T>
  *            The poolable object type
  */
 public class ObjectPool<T> {
 
 	// TODO replace with something more efficient
 	/* The backing queue */
 	private final BlockingQueue<T> objectPool;
 
 	/* User-provided object creator callback */
 	private final Callable<T> objectCreator;
 
 	/* Number of pool objects that have been created */
 	private final AtomicInteger created = new AtomicInteger(0);
 
 	/* Maximum size of this pool */
 	private final int maxObjects;
 
 	/**
 	 * Create an unbounded object pool.
 	 */
 	public ObjectPool(final Callable<T> creator_) {
		maxObjects = -1;
		objectCreator = creator_;
		objectPool = new LinkedBlockingQueue<T>();
 	}
 
 	/**
 	 * Create a fixed-size object pool.
 	 */
 	public ObjectPool(final int maxObjects_, final Callable<T> creator_) {
 		maxObjects = maxObjects_;
 		objectCreator = creator_;
		objectPool = new ArrayBlockingQueue<T>(maxObjects);
 	}
 
 	/**
 	 * Take an object from the pool if available.
 	 * 
 	 * @return The pooled object, or null if none are available
 	 */
 	public T poll() {
 		final T obj = getOrCreate();
 		if (obj != null) {
 			return obj;
 		}
 		return objectPool.poll();
 	}
 
 	/**
 	 * Take an object from the pool, waiting the specified time for one to
 	 * become available.
 	 * 
 	 * @param timeout
 	 *            The time to wait
 	 * @param units
 	 *            The time units
 	 * @return The pooled object
 	 * @throws InterruptedException
 	 *             If the thread is interrupted while waiting for an object
 	 */
 	public T poll(final long timeout, final TimeUnit units)
 			throws InterruptedException {
 		final T obj = getOrCreate();
 		if (obj != null) {
 			return obj;
 		}
 		return objectPool.poll(timeout, units);
 	}
 
 	/**
 	 * Take an object from the pool, blocking until one becomes available.
 	 * 
 	 * @return The pooled object
 	 * @throws InterruptedException
 	 *             If the thread is interrupted while waiting for an object
 	 */
 	public T take() throws InterruptedException {
 		final T obj = getOrCreate();
 		if (obj != null) {
 			return obj;
 		}
 		return objectPool.take();
 	}
 
 	/**
 	 * Get an object from the pool. If none are available and the pool is not
 	 * full, create a new object and return it.
 	 * 
 	 * @return The pooled object, or null if none are available and the pool is
 	 *         full
 	 */
 	protected T getOrCreate() {
 
 		// First just try to pull an object off the queue
 		final T instance = objectPool.poll();
 
 		if (instance == null) {
 
 			int count = created.get();
 
 			// Try to increase the counter as long as the pool is not full
 			while (maxObjects == -1 || count < maxObjects) {
 
 				if (created.compareAndSet(count, count + 1)) {
 
 					try {
 
 						// If successful, we can create a new object.
 						return objectCreator.call();
 
 					} catch (final Exception e) {
 
 						// Create failed, restore counter
 						created.decrementAndGet();
 
 						throw new RuntimeException(
 								"Unhandled exception in object creator", e);
 
 					}
 
 				}
 
 				// compareAndSet() failed, get new size and try again until the
 				// pool is full
 				count = created.get();
 
 			}
 
 		}
 
 		return instance;
 
 	}
 
 	/**
 	 * Return an object to the pool.
 	 * 
 	 * @param object
 	 */
 	public void give(final T object) {
 		if (!objectPool.offer(object)) {
 			throw new IllegalStateException(
 					"Attempted to return an object to a full pool. "
 							+ "Only return objects created by this pool.");
 		}
 	}
 
 }
