 package org.concord.sensor.labquest.jna;
 
 @SuppressWarnings("serial")
 public class LabQuestException extends Exception {
 
 	public LabQuestException() {
 		super();
 	}
 	
	public LabQuestException(String msg) {
		super(msg);
	}

 	public LabQuestException(Throwable cause) {
 		super(cause);
 	}
 	
 }
