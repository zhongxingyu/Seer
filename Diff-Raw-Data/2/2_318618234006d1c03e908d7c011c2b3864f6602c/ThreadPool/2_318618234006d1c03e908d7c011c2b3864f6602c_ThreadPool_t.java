 package dfh.thread.pool;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 /**
  * Very minimal thread pool class to facilitate parallelizing simple
  * in-application tasks. If you're creating a web server or something such you
  * should look for something more battle tested and featureful. This class just
  * accepts {@link Runnable} pieces of work and performs them when a core becomes
  * available. It is up to the user to ensure the runnable bits can be run
  * asynchronously. Use {@link #flush()} to complete all pending work when
  * necessary.
  * <p>
  * <b>Creation date:</b> Feb 19, 2011
  * 
  * @author David Houghton
  * 
  */
 public class ThreadPool {
 	/**
 	 * An object to ensure this class's threads will not outlive the class
 	 * itself.
 	 */
 	static final List<PoolThread> unloader = new LinkedList<PoolThread>();
 
 	/**
 	 * This method must be called at the appropriate moment in your
 	 * application's life cycle if the application may be stopped while the VM
 	 * keeps going. For example, if you use {@link ThreadPool} in a web
 	 * application, if the application is undeployed your servlet must call this
 	 * method when it's destroy method is called.
 	 */
 	public static void destroy() {
 		for (PoolThread t : unloader)
 			t.apoptosis();
 	}
 
 	private static class PoolThread extends Thread {
 		private boolean done = false;
 
 		/**
 		 * Start thread immediately upon construction.
 		 */
 		PoolThread() {
 			super();
 			setDaemon(true);
 			start();
 			unloader.add(this);
 		}
 
 		/**
 		 * Causes thread to start shutting down.
 		 */
 		synchronized void apoptosis() {
 			done = true;
 			interrupt();
 		}
 
 		private Runnable r;
 
 		@Override
 		public void run() {
 			while (true) {
 				synchronized (this) {
 					while (!done && r == null) {
 						try {
 							wait();
 						} catch (InterruptedException e) {
 							if (!done)
 								e.printStackTrace();
 						}
 					}
 				}
 				if (done)
 					break;
 				r.run();
 				r = null;
 				synchronized (pool) {
 					pool.add(this);
 					pool.notify();
 				}
 			}
 		}
 
 		synchronized void setR(Runnable r) {
 			this.r = r;
 			notify();
 		}
 	}
 
 	private static int poolSize = -1;
 	private static final Queue<PoolThread> pool = new LinkedList<PoolThread>();
 	private static boolean singleThreaded = false;
 
 	/**
 	 * The default number of threads will be the number of available processors.
 	 * If you want something else -- perhaps you already have a mess of threads
 	 * running -- call this method before enqueuing any work. Either setting the
 	 * pool size or enqueuing work will initialize the thread pool. After that
 	 * is done there is no resizing.
 	 * 
 	 * @param poolSize
 	 *            number of concurrent threads
 	 * @throws ThreadPoolException
 	 */
 	public static void setPoolSize(int poolSize) throws ThreadPoolException {
 		synchronized (pool) {
			if (ThreadPool.poolSize == -1) {
 				if (poolSize < 1)
 					throw new ThreadPoolException("pool size must be positive");
 				ThreadPool.poolSize = poolSize;
 				init();
 			} else
 				throw new ThreadPoolException("pool already initialized");
 		}
 	}
 
 	/**
 	 * Returns maximum number of concurrent threads in this pool. This will be
 	 * -1 until the pool is initialized or {@link #setSingleThreaded(boolean)}
 	 * is called; 0 after {@link #setSingleThreaded(boolean)} is called.
 	 * 
 	 * @return maximum number of concurrent threads in this pool
 	 */
 	public static int getPoolSize() {
 		synchronized (pool) {
 			return poolSize;
 		}
 	}
 
 	/**
 	 * Put a piece of work on the queue. This will block until a thread is
 	 * available.
 	 * 
 	 * @param r
 	 *            work to do
 	 */
 	public static void enqueue(Runnable r) {
 		if (singleThreaded)
 			r.run();
 		else {
 			PoolThread t = null;
 			synchronized (pool) {
 				if (poolSize == -1) {
 					poolSize = Runtime.getRuntime().availableProcessors();
 					init();
 				}
 				while (pool.isEmpty()) {
 					try {
 						pool.wait();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 				t = pool.remove();
 			}
 			t.setR(r);
 		}
 	}
 
 	/**
 	 * Block until all currently enqueued work is completed.
 	 */
 	public static void flush() {
 		synchronized (pool) {
 			while (pool.size() < poolSize) {
 				try {
 					pool.wait();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Lazy initialization of pool to allow user to set pool size if desired.
 	 */
 	private static void init() {
 		for (int i = 0; i < poolSize; i++)
 			pool.add(new PoolThread());
 	}
 
 	/**
 	 * Set <code>ThreadPool</code> to use a single thread. This is useful for
 	 * debugging. An exception will be thrown if you attempt to set this
 	 * property after the pool has been initialized.
 	 * 
 	 * @param singleThreaded
 	 * @throws ThreadPoolException
 	 */
 	public static void setSingleThreaded(boolean singleThreaded)
 			throws ThreadPoolException {
 		synchronized (pool) {
 			if (poolSize > -1)
 				throw new ThreadPoolException(
 						"setting single threaded after thread pool initialized");
 			ThreadPool.singleThreaded = singleThreaded;
 			poolSize = 0;
 		}
 	}
 
 	/**
 	 * @return whether <code>ThreadPool</code> is running in single-threaded
 	 *         debugging mode
 	 */
 	public static boolean isSingleThreaded() {
 		return singleThreaded;
 	}
 }
