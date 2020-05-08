 import java.io.IOException;
 import java.util.ArrayList;
 
 /** Load Manager
  * 
  * Process run by Master ProcessManager
  * Responsible for communicating with slave ProcessManagers every 5 seconds
  * Asks for the number of processes, and uses this information to balance the
  * load
  * Is able to receive processes from slaves and ship them to other slaves
  * 
  * @author Tyler Healy (thealy)
  */
 public class LoadManager {
 	
 	//Thread in which the process will be run
 	private Thread thread;
 	
 	//boolean that determines whether the main thread should run
     private volatile boolean running;
     
     //The ServerSocketWrapper on which the master is accepting connections
     private ServerSocketWrapper ssw;
     
     /** LoadManager (ServerSocketWrapper ssw)
      * 
      * Constructor for the LoadManager.
      * @param ssw - ServerSocketWrapper on which the master is accepting connections
      */
     public LoadManager(ServerSocketWrapper ssw) {
     	this.ssw = ssw;
     }
     
     /** start()
 	 * 
 	 * Starts the process of communicating with the slave ProcessManagers
 	 * every 5 seconds
 	 * @throws Exception - thrown when start is attempted and the process is
 	 * 					   already running
 	 */
 	public synchronized void start() throws Exception {
 		if (thread != null) {
 			throw new Exception("LoadManager already started.");
 		}
 		
 		thread = new Thread(new Runnable() {
 			/** run()
 			 * 
 			 * Communicates with the slave ProcessManagers every 5 seconds
 			 * Asks for and receives the number of processes running on each
 			 * slave
 			 * Uses this data to determine how to balance the load
 			 * Accepts processes from slaves and ships them to other slaves
 			 */
 			@Override
 			public void run() {
 				running = true;
 				while(running) {
 					//All connections the Master has made
 					ArrayList<SocketWrapper> connections = ssw.getScokets();
 					int size;
 					
 					if (connections != null && (size = connections.size()) > 0) {
 						int[] numProcesses = new int[size];
 						/* For each slave connected to the master, request the
 						 * number of processes running on that slave and look
 						 * for a response
 						 */
 						for (int i = 0; i < size; i++) {
 							SocketWrapper cur = connections.get(i);
 							if (!cur.isClosed()) {
 								try {
 									cur.getOut().writeObject("NumProcesses?");
 									Object response = null;
 									try {
 										response = cur.getIn().readObject();
 									} catch (ClassNotFoundException e) {
 										e.printStackTrace();
 									}
 									numProcesses[i] = (Integer) response;
								} catch (IOException e) {
									e.printStackTrace();
								}
 							}
 						}
 						
 						//Computes the average number of processes
 						int sum = 0;
 						for (int i = 0; i < numProcesses.length; i++) {
 							sum += numProcesses[i];
 						}
 						int avg = ((sum - 1) / numProcesses.length) + 1;
 						
 						/* All processes that are moving as a result of load 
 						 * balancing
 						 */
 						ArrayList<MigratableProcessWrapper> migrations = new ArrayList<MigratableProcessWrapper>();
 						
 						/* Request migrations from each ProcessManager who is
 						 * running more processes than the average
 						 */
 						for (int i = 0; i < connections.size(); i++) {
 							SocketWrapper cur = connections.get(i);
 							if (!cur.isClosed()) {
 								int over = numProcesses[i] - avg;
 								for(int j = 1; j <= over; j++) {
 									Object nm = null;
 									Object obj = null;
 									try {
 										cur.getOut().writeObject("migrate");
 										//Name of the process
 										nm = cur.getIn().readObject();
 										//The process object
 										obj = cur.getIn().readObject();
 									} catch (IOException e) {
										e.printStackTrace();
 									} catch (ClassNotFoundException e) {
 										e.printStackTrace();
 									}
 									
 									if (nm != null && obj != null) {
 										MigratableProcessWrapper mpw = new MigratableProcessWrapper((MigratableProcess) obj);
 										mpw.setName((String) nm);
 										migrations.add(mpw);
 									}
 								}
 							}
 						}
 						
 						/* For each process to be migrated, determine which
 						 * ProcessManager has the least amount of processes
 						 * and send the process to that ProcessManager
 						 */
 						for (int i = 0; i < migrations.size(); i++) {
 							int leastProcesses = -1;
 							int low_j = 0;
 							
 							for(int j = 0; j < numProcesses.length; j++) {
 								if (leastProcesses == -1 || numProcesses[j] < leastProcesses) {
 									leastProcesses = numProcesses[j];
 									low_j = j;
 								}
 							}
 							
 							SocketWrapper cur = connections.get(low_j);
 							
 							if (!cur.isClosed()) {
 								try {
 									cur.getOut().writeObject(migrations.get(i));
 								} catch (IOException e) {
									e.printStackTrace();
 								}
 							}
 						}
 					}
 					try {
 						Thread.sleep(5000);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		});
 		
 		thread.start();
 	}
 	
 	/** stop()
 	 * 
 	 * Stops the LoadManager from communicating with slave ProcessManagers
 	 */
 	public synchronized void stop() {
 		if (thread == null) {
 			return;
 		}
 		
 		running = false;
 		thread = null;
 	}
 }
