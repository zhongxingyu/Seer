 /**
  * 
  */
 package com.saplo.api.client;
 
 import java.util.IllegalFormatException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author progre55
  *
  */
 public class SaploClientException extends Exception {
 
 	private static final long serialVersionUID = -5262362627936830619L;
 	private static Logger logger = LoggerFactory.getLogger(SaploClientException.class);
 	
 	protected int errorCode;
 	protected Throwable cause;
 	
 	/**
 	 * @param message - API-Exception message
 	 */
 	public SaploClientException(String message) {
 		super(message);
 	}
 	
 	/**
 	 * @param code - API-Exception code
 	 */
 	public SaploClientException(int code) {
 		super();
 		this.errorCode = code;
 	}
 
 	/**
 	 * @param message - API-Exception message
 	 * @param code - API-Exception code
 	 */
 	public SaploClientException(String message, int code) {
 		super(message);
 		this.errorCode = code;
 	}
 	
 	public SaploClientException(String message, int errorCode, String... args) {
 		super(format(message, args));
 		this.errorCode = errorCode;
 	}
 
 	public SaploClientException(String message, int errorCode, Integer... args) {
 		super(format(message, args));
 		this.errorCode = errorCode;
 	}
 
 	/**
 	 * 
 	 * @param message - API-Exception message
 	 * @param code - API-Exception code
 	 * @param t - API-Exception cause
 	 */
 	public SaploClientException(String message, int code, Throwable t) {
		super(message, t);
 		this.cause = t;
 		this.errorCode = code;
 	}
 
 	/**
 	 * 
 	 * @param code - Client exception code
 	 * @param t - cause
 	 */
 	public SaploClientException(int code, Throwable t) {
 		super(t.getMessage());
 		this.cause = t;
 		this.errorCode = code;
 	}
 	
 	/**
 	 * @param t - API-Exception cause
 	 */
 	public SaploClientException(Throwable t) {
 		super(t.getMessage());
 		this.cause = t;
 		this.errorCode = -1;
 	}
 
 	/**
 	 * Get the API-Exception code
 	 * @return errorCode 
 	 */
 	public int getErrorCode() {
 		return errorCode;
 	}
 
 	/**
 	 * {@see super#getCause()}
 	 */
 	public Throwable getCause() {
 		return this.cause;
 	}
 
 	private static String format(String message, Integer... fields) {
 		String msg = "";
 		try {
 			msg = String.format(message, (Object[]) fields);
 		} catch (IllegalFormatException ife) {
 			logger.error("Format Exception (msg: " + message + ", flds: "
 					+ fields.length + "): " + ife.getMessage());
 			msg = message;
 		}
 		return msg;
 	}
 	
 	private static String format(String message, String... fields) {
 		String msg = "";
 		try {
 			msg = String.format(message, (Object[]) fields);
 		} catch (IllegalFormatException ife) {
 			logger.error("Format Exception (msg: " + message + ", flds: "
 					+ fields.length + "): " + ife.getMessage());
 			msg = message;
 		}
 		return msg;
 	}
 
 }
