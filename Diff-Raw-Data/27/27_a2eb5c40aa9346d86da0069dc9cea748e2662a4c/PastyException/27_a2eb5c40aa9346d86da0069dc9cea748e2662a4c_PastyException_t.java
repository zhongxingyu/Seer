 package de.electricdynamite.pasty;
 
 public class PastyException extends Exception {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8439075085500583159L;
 
 	public static final short ERROR_UNKNOWN = 0;
 	public static final short ERROR_MISC = 1;
 	public static final short ERROR_AUTHORIZATION_FAILED = 3;
 	public static final short ERROR_ILLEGAL_RESPONSE = 4;
 	public static final short ERROR_IO_EXCEPTION = 5;
	public static final short ERROR_NO_CACHE_EXCEPTION = 6;
 	
 	public short errorId;
 	
 	public PastyException() {
 	}
 
 	public PastyException(String msg) {
 	    super(msg);
 	}
 	
 	public PastyException(short errorId, String msg) {
 		super(msg);
 		this.errorId = errorId;
 	}
 	
 	public PastyException(short errorId) {
 		super();
 		this.errorId = errorId;
 	}
 }
