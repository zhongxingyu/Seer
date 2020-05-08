 package org.cotrix.io.map;
 
 import static org.cotrix.domain.utils.Utils.*;
 
 
 /**
  * A report on the mapping of a codelist onto a domain object.
  * <p>
  * Reports are thread-locals.
  * 
  * @author Fabio Simeoni
  *
  */
 public class Report {
 
 	//report registry
 	private static InheritableThreadLocal<Report> reports = new InheritableThreadLocal<Report>() {
 		
 		protected Report initialValue() {
 			return new Report();
 		};
 	};
 	
 	/**
 	 * Returns the current report.
 	 * @return
 	 */
 	public static Report report() {
 		return reports.get();
 	}
 	
 	private final double start = System.currentTimeMillis();
 	private final StringBuilder log = new StringBuilder();
 	private boolean failure;
 	
 	//create only throuugh factory method
 	private Report() {}
 	
 	/**
 	 * Adds a message to this report.
 	 * @param message the message
 	 */
 	public void log(String message) {
 		valid("message",message);
 		log.append("\n["+time()+"s] "+message);
 	}
 	
 	/**
 	 * Adds a warning message to this report.
 	 * @param message the report
 	 */
 	public void logWarning(String message) {
 		log("WARNING:"+message);
 	}
 	
 	/**
 	 * Adds an error message to this report.
 	 * @param message the report
 	 */
 	public void logError(String message) {		
		log("ERROR:"+message);
 		failure=true;
 	}
 	
 	/**
 	 * Closes the report.
 	 */
 	public void close() {
 		reports.remove(); //cleanup thread-local
 	}
 	
 	/**
 	 * Returns <code>true</code> if the report contains errors.
 	 * @return <code>true</code> if the report contains errors.
 	 */
 	public boolean isFailure() {
 		return failure;
 	}
 	
 	@Override
 	public String toString() {
 		return log.toString();
 	}
 	
 	private double time() {
 		return (System.currentTimeMillis()-start)/1000;
 	}
 	
 }
