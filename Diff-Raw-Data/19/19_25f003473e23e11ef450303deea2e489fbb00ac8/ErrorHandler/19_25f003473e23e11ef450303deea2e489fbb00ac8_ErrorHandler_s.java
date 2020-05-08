 package ch.codedump.ooplss.utils;
 
 public class ErrorHandler {	
 	private static ErrorHandler instance;
 	
 	private Exception ex;
 	
 	private boolean breakOnError = true;
 	
 	public static ErrorHandler getInstance() {
 		if (instance == null) {
 			instance = new ErrorHandler();
 		}
 		
 		return instance;
 	}
 	
 	private ErrorHandler() {}
 	
 	/**
 	 * Set break on error
 	 * 
 	 * Set whether the execution of the program should be stopped upon an error
 	 * @param v
 	 */
 	public void setBreakOnError(boolean v) {
 		this.breakOnError = v;
 	}
 	
 	/**
 	 * Report an error 
 	 * @param e
 	 */
 	public void reportError(Exception e) {
 		//this.logger.info(e.toString());
 		System.err.println(e.toString());
 		if (this.breakOnError) {
 			System.exit(100);
 		}
 		this.ex = e;
 	}
 	
 	/**
 	 * Throw the recorded exception
 	 * @throws Exception
 	 */
 	public void throwException() throws Exception {
 		if (this.ex != null) {
 			throw this.ex;	
 		}
 	}
 }
