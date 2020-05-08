 package ch.cern.atlas.apvs.domain;
 
public class APVSException extends Exception {
 
 	private static final long serialVersionUID = 3520019113105842483L;
 
 	public APVSException() {
 	}
 
 	public APVSException(String msg) {
 		super(msg);
 	}
 
 	public APVSException(Throwable t) {
 		super(t);
 	}
 	
 	public APVSException(String msg, Throwable t) {
 		super(msg, t);
 	}
 }
