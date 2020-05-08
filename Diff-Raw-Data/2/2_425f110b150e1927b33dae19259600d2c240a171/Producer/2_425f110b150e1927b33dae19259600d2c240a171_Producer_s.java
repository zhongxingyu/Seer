 package uk.co.fredemmott.jp;
 
 import java.nio.ByteBuffer;
 
 public interface Producer<T> {
 
 	/**
 	 * add will add a job's message to the pool, a PoolException will be thrown if it is unable to do this.
 	 * 
 	 * @param message T of generic type to be added to the pool.
	 * @return boolean success of whether the job completed or not. This will determine
	 * if the job will be purged from the pool or not.
 	 * 
 	 * @throws PoolException is thrown if there is a problem adding to the pool.  
 	 */
 	public void add(T message) throws PoolException;
 	
 	/**
 	 * @param message byte[] message to be de-serialised to T.
 	 * @return generic type T which was de-serialised from the byte[] message.
 	 * 
 	 */
 	public ByteBuffer serialise(T message);
 }
