 package server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.Socket;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 import server.database.Database;
 
 import model.Browser;
 import model.Job;
 import model.NamedPipeStream;
 import model.SocketString;
 import model.VirtualMachine;
 
 
 
 public class VMQueue {
 	
 	private ArrayList<Queue<Job>> jobs;
 	private ArrayList<VirtualMachine> vms;
 	
 	private Database db;
 	
 	private int jobNumber;
 	private static int PORT = 4445;
 
   public VMQueue() {
   	Database.initialize();
   	jobNumber = 0;
   	db = new Database("prod");
   	try {
 			db.startTransaction();
 			vms = (ArrayList<VirtualMachine>) db.getVirtualMachineDB().getAll();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
   	jobs = new ArrayList<Queue<Job>>();
   	for (int i = 0; i < vms.size(); i++) {
   		jobs.set(i, new LinkedList<Job>());
   	}
   }
 
   
   public void processQueue() throws SQLException {
   	for (int i = 0; i < jobs.size(); i++){
   		db.startTransaction();
   		VirtualMachine vm = db.getVirtualMachineDB().getVirtualMachine(jobs.get(i).peek().getQueue());
   		db.endTransaction(true);
   		if (vm.isAvailable() && vm.isInQueue()) {
   			Job job = jobs.get(i).remove();
   			sendSocketStream(job);
   		}
   	}
   }
   
   public boolean isAvailable(int id) throws SQLException {
   	// Query database where id
   	db.startTransaction();
   	boolean result = db.getVirtualMachineDB().getVMAvailable(id);
   	db.endTransaction(true);
   	return result;
   }
   
   public boolean isInQueue(int id) throws SQLException {
   	db.startTransaction();
   	boolean result = db.getVirtualMachineDB().getInQueue(id);
   	db.endTransaction(true);
   	return result;
   }
   
   /**
    * Adds a job to the appropriate queue
    * @param job
    * @return
    */
   public int addToQueue(Job job) {
   	//TODO: Need to update vm_queue_time as these add
   	int queue = job.getQueue();
   	jobs.get(queue).add(job);
   	return jobs.get(queue).size();
   }
 
   /**
    * This determines the most available queued machine
    * returns a job with an estimated time for the job to 
    * complete
    * @param stream
    * @return a job
    * @throws SQLException 
    */
   public Job buildJob(NamedPipeStream stream) throws SQLException {
  	// Determine Queue
   	// Build Socket String
   	// Build and return job
   
   	int queueNumber = determineQueue(stream.getBrowser(), stream.getBrowserVersion());
 
   	//TODO: Need to get a new time based on the browser time estimates?
   	SocketString socketString = buildSocketString(queueNumber, stream);
   	
   	socketString.setAntCommand(socketString.buildAntCommand(stream.getTestPackage(), stream.getTestClass()));
   	socketString.setOs(stream.getOs());
   	socketString.setBrowser(stream.getBrowser());
   	Job job = new Job(jobNumber++, socketString.toString(), stream.getTime(), queueNumber, vms.get(queueNumber).getIP());
   	return job;
   }
 
 	/**
 	 * @param queueNumber
 	 * @param stream
 	 * @return
 	 */
 	private SocketString buildSocketString(int queueNumber, NamedPipeStream stream) {
 		SocketString socketString = new SocketString();
 		
   	try {
   		socketString.setAntCommand(socketString.buildAntCommand(stream.getTestPackage(), stream.getTestClass()));
   		socketString.setOs(stream.getOs());
   		
   		db.startTransaction();
   		Browser browser = db.getBrowserDB().getBrowserVersionById(queueNumber, stream.getBrowser());
   		db.endTransaction(true);
   		
   		socketString.setBrowser(browser.getBrowserName());
   		socketString.setBrowserVersion(browser.getBrowserVersion());
 			socketString.setUrl(new URL(stream.getUrl()));
 			socketString.setLmpUser(stream.getLmpUsername());
 			socketString.setLmpPass(stream.getLmpPassword());
 			socketString.setSfUser(stream.getSfUsername());
 			socketString.setSfPass(stream.getSfPassword());
 			
 			socketString.setEmail(stream.getEmail());
 			socketString.setQueueNumber(queueNumber);
 			socketString.setTime(stream.getTime());
 			socketString.setTestPackage(stream.getTestPackage());
 			socketString.setTestClass(stream.getTestClass());
 			
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
   	
 
 		return socketString;
 	}
 
 	
 	private void sendSocketStream(Job job) {
 		Socket socket = null;
 		PrintWriter out = null;
 		try {
 			socket = new Socket(job.getHostIP(), PORT);
 			out = new PrintWriter(socket.getOutputStream(), true);
 			out.print(job.getMessage());
 			System.out.println(job.getMessage());
 		} catch (Exception e) {
 			
 		} finally {
 			try {socket.close();} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 
 	/**
 	 * @param testCase
 	 * @param subTest
 	 * @param browser
 	 * @param browserVersion
 	 * @return integer of vm_cloud id 
 	 * @throws SQLException 
 	 */
 	private int determineQueue(String browser, String browserVersion) throws SQLException {
 		assert !browser.equalsIgnoreCase("any");
 		assert browser != null && browserVersion != null;
 		List<VirtualMachine> limitedVms = null;
 		if (!browser.equalsIgnoreCase("any") && browserVersion.equalsIgnoreCase("any")) {
 			// BUILD LIST OF ONLY MACHINES THIS WILL RUN ON
 			db.startTransaction();
 			limitedVms = db.getVirtualMachineDB().getByBrowser(browser);
 			db.endTransaction(true);
 		} else {
 			db.startTransaction();
 			limitedVms = db.getVirtualMachineDB().getByBrowserAndVersion(browser, browserVersion);
 			db.endTransaction(true);
 		}
 		// This filters the lowest of the queue to be the one the job gets submited too
 		double shortestTime = limitedVms.get(0).getCurrentQueueTime();
 		int vmId = 1;
 		for (int i = 1; i < limitedVms.size(); i++) {
 			if (limitedVms.get(i).isAvailable() == true && limitedVms.get(i).isInQueue() == true) {
 				if (limitedVms.get(i).getCurrentQueueTime() < shortestTime) {
 					shortestTime = limitedVms.get(i).getCurrentQueueTime();
 					vmId = limitedVms.get(i).getId();
 				}
 			}
 		}
 		assert vmId > 0;
 		return vmId;
 	}
 }
