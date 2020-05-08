 package org.dynasoar.service;
 
 import org.apache.log4j.Logger;
 import org.dynasoar.sync.ChangeEvent;
 import org.dynasoar.sync.DirectoryWatcher;
 import org.dynasoar.config.Configuration;
 import java.util.*;
 
 /**
  * ServiceMonitor is responsible for monitoring changes in Service config files.
  * It is supposed to act on and notify NodeCommunicator of any change in
  * Service.
  * 
  * @author Rakshit Menpara
  */
 public class ServiceMonitor implements Runnable {
 
 	private static ServiceMonitor current = null;
 	private static Logger logger = Logger.getLogger(ServiceMonitor.class);
 	private static Thread th = null;
 	private static HashMap<String, DynasoarService> serviceMap = new HashMap<String, DynasoarService>();
 
 	public static void start() {
 		// TODO: Start this in a separate thread
 		current = new ServiceMonitor();
 		th = new Thread(current, "ServiceMonitor");
 		th.start();
 	}
 
 	public static boolean isRunning() {
 		if (current == null) {
 			return false;
 		}
 
 		return th.isAlive();
 	}
 
 	@Override
 	public void run() {
 
 		// Read "ServiceConfigDir" from configuration and starts listening
 		// to the directory
 		String serviceConfigDirPath = Configuration
 				.getConfig("serviceConfigDir");
 		DirectoryWatcher dir = new DirectoryWatcher(
 				new ServiceConfigChangeEvent());
 		dir.watch(serviceConfigDirPath);
 
 		// In case of any changes in directory, Read service config file,
 		// load/re-deploy the service on local server
 
 		// Notify NodeCommunicator of all the changes occurred
 
 	}
 
 	/**
 	 * Implements ChangeEvent interface, which will handle directory change
 	 * events of Service Config Directory
 	 * 
 	 * @author Rakshit Menpara
 	 */
 	public static class ServiceConfigChangeEvent implements ChangeEvent {
 
 		@Override
 		public void fileCreated(String path) {
 			DynasoarService service = this.readServiceConfig(path);
 			serviceMap.put(service.getShortName(), service);
 		}
 
 		@Override
 		public void fileModified(String path) {
 			DynasoarService service = this.readServiceConfig(path);
 			serviceMap.put(service.getShortName(), service);
 		}
 
 		@Override
 		public void fileRemoved(String path) {
 			// TODO: Correct
 			DynasoarService service = this.readServiceConfig(path);
			serviceMap.remove(service.getShortName());
 		}
 
 		private DynasoarService readServiceConfig(String path) {
 			DynasoarService service = new DynasoarService();
 
 			// TODO: Read and parse the config file using JSON parser (jackson)
 
 			return service;
 		}
 	}
 }
