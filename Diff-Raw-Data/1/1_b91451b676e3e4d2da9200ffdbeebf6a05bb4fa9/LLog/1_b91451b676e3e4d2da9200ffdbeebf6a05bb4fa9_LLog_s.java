 package org.libraryofthings;
 
 import java.util.logging.Logger;
 
 public class LLog {
 	private Logger log;
 
 	LLog(Object o) {
 		log = Logger.getLogger("" + o);
 	}
 
 	public void info(String string) {
 		log.info(string);
 	}
 
 	public void error(Object o, String sourceMethod, Throwable e1) {
 		log.throwing(o.getClass().getName(), sourceMethod, e1);
 	}
 
 	public static LLog getLogger(Object o) {
 		return new LLog(o);
 	}
 
 }
