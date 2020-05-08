 package nl.tudelft.cloud_computing_project;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class CloudOCR {
 	
 	private static Logger LOG = LoggerFactory.getLogger(CloudOCR.class);
 	
 	public static final Properties Configuration = loadProperties();
 	
 	private static final long scheduler_interval 	= Long.parseLong((String)Configuration.get("scheduler_interval"));
 	private static final long monitor_interval 		= Long.parseLong((String)Configuration.get("monitor_interval"));
 	private static final long failure_interval 		= Long.parseLong((String)Configuration.get("failure_interval"));
 	private static final long allocation_interval 	= Long.parseLong((String)Configuration.get("allocation_interval"));
 	
 	private static Thread SchedulerThread;
 	private static Thread MonitorThread;
 	private static Thread AllocationManagerThread;
 	private static Thread FaultManagerThread;
 	
 	
 	public static void main(String[] args) {
 		LOG.info("Entering Cloud OCR!");
 		
 		// Thread that runs the Scheduler
 		SchedulerThread = new Thread() {
 			public void run(){
 				Scheduler s = new Scheduler();
 				while(true){
 					try {
 						LOG.info("Scheduler Thread started.");
 						s.schedule();
 						Thread.sleep(scheduler_interval);
 					} catch (InterruptedException e) {
 						LOG.warn("SchedulerThread sleep was interrupted", e);
 					}
 				}
 			}
 		};
 		// Start the scheduler
 		SchedulerThread.run();
 		
 		// Thread that runs the Monitor
 		MonitorThread = new Thread() {
 			public void run(){
 				Monitor m = Monitor.getInstance();
 				while(true){
 					try {
 						LOG.info("Monitor Thread started.");
 						m.monitorSystem();
 						Thread.sleep(monitor_interval);
 					} catch (InterruptedException e) {
 						LOG.warn("MonitorThread sleep was interrupted", e);
 					}
 				}
 			}
 		};
 		// Start the Monitor
 		MonitorThread.run();
 		
 		// Thread that runs the FaultManager
 		FaultManagerThread = new Thread() {
 			public void run(){
 				FaultManager fm = FaultManager.getInstance();
 				while(true){
 					try {
 						LOG.info("FaultManager Thread started.");
 						fm.manageFailingJobs();
 						Thread.sleep(failure_interval);
 					} catch (InterruptedException e) {
 						LOG.warn("FaultManagerThread sleep was interrupted", e);
 					}
 				}
 			}
 		};
 		// Start the Fault Manager
 		FaultManagerThread.run();
 		
 		// Thread that runs the AllocationManager
 		AllocationManagerThread = new Thread() {
 			public void run(){
 				AllocationManager am = AllocationManager.getInstance();
 				while(true){
 					try {
 						LOG.info("AllocationManager Thread started.");
 						am.applyProvvisioningPolicy();
 						Thread.sleep(allocation_interval);
 					} catch (InterruptedException e) {
 						LOG.warn("AllocationManagerThread sleep was interrupted", e);
 					}
 				}
 			}
 		};
 		// Start the Allocation Manager
 		AllocationManagerThread.run();
 	}
 
 	private static Properties loadProperties() {
 		Properties properties = new Properties();
 
 		try {
			properties.load(new FileInputStream(System.getProperty("user.dir") + "/CloudOCR.properties"));
 		
 			for(String key : properties.stringPropertyNames()) {
 				String value = properties.getProperty(key);
 				LOG.info(key + " => " + value);
 			}
 			
 		} catch (IOException e) {
 			LOG.error("Error loading Properties\n" + e.getMessage());
 		}
 		
 		return properties;
 		
 	}
 
 }
