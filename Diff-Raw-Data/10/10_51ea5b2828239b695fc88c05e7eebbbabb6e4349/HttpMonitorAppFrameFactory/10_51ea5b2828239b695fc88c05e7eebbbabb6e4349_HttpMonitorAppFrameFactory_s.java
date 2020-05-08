 package com.yogocodes.httpmonitor.gui.frames;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class HttpMonitorAppFrameFactory {
 	
 	private final static Logger LOG = LoggerFactory.getLogger(HttpMonitorAppFrameFactory.class); 
 	private static HttpMonitorAppFrame appFrameInstance;
 
 	/**
 	 * @return the appFrameInstance
 	 */
 	public static HttpMonitorAppFrame getAppFrameInstance() {
 		
 		if( appFrameInstance == null ) {
 			synchronized (HttpMonitorAppFrameFactory.class) {
 				if( appFrameInstance == null ) {
 					appFrameInstance = new HttpMonitorAppFrame();
 				}
 			}
 		}
 		
 		return appFrameInstance;
 	}
 
 
 
 }
