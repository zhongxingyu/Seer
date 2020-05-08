 package com.pjf.mat.sys;
 
 import java.io.FileInputStream;
 import java.util.Collection;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 
 import com.pjf.marketsim.EventFeed;
 import com.pjf.mat.api.Cmd;
 import com.pjf.mat.api.Element;
 import com.pjf.mat.api.EventLog;
 import com.pjf.mat.api.LkuAuditLog;
 import com.pjf.mat.api.MatApi;
 import com.pjf.mat.api.MatLogger;
 import com.pjf.mat.api.NotificationCallback;
 import com.pjf.mat.api.RtrAuditLog;
 import com.pjf.mat.api.TimeOrdered;
 import com.pjf.mat.impl.MatInterface;
 import com.pjf.mat.impl.MatInterfaceModel;
 import com.pjf.mat.sim.MatSim;
 import com.pjf.mat.util.comms.BaseComms;
 
 
 public abstract class MatSystem {
 	protected final static Logger logger = Logger.getLogger(MatSystem.class);
 	private MatInterface mat = null;
 	private MatSim sim = null;
 	private EventFeed feed;
 	private final UnifiedEventLogger ueLogger;
 	private final NotificationHandler notificationHandler;
 
 	class NotificationHandler implements NotificationCallback {
 
 		@Override
 		public void notifyEventLog(EventLog evt) {
 			ueLogger.addLog(evt);
 			logger.debug("Event: ts=" + evt.getTimestamp() + " src=" + evt.getSrc().getId() +
 						" type=" + evt.getSrc().getType() +
 						" instrument=" + evt.getIntrumentId() +
 						" value=" + evt.getDispValue());			
 		}
 
 		@Override
 		public void notifyElementStatusUpdate(Element element) {
 			logger.info("Status Update: element=" + element.getId() +
 						" type=" + element.getType() +
 						" state=" + element.getElementStatus());			
 		}
 
 		@Override
 		public void notifyLkuAuditLogReceipt(Collection<LkuAuditLog> logs) {
 			for (LkuAuditLog log : logs) {
 				logger.debug("Received LKU Audit Log: [" + log + "]");
 				ueLogger.addLog(log);
 			}
 		}
 
 		@Override
 		public void notifyRtrAuditLogReceipt(Collection<RtrAuditLog> logs) {
 			for (RtrAuditLog log : logs) {
 				logger.debug("Received Router Audit Log: [" + log + "]");
 				ueLogger.addLog(log);
 			}
 		}
 
 		@Override
 		public void notifyUnifiedEventLog(TimeOrdered log) {
 			logger.info("Unified Event Log: " + log);			
 		}
 		
 	}
 
 	public MatSystem(){
 		notificationHandler = new NotificationHandler();
 		ueLogger = new UnifiedEventLogger(notificationHandler);
 	}
 	
 	
 	/**
 	 * Template method called during boot process
 	 * It can perform any initialisation and then call init() to init the cxn to HW
 	 */
 	protected abstract void start() throws Exception;
 
 	/**
 	 * Template method to send data
 	 * 
 	 * @param mat
 	 * @throws Exception
 	 */
 	protected abstract void configure(MatApi mat) throws Exception;
 
 	/** 
 	 * Template method to send data to the HW
 	 * 
 	 * @param mat
 	 * @param feed
 	 * @throws Exception
 	 */
 	protected void sendTradeBurst(MatApi mat, EventFeed feed) throws Exception {
 		if (feed != null) {
 			feed.sendTradeBurst("resources/GLP_27667_1.csv",20,10,1);
 		}
 	}
 
 
 	/**
 	 * Method to call to initialise the system.
 	 * 
 	 * @param propsResource	resource file for HW properties
 	 * @throws Exception
 	 */
 	protected void init(String propsResource) throws Exception {
 		Properties props = loadProperties(propsResource);
 		BaseComms comms;
 		if (System.getProperty("sim") != null) {
 			sim = new MatSim(new MatLogger() {
 				@Override
 				public void info(String message) {
 					logger.info(message);
 				}
 				@Override
 				public void error(Exception ex) {
 					logger.error(ex);
 				}
 				@Override
 				public void debug(String message) {
 					logger.debug(message);
 				}
 			});
 			comms = sim;
 		} else if (System.getProperty("dummy") != null) {
 			comms = new DummyComms();
 		} else {
 			comms = new UDPComms("192.168.0.9",2000);
 		}
 		comms.addNotificationSubscriber(notificationHandler);
 		MatInterfaceModel model = new MatInterfaceModel(props);
 		mat = new MatInterface(comms,model);
 		comms.setMat(mat);
 		if (sim != null) {
 			// transfer config data into simulator
 			sim.init(mat.getModel().getElements());
 		}
 		mat.checkHWSignature();
 // FIXME
 //		if (feed == null) {
 //			feed = new EventFeed(comms.getCxn(),15000);
 //		}
 	}
 
 	protected void run() throws Exception {
 		logger.info("-----");	reqStatus(); Thread.sleep(500);
 		configure(mat);
 		mat.syncClock(0);
 		logger.info("-----");	reqStatus(); Thread.sleep(500);
 		sendTradeBurst(mat,feed);
 		logger.info("-----");	reqAuditLogs(); reqStatus(); Thread.sleep(500);
 		Thread.sleep(1000);
 		logger.info("----------------------------------------");
 		ueLogger.flush();
 		logger.info("----------------------------------------");
 	}
 
 	protected void boot() {
 		try {
 			start();
 			if (sim != null) {
 				sim.start();
 			}
 			run();
 			shutdown();
 		} catch (Exception e) {
 			logger.error("Outer error catcher: " + e.getMessage());
 			e.printStackTrace();
			shutdown();
 		}		
 	}
 
 
 	public void shutdown() {
 		logger.info("Shutting down ...");
 		mat.shutdown();
 		ueLogger.shutdown();
 	}
 
 
 	protected void sendCmd(int elId, String cmdName) {
 		Element el = mat.getModel().getElement(elId);
 		if (el != null) {
 			Cmd cmd = el.getCmds().get(0);
 			mat.sendCmd(cmd);
 		}
 	}
 	
 	private void reqStatus() {
 		mat.getHWStatus();		
 	}
 	
 	private void reqAuditLogs() throws Exception {
 		mat.reqLkuAuditLogs();		
 	}
 
 	protected static Properties loadProperties(String resource) throws Exception {
 		Properties props = new Properties();
 		try {
 			props.load(new FileInputStream(resource));
 		} catch (Exception e) {
 			throw new Exception("Cant load properties file",e);
 		}
 		return props;
 	}
 
 }
