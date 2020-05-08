 package crescendo.base;
 
 import java.lang.Thread.UncaughtExceptionHandler;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * A Pool of constantly running threads that can be leveraged to expire notes.
  * This is a collection of threads which are started at the beginning of a song
  * so that we don't have to have the overhead of starting up a new thread every
  * time we need to use a thread to expire a note.
  * @author nickgartmann
  *
  */
 public class ThreadPool {
 
 	private int threadCount;
 	private int threadTimeout;
 	private List<Expirator> expirators;
 	private SongValidator validator;
 	
 	/**
 	 * Constructor
 	 * Initialize the state of the pool and start up all of the threads
 	 * @param validator Song validator object which will be using the pool. This object is notified when notes expire
 	 * @param poolSize number of threads to have in the pool
 	 * @param threadTimeout amount of time in milliseconds to allow before a note times out
 	 */
 	public ThreadPool(SongValidator validator,int poolSize,int threadTimeout) {
 		if(validator==null) {
 			throw new NullPointerException("Validator cannot be null");
 		} else if (poolSize<1) {
 			throw new IllegalArgumentException("Pool size must be greater than one");
 		} else if (threadTimeout<1) {
 			throw new IllegalArgumentException("Timeout must be greater than one");
 		}
 		this.validator = validator;
 		this.threadTimeout = threadTimeout;
 		threadCount = poolSize;
 		expirators = new ArrayList<Expirator>(threadCount);
 		
 		//Needed for matching threads to their runnable expirators in the exception handler
 		Map<Thread,Expirator> threadMap = new HashMap<Thread,Expirator>(); 
 		
 		//Start up all of the threads
 		for(int i=0;i<threadCount;i++) {
 			Expirator e = new Expirator(validator, this.threadTimeout);
 			Thread t = new Thread(e);
 			expirators.add(e);
 			threadMap.put(t, e);
 			t.start();
 		}
 	}
 
 	
 	/**
 	 * queries the pool for an expirator which isn't currently doing work
 	 * @return available expirator
 	 */
 	public Expirator getAvailableExpirator() {
 		Expirator available = null;
 		for(Expirator e : expirators) {
 			if(!e.isBusy()) {
 				available = e;
 				break;
 			}
 		}
 		return available;
 	}
 	
 	/**
 	 * Grabs only the busy expirators.
 	 */
 	public List<Expirator> getBusyExpirators()
 	{
 		List<Expirator> busy=new LinkedList<Expirator>();
 		for (Expirator current : this.expirators)
 		{
 			if (current.isBusy())
 			{
 				busy.add(current);
 			}
 		}
 		return busy;
 	}
 	
 	/**
 	 * suspends the expiration of all of the notes being expired in the pool
 	 */
 	public void pause() {
 		for(Expirator e : expirators) {
 			e.pause();
 		}
 	}
 
 	/**
 	 * resumes the expiration of all of the notes being expired in the pool
 	 */
 	public void resume() {
 		for(Expirator e : expirators) {
 			e.resume();
 		}
 	}
 	
 	/**
 	 * Stops all expiring notes.
 	 */
 	public void stop() {
 		for (Expirator e : expirators) {
 			e.stop();
 		}
 	}
 
 	/**
 	 * Stops the execution of all of the threads in the pool
 	 * this should only be used for cleaning up
 	 */
 	public void shutdown() {
 		for(Expirator e : expirators) {
			e.shutdown();
 		}
 		expirators.clear();
 	}
 }
