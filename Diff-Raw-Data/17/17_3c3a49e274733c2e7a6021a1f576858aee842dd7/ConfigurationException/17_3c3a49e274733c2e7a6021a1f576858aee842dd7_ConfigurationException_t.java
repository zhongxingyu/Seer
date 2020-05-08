 package edu.sjsu.cs.davsync;
 
 import java.lang.Exception;
 import java.lang.Throwable;
 
 public class ConfigurationException extends Exception {
 
	private static final long serialVersionUID = -4879636486257695816L;
 	private String error = "unknown error";
 
 	public ConfigurationException() {
 		super();
 	}
 
 	public ConfigurationException(String msg) {
 		super(msg);
 		error = msg;
 	}
 
 	public ConfigurationException(String msg, Throwable cause) {
 		super(msg, cause);
 		error = msg;
 	}
 
 	public ConfigurationException(Throwable cause) {
 		super(cause);
 	}
 
 	public String toString() {
 		return new String(error);
 	}
 }
