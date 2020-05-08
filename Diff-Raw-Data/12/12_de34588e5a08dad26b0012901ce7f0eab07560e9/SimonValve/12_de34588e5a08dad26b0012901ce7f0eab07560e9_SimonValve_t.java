 package org.javasimon.tomcat;
 
 import java.io.IOException;
 import org.apache.catalina.connector.Request;
 import org.apache.catalina.connector.Response;
 import org.apache.catalina.valves.ValveBase;
 import org.javasimon.SimonManager;
 import org.javasimon.Split;
 import org.javasimon.Stopwatch;
 
 /**
  * Tomcat HTTP Valve.
  * Monitor HTTP request durations.
  * @author gquintana
  */
 public class SimonValve extends ValveBase {
 	/**
 	 * The descriptive information about this implementation.
 	 */
 	private static final String INFO =
 		"org.javasimon.tomcat.SimonValve/3.2";
 	/**
 	 * Simon name prefix
 	 */
 	private String prefix="org.javasimon.tomcat";
 	/**
 	 * Default constructor
 	 */
 	public SimonValve() {
 	}
 	
 	public String getPrefix() {
 		return prefix;
 	}
 	public void setPrefix(String prefix) {
 		this.prefix = prefix;
 	}
 	/**
 	 * Determines whether request should be monitored or not.
 	 * Designed to be overridden
 	 */
 	protected boolean isMonitored(Request request) {
 		return true;
 	}
 	/**
 	 * Get Simon corresponding to request.
 	 * Designed to be overridden
 	 */
 	protected Stopwatch getMonitor(Request request) {
 		String uri=request.getRequestURI();
		// Allowed chars -_[]A-Za-z0-9.,@$%()<>
		String name=prefix+uri.replaceAll("[.:;?!=+*#]", "_").replace('/', '.');
 		Stopwatch stopwatch=SimonManager.manager().getStopwatch(name);
 		if (stopwatch.getNote()==null) {
 			stopwatch.setNote(uri);
 		}
 		return stopwatch;
 	}
 	/**
 	 * Valve main method
 	 */
 	@Override
 	public void invoke(Request request, Response response) throws IOException, javax.servlet.ServletException {
 		if (isMonitored(request)) {
 			Split split=getMonitor(request).start();
 			getNext().invoke(request,response);
 			split.stop();
 		} else {
 			getNext().invoke(request,response);
 		}
 		
 	}
 
 	@Override
 	public String getInfo() {
 		return INFO;
 	}
 	
 }
