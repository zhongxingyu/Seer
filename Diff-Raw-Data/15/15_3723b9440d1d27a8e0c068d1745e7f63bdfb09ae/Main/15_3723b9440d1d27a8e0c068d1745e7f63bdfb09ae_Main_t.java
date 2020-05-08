 /*
  * Copyright 2010 Softgress - http://www.softgress.com/
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package sim.server;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.concurrent.Executors;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import sim.server.compund.CompoundMetricsThread;
 
 /**
  * This class is responsible to start up the SIM-Server application.
  * 
  * SIM-Server acts as a measurements collector for all agents. Measurements are
  * then transformed into semantic concepts and then uploaded into a semantic
  * database. Some of the measurements are also uploaded into a RRD database.
  * 
  * @author mcq
  * 
  */
 public class Main {
 	
 	private static final Logger logger = LoggerFactory.getLogger(Main.class);
 
 	public static int server_port= 8099;
 	public static String storage_properties_file = null;
 	public static String storage_server_domain;
 	public static int storage_server_port;
 	public static String storage_repository_id;
 	
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws FileNotFoundException, IOException {
 		Main main = new Main();
 		switch (args.length) {
 		case 1:
 			Main.storage_properties_file = args[0];
 			break;
 		case 2:
 			server_port = Integer.valueOf(args[1]);
 			Main.storage_properties_file = args[2];
 			break;
 		default:
 			main.printUsage();
 			return;
 		}
 		System.out.println("using parameters : server-port=" + server_port + ", properties=" + Main.storage_properties_file);
 
 		Properties storageProperties = new Properties();
 		storageProperties.load(new FileInputStream(storage_properties_file));
 		storage_server_domain = storageProperties.getProperty("storage-server-domain");
 		storage_server_port = Integer.valueOf(storageProperties.getProperty("storage-server-port"));
 		storage_repository_id = storageProperties.getProperty("storage-repository-id");
 		
 		ServerHttpThread serverHttpThread = new ServerHttpThread();
 		Thread thread = new Thread(serverHttpThread);
 		thread.run();
 		
 		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
 		
 		CompoundMetricsThread compoundMetricsThread = new CompoundMetricsThread();
 		try {
			scheduler.scheduleAtFixedRate(compoundMetricsThread, 0, 5, TimeUnit.MINUTES);
 		} catch (RejectedExecutionException e) {
 			logger.error("could not compound metrics process, cause is : " + e.getMessage(), e);
 		}
 
 
 	}
 
 	private void printUsage() {
 		System.out.println("Metrics Server Usage :");
 		System.out.println("");
 		System.out.println("\tjava sim.server.Main [server-port] storage-properties-file");
 		System.out.println("");
 		System.out.println("\tserver-port : the port of the http server receiving metrics (default 8099)");
 		System.out.println("\tproperties-file : path to file containing server parameters. The parameters are : storage-server-domain, storage-server-port and storage-repository-id");
 		System.out.println("");
 	}
 
 }
