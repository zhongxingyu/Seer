 package org.paxle.filter.robots.impl;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InvalidClassException;
 import java.io.ObjectInputStream;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.osgi.framework.Constants;
 import org.osgi.service.cm.ManagedService;
 import org.paxle.filter.robots.impl.rules.RobotsTxt;
 
 public class RobotsTxtCleanupThread extends Thread implements ManagedService
 { 
 	/** the directory where the robots.txt data is stored */
 	File dir = null;
 	/** The time in minutes between each cleaning */
 	public static final String PROP_DELAY = "delay";

 	/** The configuration data for this class */
 	private Dictionary<String, Object> config = null;
 
 	Log logger = LogFactory.getLog(this.getClass());
 
 	/**
 	 * Generates a thread which wipes out robots.txt entries from the cache, which are older than their ExpirationDate
 	 * @param dir the directory where the robots.txt objects are stored
 	 */
 	RobotsTxtCleanupThread(File dir) 
 	{
 		this.dir = dir;
 		this.setName("RobotsTxtCleanupThread");
 		this.setPriority(Thread.MIN_PRIORITY);
 	}
 
 	public void run() { 
 		/** If set to false the thread terminates */
 		boolean go_on = true;
 
 		while (go_on) {
 			logger.info("Cleaning cache...");
 
 			for ( File file : dir.listFiles() )  {
 				if (!file.isFile()) {
 					logger.warn("The cache should only contain files. Please review non-file element " + file.toURI() + " manually.");
 				} else {
 					ObjectInputStream ois = null;
 					RobotsTxt robotsTxt = null;
 					try {
 						ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
 						robotsTxt = (RobotsTxt) ois.readObject();
 						ois.close();
 						ois = null;
 					} catch (InvalidClassException e) {
 						//Old type of class definition. Just delete it to avoid cruft.
 						file.delete();
 					} catch (ClassNotFoundException e) {
 						// class was refactored. just delete it
 						file.delete();
 					} catch (Exception e) {
 						logger.error("Error while reading file " + file.getName() + " - Removing it.", e);
 						file.delete();
 					} finally {
 						if (ois != null) try { ois.close(); } catch (Exception e) {/* ignore this */}
 					}					

 					if (robotsTxt != null && (robotsTxt.getExpirationDate().getTime() < System.currentTimeMillis())) {
 						logger.debug("Deleting cached robots.txt file for " + robotsTxt.getHostPort());
 						file.delete();
 					}
 				}
 				if (isInterrupted()) {
 					go_on=false;
 					break;
 				}
 				//Wait a moment to not overload the I/O
 				try {
 					TimeUnit.MILLISECONDS.sleep(500); 
 				} catch ( InterruptedException e ) {
 					go_on=false;
 					break;
 				}
 			}
 			logger.debug("Finished cleaning cache.");
 
 			if (isInterrupted()) {
 				go_on=false;
 			}
 			if (go_on) {
				if (config.get(PROP_DELAY) == null) {
					logger.warn("Delay for robots.txt cleaning is null. Loading defaults.");
					updated(getDefaults());
				}
 				try {
 					TimeUnit.SECONDS.sleep(((Integer)config.get(PROP_DELAY)).intValue()*60); 
 				} catch ( InterruptedException e ) {
 					go_on=false;
 				} 
 			}
 		}
 		logger.info("Thread terminated.");
 	}

 	/**
 	 * @see ManagedService#updated(Dictionary)
 	 */
 	@SuppressWarnings("unchecked")		// we're only implementing an interface
 	public synchronized void updated(Dictionary configuration) {
 		logger.debug("Updating configuration");
 		try {
 			if ( configuration == null ) {
 				logger.warn("Updated configuration is null. Using defaults.");
 				configuration = this.getDefaults();
 			}			
 			this.config = configuration;
 		} catch (Throwable e) {
 			logger.error("Internal exception during configuring", e);
 		}
 	}

 	/**
 	 * @return the default configuration of this service
 	 */
 	public Hashtable<String,Object> getDefaults() {
 		Hashtable<String,Object> defaults = new Hashtable<String,Object>();

 		defaults.put(PROP_DELAY, Integer.valueOf(30));
 		defaults.put(Constants.SERVICE_PID, RobotsTxtCleanupThread.class.getName());

 		return defaults;
 	}

 }
