 package de.tum.in.sonar.collector;
 
 import java.beans.PropertyVetoException;
 import java.io.IOException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.tum.in.sonar.collector.log.LogDatabase;
 import de.tum.in.sonar.collector.notification.NotificationManager;
 import de.tum.in.sonar.collector.server.CollectServiceImpl;
 import de.tum.in.sonar.collector.server.NotificationServiceImpl;
 import de.tum.in.sonar.collector.server.ServerBootstrap;
 import de.tum.in.sonar.collector.server.SqlManagementServiceImpl;
 import de.tum.in.sonar.collector.tsdb.TableCreationException;
 import de.tum.in.sonar.collector.tsdb.TimeSeriesDatabase;
 
 public class Collector {
 
 	private static Logger logger = LoggerFactory.getLogger(Collector.class);
 
 	public Collector() throws IOException {
 
 		HBaseUtil hbase = new HBaseUtil();
 
 		TimeSeriesDatabase tsdb = new TimeSeriesDatabase();
 		tsdb.setHbaseUtil(hbase);
 
 		try {
 			tsdb.setupTables();
 		} catch (TableCreationException e) {
 			logger.error("Error while creating HBase table structure", e);
 			System.exit(1);
 		}
 
 		// initilize the log database
 		LogDatabase logdb = new LogDatabase();
 		logdb.setHbaseUtil(hbase);
 		try {
 			logdb.setupTables();
 		} catch (TableCreationException e) {
 			logger.error("Error while creating HBase table structure", e);
 			System.exit(1);
 		}
 
 		NotificationManager notifyManager = new NotificationManager();
 		notifyManager.start();
 
 		CollectServiceImpl collectService = new CollectServiceImpl();
 		collectService.setTsdb(tsdb);
 		collectService.setLogdb(logdb);
 		collectService.setNotificationManager(notifyManager);
 
 		NotificationServiceImpl notificationService = new NotificationServiceImpl();
 		notificationService.setNotificationManager(notifyManager);
 
 		SqlManagementServiceImpl managementService = null;
 		try {
 			managementService = new SqlManagementServiceImpl();
 			managementService.setTsdb(tsdb);
 			managementService.setLogdb(logdb);
 		} catch (PropertyVetoException e) {
			logger.error("Failed initializing C3PO pool", e); 
			System.exit(1); 
 		}
		
 
 		ServerBootstrap dataSinkServer = new ServerBootstrap();
 		dataSinkServer.setCollectServiceImpl(collectService);
 		dataSinkServer.setManagementServiceImpl(managementService);
 		dataSinkServer.setNotificationServiceImpl(notificationService);
 
 		logger.info("Starting severs...");
 		Thread[] threads = dataSinkServer.start();
 		logger.info("Sever started");
 
 		dataSinkServer.wait(threads);
 	}
 
 	public static void main(String[] args) {
 
 		try {
 			new Collector();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
