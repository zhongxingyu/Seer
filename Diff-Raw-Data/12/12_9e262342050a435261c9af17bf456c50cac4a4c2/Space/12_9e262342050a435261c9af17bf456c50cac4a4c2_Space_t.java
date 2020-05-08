 package api;
 
import java.util.concurrent.LinkedBlockingQueue;

 /**
  * A central resource allocator for all tasks executed on remote servers
  * 
  * @author Manasa Chandrasekhar
  * @author Kowshik Prakasam
  * 
  */
 public interface Space extends java.rmi.Remote {
	
	public LinkedBlockingQueue<Task<?>> listTasks = new LinkedBlockingQueue<Task<?>>();
 	/**
 	 * Name of the exposed service
 	 */
 	String SERVICE_NAME = "Space";
 
 	/**
 	 * The client decomposes the problem into a set of Task objects, and passes
 	 * them to the Space via the put method. In principle, these task objects
 	 * can be processed in parallel by Computers.
 	 * 
 	 * @param <T>
 	 * @param task
 	 * @throws java.rmi.RemoteException
 	 */
 
 	<T> void put(Task<T> task) throws java.rmi.RemoteException;
 
 	/**
 	 * After passing all the task objects to the Space, the client retrieves the
 	 * associated Result objects via the take method. This method blocks until a
 	 * Result is available to return the the client. Thus, if the client sent 10
 	 * Task objects to the Space, it could execute:
 	 * 
 	 * Result[] results = new Result[10]; for ( int i = 0; i < results.length;
 	 * i++ ) { results[i] = takeResult(); // waits for a result to become
 	 * available. }
 	 * 
 	 * @param <T>
 	 * @return One of the results of the computation
 	 * @throws java.rmi.RemoteException
 	 */
 	<T> Result<T> takeResult() throws java.rmi.RemoteException;
 }
