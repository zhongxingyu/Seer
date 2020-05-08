 /** ProcessChild
  * 
  * Wraps a process that the ProcessRunner is running so that the ProcessRunner
  * can determine if the process has completed or terminated
  * 
  * @author Tyler Healy (thealy)
  */
 public class ProcessChild {
 	
 	//The MigratableProcessWrapper that contains the process to be run
 	private MigratableProcessWrapper mpw;
 	
 	//The thread in which the ProcessChild will run
 	private Thread thread;
 	
 	//Indicates if the process has completed
 	private boolean isComplete = false;
 	
 	//Indicates if the process has been terminated (i.e. has been moved)
 	private boolean isTerminated = false;
 	
 	/** ProcessChild(MigratableProcessWrapper mpw)
 	 * 
 	 * Constructor for ProcessChild
 	 * @param mpw - The MigratableProcessWrapper that contains the process to be
 	 *              run
 	 */
 	public ProcessChild(MigratableProcessWrapper mpw) {
 		this.mpw = mpw;
 	}
 	
 	/** start()
 	 * 
 	 * Starts the process in its own thread
 	 * @throws Exception - thrown when start is attempted and the process is
 	 * 					   already running
 	 */
 	public synchronized void start() throws Exception {
 		if (thread != null) {
 			throw new Exception("Process has already started.");
 		}
 		
 		thread = new Thread(new Runnable() {
 			@Override
 			/** run()
 			 * 
 			 * The run function of the runnable object within the thread
 			 * Runs the process being managed by ProcessManager
 			 */
 			public void run() {
 				try {
 					mpw.start();
 					mpw.getThread().join();
 					isComplete = true;
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		
 		thread.start();
 	}
 	
 	/**stop()
 	 * 
 	 * Stops the current process from running by stopping the
 	 * MigratableProcessWrapper and getting rid of the thread
 	 */
 	public synchronized void stop() {
 		if (thread == null) {
 			return;
 		}
 		
 		mpw.stop();
 		isTerminated = true;
		isComplete = false;
 		thread = null;
 	}
 	
 	/** getMPW()
 	 * 
 	 * @return the MigratableProcessWrapper containing the process to be run
 	 */
 	public synchronized MigratableProcessWrapper getMPW() {
 		return mpw;
 	}
 	
 	/** isComplete
 	 * 
 	 * @return true if the process has completed, false otherwise
 	 */
 	public synchronized boolean isComplete() {
 		return isComplete;
 	}
 	
 	/** isTerminated()
 	 * 
 	 * @return true if the process has terminated (i.e. has been moved) and
 	 *         false otherwise
 	 */
 	public synchronized boolean isTerminated() {
 		return isTerminated;
 	}
 }
