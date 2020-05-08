 package server;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.OutputStreamWriter;
 import java.net.MalformedURLException;
 import java.net.Socket;
 import java.net.URL;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import server.database.Database;
 
 import model.Job;
 import model.NamedPipeStream;
 import model.OperatingSystem;
 import model.SocketString;
 import model.VirtualMachine;
 
 
 
 public class VMQueue {
 	
 	private ArrayList<VirtualMachine> vms;
 	private List<Job> jobs;
 	
 	private Database db;
 	
 	private int jobNumber;
 	private static int PORT = 4445;
 	
 	private static Logger logger;
 	
 	static {
 		logger = Logger.getLogger(VMQueue.class.getName());
 	}
 
   public VMQueue(int port, String database) {
   	PORT = port;
   	Database.initialize();
   	jobNumber = 0;
   	db = new Database(database);
 
 		db.startTransaction();
 		vms = (ArrayList<VirtualMachine>) db.getVirtualMachineDB().getAll();
 		db.getVirtualMachineDB().updateAvailable();
 		db.endTransaction(true);
 		
   	jobs = new LinkedList<Job>();
   }
 
   
   public void processQueue() throws SQLException {
 
 		db.startTransaction();
 		vms = (ArrayList<VirtualMachine>) db.getVirtualMachineDB().getAll();
 		// Don't want to update to available until the test comes back - if we add
 		// new vm's then we need to also set the available and inqueue fields
 		//db.getVirtualMachineDB().updateAvailable();
 		db.endTransaction(true);
 		
 		
 		//TODO: May want to update queue information to just see how long job expected to be is.
   	for (int i = 0; i < vms.size(); i++) {
   		if (vms.get(i).isAvailable()) {
   			for (int j = 0; j < jobs.size(); j++) {
   				//Check if available vm has the right browser and version
   				//Any Browser would have been randomly determined earlier
   				//Have browser, don't care version
   				if (!jobs.get(j).getBrowser().equalsIgnoreCase("any") && jobs.get(j).getBrowserVersion().equalsIgnoreCase("any")) {
   					if (vms.get(i).getBrowsers().containsKey(jobs.get(j).getBrowser())) {
   						Job job = jobs.remove(j);
   						job.setQueue(vms.get(i).getId());
   						job.getMessage().setQueueNumber(vms.get(i).getId());
   						job.getMessage().setBrowserVersion(vms.get(i).getBrowsers().get(job.getBrowser()));
   						//System.out.println(vms.get(i).getBrowsers().toString());
   						job.setHostIP(vms.get(i).getIP());
   						sendSocketStream(job);
   						break;
   					} 
   				// Have browser and version 
   				} else if (!jobs.get(j).getBrowser().equalsIgnoreCase("any") && !jobs.get(j).getBrowserVersion().equalsIgnoreCase("any")){
   					if (vms.get(i).getBrowsers().containsKey(jobs.get(j).getBrowser()) && vms.get(i).getBrowsers().containsValue(jobs.get(j).getBrowserVersion())) {
   						Job job = jobs.remove(j);
   						job.setQueue(vms.get(i).getId());
   						job.getMessage().setQueueNumber(vms.get(i).getId());
   						job.setHostIP(vms.get(i).getIP());
   						
   						sendSocketStream(job);
   						break;
   					} 
   				}
   			}
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
   	db.startTransaction();
   	int jobId = db.getJobDB().insertJob(job);
   	db.endTransaction(true);
   	job.setId(jobId);  	
   	jobs.add(job);
   	return jobs.size();
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
   	assert stream != null;
     // Determine Queue
   	// Build Socket String
   	// Build and return job
   
   	//int queueNumber = determineQueue(stream.getBrowser(), stream.getBrowserVersion());
   	int queueNumber = -1;
   	String ip = "null";
 //  	db.startTransaction();
 //  	VirtualMachine vm = db.getVirtualMachineDB().getVirtualMachine(queueNumber);
 //  	db.endTransaction(true);
   	//TODO: Need to get a new time based on the browser time estimates?
   	SocketString socketString = buildSocketString(queueNumber, stream);
   	
   	socketString.setAntCommand(socketString.buildAntCommand(stream.getTestPackage(), stream.getTestClass()));
   	//TODO: need to update to reflect batch changes
   	Timestamp now = new Timestamp(new java.util.Date().getTime());
   	Job job = new Job(jobNumber++, stream.getBatchId(), socketString, stream.getTime(), queueNumber, ip,
   			false, stream.getBrowser(), stream.getBrowserVersion(), now, now);
   	return job;
   }
 
 	/**
 	 * Need to set OS, IP, QUEUE NUMBER later
 	 * @param queueNumber
 	 * @param stream
 	 * @return
 	 */
 	private SocketString buildSocketString(int queueNumber, NamedPipeStream stream) {
 		SocketString socketString = new SocketString();
 		//System.out.println("Queue: " + queueNumber + stream.toString());
   	try {  		
   		//db.startTransaction();
   		//Browser browser = db.getBrowserDB().getBrowserVersionById(queueNumber, stream.getBrowser());
   		//VirtualMachine vm = db.getVirtualMachineDB().getVirtualMachine(queueNumber);
   		//OperatingSystem os = db.getOSDB().getOSById(vm.getOsId());
   		//db.endTransaction(true);
   		
   		//socketString.setOs("os");
   		//System.out.println(os.toString());
   		socketString.setBrowser(stream.getBrowser());
   		socketString.setBrowserVersion(stream.getBrowserVersion());
 			socketString.setUrl(new URL(stream.getUrl()));
 			socketString.setLmpUser(stream.getLmpUsername());
 			socketString.setLmpPass(stream.getLmpPassword());
 			socketString.setSfUser(stream.getSfUsername());
 			socketString.setSfPass(stream.getSfPassword());
   		socketString.setAntCommand(socketString.buildAntCommand(stream.getTestPackage(), stream.getTestClass()));
 			socketString.setEmail(stream.getEmail());
 			socketString.setQueueNumber(queueNumber);
 			socketString.setTime(stream.getTime());
 			socketString.setTestPackage(stream.getTestPackage());
 			socketString.setTestClass(stream.getTestClass());
 			socketString.setJobId(stream.getBatchId());
 			
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return socketString;
 	}
 
 	
 	private void sendSocketStream(Job job) {
 		db.startTransaction();
		OperatingSystem os = db.getOSDB().getOSById(vms.get(job.getQueue()).getOsId());
 		db.endTransaction(true);
 		
 		//TODO: Need to add Job ID to socket message and possibily update db?
 		SocketString tmp = job.getMessage();
 		tmp.setJobId(job.getId());
 		tmp.setOs(os.getName());
 		job.setMessage(tmp);
 		logger.info("HOST: " + job.getHostIP()+ "  MESSAGE: " +job.getMessage());
 		
 		db.startTransaction();
 		db.getJobDB().updateJob(job);
 		db.endTransaction(true);
 		
 		//TODO: update vm_cloud available to 0;
 		Socket socket = null;
 		//BufferedWriter bufOut = null;
 		try {
 			db.startTransaction();
 			db.getVirtualMachineDB().setUnavailable(job.getQueue());
 			db.endTransaction(true);
 			
 			socket = new Socket(job.getHostIP(), PORT);
 			//bufOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
 			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
 //			bufOut.write(job.getMessage());
 //			bufOut.flush();
 			out.writeUnshared(job.getMessage());
 			out.flush();
 			//out.close();
 		} catch (Exception e) {
 			
 		} finally {
 			try {socket.close();} catch (IOException e) {
 				e.printStackTrace();
 				logger.log(Level.SEVERE, e.getMessage(), e);
 			}
 		}
 	}
 	
 }
