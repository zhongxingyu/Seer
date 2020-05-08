 import java.util.ArrayList;
 
 /** ProcessRunner
  * 
  * Class designed to manage and run processes in parallel with the
  * ProcessManager accepting commands
  * 
  * @author Tyler Healy (thealy)
  */
 public class ProcessRunner {
 	
 	//Boolean to detect if thread should still run
 	private volatile boolean running;
 	
 	//Thread on which this process runs
 	private Thread thread;
 	
 	//List of threads being managed by the ProcessManager
 	private ArrayList<ProcessChild> processes = new ArrayList<ProcessChild>();
 	
 	/** start()
 	 * 
 	 * Creates a thread to run the ProcessRunner
 	 * Runs processes within this thread
 	 * 
 	 * @throws Exception - if start is run once the thread has already started
 	 */
 	public synchronized void start() throws Exception {
 		if (thread != null) {
 			throw new Exception("ProcessRunner already started.");
 		}
 			
 		thread = new Thread(new Runnable() {
 			@Override
 			/** run()
 			 * 
 			 * The run function of the runnable object within the thread
 			 * Checks to see if any ProcessChildren have completed or terminated
 			 */
 			public void run() {
 				running = true;
 				while(running) {
 					for (int i = 0; i < processes.size(); i++) {
 						ProcessChild cur = processes.get(i);
						if (cur.isComplete()) {
							System.out.println(cur.getMPW().getName() + " has terminated");
							processes.remove(i);
						} else if (cur.isTerminated()) {
							processes.remove(i);
 						}
 					}
 				}
 			}
 		});
 		
 		thread.start();
 	}
 	
 	/** stop()
 	 * 
 	 * Stops the ProcessRunner by setting the running boolean to false
 	 */
 	public synchronized void stop() {
 		for (int i = 0; i < processes.size(); i++) {
 			processes.get(i).stop();
 		}
 		
 		running = false;
 		thread = null;
 	}
 	
 	/** addThread(Thread t)
 	 * 
 	 * Adds a thread to the list being managed by the ProcessRunner
 	 * Starts the process
 	 * @param t - Thread to be added to the list
 	 */
 	public synchronized void addThread(MigratableProcessWrapper t) {
 		ProcessChild pc = new ProcessChild(t);
 		processes.add(pc);
 		try {
 			pc.start();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/** printProcesses()
 	 * 
 	 * Prints the list of processes being managed by this ProcessRunner
 	 * Called as a result of a ps command to a ProcessManager
 	 */
 	public synchronized void printProcesses() {
 		if (processes.size() == 0) {
 			System.out.println("No processes running.");
 			return;
 		}
 		
 		for (int i = 0; i < processes.size(); i++) {
 			MigratableProcessWrapper cur = processes.get(i).getMPW();
 			System.out.println(cur.getName());
 		}
 	}
 	
 	/** getLast()
 	 * 
 	 * Returns a Thread to be migrated
 	 * @return the last Thread in the list being managed by the ProcessRunner
 	 */
 	public synchronized MigratableProcessWrapper getLast() {
 		int size;
 		if ((size = processes.size()) == 0) {
 			return null;
 		} 
 		
		processes.get(size - 1).stop();
		return processes.get(size - 1).getMPW();
 	}
 	
 	/** getSize()
 	 * 
 	 * @return The number of processes running under this ProcessRunner
 	 */
 	public synchronized int getSize() {
 		return processes.size();
 	}
 }
