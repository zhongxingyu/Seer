 package one.utils.concurrent;
 
 public interface OneExecutor {
 
 	/**
 	 * Returns an object representing the thread used to execute the runnable if
 	 * available.
 	 * 
 	 * @param runnable
 	 * @return
 	 */
 	public Object execute(Runnable runnable);
 
 	public interface WhenExecutorShutDown {
 
 		/**
 		 * Called when no threads spawned by this executor run anymore.
 		 */
 		public void thenDo();
 
 		public void onFailure(Throwable t);
 	}
 
 	/**
	 * Returns the current thread of the caller.
 	 * 
 	 * @return
 	 */
 	public Object getCurrentThread();
 
 	public void shutdown(WhenExecutorShutDown callback);
 
 }
