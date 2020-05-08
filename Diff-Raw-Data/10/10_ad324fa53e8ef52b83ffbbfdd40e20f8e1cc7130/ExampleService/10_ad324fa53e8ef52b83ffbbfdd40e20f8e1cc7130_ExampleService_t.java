 package com.chariot.simpleutil;
 
 /**
  * {@link Service} with hard-coded input data.
  */
 public class ExampleService implements Service {
 	String message;
 
 	@Override
 	public void setMessage(String message) {
 		this.message = message;
 	}
 
 	@Override
 	public String getMessage() {
 		return message != null ? message : "Hello world!";
 	}
 
 	@Override
 	public String toString() {
		return "ExampleService [message=" + getMessage() + "]";
 	}
 }
