 package MapReduceObjects;
 
 import Config.Configuration;
 import Interfaces.InputSplit440;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 
 public class WorkerListener {
 
 	//Thread in which the process will be run
 	private Thread thread;
 		
 	//boolean that determines whether the main thread should run
 	private volatile boolean running;
 	
 	MapWorker worker;
 	
 	private Socket sock;
 	   
 	public WorkerListener(Socket sock, MapWorker worker) {
 		this.sock = sock;
 		this.worker = worker;
 	}
 	
     public synchronized void start() throws Exception {
 		if (thread != null) {
 			throw new Exception("Listener already started.");
 		}
 		
 		thread = new Thread(new Runnable() {
 			/** run()
 			 * 
 			 * Listens for requests from the master.
 			 * Interprets the requests and responds to them
 			 */
 			@Override
 			public void run() {
 				running = true;
 				while(running) {
 					if (!sock.isClosed()) {
 						Object request = null;
 						ObjectOutputStream oos = null;
 						ObjectInputStream ois = null;
 						try {
 							oos = new ObjectOutputStream(sock.getOutputStream());
 							ois = new ObjectInputStream(sock.getInputStream());
 							request = (String) ois.readObject();
 						} catch (IOException e) {
 							e.printStackTrace();
 						} catch (ClassNotFoundException e) {
 							e.printStackTrace();
 						}
 						
 						if (request.equals("Still alive?")) {
 							try {
 								oos.writeObject("Yes");
 							} catch (IOException e) {
 								e.printStackTrace();
 							}
 						} else if (request.equals("Start job")) {
 							try {
 								Configuration config;
 								InputSplit440 split;
 								try {
 									config = (Configuration) ois.readObject();
 									split = (InputSplit440) ois.readObject();
 									if (!worker.currentlyWorking())
 										try {
 											worker.startJob(config, split);
 										} catch (IllegalAccessException e) {
 											e.printStackTrace();
										} catch (Exception e) {
											e.printStackTrace();
 										}
 									else oos.writeObject("Worker busy.");
 								} catch (ClassNotFoundException e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 								}
 							} catch (IOException e) {
 								e.printStackTrace();
 							}
 						} 
 					}
 				}
 			}
 		});
 		
 		thread.start();
 	}
     
     /** stop()
      * 
      * Stops the process of listening for requests
      */
     public synchronized void stop() {
 		if (thread == null) {
 			return;
 		}
 		
 		running = false;
 		thread = null;
 	}
 }
