 package net.frontlinesms.messaging;
 
 import java.io.*;
 import java.util.*;
 
 import serial.*;
 
 public class AllModemsDetector {	
 	
 //> INSTANCE PROPERTIES
 	private Logger log = new Logger(getClass());
 	private ATDeviceDetector[] detectors;
 
 //> DETECTION METHODS	
 	/** Trigger detection, and return the results when it is completed. */
 	public ATDeviceDetector[] detectBlocking() {
 		detect();
 		waitUntilDetectionComplete(detectors);
 		return getDetectors();
 	}
 	
 	/** Trigger detection. */
 	public void detect() {
 		log.trace("Starting device detection...");
 		Set<ATDeviceDetector> detectors = new HashSet<ATDeviceDetector>();
 		Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();
 		while(ports.hasMoreElements()) {
 			CommPortIdentifier port = ports.nextElement();
 			if(port.getPortType() == CommPortIdentifier.PORT_SERIAL) {
 				ATDeviceDetector d = new ATDeviceDetector(port);
 				detectors.add(d);
 				d.start();
 			} else {
 				log.info("Ignoring non-serial port: " + port.getName());
 			}
 		}
 		this.detectors = detectors.toArray(new ATDeviceDetector[0]);
 		log.trace("All detectors started.");
 	}
 	
 	public void reset() {
 		if(detectors!=null) for(ATDeviceDetector d : detectors) {
 			d.interrupt();
 		}
 	}
 
 //> ACCESSORS
 	/** Get the detectors. */
 	public ATDeviceDetector[] getDetectors() {
 		return detectors;
 	}
 	
 //> STATIC HELPER METHODS	
 	/** Blocks until all detectors have completed execution. */
 	private static void waitUntilDetectionComplete(ATDeviceDetector[] detectors) {
 		boolean completed = true;
 		do {
 			for (ATDeviceDetector portDetector : detectors) {
 				if(!portDetector.isFinished()) {
 					completed = false;
 				}
 			}
 			Utils.sleep(500);
 		} while(!completed);
 	}
 }
