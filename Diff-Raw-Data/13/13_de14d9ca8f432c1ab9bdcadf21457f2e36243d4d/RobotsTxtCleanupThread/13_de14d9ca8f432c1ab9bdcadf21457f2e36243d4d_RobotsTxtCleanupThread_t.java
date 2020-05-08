 package org.paxle.filter.robots.impl;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InvalidClassException;
 import java.io.ObjectInputStream;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class RobotsTxtCleanupThread extends Thread
 { 
 	/** the directory where the robots.txt data is stored */
 	File dir = null;
 	/** The time in minutes between each cleaning */
 	int delaytime = 10;
 
 	Log logger = LogFactory.getLog(this.getClass());
 
 	/**
 	 * Generates a thread which wipes out robots.txt entries from the cache, which are older than their ExpirationDate
 	 * @param dir the directory where the robots.txt objects are stored
 	 * @param delay the delay between the cleaning processes in minutes
 	 */
 	RobotsTxtCleanupThread(File dir, int delay) 
 	{ 
 		this.delaytime = delay;
 		this.dir = dir;
 		this.setName("RobotsTxtCleanupThread");
 		this.setPriority(Thread.MIN_PRIORITY);
 		this.start();
 	}
 
 	public void run() 
 	{ 
 		/** If set to false the thread terminates */
 		boolean go_on = true;
 
 		while (go_on) {
 			logger.info("Cleaning cache...");
 
 			for ( File file : dir.listFiles() ) 
 			{
 				if (!file.isFile()) {
 					logger.warn("The cache should only contain files. Please review non-file element " + file.toURI() + " manually.");
 				} else {
 					ObjectInputStream ois = null;
 					RobotsTxt robotsTxt = null;
 					try {
 						ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
 						robotsTxt = (RobotsTxt) ois.readObject();
 						ois.close();
 					} catch (InvalidClassException e) {
 						//Old type of class definition. Just delete it to avoid cruft.
 						file.delete();
 					} catch (Exception e) {
 						logger.error("Error while reading file " + file.getName(), e);
 					}
 					if (robotsTxt != null && (robotsTxt.getExpirationDate().getTime() < System.currentTimeMillis())) {
 						file.delete();
 						logger.debug("Deleted caching file for " + robotsTxt.getHostPort());
 					}
 				}
 				if (isInterrupted()) {
 					go_on=false;
 					break;
 				}
 				//Wait am moment to not overload the I/O
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
 				try {
					TimeUnit.SECONDS.sleep(delaytime*60); 
 				} catch ( InterruptedException e ) {
 					go_on=false;
 				} 
 			}
 		}
 		logger.info("Thread terminated.");
 	}
 }
